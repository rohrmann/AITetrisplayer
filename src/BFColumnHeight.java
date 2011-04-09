/**
 * base functions which returns the height of a specified column
 * @author rohrmann
 *
 */
public class BFColumnHeight implements BaseFunction {
	private int index;

	public BFColumnHeight(int index) {
		this.index = index;
	}

	@Override
	public double evaluate(StateEx oldState, StateEx newState) {
		return newState.getTop(index);
	}

}
