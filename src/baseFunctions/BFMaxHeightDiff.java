package baseFunctions;
import tetris.State;


public class BFMaxHeightDiff implements BaseFunction {

	@Override
	public double evaluate(State oldState, State newState) {
		return newState.getMaxHeight() - oldState.getMaxHeight();
	}

}