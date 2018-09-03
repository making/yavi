/*
 * Copyright 2008 Les Hazlewood
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package am.ik.yavi.constraint.charsequence;

import java.util.regex.Pattern;

import am.ik.yavi.jsr305.Nullable;

/**
 * An email address represents the textual string of an RFC 2822 email address and other
 * corresponding information of interest.
 *
 * If you use this code, please keep the author information in tact and reference my site
 * at leshazlewood.com. Thanks!
 *
 * @author Les Hazlewood
 */
public final class Email {
	/**
	 * This constant states that domain literals are allowed in the email address, e.g.:
	 * someone@[192.168.1.100] or john.doe@[23:33:A2:22:16:1F] or me@[my computer]
	 * <p/>
	 * The RFC says these are valid email addresses, but most people don't like allowing
	 * them.
	 * <p/>
	 * If you don't want to allow them, and only want to allow valid domain names (RFC
	 * 1035, x.y.z.com, etc), change this constant to false.
	 * <p/>
	 * Its default value is true to remain RFC 2822 compliant, but you should set it
	 * depending on what you need for your application.
	 */

	private static final boolean ALLOW_DOMAIN_LITERALS = true;

	/**
	 * This contstant states that quoted identifiers are allowed (using quotes and angle
	 * brackets around the raw address) are allowed, e.g.: "John Smith"
	 * <john.smith@somewhere.com> The RFC says this is a valid mailbox. If you don't want
	 * to allow this, because for example, you only want users to enter in a raw address
	 * (john.smith@somewhere.com - no quotes or angle brackets), then change this constant
	 * to false. Its default value is true to remain RFC 2822 compliant, but you should
	 * set it depending on what you need for your application.
	 */

	private static final boolean ALLOW_QUOTED_IDENTIFIERS = true;
	// RFC 2822 2.2.2 Structured Header Field Bodies
	private static final String wsp = "[ \\t]"; // space or tab
	private static final String fwsp = wsp + "*";

	// RFC 2822 3.2.1 Primitive tokens
	private static final String dquote = "\\\"";
	// ASCII Control characters excluding white space:
	private static final String noWsCtl = "\\x01-\\x08\\x0B\\x0C\\x0E-\\x1F\\x7F";
	// all ASCII characters except CR and LF:
	private static final String asciiText = "[\\x01-\\x09\\x0B\\x0C\\x0E-\\x7F]";

	// RFC 2822 3.2.2 Quoted characters:
	// single backslash followed by a text char
	private static final String quotedPair = "(\\\\" + asciiText + ")";

	// RFC 2822 3.2.4 Atom:
	private static final String atext = "[a-zA-Z0-9\\!\\#\\$\\%\\&\\'\\*\\+\\-\\/\\=\\?\\^\\_\\`\\{\\|\\}\\~]";
	private static final String atom = fwsp + atext + "+" + fwsp;
	private static final String dotAtomText = atext + "+" + "(" + "\\." + atext + "+)*";
	private static final String dotAtom = fwsp + "(" + dotAtomText + ")" + fwsp;

	// RFC 2822 3.2.5 Quoted strings:
	// noWsCtl and the rest of ASCII except the doublequote and backslash characters:
	private static final String qtext = "[" + noWsCtl + "\\x21\\x23-\\x5B\\x5D-\\x7E]";
	private static final String qcontent = "(" + qtext + "|" + quotedPair + ")";
	private static final String quotedString = dquote + "(" + fwsp + qcontent + ")*"
			+ fwsp + dquote;

	// RFC 2822 3.2.6 Miscellaneous tokens
	private static final String word = "((" + atom + ")|(" + quotedString + "))";
	private static final String phrase = word + "+"; // one or more words.

	// RFC 1035 tokens for domain names:
	private static final String letter = "[a-zA-Z]";
	private static final String letDig = "[a-zA-Z0-9]";
	private static final String letDigHyp = "[a-zA-Z0-9-]";
	private static final String rfcLabel = letDig + "(" + letDigHyp + "{0,61}" + letDig
			+ ")?";
	private static final String rfc1035DomainName = rfcLabel + "(\\." + rfcLabel + ")*\\."
			+ letter + "{2,6}";

	// RFC 2822 3.4 Address specification
	// domain text - non white space controls and the rest of ASCII chars not including [,
	// ], or \:
	private static final String dtext = "[" + noWsCtl + "\\x21-\\x5A\\x5E-\\x7E]";
	private static final String dcontent = dtext + "|" + quotedPair;
	private static final String domainLiteral = "\\[" + "(" + fwsp + dcontent + "+)*"
			+ fwsp + "\\]";
	private static final String rfc2822Domain = "(" + dotAtom + "|" + domainLiteral + ")";

	private static final String domain = ALLOW_DOMAIN_LITERALS ? rfc2822Domain
			: rfc1035DomainName;

	private static final String localPart = "((" + dotAtom + ")|(" + quotedString + "))";
	private static final String addrSpec = localPart + "@" + domain;
	private static final String angleAddr = "<" + addrSpec + ">";
	private static final String nameAddr = "(" + phrase + ")?" + fwsp + angleAddr;
	private static final String mailbox = nameAddr + "|" + addrSpec;

	// now compile a pattern for efficient re-use:
	// if we're allowing quoted identifiers or not:
	private static final String patternString = ALLOW_QUOTED_IDENTIFIERS ? mailbox
			: addrSpec;
	private static final Pattern VALID_PATTERN = Pattern.compile(patternString);

	/**
	 * Utility method that checks to see if the specified string is a valid email address
	 * according to the * RFC 2822 specification.
	 *
	 * @param email the email address string to test for validity.
	 * @return true if the given text valid according to RFC 2822, false otherwise.
	 */
	public static boolean isValid(@Nullable CharSequence email) {
		return (email != null) && VALID_PATTERN.matcher(email).matches();
	}
}
