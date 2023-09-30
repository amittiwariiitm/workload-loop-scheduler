package elasticBarrier;

import imop.lib.util.CellSet;

public class CostExpressionArgument {
	CellSet mustWrites;
	CellSet openReads;
	public CostExpressionArgument() {
		mustWrites = new CellSet();
		openReads = new CellSet();
	}
	
	public CostExpressionArgument(CostExpressionArgument cea) {
		mustWrites = new CellSet(cea.mustWrites);
		openReads = new CellSet(cea.openReads);
	}
}
