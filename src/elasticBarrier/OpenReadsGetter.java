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
import imop.baseVisitor.GJDepthFirstProcess;
import imop.baseVisitor.cfgTraversals.GJDepthFirstCFG;
import imop.lib.analysis.flowanalysis.Cell;
import imop.lib.cfg.info.CFGInfo;
import imop.lib.cfg.info.ForStatementCFGInfo;
import imop.lib.util.CellSet;
import imop.lib.util.Misc;

public class OpenReadsGetter {

	public static CellSet getOpenReads(Node node, CellSet mustWrites) {
		if (node instanceof Expression) {
			OpenReadsGetterInternal accessGetter = new OpenReadsGetterInternal();
			return node.accept(accessGetter, mustWrites);
		} else {
			node = Misc.getCFGNodeFor(node);
			if (node == null) {
				Misc.warnDueToLackOfFeature("Cannot obtain reads for non-executable statements.", node);
				return null;
			}
			OpenReadsGetterInternal accessGetter = new OpenReadsGetterInternal();
			return node.accept(accessGetter, mustWrites);
		}
	}
	
	/**
	 * Provides default methods for processing all the CFG nodes.
	 * Traversal shall be defined as per the requirements.
	 * Note: This visitor is just for convenience in handling all the CFGs.
	 */
	private static class OpenReadsGetterInternal extends GJDepthFirstCFG<CellSet, CellSet> {

		@Override
		public CellSet initProcess(Node n, CellSet mustWrites) {
			return null;
		}

		@Override
		public CellSet endProcess(Node n, CellSet mustWrites) {
			return null;
		}

		/**
		 * @deprecated
		 * @param n
		 * @param mustWrites
		 * @return
		 */
		@Deprecated
		public CellSet processCalls(Node n, CellSet mustWrites) {
			assert (false);
			return null;
		}

		/**
		 * Special Node
		 */
		@Override
		public CellSet visit(BeginNode n, CellSet mustWrites) {
			return Misc.setMinus(new CellSet(n.getInfo().getReads()), mustWrites);
		}

		/**
		 * Special Node
		 */
		@Override
		public CellSet visit(EndNode n, CellSet mustWrites) {
			return Misc.setMinus(new CellSet(n.getInfo().getReads()), mustWrites);
		}

		/**
		 * f0 ::= ( DeclarationSpecifiers() )?
		 * f1 ::= Declarator()
		 * f2 ::= ( DeclarationList() )?
		 * f3 ::= CompoundStatement()
		 */
		@Override
		public CellSet visit(FunctionDefinition n, CellSet mustWrites) {
			CellSet pSet = new CellSet();
			for (ParameterDeclaration pd : n.getInfo().getCFGInfo().getParameterDeclarationList()) {
				Cell c = pd.getInfo().getDeclaredSymbol();
				pSet.add(c);
			}
			pSet.addAll(mustWrites);
			CellSet _ret = n.getInfo().getCFGInfo().getBody().accept(this, pSet);
			return _ret;
		}

		/**
		 * f0 ::= DeclarationSpecifiers()
		 * f1 ::= ( InitDeclaratorList() )?
		 * f2 ::= ";"
		 */
		@Override
		public CellSet visit(Declaration n, CellSet mustWrites) {
			return Misc.setMinus(new CellSet(n.getInfo().getReads()), mustWrites);
		}

		/**
		 * f0 ::= DeclarationSpecifiers()
		 * f1 ::= ParameterAbstraction()
		 */
		@Override
		public CellSet visit(ParameterDeclaration n, CellSet mustWrites) {
			return Misc.setMinus(new CellSet(n.getInfo().getReads()), mustWrites);
		}

		/**
		 * f0 ::= "#"
		 * f1 ::= <UNKNOWN_CPP>
		 */
		@Override
		public CellSet visit(UnknownCpp n, CellSet mustWrites) {
			return Misc.setMinus(new CellSet(n.getInfo().getReads()), mustWrites);
		}

		/**
		 * f0 ::= OmpPragma()
		 * f1 ::= ParallelDirective()
		 * f2 ::= Statement()
		 */
		@Override
		public CellSet visit(ParallelConstruct n, CellSet mustWrites) {
			return n.getInfo().getCFGInfo().getBody().accept(this, mustWrites);
		}

		/**
		 * f0 ::= "#"
		 * f1 ::= <PRAGMA>
		 * f2 ::= <UNKNOWN_CPP>
		 */
		@Override
		public CellSet visit(UnknownPragma n, CellSet mustWrites) {
			return Misc.setMinus(new CellSet(n.getInfo().getReads()), mustWrites);
		}

