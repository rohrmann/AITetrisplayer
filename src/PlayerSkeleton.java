import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * This class is an extended version of the State class and is used to evaluate the base functions.
 * @author rohrmann
 *
 */
class StateEx {

	public static final int COLS = 10;
	public static final int ROWS = 21;
	public static final int N_PIECES = 7;

	public boolean lost = false;
	
	// current turn
	private int turn = 0;
	private int cleared = 0;

	private int landingHeight;
	private int erodedPieces;
	private int lastPiece;
	private double heightPiece;

	// each square in the grid - int means empty - other values mean the turn it
	// was placed
	private int[][] field = new int[ROWS][COLS];
	// top row+1 of each column
	// 0 means empty
	private int[] top = new int[COLS];

	// number of next piece
	protected int nextPiece;

	// all legal moves - first index is piece type - then a list of 2-length
	// arrays
	protected static int[][][] legalMoves = new int[N_PIECES][][];

	// indices for legalMoves
	public static final int ORIENT = 0;
	public static final int SLOT = 1;

	// possible orientations for a given piece type
	protected static int[] pOrients = { 1, 2, 4, 4, 4, 2, 2 };

	// the next several arrays define the piece vocabulary in detail
	// width of the pieces [piece ID][orientation]
	protected static int[][] pWidth = { { 2 }, { 1, 4 }, { 2, 3, 2, 3 },
			{ 2, 3, 2, 3 }, { 2, 3, 2, 3 }, { 3, 2 }, { 3, 2 } };
	// height of the pieces [piece ID][orientation]
	private static int[][] pHeight = { { 2 }, { 4, 1 }, { 3, 2, 3, 2 },
			{ 3, 2, 3, 2 }, { 3, 2, 3, 2 }, { 2, 3 }, { 2, 3 } };
	private static int[][][] pBottom = { { { 0, 0 } },
			{ { 0 }, { 0, 0, 0, 0 } },
			{ { 0, 0 }, { 0, 1, 1 }, { 2, 0 }, { 0, 0, 0 } },
			{ { 0, 0 }, { 0, 0, 0 }, { 0, 2 }, { 1, 1, 0 } },
			{ { 0, 1 }, { 1, 0, 1 }, { 1, 0 }, { 0, 0, 0 } },
			{ { 0, 0, 1 }, { 1, 0 } }, { { 1, 0, 0 }, { 0, 1 } } };
	private static int[][][] pTop = { { { 2, 2 } }, { { 4 }, { 1, 1, 1, 1 } },
			{ { 3, 1 }, { 2, 2, 2 }, { 3, 3 }, { 1, 1, 2 } },
			{ { 1, 3 }, { 2, 1, 1 }, { 3, 3 }, { 2, 2, 2 } },
			{ { 3, 2 }, { 2, 2, 2 }, { 2, 3 }, { 1, 2, 1 } },
			{ { 1, 2, 2 }, { 3, 2 } }, { { 2, 2, 1 }, { 2, 3 } } };

	// initialize legalMoves
	{
		// for each piece type
		for (int i = 0; i < N_PIECES; i++) {
			// figure number of legal moves
			int n = 0;
			for (int j = 0; j < pOrients[i]; j++) {
				// number of locations in this orientation
				n += COLS + 1 - pWidth[i][j];
			}
			// allocate space
			legalMoves[i] = new int[n][2];
			// for each orientation
			n = 0;
			for (int j = 0; j < pOrients[i]; j++) {
				// for each slot
				for (int k = 0; k < COLS + 1 - pWidth[i][j]; k++) {
					legalMoves[i][n][ORIENT] = j;
					legalMoves[i][n][SLOT] = k;
					n++;
				}
			}
		}

	}

	public int[][] getField() {
		return field;
	}

	public int[] getTop() {
		return top;
	}

	public static int[] getpOrients() {
		return pOrients;
	}

	public static int[][] getpWidth() {
		return pWidth;
	}

	public static int[][] getpHeight() {
		return pHeight;
	}

	public static int[][][] getpBottom() {
		return pBottom;
	}

	public static int[][][] getpTop() {
		return pTop;
	}

	public int getNextPiece() {
		return nextPiece;
	}

	public boolean hasLost() {
		return lost;
	}

	public int getRowsCleared() {
		return cleared;
	}

	public int getTurnNumber() {
		return turn;
	}

