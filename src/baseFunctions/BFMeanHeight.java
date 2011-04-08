package baseFunctions;
import tetris.State;


public class BFMeanHeight implements BaseFunction {

	@Override
	public double evaluate(State oldState, State newState) {
		return newState.getMeanHeight();
	}

}
