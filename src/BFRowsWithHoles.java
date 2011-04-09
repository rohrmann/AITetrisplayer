/**
 * Base function which returns the number of rows which contain at least one hole. In order to be a hole, an empty cell
 * has to be covered by a full cell which doesn't have to be directly above the empty cell.
 * @author rohrmann
 *
 */
public class BFRowsWithHoles implements BaseFunction {

	@Override
	public double evaluate(StateEx oldState, StateEx newState) {
		int result = 0;

		int[][] field = newState.getField();

		for (int r = 0; r < newState.getMaxHeight(); r++) {
			for (int c = 0; c < State.COLS; c++) {
				if (field[r][c] == 0 && newState.getTop(c) > c) {
					result++;
					break;
				}
			}
		}

		return result;
	}

}
