package baseFunctions;
import tetris.State;


public interface BaseFunction {
	double evaluate(State oldState, State newState);

}
