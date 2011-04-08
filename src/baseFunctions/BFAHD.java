package baseFunctions;
import tetris.State;


public class BFAHD implements BaseFunction {

	@Override
	public double evaluate(State oldState, State newState) {
		return newState.getAbsoluteHeightDiff();
	}

}
