/*
 * Copyright (c) 2019 Aman Nougrahiya, V Krishna Nandivada, IIT Madras.
 * This file is a part of the project IMOP, licensed under the MIT license.
 * See LICENSE.md for the full text of the license.
 * 
 * The above notice shall be included in all copies or substantial
 * portions of this file.
 */
package elasticBarrier;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Queue;
import java.util.Set;

import imop.ast.annotation.Label;
import imop.ast.annotation.SimpleLabel;
import imop.ast.node.external.*;
import imop.ast.node.internal.*;
import imop.baseVisitor.GJNoArguDepthFirstProcess;
import imop.baseVisitor.GJVoidDepthFirstProcess;
import imop.baseVisitor.cfgTraversals.GJDepthFirstCFG;
import imop.baseVisitor.cfgTraversals.GJNoArguDepthFirstCFG;
import imop.lib.analysis.flowanalysis.Symbol;
import imop.lib.builder.Builder;
import imop.lib.cfg.CFGLinkFinder;
import imop.lib.cfg.link.node.CompoundElementLink;
import imop.lib.transform.BasicTransform;
import imop.lib.transform.updater.InsertImmediatePredecessor;
import imop.lib.transform.updater.InsertImmediateSuccessor;
import imop.lib.transform.updater.NodeRemover;
import imop.lib.transform.updater.NodeReplacer;
import imop.lib.util.CellList;
import imop.lib.util.CellSet;
import imop.lib.util.Misc;
import imop.parser.FrontEnd;
import imop.parser.Program;

/**
 * Call this visitor from ForConstruct Body instead of ForConstruct. <br>
 * For now, assume number of iterations in While is 1.
 */
public class CostExpressionGenerator {
	/**
	 * return writes of cer after connecting it & then removing it later.
	 * 
	 * @param node
	 * @param insertionPoint
	 * @return
	 */
	public static CellSet getFreeCellsAfterConnecting(CostExpressionReturn node, Node insertionPoint) {
		CellSet retSet = new CellSet();
		for (Node elem : node.code) {
			CompoundElementLink ceLink = (CompoundElementLink) CFGLinkFinder.getCFGLinkFor(insertionPoint);
			ceLink.enclosingNonLeafNode.getInfo().getCFGInfo().addElement(ceLink.getIndex(), elem);
			retSet.addAll(Misc.getFreeCells(elem));
			NodeRemover.removeNode(elem);
		}
		return retSet;
	}

	/**
	 * return reads of cer after connecting it & then removing it later.
	 * 
	 * @param node
	 * @param insertionPoint
	 * @return
	 */
	public static CellSet getReadsAfterConnecting(CostExpressionReturn node, Node insertionPoint) {
		CellSet retSet = new CellSet();
		for (Node elem : node.code) {
			CompoundElementLink ceLink = (CompoundElementLink) CFGLinkFinder.getCFGLinkFor(insertionPoint);
			ceLink.enclosingNonLeafNode.getInfo().getCFGInfo().addElement(ceLink.getIndex(), elem);
			// TODO: Check this for a bug where node is not node anymore.
			retSet.addAll(new CellSet(elem.getInfo().getReads()));
			NodeRemover.removeNode(elem);
		}
		return retSet;
	}

	/**
	 * return writes of cer after connecting it & then removing it later.
	 * 
	 * @param node
	 * @param insertionPoint
	 * @return
	 */
	public static CellSet getWritesAfterConnecting(CostExpressionReturn node, Node insertionPoint) {
		CellSet retSet = new CellSet();
		for (Node elem : node.code) {
			CompoundElementLink ceLink = (CompoundElementLink) CFGLinkFinder.getCFGLinkFor(insertionPoint);
			ceLink.enclosingNonLeafNode.getInfo().getCFGInfo().addElement(ceLink.getIndex(), elem);
			// TODO: Check this for a bug where node is not node anymore.
			retSet.addAll(new CellSet(elem.getInfo().getWrites()));
			NodeRemover.removeNode(elem);
		}
		return retSet;
	}

	/**
	 * return openReads with empty mustWrites after connecting it & removing it
	 * later.`
	 * 
	 * @param node
	 * @param insertionPoint
	 * @return
	 */
	public static CellSet getOprOutPhiAfterConnecting(CostExpressionReturn node, Node insertionPoint) {
		CellSet retSet = new CellSet();
		CellSet fsw = new CellSet();
		for (Node elem : node.code) {
			CompoundElementLink ceLink = (CompoundElementLink) CFGLinkFinder.getCFGLinkFor(insertionPoint);
			ceLink.enclosingNonLeafNode.getInfo().getCFGInfo().addElement(ceLink.getIndex(), elem);
			// TODO: Check this for a bug where node is not node anymore.
			CellSet retElem = OpenReadsGetter.getOpenReads(elem, fsw);
//					elem.accept(new OpenReadsGetter(), fsw);
			if (retElem != null) {
				retSet.addAll(retElem);
			}
			CellList fswElem = MustWritesGetter.getMustWrites(elem);
			if (fswElem != null) {
				fsw.addAll(fswElem);
			}
			NodeRemover.removeNode(elem);
		}
		return retSet;
	}

	public static CostExpressionReturn obtainCERForInsertionPoint(Node baseElement, int insertionIndex,
			boolean isThisInvocationDoneForFindingCostExpressionNotCountExpression) {
		/*
		 * Populate CEA & other data structures while traversing from IP to baseElement
		 * (for eg. from #pob to #pof)
		 * 
		 */
		List<CellSet> stmtOpRList = new ArrayList<>();
		List<CellSet> stmtOpRPhiList = new ArrayList<>();
		List<CellSet> stmtFswPhiList = new ArrayList<>();
		List<CellSet> stmtFreeVarPhiList = new ArrayList<>();
		CompoundElementLink cel = (CompoundElementLink) CFGLinkFinder.getCFGLinkFor(baseElement);
		CompoundStatement cs = (CompoundStatement) cel.getEnclosingNode();

		CostExpressionArgument ceaTemp = new CostExpressionArgument();
		for (int elemIndex = insertionIndex; elemIndex <= cel.getIndex() - 1; elemIndex++) {
			Node elem = cs.getInfo().getCFGInfo().getElementList().get(elemIndex);

			ceaTemp.openReads.addAll(OpenReadsGetter.getOpenReads(elem, ceaTemp.mustWrites));
			ceaTemp.mustWrites.addAll(MustWritesGetter.getMustWrites(elem));

			stmtOpRList.add(new CellSet(ceaTemp.openReads));
			stmtOpRPhiList.add(OpenReadsGetter.getOpenReads(elem, new CellSet()));
			stmtFswPhiList.add(new CellSet(MustWritesGetter.getMustWrites(elem))); // Since this is phi, we do not
																						// use ceaTemp.fsw.
			stmtFreeVarPhiList.add(Misc.getFreeCells(elem));
		}
		CostExpressionReturn cer;
		if (isThisInvocationDoneForFindingCostExpressionNotCountExpression) {
			assert (baseElement instanceof ForConstruct);
			ForConstruct fc = (ForConstruct) baseElement;
			cer = CostExpressionGenerator.getCostExpression(fc.getInfo().getCFGInfo().getBody(), ceaTemp);
		} else {
			cer = Utilities.getCountforForHeader(baseElement);
		}

		if (cel.getIndex() == insertionIndex) {
			// If there are no elements in between the insertion point and the forCons
			return cer;
		}

		/*
		 * if WriteSet(CER(baseElement)) intersects with opRSetOut(baseElement),
		 * then throw Exception.
		 * L = opRSetPhi(CER(baseElement))
		 * FV = freeVarPhi(CER(baseElement))
		 * For each S(y) in (baseElementIndex-1) to insertionIndex:
		 * ,,,,boolean considerForMark = false;
		 * ,,,,if (L intersects with WriteSet(S(y)))
		 * ,,,,then considerForMark = true;
		 * ,,,,
		 * ,,,,if (S(y) declares any node in FV)
		 * ,,,,then considerForMark = true;
		 * ,,,,
		 * ,,,,if (!considerForMark)
		 * ,,,,then continue;
		 * ,,,,
		 * ,,,,if WriteSet(S(y)) intersects with opRSetOut(S(y))
		 * ,,,,,,,OR S(y) contains any prints,
		 * ,,,,then throw Exception.
		 * ,,,,
		 * ,,,,CER.code.add(0, S(y))
		 * ,,,,L = L - FSWPhi(S(y)) union opRSetPhi(S(y))
		 * ,,,,FV = FV - decl(S(y)) union freeVarPhi(S(y))
		 */
		if (Misc.doIntersect(CostExpressionGenerator.getWritesAfterConnecting(cer, baseElement),
				stmtOpRList.get(cel.getIndex() - 1 - insertionIndex))) {
			Misc.exitDueToError(
					"Found conflict between CostExp write & OpenRead of any elem between WLLoop to this point: "
							+ cs.getInfo().getCFGInfo().getElementList().get(cel.getIndex() - 1));
		}

		CellSet setL = new CellSet(CostExpressionGenerator.getOprOutPhiAfterConnecting(cer, baseElement));
		CellSet setFV = new CellSet(CostExpressionGenerator.getFreeCellsAfterConnecting(cer, baseElement));

		for (int j = cel.getIndex() - 1; j >= insertionIndex; j--) {
			Node elem = cs.getInfo().getCFGInfo().getElementList().get(j);
			boolean considerForMarking = false;

			if (Misc.doIntersect(setL, elem.getInfo().getWrites())) {
				considerForMarking = true;
			}

			if (elem instanceof Declaration) {
				Declaration decl = (Declaration) elem;
				if (setFV.contains(decl.getInfo().getDeclaredSymbol())) {
					considerForMarking = true;
				}
			}

			if (!considerForMarking) {
				continue;
			}

			if (Misc.doIntersect(elem.getInfo().getWrites(), stmtOpRList.get(j - insertionIndex))) {
				Misc.exitDueToError(
						"Found conflict between Stmt (to be copied) write & OpenRead of any elem between WLLoop to this point: "
								+ elem);

			}

			cer.code.add(0, elem);
			setL.removeAll(stmtFswPhiList.get(j - insertionIndex));
			setL.addAll(stmtOpRPhiList.get(j - insertionIndex));
			if (elem instanceof Declaration) {
				Declaration decl = (Declaration) elem;
				setFV.remove(decl.getInfo().getDeclaredSymbol());
			}
			setFV.addAll(stmtFreeVarPhiList.get(j - insertionIndex));
		}

		return cer;
	}

