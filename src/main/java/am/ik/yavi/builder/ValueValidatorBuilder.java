package am.ik.yavi.builder;

import java.util.function.Function;

import am.ik.yavi.core.ValueValidator;

/**
 * Builder for ValueValidator
 *
 * @param <A> argument type
 * @param <R> result type
 * @since 0.14.0
 */
public interface ValueValidatorBuilder<A, R> {
	ValueValidator<? super A, ? extends R> build();

	<T> ValueValidator<? super A, ? extends T> build(
			Function<? super R, ? extends T> mapper);
}
