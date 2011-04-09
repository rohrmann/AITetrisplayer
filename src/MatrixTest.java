

import org.junit.Assert;
import org.junit.Test;

public class MatrixTest {

	@Test
	public void testInvert() {
		int n = 10;
		double[][] values = new double[n][n];

		for (int i = 0; i < n; i++) {
			for (int j = 0; j < n; j++) {
				values[i][j] = Math.pow(i + 1, j);
			}
		}

		Matrix m = new Matrix(values);
		Matrix im = m.invert();
		Matrix id = Matrix.identity(n);

		m.mul(im).printMatrix();

		Assert.assertTrue(id.equals(m.mul(im)));
	}

}
