/*
 * SPDX-FileCopyrightText: 2018-2026 Toshiaki Maki <makingx@gmail.com>
 * SPDX-License-Identifier: Apache-2.0
 */
package am.ik.yavi.docgen;

import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import com.vladsch.flexmark.ast.FencedCodeBlock;
import com.vladsch.flexmark.html.AttributeProvider;
import com.vladsch.flexmark.html.AttributeProviderFactory;
import com.vladsch.flexmark.html.renderer.AttributablePart;
import com.vladsch.flexmark.html.renderer.LinkResolverContext;
import com.vladsch.flexmark.util.ast.Node;
import com.vladsch.flexmark.util.html.MutableAttributes;

/**
 * Rewrites rendered runnable code blocks into interactive cells.
 *
 * <p>
 * A fenced block whose info string is {@code java runnable} is the site's only runnable
 * form. flexmark renders its info string as the code element's classes, so the block
 * arrives as {@code <pre><code class="language-java runnable">...}. This transformer
 * replaces each such block with an editable {@code <textarea>} plus a Run button and an
 * output area. {@code docs.js} lazily loads the tree's runtime ({@code
 * assets/runtime/<version>/yavi-runtime.js}) on the first Run and hands the cell's source
 * to {@code window.YaviRuntime.run(source)}; the source is a complete compilation unit
 * (see doc-site/README.md for the contract).
 *
 * <p>
 * Every other block -- a plain {@code ```java}, {@code ```bash}, a console transcript --
 * stays static and gets a copy button from {@code docs.js}; a block of a language
 * {@link CodeHighlighter} knows is highlighted at build time.
 */
public final class RunnableBlockTransformer {

	// Every rendered fence, in page order. The marker may sit on the <pre> (the
	// attribute provider puts it there) or, defensively, on the <code>.
	private static final Pattern BLOCK = Pattern
		.compile("<pre(?: class=\"([^\"]*)\")?><code(?: class=\"([^\"]*)\")?>(.*?)</code></pre>", Pattern.DOTALL);

	private RunnableBlockTransformer() {
	}

	/**
	 * Marks a fenced block whose info string is exactly {@code java runnable}. flexmark
	 * renders only the first word of an info string as the language class, so without
	 * this provider the second word would be lost before {@link #transform(String)
	 * transform} sees the block.
	 */
	static AttributeProviderFactory marker() {
		return new AttributeProviderFactory() {
			@Override
			public Set<Class<?>> getAfterDependents() {
				return null;
			}

			@Override
			public Set<Class<?>> getBeforeDependents() {
				return null;
			}

			@Override
			public boolean affectsGlobalScope() {
				return false;
			}

			@Override
			public AttributeProvider apply(LinkResolverContext context) {
				return (Node node, AttributablePart part, MutableAttributes attributes) -> {
					if (node instanceof FencedCodeBlock fenced && "NODE".equals(part.getName())
							&& isRunnableFence(fenced.getInfo().toString())) {
						attributes.addValue("class", "runnable");
					}
				};
			}
		};
	}

	/** Whether a fence's info string is the {@code java runnable} form. */
	static boolean isRunnableFence(String info) {
		return info != null && info.trim().replaceAll("\\s+", " ").equals("java runnable");
	}

	/**
	 * Replaces runnable {@code java runnable} blocks in {@code html} with cells,
	 * highlights the static blocks of a language the highlighter knows, and leaves every
	 * other block untouched.
	 */
	public static String transform(String html) {
		Matcher matcher = BLOCK.matcher(html);
		StringBuilder out = new StringBuilder();
		while (matcher.find()) {
			String marker = matcher.group(1);
			String classes = matcher.group(2);
			String escapedCode = matcher.group(3);
			String language = language(classes);
			String replacement = matcher.group();
			if ((isRunnable(marker) || isRunnable(classes)) && "java".equals(language)) {
				replacement = runnableCell(escapedCode);
			}
			else if (CodeHighlighter.supports(language)) {
				replacement = "<pre" + classAttribute(marker) + "><code" + classAttribute(classes) + ">"
						+ CodeHighlighter.highlight(language, CodeHighlighter.unescape(escapedCode)) + "</code></pre>";
			}
			matcher.appendReplacement(out, Matcher.quoteReplacement(replacement));
		}
		matcher.appendTail(out);
		return out.toString();
	}

	private static String classAttribute(String classes) {
		return (classes == null) ? "" : " class=\"" + classes + "\"";
	}

	private static boolean isRunnable(String classes) {
		if (classes == null) {
			return false;
		}
		for (String token : classes.split("\\s+")) {
			if (token.equals("runnable")) {
				return true;
			}
		}
		return false;
	}

	private static String language(String classes) {
		if (classes == null) {
			return null;
		}
		for (String token : classes.split("\\s+")) {
			if (token.startsWith("language-")) {
				return token.substring("language-".length());
			}
		}
		return null;
	}

	private static String runnableCell(String escapedCode) {
		String code = stripTrailingNewline(escapedCode);
		int rows = Math.max(1, countLines(code));
		return """
				<div class="code-cell" data-lang="java">\
				<div class="cell-toolbar"><button class="run" type="button">Run</button>\
				<span class="cell-status"></span></div>\
				<textarea class="cell-src" spellcheck="false" wrap="off" rows="%d" aria-label="Editable Java source">%s</textarea>\
				<pre class="cell-out" hidden></pre>\
				</div>"""
			.formatted(rows, code);
	}

	private static String stripTrailingNewline(String s) {
		int end = s.length();
		while (end > 0 && (s.charAt(end - 1) == '\n' || s.charAt(end - 1) == '\r')) {
			end--;
		}
		return s.substring(0, end);
	}

	private static int countLines(String s) {
		if (s.isEmpty()) {
			return 1;
		}
		int lines = 1;
		for (int i = 0; i < s.length(); i++) {
			if (s.charAt(i) == '\n') {
				lines++;
			}
		}
		return lines;
	}

}
