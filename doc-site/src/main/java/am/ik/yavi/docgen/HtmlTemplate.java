/*
 * SPDX-FileCopyrightText: 2018-2026 Toshiaki Maki <makingx@gmail.com>
 * SPDX-License-Identifier: Apache-2.0
 */
package am.ik.yavi.docgen;

import java.nio.file.Path;
import java.util.List;

/**
 * Renders the full HTML page shell around a rendered Markdown body: the top bar (site
 * title, search, version switcher, language switcher, runtime status, reset), the left
 * sidebar navigation, the content, and previous/next links. The theme lives in
 * {@code assets/docs.css}; the runnable-cell wiring and the search dialog in
 * {@code assets/docs.js}, which finds this tree's {@link SearchIndex} files through the
 * {@code data-search-base} the body carries and loads the Java runtime from the
 * {@code data-runtime-src} beside it.
 */
public final class HtmlTemplate {

	private HtmlTemplate() {
	}

	/**
	 * A target of the version or language switcher: the display label and the
	 * docs-root-relative HTML path it links to. {@code active} marks the current entry.
	 */
	public record Switch(String code, String name, String docPath, boolean active) {
	}

	/**
	 * A navigation target: the docs-root-relative path of an HTML page plus its display
	 * title. Used for previous/next links and the back link.
	 */
	public record Crumb(String docPath, String title) {
	}

	/**
	 * Everything needed to render one page.
	 *
	 * @param nav the sidebar navigation model for this tree
	 * @param siteTitle the site name shown in the brand and {@code <title>}
	 * @param siteDescription the site description for the meta tag
	 * @param version the version directory of this tree
	 * @param lang the language code of this tree
	 * @param title the page title
	 * @param currentDocPath docs-root-relative path of this page's HTML output
	 * @param homeDocPath docs-root-relative path of this tree's index page
	 * @param activeNavDocPath docs-root-relative path of the sidebar entry to highlight
	 * @param bodyHtml the rendered Markdown body
	 * @param tocHtml the "On this page" table of contents, or an empty string
	 * @param backlink an optional link shown above the content, or {@code null}
	 * @param prev the previous-page link, or {@code null}
	 * @param next the next-page link, or {@code null}
	 * @param versions every published version for the version switcher
	 * @param languages every language present in this version for the language switcher
	 */
	public record PageContext(Nav nav, String siteTitle, String siteDescription, String version, String lang,
			String title, String currentDocPath, String homeDocPath, String activeNavDocPath, String bodyHtml,
			String tocHtml, Crumb backlink, Crumb prev, Crumb next, List<Switch> versions, List<Switch> languages) {
	}

	/** Renders the complete HTML document for one page. */
	public static String render(PageContext ctx) {
		String assetsCss = rel(ctx.currentDocPath(), "assets/docs.css");
		String assetsJs = rel(ctx.currentDocPath(), "assets/docs.js");
		String runtimeSrc = rel(ctx.currentDocPath(), "assets/runtime/" + ctx.version() + "/yavi-runtime.js");
		String faviconSrc = rel(ctx.currentDocPath(), "assets/favicon.svg");
		String searchBase = rel(ctx.currentDocPath(), ctx.version() + "/" + ctx.lang());
		String pageTitle = ctx.title() + " · " + ctx.siteTitle() + " " + ctx.version() + " documentation";

		StringBuilder html = new StringBuilder();
		html.append("<!DOCTYPE html>\n<html lang=\"").append(esc(ctx.lang())).append("\">\n<head>\n");
		html.append("<meta charset=\"UTF-8\">\n");
		html.append("<meta name=\"viewport\" content=\"width=device-width, initial-scale=1.0\">\n");
		html.append("<title>").append(esc(pageTitle)).append("</title>\n");
		html.append("<meta name=\"description\" content=\"").append(esc(ctx.siteDescription())).append("\">\n");
		html.append("<meta name=\"color-scheme\" content=\"light dark\">\n");
		html.append("<meta name=\"theme-color\" content=\"#590f3b\" media=\"(prefers-color-scheme: light)\">\n");
		html.append("<meta name=\"theme-color\" content=\"#160b12\" media=\"(prefers-color-scheme: dark)\">\n");
		html.append("<link rel=\"icon\" href=\"").append(faviconSrc).append("\" type=\"image/svg+xml\">\n");
		html.append("<link rel=\"stylesheet\" href=\"").append(assetsCss).append("\">\n");
		html.append("</head>\n");
		// The search index lives beside the pages of this tree; docs.js resolves every
		// result link against this base. The runtime script is loaded only on the first
		// Run, from the same assets directory.
		html.append("<body data-runtime-src=\"")
			.append(runtimeSrc)
			.append("\" data-search-base=\"")
			.append(searchBase)
			.append("\">\n");

		html.append("<a class=\"skip-link\" href=\"#main\">Skip to content</a>\n");
		appendTopbar(html, ctx);

		html.append("<div class=\"layout\">\n");
		appendSidebar(html, ctx);
		html.append("<main class=\"content\" id=\"main\" tabindex=\"-1\">\n");
		if (ctx.backlink() != null) {
			html.append("<a class=\"backlink\" href=\"")
				.append(rel(ctx.currentDocPath(), ctx.backlink().docPath()))
				.append("\">&larr; ")
				.append(esc(ctx.backlink().title()))
				.append("</a>\n");
		}
		html.append("<article class=\"markdown\">\n").append(ctx.bodyHtml()).append("\n</article>\n");
		appendPrevNext(html, ctx);
		appendColophon(html, ctx);
		html.append("</main>\n");
		if (ctx.tocHtml() != null && !ctx.tocHtml().isEmpty()) {
			html.append(ctx.tocHtml());
		}
		html.append("</div>\n");

		html.append("<script src=\"").append(assetsJs).append("\"></script>\n");
		html.append("</body>\n</html>\n");
		return html.toString();
	}

