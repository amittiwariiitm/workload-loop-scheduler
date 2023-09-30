package elasticBarrier;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import imop.ast.node.external.BarrierDirective;
import imop.ast.node.external.CompoundStatement;
import imop.ast.node.external.Declaration;
import imop.ast.node.external.ForConstruct;
import imop.ast.node.external.FunctionDefinition;
import imop.ast.node.external.Node;
import imop.ast.node.external.ParallelConstruct;
import imop.ast.node.external.Statement;
import imop.lib.builder.Builder;
import imop.lib.transform.updater.InsertImmediatePredecessor;
import imop.lib.transform.updater.InsertImmediateSuccessor;
import imop.lib.util.Misc;
import imop.parser.FrontEnd;
import imop.parser.Program;

public class TimingProfiler {

	static Map<Node, String> tidMap = new HashMap<>();

	public static boolean returnTrueForTidPlacementInParallelConstruct(Node parConstruct) {
		Set<Class<? extends Node>> setOfClass = new HashSet<>();
		setOfClass.add(ForConstruct.class);
		setOfClass.add(BarrierDirective.class);
	
		for (Node ompForOrBarrier : Misc.getExactEnclosee(parConstruct, setOfClass)) {
			if (Misc.getEnclosingNode(ompForOrBarrier, ParallelConstruct.class) == parConstruct) {
				return true;
			}
		}
		return false;
	}

	public static boolean returnTrueForTidPlacementInFunc(Node function) {
		Set<Class<? extends Node>> setOfClass = new HashSet<>();
		setOfClass.add(ForConstruct.class);
		setOfClass.add(BarrierDirective.class);
		for (Node ompForOrBarrier : Misc.getExactEnclosee(function, setOfClass)) {
			if (Misc.getEnclosingNode(ompForOrBarrier, ParallelConstruct.class) == null) {
				return true;
			}
		}
		return false;
	}

	public static void repopulateTidMap() {
		Set<Class<? extends Node>> classSet = new HashSet<>();
		classSet.add(ParallelConstruct.class);
		classSet.add(FunctionDefinition.class);
		for (Node node : Misc.getInheritedEnclosee(Program.getRoot(), classSet)) {
			CompoundStatement body = (CompoundStatement) node.getInfo().getCFGInfo().getBody();
	
			for (String var : body.getInfo().getSymbolTable().keySet()) {
				if (var.startsWith("tidElastic")) {
					System.out.println("Found tid var : " + var);
					tidMap.put(node, var);
				}
			}
		}
	}

	public static void addIMsuiteTimers() {
	
		Set<Class<? extends Node>> funcOrParallelClass = new HashSet<>();
		funcOrParallelClass.add(FunctionDefinition.class);
		funcOrParallelClass.add(ParallelConstruct.class);
		boolean placeTid = false;
		for (Node node : Misc.getExactEnclosee(Program.getRoot(), funcOrParallelClass)) {
			if (node instanceof FunctionDefinition) {
				placeTid = returnTrueForTidPlacementInFunc(node);
	
				if (placeTid) {
					String nameTid = Builder.getNewTempName("tidElastic");
					String declare = "int " + nameTid + ";";
					Declaration decTid = FrontEnd.parseAndNormalize(declare, Declaration.class);
					InsertImmediateSuccessor.insert(((FunctionDefinition) node).getInfo().getCFGInfo().getBody()
							.getInfo().getCFGInfo().getNestedCFG().getBegin(), decTid);
					String printTid = nameTid + " = " + "omp_get_thread_num();";
					Statement tidStmt = FrontEnd.parseAndNormalize(printTid, Statement.class);
					InsertImmediateSuccessor.insert(decTid, tidStmt);
					tidMap.put(node, nameTid);
	
				}
			} else if (node instanceof ParallelConstruct) {
				placeTid = returnTrueForTidPlacementInParallelConstruct(node);
				if (placeTid) {
					String nameTid = Builder.getNewTempName("tidElastic");
					String declare = "int " + nameTid + ";";
					Declaration decTid = FrontEnd.parseAndNormalize(declare, Declaration.class);
					InsertImmediateSuccessor.insert(((ParallelConstruct) node).getInfo().getCFGInfo().getBody()
							.getInfo().getCFGInfo().getNestedCFG().getBegin(), decTid);
					String printTid = nameTid + " = " + "omp_get_thread_num();";
					Statement tidStmt = FrontEnd.parseAndNormalize(printTid, Statement.class);
					InsertImmediateSuccessor.insert(decTid, tidStmt);
					tidMap.put(node, nameTid);
				}
			} else {
				assert (false);
			}
		}
	
		int loopOrBarrId = 0;
		Set<Class<? extends Node>> setOfClass = new HashSet<>();
		setOfClass.add(ForConstruct.class);
		setOfClass.add(BarrierDirective.class);
	
		for (Node n : Misc.getExactPostOrderEnclosee(Program.getRoot(), setOfClass)) {
	
			// Step 1: Adding comments & Declaration of timers
	
			if (n instanceof ForConstruct) {
				loopOrBarrId++;
				n.getInfo().getComments().add("loop_" + loopOrBarrId);
			} else {
				loopOrBarrId++;
				n.getInfo().getComments().add("barrier_" + loopOrBarrId);
			}
			String nameStart = Builder.getNewTempName("start");
			String start = "double " + nameStart + ";";
			Declaration dec = FrontEnd.parseAndNormalize(start, Declaration.class);
			InsertImmediatePredecessor.insert(n, dec);
	
			// Step 2: Starting the timer
	
			String nameStartStmt = nameStart + " = " + "omp_get_wtime();";
			Statement stmt = FrontEnd.parseAndNormalize(nameStartStmt, Statement.class);
			InsertImmediateSuccessor.insert(dec, stmt);
	
			// Step 3: Declaration & Ending the timer
	
			String nameEnd = Builder.getNewTempName("end");
			String end = "double " + nameEnd + ";";
			Declaration dec2 = FrontEnd.parseAndNormalize(end, Declaration.class);
			InsertImmediateSuccessor.insert(n, dec2);
	
			String nameEndStmt = nameEnd + " = " + "omp_get_wtime();";
			Statement stmt2 = FrontEnd.parseAndNormalize(nameEndStmt, Statement.class);
			InsertImmediateSuccessor.insert(dec2, stmt2);
	
			// Step 4: Printing the difference in timing
			String nameUse = nameEnd + "=" + nameEnd + "-" + nameStart + ";";
			Statement stmt3 = FrontEnd.parseAndNormalize(nameUse, Statement.class);
			InsertImmediateSuccessor.insert(stmt2, stmt3);
	
			String printer;
			if (n instanceof ForConstruct) {
				String tidName = returnTidName(n);
				printer = "printf(\"%d\\tL%d\\t%.10lf\\n\"," + tidName + "," + loopOrBarrId + "," + nameEnd + ");";
			} else {
				String tidName = returnTidName(n);
				printer = "printf(\"%d\\tB%d\\t%.10lf\\n\"," + tidName + "," + loopOrBarrId + "," + nameEnd + ");";
			}
	
			Statement stmt4 = FrontEnd.parseAndNormalize(printer, Statement.class);
			InsertImmediateSuccessor.insert(stmt3, stmt4);
		}
	
	}

	public static String returnTidName(Node forOrBarr) {
	
		ParallelConstruct parCons = Misc.getEnclosingNode(forOrBarr, ParallelConstruct.class);
	
		if (parCons != null) {
			return tidMap.get(parCons);
		} else {
			FunctionDefinition func = Misc.getEnclosingFunction(forOrBarr);
			return tidMap.get(func);
		}
	}

}
