package baseFunctions;
import tetris.State;


public class BFColumnHeight implements BaseFunction {
	private int index;
	
	public BFColumnHeight(int index){
		this.index = index;
	}

	@Override
	public double evaluate(State oldState, State newState) {
		return newState.getTop(index);
	}

}
