/**
 * base function which returns the change of the maximum height between the new and the old state.
 * @author rohrmann
 *
 */
public class BFMaxHeightDiff implements BaseFunction {

	@Override
	public double evaluate(StateEx oldState, StateEx newState) {
		return newState.getMaxHeight() - oldState.getMaxHeight();
	}

}
