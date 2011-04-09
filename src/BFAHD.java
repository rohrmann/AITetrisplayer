/**
 * base function which calculates the absolute height difference = sum of absolute height differences of adjacent columns
 * @author rohrmann
 *
 */
public class BFAHD implements BaseFunction {

	@Override
	public double evaluate(StateEx oldState, StateEx newState) {
		return newState.getAbsoluteHeightDiff();
	}

}
