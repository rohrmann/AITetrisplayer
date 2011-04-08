package baseFunctions;
import tetris.State;


public class BFColumnHeightDiff implements BaseFunction {
	private int column;
	
	public BFColumnHeightDiff(int column){
		this.column = column;
	}

	@Override
	public double evaluate(State oldState, State newState) {
		return newState.getTop(column) - oldState.getTop(column);
	}

}
