
public class BFAHDDiff implements BaseFunction {

	@Override
	public double evaluate(StateEx oldState, StateEx newState) {
		return newState.getAbsoluteHeightDiff()
				- oldState.getAbsoluteHeightDiff();
	}

}