	public static int getInsertionPointBarrierIndex(Node baseElement) {
		CompoundElementLink cel = (CompoundElementLink) CFGLinkFinder.getCFGLinkFor(baseElement);
		List<Node> elemList = ((CompoundStatement) cel.getEnclosingNode()).getInfo().getCFGInfo().getElementList();
		int baseElemIndex = cel.getIndex();

		for (int i = baseElemIndex - 1; i >= 0; i--) {
			Node prevNode = elemList.get(i);

			if (prevNode instanceof BarrierDirective) {
				return i;
			}
			if (prevNode.getInfo().hasBarrierInCFG()) {
				break;
			}
		}

		InsertImmediatePredecessor.insert(baseElement,
				FrontEnd.parseAndNormalize("#pragma omp barrier\n", BarrierDirective.class));
		cel = (CompoundElementLink) CFGLinkFinder.getCFGLinkFor(baseElement);
		int newBaseElemIndex = cel.getIndex();
		return newBaseElemIndex - 1;
	}

	public static void convertToStartEnd(ForConstruct forCons) {
		/*
		 * Step 0: Return if this forCons contains an omp-ordered
		 */
		for (Node leaf : forCons.getInfo().getCFGInfo().getIntraTaskCFGLeafContents()) {
			if (!(leaf instanceof BeginNode)) {
				continue;
			}
			BeginNode bNode = (BeginNode) leaf;
			if (bNode.getParent() instanceof OrderedConstruct) {
				Misc.warnDueToLackOfFeature("Not converting loops with ordered-construct to start-end.", forCons);
				return;
			}
		}

		/*
		 * Step 1: Traverse backwards until you find a #pob.
		 * ,,,,,,,,Step 1a: If found, it is our Insertion point (IP) (point above which
		 * ,,,,,,,,,,,,,,,,,we populate the WLLoop (Cost Loop))
		 * ,,,,,,,,Step 1b: Else, once you find the start of #pop, create a #pob below
		 * ,,,,,,,,,,,,,,,,,it & repeat the 1a process.
		 */
		int insertionIndexForWLLoop = getInsertionPointBarrierIndex(forCons);

		/*
		 * Step 1.2: If there exists any read in omp-for-header which is being
		 * written
		 * or declared in between #pof and #pob, then throw an exception.
		 */

		/*
		 * Step 2: Above the insertion point, create a WLLoop (#pof) with:
		 * ,,,,,,,,Step 2a: Suitable forCons header.
		 */
		String pofStr = "#pragma omp for nowait\n" + forCons.getF2() + "{}";
		Statement wlLoop = FrontEnd.parseAndNormalize(pofStr, Statement.class);
		CompoundElementLink cel = (CompoundElementLink) CFGLinkFinder.getCFGLinkFor(forCons);
		CompoundStatement cs = (CompoundStatement) cel.getEnclosingNode();
		cs.getInfo().getCFGInfo().addElement(insertionIndexForWLLoop, wlLoop);

		/*
		 * Step 3: Declaration of WLLoop: Globally.
		 */
		String wlLoopName = Builder.getNewTempName("WlLoop");
		String wlLoopDecl = "double *" + wlLoopName + ";";
		Builder.addDeclarationBeforeFirstFunction(FrontEnd.parseAndNormalize(wlLoopDecl, Declaration.class));

		/*
		 * Step 4: Paste the malloc for init of WLLoop.
		 * ,,,,,,,,Step 4a: for every given forCons, maintain its CountVar & associated
		 * ,,,,,,,,list of Statements (just like we
		 * ,,,,,,,,,,,,,,,,,calculated it for ForStmt).
		 * ,,,,,,,,Step 4b: Use this forCons CountVar in the malloc of WLLoop.
		 * ,,,,,,,,Step 4c: Find another #pob traversing backwards (or upwards)
		 * ,,,,,,,,Step 4d: Paste the malloc of WLLoop before this #pob, in #pom.
		 * ,,,,,,,,,,,,,,,,,or create new #pob if start of #pop is found earlier.
		 * ,,,,,,,,,,,,,,,,,Step 4d1: Paste CountVar decl inside #pom
		 * ,,,,,,,,,,,,,,,,,Step 4d2: Paste CountVar stmts inside #pom after 4d1.
		 * ,,,,,,,,,,,,,,,,,Step 4d3: Use CountVar in the malloc.
		 */
		int insertionIndexForMalloc = CostExpressionGenerator.getInsertionPointBarrierIndex(wlLoop);
		CostExpressionReturn mallocCER = CostExpressionGenerator.obtainCERForInsertionPoint(forCons,
				insertionIndexForMalloc, false);
		String masterForMallocString = "#pragma omp master\n {";
		masterForMallocString += "int " + mallocCER.costVarName + ";";
		for (Node mallocTempNode : mallocCER.code) {
			masterForMallocString += mallocTempNode.toString() + "\n";
		}

		String tStr = Builder.getNewTempName();
		String bStr = Builder.getNewTempName();
		masterForMallocString += "int " + tStr + ";";
		masterForMallocString += "void* " + bStr + ";";
		masterForMallocString += tStr + " = " + "sizeof(double) * " + mallocCER.costVarName + ";";
		masterForMallocString += bStr + " = " + "malloc(" + tStr + ");";
		masterForMallocString += wlLoopName + " = (double *)" + bStr + ";\n}";

		Statement masterStmt = FrontEnd.parseAndNormalize(masterForMallocString, Statement.class);
		cs.getInfo().getCFGInfo().addElement(insertionIndexForMalloc, masterStmt);

		/*
		 * Step 5: Find CER of forCons from forCons to 1st #pob: Traverse downwards from
		 * ,,,,,,,,the 1st #pob till the given forCons & populate CEA.
		 * ,,,,,,,,Step 5a: CEA is populated like it is populated inside the visit of
		 * ,,,,,,,,,,,,,,,,,CompoundStatement,
		 * ,,,,,,,,,,,,,,,,,i.e., updating OpenReads & MustWrites suitably.
		 * Step 6: Find final CER for the forCons body.
		 * Step 7: Employ the same process as that applied for marking list of Stmts
		 * ,,,,,,,,inside the visit of CompoundStatement as when taking the
		 * ,,,,,,,,forCons-body-CER to IP
		 */
		insertionIndexForWLLoop = getInsertionPointBarrierIndex(forCons);
		CostExpressionReturn wlLoopCER = CostExpressionGenerator.obtainCERForInsertionPoint(forCons,
				insertionIndexForWLLoop, true);

		/*
		 * Step 8: For each iter in WLLoop body, we have:
		 * ,,,,,,,,Step 8a: a dec for CostExp var, attached just above the list of Stmt.
		 * ,,,,,,,,Step 8b: a list of Stmt, paste them inside the WLLoop.
		 * ,,,,,,,,Step 8c: a CostExp var, paste it like WL[i] = var;
		 * ,,,,,,,,Step 8d: Rename induction variable.
		 */
		String wlLoopBodyStr = "{";

		wlLoopBodyStr += "double " + wlLoopCER.costVarName + ";";
		for (Node cerElem : wlLoopCER.code) {
			wlLoopBodyStr += cerElem + "\n";
		}
		String indVarName = CostExpressionGenerator.getInductionVarName(forCons);
		wlLoopBodyStr += wlLoopName + "[" + indVarName + "] = " + wlLoopCER.costVarName + ";";
		wlLoopBodyStr += "}";

		Statement wlLoopBody = FrontEnd.parseAndNormalize(wlLoopBodyStr, Statement.class);
		ForConstruct wlFC = (ForConstruct) Misc.getCFGNodeFor(wlLoop);
		wlFC.getInfo().getCFGInfo().setBody(wlLoopBody);
		wlFC.getInfo().removeExtraScopes();

		HashMap<String, String> renamingMap = new HashMap<>();
		String newIndVarName = Builder.getNewTempName(indVarName);
		renamingMap.put(indVarName, newIndVarName);

		Statement newWLLoop = (Statement) BasicTransform.obtainRenamedNode(wlLoop, renamingMap);
		NodeReplacer.replaceNodes(wlLoop, newWLLoop);

		InsertImmediatePredecessor.insert(newWLLoop,
				FrontEnd.parseAndNormalize("int " + newIndVarName + ";", Declaration.class));

		/*
		 * Step 9: Emit the CostDivider function globally, if not already present.
		 */
		CostExpressionGenerator.insertGlobalDeclarationsIfNeeded();

		/*
		 * Step 10: For the given forCons, populate the Start-End variables, right after
		 * ,,,,,,,,the 1st found #pob traversing backwards using workDivider.
		 * ,,,,,,,, Step 10a: Create following arg var for workDivider()
		 * ,,,,,,,,,,,,,,,,,, Step 10a1 tid
		 * ,,,,,,,,,,,,,,,,,, Step 10a2 ,,,,,,1. get the count expression (with its
		 * ,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,conflicted
		 * ,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,statements) i.e., Count Expression CER
		 * ,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,2. Use the CER cost variable to be given
		 * ,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,as the 2nd arg
		 * ,,,,,,,,,,,,,,,,,, Step 10a3 Pass WLLoop name as 3rd arg
		 * ,,,,,,,,,Step 10b: Invoke workDivider()
		 * 
		 */
		int insertionIndexForStartEnd = getInsertionPointBarrierIndex(forCons) + 1;

		String chunkName = Builder.getNewTempName("chunk");
		String tidName = Builder.getNewTempName("tidSE");
		String startName = Builder.getNewTempName("start");
		String endName = Builder.getNewTempName("end");

		CostExpressionReturn countCer = CostExpressionGenerator.obtainCERForInsertionPoint(forCons,
				insertionIndexForStartEnd, false);
		cs.getInfo().getCFGInfo().addElement(insertionIndexForStartEnd,
				FrontEnd.parseAndNormalize("int " + tidName + ";", Declaration.class));
		cs.getInfo().getCFGInfo().addElement(insertionIndexForStartEnd + 1,
				FrontEnd.parseAndNormalize(tidName + " = omp_get_thread_num();", Statement.class));
		cs.getInfo().getCFGInfo().addElement(insertionIndexForStartEnd + 2,
				FrontEnd.parseAndNormalize("chunk " + chunkName + ";", Declaration.class));
		cs.getInfo().getCFGInfo().addElement(insertionIndexForStartEnd + 3,
				FrontEnd.parseAndNormalize("int " + startName + ";", Declaration.class));
		cs.getInfo().getCFGInfo().addElement(insertionIndexForStartEnd + 4,
				FrontEnd.parseAndNormalize("int " + endName + ";", Declaration.class));
		cs.getInfo().getCFGInfo().addElement(insertionIndexForStartEnd + 5,
				FrontEnd.parseAndNormalize("int " + countCer.costVarName + ";", Declaration.class));

		String countCode = "{";
		for (Node countElem : countCer.code) {
			countCode += countElem.toString() + "\n";
		}
		countCode += "}";
		cs.getInfo().getCFGInfo().addElement(insertionIndexForStartEnd + 6,
				FrontEnd.parseAndNormalize(countCode, Statement.class));
		cs.getInfo().getCFGInfo().addElement(insertionIndexForStartEnd + 7,
				FrontEnd.parseAndNormalize(
						chunkName + " = workDivider(" + tidName + "," + countCer.costVarName + "," + wlLoopName + ");",
						Statement.class));

		cs.getInfo().getCFGInfo().addElement(insertionIndexForStartEnd + 8,
				FrontEnd.parseAndNormalize(startName + " = " + chunkName + ".start;", Statement.class));
		cs.getInfo().getCFGInfo().addElement(insertionIndexForStartEnd + 9,
				FrontEnd.parseAndNormalize(endName + " = " + chunkName + ".end;", Statement.class));

		/*
		 * Step 11: Convert the given forCons to forStmt traversing from Start to End.
		 * ,,,,,,,,for each thread.
		 * ,,,,,,,,Step 11a: Privatise the induction variable.
		 */
		String chunkedForStmtString = "for(" + newIndVarName + " = " + startName + "; " + newIndVarName + "<=" + endName
				+ ";" + newIndVarName + "++)";
		chunkedForStmtString += forCons.getInfo().getCFGInfo().getBody();
		Statement chunkedForStmt = FrontEnd.parseAndNormalize(chunkedForStmtString, Statement.class);
		NodeReplacer.replaceNodes(forCons, chunkedForStmt);
		Statement renamedChunkedForStmt = (Statement) BasicTransform.obtainRenamedNode(chunkedForStmt, renamingMap);
		NodeReplacer.replaceNodes(chunkedForStmt, renamedChunkedForStmt);
		Program.getRoot().getInfo().removeExtraScopes();
	}

