

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
