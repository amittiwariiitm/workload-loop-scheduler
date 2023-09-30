package elasticBarrier;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import imop.Main;
import imop.ast.node.external.BarrierDirective;
import imop.ast.node.external.CompoundStatement;
import imop.ast.node.external.Declaration;
import imop.ast.node.external.ForConstruct;
import imop.ast.node.external.ForStatement;
import imop.ast.node.external.FunctionDefinition;
import imop.ast.node.external.IfStatement;
import imop.ast.node.external.Node;
import imop.ast.node.external.ParallelConstruct;
import imop.ast.node.external.ReturnStatement;
import imop.ast.node.external.Statement;
import imop.ast.node.internal.PreCallNode;
import imop.lib.builder.Builder;
import imop.lib.cfg.CFGLinkFinder;
import imop.lib.cfg.link.node.CFGLink;
import imop.lib.cfg.link.node.CompoundElementLink;
import imop.lib.transform.BasicTransform;
import imop.lib.transform.simplify.FunctionInliner;
import imop.lib.transform.simplify.ParallelConstructExpander;
import imop.lib.transform.simplify.RedundantSynchronizationRemoval;
import imop.lib.transform.simplify.RedundantSynchronizationRemovalForYA;
import imop.lib.transform.updater.InsertImmediatePredecessor;
import imop.lib.transform.updater.InsertImmediateSuccessor;
import imop.lib.util.DumpSnapshot;
import imop.lib.util.Misc;
import imop.parser.FrontEnd;
import imop.parser.Program;

public class ElasticOptimizationDriver {
	static int id = 0;
	static String labelSubstr = "LStartEnd";

	public static void putLabelAcrossIndependentCode() {
		IndependentLabelPutter ilp = new IndependentLabelPutter();
		for (ForConstruct fc: Misc.getExactEnclosee(Program.getRoot(), ForConstruct.class)) {
			fc.accept(ilp);
		}
		DumpSnapshot.forceDumpRoot("label");
		System.out.println("\t\tSuccessfully elastruct-labeled. Yay!\n");
		System.exit(0);
	}

	public static void putLabelPrintfTimer() {
		Utilities.addOmpGetWTime();
		Utilities.addFprintf();
		IndependentPrintfPutter ipp = new IndependentPrintfPutter();
		Declaration fileDec = FrontEnd.parseAndNormalize("FILE *independentFile;", Declaration.class);
		Builder.addDeclarationBeforeFirstFunction(fileDec);
		Statement fileCreate = FrontEnd.parseAndNormalize(
				"independentFile = fopen(\"independentCost_" + Program.fileName + ".txt\",\"w\");", Statement.class);
		CompoundStatement mainBody = Program.getRoot().getInfo().getMainFunction().getInfo().getCFGInfo().getBody();
		mainBody.getInfo().getCFGInfo().addElement(0, fileCreate);

		String checkFile = "if (independentFile == ((void*) 0)) {printf(\"Unable to open the file\\n\");return 1;}";
		IfStatement fileCheck = FrontEnd.parseAndNormalize(checkFile, IfStatement.class);
		InsertImmediateSuccessor.insert(fileCreate, fileCheck);

		FunctionDefinition funcDef = Program.getRoot().getInfo().getMainFunction();

		for (ReturnStatement retStmt : Misc.getExactEnclosee(funcDef, ReturnStatement.class)) {
			Statement fileClose = FrontEnd.parseAndNormalize("fclose(independentFile);", Statement.class);
			InsertImmediatePredecessor.insert(retStmt, fileClose);
		}

		for (ForConstruct fc : Misc.getExactEnclosee(Program.getRoot(), ForConstruct.class)) {
			fc.accept(ipp);
		}
		DumpSnapshot.forceDumpRoot("printf");
		System.out.println("\t\tSuccessfully ela-label-printed. Yay!\n");
		System.exit(0);
	}

	public static void addTimerInvoke() {

		TimingProfiler.addIMsuiteTimers();
//		TimingProfiler.repopulateTidMap();

		DumpSnapshot.forceDumpRoot("tid-timer");
		System.out.println("\t\tSuccessfully added the timers. Yay!\n");
	}

	public static void startEndConverter() {
		for (ForConstruct fc : Misc.getInheritedEnclosee(Program.getRoot(), ForConstruct.class)) {
			CostExpressionGenerator.convertToStartEnd(fc);
		}
		System.out.println("\t\tSuccessfully generated start-end. Yay!\n");
		DumpSnapshot.forceDumpRoot("startend");
		System.exit(0);
	}

	public static void startEndConverterSensitive() {
		for (ForConstruct fc : Misc.getInheritedPostOrderEnclosee(Program.getRoot(), ForConstruct.class)) {
			CostExpressionGeneratorSensitive.convertToStartEndSensitive(fc);
		}
		RedundantSynchronizationRemovalForYA.removeBarriers(Program.getRoot());
		System.out.println("\t\tSuccessfully generated start-end-sensitive. Yay!\n");
		DumpSnapshot.forceDumpRoot("startendsensitive");
		System.exit(0);
	}
}
