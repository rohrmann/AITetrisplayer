package baseFunctions;
import tetris.State;


public class BFMeanHeightDiff implements BaseFunction {

	@Override
	public double evaluate(State oldState, State newState) {
		return newState.getMeanHeight() - oldState.getMeanHeight();
	}

}
