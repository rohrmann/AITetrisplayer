/**
 * Convenience class. If you want to give 2 values back from a method then you don't have to
 * write an own class, just use the Pair<S,T> class.
 * @author rohrmann
 *
 * @param <S>
 * @param <T>
 */
public class Pair<S, T> {
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
