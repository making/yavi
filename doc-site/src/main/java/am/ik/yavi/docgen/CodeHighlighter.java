/*
 * SPDX-FileCopyrightText: 2018-2026 Toshiaki Maki <makingx@gmail.com>
 * SPDX-License-Identifier: Apache-2.0
 */
package am.ik.yavi.docgen;

import java.util.Locale;
import java.util.Set;

/**
 * Build-time syntax highlighting for the static code blocks of the documentation.
 *
 * <p>
 * The site is generated offline and must stay offline when it is viewed, so there is no
 * client-side highlighter and no network request: the generator tokenizes the source and
 * emits {@code <span class="tok-...">} elements into the rendered page. {@code docs.css}
 * maps those classes to the YAVI palette for the light and dark themes.
 *
 * <p>
 * Only the languages the documentation actually uses are tokenized -- Java, Kotlin, XML,
 * JSON, properties and shell -- plus plain text, which is left as it is. A fence language
 * without a tokenizer is also left as it is, so an unknown language degrades to
 * monochrome instead of failing the build.
 *
 * <p>
 * The input is the HTML-escaped code flexmark rendered; {@link #unescape(String)} turns
 * it back into source before tokenizing, and every token is escaped again on the way out.
 * {@code textContent} of the code element is then exactly the source (the copy button
 * relies on that), and the search index is built from the body before this runs, so
 * highlighting never reaches the index.
 */
final class CodeHighlighter {

	private static final Set<String> JAVA_KEYWORDS = Set.of("abstract", "assert", "boolean", "break", "byte", "case",
			"catch", "char", "class", "const", "continue", "default", "do", "double", "else", "enum", "extends",
			"final", "finally", "float", "for", "goto", "if", "implements", "import", "instanceof", "int", "interface",
			"long", "native", "new", "package", "private", "protected", "public", "return", "short", "static",
			"strictfp", "super", "switch", "synchronized", "this", "throw", "throws", "transient", "try", "void",
			"volatile", "while", "var", "yield", "record", "sealed", "permits", "module", "requires", "exports",
			"opens", "uses", "provides", "transitive", "to", "with");

	private static final Set<String> KOTLIN_KEYWORDS = Set.of("as", "break", "class", "continue", "do", "else", "for",
			"fun", "if", "in", "interface", "is", "object", "package", "return", "super", "this", "throw", "try",
			"typealias", "typeof", "val", "var", "when", "while", "by", "catch", "constructor", "delegate", "dynamic",
			"field", "file", "finally", "get", "import", "init", "param", "property", "receiver", "set", "setparam",
			"where", "actual", "abstract", "annotation", "companion", "const", "crossinline", "data", "enum", "expect",
			"external", "final", "infix", "inline", "inner", "internal", "lateinit", "noinline", "open", "operator",
			"out", "override", "private", "protected", "public", "reified", "sealed", "suspend", "tailrec", "vararg");

	private static final Set<String> SHELL_KEYWORDS = Set.of("if", "then", "else", "elif", "fi", "for", "while",
			"until", "do", "done", "case", "esac", "in", "function", "select", "time", "coproc", "export", "local",
			"readonly", "declare", "unset", "shift", "return", "exit", "set", "source", "alias", "echo", "printf",
			"read", "test");

	private static final Set<String> LITERALS = Set.of("true", "false", "null", "undefined", "nil");

	private CodeHighlighter() {
	}

	/** Whether the fence language has a tokenizer. */
	static boolean supports(String language) {
		String languageId = normalize(language);
		return languageId != null && switch (languageId) {
			case "java", "kotlin", "xml", "json", "properties", "bash" -> true;
			default -> false;
		};
	}

	/**
	 * Highlights {@code source} for {@code language} and returns HTML-escaped markup.
	 */
	static String highlight(String language, String source) {
		return switch (normalize(language)) {
			case "java" -> javaLike(source, JAVA_KEYWORDS, false);
			case "kotlin" -> javaLike(source, KOTLIN_KEYWORDS, true);
			case "xml" -> xml(source);
			case "json" -> json(source);
			case "properties" -> properties(source);
			case "bash" -> shell(source);
			default -> {
				StringBuilder out = new StringBuilder(source.length());
				escape(source, 0, source.length(), out);
				yield out.toString();
			}
		};
	}

	private static String normalize(String language) {
		if (language == null) {
			return null;
		}
		return switch (language.toLowerCase(Locale.ROOT)) {
			case "kt", "kts" -> "kotlin";
			case "html", "xhtml", "svg" -> "xml";
			case "sh", "shell", "zsh", "console", "terminal" -> "bash";
			case "ini", "conf" -> "properties";
			default -> language.toLowerCase(Locale.ROOT);
		};
	}

