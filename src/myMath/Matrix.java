package myMath;

import tetris.Helper;

public class Matrix implements Cloneable {
	private int m;
	private int n;
	private double [][] values;
	
	public Matrix(){
		m = 0;
		n = 0;
		
		initMatrix();
	}
	
	public Matrix(int m, int n){
		this.m = m;
		this.n = n;
		initMatrix();
	}
	
	public Matrix(double[][] values){
		this.values = values;
		
		m = values.length;
		
		if(0 ==m){
			n =0;
		}
		else
			n = values[0].length;
	}
	
	protected void initMatrix(){
		values = new double[m][n];
		
		clear();
	}
	
	public double at(int x, int y){
		if(x < 0 || x >= m || y < 0 || y >=n){
			throw new IllegalArgumentException();
		}
		
		return values[x][y];
	}
	
	public void set(int x, int y, double value){
		if(x < 0 || x>= m || y < 0 || y >= n){
			throw new IllegalArgumentException();
		}
		
		values[x][y] = value;
	}
	
	public void resize(int newM, int newN){
		double[][] newValues = new double[newM][newN];
		
		int minM = Math.min(newM, m);
		int minN = Math.min(newN,n);
		
		for(int i = 0; i < minM ;i++){
			for(int j = 0; j < minN; j++){
				newValues[i][j] = values[i][j];
			}
		}
		
		values = newValues;
		m = newM;
		n = newN;
	}
	
	public Matrix add(Matrix operand){
		if(operand.m != m || operand.n != n){
			throw new IllegalArgumentException();
		}
		
		double[][] result = new double[m][n];
		
		for(int i =0; i< m; i++){
			for(int j =0; j< n; j++){
				result[i][j] = values[i][j] + operand.values[i][j];
			}
		}
		
		return new Matrix(result);
	}
	
	public Matrix mul(double scalar){
		
		double[][] result = new double[m][n];
		
		for(int i=0; i < m; i++){
			for(int j =0; j< n; j++){
				result[i][j] = values[i][j]*scalar;
			}
		}
		
		return new Matrix(result);
	}
	
	public Matrix sub(Matrix operand){
		return add(operand.mul(-1));
	}
	
	public Matrix mul(Matrix operand){
		if(n != operand.m){
			throw new IllegalArgumentException("n==operand.m for matrix-matrix multiplication");
		}
		
		double[][] result = new double[m][operand.n];
		
		for(int i =0; i < m; i++){
			for(int j =0; j < operand.n;j++){
				double temp = 0;
				for(int k = 0; k < n; k++){
					temp += values[i][k]*operand.values[k][j];
				}
				result[i][j] = temp;
			}
		}
		
		return new Matrix(result);
	}
	
	public void clear(){
		for(int i =0; i< m; i++){
			for(int j =0; j < n; j++){
				values[i][j] = 0;
			}
		}
	}
	
	
	