	// constructor
	public StateEx(State state) {
		lost = state.lost;
		turn = state.getTurnNumber();
		cleared = state.getRowsCleared();
		landingHeight = -1;
		erodedPieces = 0;
		lastPiece = -1;
		heightPiece = -1;
		nextPiece = state.nextPiece;
		
		top = state.getTop().clone();
		field = Helper.clone(state.getField());
	}
	
	public StateEx(){
		lost = false;
		turn = 0;
		cleared =0;
		landingHeight = -1;
		erodedPieces = 0;
		lastPiece = -1;
		heightPiece = -1.0;
		nextPiece = randomPiece();
	}

	// random integer, returns 0-6
	private int randomPiece() {
		return (int) (Math.random() * N_PIECES);
	}

	// gives legal moves for
	public int[][] legalMoves() {
		return legalMoves[nextPiece];
	}

	// make a move based on the move index - its order in the legalMoves list
	public boolean makeMove(int move) {
		return makeMove(legalMoves[nextPiece][move]);
	}

	// make a move based on an array of orient and slot
	public boolean makeMove(int[] move) {
		return makeMove(move[ORIENT], move[SLOT]);
	}

	// returns false if you lose - true otherwise
	public boolean makeMove(int orient, int slot) {
		turn++;
		// height if the first column makes contact
		int height = top[slot] - pBottom[nextPiece][orient][0];
		// for each column beyond the first in the piece
		for (int c = 1; c < pWidth[nextPiece][orient]; c++) {
			height = Math.max(height, top[slot + c]
					- pBottom[nextPiece][orient][c]);
		}

		// check if game ended
		if (height + pHeight[nextPiece][orient] >= ROWS) {
			lost = true;
			return false;
		}

		landingHeight = height;
		erodedPieces = 0;
		lastPiece = nextPiece;
		//save the landing height of the current piece
		heightPiece = landingHeight + (pHeight[nextPiece][orient]-1) / 2.0;

		// for each column in the piece - fill in the appropriate blocks
		for (int i = 0; i < pWidth[nextPiece][orient]; i++) {

			// from bottom to top of brick
			for (int h = height + pBottom[nextPiece][orient][i]; h < height
					+ pTop[nextPiece][orient][i]; h++) {
				field[h][i + slot] = turn;
			}
		}

		// adjust top
		for (int c = 0; c < pWidth[nextPiece][orient]; c++) {
			top[slot + c] = height + pTop[nextPiece][orient][c];
		}

		int rowsCleared = 0;

		// check for full rows - starting at the top
		for (int r = height + pHeight[nextPiece][orient] - 1; r >= height; r--) {
			// check all columns in the row
			boolean full = true;
			for (int c = 0; c < COLS; c++) {
				if (field[r][c] == 0) {
					full = false;
					break;
				}
			}
			// if the row was full - remove it and slide above stuff down
			if (full) {
				rowsCleared++;
				cleared++;
				// for each column
				for (int c = 0; c < COLS; c++) {
					//check whether the eroded cell at (r,c) belongs to the last piece.
					if (field[r][c] == turn) {
						erodedPieces++;
					}
					// slide down all bricks
					for (int i = r; i < top[c]; i++) {
						field[i][c] = field[i + 1][c];
					}
					// lower the top
					top[c]--;
					while (top[c] >= 1 && field[top[c] - 1][c] == 0)
						top[c]--;
				}
			}
		}

		// pick a new piece
		nextPiece = randomPiece();

		return true;
	}

	
	@Override
	public StateEx clone() {
		StateEx result = new StateEx();
		result.cleared = cleared;
		result.field = Helper.clone(field);
		result.top = top.clone();
		result.turn = turn;
		result.nextPiece = nextPiece;

		return result;
	}

	public int getMaxHeight() {
		int max = top[0];

		for (int i = 1; i < COLS; i++) {
			if (max < top[i]) {
				max = top[i];
			}
		}
		return max;
	}

	public int getNumHoles() {
		int result = 0;
		for (int c = 0; c < COLS; c++) {
			for (int r = 0; r < top[c] - 1; r++) {
				if (field[r][c] == 0) {
					result++;
				}
			}
		}

		return result;
	}

	public double getMeanHeight() {
		double result = 0;

		for (int c = 0; c < COLS; c++) {
			result += top[c];
		}

		return result / COLS;
	}

	public int getAbsoluteHeightDiff() {
		int result = 0;

		for (int c = 0; c < COLS - 1; c++) {
			result += Math.abs(top[c] - top[c + 1]);
		}

		return result;
	}

