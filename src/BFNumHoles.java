/**
 * Base function which returns the number of holes in the new state. A hole is defined as an empty cell
 * which is covered by a full cell. The full cell doesn't have to be directly above the empty cell.
 * @author rohrmann
 *
 */
public class BFNumHoles implements BaseFunction {

	@Override
	public double evaluate(StateEx oldState, StateEx newState) {
		return newState.getNumHoles();
	}

}
