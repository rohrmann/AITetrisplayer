package tetris;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import myMath.Matrix;

//import baseFunctions.BFAAHD;
//import baseFunctions.BFAAHDDiff;
import baseFunctions.BFAHD;
import baseFunctions.BFAHDDiff;
//import baseFunctions.BFColumnHeight;
//import baseFunctions.BFColumnHeightDiff;
import baseFunctions.BFCompletedRows;
import baseFunctions.BFConstant;
import baseFunctions.BFMaxHeight;
import baseFunctions.BFMaxHeightDiff;
import baseFunctions.BFMeanHeight;
import baseFunctions.BFMeanHeightDiff;
import baseFunctions.BFNumHoles;
import baseFunctions.BFNumHolesDiff;
import baseFunctions.BaseFunctions;



public class PlayerSkeleton {
	public static final int MAX = 2147483647;
	public static final int MIN = -2147483648;
	
	private Matrix weights;
	private BaseFunctions baseFunctions;
	private final double discountFactor=0.99;
	private final int policySamples =0;
	private final int randomSamples =0;
	private final int randomPolicySamples=0;
	private final int randomPlayingSamples=0;
	private final int iterations = 10;
	
	private final double delta = 0.000001;
	
	public PlayerSkeleton(){
		baseFunctions = new BaseFunctions();
		initFunctionsWeights();
		learnWeights();
		
		weights.transpose().printMatrix();
	}
	
	protected List<Sample> getRandomSamples(int numberSamples){
		List<Sample> result = new ArrayList<Sample>();
		int n =0;
		
		while(n < numberSamples){
			State state = State.getRandomState();
			int[][] moves = state.legalMoves();
			
			int choice = (int)(Math.random()*moves.length);
			
			
			State newState = state.clone();
			if(newState.makeMove(moves[choice][0], moves[choice][1])){
				result.add(new Sample(state,newState,newState.getRowsCleared()-state.getRowsCleared()));
				n++;
			}	
		}
		return result;
	}
	
	protected List<Sample> getRandomSamplesByPlayingPolicy(int numSamples, Matrix weights, BaseFunctions baseFunctions){
		List<Sample> result = new ArrayList<Sample>();
		int n =0;
		
		while(n < numSamples){
			State state = State.getRandomState();
			int[][] moves = state.legalMoves();
			
			int choice = applyPolicy(state, moves, weights, baseFunctions);
			
			State newState = state.clone();
			if(newState.makeMove(moves[choice][0], moves[choice][1])){
				result.add(new Sample(state,newState,newState.getRowsCleared()-state.getRowsCleared()));
				n++;
			}	
		}
		return result;
	}
	
	protected List<Sample> getSamplesByPlayingRandomly(int numSamples,Matrix weights, BaseFunctions baseFunctions){
		List<Sample> result = new ArrayList<Sample>();
		
		State state = new State();
		int samples = 0;
		
		while(samples < numSamples){
			int choice = (int)(Math.random()*state.legalMoves().length);
			State nextState = state.clone();
			if(nextState.makeMove(state.legalMoves()[choice][0], state.legalMoves()[choice][1])){
				result.add(new Sample(state,nextState,nextState.getRowsCleared()-state.getRowsCleared()));
				samples++;
				state = nextState;
			}
			else{
				state = new State();
			}
		}
		return result;
	}
	
	protected List<Sample> getSamplesByPlayingPolicy(int numSamples, Matrix weights, BaseFunctions baseFunctions){
		List<Sample> result = new ArrayList<Sample>();
		
		State state = new State();
		int samples = 0;
		
		while(samples < numSamples){
			int choice = applyPolicy(state, state.legalMoves(), weights, baseFunctions);
			State nextState = state.clone();
			if(nextState.makeMove(state.legalMoves()[choice][0], state.legalMoves()[choice][1])){
				result.add(new Sample(state,nextState,nextState.getRowsCleared()-state.getRowsCleared()));
				samples++;
				state = nextState;
			}
			else{
				state = new State();
			}
		}
		return result;
	}
	
