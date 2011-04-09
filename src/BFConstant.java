/**
 * base function which returns a constant value
 * @author rohrmann
 *
 */
public class BFConstant implements BaseFunction {
	private double value;

	public BFConstant(double value) {
		this.value = value;
	}

	@Override
	public double evaluate(StateEx oldState, StateEx newState) {
		return value;
	}

}