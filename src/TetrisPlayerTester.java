import java.util.ArrayList;
import java.util.List;


public class TetrisPlayerTester implements Runnable{
	
	/* The number of games to play..
	 * A new thread is spawned for each new game,
	 * so dont go to crazy
	 */
	public static final int TESTS = 20;
	
	/* Change to true if you want to 
	 * show the game (instead of just 
	 * printing results). Unless your
	 * using a supercomputer, probably 
	 * don't want to have TESTS > ~5
	 */
	public static final boolean DRAW = false;
	
	public static int min = PlayerSkeleton.MAX;
	public static int max = PlayerSkeleton.MIN;
	public static double average = 0.0;
	
	public static Object maxLock = new Object();
	public static Object minLock = new Object();
	public static Object averageLock = new Object();
	
	public void run(){
		State s = new State();
		if(DRAW)
			new TFrame(s);
		PlayerSkeleton p = new PlayerSkeleton();
		
		while(!s.hasLost()){ 
			s.makeMove( p.pickMove( s,s.legalMoves() ) );
			if(DRAW){
				s.draw();
				s.drawNext(0,0);
			}
			
			try {
				Thread.sleep(10);
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
		}
		
		int rowsCleared = s.getRowsCleared();
		
		synchronized(maxLock){
			if(rowsCleared > max)
				max = rowsCleared;
			
			maxLock.notify();
		}

		synchronized(minLock){
			if(rowsCleared < min)
				min = rowsCleared;
			
			minLock.notify();
		}

		synchronized(averageLock){
			average += rowsCleared;
			
			averageLock.notify();
		}
		
		System.out.println("ROWS CLEARED: " + rowsCleared);
	}
	
	public static void main(String[] args) {
		List<Thread> threadList = new ArrayList<Thread>();
		
		//Instantiate each new thread
		for(int i = 0; i < TESTS; i++){
			Thread t = new Thread( new TetrisPlayerTester() );
			t.start();
			threadList.add(t);
		}
		
		//Wait for all threads before printing final results..
		try {
			for(Thread t: threadList)
				t.join();
		} catch (InterruptedException e) {}
		
		System.err.println("MIN: " + min + " MAX: " + max + " AVERAGE: " + average/TESTS);
		
	}

}
