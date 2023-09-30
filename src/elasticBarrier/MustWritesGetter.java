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
import imop.baseVisitor.cfgTraversals.GJNoArguDepthFirstCFG;
import imop.lib.analysis.flowanalysis.Cell;
import imop.lib.cfg.info.ForStatementCFGInfo;
import imop.lib.util.CellList;
import imop.lib.util.CellSet;
import imop.lib.util.Misc;

public class MustWritesGetter {

	public static CellList getMustWrites(Node n) {
		if (!(n instanceof Expression)) {
			n = Misc.getCFGNodeFor(n);
		}
		
		MustWritesGetterInternal fswi = new MustWritesGetterInternal();
		return n.accept(fswi);
	}
	
	
	
	/**
	 * This returns a Set of variables that are For Sure (unconditionally) written
	 * by the visited nodes.
	 * Assumption: All loops will execute at least once.
	 */
	private static class MustWritesGetterInternal extends GJNoArguDepthFirstCFG<CellList> {

		public CellList initProcess(Node n) {
			return null;
		}

		public CellList endProcess(Node n) {
			return null;
		}

		/**
		 * @deprecated
		 * @param n
		 * @return
		 */
		@Deprecated
		public CellList processCalls(Node n) {
			return null;
		}

		/**
		 * Special Node
		 */
		public CellList visit(BeginNode n) {
			return new CellList();
		}

		/**
		 * Special Node
		 */
		public CellList visit(EndNode n) {
			return new CellList();
		}

		/**
		 * f0 ::= ( DeclarationSpecifiers() )?
		 * f1 ::= Declarator()
		 * f2 ::= ( DeclarationList() )?
		 * f3 ::= CompoundStatement()
		 */
		public CellList visit(FunctionDefinition n) {
			return n.getInfo().getCFGInfo().getBody().accept(this);
		}

		/**
		 * f0 ::= DeclarationSpecifiers()
		 * f1 ::= ( InitDeclaratorList() )?
		 * f2 ::= ";"
		 */
		public CellList visit(Declaration n) {
			return n.getInfo().getWrites();
		}

		/**
		 * f0 ::= DeclarationSpecifiers()
		 * f1 ::= ParameterAbstraction()
		 */
		public CellList visit(ParameterDeclaration n) {
			assert (false);
			return null;
		}

		/**
		 * f0 ::= "#"
		 * f1 ::= <UNKNOWN_CPP>
		 */
		public CellList visit(UnknownCpp n) {
			return n.getInfo().getWrites();
		}

		/**
		 * f0 ::= OmpPragma()
		 * f1 ::= ParallelDirective()
		 * f2 ::= Statement()
		 */
		public CellList visit(ParallelConstruct n) {
			return n.getInfo().getCFGInfo().getBody().accept(this);
		}

		/**
		 * f0 ::= "#"
		 * f1 ::= <PRAGMA>
		 * f2 ::= <UNKNOWN_CPP>
		 */
		public CellList visit(UnknownPragma n) {
			return n.getInfo().getWrites();
		}

		/**
		 * f0 ::= <IF>
		 * f1 ::= "("
		 * f2 ::= Expression()
		 * f3 ::= ")"
		 */
		public CellList visit(IfClause n) {
			return n.getInfo().getWrites();
		}

		/**
		 * f0 ::= <NUM_THREADS>
		 * f1 ::= "("
		 * f2 ::= Expression()
		 * f3 ::= ")"
		 */
		public CellList visit(NumThreadsClause n) {
			return n.getInfo().getWrites();
		}

		/**
		 * f0 ::= OmpPragma()
		 * f1 ::= ForDirective()
		 * f2 ::= OmpForHeader()
		 * f3 ::= Statement()
		 */
		public CellList visit(ForConstruct n) {
			CellList _ret = new CellList();
			_ret.addAll(n.getInfo().getCFGInfo().getInitExpression().accept(this));
			_ret.addAll(n.getInfo().getCFGInfo().getForConditionExpression().accept(this));
			_ret.addAll(n.getInfo().getCFGInfo().getReinitExpression().accept(this));
			_ret.addAll(n.getInfo().getCFGInfo().getBody().accept(this));
			return _ret;
		}

		/**
		 * f0 ::= <IDENTIFIER>
		 * f1 ::= "="
		 * f2 ::= Expression()
		 */
		public CellList visit(OmpForInitExpression n) {
			return n.getInfo().getWrites();
		}