	public int getTop(int column) {
		return top[column];
	}

	public static StateEx getRandomStateEx() {
		StateEx result = new StateEx();

		for (int i = 0; i < ROWS - 1; i++) {
			for (int j = 0; j < COLS; j++) {
				if (Math.random() < 0.75) {
					result.field[i][j] = 1;
				} else {
					result.field[i][j] = 0;
				}
			}
		}

		for (int c = 0; c < COLS; c++) {
			for (int r = ROWS - 1; r >= 0; r--) {
				if (result.field[r][c] != 0) {
					result.top[c] = r + 1;
					break;
				}
			}
		}

		return result;
	}

	public void clear() {
		for (int i = 0; i < ROWS; i++) {
			for (int j = 0; j < COLS; j++) {
				field[i][j] = 0;
			}
		}

		nextPiece = randomPiece();
		cleared = 0;
		turn = 0;
		lost = false;
		
		erodedPieces = 0;
		landingHeight = -1;
		heightPiece = -1.0;
		lastPiece = -1;
	}

	public int getLandingHeight() {
		return landingHeight;
	}

	public int getLastPiece() {
		return lastPiece;
	}

	public double getHeightPiece() {
		return heightPiece;
	}

	public int getErodedCells() {
		return erodedPieces;
	}
	
	@Override
	public int hashCode(){
		int result = 0;
		int temp = 0;
		int counter = 0;
		
		for(int r = 0; r < State.ROWS-1;r++){
			for(int c =0; c < State.COLS;c++){
				if(field[r][c] != 0){
					temp |= 1 << counter;
				}
				
				counter++;
				
				if(counter >= 32){
					result ^= temp;
					counter =0;
				}
			}
		}
		
		result ^= temp;
		
		return result;
	}
}


/**
 * Interface for the base functions
 * 
 * @author rohrmann
 *
 */
interface BaseFunction {
	public double evaluate(StateEx oldState, StateEx newState);

}

/**
 * Aggregator class for the base functions
 * @author rohrmann
 *
 */
class BaseFunctions {

	List<BaseFunction> baseFunctions;

	public BaseFunctions() {
		baseFunctions = new ArrayList<BaseFunction>();
	}

	public void add(BaseFunction function) {
		baseFunctions.add(function);
	}

	public Matrix evaluate(StateEx oldState, StateEx newState) {
		double[][] result = new double[baseFunctions.size()][1];

		int i = 0;
		for (BaseFunction function : baseFunctions) {
			result[i++][0] = function.evaluate(oldState, newState);
		}

		return new Matrix(result);
	}

	public int size() {
		return baseFunctions.size();
	}

}

/**
 * base function which calculates the absolute height difference between two adjacent columns
 * @author rohrmann
 *
 */
class BFAAHD implements BaseFunction {

	private int column;

	public BFAAHD(int column) {
		this.column = column;
	}

	@Override
	public double evaluate(StateEx oldState, StateEx newState) {
		return Math.abs(newState.getTop(column) - newState.getTop(column + 1));
	}

}

/**
 * base function which calculates the difference of the absolute height difference between two adjacent columns
 * @author rohrmann
 *
 */
class BFAAHDDiff implements BaseFunction {
	private int column;

	public BFAAHDDiff(int column) {
		this.column = column;
	}

	@Override
	public double evaluate(StateEx oldState, StateEx newState) {
		return Math.abs(newState.getTop(column) - newState.getTop(column + 1))
				- Math.abs(oldState.getTop(column) - newState.getTop(column)
						- 1);
	}

}

/**
 * base function which calculates the absolute height difference = sum of absolute height differences of adjacent columns
 * @author rohrmann
 *
 */
class BFAHD implements BaseFunction {

	@Override
	public double evaluate(StateEx oldState, StateEx newState) {
		return newState.getAbsoluteHeightDiff();
	}

}

/**
 * base function which calculates the difference of the absolute height difference between the old and the
 * new state. The absoulte height difference is the sum of the absolute height differences of adjacent columns.
 * @author rohrmann
 *
 */
class BFAHDDiff implements BaseFunction {

	@Override
	public double evaluate(StateEx oldState, StateEx newState) {
		return newState.getAbsoluteHeightDiff()
				- oldState.getAbsoluteHeightDiff();
	}

}

/**
 * base functions which returns the height of a specified column
 * @author rohrmann
 *
 */
