
public class BFAAHD implements BaseFunction {

	private int column;

	public BFAAHD(int column) {
		this.column = column;
	}

	@Override
	public double evaluate(StateEx oldState, StateEx newState) {
		return Math.abs(newState.getTop(column) - newState.getTop(column + 1));
	}

}
