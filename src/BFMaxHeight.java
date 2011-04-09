/**
 * base function which returns the maximum height of the new state.
 * @author rohrmann
 *
 */
public class BFMaxHeight implements BaseFunction {

	@Override
	public double evaluate(StateEx oldState, StateEx newState) {
		return newState.getMaxHeight();
	}

}