class BFColumnHeight implements BaseFunction {
	private int index;

	public BFColumnHeight(int index) {
		this.index = index;
	}

	@Override
	public double evaluate(StateEx oldState, StateEx newState) {
		return newState.getTop(index);
	}

}

/**
 * base function which calculates the height difference of a specified column between the new and the old state
 * @author rohrmann
 *
 */
class BFColumnHeightDiff implements BaseFunction {
	private int column;

	public BFColumnHeightDiff(int column) {
		this.column = column;
	}

	@Override
	public double evaluate(StateEx oldState, StateEx newState) {
		return newState.getTop(column) - oldState.getTop(column);
	}

}

/**
 * base functions which calculates the column transition. A column transition occurs if a full cell
 * is directly below an empty cell in the same column or vice versa. The bottom row is considered to 
 * contain full cells.
 * @author rohrmann
 *
 */
class BFColumnTransitions implements BaseFunction {

	@Override
	public double evaluate(StateEx oldState, StateEx newState) {
		int result = 0;
		int[][] field = newState.getField();

		for (int c = 0; c < State.COLS; c++) {
			boolean full = true;
			for (int r = 0; r < State.ROWS - 1; r++) {
				if (full == true && field[r][c] == 0) {
					result++;
					full = false;
				} else if (full == false && field[r][c] != 0) {
					result++;
					full = true;
				}
			}
		}

		return result;
	}

}

/**
 * base function which returns the completed rows after one move
 * @author rohrmann
 *
 */
class BFCompletedRows implements BaseFunction {

	@Override
	public double evaluate(StateEx oldState, StateEx newState) {
		return newState.getRowsCleared() - oldState.getRowsCleared();
	}

}

/**
 * base function which returns a constant value
 * @author rohrmann
 *
 */
class BFConstant implements BaseFunction {
	private double value;

	public BFConstant(double value) {
		this.value = value;
	}

	@Override
	public double evaluate(StateEx oldState, StateEx newState) {
		return value;
	}

}

/**
 * base function which returns the sum of all wells. A well is defined to be a one cell wide hole thus it has
 * to have full neighboring cells. The value of the well depends on how many wells are directly above this well.
 * e.g. | X X|
 * 		| XXX|
 * contains 3 wells:
 * 		|wXwX|
 * 		|wXXX|
 * and the values are
 * 		|1X1X|
 * 		|2XXX|
 * @author rohrmann
 *
 */
class BFCumulativeWells implements BaseFunction {

	@Override
	public double evaluate(StateEx oldState, StateEx newState) {
		int result = 0;
		int[][] field = newState.getField();

		for (int r = 0; r < State.ROWS - 1; r++) {
			for (int c = 0; c < State.COLS; c++) {
				if (isWell(field, r, c)) {
					result++;

					for (int r2 = r + 1; r2 < State.ROWS - 1; r2++) {
						if (isWell(field, r2, c)) {
							result++;
						} else {
							break;
						}
					}
				}
			}
		}

		return result;
	}

	private boolean isWell(int[][] field, int r, int c) {
		if (field[r][c] == 0) {
			if (c == 0) {
				return field[r][c + 1] != 0;
			} else if (c == State.COLS - 1) {
				return field[r][c - 1] != 0;
			} else {
				return field[r][c + 1] != 0 && field[r][c - 1] != 0;
			}
		}

		return false;
	}

}

/**
 * base function which returns the product of the completed rows and the eroded cells of the last place
 * piece.
 * @author rohrmann
 *
 */
class BFErodedCells implements BaseFunction {

	@Override
	public double evaluate(StateEx oldState, StateEx newState) {
		return newState.getErodedCells()
				* (newState.getRowsCleared() - oldState.getRowsCleared());
	}

}

/**
 * base function which returns the sum of all hole depths on the board. The depth of a hole is defined as
 * the number of full cells which are directly above the hole.
 * @author rohrmann
 *
 */
class BFHoleDepth implements BaseFunction {

	@Override
	public double evaluate(StateEx oldState, StateEx newState) {
		int[][] field = newState.getField();
		int result = 0;

		for (int c = 0; c < State.COLS; c++) {
			boolean hole = false;
			for (int r = 0; r < newState.getTop(c); r++) {
				if (hole == true && field[r][c] != 0) {
					result++;
				} else if (field[r][c] == 0) {
					hole = true;
				}

			}
		}

		return result;
	}

}