		/**
		 * f0 ::= <IF>
		 * f1 ::= "("
		 * f2 ::= Expression()
		 * f3 ::= ")"
		 */
		@Override
		public CellSet visit(IfClause n, CellSet mustWrites) {
			return Misc.setMinus(new CellSet(n.getInfo().getReads()), mustWrites);
		}

		/**
		 * f0 ::= <NUM_THREADS>
		 * f1 ::= "("
		 * f2 ::= Expression()
		 * f3 ::= ")"
		 */
		@Override
		public CellSet visit(NumThreadsClause n, CellSet mustWrites) {
			return Misc.setMinus(new CellSet(n.getInfo().getReads()), mustWrites);
		}

		/**
		 * f0 ::= OmpPragma()
		 * f1 ::= ForDirective()
		 * f2 ::= OmpForHeader()
		 * f3 ::= Statement()
		 */
		@Override
		public CellSet visit(ForConstruct n, CellSet mustWrites) {
			CellSet fsw = new CellSet(mustWrites);

			CellSet _ret = n.getInfo().getCFGInfo().getInitExpression().accept(this, fsw);
			fsw.addAll(MustWritesGetter.getMustWrites(n.getInfo().getCFGInfo().getInitExpression()));

			_ret.addAll(n.getInfo().getCFGInfo().getForConditionExpression().accept(this, fsw));
			fsw.addAll(MustWritesGetter.getMustWrites(n.getInfo().getCFGInfo().getForConditionExpression()));

			_ret.addAll(n.getInfo().getCFGInfo().getBody().accept(this, fsw));
			fsw.addAll(MustWritesGetter.getMustWrites(n.getInfo().getCFGInfo().getBody()));

			_ret.addAll(n.getInfo().getCFGInfo().getReinitExpression().accept(this, fsw));
			fsw.addAll(MustWritesGetter.getMustWrites(n.getInfo().getCFGInfo().getReinitExpression()));

			return _ret;
		}

		/**
		 * f0 ::= <IDENTIFIER>
		 * f1 ::= "="
		 * f2 ::= Expression()
		 */
		@Override
		public CellSet visit(OmpForInitExpression n, CellSet mustWrites) {
			return Misc.setMinus(new CellSet(n.getInfo().getReads()), mustWrites);
		}