		/**
		 * f0 ::= OmpForLTCondition()
		 * | OmpForLECondition()
		 * | OmpForGTCondition()
		 * | OmpForGECondition()
		 */
		public CellList visit(OmpForCondition n) {
			return n.getInfo().getWrites();
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
		public CellList visit(OmpForReinitExpression n) {
			return n.getInfo().getWrites();
		}

		/**
		 * f0 ::= OmpPragma()
		 * f1 ::= <SECTIONS>
		 * f2 ::= NowaitDataClauseList()
		 * f3 ::= OmpEol()
		 * f4 ::= SectionsScope()
		 */
		public CellList visit(SectionsConstruct n) {
			return n.getInfo().getCFGInfo().getBody().accept(this);
		}

		/**
		 * f0 ::= OmpPragma()
		 * f1 ::= <SINGLE>
		 * f2 ::= SingleClauseList()
		 * f3 ::= OmpEol()
		 * f4 ::= Statement()
		 */
		public CellList visit(SingleConstruct n) {
			return n.getInfo().getCFGInfo().getBody().accept(this);
		}

		/**
		 * f0 ::= OmpPragma()
		 * f1 ::= <TASK>
		 * f2 ::= ( TaskClause() )*
		 * f3 ::= OmpEol()
		 * f4 ::= Statement()
		 */
		public CellList visit(TaskConstruct n) {
			return n.getInfo().getCFGInfo().getBody().accept(this);
		}

		/**
		 * f0 ::= <FINAL>
		 * f1 ::= "("
		 * f2 ::= Expression()
		 * f3 ::= ")"
		 */
		public CellList visit(FinalClause n) {
			return n.getInfo().getWrites();
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
		public CellList visit(ParallelForConstruct n) {
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
		public CellList visit(ParallelSectionsConstruct n) {
			assert (false);
			return null;
		}

		/**
		 * f0 ::= OmpPragma()
		 * f1 ::= <MASTER>
		 * f2 ::= OmpEol()
		 * f3 ::= Statement()
		 */
		public CellList visit(MasterConstruct n) {
			return n.getInfo().getCFGInfo().getBody().accept(this);
		}

		/**
		 * f0 ::= OmpPragma()
		 * f1 ::= <CRITICAL>
		 * f2 ::= ( RegionPhrase() )?
		 * f3 ::= OmpEol()
		 * f4 ::= Statement()
		 */
		public CellList visit(CriticalConstruct n) {
			return n.getInfo().getCFGInfo().getBody().accept(this);
		}

		/**
		 * f0 ::= OmpPragma()
		 * f1 ::= <ATOMIC>
		 * f2 ::= ( AtomicClause() )?
		 * f3 ::= OmpEol()
		 * f4 ::= ExpressionStatement()
		 */
		public CellList visit(AtomicConstruct n) {
			return n.getInfo().getCFGInfo().getBody().accept(this);
		}

		/**
		 * f0 ::= OmpPragma()
		 * f1 ::= <FLUSH>
		 * f2 ::= ( FlushVars() )?
		 * f3 ::= OmpEol()
		 */
		public CellList visit(FlushDirective n) {
			return n.getInfo().getWrites();
		}

		/**
		 * f0 ::= OmpPragma()
		 * f1 ::= <ORDERED>
		 * f2 ::= OmpEol()
		 * f3 ::= Statement()
		 */
		public CellList visit(OrderedConstruct n) {
			return n.getInfo().getCFGInfo().getBody().accept(this);
		}

		/**
		 * f0 ::= OmpPragma()
		 * f1 ::= <BARRIER>
		 * f2 ::= OmpEol()
		 */
		public CellList visit(BarrierDirective n) {
			return n.getInfo().getWrites();
		}

		/**
		 * f0 ::= OmpPragma()
		 * f1 ::= <TASKWAIT>
		 * f2 ::= OmpEol()
		 */
		public CellList visit(TaskwaitDirective n) {
			return n.getInfo().getWrites();
		}

		/**
		 * f0 ::= OmpPragma()
		 * f1 ::= <TASKYIELD>
		 * f2 ::= OmpEol()
		 */
		public CellList visit(TaskyieldDirective n) {
			return n.getInfo().getWrites();
		}

		/**
		 * f0 ::= ( Expression() )?
		 * f1 ::= ";"
		 */
		public CellList visit(ExpressionStatement n) {
			return n.getInfo().getWrites();
		}

		/**
		 * f0 ::= "{"
		 * f1 ::= ( CompoundStatementElement() )*
		 * f2 ::= "}"
		 */
		public CellList visit(CompoundStatement n) {
			CellList _ret = new CellList();
			for (Node elem : n.getInfo().getCFGInfo().getElementList()) {
				_ret.addAll(elem.accept(this));
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
		public CellList visit(IfStatement n) {
			CellList _ret = new CellList();
			if (n.getInfo().getCFGInfo().hasElseBody()) {
				CellList clThen = n.getInfo().getCFGInfo().getThenBody().accept(this);
				CellList clElse = n.getInfo().getCFGInfo().getElseBody().accept(this);
				for (Cell c : clThen) {
					if (clElse.contains(c)) {
						_ret.add(c);
					}
				}
			}
			_ret.addAll(n.getInfo().getCFGInfo().getPredicate().accept(this));
			return _ret;
		}

		/**
		 * f0 ::= <SWITCH>
		 * f1 ::= "("
		 * f2 ::= Expression()
		 * f3 ::= ")"
		 * f4 ::= Statement()
		 */
		public CellList visit(SwitchStatement n) {
			Misc.warnDueToLackOfFeature("Assuming that there are no unconditional writes in SwitchStmt", n);
			return new CellList();
		}

		/**
		 * f0 ::= <WHILE>
		 * f1 ::= "("
		 * f2 ::= Expression()
		 * f3 ::= ")"
		 * f4 ::= Statement()
		 */
		public CellList visit(WhileStatement n) {
			CellList _ret = n.getInfo().getCFGInfo().getPredicate().accept(this);
			_ret.addAll(n.getInfo().getCFGInfo().getBody().accept(this));
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
		public CellList visit(DoStatement n) {
			CellList _ret = n.getInfo().getCFGInfo().getPredicate().accept(this);
			_ret.addAll(n.getInfo().getCFGInfo().getBody().accept(this));
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
		public CellList visit(ForStatement n) {
			CellList _ret = new CellList();
			ForStatementCFGInfo forCFG = n.getInfo().getCFGInfo();
			if (forCFG.hasInitExpression()) {
				_ret.addAll(forCFG.getInitExpression().accept(this));
			}
			if (forCFG.hasTerminationExpression()) {
				_ret.addAll(forCFG.getTerminationExpression().accept(this));
			}
			if (forCFG.hasStepExpression()) {
				_ret.addAll(forCFG.getStepExpression().accept(this));
			}
			_ret.addAll(forCFG.getBody().accept(this));
			return _ret;
		}

		/**
		 * f0 ::= <GOTO>
		 * f1 ::= <IDENTIFIER>
		 * f2 ::= ";"
		 */
		public CellList visit(GotoStatement n) {
			return new CellList();
		}

		/**
		 * f0 ::= <CONTINUE>
		 * f1 ::= ";"
		 */
		public CellList visit(ContinueStatement n) {
			return new CellList();
		}

		/**
		 * f0 ::= <BREAK>
		 * f1 ::= ";"
		 */
		public CellList visit(BreakStatement n) {
			return new CellList();
		}

		/**
		 * f0 ::= <RETURN>
		 * f1 ::= ( Expression() )?
		 * f2 ::= ";"
		 */
		public CellList visit(ReturnStatement n) {
			return n.getInfo().getWrites();
		}

		/**
		 * f0 ::= AssignmentExpression()
		 * f1 ::= ( "," AssignmentExpression() )*
		 */
		public CellList visit(Expression n) {
			return n.getInfo().getWrites();
		}

		public CellList visit(DummyFlushDirective n) {
			return new CellList();
		}

		public CellList visit(PreCallNode n) {
			return new CellList();
		}

		public CellList visit(PostCallNode n) {
			return n.getInfo().getWrites();
		}

		public CellList visit(CallStatement n) {
			CellList _ret = new CellList();
			boolean firstSite = true;
			for (FunctionDefinition fd : n.getInfo().getCalledDefinitions()) {
				if (firstSite) {
					_ret.addAll(fd.accept(this));
					firstSite = false;
					continue;
				}

				CellSet tempSet = Misc.setIntersection(new CellSet(_ret), new CellSet(fd.accept(this)));
				_ret.clear();

				for (Cell c : tempSet) {
					_ret.add(c);
				}
			}
			return _ret;
		}
	}
}