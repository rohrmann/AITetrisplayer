package baseFunctions;
import tetris.State;


public class BFNumHolesDiff implements BaseFunction {

	@Override
	public double evaluate(State oldState, State newState) {
		return newState.getNumHoles() - oldState.getNumHoles();
	}

}
