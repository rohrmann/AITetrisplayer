/**
 * Base function which returns the change of the mean height between the new and the old state.s
 * @author rohrmann
 *
 */
public class BFMeanHeightDiff implements BaseFunction {

	@Override
	public double evaluate(StateEx oldState, StateEx newState) {
		return newState.getMeanHeight() - oldState.getMeanHeight();
	}

}
