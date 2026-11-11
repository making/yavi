/*
 * SPDX-FileCopyrightText: 2018-2026 Toshiaki Maki <makingx@gmail.com>
 * SPDX-License-Identifier: Apache-2.0
 */
package am.ik.yavi.constraint.inetaddress;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.regex.Pattern;

/**
 * Utilities that validate IPv4 and IPv6 address literals without resolving them.
 *
 * <p>
 * The methods back the {@link am.ik.yavi.constraint.CharSequenceConstraint#ipv4()} and
 * {@link am.ik.yavi.constraint.CharSequenceConstraint#ipv6()} constraints and can also be
 * called directly. They only inspect the textual form of the address; no name resolution
 * or route lookup is performed.
 * </p>
 *
 * @since 0.7.0
 */
public class InetAddressUtils {

	/**
	 * Regular expression that matches an IPv4 address of four decimal octets, each in the
	 * range 0 to 255. An octet has no leading zero unless it is the single digit
	 * {@code 0}.
	 */
	public static final String IPV4_REGEX = "^(?:(?:25[0-5]|2[0-4][0-9]|1[0-9][0-9]|[1-9]?[0-9])\\.){3}(?:25[0-5]|2[0-4][0-9]|1[0-9][0-9]|[1-9]?[0-9])$";

	private static final Pattern IPV4_PATTERN = Pattern.compile(IPV4_REGEX);

	private static final int MAX_BYTE = 128;

	private static final int MAX_UNSIGNED_SHORT = 0xffff;

	private static final int BASE_16 = 16;

	// Max number of hex groups (separated by :) in an IPV6 address
	private static final int IPV6_MAX_HEX_GROUPS = 8;

	// Max hex digits in each IPv6 group
	private static final int IPV6_MAX_HEX_DIGITS_PER_GROUP = 4;

	/**
	 * Creates an instance. The class has no state beyond the static patterns.
	 */
	public InetAddressUtils() {
	}

	/**
	 * Returns whether the given string is an IPv4 address in dotted decimal notation.
	 * @param s the string to check
	 * @return {@code true} if the whole string matches {@link #IPV4_REGEX}
	 */
	public static boolean isIpv4(String s) {
		return IPV4_PATTERN.matcher(s).matches();
	}

	/**
	 * Returns whether the given string is an IPv6 address.
	 *
	 * <p>
	 * The compressed form with {@code ::} is accepted, as is an embedded IPv4 address in
	 * the last group. A zone id may follow the address after {@code '%'} and a prefix
	 * length after {@code '/'}; the prefix length must be between 0 and 128.
	 * </p>
	 * @param s the string to check
	 * @return {@code true} if the string is a valid IPv6 address
	 * @see <a href=
	 * "https://github.com/apache/commons-validator/blob/master/src/main/java/org/apache/commons/validator/routines/InetAddressValidator.java">Apache
	 * Commons Validator</a>
	 */
	public static boolean isIpv6(String s) {
		// remove prefix size. This will appear after the zone id (if any)
		final String[] parts = s.split("/", -1);
		if (parts.length > 2) {
			return false; // can only have one prefix specifier
		}
		if (parts.length == 2) {
			if (!parts[1].matches("\\d{1,3}")) {
				return false; // not a valid number
			}
			final int bits = Integer.parseInt(parts[1]); // cannot fail because of RE
															// check
			if (bits < 0 || bits > MAX_BYTE) {
				return false; // out of range
			}
		}
		// remove zone-id
		final String[] partsZoneIdRemoved = parts[0].split("%", -1);
		if (partsZoneIdRemoved.length > 2) {
			return false;
		}
		// The id syntax is implementation independent, but it presumably cannot allow:
		// whitespace, '/' or '%'
		if ((partsZoneIdRemoved.length == 2) && !partsZoneIdRemoved[1].matches("[^\\s/%]+")) {
			return false; // invalid id
		}

		final String firstPart = partsZoneIdRemoved[0];
		final boolean containsCompressedZeroes = firstPart.contains("::");
		if (containsCompressedZeroes && (firstPart.indexOf("::") != firstPart.lastIndexOf("::"))) {
			return false;
		}
		if ((firstPart.startsWith(":") && !firstPart.startsWith("::"))
				|| (firstPart.endsWith(":") && !firstPart.endsWith("::"))) {
			return false;
		}
		String[] octets = firstPart.split(":");
		if (containsCompressedZeroes) {
			final List<String> octetList = new ArrayList<>(Arrays.asList(octets));
			if (firstPart.endsWith("::")) {
				// String.split() drops ending empty segments
				octetList.add("");
			}
			else if (firstPart.startsWith("::") && !octetList.isEmpty()) {
				octetList.remove(0);
			}
			octets = octetList.toArray(new String[0]);
		}
		if (octets.length > IPV6_MAX_HEX_GROUPS) {
			return false;
		}
		int validOctets = 0;
		int emptyOctets = 0; // consecutive empty chunks
		for (int index = 0; index < octets.length; index++) {
			String octet = octets[index];
			if (octet.isEmpty()) {
				emptyOctets++;
				if (emptyOctets > 1) {
					return false;
				}
			}
			else {
				emptyOctets = 0;
				// Is last chunk an IPv4 address?
				if (index == octets.length - 1 && octet.contains(".")) {
					if (!isIpv4(octet)) {
						return false;
					}
					validOctets += 2;
					continue;
				}
				if (octet.length() > IPV6_MAX_HEX_DIGITS_PER_GROUP) {
					return false;
				}
				int octetInt = 0;
				try {
					octetInt = Integer.parseInt(octet, BASE_16);
				}
				catch (NumberFormatException e) {
					return false;
				}
				if (octetInt < 0 || octetInt > MAX_UNSIGNED_SHORT) {
					return false;
				}
			}
			validOctets++;
		}
		if (validOctets > IPV6_MAX_HEX_GROUPS || (validOctets < IPV6_MAX_HEX_GROUPS && !containsCompressedZeroes)) {
			return false;
		}
		return true;
	}

}
