/**
 * base function which returns the landing height of the last moved piece. The landing height is the middle point of
 * the piece.
 * @author rohrmann
 *
 */
public class BFLandingHeight implements BaseFunction {

	@Override
	public double evaluate(StateEx oldState, StateEx newState) {
		return newState.getHeightPiece();
	}

}
