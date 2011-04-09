/**
 * base function which calculates the height difference of a specified column between the new and the old state
 * @author rohrmann
 *
 */
public class BFColumnHeightDiff implements BaseFunction {
	private int column;

	public BFColumnHeightDiff(int column) {
		this.column = column;
	}

	@Override
	public double evaluate(StateEx oldState, StateEx newState) {
		return newState.getTop(column) - oldState.getTop(column);
	}

}
