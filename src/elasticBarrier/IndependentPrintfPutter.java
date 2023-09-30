/*
 * Copyright (c) 2019 Aman Nougrahiya, V Krishna Nandivada, IIT Madras.
 * This file is a part of the project IMOP, licensed under the MIT license.
 * See LICENSE.md for the full text of the license.
 * 
 * The above notice shall be included in all copies or substantial
 * portions of this file.
 */
package elasticBarrier;

import java.util.HashSet;
import java.util.Set;

import imop.ast.annotation.Label;
import imop.ast.annotation.SimpleLabel;
import imop.ast.node.external.*;
import imop.ast.node.internal.*;
import imop.baseVisitor.DepthFirstProcess;
import imop.baseVisitor.cfgTraversals.DepthFirstCFG;
import imop.lib.builder.Builder;
import imop.lib.transform.updater.InsertImmediatePredecessor;
import imop.lib.transform.updater.InsertImmediateSuccessor;
import imop.parser.FrontEnd;
import imop.parser.Program;

/**
 * Provides default methods for processing all the CFG nodes.
 * Traversal shall be defined as per the requirements.
 * Note: This visitor is just for convenience in handling all the CFGs.
 */
public class IndependentPrintfPutter extends DepthFirstCFG {
	int id = 0;
	private Set<FunctionDefinition> visitedFunctions = new HashSet<>();

	@Override
	public void initProcess(Node n) {
		return;
	}

	@Override
	public void endProcess(Node n) {
		return;
	}

	/**
	 * @deprecated
	 * @param n
	 */
	@Deprecated
	public void processCalls(Node n) {
	}

	/**
	 * Special Node
	 */
	@Override
	public void visit(BeginNode n) {
		assert (false);
		initProcess(n);
		endProcess(n);
	}

	/**
	 * Special Node
	 */
	@Override
	public void visit(EndNode n) {
		assert (false);
		initProcess(n);
		endProcess(n);
	}

	/**
	 * f0 ::= ( DeclarationSpecifiers() )?
	 * f1 ::= Declarator()
	 * f2 ::= ( DeclarationList() )?
	 * f3 ::= CompoundStatement()
	 */
	@Override
	public void visit(FunctionDefinition n) {
		n.getInfo().getCFGInfo().getBody().accept(this);
	}

	/**
	 * f0 ::= DeclarationSpecifiers()
	 * f1 ::= ( InitDeclaratorList() )?
	 * f2 ::= ";"
	 */
	@Override
	public void visit(Declaration n) {
	}

	/**
	 * f0 ::= DeclarationSpecifiers()
	 * f1 ::= ParameterAbstraction()
	 */
	@Override
	public void visit(ParameterDeclaration n) {
		assert (false);
	}

	/**
	 * f0 ::= "#"
	 * f1 ::= <UNKNOWN_CPP>
	 */
	@Override
	public void visit(UnknownCpp n) {
	}

	/**
	 * f0 ::= OmpPragma()
	 * f1 ::= ParallelDirective()
	 * f2 ::= Statement()
	 */
	@Override
	public void visit(ParallelConstruct n) {
		assert (false);
	}

	/**
	 * f0 ::= "#"
	 * f1 ::= <PRAGMA>
	 * f2 ::= <UNKNOWN_CPP>
	 */
	@Override
	public void visit(UnknownPragma n) {
	}

	/**
	 * f0 ::= <IF>
	 * f1 ::= "("
	 * f2 ::= Expression()
	 * f3 ::= ")"
	 */
	@Override
	public void visit(IfClause n) {
	}

	/**
	 * f0 ::= <NUM_THREADS>
	 * f1 ::= "("
	 * f2 ::= Expression()
	 * f3 ::= ")"
	 */
	@Override
	public void visit(NumThreadsClause n) {
	}

	/**
	 * f0 ::= OmpPragma()
	 * f1 ::= ForDirective()
	 * f2 ::= OmpForHeader()
	 * f3 ::= Statement()
	 */
	@Override
	public void visit(ForConstruct n) {
		n.getInfo().getCFGInfo().getBody().accept(this);
	}

	/**
	 * f0 ::= <IDENTIFIER>
	 * f1 ::= "="
	 * f2 ::= Expression()
	 */
	@Override
	public void visit(OmpForInitExpression n) {
	}

