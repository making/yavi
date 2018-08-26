package am.ik.yavi.core;

import java.util.Objects;

public class ViolatedValue {
	private final Object value;

	public ViolatedValue(Object value) {
		this.value = value;
	}

	public Object value() {
		return this.value;
	}

	@Override
	public String toString() {
		return Objects.toString(this.value, "");
	}
}
