package baseFunctions;
import tetris.State;


public class BFAHDDiff implements BaseFunction {

	@Override
	public double evaluate(State oldState, State newState) {
		return newState.getAbsoluteHeightDiff() - oldState.getAbsoluteHeightDiff();
	}

}
