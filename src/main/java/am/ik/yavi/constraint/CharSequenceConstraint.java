package am.ik.yavi.constraint;

import java.net.MalformedURLException;
import java.net.URL;
import java.util.function.ToIntFunction;
import java.util.regex.Pattern;

import static am.ik.yavi.core.NullValidity.NULL_IS_INVALID;
import static am.ik.yavi.core.NullValidity.NULL_IS_VALID;

import am.ik.yavi.constraint.base.ContainerConstraintBase;
import am.ik.yavi.core.ConstraintHolder;

public class CharSequenceConstraint<T>
		extends ContainerConstraintBase<T, CharSequence, CharSequenceConstraint<T>> {
	public static final Pattern VALID_EMAIL_ADDRESS_REGEX = Pattern.compile(
			"^[A-Z0-9._%+-]+@[A-Z0-9.-]+\\.[A-Z]{2,6}$", Pattern.CASE_INSENSITIVE);

	@Override
	public CharSequenceConstraint<T> cast() {
		return this;
	}

	@Override
	protected ToIntFunction<CharSequence> size() {
		return CharSequence::length;
	}

	public CharSequenceConstraint<T> notBlank() {
		this.holders()
				.add(new ConstraintHolder<>(
						x -> x != null && x.toString().trim().length() != 0,
						"charSequence.notBlank", "\"{0}\" must not be blank",
						() -> new Object[] {}, NULL_IS_INVALID));
		return this;
	}

	public CharSequenceConstraint<T> contains(CharSequence s) {
		this.holders()
				.add(new ConstraintHolder<>(x -> x.toString().contains(s),
						"charSequence.contains", "\"{0}\" must contain {1}",
						() -> new Object[] { s }, NULL_IS_VALID));
		return this;
	}

	public CharSequenceConstraint<T> email() {
		this.holders()
				.add(new ConstraintHolder<>(
						x -> VALID_EMAIL_ADDRESS_REGEX.matcher(x).matches(),
						"charSequence.email", "\"{0}\" must be a valid email address",
						() -> new Object[] {}, NULL_IS_VALID));
		return this;
	}

	public CharSequenceConstraint<T> url() {
		this.holders().add(new ConstraintHolder<>(x -> {
			try {
				new URL(x.toString());
				return true;
			}
			catch (MalformedURLException e) {
				return false;
			}
		}, "charSequence.url", "\"{0}\" must be a valid URL", () -> new Object[] {},
				NULL_IS_VALID));
		return this;
	}

	public CharSequenceConstraint<T> pattern(String regex) {
		this.holders()
				.add(new ConstraintHolder<>(x -> Pattern.matches(regex, x),
						"charSequence.pattern", "\"{0}\" must match {1}",
						() -> new Object[] { regex }, NULL_IS_VALID));
		return this;
	}
}
