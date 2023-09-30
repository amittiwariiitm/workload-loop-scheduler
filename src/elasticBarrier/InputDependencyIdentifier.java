/*
 * Copyright (c) 2019 Aman Nougrahiya, V Krishna Nandivada, IIT Madras.
 * This file is a part of the project IMOP, licensed under the MIT license.
 * See LICENSE.md for the full text of the license.
 * 
 * The above notice shall be included in all copies or substantial
 * portions of this file.
 */
package elasticBarrier;

import imop.ast.node.external.*;
import imop.ast.node.internal.*;
import imop.baseVisitor.GJNoArguDepthFirstProcess;
import imop.baseVisitor.GJVoidDepthFirstProcess;
import imop.baseVisitor.cfgTraversals.GJNoArguDepthFirstCFG;
import imop.lib.analysis.flowanalysis.Cell;
import imop.lib.analysis.flowanalysis.FreeVariable;
import imop.lib.analysis.flowanalysis.Symbol;
import imop.lib.util.CellSet;
import imop.lib.util.Misc;
import imop.parser.FrontEnd;

/**
 * Returns True for Input dependent node & false otherwise.
 */
public class InputDependencyIdentifier extends GJNoArguDepthFirstCFG<Boolean> {
	public Boolean initProcess(Node n) {
		return null;
	}

	public Boolean endProcess(Node n) {
		return null;
	}

	/**
	 * @deprecated
	 * @param n
	 * @return
	 */
	@Deprecated
	public Boolean processCalls(Node n) {
		return null;
	}

	/**
	 * Special Node
	 */
	public Boolean visit(BeginNode n) {
		assert (false);
		Boolean _ret = false;
		return _ret;
	}

	/**
	 * Special Node
	 */
	public Boolean visit(EndNode n) {
		assert (false);
		Boolean _ret = false;
		return _ret;
	}

	/**
	 * f0 ::= ( DeclarationSpecifiers() )?
	 * f1 ::= Declarator()
	 * f2 ::= ( DeclarationList() )?
	 * f3 ::= CompoundStatement()
	 */
	public Boolean visit(FunctionDefinition n) {
		Boolean _ret = n.getInfo().getCFGInfo().getBody().accept(this);
		return _ret;
	}

	/**
	 * f0 ::= DeclarationSpecifiers()
	 * f1 ::= ( InitDeclaratorList() )?
	 * f2 ::= ";"
	 */
	public Boolean visit(Declaration n) {
		Boolean _ret = false;
		return _ret;
	}

	/**
	 * f0 ::= DeclarationSpecifiers()
	 * f1 ::= ParameterAbstraction()
	 */
	public Boolean visit(ParameterDeclaration n) {
		Boolean _ret = false;
		return _ret;
	}

	/**
	 * f0 ::= "#"
	 * f1 ::= <UNKNOWN_CPP>
	 */
	public Boolean visit(UnknownCpp n) {
		Boolean _ret = false;
		return _ret;
	}

	/**
	 * f0 ::= OmpPragma()
	 * f1 ::= ParallelDirective()
	 * f2 ::= Statement()
	 */
	public Boolean visit(ParallelConstruct n) {
		assert (false);
		return false;
	}

	/**
	 * f0 ::= "#"
	 * f1 ::= <PRAGMA>
	 * f2 ::= <UNKNOWN_CPP>
	 */
	public Boolean visit(UnknownPragma n) {
		Boolean _ret = false;
		return _ret;
	}

	/**
	 * f0 ::= <IF>
	 * f1 ::= "("
	 * f2 ::= Expression()
	 * f3 ::= ")"
	 */
	public Boolean visit(IfClause n) {
		Boolean _ret = false;
		return _ret;
	}

	/**
	 * f0 ::= <NUM_THREADS>
	 * f1 ::= "("
	 * f2 ::= Expression()
	 * f3 ::= ")"
	 */
	public Boolean visit(NumThreadsClause n) {
		Boolean _ret = false;
		return _ret;
	}

	/**
	 * f0 ::= OmpPragma()
	 * f1 ::= ForDirective()
	 * f2 ::= OmpForHeader()
	 * f3 ::= Statement()
	 */
	public Boolean visit(ForConstruct n) {
		assert (false);
		return false;
	}

