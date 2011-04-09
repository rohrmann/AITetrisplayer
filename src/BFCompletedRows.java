public class BFCompletedRows implements BaseFunction {

	@Override
	public double evaluate(StateEx oldState, StateEx newState) {
		return newState.getRowsCleared() - oldState.getRowsCleared();
	}

}
