/**
 * This class encapsulates the necessary information for one sample.
 * @author rohrmann
 *
 */
public class Sample {
	public StateEx oldState;
	public StateEx newState;
	public double reward;

	public Sample(StateEx oldState, StateEx newState, double reward) {
		this.oldState = oldState;
		this.newState = newState;
		this.reward = reward;
	}
	
	@Override
	public int hashCode(){
		Double re = reward;
		return oldState.hashCode()^newState.hashCode()^re.hashCode();
	}

}
