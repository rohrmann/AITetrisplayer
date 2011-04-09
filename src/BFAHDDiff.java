/**
 * base function which calculates the difference of the absolute height difference between the old and the
 * new state. The absoulte height difference is the sum of the absolute height differences of adjacent columns.
 * @author rohrmann
 *
 */
public class BFAHDDiff implements BaseFunction {

	@Override
	public double evaluate(StateEx oldState, StateEx newState) {
		return newState.getAbsoluteHeightDiff()
				- oldState.getAbsoluteHeightDiff();
	}

}
