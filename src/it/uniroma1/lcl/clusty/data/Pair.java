package it.uniroma1.lcl.clusty.data;

/**
 * 
 * @author 
 *
 * @param <T> the first pair element
 * @param <S> the second pair element
 */
public class Pair<T, S> {

	private T first;
	private S second;

	public Pair(T first, S second) {
		this.first = first;
		this.second = second;
	}

	/**
	 * @return Returns the first pair element
	 */
	public T getFirst() {
		return first;
	}

	/**
	 * @return Returns the second pair element
	 */
	public S getSecond() {
		return second;
	}
}
