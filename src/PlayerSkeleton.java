import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * This class implements our Tetris AI
 * @author rohrmann
 *
 */
public class PlayerSkeleton {
	public static final int MAX = 2147483647;
	public static final int MIN = -2147483648;

	private Matrix weights;
	private BaseFunctions baseFunctions;
	
//	used for the LSPI algorithm
	private final double discountFactor = 0.99999;
	private final int iterations = 10;
	private final double delta = 0.000001;
	private final int gamesPerIteration = 10;
	private final int intialSamples = 1000;
	private final int maxSamples = 5000;

	public PlayerSkeleton() {
		baseFunctions = new BaseFunctions();
		//initDellacherie();
		initThiery();
		learnWeights();
	}
	

	/**
	 * Generation of random samples by creating a random state and playing randomly
	 * @param numberSamples
	 * @return
	 */
	protected Set<Sample> getRandomSamples(int numberSamples) {
		Set<Sample> result = new HashSet<Sample>();
		int n = 0;

		while (n < numberSamples) {
			StateEx state = StateEx.getRandomStateEx();
			int[][] moves = state.legalMoves();

			int choice = (int) (Math.random() * moves.length);

			StateEx newState = state.clone();
			if (newState.makeMove(choice)) {
				result.add(new Sample(state, newState, newState
						.getRowsCleared()
						- state.getRowsCleared()));
				n++;
			}
		}
		return result;
	}

	/**
	 * Sampling by generating random states and playing according to the current policy.
	 * @param numSamples
	 * @param weights
	 * @param baseFunctions
	 * @return
	 */
	protected Set<Sample> getRandomSamplesByPlayingPolicy(int numSamples,
			Matrix weights, BaseFunctions baseFunctions) {
		Set<Sample> result = new HashSet<Sample>();
		int n = 0;

		while (n < numSamples) {
			StateEx state = StateEx.getRandomStateEx();
			int[][] moves = state.legalMoves();

			int choice = applyPolicy(state, moves, weights, baseFunctions);

			StateEx newState = state.clone();
			if (newState.makeMove(moves[choice][0], moves[choice][1]) && result.add(new Sample(state, newState, newState
					.getRowsCleared()
					- state.getRowsCleared()))) {
				n++;
			}
		}
		return result;
	}

	/**
	 * Sampling by playing several Tetris game with a random choice of actions.
	 * @param numSamples
	 * @param weights
	 * @param baseFunctions
	 * @return
	 */
	protected Set<Sample> getSamplesByPlayingRandomly(int numSamples,
			Matrix weights, BaseFunctions baseFunctions) {
		Set<Sample> result = new HashSet<Sample>();

		StateEx state = new StateEx();
		int samples = 0;

		while (samples < numSamples) {
			int choice = (int) (Math.random() * state.legalMoves().length);
			StateEx nextState = state.clone();
			if (nextState.makeMove(state.legalMoves()[choice][0], state
					.legalMoves()[choice][1])) {
				result.add(new Sample(state, nextState, nextState
						.getRowsCleared()
						- state.getRowsCleared()));
				samples++;
				state = nextState;
			} else {
				state = new StateEx();
			}
		}
		return result;
	}

	/**
	 * Sampling by playing several tetris games according to the current policy.
	 * @param numSamples
	 * @param weights
	 * @param baseFunctions
	 * @return
	 */
	protected Set<Sample> getSamplesByPlayingPolicy(int numSamples,
			Matrix weights, BaseFunctions baseFunctions) {
		Set<Sample> result = new HashSet<Sample>();

		StateEx state = new StateEx();
		int samples = 0;

		while (samples < numSamples) {
			int choice = applyPolicy(state, state.legalMoves(), weights,
					baseFunctions);
			StateEx nextState = state.clone();
			if (nextState.makeMove(state.legalMoves()[choice][0], state
					.legalMoves()[choice][1])) {
				result.add(new Sample(state, nextState, nextState
						.getRowsCleared()
						- state.getRowsCleared()));
				samples++;
				state = nextState;
			} else {
				state = new StateEx();
			}
		}
		return result;
	}

