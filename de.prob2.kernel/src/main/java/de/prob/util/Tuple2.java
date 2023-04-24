package de.prob.util;

import java.util.Objects;

/**
 * @deprecated All uses of this class in ProB 2 itself have been deprecated and replaced with other data structures.
 *     If you are using this class in your own code,
 *     consider creating a dedicated class for your use case instead,
 *     or switch to an established implementation like {@code Tuple2} from Groovy or {@code Pair} from Apache Commons or JavaFX.
 */
@Deprecated
public class Tuple2<S, T> {
	private final S first;
	private final T second;

	public Tuple2(S first, T second) {
		this.first = first;
		this.second = second;
	}

	public S getFirst() {
		return first;
	}

	public T getSecond() {
		return second;
	}

	@Override
	public String toString() {
		return "(" + first + "," + second + ")";
	}

	@Override
	public boolean equals(final Object obj) {
		if (this == obj) {
			return true;
		}
		if (obj == null || this.getClass() != obj.getClass()) {
			return false;
		}
		final Tuple2<?, ?> other = (Tuple2<?, ?>)obj;
		return Objects.equals(this.getFirst(), other.getFirst())
				&& Objects.equals(this.getSecond(), other.getSecond());
	}
	
	@Override
	public int hashCode() {
		return Objects.hash(this.getFirst(), this.getSecond());
	}
}
