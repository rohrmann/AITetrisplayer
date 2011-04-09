/**
 * base functions which calculates the column transition. A column transition occurs if a full cell
 * is directly below an empty cell in the same column or vice versa. The bottom row is considered to 
 * contain full cells.
 * @author rohrmann
 *
 */
public class BFColumnTransitions implements BaseFunction {

	@Override
	public double evaluate(StateEx oldState, StateEx newState) {
		int result = 0;
		int[][] field = newState.getField();

		for (int c = 0; c < State.COLS; c++) {
			boolean full = true;
			for (int r = 0; r < State.ROWS - 1; r++) {
				if (full == true && field[r][c] == 0) {
					result++;
					full = false;
				} else if (full == false && field[r][c] != 0) {
					result++;
					full = true;
				}
			}
		}

		return result;
	}

}
