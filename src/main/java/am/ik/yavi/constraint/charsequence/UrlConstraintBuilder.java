/*
 * Copyright (C) 2018-2025 Toshiaki Maki <makingx@gmail.com>
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *         http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
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
 * <p>
 * An empty value is considered valid, and so is a value that is not parsable as far as
 * the protocol, the host and the port are concerned, so that a single unparsable value
 * reports one violation instead of one per restriction.
 *
 * @since 0.17.1
 */
public final class UrlConstraintBuilder<E extends CharSequence> {

	private final Set<String> hosts = new LinkedHashSet<>();

	private final Set<Integer> ports = new LinkedHashSet<>();

	private final Set<String> protocols = new LinkedHashSet<>();

	private final ToIntFunction<E> size;

	public UrlConstraintBuilder(ToIntFunction<E> size) {
		this.size = size;
	}

	/**
	 * Restricts the host of the URL to the given values. The comparison is
	 * case-insensitive. Note that a host is matched as it is written, so that
	 * {@code host("example.com")} does not accept {@code https://www.example.com}.
	 */
	public UrlConstraintBuilder<E> host(String... hosts) {
		this.hosts.addAll(Arrays.asList(hosts));
		return this;
	}

	/**
	 * Restricts the port of the URL to the given values. A URL that does not specify a
	 * port is matched against the default port of its protocol, so that {@code port(443)}
	 * accepts {@code https://example.com}.
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
	 */
	public UrlConstraintBuilder<E> protocol(String... protocols) {
		this.protocols.addAll(Arrays.asList(protocols));
		return this;
	}

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

	private URL parse(E value) {
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
