/*
 * SPDX-FileCopyrightText: 2018-2026 Toshiaki Maki <makingx@gmail.com>
 * SPDX-License-Identifier: Apache-2.0
 */
package am.ik.yavi.input;

import java.util.List;

import am.ik.yavi.core.ConstraintViolation;
import am.ik.yavi.core.PathSegment;
import am.ik.yavi.core.Validated;
import am.ik.yavi.core.ViolationMessage;
import org.jspecify.annotations.Nullable;

/**
 * How the violations of one property read to a person: what the property is called, and
 * what is said when it has no value or a value of another type. These are the violations
 * that no constraint of the property reports, so no {@code message(...)} of a constraint
 * reaches them.
 *
 * <p>
 * {@code label}, {@code required} and {@code typeMessage} of the builders returned by
 * {@code Yavi.schema()} create the messages of a property:
 *
 * <pre>{@code
 * Yavi.schema()
 *     ._string("email", c -> c.notBlank().email()).label("Email address")
 *     ._integer("ttl", c -> c.positive()).typeMessage("\"{0}\" must be a number of seconds")
 *     ._list("roles", Yavi._string()).required("Select at least one role")
 * }</pre>
 *
 * <p>
 * The label and the message of another type apply to the violations on the property
 * itself and on its elements ({@code roles} and {@code roles[1]}), which a form shows
 * with the field of the property. A violation of a nested property
 * ({@code address.street}) is left to that property.
 *
 * @param label what the property is called in a message instead of its name (see
 * {@link ConstraintViolation#labeled(String)}); {@code null} for the name
 * @param required the message of a property without a value, instead of the one of
 * {@code object.required}; {@code null} for that one
 * @param type the message of a value that does not represent the type of the property,
 * instead of the one of {@code object.type}; {@code null} for that one
 * @since 0.18.0
 */
public record PropertyMessages(@Nullable String label, @Nullable ViolationMessage required,
		@Nullable ViolationMessage type) {

	private static final PropertyMessages NONE = new PropertyMessages(null, null, null);

	/**
	 * Returns the messages of a property that is called by its name and reports the
	 * default messages.
	 * @return messages that change nothing
	 */
	public static PropertyMessages none() {
		return NONE;
	}

	/**
	 * Returns these messages with the given label.
	 * @param label what the property is called in a message
	 * @return new messages
	 */
	public PropertyMessages withLabel(String label) {
		return new PropertyMessages(label, this.required, this.type);
	}

	/**
	 * Returns these messages with the given message of a property without a value.
	 * @param required the message reported instead of the one of {@code object.required}
	 * @return new messages
	 */
	public PropertyMessages withRequired(ViolationMessage required) {
		return new PropertyMessages(this.label, required, this.type);
	}

	/**
	 * Returns these messages with the given message of a value of another type.
	 * @param type the message reported instead of the one of {@code object.type}
	 * @return new messages
	 */
	public PropertyMessages withType(ViolationMessage type) {
		return new PropertyMessages(this.label, this.required, type);
	}

	/**
	 * Returns the given result of validating the property with the given name as these
	 * messages state it: its violations on the property and on its elements are labeled
	 * unless they have a label, and those that report a missing value or a value of
	 * another type with the default message are given the message for it.
	 * @param name the name under which the violations of the property are located, which
	 * is empty for a value that is not a property
	 * @param validated the result of validating the property
	 * @param <T> the type of the validated value
	 * @return the result with the messages of the property
	 */
	public <T extends @Nullable Object> Validated<T> applyTo(String name, Validated<T> validated) {
		if (validated.isValid() || this == NONE) {
			return validated;
		}
		return Validated.of(validated.mapError(violation -> this.applyTo(name, violation)));
	}

	private ConstraintViolation applyTo(String name, ConstraintViolation violation) {
		final List<PathSegment> path = violation.path();
		final int start = name.isEmpty() ? 0 : 1;
		if (path.size() < start || (start == 1
				&& !(path.get(0) instanceof PathSegment.Property property && property.name().equals(name)))) {
			return violation;
		}
		for (int i = start; i < path.size(); i++) {
			if (!(path.get(i) instanceof PathSegment.Index)) {
				return violation;
			}
		}
		ConstraintViolation result = violation;
		if (this.required != null && path.size() == start
				&& isDefault(violation, ViolationMessage.Default.OBJECT_REQUIRED)) {
			result = result.withMessage(this.required);
		}
		else if (this.type != null && isDefault(violation, ViolationMessage.Default.OBJECT_TYPE)) {
			result = result.withMessage(this.type);
		}
		return this.label != null && !result.hasLabel() ? result.labeled(this.label) : result;
	}

	private static boolean isDefault(ConstraintViolation violation, ViolationMessage message) {
		return message.messageKey().equals(violation.messageKey())
				&& message.defaultMessageFormat().equals(violation.defaultMessageFormat());
	}

}