	/**
	 * f0 ::= OmpForLTCondition()
	 * | OmpForLECondition()
	 * | OmpForGTCondition()
	 * | OmpForGECondition()
	 */
	@Override
	public void visit(OmpForCondition n) {
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
	@Override
	public void visit(OmpForReinitExpression n) {
	}

	/**
	 * f0 ::= OmpPragma()
	 * f1 ::= <SECTIONS>
	 * f2 ::= NowaitDataClauseList()
	 * f3 ::= OmpEol()
	 * f4 ::= SectionsScope()
	 */
	@Override
	public void visit(SectionsConstruct n) {
		assert (false);
	}

	/**
	 * f0 ::= OmpPragma()
	 * f1 ::= <SINGLE>
	 * f2 ::= SingleClauseList()
	 * f3 ::= OmpEol()
	 * f4 ::= Statement()
	 */
	@Override
	public void visit(SingleConstruct n) {
		assert (false);
	}

	/**
	 * f0 ::= OmpPragma()
	 * f1 ::= <TASK>
	 * f2 ::= ( TaskClause() )*
	 * f3 ::= OmpEol()
	 * f4 ::= Statement()
	 */
	@Override
	public void visit(TaskConstruct n) {
		n.getInfo().getCFGInfo().getBody().accept(this);
	}

	/**
	 * f0 ::= <FINAL>
	 * f1 ::= "("
	 * f2 ::= Expression()
	 * f3 ::= ")"
	 */
	@Override
	public void visit(FinalClause n) {
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
	@Override
	public void visit(ParallelForConstruct n) {
		assert (false);
	}

	/**
	 * f0 ::= OmpPragma()
	 * f1 ::= <PARALLEL>
	 * f2 ::= <SECTIONS>
	 * f3 ::= UniqueParallelOrDataClauseList()
	 * f4 ::= OmpEol()
	 * f5 ::= SectionsScope()
	 */
	@Override
	public void visit(ParallelSectionsConstruct n) {
		assert (false);
	}

	/**
	 * f0 ::= OmpPragma()
	 * f1 ::= <MASTER>
	 * f2 ::= OmpEol()
	 * f3 ::= Statement()
	 */
	@Override
	public void visit(MasterConstruct n) {
		n.getInfo().getCFGInfo().getBody().accept(this);
	}

	/**
	 * f0 ::= OmpPragma()
	 * f1 ::= <CRITICAL>
	 * f2 ::= ( RegionPhrase() )?
	 * f3 ::= OmpEol()
	 * f4 ::= Statement()
	 */
	@Override
	public void visit(CriticalConstruct n) {
		n.getInfo().getCFGInfo().getBody().accept(this);
	}

	/**
	 * f0 ::= OmpPragma()
	 * f1 ::= <ATOMIC>
	 * f2 ::= ( AtomicClause() )?
	 * f3 ::= OmpEol()
	 * f4 ::= ExpressionStatement()
	 */
	@Override
	public void visit(AtomicConstruct n) {
	}

	/**
	 * f0 ::= OmpPragma()
	 * f1 ::= <FLUSH>
	 * f2 ::= ( FlushVars() )?
	 * f3 ::= OmpEol()
	 */
	@Override
	public void visit(FlushDirective n) {
	}

	/**
	 * f0 ::= OmpPragma()
	 * f1 ::= <ORDERED>
	 * f2 ::= OmpEol()
	 * f3 ::= Statement()
	 */
	@Override
	public void visit(OrderedConstruct n) {
		n.getInfo().getCFGInfo().getBody().accept(this);
	}

	/**
	 * f0 ::= OmpPragma()
	 * f1 ::= <BARRIER>
	 * f2 ::= OmpEol()
	 */
	@Override
	public void visit(BarrierDirective n) {
	}

	/**
	 * f0 ::= OmpPragma()
	 * f1 ::= <TASKWAIT>
	 * f2 ::= OmpEol()
	 */
	@Override
	public void visit(TaskwaitDirective n) {
	}

	/**
	 * f0 ::= OmpPragma()
	 * f1 ::= <TASKYIELD>
	 * f2 ::= OmpEol()
	 */
	@Override
	public void visit(TaskyieldDirective n) {
	}

	/**
	 * f0 ::= ( Expression() )?
	 * f1 ::= ";"
	 */
	@Override
	public void visit(ExpressionStatement n) {
	}

	/**
	 * f0 ::= "{"
	 * f1 ::= ( CompoundStatementElement() )*
	 * f2 ::= "}"
	 */
	@Override
	public void visit(CompoundStatement n) {
		String startLabelName = null;
		String startTimerName = null;
		boolean visit = true;
		for (Node elem : n.getInfo().getCFGInfo().getElementList()) {
			if (elem instanceof Declaration) {
				continue;
			}
			Statement stmt = (Statement) elem;
			boolean startFound = false;
			boolean endFound = false;
			for (Label l : stmt.getInfo().getLabelAnnotations()) {
				if (l instanceof SimpleLabel) {
					String lName = ((SimpleLabel) l).getLabelName();
					if (lName.startsWith("LIndependent_start")) {
						startFound = true;
						String timerName = "tIndependent_start_" + id;
						startTimerName = timerName;
						startLabelName = lName;
						Declaration timerDec = FrontEnd.parseAndNormalize("double " + timerName + ";",
								Declaration.class);
						InsertImmediateSuccessor.insert(stmt, timerDec);
						Statement timerStmt = FrontEnd.parseAndNormalize(timerName + " = " + "omp_get_wtime();",
								Statement.class);
						InsertImmediateSuccessor.insert(timerDec, timerStmt);
					} else if (lName.startsWith("LIndependent_end")) {
						endFound = true;
						String timerName = "tIndependent_end_" + id;
						Declaration timerDec = FrontEnd.parseAndNormalize("double " + timerName + ";",
								Declaration.class);
						InsertImmediateSuccessor.insert(stmt, timerDec);
						Statement timerStmt = FrontEnd.parseAndNormalize(timerName + " = " + "omp_get_wtime();",
								Statement.class);
						InsertImmediateSuccessor.insert(timerDec, timerStmt);
						Statement timeDiff = FrontEnd.parseAndNormalize(
								timerName + "=" + timerName + "-" + startTimerName + ";", Statement.class);
						InsertImmediateSuccessor.insert(timerStmt, timeDiff);
						String print = "fprintf(independentFile," + "\"" + startLabelName + "-" + lName
								+ ": %.10lf\\n\", " + timerName + ");";
						Statement printStmt = FrontEnd.parseAndNormalize(print, Statement.class);
						InsertImmediateSuccessor.insert(timeDiff, printStmt);
						id++;
					}
				}
			}
			if (startFound) {
				visit = false;
			}
			if (endFound) {
				visit = true;
				continue;
			}
			if (visit) {
				elem.accept(this);
			}

		}
	}

	/**
	 * f0 ::= <IF>
	 * f1 ::= "("
	 * f2 ::= Expression()
	 * f3 ::= ")"
	 * f4 ::= Statement()
	 * f5 ::= ( <ELSE> Statement() )?
	 */
	@Override
	public void visit(IfStatement n) {
		n.getInfo().getCFGInfo().getThenBody().accept(this);
		if (n.getInfo().getCFGInfo().hasElseBody()) {
			n.getInfo().getCFGInfo().getElseBody().accept(this);
		}

		for (Label l : n.getInfo().getLabelAnnotations()) {
			if (l instanceof SimpleLabel) {
				SimpleLabel sl = (SimpleLabel) l;
				String lName = sl.getLabelName();

				if (lName.startsWith("LabelIf")) {
					String printIf = "fprintf(independentFile, \"" + lName + ": 1\\n\");";
					CompoundStatement ifBody = (CompoundStatement) n.getInfo().getCFGInfo().getThenBody();
					Statement printStmtIf = FrontEnd.parseAndNormalize(printIf, Statement.class);
					ifBody.getInfo().getCFGInfo().addElement(0, printStmtIf);

					if (n.getInfo().getCFGInfo().hasElseBody()) {
						String printElse = "fprintf(independentFile, \"" + lName + ": 0\\n\");";
						CompoundStatement elseBody = (CompoundStatement) n.getInfo().getCFGInfo().getElseBody();
						Statement printStmtElse = FrontEnd.parseAndNormalize(printElse, Statement.class);
						elseBody.getInfo().getCFGInfo().addElement(0, printStmtElse);
					} else {
						String printElse = "{fprintf(independentFile, \"" + lName + ": 0\\n\");}";
						n.getInfo().getCFGInfo().setElseBody(FrontEnd.parseAndNormalize(printElse, Statement.class));
					}
				}
			}
		}
	}

	/**
	 * f0 ::= <SWITCH>
	 * f1 ::= "("
	 * f2 ::= Expression()
	 * f3 ::= ")"
	 * f4 ::= Statement()
	 */
	@Override
	public void visit(SwitchStatement n) {
		assert (false);
	}

	/**
	 * f0 ::= <WHILE>
	 * f1 ::= "("
	 * f2 ::= Expression()
	 * f3 ::= ")"
	 * f4 ::= Statement()
	 */
	@Override
	public void visit(WhileStatement n) {
		n.getInfo().getCFGInfo().getBody().accept(this);
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
	@Override
	public void visit(DoStatement n) {
		n.getInfo().getCFGInfo().getBody().accept(this);
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
	@Override
	public void visit(ForStatement n) {
		n.getInfo().getCFGInfo().getBody().accept(this);
	}

	/**
	 * f0 ::= <GOTO>
	 * f1 ::= <IDENTIFIER>
	 * f2 ::= ";"
	 */
	@Override
	public void visit(GotoStatement n) {
	}

	/**
	 * f0 ::= <CONTINUE>
	 * f1 ::= ";"
	 */
	@Override
	public void visit(ContinueStatement n) {
	}

	/**
	 * f0 ::= <BREAK>
	 * f1 ::= ";"
	 */
	@Override
	public void visit(BreakStatement n) {
	}

	/**
	 * f0 ::= <RETURN>
	 * f1 ::= ( Expression() )?
	 * f2 ::= ";"
	 */
	@Override
	public void visit(ReturnStatement n) {
	}

	/**
	 * f0 ::= AssignmentExpression()
	 * f1 ::= ( "," AssignmentExpression() )*
	 */
	@Override
	public void visit(Expression n) {
	}

	@Override
	public void visit(DummyFlushDirective n) {
	}

	@Override
	public void visit(PreCallNode n) {
	}

	@Override
	public void visit(PostCallNode n) {
	}

	@Override
	public void visit(CallStatement n) {
		for (FunctionDefinition fd : n.getInfo().getCalledDefinitions()) {
			if (visitedFunctions.contains(fd)) {
				continue;
			}
			if (!fd.getInfo().isRecursive()) {
				visitedFunctions.add(fd);
				fd.accept(this);
			}
		}
	}

}