	private static void appendTopbar(StringBuilder html, PageContext ctx) {
		String logoSrc = rel(ctx.currentDocPath(), "assets/yavi-logo.png");
		html.append("<header class=\"topbar\">\n");
		html.append("<button type=\"button\" class=\"nav-toggle\" aria-label=\"Toggle navigation\"")
			.append(" aria-expanded=\"false\" aria-controls=\"sidebar\">\n")
			.append("<span class=\"nav-toggle-bar\"></span>")
			.append("<span class=\"nav-toggle-bar\"></span>")
			.append("<span class=\"nav-toggle-bar\"></span>")
			.append("</button>\n");
		html.append("<a class=\"brand\" href=\"")
			.append(rel(ctx.currentDocPath(), ctx.homeDocPath()))
			.append("\">")
			.append("<img class=\"brand-logo\" src=\"")
			.append(logoSrc)
			.append("\" alt=\"")
			.append(esc(ctx.siteTitle()))
			.append("\" width=\"108\" height=\"36\">")
			.append("</a>\n");
		html.append("<nav class=\"topnav\" aria-label=\"Site\">\n");
		html.append("<button type=\"button\" class=\"search-open\" aria-label=\"Search Ctrl K\">")
			.append("<span class=\"search-open-label\">Search</span>")
			.append("<kbd class=\"search-open-key\" aria-hidden=\"true\">Ctrl K</kbd>")
			.append("</button>\n");
		appendSwitcher(html, "versionswitch", "Version", ctx.currentDocPath(), ctx.versions());
		appendSwitcher(html, "langswitch", "Language", ctx.currentDocPath(), ctx.languages());
		html.append("<a href=\"https://github.com/making/yavi\" target=\"_blank\" rel=\"noopener\">GitHub</a>\n");
		html.append(
				"<button type=\"button\" class=\"reset-runtime\" title=\"Reset the in-page runtime\">Reset runtime</button>\n");
		html.append("<span class=\"runtime-status\" data-state=\"idle\"></span>\n");
		html.append("</nav>\n");
		html.append("</header>\n");
	}

	private static void appendSwitcher(StringBuilder html, String className, String label, String currentDocPath,
			List<Switch> targets) {
		html.append("<span class=\"").append(className).append("\"><span class=\"switch-label\">");
		html.append(esc(label)).append("</span>");
		boolean first = true;
		for (Switch target : targets) {
			if (!first) {
				html.append(" ");
			}
			first = false;
			if (target.active()) {
				html.append("<span class=\"switch active\">").append(esc(target.name())).append("</span>");
			}
			else {
				html.append("<a class=\"switch\" href=\"")
					.append(rel(currentDocPath, target.docPath()))
					.append("\">")
					.append(esc(target.name()))
					.append("</a>");
			}
		}
		html.append("</span>\n");
	}

