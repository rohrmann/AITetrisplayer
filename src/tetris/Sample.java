package tetris;

public class Sample {
	public State oldState;
	public State newState;
	public double reward;
	
	public Sample(State oldState, State newState, double reward){
		this.oldState = oldState;
		this.newState = newState;
		this.reward = reward;
	}

}
