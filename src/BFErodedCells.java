
public class BFErodedCells implements BaseFunction {

	@Override
	public double evaluate(StateEx oldState, StateEx newState) {
		return newState.getErodedCells()
				* (newState.getRowsCleared() - oldState.getRowsCleared());
	}

}
