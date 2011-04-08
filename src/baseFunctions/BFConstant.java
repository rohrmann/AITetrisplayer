package baseFunctions;
import tetris.State;


public class BFConstant implements BaseFunction {
	private double value;
	
	public BFConstant(double value){
		this.value = value;
	}

	@Override
	public double evaluate(State oldState, State newState) {
		return value;
	}

}
