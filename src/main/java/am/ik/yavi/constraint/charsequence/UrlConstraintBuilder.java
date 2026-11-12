/*
 * SPDX-FileCopyrightText: 2018-2026 Toshiaki Maki <makingx@gmail.com>
 * SPDX-License-Identifier: Apache-2.0
 */
package am.ik.yavi.constraint.charsequence;

import am.ik.yavi.core.ConstraintPredicate;
import am.ik.yavi.core.ViolationMessage;
import java.net.MalformedURLException;
import java.net.URI;
import java.net.URL;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

import org.jspecify.annotations.Nullable;
import java.util.function.Function;
import java.util.function.ToIntFunction;

import static am.ik.yavi.core.NullAs.VALID;
import static am.ik.yavi.core.ViolationMessage.Default.CHAR_SEQUENCE_URL;
import static am.ik.yavi.core.ViolationMessage.Default.CHAR_SEQUENCE_URL_HOST;
import static am.ik.yavi.core.ViolationMessage.Default.CHAR_SEQUENCE_URL_PORT;
import static am.ik.yavi.core.ViolationMessage.Default.CHAR_SEQUENCE_URL_PROTOCOL;

/**
 * Builds the constraints on a value that is expected to be a URL. The value is always
 * required to be parsable as a URL, and each of the protocol, the host and the port can
 * additionally be restricted to a set of expected values.
 *
 * <p>
 * The value is parsed with {@link java.net.URI} and converted to a {@link java.net.URL}.
 * An empty value is considered valid, and so is a value that is not parsable as far as
 * the protocol, the host and the port are concerned, so that a single unparsable value
 * reports one violation instead of one per restriction.
 * </p>
 *
 * <p>
 * This builder is used through
 * {@link am.ik.yavi.constraint.CharSequenceConstraint#url(Function)}.
 * </p>
 *
 * @param <E> the type of the character sequence the constraints apply to
 * @since 0.17.1
 */
public final class UrlConstraintBuilder<E extends CharSequence> {

	private final Set<String> hosts = new LinkedHashSet<>();

	private final Set<Integer> ports = new LinkedHashSet<>();

	private final Set<String> protocols = new LinkedHashSet<>();

	private final ToIntFunction<E> size;

	/**
	 * Creates the builder.
	 * @param size the function that returns the length of a value; an empty value is
	 * treated as valid
	 */
	public UrlConstraintBuilder(ToIntFunction<E> size) {
		this.size = size;
	}

	/**
	 * Restricts the host of the URL to the given values. The comparison is
	 * case-insensitive. Note that a host is matched as it is written, so that
	 * {@code host("example.com")} does not accept {@code https://www.example.com}.
	 *
	 * <p>
	 * A violation is reported with the message key {@code charSequence.url.host}, where
	 * {@code {1}} is the collection of the expected hosts.
	 * </p>
	 * @param hosts the accepted hosts
	 * @return this builder
	 */
	public UrlConstraintBuilder<E> host(String... hosts) {
		this.hosts.addAll(Arrays.asList(hosts));
		return this;
	}

	/**
	 * Restricts the port of the URL to the given values. A URL that does not specify a
	 * port is matched against the default port of its protocol, so that {@code port(443)}
	 * accepts {@code https://example.com}.
	 *
	 * <p>
	 * A violation is reported with the message key {@code charSequence.url.port}, where
	 * {@code {1}} is the collection of the expected ports.
	 * </p>
	 * @param ports the accepted ports
	 * @return this builder
	 */
	public UrlConstraintBuilder<E> port(int... ports) {
		for (int port : ports) {
			this.ports.add(port);
		}
		return this;
	}

	/**
	 * Restricts the protocol of the URL to the given values. The comparison is
	 * case-insensitive.
	 *
	 * <p>
	 * A violation is reported with the message key {@code charSequence.url.protocol},
	 * where {@code {1}} is the collection of the expected protocols.
	 * </p>
	 * @param protocols the accepted protocols
	 * @return this builder
	 */
	public UrlConstraintBuilder<E> protocol(String... protocols) {
		this.protocols.addAll(Arrays.asList(protocols));
		return this;
	}

	/**
	 * Returns the predicates of the configured constraints. The first one requires the
	 * value to be a parsable URL, with the message key {@code charSequence.url}; it is
	 * followed by one predicate for each restricted attribute.
	 * @return the constraint predicates
	 */
	public List<ConstraintPredicate<E>> build() {
		final List<ConstraintPredicate<E>> predicates = new ArrayList<>();
		predicates.add(ConstraintPredicate.of(x -> this.size.applyAsInt(x) == 0 || this.parse(x) != null,
				CHAR_SEQUENCE_URL, () -> new Object[] {}, VALID));
		if (!this.protocols.isEmpty()) {
			predicates.add(this.stringAttributeConstraintPredicate(URL::getProtocol, this.protocols,
					CHAR_SEQUENCE_URL_PROTOCOL));
		}
		if (!this.hosts.isEmpty()) {
			predicates.add(this.stringAttributeConstraintPredicate(URL::getHost, this.hosts, CHAR_SEQUENCE_URL_HOST));
		}
		if (!this.ports.isEmpty()) {
			predicates.add(this.portConstraintPredicate());
		}
		return predicates;
	}

	private ConstraintPredicate<E> stringAttributeConstraintPredicate(Function<URL, String> attribute,
			Set<String> expected, ViolationMessage message) {
		return ConstraintPredicate.of(x -> {
			final URL url = this.parse(x);
			if (url == null) {
				return true;
			}
			final String actual = attribute.apply(url);
			return expected.stream().anyMatch(e -> e.equalsIgnoreCase(actual));
		}, message, () -> new Object[] { expected }, VALID);
	}

	private ConstraintPredicate<E> portConstraintPredicate() {
		return ConstraintPredicate.of(x -> {
			final URL url = this.parse(x);
			if (url == null) {
				return true;
			}
			return this.ports.contains(url.getPort() == -1 ? url.getDefaultPort() : url.getPort());
		}, CHAR_SEQUENCE_URL_PORT, () -> new Object[] { this.ports }, VALID);
	}

	private @Nullable URL parse(E value) {
		if (this.size.applyAsInt(value) == 0) {
			return null;
		}
		try {
			return URI.create(value.toString()).toURL();
		}
		catch (IllegalArgumentException | MalformedURLException e) {
			return null;
		}
	}

}
