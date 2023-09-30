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
import imop.parser.FrontEnd;

/**
 * assert(false); for all the leaves
 * For nodes with body, we simply call accept() on the body except for IfStmt.
 */
public class IndependentLabelPutter extends DepthFirstCFG {
	int id = 0;
	int idIf = 0;
	String labSubstr = "LIndependent";
	InputDependencyIdentifier idi = new InputDependencyIdentifier();
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
	}

	/**
	 * Special Node
	 */
	@Override
	public void visit(EndNode n) {
		assert (false);
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
		assert (false);
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
		assert (false);
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
		assert (false);
	}

	/**
	 * f0 ::= <IF>
	 * f1 ::= "("
	 * f2 ::= Expression()
	 * f3 ::= ")"
	 */
	@Override
	public void visit(IfClause n) {
		assert (false);
	}

	/**
	 * f0 ::= <NUM_THREADS>
	 * f1 ::= "("
	 * f2 ::= Expression()
	 * f3 ::= ")"
	 */
	@Override
	public void visit(NumThreadsClause n) {
		assert (false);
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
		assert (false);
	}

	/**
	 * f0 ::= OmpForLTCondition()
	 * | OmpForLECondition()
	 * | OmpForGTCondition()
	 * | OmpForGECondition()
	 */
	@Override
	public void visit(OmpForCondition n) {
		assert (false);
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
		assert (false);
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
		assert (false);
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
		assert (false);
	}

	/**
	 * f0 ::= OmpPragma()
	 * f1 ::= <FLUSH>
	 * f2 ::= ( FlushVars() )?
	 * f3 ::= OmpEol()
	 */
	@Override
	public void visit(FlushDirective n) {
		assert (false);
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
		assert (false);
	}

	/**
	 * f0 ::= OmpPragma()
	 * f1 ::= <TASKWAIT>
	 * f2 ::= OmpEol()
	 */
	@Override
	public void visit(TaskwaitDirective n) {
		assert (false);
	}

	/**
	 * f0 ::= OmpPragma()
	 * f1 ::= <TASKYIELD>
	 * f2 ::= OmpEol()
	 */
	@Override
	public void visit(TaskyieldDirective n) {
		assert (false);
	}

	/**
	 * f0 ::= ( Expression() )?
	 * f1 ::= ";"
	 */
	@Override
	public void visit(ExpressionStatement n) {
		assert (false);
	}

	/**
	 * f0 ::= "{"
	 * f1 ::= ( CompoundStatementElement() )*
	 * f2 ::= "}"
	 */
	@Override
	public void visit(CompoundStatement n) {
		CompoundStatement fcBody = n;
		boolean prevDep = true;
		boolean currDep = true;

		for (Node elem : fcBody.getInfo().getCFGInfo().getElementList()) {
			currDep = elem.accept(idi);

			if (prevDep && !currDep) {
				String labelStr1 = labSubstr + "_start_" + id + ": ;";
				Statement labelStmt1 = FrontEnd.parseAndNormalize(labelStr1, Statement.class);
				InsertImmediatePredecessor.insert(elem, labelStmt1);
				prevDep = false;
			}
			if (!prevDep && currDep) {
				String labelStr2 = labSubstr + "_end_" + id + ": ;";
				Statement labelStmt2 = FrontEnd.parseAndNormalize(labelStr2, Statement.class);
				InsertImmediatePredecessor.insert(elem, labelStmt2);
				id++;
				prevDep = true;
			}
			// Recursively place labels for code within the current Input-dependent element.
			if (currDep) {
				elem.accept(this);
			}
		}

		if (!currDep) {
			String labelStr2 = labSubstr + "_end_" + id + ": ;";
			Statement labelStmt2 = FrontEnd.parseAndNormalize(labelStr2, Statement.class);
			fcBody.getInfo().getCFGInfo().addAtLast(labelStmt2);
			id++;
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
		String labelStr = "LabelIf"+idIf++;
		n.getInfo().addLabelAnnotation(new SimpleLabel(n, labelStr));
		if (n.getInfo().getCFGInfo().hasElseBody()) {
			n.getInfo().getCFGInfo().getElseBody().accept(this);
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
		assert (false) : "Not Handling Switch Statement";
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
		assert (false);
	}

	/**
	 * f0 ::= <CONTINUE>
	 * f1 ::= ";"
	 */
	@Override
	public void visit(ContinueStatement n) {
		assert (false);
	}

	/**
	 * f0 ::= <BREAK>
	 * f1 ::= ";"
	 */
	@Override
	public void visit(BreakStatement n) {
		assert (false);
	}

	/**
	 * f0 ::= <RETURN>
	 * f1 ::= ( Expression() )?
	 * f2 ::= ";"
	 */
	@Override
	public void visit(ReturnStatement n) {
		assert (false);
	}

	/**
	 * f0 ::= AssignmentExpression()
	 * f1 ::= ( "," AssignmentExpression() )*
	 */
	@Override
	public void visit(Expression n) {
		assert (false);
	}

	@Override
	public void visit(DummyFlushDirective n) {
		assert (false);
	}

	@Override
	public void visit(PreCallNode n) {
		assert (false);
	}

	@Override
	public void visit(PostCallNode n) {
		assert (false);
	}

	@Override
	public void visit(CallStatement n) {
		for(FunctionDefinition fd: n.getInfo().getCalledDefinitions()) {
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