	/**
	 * Sampling by playing one Tetris game according to the current policy.
	 * @param games
	 * @param weights
	 * @param baseFunctions
	 * @return
	 */
	protected Pair<Set<Sample>, Double> getSamplesByPlayingGame(int games,
			Matrix weights, BaseFunctions baseFunctions) {
		Set<Sample> result = new HashSet<Sample>();
		int rowsCleared = 0;

		for (int i = 0; i < games; i++) {
			StateEx state = new StateEx();
			StateEx newState;
			while (!state.hasLost()) {
				int choice = applyPolicy(state, state.legalMoves(), weights,
						baseFunctions);
				newState = state.clone();

				newState.makeMove(state.legalMoves()[choice][0], state
						.legalMoves()[choice][1]);

				result.add(new Sample(state, newState, newState
						.getRowsCleared()
						- state.getRowsCleared()));

				state = newState;
			}

			rowsCleared += state.getRowsCleared();
		}

		System.out.println("Average rows cleared:" + (double) rowsCleared
				/ games);
		return new Pair<Set<Sample>, Double>(result, (double) rowsCleared
				/ games);
	}
	
	protected List<Integer> getRandomNumbers(int start, int end,int numbers){
		Set<Integer> set = new HashSet<Integer>();
		int number = 0;
		while(number < numbers){
			if(set.add((int)(Math.random()*(end-start)+start))){
				number++;
			}
		}
		
		List<Integer> result = new ArrayList<Integer>(set);
		Collections.sort(result);
		
		return result;
	}

	/**
	 * Function which uses the LSPI algorithm to learn an approximation of the state action value function. It
	 * generates samples by playing Tetris with the current policy. These samples are then used to calculate the
	 * new policy before a new iteration is started. During the iteration the function saves the best weight vector
	 * which is set as the final weight vector at the end.
	 */
	protected void learnWeights() {
		Set<Sample> samples = new HashSet<Sample>();
		Matrix newWeights = null;
		Matrix maxWeights = null;
		double max = 0;
		
		samples.addAll(getSamplesByPlayingPolicy(intialSamples, weights, baseFunctions));
		for (int i = 0; i < iterations; i++) {
			Pair<Set<Sample>, Double> pair = getSamplesByPlayingGame(gamesPerIteration,
					weights, baseFunctions);
			samples.addAll(pair.a());
			
			if(samples.size() > maxSamples){
				List<Integer> randomNumbers = getRandomNumbers(0, samples.size(), maxSamples);
				Set<Sample> newSamples = new HashSet<Sample>();
				int index = 0;
				int indexToPick = randomNumbers.get(0);
				int indexRandomNumbers = 0;
				for(Sample sample: samples){
					if(index == indexToPick){
						newSamples.add(sample);
						indexRandomNumbers++;
						if(indexRandomNumbers >= randomNumbers.size())
							break;
						else
							indexToPick = randomNumbers.get(indexRandomNumbers);
					}
					index++;
				}
				
				samples = newSamples;
			}

			if (max < pair.b()) {
				max = pair.b();
				maxWeights = weights;
			}

			newWeights = LSPI(samples, weights, baseFunctions);
			
			//If the samples are not well distributed, then the LSPI algorithm doesn't converge but instead
			//moves in a space around the optimal solution. If the LSPI algorithm notes that, it will return
			//null.
			if (newWeights != null) {
				weights = newWeights;
			} else {
				System.out.println("LSPI didn't converge");
				weights = maxWeights;
				samples.clear();
			}
		}

		weights = maxWeights;
		
		weights.transpose().printMatrix();
	}

	/**
	 * Least Square Policy Iteration with recognition of weight vector cycles. If the
	 * samples are not well distributed, it can happen that the algorithm isn't converging against
	 * a fix point but instead is oscillating in a space around the optimal solution. This has to be
	 * recognized.
	 * @param samples
	 * @param weights
	 * @param baseFunctions
	 * @return
	 */
	protected Matrix LSPI(Set<Sample> samples, Matrix weights,
			BaseFunctions baseFunctions) {
		Matrix newWeights = weights.clone();
		Matrix oldWeights;

		Set<Matrix> values = new HashSet<Matrix>();
		//fix point iteration
		do {
			if (values.contains(newWeights)) {
				return null;
			} else {
				values.add(newWeights);
			}
			oldWeights = newWeights;
			newWeights = LSTDQOPT(samples, oldWeights, baseFunctions);
		} while (!oldWeights.equals(newWeights));

		return newWeights;
	}

