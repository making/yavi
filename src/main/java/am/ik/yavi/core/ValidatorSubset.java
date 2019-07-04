package am.ik.yavi.core;

import java.util.Locale;

public interface ValidatorSubset<T> {
	ConstraintViolations validate(T target, Locale locale);
}
