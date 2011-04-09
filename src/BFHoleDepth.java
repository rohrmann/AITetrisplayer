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