	// ------------------------------------------------------------------
	// Java / Kotlin
	// ------------------------------------------------------------------

	private static String javaLike(String source, Set<String> keywords, boolean kotlin) {
		StringBuilder out = new StringBuilder(source.length() + 256);
		int i = 0;
		int n = source.length();
		while (i < n) {
			char c = source.charAt(i);
			if (c == '/' && i + 1 < n && source.charAt(i + 1) == '/') {
				int end = source.indexOf('\n', i);
				end = (end < 0) ? n : end;
				span(out, "tok-com", source, i, end);
				i = end;
			}
			else if (c == '/' && i + 1 < n && source.charAt(i + 1) == '*') {
				int end = blockCommentEnd(source, i, kotlin);
				span(out, "tok-com", source, i, end);
				i = end;
			}
			else if (source.startsWith("\"\"\"", i)) {
				int end = source.indexOf("\"\"\"", i + 3);
				end = (end < 0) ? n : end + 3;
				span(out, "tok-str", source, i, end);
				i = end;
			}
			else if (c == '"' || c == '\'') {
				int end = stringEnd(source, i, c);
				span(out, "tok-str", source, i, end);
				i = end;
			}
			else if (c == '@' && i + 1 < n && isIdentifierStart(source.charAt(i + 1))) {
				int end = i + 1;
				while (end < n && isIdentifierPart(source.charAt(end))) {
					end++;
				}
				span(out, "tok-ann", source, i, end);
				i = end;
			}
			else if (isDigit(c) || (c == '.' && i + 1 < n && isDigit(source.charAt(i + 1)))) {
				int end = numberEnd(source, i);
				span(out, "tok-num", source, i, end);
				i = end;
			}
			else if (isIdentifierStart(c)) {
				int end = i + 1;
				while (end < n && isIdentifierPart(source.charAt(end))) {
					end++;
				}
				String word = source.substring(i, end);
				if (keywords.contains(word)) {
					span(out, "tok-key", source, i, end);
				}
				else if (LITERALS.contains(word)) {
					span(out, "tok-lit", source, i, end);
				}
				else if (startsWithCallParen(source, end)) {
					span(out, "tok-fn", source, i, end);
				}
				else if (Character.isUpperCase(word.charAt(0))) {
					span(out, "tok-type", source, i, end);
				}
				else {
					escape(source, i, end, out);
				}
				i = end;
			}
			else {
				escape(source, i, i + 1, out);
				i++;
			}
		}
		return out.toString();
	}

	private static boolean startsWithCallParen(String source, int from) {
		int i = from;
		while (i < source.length() && Character.isWhitespace(source.charAt(i))) {
			i++;
		}
		return i < source.length() && source.charAt(i) == '(';
	}

	private static int blockCommentEnd(String source, int start, boolean nested) {
		int depth = 1;
		int i = start + 2;
		while (i < source.length()) {
			if (nested && source.startsWith("/*", i)) {
				depth++;
				i += 2;
			}
			else if (source.startsWith("*/", i)) {
				depth--;
				i += 2;
				if (depth == 0) {
					return i;
				}
			}
			else {
				i++;
			}
		}
		return source.length();
	}

	private static int stringEnd(String source, int start, char quote) {
		int i = start + 1;
		while (i < source.length()) {
			char c = source.charAt(i);
			if (c == '\\') {
				i += 2;
			}
			else if (c == quote) {
				return i + 1;
			}
			else if (c == '\n' && quote == '\'') {
				return i;
			}
			else {
				i++;
			}
		}
		return source.length();
	}

	private static int numberEnd(String source, int start) {
		// The caller only starts a number on a digit, a '.', or a leading '-', so the
		// first character always belongs to the token.
		int i = start + 1;
		while (i < source.length()) {
			char c = source.charAt(i);
			if (Character.isLetterOrDigit(c) || c == '_' || c == '.') {
				i++;
			}
			else {
				break;
			}
		}
		return i;
	}

	// ------------------------------------------------------------------
	// XML / HTML
	// ------------------------------------------------------------------

