package tetris;


public class Helper {
	
	public static final double eps = 0.00001;
	
	public static  double[][] clone(double[][] array){
		if(array.length == 0){
			return new double[0][0];
		}
		
		double[][] result = new double[array.length][array[0].length];
		
		for(int i =0; i<array.length; i++){
			System.arraycopy(array[i], 0, result[i], 0, array[i].length);
		}
		
		return result;
	}
	
	public static  int[][] clone(int[][] array){
		if(array.length == 0){
			return new int[0][0];
		}
		
		int[][] result = new int[array.length][array[0].length];
		
		for(int i =0; i<array.length; i++){
			System.arraycopy(array[i], 0, result[i], 0, array[i].length);
		}
		
		return result;
	}

}