/**
 * base function which returns the landing height of the last moved piece. The landing height is the middle point of
 * the piece.
 * @author rohrmann
 *
 */
class BFLandingHeight implements BaseFunction {

	@Override
	public double evaluate(StateEx oldState, StateEx newState) {
		return newState.getHeightPiece();
	}

}

/**
 * base function which returns the maximum height of the new state.
 * @author rohrmann
 *
 */
class BFMaxHeight implements BaseFunction {

	@Override
	public double evaluate(StateEx oldState, StateEx newState) {
		return newState.getMaxHeight();
	}

}

/**
 * base function which returns the change of the maximum height between the new and the old state.
 * @author rohrmann
 *
 */
class BFMaxHeightDiff implements BaseFunction {

	@Override
	public double evaluate(StateEx oldState, StateEx newState) {
		return newState.getMaxHeight() - oldState.getMaxHeight();
	}

}

/**
 * base function which returns the mean height of the new state.
 * @author rohrmann
 *
 */
class BFMeanHeight implements BaseFunction {

	@Override
	public double evaluate(StateEx oldState, StateEx newState) {
		return newState.getMeanHeight();
	}

}

/**
 * Base function which returns the change of the mean height between the new and the old state.s
 * @author rohrmann
 *
 */
class BFMeanHeightDiff implements BaseFunction {

	@Override
	public double evaluate(StateEx oldState, StateEx newState) {
		return newState.getMeanHeight() - oldState.getMeanHeight();
	}

}

/**
 * Base function which returns the number of holes in the new state. A hole is defined as an empty cell
 * which is covered by a full cell. The full cell doesn't have to be directly above the empty cell.
 * @author rohrmann
 *
 */
class BFNumHoles implements BaseFunction {

	@Override
	public double evaluate(StateEx oldState, StateEx newState) {
		return newState.getNumHoles();
	}

}

/**
 * Base function which return the difference of the number of holes of the new state and the number of holes
 * of the old state.
 * @author rohrmann
 *
 */
class BFNumHolesDiff implements BaseFunction {

	@Override
	public double evaluate(StateEx oldState, StateEx newState) {
		return newState.getNumHoles() - oldState.getNumHoles();
	}

}

/**
 * Base function which returns the number of rows which contain at least one hole. In order to be a hole, an empty cell
 * has to be covered by a full cell which doesn't have to be directly above the empty cell.
 * @author rohrmann
 *
 */
class BFRowsWithHoles implements BaseFunction {

	@Override
	public double evaluate(StateEx oldState, StateEx newState) {
		int result = 0;

		int[][] field = newState.getField();

		for (int r = 0; r < newState.getMaxHeight(); r++) {
			for (int c = 0; c < State.COLS; c++) {
				if (field[r][c] == 0 && newState.getTop(c) > c) {
					result++;
					break;
				}
			}
		}

		return result;
	}

}

/**
 * base function which returns the number of row transition. A row transition is a change from a full cell to an emtpy
 * cell or vice versa in the same row. The side walls are considered to be full cells.
 * @author rohrmann
 *
 */
class BFRowTransitions implements BaseFunction {

	@Override
	public double evaluate(StateEx oldState, StateEx newState) {
		int[][] field = newState.getField();

		int result = 0;

		for (int i = 0; i < State.ROWS - 1; i++) {
			boolean full = true;
			for (int j = 0; j < State.COLS; j++) {
				if (full == true && field[i][j] == 0) {
					result++;
					full = false;
				} else if (full == false && field[i][j] != 0) {
					result++;
					full = true;
				}
			}

			if (full == false) {
				result++;
			}
		}

		return result;
	}

}

/**
 * Convenience class which contains helpful methods
 * @author rohrmann
 *
 */
class Helper {

	public static final double eps = 0.00001;

	public static double[][] clone(double[][] array) {
		if (array.length == 0) {
			return new double[0][0];
		}

		double[][] result = new double[array.length][array[0].length];

		for (int i = 0; i < array.length; i++) {
			System.arraycopy(array[i], 0, result[i], 0, array[i].length);
		}

		return result;
	}

	public static int[][] clone(int[][] array) {
		if (array.length == 0) {
			return new int[0][0];
		}

		int[][] result = new int[array.length][array[0].length];

		for (int i = 0; i < array.length; i++) {
			System.arraycopy(array[i], 0, result[i], 0, array[i].length);
		}

		return result;
	}

}

