/*
 * SPDX-FileCopyrightText: 2018-2026 Toshiaki Maki <makingx@gmail.com>
 * SPDX-License-Identifier: Apache-2.0
 */
package am.ik.yavi.core;

import java.util.Objects;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.function.Supplier;

import org.jspecify.annotations.Nullable;

/**
 * The three states of a property of an input: it has a value ({@link Present}), it is
 * {@code null} ({@link PresentNull}) or the input does not have it ({@link Absent}).
 *
 * <p>
 * A partial update, such as a PATCH request (JSON Merge Patch), needs all three: an
 * absent property is left unchanged and a {@code null} one is cleared. An argument added
 * with {@code presence()} of the builders returned by {@code Yavi.arguments()} is read
 * from a map as a {@code Presence}:
 *
 * <pre>{@code
 * record ProfilePatch(Presence<String> nickname, Presence<String> bio) {}
 *
 * MapInputValidator<ProfilePatch> validator = Yavi.arguments()
 *     ._string("nickname", c -> c.lessThanOrEqual(50)).presence()
 *     ._string("bio", c -> c.lessThanOrEqual(500)).presence()
 *     .fromMap(ProfilePatch::new);
 *
 * patch.nickname().ifPresent(profile::setNickname); // not called if absent
 * String bio = patch.bio().orElse(profile.getBio()); // null if cleared
 * }</pre>
 *
 * The states are told apart with a {@code switch} too:
 *
 * <pre>{@code
 * switch (patch.bio()) {
 *     case Presence.Present<String>(String bio) -> profile.setBio(bio);
 *     case Presence.PresentNull<String> cleared -> profile.clearBio();
 *     case Presence.Absent<String> unchanged -> { }
 * }
 * }</pre>
 *
 * @param <T> the type of the value
 * @since 0.18.0
 * @see ValueValidator#liftPresence()
 */
public sealed interface Presence<T> {

	/**
	 * A property that has a value.
	 *
	 * @param value the value
	 * @param <T> the type of the value
	 */
	record Present<T> (T value) implements Presence<T> {

		/**
		 * Creates the state of a property that has the given value.
		 */
		public Present {
			Objects.requireNonNull(value, "'value' must not be null. Use Presence.ofNullable(value).");
		}
	}

	/**
	 * A property that is {@code null}.
	 *
	 * @param <T> the type of the value
	 */
	record PresentNull<T> () implements Presence<T> {

		private static final PresentNull<?> INSTANCE = new PresentNull<>();

	}

	/**
	 * A property that the input does not have.
	 *
	 * @param <T> the type of the value
	 */
	record Absent<T> () implements Presence<T> {

		private static final Absent<?> INSTANCE = new Absent<>();

	}

	/**
	 * Returns the state of a property that has the given value.
	 * @param value the value
	 * @param <T> the type of the value
	 * @return {@link Present}
	 */
	static <T> Presence<T> of(T value) {
		return new Present<>(value);
	}

	/**
	 * Returns the state of a property that the input has.
	 * @param value the value of the property
	 * @param <T> the type of the value
	 * @return {@link PresentNull} if the value is {@code null}, {@link Present} otherwise
	 */
	static <T> Presence<T> ofNullable(@Nullable T value) {
		return value == null ? presentNull() : new Present<>(value);
	}

	/**
	 * Returns the state of a property that is {@code null}.
	 * @param <T> the type of the value
	 * @return {@link PresentNull}
	 */
	@SuppressWarnings("unchecked")
	static <T> Presence<T> presentNull() {
		return (Presence<T>) PresentNull.INSTANCE;
	}

	/**
	 * Returns the state of a property that the input does not have.
	 * @param <T> the type of the value
	 * @return {@link Absent}
	 */
	@SuppressWarnings("unchecked")
	static <T> Presence<T> absent() {
		return (Presence<T>) Absent.INSTANCE;
	}

	/**
	 * Returns whether the input does not have the property.
	 * @return {@code true} for {@link Absent}
	 */
	default boolean isAbsent() {
		return this instanceof Absent;
	}

	/**
	 * Returns whether the input has the property, with a value or {@code null}.
	 * @return {@code true} for {@link Present} and {@link PresentNull}
	 */
	default boolean isPresent() {
		return !this.isAbsent();
	}

	/**
	 * Returns the value the property is given, or the given one if the input does not
	 * have the property. This merges a partial update into the current value.
	 * @param other the value returned for an absent property, such as the current value
	 * @return the value of {@link Present}, {@code null} for {@link PresentNull} and
	 * {@code other} for {@link Absent}
	 */
	default @Nullable T orElse(@Nullable T other) {
		return this.fold(() -> other, () -> null, value -> value);
	}

	/**
	 * Gives the value of the property to the given action if the input has the property.
	 * @param action the action called with the value of {@link Present} or with
	 * {@code null} for {@link PresentNull}; it is not called for {@link Absent}
	 */
	default void ifPresent(Consumer<? super @Nullable T> action) {
		if (this.isPresent()) {
			action.accept(this.orElse(null));
		}
	}

	/**
	 * Returns the state with the value mapped by the given function. {@link PresentNull}
	 * and {@link Absent} are kept.
	 * @param mapper the function applied to the value of {@link Present}; if it returns
	 * {@code null}, the result is {@link PresentNull}
	 * @param <U> the type of the mapped value
	 * @return the mapped state
	 */
	default <U> Presence<U> map(Function<? super T, ? extends @Nullable U> mapper) {
		return this.fold(Presence::absent, Presence::presentNull, value -> ofNullable(mapper.apply(value)));
	}

	/**
	 * Returns the result of the function of the state.
	 * @param absent called for {@link Absent}
	 * @param presentNull called for {@link PresentNull}
	 * @param present called with the value of {@link Present}
	 * @param <U> the type of the result
	 * @return the result of the called function
	 */
	default <U extends @Nullable Object> U fold(Supplier<? extends U> absent, Supplier<? extends U> presentNull,
			Function<? super T, ? extends U> present) {
		if (this instanceof Present<T>value) {
			return present.apply(value.value());
		}
		return this.isAbsent() ? absent.get() : presentNull.get();
	}

}