	private static boolean insertedGlobals = false;

	private static void insertGlobalDeclarationsIfNeeded() {
		if (insertedGlobals) {
			return;
		}

		insertedGlobals = true;
		String coDiv = "";
		WorkOnImop.deltaName = Builder.getNewTempName("deltaEB");
		coDiv += "float " + WorkOnImop.deltaName + " = " + WorkOnImop.deltaForWorkDiv + ";\n";
		Builder.addDeclarationToGlobals(0, FrontEnd.parseAndNormalize(coDiv, Declaration.class));

		WorkOnImop.totalThreadsName = Builder.getNewTempName("totalThreadsEB");
		coDiv = "int " + WorkOnImop.totalThreadsName + ";\n";
		Builder.addDeclarationToGlobals(1, FrontEnd.parseAndNormalize(coDiv, Declaration.class));

		String popStr = "#pragma omp parallel\n { #pragma omp master\n {" + WorkOnImop.totalThreadsName
				+ " = omp_get_num_threads();}}";
		Statement popStmt = FrontEnd.parseAndNormalize(popStr, Statement.class);
		Program.getRoot().getInfo().getMainFunction().getInfo().getCFGInfo().getBody().getInfo().getCFGInfo()
				.addElement(0, popStmt);

		coDiv = "typedef struct {\n";
		coDiv += "int start;\n";
		coDiv += "int end;\n";
		coDiv += "} chunk;\n";
		Builder.addDeclarationToGlobals(2, FrontEnd.parseAndNormalize(coDiv, Declaration.class));

		coDiv = "typedef struct {\n";
		coDiv += "double costTillNow;\n";
		coDiv += "int j;\n";
		coDiv += "} crossPair;\n";
		Builder.addDeclarationToGlobals(3, FrontEnd.parseAndNormalize(coDiv, Declaration.class));

		coDiv = "crossPair findCrossingIter(double crossingValue, int size, double *WLArray) {\n";
		coDiv += "crossPair retPair;\n";
		coDiv += "retPair.costTillNow = 0.0;\n";
		coDiv += "int j;\n";
		coDiv += "for (j = 0; j < size; j++) {\n";
		coDiv += "if (retPair.costTillNow + WLArray[j] >= crossingValue) {\n";
		coDiv += "retPair.j = j;\n";
		coDiv += "return retPair;\n";
		coDiv += "}\n";
		coDiv += "retPair.costTillNow += WLArray[j];\n";
		coDiv += "}\n";
		coDiv += "retPair.j = size - 1;\n";
		coDiv += "return retPair;\n";
		coDiv += "}\n";
		Builder.addFunctionToGlobal(4, FrontEnd.parseAndNormalize(coDiv, FunctionDefinition.class));

		coDiv = "chunk workDivider(int i, int size, double *WLArray) {\n";
		coDiv += "chunk Qi;\n";
		coDiv += "double WLSum = 0.0;\n";
		coDiv += "int k;\n";
		coDiv += "for (k = 0; k < size; k++) {\n";
		coDiv += "WLSum += WLArray[k];\n";
		coDiv += "}\n";
		coDiv += "double avgCost = WLSum/" + WorkOnImop.totalThreadsName + ";\n";
		coDiv += "double deltaCost =  " + WorkOnImop.deltaForWorkDiv + "* avgCost;\n";
		coDiv += "double iclIMinus1 = (avgCost * (i - 1)) - deltaCost;\n";
		coDiv += "double iclI = (avgCost * i) - deltaCost;\n";
		coDiv += "double iclIPlus1 = (avgCost * (i + 1)) - deltaCost;\n";
		coDiv += "double iclIPlus2 = (avgCost * (i + 2)) - deltaCost;\n";
		coDiv += "crossPair myPair;\n";
		coDiv += "myPair = findCrossingIter(iclI, size, WLArray); \n";
		coDiv += "int j = myPair.j;\n";
		coDiv += "double costTillNow = myPair.costTillNow;\n";
		coDiv += "if (i != 0) {\n";
		coDiv += "if (costTillNow < iclIMinus1) {\n";
		coDiv += "Qi.start = j + 1;\n";
		coDiv += "} else {\n";
		coDiv += "if (costTillNow + WLArray[j] >= iclIPlus1) {\n";
		coDiv += "Qi.start = j;\n";
		coDiv += "} else {\n";
		coDiv += "Qi.start = j + 1;\n";
		coDiv += "}\n";
		coDiv += "}\n";
		coDiv += "} else {\n";
		coDiv += "Qi.start = 0;\n";
		coDiv += "}\n";
		coDiv += "myPair = findCrossingIter(iclIPlus1, size, WLArray);\n";
		coDiv += "j = myPair.j;\n";
		coDiv += "costTillNow = myPair.costTillNow;\n";
		coDiv += "if (i !=" + WorkOnImop.totalThreadsName + "-1) {\n";
		coDiv += "if (costTillNow < iclI) {\n";
		coDiv += "Qi.end = j;\n";
		coDiv += "} else {\n";
		coDiv += "if (costTillNow + WLArray[j] >= iclIPlus2) {\n";
		coDiv += "Qi.end = j - 1;\n";
		coDiv += "} else {\n";
		coDiv += "Qi .end = j ;\n";
		coDiv += "}\n";
		coDiv += "}\n";
		coDiv += "} else {\n";
		coDiv += "Qi.end = size - 1; \n";
		coDiv += "}\n";
		coDiv += "return Qi;\n";
		coDiv += "}\n";
		Builder.addFunctionToGlobal(5, FrontEnd.parseAndNormalize(coDiv, FunctionDefinition.class));

	}