/**
 * This class represents a matrix whose values are doubles. It implements the most basic
 * operations on matrices.
 * @author rohrmann
 *
 */
class Matrix implements Cloneable {
	private int m;
	private int n;
	private double[][] values;

	public Matrix() {
		m = 0;
		n = 0;

		initMatrix();
	}

	public Matrix(int m, int n) {
		this.m = m;
		this.n = n;
		initMatrix();
	}

	public Matrix(double[][] values) {
		this.values = values;

		m = values.length;

		if (0 == m) {
			n = 0;
		} else
			n = values[0].length;
	}

	protected void initMatrix() {
		values = new double[m][n];

		clear();
	}

	public double at(int x, int y) {
		if (x < 0 || x >= m || y < 0 || y >= n) {
			throw new IllegalArgumentException();
		}

		return values[x][y];
	}

	public void set(int x, int y, double value) {
		if (x < 0 || x >= m || y < 0 || y >= n) {
			throw new IllegalArgumentException();
		}

		values[x][y] = value;
	}

	public void resize(int newM, int newN) {
		double[][] newValues = new double[newM][newN];

		int minM = Math.min(newM, m);
		int minN = Math.min(newN, n);

		for (int i = 0; i < minM; i++) {
			for (int j = 0; j < minN; j++) {
				newValues[i][j] = values[i][j];
			}
		}

		values = newValues;
		m = newM;
		n = newN;
	}

	public Matrix add(Matrix operand) {
		if (operand.m != m || operand.n != n) {
			throw new IllegalArgumentException();
		}

		double[][] result = new double[m][n];

		for (int i = 0; i < m; i++) {
			for (int j = 0; j < n; j++) {
				result[i][j] = values[i][j] + operand.values[i][j];
			}
		}

		return new Matrix(result);
	}

	public Matrix mul(double scalar) {

		double[][] result = new double[m][n];

		for (int i = 0; i < m; i++) {
			for (int j = 0; j < n; j++) {
				result[i][j] = values[i][j] * scalar;
			}
		}

		return new Matrix(result);
	}

	public Matrix sub(Matrix operand) {
		return add(operand.mul(-1));
	}

	public Matrix mul(Matrix operand) {
		if (n != operand.m) {
			throw new IllegalArgumentException(
					"n==operand.m for matrix-matrix multiplication");
		}

		double[][] result = new double[m][operand.n];

		for (int i = 0; i < m; i++) {
			for (int j = 0; j < operand.n; j++) {
				double temp = 0;
				for (int k = 0; k < n; k++) {
					temp += values[i][k] * operand.values[k][j];
				}
				result[i][j] = temp;
			}
		}

		return new Matrix(result);
	}

	public void clear() {
		for (int i = 0; i < m; i++) {
			for (int j = 0; j < n; j++) {
				values[i][j] = 0;
			}
		}
	}

	public Matrix invert() {
		assert (m == n);

		Matrix tempMatrix = identity(m);
		double[][] temp = tempMatrix.values;
		double[][] workingValues = Helper.clone(values);
		int[] index = new int[m];

		for (int i = 0; i < m; i++) {
			index[i] = i;
		}

		// establish upper triangular matrix
		for (int i = 0; i < m; i++) {
			int maxIndex = -1;
			double maxValue = 0;

			// find max value in column i
			for (int j = i; j < m; j++) {
				if (Math.abs(workingValues[index[j]][i]) > maxValue) {
					maxIndex = j;
					maxValue = Math.abs(workingValues[index[j]][i]);
				}
			}

			if (maxIndex == -1) {
				printMappedArray(workingValues, index);
				throw new IllegalArgumentException("Matrix is singular");
			}

			int swap = index[i];
			index[i] = index[maxIndex];
			index[maxIndex] = swap;

			maxValue = workingValues[index[i]][i];

			// eliminate entries in column j below row i
			for (int j = i + 1; j < m; j++) {
				double value = workingValues[index[j]][i] / maxValue;
				workingValues[index[j]][i] = 0;

				for (int k = i + 1; k < m; k++) {
					workingValues[index[j]][k] = workingValues[index[j]][k]
							- value * workingValues[index[i]][k];
				}

				for (int k = 0; k < m; k++) {
					temp[index[j]][k] = temp[index[j]][k] - value
							* temp[index[i]][k];
				}
			}
		}

		// establish identity matrix
		for (int i = m - 1; i >= 0; i--) {
			for (int j = i - 1; j >= 0; j--) {
				double value = workingValues[index[j]][i]
						/ workingValues[index[i]][i];
				workingValues[index[j]][i] = 0;

				for (int k = 0; k < m; k++) {
					temp[index[j]][k] -= value * temp[index[i]][k];
				}
			}

			double value = workingValues[index[i]][i];
			workingValues[index[i]][i] = 1;

			for (int k = 0; k < m; k++) {
				temp[index[i]][k] /= value;
			}
		}

		double[][] result = new double[m][m];

		for (int i = 0; i < m; i++) {
			for (int j = 0; j < m; j++) {
				result[i][j] = temp[index[i]][j];
			}
		}

		return new Matrix(result);
	}

