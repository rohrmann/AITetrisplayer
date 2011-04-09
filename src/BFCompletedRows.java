/**
 * base function which returns the completed rows after one move
 * @author rohrmann
 *
 */
public class BFCompletedRows implements BaseFunction {

	@Override
	public double evaluate(StateEx oldState, StateEx newState) {
		return newState.getRowsCleared() - oldState.getRowsCleared();
	}

}
