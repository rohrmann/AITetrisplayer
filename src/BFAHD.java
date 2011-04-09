
public class BFAHD implements BaseFunction {

	@Override
	public double evaluate(StateEx oldState, StateEx newState) {
		return newState.getAbsoluteHeightDiff();
	}

}