	protected List<Sample> getSamplesByPlayingGame(int games,Matrix weights, BaseFunctions baseFunctions){
		List<Sample> result = new ArrayList<Sample>();
		int rowsCleared = 0;
		
		for(int i =0; i<games;i++){
			State state = new State();
			State newState;
			while(!state.hasLost()){
				int choice = applyPolicy(state, state.legalMoves(),weights, baseFunctions);
				newState = state.clone();
				
				newState.makeMove(state.legalMoves()[choice][0], state.legalMoves()[choice][1]);
				
				result.add(new Sample(state,newState,newState.getRowsCleared()-state.getRowsCleared()));
				
				state = newState;
			}
			
			rowsCleared += state.getRowsCleared();
		}
		
		System.out.println("Average rows cleared:" + rowsCleared/games);
		return result;
	}
	
	protected void learnWeights(){
		List<Sample> samples = new ArrayList<Sample>();
		Matrix newWeights = null;
		for(int i =0; i<iterations;i++){
			samples=(getSamplesByPlayingGame(10,weights, baseFunctions));
			newWeights = LSPI(samples,weights,baseFunctions);
			
			if(newWeights != null){
				weights = newWeights;
			}
			else{
				System.out.println("LSPI didn't converge");
			}
		}
	}
	
	protected Matrix LSPI(List<Sample> samples, Matrix weights, BaseFunctions baseFunctions){
		Matrix newWeights = weights.clone();
		Matrix oldWeights;
		
		Set<Matrix> values = new HashSet<Matrix>();
		
		do{	
			if(values.contains(newWeights)){
				return null;
			}
			else{
				values.add(newWeights);
			}
			oldWeights = newWeights;
			newWeights = LSTDQOPT(samples,oldWeights,baseFunctions);
		}while(!oldWeights.equals(newWeights));
		
		return newWeights;
	}
	
	
	protected Matrix LSTDQ(List<Sample> samples, Matrix weights, BaseFunctions baseFunctions){
		Matrix A = Matrix.identity(baseFunctions.size()).mul(delta);
		Matrix b = new Matrix(baseFunctions.size(),1);
		
		for(Sample sample: samples){
			Matrix bf = baseFunctions.evaluate(sample.oldState, sample.newState);
	
			State predictedState = sample.newState.clone();
			
			int choice = applyPolicy(predictedState,predictedState.legalMoves(),weights,baseFunctions);
			boolean doable = predictedState.makeMove(predictedState.legalMoves()[choice][0], predictedState.legalMoves()[choice][1]);
			
			Matrix nbf = null;
			
			if(doable)
				nbf = baseFunctions.evaluate(sample.newState,predictedState);
			else
				nbf = new Matrix(baseFunctions.size(),1);
			
			
			Matrix temp = bf.sub(nbf.mul(discountFactor)).transpose();
			
			
			Matrix sumOperand = bf.mul(temp);
			A = A.add(sumOperand);			
			b = b.add(bf.mul(sample.reward));
		}
		
		return A.invert().mul(b);
	}
	
	protected Matrix LSTDQOPT(List<Sample> samples, Matrix weights, BaseFunctions baseFunctions){
		Matrix B = Matrix.identity(baseFunctions.size()).mul(1/delta);
		Matrix b = new Matrix(baseFunctions.size(),1);
		
		for(Sample sample: samples){
			Matrix bf = baseFunctions.evaluate(sample.oldState, sample.newState);
	
			State predictedState = sample.newState.clone();
			
			int choice = applyPolicy(predictedState,predictedState.legalMoves(),weights,baseFunctions);
			boolean doable = predictedState.makeMove(predictedState.legalMoves()[choice][0], predictedState.legalMoves()[choice][1]);
			
			Matrix nbf = null;
			
			if(doable)
				nbf = baseFunctions.evaluate(sample.newState,predictedState);
			else
				nbf = new Matrix(baseFunctions.size(),1);
			
			Matrix temp = bf.sub(nbf.mul(discountFactor));
			
			Matrix factor1 = B.mul(bf);
			Matrix factor2 = temp.transpose().mul(B);
			
			double denominator = 1 + temp.dot(factor1);
			
			B = B.sub(factor1.mul(factor2).mul(1/denominator));
			
			b = b.add(bf.mul(sample.reward));
		}
		
		return B.mul(b);
	}
	
