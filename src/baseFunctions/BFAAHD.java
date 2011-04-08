package baseFunctions;
import tetris.State;


public class BFAAHD implements BaseFunction {

	private int column;
	
	public BFAAHD(int column){
		this.column = column;
	}
	
	@Override
	public double evaluate(State oldState, State newState) {
		return Math.abs(newState.getTop(column) - newState.getTop(column+1));
	}

}
