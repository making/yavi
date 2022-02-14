package am.ik.yavi.core;

import java.util.Arrays;

final class ArrayArgument {
	private final Object[] value;

	ArrayArgument(Object[] value) {
		this.value = value;
	}

	public Object[] getValue() {
		return value;
	}

	@Override
	public String toString() {
		return Arrays.toString(value);
	}
}
