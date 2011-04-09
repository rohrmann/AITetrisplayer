public class BFLandingHeight implements BaseFunction {

	@Override
	public double evaluate(StateEx oldState, StateEx newState) {
		return newState.getHeightPiece();
	}

}