		/**
		 * f0 ::= OmpForLTCondition()
		 * | OmpForLECondition()
		 * | OmpForGTCondition()
		 * | OmpForGECondition()
		 */
		@Override
		public CellSet visit(OmpForCondition n, CellSet mustWrites) {
			return Misc.setMinus(new CellSet(n.getInfo().getReads()), mustWrites);
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
		public CellSet visit(OmpForReinitExpression n, CellSet mustWrites) {
			return Misc.setMinus(new CellSet(n.getInfo().getReads()), mustWrites);
		}

		/**
		 * f0 ::= OmpPragma()
		 * f1 ::= <SECTIONS>
		 * f2 ::= NowaitDataClauseList()
		 * f3 ::= OmpEol()
		 * f4 ::= SectionsScope()
		 */
		@Override
		public CellSet visit(SectionsConstruct n, CellSet mustWrites) {
			return n.getInfo().getCFGInfo().getBody().accept(this, mustWrites);
		}

		/**
		 * f0 ::= OmpPragma()
		 * f1 ::= <SINGLE>
		 * f2 ::= SingleClauseList()
		 * f3 ::= OmpEol()
		 * f4 ::= Statement()
		 */
		@Override
		public CellSet visit(SingleConstruct n, CellSet mustWrites) {
			return n.getInfo().getCFGInfo().getBody().accept(this, mustWrites);
		}

		/**
		 * f0 ::= OmpPragma()
		 * f1 ::= <TASK>
		 * f2 ::= ( TaskClause() )*
		 * f3 ::= OmpEol()
		 * f4 ::= Statement()
		 */
		@Override
		public CellSet visit(TaskConstruct n, CellSet mustWrites) {
			return n.getInfo().getCFGInfo().getBody().accept(this, mustWrites);
		}

		/**
		 * f0 ::= <FINAL>
		 * f1 ::= "("
		 * f2 ::= Expression()
		 * f3 ::= ")"
		 */
		@Override
		public CellSet visit(FinalClause n, CellSet mustWrites) {
			return Misc.setMinus(new CellSet(n.getInfo().getReads()), mustWrites);
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
		public CellSet visit(ParallelForConstruct n, CellSet mustWrites) {
			assert (false);
			return n.getInfo().getCFGInfo().getBody().accept(this, mustWrites);
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
		public CellSet visit(ParallelSectionsConstruct n, CellSet mustWrites) {
			assert (false);
			return n.getInfo().getCFGInfo().getBody().accept(this, mustWrites);
		}

		/**
		 * f0 ::= OmpPragma()
		 * f1 ::= <MASTER>
		 * f2 ::= OmpEol()
		 * f3 ::= Statement()
		 */
		@Override
		public CellSet visit(MasterConstruct n, CellSet mustWrites) {
			return n.getInfo().getCFGInfo().getBody().accept(this, mustWrites);
		}

		/**
		 * f0 ::= OmpPragma()
		 * f1 ::= <CRITICAL>
		 * f2 ::= ( RegionPhrase() )?
		 * f3 ::= OmpEol()
		 * f4 ::= Statement()
		 */
		@Override
		public CellSet visit(CriticalConstruct n, CellSet mustWrites) {
			return n.getInfo().getCFGInfo().getBody().accept(this, mustWrites);
		}

		/**
		 * f0 ::= OmpPragma()
		 * f1 ::= <ATOMIC>
		 * f2 ::= ( AtomicClause() )?
		 * f3 ::= OmpEol()
		 * f4 ::= ExpressionStatement()
		 */
		@Override
		public CellSet visit(AtomicConstruct n, CellSet mustWrites) {
			return n.getInfo().getCFGInfo().getBody().accept(this, mustWrites);
		}

		/**
		 * f0 ::= OmpPragma()
		 * f1 ::= <FLUSH>
		 * f2 ::= ( FlushVars() )?
		 * f3 ::= OmpEol()
		 */
		@Override
		public CellSet visit(FlushDirective n, CellSet mustWrites) {
			return Misc.setMinus(new CellSet(n.getInfo().getReads()), mustWrites);
		}

		/**
		 * f0 ::= OmpPragma()
		 * f1 ::= <ORDERED>
		 * f2 ::= OmpEol()
		 * f3 ::= Statement()
		 */
		@Override
		public CellSet visit(OrderedConstruct n, CellSet mustWrites) {
			return n.getInfo().getCFGInfo().getBody().accept(this, mustWrites);
		}

		/**
		 * f0 ::= OmpPragma()
		 * f1 ::= <BARRIER>
		 * f2 ::= OmpEol()
		 */
		@Override
		public CellSet visit(BarrierDirective n, CellSet mustWrites) {
			return Misc.setMinus(new CellSet(n.getInfo().getReads()), mustWrites);
		}

		/**
		 * f0 ::= OmpPragma()
		 * f1 ::= <TASKWAIT>
		 * f2 ::= OmpEol()
		 */
		@Override
		public CellSet visit(TaskwaitDirective n, CellSet mustWrites) {
			return Misc.setMinus(new CellSet(n.getInfo().getReads()), mustWrites);
		}

		/**
		 * f0 ::= OmpPragma()
		 * f1 ::= <TASKYIELD>
		 * f2 ::= OmpEol()
		 */
		@Override
		public CellSet visit(TaskyieldDirective n, CellSet mustWrites) {
			return Misc.setMinus(new CellSet(n.getInfo().getReads()), mustWrites);
		}

		/**
		 * f0 ::= ( Expression() )?
		 * f1 ::= ";"
		 */
		@Override
		public CellSet visit(ExpressionStatement n, CellSet mustWrites) {
			return Misc.setMinus(new CellSet(n.getInfo().getReads()), mustWrites);
		}

		/**
		 * f0 ::= "{"
		 * f1 ::= ( CompoundStatementElement() )*
		 * f2 ::= "}"
		 */
		@Override
		public CellSet visit(CompoundStatement n, CellSet mustWrites) {
			CellSet fsw = new CellSet(mustWrites);
			CellSet _ret = new CellSet();

			for (Node elem : n.getInfo().getCFGInfo().getElementList()) {
				_ret.addAll(elem.accept(this, fsw));
				fsw.addAll(MustWritesGetter.getMustWrites(elem));
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
		@Override
		public CellSet visit(IfStatement n, CellSet mustWrites) {
			CellSet fsw = new CellSet(mustWrites);

			CellSet _ret = n.getInfo().getCFGInfo().getPredicate().accept(this, fsw);
			fsw.addAll(MustWritesGetter.getMustWrites(n.getInfo().getCFGInfo().getPredicate()));

			_ret.addAll(n.getInfo().getCFGInfo().getThenBody().accept(this, fsw));

			if (n.getInfo().getCFGInfo().hasElseBody()) {
				_ret.addAll(n.getInfo().getCFGInfo().getElseBody().accept(this, fsw));
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
		@Override
		public CellSet visit(SwitchStatement n, CellSet mustWrites) {
			Misc.warnDueToLackOfFeature("Not Handling Switch Stmt", n);
			return new CellSet();
		}

		/**
		 * f0 ::= <WHILE>
		 * f1 ::= "("
		 * f2 ::= Expression()
		 * f3 ::= ")"
		 * f4 ::= Statement()
		 */
		@Override
		public CellSet visit(WhileStatement n, CellSet mustWrites) {
			CellSet fsw = new CellSet(mustWrites);
			CellSet _ret = new CellSet();

			_ret.addAll(n.getInfo().getCFGInfo().getPredicate().accept(this, fsw));
			fsw.addAll(MustWritesGetter.getMustWrites(n.getInfo().getCFGInfo().getPredicate()));

			_ret.addAll(n.getInfo().getCFGInfo().getBody().accept(this, fsw));
			fsw.addAll(MustWritesGetter.getMustWrites(n.getInfo().getCFGInfo().getBody()));

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
		@Override
		public CellSet visit(DoStatement n, CellSet mustWrites) {
			CellSet fsw = new CellSet(mustWrites);
			CellSet _ret = new CellSet();

			_ret.addAll(n.getInfo().getCFGInfo().getBody().accept(this, fsw));
			fsw.addAll(MustWritesGetter.getMustWrites(n.getInfo().getCFGInfo().getBody()));

			_ret.addAll(n.getInfo().getCFGInfo().getPredicate().accept(this, fsw));
			fsw.addAll(MustWritesGetter.getMustWrites(n.getInfo().getCFGInfo().getPredicate()));

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
		@Override
		public CellSet visit(ForStatement n, CellSet mustWrites) {
			CellSet fsw = new CellSet(mustWrites);

			ForStatementCFGInfo forCFG = n.getInfo().getCFGInfo();
			CellSet _ret = new CellSet();

			if (forCFG.hasInitExpression()) {
				_ret.addAll(forCFG.getInitExpression().accept(this, fsw));
				fsw.addAll(MustWritesGetter.getMustWrites(forCFG.getInitExpression()));
			}

			if (forCFG.hasTerminationExpression()) {
				_ret.addAll(forCFG.getTerminationExpression().accept(this, fsw));
				fsw.addAll(MustWritesGetter.getMustWrites(forCFG.getTerminationExpression()));
			}

			_ret.addAll(forCFG.getBody().accept(this, fsw));
			fsw.addAll(MustWritesGetter.getMustWrites(forCFG.getBody()));

			if (forCFG.hasStepExpression()) {
				_ret.addAll(forCFG.getStepExpression().accept(this, fsw));
				fsw.addAll(MustWritesGetter.getMustWrites(forCFG.getStepExpression()));
			}

			return _ret;
		}

		/**
		 * f0 ::= <GOTO>
		 * f1 ::= <IDENTIFIER>
		 * f2 ::= ";"
		 */
		@Override
		public CellSet visit(GotoStatement n, CellSet mustWrites) {
			return Misc.setMinus(new CellSet(n.getInfo().getReads()), mustWrites);
		}

		/**
		 * f0 ::= <CONTINUE>
		 * f1 ::= ";"
		 */
		@Override
		public CellSet visit(ContinueStatement n, CellSet mustWrites) {
			return Misc.setMinus(new CellSet(n.getInfo().getReads()), mustWrites);
		}

		/**
		 * f0 ::= <BREAK>
		 * f1 ::= ";"
		 */
		@Override
		public CellSet visit(BreakStatement n, CellSet mustWrites) {
			return Misc.setMinus(new CellSet(n.getInfo().getReads()), mustWrites);
		}

		/**
		 * f0 ::= <RETURN>
		 * f1 ::= ( Expression() )?
		 * f2 ::= ";"
		 */
		@Override
		public CellSet visit(ReturnStatement n, CellSet mustWrites) {
			return Misc.setMinus(new CellSet(n.getInfo().getReads()), mustWrites);
		}

		/**
		 * f0 ::= AssignmentExpression()
		 * f1 ::= ( "," AssignmentExpression() )*
		 */
		@Override
		public CellSet visit(Expression n, CellSet mustWrites) {
			return Misc.setMinus(new CellSet(n.getInfo().getReads()), mustWrites);
		}

		@Override
		public CellSet visit(DummyFlushDirective n, CellSet mustWrites) {
			return Misc.setMinus(new CellSet(n.getInfo().getReads()), mustWrites);
		}

		@Override
		public CellSet visit(PreCallNode n, CellSet mustWrites) {
			return Misc.setMinus(new CellSet(n.getInfo().getReads()), mustWrites);
		}

		@Override
		public CellSet visit(PostCallNode n, CellSet mustWrites) {
			return Misc.setMinus(new CellSet(n.getInfo().getReads()), mustWrites);
		}

		@Override
		public CellSet visit(CallStatement n, CellSet mustWrites) {
			CellSet fsw = new CellSet(mustWrites);
			CellSet _ret = n.getInfo().getCFGInfo().getPreCallNode().accept(this, fsw);

			for (FunctionDefinition fd : n.getInfo().getCalledDefinitions()) {
				_ret.addAll(fd.accept(this, fsw));
			}
			return _ret;
		}
	}

}