public class BFRowTransitions implements BaseFunction {

	@Override
	public double evaluate(StateEx oldState, StateEx newState) {
		int[][] field = newState.getField();

		int result = 0;

		for (int i = 0; i < State.ROWS - 1; i++) {
			boolean full = true;
			for (int j = 0; j < State.COLS; j++) {
				if (full == true && field[i][j] == 0) {
					result++;
					full = false;
				} else if (full == false && field[i][j] != 0) {
					result++;
					full = true;
				}
			}

			if (full == false) {
				result++;
			}
		}

		return result;
	}

}