	/**
	 * Least squares temporal differences learning for the state action value function Q.
	 * @param samples
	 * @param weights
	 * @param baseFunctions
	 * @return
	 */
	protected Matrix LSTDQ(Set<Sample> samples, Matrix weights,
			BaseFunctions baseFunctions) {
		Matrix A = Matrix.identity(baseFunctions.size()).mul(delta);
		Matrix b = new Matrix(baseFunctions.size(), 1);

		for (Sample sample : samples) {
			Matrix bf = baseFunctions
					.evaluate(sample.oldState, sample.newState);

			StateEx predictedState = sample.newState.clone();

			int choice = applyPolicy(predictedState, predictedState
					.legalMoves(), weights, baseFunctions);
			//decide whether we can execute the move
			boolean doable = predictedState.makeMove(choice);

			Matrix nbf = null;

			if (doable)
				nbf = baseFunctions.evaluate(sample.newState, predictedState);
			else
				nbf = new Matrix(baseFunctions.size(), 1);

			Matrix temp = bf.sub(nbf.mul(discountFactor)).transpose();

			Matrix sumOperand = bf.mul(temp);
			A = A.add(sumOperand);
			b = b.add(bf.mul(sample.reward));
		}

		return A.invert().mul(b);
	}

	/**
	 * Optimized version of the least squares temporal difference learning of the state action value function.
	 * By applying the Sherman-Morrison formula we can get rid of the inversion of the Matrix A.
	 * @param samples
	 * @param weights
	 * @param baseFunctions
	 * @return
	 */
	protected Matrix LSTDQOPT(Set<Sample> samples, Matrix weights,
			BaseFunctions baseFunctions) {
		Matrix B = Matrix.identity(baseFunctions.size()).mul(1 / delta);
		Matrix b = new Matrix(baseFunctions.size(), 1);

		for (Sample sample : samples) {
			Matrix bf = baseFunctions
					.evaluate(sample.oldState, sample.newState);

			StateEx predictedState = sample.newState.clone();

			int choice = applyPolicy(predictedState, predictedState
					.legalMoves(), weights, baseFunctions);
			boolean doable = predictedState.makeMove(predictedState
					.legalMoves()[choice][0],
					predictedState.legalMoves()[choice][1]);

			Matrix nbf = null;

			if (doable)
				nbf = baseFunctions.evaluate(sample.newState, predictedState);
			else
				nbf = new Matrix(baseFunctions.size(), 1);

			Matrix temp = bf.sub(nbf.mul(discountFactor));

			Matrix factor1 = B.mul(bf);
			Matrix factor2 = temp.transpose().mul(B);

			double denominator = 1 + temp.dot(factor1);

			B = B.sub(factor1.mul(factor2).mul(1 / denominator));

			b = b.add(bf.mul(sample.reward));
		}

		return B.mul(b);
	}

	/**
	 * This function initialize the base function set and the weight vector with the base functions and the weights
	 * which were used by Dellacherie. The weights weren't derived by machine learning means but by try and error.
	 */
	protected void initDellacherie(){
		List<Double> weights = new ArrayList<Double>();
		
		baseFunctions.add(new BFLandingHeight());
		weights.add(-1.0);

		baseFunctions.add(new BFErodedCells());
		weights.add(1.0);

		baseFunctions.add(new BFRowTransitions());
		weights.add(-1.0);

		baseFunctions.add(new BFColumnTransitions());
		weights.add(-1.0);

		baseFunctions.add(new BFNumHoles());
		weights.add(-4.0);

		baseFunctions.add(new BFCumulativeWells());
		weights.add(-1.0);

		double[][] weightValues = new double[weights.size()][1];

		for (int i = 0; i < weights.size(); i++) {
			weightValues[i][0] = weights.get(i);
		}

		this.weights = new Matrix(weightValues);
	}
	
