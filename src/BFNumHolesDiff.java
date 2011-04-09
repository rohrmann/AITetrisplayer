/**
 * Base function which return the difference of the number of holes of the new state and the number of holes
 * of the old state.
 * @author rohrmann
 *
 */
public class BFNumHolesDiff implements BaseFunction {

	@Override
	public double evaluate(StateEx oldState, StateEx newState) {
		return newState.getNumHoles() - oldState.getNumHoles();
	}

}