	private static String xml(String source) {
		StringBuilder out = new StringBuilder(source.length() + 256);
		int i = 0;
		int n = source.length();
		while (i < n) {
			if (source.startsWith("<!--", i)) {
				int end = source.indexOf("-->", i + 4);
				end = (end < 0) ? n : end + 3;
				span(out, "tok-com", source, i, end);
				i = end;
			}
			else if (source.startsWith("<![CDATA[", i)) {
				int end = source.indexOf("]]>", i + 9);
				end = (end < 0) ? n : end + 3;
				span(out, "tok-str", source, i, end);
				i = end;
			}
			else if (source.startsWith("<?", i)) {
				int end = source.indexOf("?>", i + 2);
				end = (end < 0) ? n : end + 2;
				span(out, "tok-tag", source, i, end);
				i = end;
			}
			else if (source.charAt(i) == '<') {
				i = xmlTag(source, i, out);
			}
			else {
				escape(source, i, i + 1, out);
				i++;
			}
		}
		return out.toString();
	}

	private static int xmlTag(String source, int start, StringBuilder out) {
		int n = source.length();
		int i = start + 1;
		escape(source, start, i, out);
		if (i < n && source.charAt(i) == '/') {
			escape(source, i, i + 1, out);
			i++;
		}
		int nameStart = i;
		while (i < n && isXmlName(source.charAt(i))) {
			i++;
		}
		if (i == nameStart) {
			return i;
		}
		span(out, "tok-tag", source, nameStart, i);
		while (i < n && source.charAt(i) != '>') {
			char c = source.charAt(i);
			if (c == '"' || c == '\'') {
				int end = stringEnd(source, i, c);
				span(out, "tok-str", source, i, end);
				i = end;
			}
			else if (isXmlNameStart(c)) {
				int attrStart = i;
				while (i < n && isXmlName(source.charAt(i))) {
					i++;
				}
				span(out, "tok-attr", source, attrStart, i);
			}
			else {
				escape(source, i, i + 1, out);
				i++;
			}
		}
		if (i < n) {
			escape(source, i, i + 1, out);
			i++;
		}
		return i;
	}

	private static boolean isXmlNameStart(char c) {
		return Character.isLetter(c) || c == '_' || c == ':';
	}

	private static boolean isXmlName(char c) {
		return Character.isLetterOrDigit(c) || c == '_' || c == ':' || c == '-' || c == '.';
	}

	// ------------------------------------------------------------------
	// JSON
	// ------------------------------------------------------------------

	private static String json(String source) {
		StringBuilder out = new StringBuilder(source.length() + 256);
		int i = 0;
		int n = source.length();
		while (i < n) {
			char c = source.charAt(i);
			if (c == '"') {
				int end = stringEnd(source, i, '"');
				int next = end;
				while (next < n && Character.isWhitespace(source.charAt(next))) {
					next++;
				}
				span(out, (next < n && source.charAt(next) == ':') ? "tok-key" : "tok-str", source, i, end);
				i = end;
			}
			else if (c == '-' || isDigit(c)) {
				int end = numberEnd(source, i);
				span(out, "tok-num", source, i, end);
				i = end;
			}
			else if (isIdentifierStart(c)) {
				int end = i + 1;
				while (end < n && isIdentifierPart(source.charAt(end))) {
					end++;
				}
				String word = source.substring(i, end);
				if (LITERALS.contains(word)) {
					span(out, "tok-lit", source, i, end);
				}
				else {
					escape(source, i, end, out);
				}
				i = end;
			}
			else {
				escape(source, i, i + 1, out);
				i++;
			}
		}
		return out.toString();
	}

	// ------------------------------------------------------------------
	// Properties
	// ------------------------------------------------------------------

	private static String properties(String source) {
		StringBuilder out = new StringBuilder(source.length() + 256);
		int i = 0;
		int n = source.length();
		while (i < n) {
			int end = source.indexOf('\n', i);
			end = (end < 0) ? n : end;
			int first = i;
			while (first < end && Character.isWhitespace(source.charAt(first))) {
				first++;
			}
			if (first < end && (source.charAt(first) == '#' || source.charAt(first) == '!')) {
				span(out, "tok-com", source, i, end);
				i = end;
				continue;
			}
			int separator = first;
			while (separator < end && !isPropertySeparator(source.charAt(separator))
					&& !Character.isWhitespace(source.charAt(separator))) {
				separator++;
			}
			if (separator == first) {
				escape(source, i, end, out);
			}
			else {
				escape(source, i, first, out);
				span(out, "tok-key", source, first, separator);
				int value = separator;
				while (value < end && (isPropertySeparator(source.charAt(value))
						|| Character.isWhitespace(source.charAt(value)))) {
					value++;
				}
				escape(source, separator, value, out);
				if (value < end) {
					span(out, "tok-str", source, value, end);
				}
			}
			i = end;
			if (i < n) {
				escape(source, i, i + 1, out);
				i++;
			}
		}
		return out.toString();
	}