	/**
	 * This functions sets the base functions and weights used by Thiery. It's supposed to be better than Dellacharie
	 * but the simulations couldn't prove that.
	 */
	protected void initThiery(){
		List<Double> weights = new ArrayList<Double>();
		
		 baseFunctions.add(new BFLandingHeight());
		 weights.add(-12.63);
				
		 baseFunctions.add(new BFErodedCells());
		 weights.add(6.6);
				
		 baseFunctions.add(new BFRowTransitions());
		 weights.add(-9.22);
				
		 baseFunctions.add(new BFColumnTransitions());
		 weights.add(-19.77);
				
		 baseFunctions.add(new BFNumHoles());
		 weights.add(-13.08);
				
		 baseFunctions.add(new BFCumulativeWells());
		 weights.add(-10.49);
				
		 baseFunctions.add(new BFHoleDepth());
		 weights.add(-1.61);
				
		 baseFunctions.add(new BFRowsWithHoles());
		 weights.add(-24.04);

		double[][] weightValues = new double[weights.size()][1];

		for (int i = 0; i < weights.size(); i++) {
			weightValues[i][0] = weights.get(i);
		}

		this.weights = new Matrix(weightValues);
	}
	
	/**
	 * This functions sets the base functions used by Lagoudakis. The weights are learned by the LSPI algorithm.
	 * But it performs far worse than the evaluation function of Dellacharie.
	 */
	void initLagoudakis(){
		List<Double> weights = new ArrayList<Double>();
		 //2700 rows cleared: 	-8.78951411243151;0.08151298534273177;431.6138387336361;1097.2245005375664;4.564176063822103;-0.9564016301739926;-9.207422350726727;432.3913694007997;-42.26627603559048;-46.52407063944807
		//						-6.56954858742778;0.08181327300598731;370.2593875121343;722.7739339087932;1.7262246126294072;-0.7238505736718397;-6.311884882167713;370.4098800647336;-27.542721346049227;-39.82952820318409
		 //sum of absolute adjacent column height diffs
		 baseFunctions.add(new BFAHD());
		 weights.add(-8.78951411243151);
				
		 //difference of sum of absolute adjacent column height diffs
		 baseFunctions.add(new BFAHDDiff());
		 weights.add(0.08151298534273177);
				
		 baseFunctions.add(new BFCompletedRows());
		 weights.add(431.6138387336361);
				
		 baseFunctions.add(new BFConstant(1.0));
		 weights.add(1097.2245005375664);
				
		 baseFunctions.add(new BFMaxHeight());
		 weights.add(4.564176063822103);
				
		 baseFunctions.add(new BFMaxHeightDiff());
		 weights.add(-0.9564016301739926);
			
		 baseFunctions.add(new BFMeanHeight());
		 weights.add(-9.207422350726727);
				
		 baseFunctions.add(new BFMeanHeightDiff());
		 weights.add(432.3913694007997);
				
		 baseFunctions.add(new BFNumHoles());
		 weights.add(-42.26627603559048);
				
		 baseFunctions.add(new BFNumHolesDiff());
		 weights.add(-46.52407063944807);
		 
		 double[][] finalWeights = new double[weights.size()][1];
		 
		 for(int i =0; i<weights.size();i++){
			 finalWeights[i][0] = weights.get(i);
		 }
		 
		 this.weights = new Matrix(finalWeights);
	}

	/**
	 * This function calculates the best action given a certain state, weights and a set of base functions.
	 * @param s
	 * @param legalMoves
	 * @param weights
	 * @param baseFunctions
	 * @return
	 */
	public int applyPolicy(StateEx s, int[][] legalMoves, Matrix weights,
			BaseFunctions baseFunctions) {
		StateEx newState;

		int bestMove = 0;
		double value = Double.NEGATIVE_INFINITY;

		for (int i = 0; i < legalMoves.length; i++) {
			newState = s.clone();
			if (newState.makeMove(i)) {
				double temp = calcHeuristic(s, newState, weights, baseFunctions);

				if (temp > value) {
					value = temp;
					bestMove = i;
				}
			}
		}

		return bestMove;
	}

	/**
	 * Wrapper function for applyPolicy
	 * @param s
	 * @param legalMoves
	 * @return
	 */
	public int pickMove(State s, int[][] legalMoves) {
		return applyPolicy(new StateEx(s), legalMoves, weights, baseFunctions);

	}

	public double calcHeuristic(StateEx oldState, StateEx newState, Matrix weights,
			BaseFunctions baseFunctions) {
		Matrix fValues = baseFunctions.evaluate(oldState, newState);

		return weights.dot(fValues);
	}

	public static void main(String[] args) {
		PlayerSkeleton p = new PlayerSkeleton();
		State s = new State();
		while (!s.hasLost()) {
			s.makeMove(p.pickMove(s, s.legalMoves()));
		}

		System.out.println(s.getRowsCleared());

	}

}