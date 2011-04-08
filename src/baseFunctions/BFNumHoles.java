package baseFunctions;
import tetris.State;


public class BFNumHoles implements BaseFunction {

	@Override
	public double evaluate(State oldState, State newState) {
		return newState.getNumHoles();
	}

}