	private static void appendSidebar(StringBuilder html, PageContext ctx) {
		html.append("<div class=\"sidebar-backdrop\" hidden></div>\n");
		html.append("<aside class=\"sidebar\" id=\"sidebar\">\n");
		html.append("<div class=\"sidebar-head\"><span class=\"sidebar-title\">Contents</span>")
			.append("<button type=\"button\" class=\"nav-close\" aria-label=\"Close navigation\">Close</button></div>\n");
		html.append("<nav aria-label=\"Documentation\">\n");
		for (Nav.Section section : ctx.nav().sections()) {
			html.append("<div class=\"nav-section\">\n");
			html.append("<div class=\"nav-section-title\">").append(esc(section.title())).append("</div>\n");
			html.append("<ul>\n");
			for (Nav.Page page : section.pages()) {
				String docPath = ctx.version() + "/" + ctx.lang() + "/" + replaceExtension(page.file());
				boolean active = docPath.equals(ctx.activeNavDocPath());
				String href = rel(ctx.currentDocPath(), docPath);
				html.append("<li><a class=\"nav-link")
					.append(active ? " active" : "")
					.append("\" href=\"")
					.append(href)
					.append("\"")
					.append(active ? " aria-current=\"page\"" : "")
					.append(">")
					.append(esc(page.title()))
					.append("</a></li>\n");
			}
			html.append("</ul>\n</div>\n");
		}
		html.append("</nav>\n");
		html.append("<div class=\"sidebar-meta\">\n");
		appendSwitcher(html, "versionswitch sidebar-switch", "Version", ctx.currentDocPath(), ctx.versions());
		appendSwitcher(html, "langswitch sidebar-switch", "Language", ctx.currentDocPath(), ctx.languages());
		html.append("<a class=\"sidebar-github\" href=\"https://github.com/making/yavi\" target=\"_blank\"")
			.append(" rel=\"noopener\">GitHub</a>\n");
		html.append("</div>\n");
		html.append("</aside>\n");
	}

	private static void appendPrevNext(StringBuilder html, PageContext ctx) {
		if (ctx.prev() == null && ctx.next() == null) {
			return;
		}
		html.append("<nav class=\"prevnext\">\n");
		if (ctx.prev() != null) {
			html.append("<a class=\"prev\" href=\"")
				.append(rel(ctx.currentDocPath(), ctx.prev().docPath()))
				.append("\"><span class=\"dir\">&larr; Previous</span><span class=\"label\">")
				.append(esc(ctx.prev().title()))
				.append("</span></a>\n");
		}
		else {
			html.append("<span></span>\n");
		}
		if (ctx.next() != null) {
			html.append("<a class=\"next\" href=\"")
				.append(rel(ctx.currentDocPath(), ctx.next().docPath()))
				.append("\"><span class=\"dir\">Next &rarr;</span><span class=\"label\">")
				.append(esc(ctx.next().title()))
				.append("</span></a>\n");
		}
		html.append("</nav>\n");
	}

	private static void appendColophon(StringBuilder html, PageContext ctx) {
		String langName = ctx.lang();
		for (Switch language : ctx.languages()) {
			if (language.active()) {
				langName = language.name();
			}
		}
		html.append("<footer class=\"colophon\">\n");
		html.append("<span class=\"colophon-item\">")
			.append(esc(ctx.siteTitle()))
			.append(" ")
			.append(esc(ctx.version()))
			.append("</span>\n");
		html.append("<span class=\"colophon-item\">").append(esc(langName)).append("</span>\n");
		html.append("<span class=\"colophon-item\">Apache-2.0</span>\n");
		html.append("<a class=\"colophon-item\" href=\"https://github.com/making/yavi\" target=\"_blank\"")
			.append(" rel=\"noopener\">github.com/making/yavi</a>\n");
		html.append("</footer>\n");
	}

	/** Computes a relative URL from one docs-root-relative path to another. */
	static String rel(String fromDocPath, String toDocPath) {
		Path from = Path.of(fromDocPath).getParent();
		Path to = Path.of(toDocPath);
		Path relative = (from == null) ? to : from.relativize(to);
		String result = relative.toString().replace('\\', '/');
		return result.isEmpty() ? "." : result;
	}

	static String replaceExtension(String mdFile) {
		return mdFile.endsWith(".md") ? mdFile.substring(0, mdFile.length() - 3) + ".html" : mdFile;
	}

	static String esc(String s) {
		StringBuilder sb = new StringBuilder(s.length());
		for (int i = 0; i < s.length(); i++) {
			char c = s.charAt(i);
			switch (c) {
				case '&' -> sb.append("&amp;");
				case '<' -> sb.append("&lt;");
				case '>' -> sb.append("&gt;");
				case '"' -> sb.append("&quot;");
				default -> sb.append(c);
			}
		}
		return sb.toString();
	}

}
