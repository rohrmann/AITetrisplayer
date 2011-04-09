/**
 * base function which returns the mean height of the new state.
 * @author rohrmann
 *
 */
public class BFMeanHeight implements BaseFunction {

	@Override
	public double evaluate(StateEx oldState, StateEx newState) {
		return newState.getMeanHeight();
	}

}