	protected void initFunctionsWeights(){
		List<Double> weights = new ArrayList<Double>();
		
//		//column height
//		for(int c = 0; c < State.COLS; c++){
//			baseFunctions.add(new BFColumnHeight(c));
//			weights.add(-1.0);
//		}
//		
//		//column height diff
//		for(int c= 0; c < State.COLS; c++){
//			baseFunctions.add(new BFColumnHeightDiff(c));
//			weights.add(-1.0);
//		}
//		
//		//absolute adjacent column height diff
//		for(int c = 0; c < State.COLS-1; c++){
//			baseFunctions.add(new BFAAHD(c));
//			weights.add(-0.5);
//		}
//		
////		//absolute adjacent column height diff diff
//		for(int c = 0; c <State.COLS-1; c++){
//			baseFunctions.add(new BFAAHDDiff(c));
//			weights.add(.0);
//		}
		
		//sum of absolute adjacent column height diffs
		baseFunctions.add(new BFAHD());
		weights.add(0.0);
		
		//difference of sum of absolute adjacent column height diffs
		baseFunctions.add(new BFAHDDiff());
		weights.add(-1.0);
		
		baseFunctions.add(new BFCompletedRows());
		weights.add(2.0);
		
		baseFunctions.add(new BFConstant(1.0));
		weights.add(0.0);
		
		baseFunctions.add(new BFMaxHeight());
		weights.add(0.0);
		
		baseFunctions.add(new BFMaxHeightDiff());
		weights.add(-1.0);
	
		baseFunctions.add(new BFMeanHeight());
		weights.add(0.0);
		
		baseFunctions.add(new BFMeanHeightDiff());
		weights.add(0.0);
		
		baseFunctions.add(new BFNumHoles());
		weights.add(0.0);
		
		baseFunctions.add(new BFNumHolesDiff());
		weights.add(-4.0);
		
		double[][] weightValues = new double[weights.size()][1];
		
		for(int i = 0; i < weights.size(); i++){
			weightValues[i][0] = weights.get(i);
		}
		
		this.weights = new Matrix(weightValues);
	}
	
	public int applyPolicy(State s, int[][] legalMoves, Matrix weights, BaseFunctions baseFunctions){
		State newState = s.clone();
		newState.makeMove(legalMoves[0][0],legalMoves[0][1]);
		
		int bestMove = 0;
		double value = calcHeuristic(s,newState,weights,baseFunctions);
		
		for(int i = 1; i < legalMoves.length; i++){
			newState = s.clone();
			newState.makeMove(legalMoves[i][0],legalMoves[i][1]);
			double temp = calcHeuristic(s,newState, weights,baseFunctions);
			
			if(temp > value){
				value = temp;
				bestMove = i;
			}
		}
		
		return bestMove;
	}

	public int pickMove(State s, int[][] legalMoves) {
		return applyPolicy(s,legalMoves,weights,baseFunctions);
		
	}
	
	public double calcHeuristic(State oldState, State newState, Matrix weights,BaseFunctions baseFunctions){
		Matrix fValues = baseFunctions.evaluate(oldState, newState);
		
		return weights.dot(fValues);
	}
	
	public static void main(String[] args) {
		int n = 10;
		int rows = 0;
		State s = new State();
		PlayerSkeleton p = new PlayerSkeleton();
		for(int i =0; i< n ;i++){
			s.clear();
			while (!s.hasLost()) {
				s.makeMove(p.pickMove(s, s.legalMoves()));
			}
			
			rows += s.getRowsCleared();
		}
		
		System.out.println("Average rows:" + (double)rows/n);
	
	}

}