	public Matrix invert(){
		assert(m == n);
		
		Matrix tempMatrix = identity(m);
		double[][] temp = tempMatrix.values;
		double[][] workingValues = Helper.clone(values);
		int[] index = new int[m];
		
		for(int i=0; i<m; i++){
			index[i] = i;
		}
		
		//establish upper triangular matrix
		for(int i=0; i<m; i++){
			int maxIndex = -1;
			double maxValue=0;
			
			//find max value in column i
			for(int j=i;j<m;j++){
				if(Math.abs(workingValues[index[j]][i]) > maxValue){
					maxIndex = j;
					maxValue = Math.abs(workingValues[index[j]][i]);
				}
			}
			
			if(maxIndex == -1){
				printMappedArray(workingValues, index);
				throw new IllegalArgumentException("Matrix is singular");
			}
			
			int swap = index[i];
			index[i] = index[maxIndex];
			index[maxIndex] = swap;
			
			maxValue = workingValues[index[i]][i];
			
			//eliminate entries in column j below row i
			for(int j = i+1;j<m;j++){
				double value = workingValues[index[j]][i]/maxValue;
				workingValues[index[j]][i] = 0;
				
				for(int k =  i+1; k < m; k++){
					workingValues[index[j]][k] = workingValues[index[j]][k] - value*workingValues[index[i]][k];
				}
				
				for(int k = 0; k< m; k++){
					temp[index[j]][k] = temp[index[j]][k] - value*temp[index[i]][k];
				}
			}
		}
		
		//establish identity matrix
		for(int i=m-1; i>=0; i--){
			for(int j = i-1; j>=0;j--){
				double value = workingValues[index[j]][i]/workingValues[index[i]][i];
				workingValues[index[j]][i] = 0;
				
				for(int k =0; k<m; k++){
					temp[index[j]][k] -= value*temp[index[i]][k];
				}
			}
			
			double value = workingValues[index[i]][i];
			workingValues[index[i]][i] = 1;
			
			for(int k=0; k<m; k++){
				temp[index[i]][k] /= value;
			}
		}
		
		double[][] result = new double[m][m];
		
		for(int i=0; i<m; i++){
			for(int j=0; j<m; j++){
				result[i][j] = temp[index[i]][j];
			}
		}
		
		return new Matrix(result);
	}
	
	
	private void printMappedArray(double[][] array, int[] mapping){
		for(int i=0; i< array.length; i++){
			for(int j =0; j<array[0].length;j++){
				System.out.print(array[mapping[i]][j]);
				
				if(j < array[0].length-1){
					System.out.print(";");
				}
			}
			System.out.println();
		}
	}
	
	public void printMatrix(){
		for(int i =0; i<m; i++){
			for(int j=0; j<n; j++){
				System.out.print(values[i][j]);
				if(j < n-1){
					System.out.print(";");
				}
			}
			System.out.println();
		}
	}
	
	public Matrix transpose(){
		double[][] result = new double[n][m];
		
		for(int i=0; i<m; i++){
			for(int j=0; j<n;j++){
				result[j][i] = values[i][j];
			}
		}
		
		return new Matrix(result);
	}
	
	public double dot(Matrix operand){
		assert(m==operand.m && n==1 && operand.n==1);
		
		double result = 0;
		
		for(int i =0; i< m; i++){
			result += values[i][0]*operand.values[i][0];
		}
		
		return result;
	}
	
	public static Matrix identity(int n){
		double[][] values = new double[n][n];
		
		for(int i= 0; i<n; i++){
			for(int j = 0; j<n; j++){
				values[i][j] = i==j?1:0;
			}
		}
		
		return new Matrix(values);
	}
	
	@Override
	public boolean equals(Object obj){
		if(obj == null || !(obj instanceof Matrix)){
			return false;
		}
		
		Matrix matrix = (Matrix)obj;
		
		if(matrix.m != m || matrix.n != n)
			return false;
		
		for(int i =0; i< m; i++){
			for(int j=0; j<n;j++){
				if(Math.abs(matrix.values[i][j]-values[i][j]) > Helper.eps){
					return false;
				}
			}
		}
		
		return true;
	}
	
	@Override
	public Matrix clone(){
		double[][] result = Helper.clone(values);
		return new Matrix(result);
	}
	
	public static void main(String[]args){
		double[][] values = {{4,3},{2,1}};
		
		Matrix matrix = new Matrix(values);
		
		Matrix inverse = matrix.invert();
		
		inverse.printMatrix();
		
		Matrix mul = matrix.mul(inverse);
		
		mul.printMatrix();
	}
	
	@Override
	public int hashCode(){
		int result = 0;
		
		for(int i =0; i<m; i++){
			for(int j=0; j<n; j++){
				long v = Double.doubleToLongBits(values[i][j]);
				result ^= (int)((v>>32)^v);
			}
		}
		
		return result;
	}
}