	/**
	 * This function is only used for debugging purposes. It prints the array array so that the ordering
	 * of the rows conforms with the row ordering specified by mapping. The entry mapping[i] is the row index
	 * which is supposed to be the i-th row.
	 * @param array
	 * @param mapping
	 */
	private void printMappedArray(double[][] array, int[] mapping) {
		for (int i = 0; i < array.length; i++) {
			for (int j = 0; j < array[0].length; j++) {
				System.out.print(array[mapping[i]][j]);

				if (j < array[0].length - 1) {
					System.out.print(";");
				}
			}
			System.out.println();
		}
	}

	public void printMatrix() {
		for (int i = 0; i < m; i++) {
			for (int j = 0; j < n; j++) {
				System.out.print(values[i][j]);
				if (j < n - 1) {
					System.out.print(";");
				}
			}
			System.out.println();
		}
	}

	public Matrix transpose() {
		double[][] result = new double[n][m];

		for (int i = 0; i < m; i++) {
			for (int j = 0; j < n; j++) {
				result[j][i] = values[i][j];
			}
		}

		return new Matrix(result);
	}

	public double dot(Matrix operand) {
		assert (m == operand.m && n == 1 && operand.n == 1);

		double result = 0;

		for (int i = 0; i < m; i++) {
			result += values[i][0] * operand.values[i][0];
		}

		return result;
	}

	public static Matrix identity(int n) {
		double[][] values = new double[n][n];

		for (int i = 0; i < n; i++) {
			for (int j = 0; j < n; j++) {
				values[i][j] = i == j ? 1 : 0;
			}
		}

		return new Matrix(values);
	}

	@Override
	public boolean equals(Object obj) {
		if (obj == null || !(obj instanceof Matrix)) {
			return false;
		}

		Matrix matrix = (Matrix) obj;

		if (matrix.m != m || matrix.n != n)
			return false;

		for (int i = 0; i < m; i++) {
			for (int j = 0; j < n; j++) {
				if (Math.abs(matrix.values[i][j] - values[i][j]) > Helper.eps) {
					return false;
				}
			}
		}

		return true;
	}

	@Override
	public Matrix clone() {
		double[][] result = Helper.clone(values);
		return new Matrix(result);
	}

	public static void main(String[] args) {
		double[][] values = { { 4, 3 }, { 2, 1 } };

		Matrix matrix = new Matrix(values);

		Matrix inverse = matrix.invert();

		inverse.printMatrix();

		Matrix mul = matrix.mul(inverse);

		mul.printMatrix();
	}

	@Override
	public int hashCode() {
		int result = 0;

		for (int i = 0; i < m; i++) {
			for (int j = 0; j < n; j++) {
				long v = Double.doubleToLongBits(values[i][j]);
				result ^= (int) ((v >> 32) ^ v);
			}
		}

		return result;
	}
}

/**
 * Convenience class. If you want to give 2 values back from a method then you don't have to
 * write an own class, just use the Pair<S,T> class.
 * @author rohrmann
 *
 * @param <S>
 * @param <T>
 */
class Pair<S, T> {
	private S aValue;
	private T bValue;

	public Pair(S aValue, T bValue) {
		this.aValue = aValue;
		this.bValue = bValue;
	}

	public S a() {
		return aValue;
	}

	public T b() {
		return bValue;
	}

}

/**
 * This class encapsulates the necessary information for one sample.
 * @author rohrmann
 *
 */
class Sample {
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
	private final int gamesPerIteration = 1;
	private final int intialSamples = 1000;
	private final int maxSamples = 5000;

	public PlayerSkeleton() {
		baseFunctions = new BaseFunctions();
		initDellacherie();
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