public class BFNumHolesDiff implements BaseFunction {

	@Override
	public double evaluate(StateEx oldState, StateEx newState) {
		return newState.getNumHoles() - oldState.getNumHoles();
	}

}
