package baseFunctions;

import tetris.State;

public class BFAAHDDiff implements BaseFunction {
	private int column;
	
	public BFAAHDDiff(int column){
		this.column = column;
	}

	@Override
	public double evaluate(State oldState, State newState) {
		return Math.abs(newState.getTop(column) - newState.getTop(column+1)) - Math.abs(oldState.getTop(column) - newState.getTop(column)-1);
	}

}
