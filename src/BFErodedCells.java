/**
 * base function which returns the product of the completed rows and the eroded cells of the last place
 * piece.
 * @author rohrmann
 *
 */
public class BFErodedCells implements BaseFunction {

	@Override
	public double evaluate(StateEx oldState, StateEx newState) {
		return newState.getErodedCells()
				* (newState.getRowsCleared() - oldState.getRowsCleared());
	}

}
