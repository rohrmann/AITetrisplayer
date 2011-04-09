
/**
 * base function which calculates the difference of the absolute height difference between two adjacent columns
 * @author rohrmann
 *
 */
public class BFAAHDDiff implements BaseFunction {
	private int column;

	public BFAAHDDiff(int column) {
		this.column = column;
	}

	@Override
	public double evaluate(StateEx oldState, StateEx newState) {
		return Math.abs(newState.getTop(column) - newState.getTop(column + 1))
				- Math.abs(oldState.getTop(column) - newState.getTop(column)
						- 1);
	}

}