	private static boolean isPropertySeparator(char c) {
		return c == '=' || c == ':';
	}

	// ------------------------------------------------------------------
	// Shell
	// ------------------------------------------------------------------

	private static String shell(String source) {
		StringBuilder out = new StringBuilder(source.length() + 256);
		int i = 0;
		int n = source.length();
		while (i < n) {
			char c = source.charAt(i);
			if (c == '#' && (i == 0 || Character.isWhitespace(source.charAt(i - 1)))) {
				int end = source.indexOf('\n', i);
				end = (end < 0) ? n : end;
				span(out, "tok-com", source, i, end);
				i = end;
			}
			else if (c == '"' || c == '\'') {
				int end = stringEnd(source, i, c);
				span(out, "tok-str", source, i, end);
				i = end;
			}
			else if (c == '$' && i + 1 < n) {
				int end = i + 1;
				if (source.charAt(end) == '{') {
					int close = source.indexOf('}', end);
					end = (close < 0) ? n : close + 1;
				}
				else {
					while (end < n && isIdentifierPart(source.charAt(end))) {
						end++;
					}
				}
				span(out, "tok-var", source, i, end);
				i = end;
			}
			else if (isDigit(c)) {
				int end = i + 1;
				while (end < n && (Character.isLetterOrDigit(source.charAt(end)) || source.charAt(end) == '.')) {
					end++;
				}
				span(out, "tok-num", source, i, end);
				i = end;
			}
			else if (isIdentifierStart(c)) {
				int end = i + 1;
				while (end < n && isIdentifierPart(source.charAt(end))) {
					end++;
				}
				String word = source.substring(i, end);
				if (SHELL_KEYWORDS.contains(word)) {
					span(out, "tok-key", source, i, end);
				}
				else {
					escape(source, i, end, out);
				}
				i = end;
			}
			else {
				escape(source, i, i + 1, out);
				i++;
			}
		}
		return out.toString();
	}

	// ------------------------------------------------------------------
	// Shared helpers
	// ------------------------------------------------------------------

	private static boolean isIdentifierStart(char c) {
		return Character.isLetter(c) || c == '_' || c == '$';
	}

	private static boolean isIdentifierPart(char c) {
		return Character.isLetterOrDigit(c) || c == '_' || c == '$';
	}

	private static boolean isDigit(char c) {
		return c >= '0' && c <= '9';
	}

	private static void span(StringBuilder out, String className, String source, int start, int end) {
		if (start >= end) {
			return;
		}
		out.append("<span class=\"").append(className).append("\">");
		escape(source, start, end, out);
		out.append("</span>");
	}

	private static void escape(String source, int start, int end, StringBuilder out) {
		for (int i = start; i < end; i++) {
			char c = source.charAt(i);
			switch (c) {
				case '&' -> out.append("&amp;");
				case '<' -> out.append("&lt;");
				case '>' -> out.append("&gt;");
				case '"' -> out.append("&quot;");
				default -> out.append(c);
			}
		}
	}

	/**
	 * Decodes the entities flexmark emits in the code of a fenced block, so the tokenizer
	 * sees the source text rather than its escaped form.
	 */
	static String unescape(String html) {
		if (html.indexOf('&') < 0) {
			return html;
		}
		StringBuilder out = new StringBuilder(html.length());
		int i = 0;
		while (i < html.length()) {
			char c = html.charAt(i);
			if (c != '&') {
				out.append(c);
				i++;
				continue;
			}
			int semicolon = html.indexOf(';', i + 1);
			if (semicolon < 0 || semicolon - i > 10) {
				out.append(c);
				i++;
				continue;
			}
			String entity = html.substring(i + 1, semicolon);
			String decoded = switch (entity) {
				case "amp" -> "&";
				case "lt" -> "<";
				case "gt" -> ">";
				case "quot" -> "\"";
				case "apos", "#39" -> "'";
				case "nbsp" -> " ";
				default -> decodeNumeric(entity);
			};
			if (decoded == null) {
				out.append(c);
				i++;
			}
			else {
				out.append(decoded);
				i = semicolon + 1;
			}
		}
		return out.toString();
	}

	private static String decodeNumeric(String entity) {
		try {
			if (entity.startsWith("#x") || entity.startsWith("#X")) {
				return String.valueOf(Character.toChars(Integer.parseInt(entity.substring(2), 16)));
			}
			if (entity.startsWith("#")) {
				return String.valueOf(Character.toChars(Integer.parseInt(entity.substring(1))));
			}
		}
		catch (IllegalArgumentException ex) {
			return null;
		}
		return null;
	}

}
