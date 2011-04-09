public class BFColumnHeightDiff implements BaseFunction {
	private int column;

	public BFColumnHeightDiff(int column) {
		this.column = column;
	}

	@Override
	public double evaluate(StateEx oldState, StateEx newState) {
		return newState.getTop(column) - oldState.getTop(column);
	}

}
