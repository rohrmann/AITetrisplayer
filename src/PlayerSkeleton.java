


public class PlayerSkeleton {
	public static final int MAX = 2147483647;
	public static final int MIN = -2147483648;
		
	public int pickMove(State s, int[][] legalMoves) {
		double maxHeuristic = -90000;
		int maxHeuristicIndex = 0;

		//For each legal move..		
		for(int i = 0; i < legalMoves.length; i++){
			//Get the heuristic value of the move
			Heuristic h = new Heuristic(s, legalMoves[i]);
			double totalHeuristic = h.getTotalHeuristicValue();
			
			//Record our best move
			if(Double.compare(totalHeuristic,maxHeuristic) > 0){
				maxHeuristic = totalHeuristic;
				maxHeuristicIndex = i;
			}
		}
		
		/* Uncomment below to step through each
		 * step in the game.. useful for debugging */
		/*try {System.in.read();} catch (IOException e) {}*/
		
		
		/*Just in case something went wrong, 
		 * we don't want an IndexOutOfBoundsException*/
		return (maxHeuristicIndex > (-1) ) ? maxHeuristicIndex : 0;
	}

	public static void main(String[] args) {
		State s = new State();
		new TFrame(s);
		PlayerSkeleton p = new PlayerSkeleton();
		while(!s.hasLost()) {
			s.makeMove( p.pickMove( s,s.legalMoves() ) );
			s.draw();
			s.drawNext(0,0);
			try {
				Thread.sleep(10);
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
		}
		System.out.println("You have completed "+s.getRowsCleared()+" rows.");
	}
	
	
	/* This class is where all the action is.
	 * We need to experiment with different weights 
	 * & uses of different heuristics..
	 */
	private class Heuristic{
		//Current State
		private State s;
		
		//What the state will look like if we complete the next legal move
		private StateCopy sc;
		
		//Weighting for different heuristics
		private static final int ROWS_COMPLETED_WEIGHT     = 388;
		private static final int MAX_HEIGHT_WEIGHT         = -3;
		private static final int DIF_MAX_HEIGHT_WEIGHT     = 4;
		private static final int HOLES_WEIGHT              = -68;
		private static final int DIF_HOLES_WEIGHT          = 110;
		private static final int AVERAGE_HEIGHT_WEIGHT     = -10;
		private static final int DIF_AVERAGE_HEIGHT_WEIGHT = 379;
		private static final int ABS_HEIGHT_WEIGHT         = -22;
		private static final int DIF_ABS_HEIGHT_WEIGHT     = 20;
		
		private boolean hasLost = false;
		
		public Heuristic(State s, int[] m){
			this.s = s;
						
			this.sc = new StateCopy(s);
			this.sc.makeMove(m);
		}
		
		public double getTotalHeuristicValue(){
			
			if(hasLost)
				return -50000;// We really, reeeeally don't want to choose this move..
			
			double rowsCompleted = getRowsCompleted(sc);
			double currentMaxHeight= getMaxHeight(s);
			double differenceInMaxHeight = currentMaxHeight - getMaxHeight(sc);
			double currentHolesInBoard = getHoles(s);
			double differenceInHolesInBoard = currentHolesInBoard - getHoles(sc);
			double currentAbsHeightDifference = getAbsHeightDifference(s);
			double differenceInAbsHeightDifference = currentAbsHeightDifference - getAbsHeightDifference(sc);
			double currentAverageHeight = getAverageHeight(s);
			double differenceInAverageHeight = currentAverageHeight - getAverageHeight(sc);

			double totalHeuristic = (rowsCompleted * ROWS_COMPLETED_WEIGHT) +
									(currentMaxHeight * MAX_HEIGHT_WEIGHT) +
									(differenceInMaxHeight * DIF_MAX_HEIGHT_WEIGHT) +
									(currentHolesInBoard * HOLES_WEIGHT) +
									(differenceInHolesInBoard * DIF_HOLES_WEIGHT) +
									(currentAverageHeight * AVERAGE_HEIGHT_WEIGHT) + 
									(differenceInAverageHeight * DIF_AVERAGE_HEIGHT_WEIGHT) +
									(currentAbsHeightDifference * ABS_HEIGHT_WEIGHT) +
									(differenceInAbsHeightDifference * DIF_ABS_HEIGHT_WEIGHT)
									;
			
			return totalHeuristic;
		}
			
		public int getHoles(Object s){
			int field[][], top[], cols = State.COLS;
			try{
				field = State.class.cast(s).getField();
				top = State.class.cast(s).getTop();
			}catch(ClassCastException e){
				try{
					field = StateCopy.class.cast(s).getField();
					top = StateCopy.class.cast(s).getTop();
				}catch(ClassCastException e2){
					System.err.println("Error casting object");
					return -1000;
				}
			}
			
			int holes = 0;
			for(int i = 0; i < cols; i++)
				for(int j = 0; j < top[i]; j++){
					try{
						if(field[j][i] == 0 && field[j+1][i] != 0)
							holes++;
					}catch(IndexOutOfBoundsException e){
						//Thats ok, means we are at the top of the board
					}
				}
				
			return holes;
		}
	
		public double getAbsHeightDifference(Object s){
			int top[];
			try{
				top = State.class.cast(s).getTop();
			}catch(ClassCastException e){
				try{
					top = StateCopy.class.cast(s).getTop();
				}catch(ClassCastException e2){
					System.err.println("Error casting object");
					return -1000;
				}
			}

			int sum = 0;
			
			try{
				for(int i = 0; i < top.length; i++){
					sum += Math.abs(top[i] - top[i+1]);
				}
			}catch(IndexOutOfBoundsException e){
				//Thats ok, means we have looked at all rows..
			}
				
			return sum;
		}
		
		public double getMaxHeight(Object s){
			int top[];
			try{
				top = State.class.cast(s).getTop();
			}catch(ClassCastException e){
				try{
					top = StateCopy.class.cast(s).getTop();
				}catch(ClassCastException e2){
					System.err.println("Error casting object");
					return -1000;
				}
			}

			int max = -1;
			for(int i : top){
				if(i>max)
					max=i;
			}
			return max;
		}
				
		public double getAverageHeight(Object s){
			int top[];
			try{
				top = State.class.cast(s).getTop();
			}catch(ClassCastException e){
				try{
					top = StateCopy.class.cast(s).getTop();
				}catch(ClassCastException e2){
					System.err.println("Error casting object");
					return -1000;
				}
			}

			double sum = 0;
			for(int i : top)
				sum+=i;

			return sum/(double)top.length;
		}
			
		public double getRowsCompleted(Object s){
			int rowsCleared;
			try{
				rowsCleared = State.class.cast(s).getRowsCleared();
			}catch(ClassCastException e){
				try{
					rowsCleared = StateCopy.class.cast(s).getRowsCleared();
				}catch(ClassCastException e2){
					System.err.println("Error casting object");
					return -1000;
				}
			}
			return rowsCleared;
		}
		
		/* Simple class which just copies a given state
		 * and simulates the 'State' class so that we 
		 * can freely and safely test future moves..
		 */
		private class StateCopy {
			public final int COLS = 10;
			public final int ROWS = 21;
			public final int N_PIECES = 7;
			public final int ORIENT = 0;
			public final int SLOT = 1;
			
			private int turn = 0;
			private int cleared = 0;
			protected int nextPiece;
			
			private int[][] field = new int[ROWS][COLS];
			private int[] top = new int[COLS];
			
			protected int[][][] legalMoves = new int[N_PIECES][][];
			protected int[] pOrients = {1,2,4,4,4,2,2};
			protected int[][] pWidth = {
					{2},
					{1,4},
					{2,3,2,3},
					{2,3,2,3},
					{2,3,2,3},
					{3,2},
					{3,2}
			};
	
			private int[][] pHeight = {
					{2},
					{4,1},
					{3,2,3,2},
					{3,2,3,2},
					{3,2,3,2},
					{2,3},
					{2,3}
			};
			private int[][][] pBottom = {
				{{0,0}},
				{{0},{0,0,0,0}},
				{{0,0},{0,1,1},{2,0},{0,0,0}},
				{{0,0},{0,0,0},{0,2},{1,1,0}},
				{{0,1},{1,0,1},{1,0},{0,0,0}},
				{{0,0,1},{1,0}},
				{{1,0,0},{0,1}}
			};
			private int[][][] pTop = {
				{{2,2}},
				{{4},{1,1,1,1}},
				{{3,1},{2,2,2},{3,3},{1,1,2}},
				{{1,3},{2,1,1},{3,3},{2,2,2}},
				{{3,2},{2,2,2},{2,3},{1,2,1}},
				{{1,2,2},{3,2}},
				{{2,2,1},{2,3}}
			};
			
			public StateCopy(State s){
				for(int i = 0; i < N_PIECES; i++) {
					int n = 0;
					for(int j = 0; j < pOrients[i]; j++) {
						n += COLS+1-pWidth[i][j];
					}
					legalMoves[i] = new int[n][2];
					n = 0;
					for(int j = 0; j < pOrients[i]; j++) {
						for(int k = 0; k < COLS+1-pWidth[i][j];k++) {
							legalMoves[i][n][ORIENT] = j;
							legalMoves[i][n][SLOT] = k;
							n++;
						}
					}
				}
				
				nextPiece = s.getNextPiece();
				
				int currentLegalMoves[][] = s.legalMoves();
				for(int i = 0; i < this.legalMoves[nextPiece].length;i++)
					for(int j = 0; j < this.legalMoves[nextPiece][i].length;j++)
						this.legalMoves[nextPiece][i][j] = currentLegalMoves[i][j];
				
				int currentField[][] = s.getField();
				for(int i = 0; i < ROWS;i++)
					for(int j = 0; j < COLS;j++)
						this.field[i][j] = currentField[i][j];
				
				int currentTop[] = s.getTop();
				for(int i = 0; i  < currentTop.length; i++)
					this.top[i] = currentTop[i];
			}
	
			public int[][] getField() {
				return field;
			}
			
			public int[] getTop() {
				return top;
			}
	
			public int getRowsCleared() {
				return cleared;
			}
			
			public void makeMove(int[] move) {
				makeMove(move[ORIENT],move[SLOT]);
			}
			
			public void makeMove(int orient, int slot) {
				turn++;
				int height = top[slot]-pBottom[nextPiece][orient][0];
				for(int c = 1; c < pWidth[nextPiece][orient];c++) {
					height = Math.max(height,top[slot+c]-pBottom[nextPiece][orient][c]);
				}
				
				if(height+pHeight[nextPiece][orient] >= ROWS){
					hasLost = true;
					return;
				}
	
				
				for(int i = 0; i < pWidth[nextPiece][orient]; i++) {
					for(int h = height+pBottom[nextPiece][orient][i]; h < height+pTop[nextPiece][orient][i]; h++) {
						field[h][i+slot] = turn;
					}
				}
				
				for(int c = 0; c < pWidth[nextPiece][orient]; c++) {
					top[slot+c]=height+pTop[nextPiece][orient][c];
				}
				
				int rowsCleared = 0;
				
				for(int r = height+pHeight[nextPiece][orient]-1; r >= height; r--) {
					boolean full = true;
					for(int c = 0; c < COLS; c++) {
						if(field[r][c] == 0) {
							full = false;
							break;
						}
					}
	
					if(full) {
						rowsCleared++;
						cleared++;
						for(int c = 0; c < COLS; c++) {
							for(int i = r; i < top[c]; i++)
								field[i][c] = field[i+1][c];
							top[c]--;
							while(top[c]>=1 && field[top[c]-1][c]==0)	top[c]--;
						}
					}
				}
			}		
		}	
	}

}