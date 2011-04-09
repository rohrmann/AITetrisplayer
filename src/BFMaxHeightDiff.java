public class BFMaxHeightDiff implements BaseFunction {

	@Override
	public double evaluate(StateEx oldState, StateEx newState) {
		return newState.getMaxHeight() - oldState.getMaxHeight();
	}

}
