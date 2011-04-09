
public class Sample {
	public StateEx oldState;
	public StateEx newState;
	public double reward;

	public Sample(StateEx oldState, StateEx newState, double reward) {
		this.oldState = oldState;
		this.newState = newState;
		this.reward = reward;
	}

}
