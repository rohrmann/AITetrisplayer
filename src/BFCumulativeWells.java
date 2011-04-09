
public class BFCumulativeWells implements BaseFunction {

	@Override
	public double evaluate(StateEx oldState, StateEx newState) {
		int result = 0;
		int[][] field = newState.getField();

		for (int r = 0; r < State.ROWS - 1; r++) {
			for (int c = 0; c < State.COLS; c++) {
				if (isWell(field, r, c)) {
					result++;

					for (int r2 = r + 1; r2 < State.ROWS - 1; r2++) {
						if (isWell(field, r2, c)) {
							result++;
						} else {
							break;
						}
					}
				}
			}
		}

		return result;
	}

	private boolean isWell(int[][] field, int r, int c) {
		if (field[r][c] == 0) {
			if (c == 0) {
				return field[r][c + 1] != 0;
			} else if (c == State.COLS - 1) {
				return field[r][c - 1] != 0;
			} else {
				return field[r][c + 1] != 0 && field[r][c - 1] != 0;
			}
		}

		return false;
	}

}
