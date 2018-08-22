package am.ik.yavi.core;

import java.util.function.Predicate;

public interface CustomConstraint<V> {
	Predicate<V> predicate();

	String messageKey();

	String defaultMessageFormat();
}
