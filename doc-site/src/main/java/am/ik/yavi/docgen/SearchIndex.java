/*
 * SPDX-FileCopyrightText: 2018-2026 Toshiaki Maki <makingx@gmail.com>
 * SPDX-License-Identifier: Apache-2.0
 */
package am.ik.yavi.docgen;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Builds the client-side search index of one version/language tree.
 *
 * <p>
 * The site is static, so the search runs in the browser and the index is written beside
 * the pages it describes. It has two tiers so that a page view stays free:
 *
 * <ul>
 * <li>{@code search-index.json} -- page paths, titles and every H1-H3 heading, prefetched
 * when the browser goes idle so the common "I know the name, take me there" query answers
 * without a network round trip.
 * <li>{@code search-body.json} -- the section bodies keyed to the same pages and
 * headings, fetched on the first keystroke.
 * </ul>
 *
 * <p>
 * Matching is plain substring, so nothing here tokenizes: a substring needs no segmenter,
 * which is what the Japanese tree wants, and in English it covers most of what stemming
 * would. Each tree gets its own pair of files and a search never crosses trees.
 *
 * <p>
 * Both files are keyed by POSITION -- a page is an index into {@code pages}, a section an
 * index into that page's headings -- and every language tree renders the same documents
 * in the same order with the same anchors (the build enforces this), so the trees' files
 * are structurally identical and only their strings are translated.
 *
 * <p>
 * A page is indexed per SECTION, so a hit links to {@code page.html#anchor} rather than
 * to the top of the page. The anchors are the ones the build has already settled, so a
 * translated page's index entries carry the reference language's anchors, exactly like
 * its links.
 */
public final class SearchIndex {

	/** The index format version, so a cached {@code docs.js} can reject a stale file. */
	static final int VERSION = 1;

	/** A level-1 to level-3 heading with its generated id; the label may hold markup. */
	private static final Pattern HEADING = Pattern.compile("<h([1-3])\\b[^>]*\\bid=\"([^\"]*)\"[^>]*>(.*?)</h\\1>",
			Pattern.DOTALL);

	private static final Pattern TAG = Pattern.compile("<[^>]+>");

	/** Tags that sit INSIDE a word; removing them must not insert a space. */
	private static final Pattern INLINE_TAG = Pattern
		.compile("</?(?:code|em|strong|a|span|sup|sub|kbd|b|i|del|s|br)\\b[^>]*>", Pattern.CASE_INSENSITIVE);

	/** One heading of a page: the anchor to link to and its (plain) label. */
	record Heading(String anchor, String text) {
	}

	/**
	 * One section body: the index of the heading it sits under, {@code -1} for the lead.
	 */
	private record Section(int headingIndex, String text) {
	}

	private record Page(String path, String title, List<Heading> headings, List<Section> sections) {
	}

	private final String lang;

	private final List<Page> pages = new ArrayList<>();

	public SearchIndex(String lang) {
		this.lang = lang;
	}

	/**
	 * Indexes one rendered page.
	 * @param path the page's HTML path relative to the language root (e.g.
	 * {@code getting-started.html})
	 * @param title the page title as the sidebar and the results list show it
	 * @param bodyHtml the rendered body, BEFORE the runnable cells are built -- the cell
	 * chrome is markup nobody searches for, while the code inside it is searchable
	 */
	public void addPage(String path, String title, String bodyHtml) {
		List<Heading> headings = new ArrayList<>();
		List<Section> sections = new ArrayList<>();
		Matcher matcher = HEADING.matcher(bodyHtml);
		int bodyStart = 0;
		int headingIndex = -1;
		while (matcher.find()) {
			addSection(sections, headingIndex, bodyHtml.substring(bodyStart, matcher.start()));
			// EVERY heading is recorded, the <h1> that repeats the page title included:
			// the index shape is then a function of the document structure alone, which
			// the trees share, and not of the nav titles, which they translate.
			headings.add(new Heading(matcher.group(2), plainText(matcher.group(3))));
			headingIndex = headings.size() - 1;
			bodyStart = matcher.end();
		}
		addSection(sections, headingIndex, bodyHtml.substring(bodyStart));
		this.pages.add(new Page(path, title, headings, sections));
	}

	private static void addSection(List<Section> sections, int headingIndex, String html) {
		String text = plainText(html);
		if (!text.isEmpty()) {
			sections.add(new Section(headingIndex, text));
		}
	}

	/** Writes both tiers into {@code <out>/<version>/<lang>/}. */
	public void write(Path dir) throws IOException {
		Files.createDirectories(dir);
		Files.writeString(dir.resolve("search-index.json"), tier1(), StandardCharsets.UTF_8);
		Files.writeString(dir.resolve("search-body.json"), tier2(), StandardCharsets.UTF_8);
	}

	/** Page paths, titles and headings -- the jump-to-page tier. */
	String tier1() {
		StringBuilder json = new StringBuilder();
		json.append("{\"v\":").append(VERSION).append(",\"lang\":").append(quote(this.lang)).append(",\"pages\":[");
		for (int i = 0; i < this.pages.size(); i++) {
			Page page = this.pages.get(i);
			if (i > 0) {
				json.append(',');
			}
			json.append("{\"p\":").append(quote(page.path())).append(",\"t\":").append(quote(page.title()));
			json.append(",\"h\":[");
			for (int h = 0; h < page.headings().size(); h++) {
				Heading heading = page.headings().get(h);
				if (h > 0) {
					json.append(',');
				}
				json.append('[').append(quote(heading.anchor())).append(',').append(quote(heading.text())).append(']');
			}
			json.append("]}");
		}
		return json.append("]}\n").toString();
	}

	/** The section bodies, as {@code [pageIndex, headingIndex, text]} triples. */
	String tier2() {
		StringBuilder json = new StringBuilder();
		json.append("{\"v\":").append(VERSION).append(",\"lang\":").append(quote(this.lang)).append(",\"s\":[");
		boolean first = true;
		for (int i = 0; i < this.pages.size(); i++) {
			for (Section section : this.pages.get(i).sections()) {
				if (!first) {
					json.append(',');
				}
				first = false;
				json.append('[')
					.append(i)
					.append(',')
					.append(section.headingIndex())
					.append(',')
					.append(quote(section.text()))
					.append(']');
			}
		}
		return json.append("]}\n").toString();
	}

	/**
	 * The searchable text of a fragment of rendered HTML: tags dropped, entities decoded,
	 * whitespace collapsed. A block tag becomes a space so that two list items do not run
	 * into one word; an inline tag becomes nothing, because {@code <code>x</code>s} is
	 * one.
	 */
	static String plainText(String html) {
		String text = INLINE_TAG.matcher(html).replaceAll("");
		text = TAG.matcher(text).replaceAll(" ");
		text = unescape(text);
		return text.replaceAll("\\s+", " ").trim();
	}

	/** Decodes the entities the renderer emits (it does no typographic substitution). */
	private static String unescape(String text) {
		if (text.indexOf('&') < 0) {
			return text;
		}
		return text.replace("&lt;", "<")
			.replace("&gt;", ">")
			.replace("&quot;", "\"")
			.replace("&#39;", "'")
			.replace("&nbsp;", " ")
			// last, so that "&amp;lt;" decodes to "&lt;" rather than to "<"
			.replace("&amp;", "&");
	}

	/** A JSON string literal. Non-ASCII stays verbatim -- the files are served UTF-8. */
	static String quote(String s) {
		StringBuilder sb = new StringBuilder(s.length() + 2).append('"');
		for (int i = 0; i < s.length(); i++) {
			char c = s.charAt(i);
			switch (c) {
				case '"' -> sb.append("\\\"");
				case '\\' -> sb.append("\\\\");
				case '\n' -> sb.append("\\n");
				case '\r' -> sb.append("\\r");
				case '\t' -> sb.append("\\t");
				default -> {
					if (c < 0x20) {
						sb.append("\\u%04x".formatted((int) c));
					}
					else {
						sb.append(c);
					}
				}
			}
		}
		return sb.append('"').toString();
	}

}
