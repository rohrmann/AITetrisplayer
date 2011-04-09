/**
 * base function which returns the sum of all hole depths on the board. The depth of a hole is defined as
 * the number of full cells which are directly above the hole.
 * @author rohrmann
 *
 */
public class BFHoleDepth implements BaseFunction {

	@Override
	public double evaluate(StateEx oldState, StateEx newState) {
		int[][] field = newState.getField();
		int result = 0;

		for (int c = 0; c < State.COLS; c++) {
			boolean hole = false;
			for (int r = 0; r < newState.getTop(c); r++) {
				if (hole == true && field[r][c] != 0) {
					result++;
				} else if (field[r][c] == 0) {
					hole = true;
				}

			}
		}

		return result;
	}

}
