

import java.util.ArrayList;
import java.util.List;

/**
 * Aggregator class for the base functions
 * @author rohrmann
 *
 */
public class BaseFunctions {

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
