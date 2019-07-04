package am.ik.yavi.core;

import java.util.Locale;
import java.util.function.Function;

public class NestedValidatorSubset<T, N> implements ValidatorSubset<T> {
	private final Function<T, N> nested;
	private final ValidatorSubset<N> validator;
	private final String prefix;

	public NestedValidatorSubset(Function<T, N> nested, ValidatorSubset<N> validator,
			String prefix) {
		this.nested = nested;
		this.validator = validator;
		this.prefix = prefix;
	}

	@Override
	public ConstraintViolations validate(T target, Locale locale) {
		final N n = this.nested.apply(target);
		return this.validator.validate(n, locale);
	}
}
