public class BFRowsWithHoles implements BaseFunction {

	@Override
	public double evaluate(StateEx oldState, StateEx newState) {
		int result = 0;

		int[][] field = newState.getField();

		for (int r = 0; r < newState.getMaxHeight(); r++) {
			for (int c = 0; c < State.COLS; c++) {
				if (field[r][c] == 0) {
					result++;
					break;
				}
			}
		}

		return result;
	}

}
