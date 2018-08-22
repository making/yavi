package am.ik.yavi.core;

import java.util.function.Predicate;
import java.util.function.Supplier;

public class ConstraintPredicate<V> {
	private final Predicate<V> predicate;
	private final String messageKey;
	private final String defaultMessageFormat;
	private final Supplier<Object[]> args;
	private final NullValidity nullValidity;

	public ConstraintPredicate(Predicate<V> predicate, String messageKey,
			String defaultMessageFormat, Supplier<Object[]> args,
			NullValidity nullValidity) {
		this.predicate = predicate;
		this.messageKey = messageKey;
		this.defaultMessageFormat = defaultMessageFormat;
		this.args = args;
		this.nullValidity = nullValidity;
	}

	public final Predicate<V> predicate() {
		return this.predicate;
	}

	public String messageKey() {
		return this.messageKey;
	}

	public final String defaultMessageFormat() {
		return this.defaultMessageFormat;
	}

	public Supplier<Object[]> args() {
		return this.args;
	}

	public final NullValidity nullValidity() {
		return this.nullValidity;
	}
}