	/**
	 * f0 ::= <IDENTIFIER>
	 * f1 ::= "="
	 * f2 ::= Expression()
	 */
	public Boolean visit(OmpForInitExpression n) {
		Boolean _ret = false;
		return _ret;
	}

	/**
	 * f0 ::= OmpForLTCondition()
	 * | OmpForLECondition()
	 * | OmpForGTCondition()
	 * | OmpForGECondition()
	 */
	public Boolean visit(OmpForCondition n) {
		Boolean _ret = false;
		return _ret;
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
	public Boolean visit(OmpForReinitExpression n) {
		Boolean _ret = false;
		return _ret;
	}

	/**
	 * f0 ::= OmpPragma()
	 * f1 ::= <SECTIONS>
	 * f2 ::= NowaitDataClauseList()
	 * f3 ::= OmpEol()
	 * f4 ::= SectionsScope()
	 */
	public Boolean visit(SectionsConstruct n) {
		assert (false);
		return false;
	}

	/**
	 * f0 ::= OmpPragma()
	 * f1 ::= <SINGLE>
	 * f2 ::= SingleClauseList()
	 * f3 ::= OmpEol()
	 * f4 ::= Statement()
	 */
	public Boolean visit(SingleConstruct n) {
		assert (false);
		return false;
	}

	/**
	 * f0 ::= OmpPragma()
	 * f1 ::= <TASK>
	 * f2 ::= ( TaskClause() )*
	 * f3 ::= OmpEol()
	 * f4 ::= Statement()
	 */
	public Boolean visit(TaskConstruct n) {
		Boolean _ret = n.getInfo().getCFGInfo().getBody().accept(this);
		return _ret;
	}

	/**
	 * f0 ::= <FINAL>
	 * f1 ::= "("
	 * f2 ::= Expression()
	 * f3 ::= ")"
	 */
	public Boolean visit(FinalClause n) {
		Boolean _ret = false;
		return _ret;
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
	public Boolean visit(ParallelForConstruct n) {
		assert (false);
		return false;
	}

	/**
	 * f0 ::= OmpPragma()
	 * f1 ::= <PARALLEL>
	 * f2 ::= <SECTIONS>
	 * f3 ::= UniqueParallelOrDataClauseList()
	 * f4 ::= OmpEol()
	 * f5 ::= SectionsScope()
	 */
	public Boolean visit(ParallelSectionsConstruct n) {
		assert (false);
		return false;
	}

	/**
	 * f0 ::= OmpPragma()
	 * f1 ::= <MASTER>
	 * f2 ::= OmpEol()
	 * f3 ::= Statement()
	 */
	public Boolean visit(MasterConstruct n) {
		Boolean _ret = n.getInfo().getCFGInfo().getBody().accept(this);
		return _ret;
	}

	/**
	 * f0 ::= OmpPragma()
	 * f1 ::= <CRITICAL>
	 * f2 ::= ( RegionPhrase() )?
	 * f3 ::= OmpEol()
	 * f4 ::= Statement()
	 */
	public Boolean visit(CriticalConstruct n) {
		Boolean _ret = n.getInfo().getCFGInfo().getBody().accept(this);
		return _ret;
	}

	/**
	 * f0 ::= OmpPragma()
	 * f1 ::= <ATOMIC>
	 * f2 ::= ( AtomicClause() )?
	 * f3 ::= OmpEol()
	 * f4 ::= ExpressionStatement()
	 */
	public Boolean visit(AtomicConstruct n) {
		// Input Independent Special Case
		return false;
	}

	/**
	 * f0 ::= OmpPragma()
	 * f1 ::= <FLUSH>
	 * f2 ::= ( FlushVars() )?
	 * f3 ::= OmpEol()
	 */
	public Boolean visit(FlushDirective n) {
		Boolean _ret = false;
		return _ret;
	}

	/**
	 * f0 ::= OmpPragma()
	 * f1 ::= <ORDERED>
	 * f2 ::= OmpEol()
	 * f3 ::= Statement()
	 */
	public Boolean visit(OrderedConstruct n) {
		Boolean _ret = n.getInfo().getCFGInfo().getBody().accept(this);
		return _ret;
	}

	/**
	 * f0 ::= OmpPragma()
	 * f1 ::= <BARRIER>
	 * f2 ::= OmpEol()
	 */
	public Boolean visit(BarrierDirective n) {
		Boolean _ret = false;
		return _ret;
	}

	/**
	 * f0 ::= OmpPragma()
	 * f1 ::= <TASKWAIT>
	 * f2 ::= OmpEol()
	 */
	public Boolean visit(TaskwaitDirective n) {
		Boolean _ret = false;
		return _ret;
	}

	/**
	 * f0 ::= OmpPragma()
	 * f1 ::= <TASKYIELD>
	 * f2 ::= OmpEol()
	 */
	public Boolean visit(TaskyieldDirective n) {
		Boolean _ret = false;
		return _ret;
	}

	/**
	 * f0 ::= ( Expression() )?
	 * f1 ::= ";"
	 */
	public Boolean visit(ExpressionStatement n) {
		Boolean _ret = false;
		return _ret;
	}

	/**
	 * f0 ::= "{"
	 * f1 ::= ( CompoundStatementElement() )*
	 * f2 ::= "}"
	 */
	public Boolean visit(CompoundStatement n) {
		Boolean _ret = false;
		for (Node elem : n.getInfo().getCFGInfo().getElementList()) {
			if (elem.accept(this)) {
				return true;
			}
		}
		return _ret;
	}

	/**
	 * f0 ::= <IF>
	 * f1 ::= "("
	 * f2 ::= Expression()
	 * f3 ::= ")"
	 * f4 ::= Statement()
	 * f5 ::= ( <ELSE> Statement() )?
	 */
	public Boolean visit(IfStatement n) {
		Boolean _ret = false;
		if (n.getInfo().getCFGInfo().getThenBody().accept(this)) {
			return true;
		}
		if (n.getInfo().getCFGInfo().hasElseBody() && n.getInfo().getCFGInfo().getElseBody().accept(this)) {
			return true;
		}
		return _ret;
	}

	/**
	 * f0 ::= <SWITCH>
	 * f1 ::= "("
	 * f2 ::= Expression()
	 * f3 ::= ")"
	 * f4 ::= Statement()
	 */
	public Boolean visit(SwitchStatement n) {
		Boolean _ret = true;
		Misc.warnDueToLackOfFeature("Not handling SwitchStatements for Cost Expression Generation", n);
		return _ret;
	}

	/**
	 * f0 ::= <WHILE>
	 * f1 ::= "("
	 * f2 ::= Expression()
	 * f3 ::= ")"
	 * f4 ::= Statement()
	 */
	public Boolean visit(WhileStatement n) {
		Boolean _ret = true;
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
	public Boolean visit(DoStatement n) {
		Boolean _ret = true;
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
	/**
	 * Step 1: Find RHS of the 3 expressions of For. 
	 * Step 2: RHS deduced is disconnected.
	 * Step 3: Find the init, cond or step expression of the given ForStatement which is connected
	 * Step 4: Go over all its sub-expressions.
	 * Step 5: Identify the connected sub-expression using the RHS deduced (v1, v2, v3) & break if found.
	 * Step 6: Add all the accesses of this sub-expression (RHS) into a CellSet.
	 * Step 7: If any access's value is Input dependent implies the Count Expression made using these RHS
	 * 			values will be Input dependent. Hence, return true.
	 * Step 8: If any of these RHS value is Input dependent => ForStatement is Input dependent.
	 * Step 9: Else, the ForStatement is Input dependent/independent based on the behaviour of its body.
	 */
	public Boolean visit(ForStatement n) {
		Boolean _ret = false;
//		System.out.println("\n====\nVISITING: " + n);
		Expression v1 = Utilities.getRhsOfForInit(n);
		Expression v2 = Utilities.getRhsOfForCondition(n);
		Expression v3 = Utilities.getRhsOfForStep(n);

		// v1,v2,v3 are disconnected because of OmpFor parseNormalize check

		Expression initConnectedExp = n.getInfo().getCFGInfo().getInitExpression();
		CellSet connectedV1Cells = new CellSet();
		for (Expression exp : Misc.getInheritedEnclosee(initConnectedExp, Expression.class)) {
			if (v1.toString().equals(exp.toString())) {
				connectedV1Cells.addAll(exp.getInfo().getAccesses());
				break;
			}
		}

//		System.out.println("Init exp: " + initConnectedExp);
//		System.out.println("v1: " + v1);
//		System.out.println("Cells: " + connectedV1Cells);
		for (Cell c : connectedV1Cells) {
			_ret = Utilities.isVariableValueInputDependentAtNode(c, initConnectedExp);
//			System.out.println("Input Dependency is " + _ret + " for " + initConnectedExp);
			if (_ret) {
				return true;
			}
		}
		Expression condConnectedExp = n.getInfo().getCFGInfo().getTerminationExpression();
		CellSet connectedV2Cells = new CellSet();
		for (Expression exp : Misc.getInheritedEnclosee(condConnectedExp, Expression.class)) {
			if (v2.toString().equals(exp.toString())) {
				connectedV2Cells.addAll(exp.getInfo().getAccesses());
				break;
			}
		}
//		System.out.println("Term exp: " + condConnectedExp);
//		System.out.println("v2: " + v2);
//		System.out.println("Cells: " + connectedV2Cells);
		for (Cell c : connectedV2Cells) {
			_ret = Utilities.isVariableValueInputDependentAtNode(c, condConnectedExp);
//			System.out.println("Input Dependency is " + _ret + " for " + condConnectedExp);
			if (_ret) {
				return true;
			}
		}
		Expression stepConnectedExp = n.getInfo().getCFGInfo().getStepExpression();
		CellSet connectedV3Cells = new CellSet();
		for (Expression exp : Misc.getInheritedEnclosee(stepConnectedExp, Expression.class)) {
			if (v3.toString().equals(exp.toString())) {
				connectedV3Cells.addAll(exp.getInfo().getAccesses());
				break;
			}
		}
//		System.out.println("Step exp: " + stepConnectedExp);
//		System.out.println("v3: " + v3);
//		System.out.println("Cells: " + connectedV3Cells);
		for (Cell c : connectedV3Cells) {
			_ret = Utilities.isVariableValueInputDependentAtNode(c, stepConnectedExp);
//			System.out.println("Input Dependency is " + _ret + " for " + stepConnectedExp);
			if (_ret) {
				return true;
			}
		}
		return n.getInfo().getCFGInfo().getBody().accept(this);
	}

	/**
	 * f0 ::= <GOTO>
	 * f1 ::= <IDENTIFIER>
	 * f2 ::= ";"
	 */
	public Boolean visit(GotoStatement n) {
		Boolean _ret = false;
		return _ret;
	}

	/**
	 * f0 ::= <CONTINUE>
	 * f1 ::= ";"
	 */
	public Boolean visit(ContinueStatement n) {
		Boolean _ret = false;
		return _ret;
	}

	/**
	 * f0 ::= <BREAK>
	 * f1 ::= ";"
	 */
	public Boolean visit(BreakStatement n) {
		Boolean _ret = false;
		return _ret;
	}

	/**
	 * f0 ::= <RETURN>
	 * f1 ::= ( Expression() )?
	 * f2 ::= ";"
	 */
	public Boolean visit(ReturnStatement n) {
		Boolean _ret = false;
		return _ret;
	}

	/**
	 * f0 ::= AssignmentExpression()
	 * f1 ::= ( "," AssignmentExpression() )*
	 */
	public Boolean visit(Expression n) {
		Boolean _ret = false;
		return _ret;
	}

	public Boolean visit(DummyFlushDirective n) {
		Boolean _ret = false;
		return _ret;
	}

	public Boolean visit(PreCallNode n) {
		Boolean _ret = false;
		return _ret;
	}

	public Boolean visit(PostCallNode n) {
		Boolean _ret = false;
		return _ret;
	}

	public Boolean visit(CallStatement n) {
		Boolean _ret = false;
		for (FunctionDefinition fd : n.getInfo().getCalledDefinitions()) {
			if (fd.getInfo().isRecursive()) {
				return true;
			}
			if (fd.accept(this)) {
				return true;
			}
		}
		return _ret;
	}
}