	private static String getInductionVarName(ForConstruct forCons) {
		return forCons.getInfo().getCFGInfo().getInitExpression().getF0().getTokenImage();
	}

	public static CostExpressionReturn getCostExpression(Node node, CostExpressionArgument cea) {
		if (node instanceof Expression) {
			CostExpressionGeneratorInternal accessGetter = new CostExpressionGeneratorInternal();
			return node.accept(accessGetter, cea);
		} else {
			node = Misc.getCFGNodeFor(node);
			if (node == null) {
				Misc.warnDueToLackOfFeature("Cannot obtain cost expression for non-executable statements.", node);
				return null;
			}
			CostExpressionGeneratorInternal accessGetter = new CostExpressionGeneratorInternal();
			return node.accept(accessGetter, cea);
		}
	}

	private static class CostExpressionGeneratorInternal
			extends GJDepthFirstCFG<CostExpressionReturn, CostExpressionArgument> {

//	public static ForConstruct insertWLLoop(ForConstruct fc, Node insertionPoint, String wlName) {
//		CostExpressionReturn cer = fc.getInfo().getCFGInfo().getBody().accept(new CostExpressionGenerator());
//		String iter = fc.getInfo().getCFGInfo().getInitExpression().getF0().getTokenImage();
//		String ofhString = fc.getF2().toString();
//		ForConstruct fcCopy = FrontEnd.parseAndNormalize("#pragma omp for \n \t"+ ofhString +"\n{\n;\n}", ForConstruct.class);
//		InsertImmediateSuccessor.insert(insertionPoint, fcCopy);
//		CompoundStatement fcCopyBody = (CompoundStatement) fcCopy.getInfo().getCFGInfo().getBody();
//		fcCopyBody.getInfo().getCFGInfo().addElement(0, cer.code);
//		String wlExpSting = wlName + "[" + iter + "] = " + cer.cost.toString() + ";\n";
//		ExpressionStatement wlExpStmt = FrontEnd.parseAndNormalize(wlExpSting, ExpressionStatement.class); 
//		InsertImmediateSuccessor.insert(cer.code, wlExpStmt);
//		fcCopyBody.getInfo().removeExtraScopes();
//		Declaration dec = FrontEnd.parseAndNormalize("int " + iter + ";", Declaration.class);
//		InsertImmediatePredecessor.insert(fcCopy, dec);
//		return fcCopy;
//	}

		public CostExpressionReturn initProcess(Node n, CostExpressionArgument cea) {
			assert false;
			return null;
		}

		public CostExpressionReturn endProcess(Node n, CostExpressionArgument cea) {
			assert (false);
			return null;
		}

		/**
		 * @deprecated
		 * @param n
		 * @return
		 */
		@Deprecated
		public CostExpressionReturn processCalls(Node n, CostExpressionArgument cea) {
			return null;
		}

		/**
		 * . * Special Node
		 */
		public CostExpressionReturn visit(BeginNode n, CostExpressionArgument cea) {
			assert (false);
			return null;
		}

		/**
		 * Special Node
		 */
		public CostExpressionReturn visit(EndNode n, CostExpressionArgument cea) {
			assert (false);
			return null;
		}

		/**
		 * f0 ::= ( DeclarationSpecifiers() )?
		 * f1 ::= Declarator()
		 * f2 ::= ( DeclarationList() )?
		 * f3 ::= CompoundStatement()
		 */
		public CostExpressionReturn visit(FunctionDefinition n, CostExpressionArgument cea) {
			CostExpressionArgument ceaTemp = new CostExpressionArgument(cea);
			for (ParameterDeclaration pd : n.getInfo().getCFGInfo().getParameterDeclarationList()) {
				ceaTemp.mustWrites.add(pd.getInfo().getDeclaredSymbol());
			}
			CostExpressionReturn _ret = n.getInfo().getCFGInfo().getBody().accept(this, ceaTemp);
			return _ret;
		}

		/**
		 * f0 ::= DeclarationSpecifiers()
		 * f1 ::= ( InitDeclaratorList() )?
		 * f2 ::= ";"
		 */
		public CostExpressionReturn visit(Declaration n, CostExpressionArgument cea) {
			assert (false);
			return null;
		}

		/**
		 * f0 ::= DeclarationSpecifiers()
		 * f1 ::= ParameterAbstraction()
		 */
		public CostExpressionReturn visit(ParameterDeclaration n, CostExpressionArgument cea) {
			assert (false);
			return null;
		}

		/**
		 * f0 ::= "#"
		 * f1 ::= <UNKNOWN_CPP>
		 */
		public CostExpressionReturn visit(UnknownCpp n, CostExpressionArgument cea) {
			assert (false);
			return null;
		}

		/**
		 * f0 ::= OmpPragma()
		 * f1 ::= ParallelDirective()
		 * f2 ::= Statement()
		 */
		public CostExpressionReturn visit(ParallelConstruct n, CostExpressionArgument cea) {
			assert (false);
			return null;
		}

		/**
		 * f0 ::= "#"
		 * f1 ::= <PRAGMA>
		 * f2 ::= <UNKNOWN_CPP>
		 */
		public CostExpressionReturn visit(UnknownPragma n, CostExpressionArgument cea) {
			assert (false);
			return null;
		}

		/**
		 * f0 ::= <IF>
		 * f1 ::= "("
		 * f2 ::= Expression()
		 * f3 ::= ")"
		 */
		public CostExpressionReturn visit(IfClause n, CostExpressionArgument cea) {
			assert (false);
			return null;
		}

		/**
		 * f0 ::= <NUM_THREADS>
		 * f1 ::= "("
		 * f2 ::= Expression()
		 * f3 ::= ")"
		 */
		public CostExpressionReturn visit(NumThreadsClause n, CostExpressionArgument cea) {
			assert (false);
			return null;
		}

		/**
		 * f0 ::= OmpPragma()
		 * f1 ::= ForDirective()
		 * f2 ::= OmpForHeader()
		 * f3 ::= Statement()
		 */
		public CostExpressionReturn visit(ForConstruct n, CostExpressionArgument cea) {
			Misc.warnDueToLackOfFeature(
					"Not handling CostExpressionGeneration for ForConstruct; " + "call this visitor on its body", n);
			assert (false);
			return null;
		}

		/**
		 * f0 ::= <IDENTIFIER>
		 * f1 ::= "="
		 * f2 ::= Expression()
		 */
		public CostExpressionReturn visit(OmpForInitExpression n, CostExpressionArgument cea) {
			assert (false);
			return null;

		}

		/**
		 * f0 ::= OmpForLTCondition()
		 * | OmpForLECondition()
		 * | OmpForGTCondition()
		 * | OmpForGECondition()
		 */
		public CostExpressionReturn visit(OmpForCondition n, CostExpressionArgument cea) {
			assert (false);
			return null;
		}

		/**
		 * f0 ::= PostIncrementId()
		 * | PostDecrementId()
		 * | PreIncrementId()
		 * | PreDecrementId()
		 * | ShortAssignPlus()
		 * | ShortAssignMinus()
		 * | OmpForAdditive()
		 * | OmpForSubtractive()
		 * | OmpForMultiplicative()
		 */
		public CostExpressionReturn visit(OmpForReinitExpression n, CostExpressionArgument cea) {
			assert (false);
			return null;
		}

		/**
		 * f0 ::= OmpPragma()
		 * f1 ::= <SECTIONS>
		 * f2 ::= NowaitDataClauseList()
		 * f3 ::= OmpEol()
		 * f4 ::= SectionsScope()
		 */
		public CostExpressionReturn visit(SectionsConstruct n, CostExpressionArgument cea) {
			CostExpressionReturn _ret = n.getInfo().getCFGInfo().getBody().accept(this, cea);
			return _ret;
		}

		/**
		 * f0 ::= OmpPragma()
		 * f1 ::= <SINGLE>
		 * f2 ::= SingleClauseList()
		 * f3 ::= OmpEol()
		 * f4 ::= Statement()
		 */
		public CostExpressionReturn visit(SingleConstruct n, CostExpressionArgument cea) {
			CostExpressionReturn _ret = n.getInfo().getCFGInfo().getBody().accept(this, cea);
			return _ret;
		}

		/**
		 * f0 ::= OmpPragma()
		 * f1 ::= <TASK>
		 * f2 ::= ( TaskClause() )*
		 * f3 ::= OmpEol()
		 * f4 ::= Statement()
		 */
		public CostExpressionReturn visit(TaskConstruct n, CostExpressionArgument cea) {
			CostExpressionReturn _ret = n.getInfo().getCFGInfo().getBody().accept(this, cea);
			return _ret;
		}

		/**
		 * f0 ::= <FINAL>
		 * f1 ::= "("
		 * f2 ::= Expression()
		 * f3 ::= ")"
		 */
		public CostExpressionReturn visit(FinalClause n, CostExpressionArgument cea) {
			assert (false);
			return null;
		}

		/**
		 * f0 ::= OmpPragma()
		 * f1 ::= <PARALLEL>
		 * f2 ::= <FOR>
		 * f3 ::= UniqueParallelOrUniqueForOrDataClauseList()
		 * f4 ::= OmpEol()
		 * f5 ::= OmpForHeader()
		 * f6 ::= Statement()
		 */
		public CostExpressionReturn visit(ParallelForConstruct n, CostExpressionArgument cea) {
			assert (false);
			return null;
		}

		/**
		 * f0 ::= OmpPragma()
		 * f1 ::= <PARALLEL>
		 * f2 ::= <SECTIONS>
		 * f3 ::= UniqueParallelOrDataClauseList()
		 * f4 ::= OmpEol()
		 * f5 ::= SectionsScope()
		 */
		public CostExpressionReturn visit(ParallelSectionsConstruct n, CostExpressionArgument cea) {
			assert (false);
			return null;
		}

		/**
		 * f0 ::= OmpPragma()
		 * f1 ::= <MASTER>
		 * f2 ::= OmpEol()
		 * f3 ::= Statement()
		 */
		public CostExpressionReturn visit(MasterConstruct n, CostExpressionArgument cea) {
			CostExpressionReturn _ret = n.getInfo().getCFGInfo().getBody().accept(this, cea);
			return _ret;
		}

		/**
		 * f0 ::= OmpPragma()
		 * f1 ::= <CRITICAL>
		 * f2 ::= ( RegionPhrase() )?
		 * f3 ::= OmpEol()
		 * f4 ::= Statement()
		 */
		public CostExpressionReturn visit(CriticalConstruct n, CostExpressionArgument cea) {
			CostExpressionReturn _ret = n.getInfo().getCFGInfo().getBody().accept(this, cea);
			return _ret;
		}

		/**
		 * f0 ::= OmpPragma()
		 * f1 ::= <ATOMIC>
		 * f2 ::= ( AtomicClause() )?
		 * f3 ::= OmpEol()
		 * f4 ::= ExpressionStatement()
		 */
		public CostExpressionReturn visit(AtomicConstruct n, CostExpressionArgument cea) {
			CostExpressionReturn _ret = n.getInfo().getCFGInfo().getBody().accept(this, cea);
			return _ret;
		}

		/**
		 * f0 ::= OmpPragma()
		 * f1 ::= <FLUSH>
		 * f2 ::= ( FlushVars() )?
		 * f3 ::= OmpEol()
		 */
		public CostExpressionReturn visit(FlushDirective n, CostExpressionArgument cea) {
			assert (false);
			return null;
		}

		/**
		 * f0 ::= OmpPragma()
		 * f1 ::= <ORDERED>
		 * f2 ::= OmpEol()
		 * f3 ::= Statement()
		 */
		public CostExpressionReturn visit(OrderedConstruct n, CostExpressionArgument cea) {
			CostExpressionReturn _ret = n.getInfo().getCFGInfo().getBody().accept(this, cea);
			return _ret;
		}

		/**
		 * f0 ::= OmpPragma()
		 * f1 ::= <BARRIER>
		 * f2 ::= OmpEol()
		 */
		public CostExpressionReturn visit(BarrierDirective n, CostExpressionArgument cea) {
			assert (false);
			return null;
		}

		/**
		 * f0 ::= OmpPragma()
		 * f1 ::= <TASKWAIT>
		 * f2 ::= OmpEol()
		 */
		public CostExpressionReturn visit(TaskwaitDirective n, CostExpressionArgument cea) {
			assert (false);
			return null;
		}

		/**
		 * f0 ::= OmpPragma()
		 * f1 ::= <TASKYIELD>
		 * f2 ::= OmpEol()
		 */
		public CostExpressionReturn visit(TaskyieldDirective n, CostExpressionArgument cea) {
			assert (false);
			return null;
		}

		/**
		 * f0 ::= ( Expression() )?
		 * f1 ::= ";"
		 */
		public CostExpressionReturn visit(ExpressionStatement n, CostExpressionArgument cea) {
			assert (false);
			return null;
		}

		/**
		 * f0 ::= "{"
		 * f1 ::= ( CompoundStatementElement() )*
		 * f2 ::= "}"
		 */
		public CostExpressionReturn visit(CompoundStatement n, CostExpressionArgument cea) {

			List<CostExpressionReturn> cerList = new ArrayList<>();
			List<CellSet> cerWriteList = new ArrayList<>(); // cer is disconnected
			List<CellSet> cerOpRPhiList = new ArrayList<>(); // cer is disconnected
			List<CellSet> cerFreeVarPhiList = new ArrayList<>(); // cer is disconnected
			List<CellSet> stmtOpRList = new ArrayList<>();
			List<CellSet> stmtOpRPhiList = new ArrayList<>();
			List<CellSet> stmtFswPhiList = new ArrayList<>();
			List<CellSet> stmtFreeVarPhiList = new ArrayList<>();
			boolean[] markedStmt = new boolean[n.getInfo().getCFGInfo().getElementList().size()];

			/*
			 * Step 1: Given the FSW & opR, Obtain the CER for each Statement separately
			 * while changing the CEA appropriately.
			 * Populate all lists declared above
			 */

			CostExpressionArgument ceaTemp = new CostExpressionArgument(cea);

			boolean insideIndependentArea = false;
			for (Node elem : n.getInfo().getCFGInfo().getElementList()) {
				if (elem instanceof Declaration || elem instanceof DummyFlushDirective) {
					cerList.add(new CostExpressionReturn(new ArrayList<>(), 0.0, new HashSet<>()));
					cerWriteList.add(new CellSet());
					cerOpRPhiList.add(new CellSet());
					cerFreeVarPhiList.add(new CellSet());

					ceaTemp.openReads.addAll(OpenReadsGetter.getOpenReads(elem, ceaTemp.mustWrites));
					ceaTemp.mustWrites.addAll(MustWritesGetter.getMustWrites(elem));

					stmtOpRList.add(new CellSet(ceaTemp.openReads));
					stmtOpRPhiList.add(OpenReadsGetter.getOpenReads(elem, new CellSet()));
					stmtFswPhiList.add(new CellSet(MustWritesGetter.getMustWrites(elem))); // Since this is phi, we
																								// do not use
																								// ceaTemp.fsw.
					stmtFreeVarPhiList.add(Misc.getFreeCells(elem));
					continue;
				}

				Statement stmt = (Statement) elem;
				boolean foundStart = false;
				boolean foundEnd = false;
				String currLabelName = "";

				if (stmt.getInfo().hasLabelAnnotations()) {
					for (Label l : stmt.getInfo().getLabelAnnotations()) {
						if (l instanceof SimpleLabel) {
							String lName = ((SimpleLabel) l).getLabelName();

							if (lName.startsWith("LIndependent_start")) {
								foundStart = true;
								insideIndependentArea = true;
								currLabelName = lName;
								break;
							} else if (lName.startsWith("LIndependent_end")) {
								foundEnd = true;
								insideIndependentArea = false;
								currLabelName = lName;
								break;
							}
						}
					}
				}

				if (foundStart) {
					double partIndependentCost = Utilities.getIndependentCostForLabel(currLabelName);
					cerList.add(new CostExpressionReturn(new ArrayList<>(), partIndependentCost, new HashSet<>()));
					cerWriteList.add(new CellSet());
					cerOpRPhiList.add(new CellSet());
					cerFreeVarPhiList.add(new CellSet());
				} else if (foundEnd) {
					cerList.add(new CostExpressionReturn(new ArrayList<>(), 0.0, new HashSet<>()));
					cerWriteList.add(new CellSet());
					cerOpRPhiList.add(new CellSet());
					cerFreeVarPhiList.add(new CellSet());
				} else {
					if (insideIndependentArea) {
						cerList.add(new CostExpressionReturn(new ArrayList<>(), 0.0, new HashSet<>()));
						cerWriteList.add(new CellSet());
						cerOpRPhiList.add(new CellSet());
						cerFreeVarPhiList.add(new CellSet());
					} else {
						// Inside Dependent Area (and not a DFD)
						CostExpressionReturn retCer = elem.accept(this, ceaTemp);

						cerList.add(retCer);
						cerWriteList.add(CostExpressionGenerator.getWritesAfterConnecting(retCer, elem));
						cerOpRPhiList.add(CostExpressionGenerator.getOprOutPhiAfterConnecting(retCer, elem));
						cerFreeVarPhiList.add(CostExpressionGenerator.getFreeCellsAfterConnecting(retCer, elem));
					}
				}
				ceaTemp.openReads.addAll(OpenReadsGetter.getOpenReads(elem, ceaTemp.mustWrites));
				ceaTemp.mustWrites.addAll(MustWritesGetter.getMustWrites(elem));

				stmtOpRList.add(new CellSet(ceaTemp.openReads));
				stmtOpRPhiList.add(OpenReadsGetter.getOpenReads(elem, new CellSet()));
				stmtFswPhiList.add(new CellSet(MustWritesGetter.getMustWrites(elem))); // Since this is phi, we do
																							// not use ceaTemp.fsw.
				stmtFreeVarPhiList.add(Misc.getFreeCells(elem));
			}

			// Step 2: For each CER(x) in 1 to n:
			/*
			 * ,,,,,,,,,Step 2a: Check1:
			 * ,,,,,,,,,if WriteSet(CER(x)) intersects with opRSetOut(S(x)),
			 * ,,,,,,,,,then throw Exception.
			 * ,,,,,,,,,L = opRSetPhi(CER(x))
			 * ,,,,,,,,,FV = freeVarPhi(CER(x))
			 * ,,,,,,,,,For each S(y) in (x-1) to 1:
			 * ,,,,,,,,,,,Step 2b: Check2:
			 * ,,,,,,,,,,,boolean considerForMark = false;
			 * ,,,,,,,,,,,if (L intersects with WriteSet(S(y)))
			 * ,,,,,,,,,,,then considerForMark = true;
			 * ,,,,,,,,,,,
			 * ,,,,,,,,,,,if (S(y) declares any node in FV)
			 * ,,,,,,,,,,,then considerForMark = true;
			 * ,,,,,,,,,,,
			 * ,,,,,,,,,,,if (!considerForMark)
			 * ,,,,,,,,,,,then continue;
			 * ,,,,,,,,,,,
			 * ,,,,,,,,,,,if WriteSet(S(y)) intersects with opRSetOut(S(y))
			 * ,,,,,,,,,,,,,,OR S(y) contains any prints,
			 * ,,,,,,,,,,,then throw Exception.
			 * ,,,,,,,,,,,
			 * ,,,,,,,,,,,Mark S(y)
			 * ,,,,,,,,,,,L = L - FSWPhi(S(y)) union opRSetPhi(S(y))
			 * ,,,,,,,,,,,FV = FV - decl(S(y)) union freeVarPhi(S(y))
			 */

			for (int index = 0; index < n.getInfo().getCFGInfo().getElementList().size(); index++) {
				// Processing CER(index)
				if (Misc.doIntersect(cerWriteList.get(index), stmtOpRList.get(index))) {
					Misc.exitDueToError(
							"Found conflict between CostExp write & OpenRead of any elem between WLLoop to this point: "
									+ n.getInfo().getCFGInfo().getElementList().get(index));
				}

				CellSet setL = new CellSet(cerOpRPhiList.get(index));
				CellSet setFV = new CellSet(cerFreeVarPhiList.get(index));

				for (int j = index - 1; j >= 0; j--) {
					Node elem = n.getInfo().getCFGInfo().getElementList().get(j);
					boolean considerForMarking = false;

					if (Misc.doIntersect(setL, elem.getInfo().getWrites())) {
						considerForMarking = true;
					}

					if (elem instanceof Declaration) {
						Declaration decl = (Declaration) elem;
						if (setFV.contains(decl.getInfo().getDeclaredSymbol())) {
							considerForMarking = true;
						}
					}

					if (!considerForMarking) {
						continue;
					}

					if (Misc.doIntersect(elem.getInfo().getWrites(), stmtOpRList.get(j))) {
						Misc.exitDueToError(
								"Found conflict between Stmt (to be copied) write & OpenRead of any elem between WLLoop to this point: "
										+ elem);

					}

					markedStmt[j] = true;
					setL.removeAll(stmtFswPhiList.get(j));
					setL.addAll(stmtOpRPhiList.get(j));
					if (elem instanceof Declaration) {
						Declaration decl = (Declaration) elem;
						setFV.remove(decl.getInfo().getDeclaredSymbol());
					}
					setFV.addAll(stmtFreeVarPhiList.get(j));
				}

			}

			/*
			 * Step 4: Create an empty list K
			 * ,,,,,,,,stmtStr = "amit = " // name representing Cost Expression for this
			 * ,,,,,,,,,,,,,,,,,,,,,,,,,,,,// CompoundStmt
			 * ,,,,,,,,For each C = CER(x) from 1-n:
			 * ,,,,,,,,,,1. Declare all the returned pending declarations in K
			 * ,,,,,,,,,,2. Paste all the returned list of statements(nodes) from C in K
			 * ,,,,,,,,,,3. stmtStr += "C.costExpName/C.costExpValue"
			 * ,,,,,,,,,,4. If (S(x)) is marked, then add it to K.
			 * ,,,,,,,,Done
			 * ,,,,,,,,Now, create the statement from stmtStr which represents population of
			 * ,,,,,,,,,,costExp for this CompoundStmt & add it to K
			 * ,,,,,,,,Create a CompoundStmt from K & add it as a single element of CER.list
			 * ,,,,,,,,CER.name = "amit"
			 * ,,,,,,,,CER.pendingDec = {"amit"}
			 * ,,,,,,,,return CER
			 * 
			 */
			String costExpVar = Builder.getNewTempName("costExpVar");
			String stmtString = costExpVar + " = ";
			double independentCostSum = 0.0;

			boolean foundVar = false;
			List<Node> listK = new ArrayList<>();
			int cerIndex = -1;
			for (CostExpressionReturn cer : cerList) {
				cerIndex++;
				if (cer.isAVal()) {
					independentCostSum += cer.costVal;
					// Add marked statement now:
					if (markedStmt[cerIndex]) {
						listK.add(n.getInfo().getCFGInfo().getElementList().get(cerIndex));
					}
					continue;
				}

				for (String penDecName : cer.pendingDeclarationNames) {
					String penDecStr = "";
					if (penDecName.contains("iterCount")) {
						penDecStr = "int " + penDecName + ";\n";
					} else {
						penDecStr = "double " + penDecName + ";\n";
					}
					Declaration decl = FrontEnd.parseAndNormalize(penDecStr, Declaration.class);
					listK.add(decl);
				}

				for (Node nestedElem : cer.code) {
					listK.add(nestedElem);
				}
				// Add marked statement now:
				if (markedStmt[cerIndex]) {
					listK.add(n.getInfo().getCFGInfo().getElementList().get(cerIndex));
				}

				if (foundVar) {
					stmtString += " + ";
				}
				foundVar = true;
				stmtString += cer.costVarName + " ";
			}
			if (foundVar) {
				stmtString += " + ";
			}

			stmtString += independentCostSum + ";";

			String returnElementStr = "{";
			for (Node h : listK) {
				returnElementStr += "\n" + h;
			}
			returnElementStr += "\n" + stmtString + "}";

			List<Node> retCERCode = new ArrayList<>();
			retCERCode.add(FrontEnd.parseAndNormalize(returnElementStr, CompoundStatement.class));
			Set<String> retCERPenDec = new HashSet<>();
			retCERPenDec.add(costExpVar);
			return new CostExpressionReturn(retCERCode, costExpVar, retCERPenDec);
		}

		/**
		 * f0 ::= <IF>
		 * f1 ::= "("
		 * f2 ::= Expression()
		 * f3 ::= ")"
		 * f4 ::= Statement()
		 * f5 ::= ( <ELSE> Statement() )?
		 */
		public CostExpressionReturn visit(IfStatement n, CostExpressionArgument cea) {
			String currLabel = "";
			List<Node> stmtList = new ArrayList<>();
			Set<String> pendDecSet = new HashSet<>();
			String expThenString = "";
			double expThenDouble = 0.0;
			String expElseString = "";
			double expElseDouble = 0.0;
			for (Label l : n.getInfo().getLabelAnnotations()) {
				if (l instanceof SimpleLabel) {
					String labelName = ((SimpleLabel) l).getLabelName();
					if (labelName.startsWith("LabelIf")) {
						currLabel = labelName;
						break;
					}
				}
			}
			assert (!currLabel.isBlank());

			double frac = Utilities.getFracCostForIfLabel(currLabel);

			// Process THEN body
			CostExpressionArgument ceaThen = new CostExpressionArgument(cea);
			ceaThen.mustWrites.addAll(MustWritesGetter.getMustWrites(n.getInfo().getCFGInfo().getPredicate()));
			ceaThen.openReads
					.addAll(OpenReadsGetter.getOpenReads(n.getInfo().getCFGInfo().getPredicate(), cea.mustWrites));
			CostExpressionReturn cerIf = n.getInfo().getCFGInfo().getThenBody().accept(this, ceaThen);

			if (cerIf.isAVal()) {
				expThenDouble = cerIf.costVal * frac;
			} else {
				expThenString = cerIf.costVarName + " * " + frac;
				pendDecSet.addAll(cerIf.pendingDeclarationNames);
			}
			stmtList.addAll(cerIf.code);

			// Process ELSE body
			boolean foundElse = n.getInfo().getCFGInfo().hasElseBody();
			if (foundElse) {
				CostExpressionReturn cerElse = n.getInfo().getCFGInfo().getElseBody().accept(this, ceaThen);

				if (cerElse.isAVal()) {
					expElseDouble = cerElse.costVal * (1 - frac);
				} else {
					expElseString = cerElse.costVarName + " * (1 - " + frac + ")";
					pendDecSet.addAll(cerElse.pendingDeclarationNames);
				}
				stmtList.addAll(cerElse.code);
			} else {
				// Do nothing
			}

			Statement expStmtString = null;
			String expString = null;
			double expDouble = 0.0;
			if (foundElse) {
				if (expThenString.isBlank() && expElseString.isBlank()) {
					expDouble = expThenDouble + expElseDouble;
				} else {
					expString = Builder.getNewTempName("localCostIfVar");
					String stmtString = "";
					if (expThenString.isBlank() && !expElseString.isBlank()) {
						stmtString = expString + " = " + String.format("%.10f", expThenDouble) + " + " + expElseString
								+ ";";
					} else if (!expThenString.isBlank() && expElseString.isBlank()) {
						stmtString = expString + " = " + expThenString + " + " + String.format("%.10f", expElseDouble)
								+ ";";
					} else {
						stmtString = expString + " = " + expThenString + " + " + expElseString + ";";
					}
					expStmtString = FrontEnd.parseAndNormalize(stmtString, Statement.class);
					stmtList.add(expStmtString);
				}
			} else {
				if (expThenString.isBlank()) {
					expDouble = expThenDouble;
				} else {
					expString = Builder.getNewTempName("localCostIfVar");
					String stmtString = "";
					stmtString =  expString + " = " +  expThenString + ";";
					expStmtString = FrontEnd.parseAndNormalize(stmtString, Statement.class);
					stmtList.add(expStmtString);
				}
			}

			if (expString != null) {
				pendDecSet.add(expString);
				return new CostExpressionReturn(stmtList, expString, pendDecSet);
			} else {
				return new CostExpressionReturn(stmtList, expDouble, pendDecSet);
			}
		}

		/**
		 * f0 ::= <SWITCH>
		 * f1 ::= "("
		 * f2 ::= Expression()
		 * f3 ::= ")"
		 * f4 ::= Statement()
		 */
		public CostExpressionReturn visit(SwitchStatement n, CostExpressionArgument cea) {
			assert (false) : "Switch Statement Not Handled During Cost Expression Generator";
			return null;
		}

		/**
		 * f0 ::= <WHILE>
		 * f1 ::= "("
		 * f2 ::= Expression()
		 * f3 ::= ")"
		 * f4 ::= Statement()
		 */
		public CostExpressionReturn visit(WhileStatement n, CostExpressionArgument cea) {
			CostExpressionArgument ceaPred = new CostExpressionArgument(cea);
			ceaPred.mustWrites.addAll(MustWritesGetter.getMustWrites(n.getInfo().getCFGInfo().getPredicate()));
			ceaPred.openReads
					.addAll(OpenReadsGetter.getOpenReads(n.getInfo().getCFGInfo().getPredicate(), cea.mustWrites));
			CostExpressionReturn _ret = n.getInfo().getCFGInfo().getBody().accept(this, ceaPred);
			return _ret;
		}

		/**
		 * f0 ::= <DO>
		 * f1 ::= Statement()
		 * f2 ::= <WHILE>
		 * f3 ::= "("
		 * f4 ::= Expression()
		 * f5 ::= ")"
		 * f6 ::= ";"
		 */
		public CostExpressionReturn visit(DoStatement n, CostExpressionArgument cea) {
			CostExpressionReturn _ret = n.getInfo().getCFGInfo().getBody().accept(this, cea);
			return _ret;
		}

		/**
		 * f0 ::= <FOR>
		 * f1 ::= "("
		 * f2 ::= ( Expression() )?
		 * f3 ::= ";"
		 * f4 ::= ( Expression() )?
		 * f5 ::= ";"
		 * f6 ::= ( Expression() )?
		 * f7 ::= ")"
		 * f8 ::= Statement()
		 */
		public CostExpressionReturn visit(ForStatement n, CostExpressionArgument cea) {
			Set<String> penDecSet = new HashSet<>();
			CostExpressionReturn cerForHeader = Utilities.getCountforForHeader(n);
			CostExpressionArgument ceaBody = new CostExpressionArgument(cea);
			if (n.getInfo().getCFGInfo().hasInitExpression()) {
				ceaBody.mustWrites
						.addAll(MustWritesGetter.getMustWrites(n.getInfo().getCFGInfo().getInitExpression()));
				ceaBody.openReads.addAll(
						OpenReadsGetter.getOpenReads(n.getInfo().getCFGInfo().getInitExpression(), cea.mustWrites));
			}
			if (n.getInfo().getCFGInfo().hasTerminationExpression()) {
				ceaBody.openReads.addAll(OpenReadsGetter
						.getOpenReads(n.getInfo().getCFGInfo().getTerminationExpression(), ceaBody.mustWrites));
				ceaBody.mustWrites.addAll(
						MustWritesGetter.getMustWrites(n.getInfo().getCFGInfo().getTerminationExpression()));
			}

			CostExpressionReturn cerBody = n.getInfo().getCFGInfo().getBody().accept(this, ceaBody);
			penDecSet.addAll(cerBody.pendingDeclarationNames);
			if (cerForHeader == null) {
				System.err.println("Unknown number of iterations case");
				return cerBody;
			}
			penDecSet.addAll(cerForHeader.pendingDeclarationNames);

			List<Node> tempList = new ArrayList<>();
			tempList.addAll(cerForHeader.code);
			tempList.addAll(cerBody.code);

			assert (cerForHeader.isAVar());
			String expString = Builder.getNewTempName("localCostForVar");
			String stmtString = "";

			if (cerBody.isAVar()) {
				stmtString = expString + " = " + cerForHeader.costVarName + " * " + cerBody.costVarName + ";";
			} else {
				stmtString = expString + " = " + cerForHeader.costVarName + " * " + cerBody.costVal + ";";
			}
			penDecSet.add(expString);

			Statement tempStmt = FrontEnd.parseAndNormalize(stmtString, Statement.class);
			tempList.add(tempStmt);
			CostExpressionReturn cer = new CostExpressionReturn(tempList, expString, penDecSet);
			return cer;
		}

		/**
		 * f0 ::= <GOTO>
		 * f1 ::= <IDENTIFIER>
		 * f2 ::= ";"
		 */
		public CostExpressionReturn visit(GotoStatement n, CostExpressionArgument cea) {
			assert (false);
			return null;
		}

		/**
		 * f0 ::= <CONTINUE>
		 * f1 ::= ";"
		 */
		public CostExpressionReturn visit(ContinueStatement n, CostExpressionArgument cea) {
			assert (false);
			return null;
		}

		/**
		 * f0 ::= <BREAK>
		 * f1 ::= ";"
		 */
		public CostExpressionReturn visit(BreakStatement n, CostExpressionArgument cea) {
			assert (false);
			return null;
		}

		/**
		 * f0 ::= <RETURN>
		 * f1 ::= ( Expression() )?
		 * f2 ::= ";"
		 */
		public CostExpressionReturn visit(ReturnStatement n, CostExpressionArgument cea) {
			assert (false);
			return null;
		}

		/**
		 * f0 ::= AssignmentExpression()
		 * f1 ::= ( "," AssignmentExpression() )*
		 */
		public CostExpressionReturn visit(Expression n, CostExpressionArgument cea) {
			assert (false);
			return null;
		}

		public CostExpressionReturn visit(DummyFlushDirective n, CostExpressionArgument cea) {
			assert (false);
			return null;
		}

		public CostExpressionReturn visit(PreCallNode n, CostExpressionArgument cea) {
			assert (false);
			return null;
		}

		public CostExpressionReturn visit(PostCallNode n, CostExpressionArgument cea) {
			assert (false);
			return null;
		}

		public CostExpressionReturn visit(CallStatement n, CostExpressionArgument cea) {
			List<FunctionDefinition> calledFunctions = n.getInfo().getCalledDefinitions();
			HashMap<String, String> renameParDecToArgMap = new HashMap<>();

			if (calledFunctions.size() == 0) {
				return new CostExpressionReturn(new ArrayList<>(), 0, new HashSet<>());
			} else if (calledFunctions.size() > 1) {
				assert (false) : "Multiple Targets";
			}
			FunctionDefinition fd = calledFunctions.get(0);
			PreCallNode preCn = n.getPreCallNode();
			int id = -1;

			for (SimplePrimaryExpression spe : preCn.getArgumentList()) {
				id++;
				if (spe.isAConstant()) {
					continue;
				}
				String argName = spe.getIdentifier().getTokenImage();
				ParameterDeclaration paramDec = fd.getInfo().getCFGInfo().getParameterDeclarationList().get(id);
				String paramName = paramDec.getInfo().getDeclaredSymbol().getName();
				renameParDecToArgMap.put(paramName, argName);
			}

			CostExpressionArgument ceaArg = new CostExpressionArgument(cea);
			ceaArg.openReads.addAll(OpenReadsGetter.getOpenReads(n.getPreCallNode(), cea.mustWrites));
			CostExpressionReturn cer = fd.getInfo().getCFGInfo().getBody().accept(this, ceaArg);
			List<Node> tempList = new ArrayList<>();

			for (Node s : cer.code) {
				tempList.add((Statement) BasicTransform.obtainRenamedNode(s, renameParDecToArgMap));
			}
			cer.code = tempList;

			return cer;
		}
	}

}