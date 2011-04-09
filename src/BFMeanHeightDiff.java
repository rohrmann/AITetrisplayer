public class BFMeanHeightDiff implements BaseFunction {

	@Override
	public double evaluate(StateEx oldState, StateEx newState) {
		return newState.getMeanHeight() - oldState.getMeanHeight();
	}

}
