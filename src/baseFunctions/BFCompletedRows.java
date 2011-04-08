package baseFunctions;
import tetris.State;


public class BFCompletedRows implements BaseFunction {

	@Override
	public double evaluate(State oldState, State newState) {
		return newState.getRowsCleared() - oldState.getRowsCleared();
	}

}
