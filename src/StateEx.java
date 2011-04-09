/**
 * This class is an extended version of the State class and is used to evaluate the base functions.
 * @author rohrmann
 *
 */
public class StateEx {

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
}
