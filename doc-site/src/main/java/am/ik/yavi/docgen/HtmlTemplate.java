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
 * {@code assets/docs.css}; the runnable-cell wiring, the theme control and the search
 * dialog in {@code assets/docs.js}, which finds this tree's {@link SearchIndex} files
 * through the {@code data-search-base} the body carries and loads the Java runtime from
 * the {@code data-runtime-src} beside it.
 *
 * <p>
 * A tiny inline script runs before the stylesheet so the stored theme
 * ({@code localStorage["yavi-doc-theme"]}) is on {@code <html>} before the first paint;
 * without it the page would render the system palette and then flip. The rest of the
 * shell is English, but the theme labels are translated for every language the site
 * publishes.
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
		// Before the stylesheet: the stored choice has to be on <html> for the first
		// paint, or the page would briefly show the system palette and then flip.
		html.append("<script>")
			.append("(function(){try{")
			.append("var m=localStorage.getItem(\"yavi-doc-theme\");")
			.append("if(m!==\"light\"&&m!==\"dark\"){m=\"system\";}")
			.append("var d=m===\"dark\"||(m===\"system\"&&window.matchMedia&&")
			.append("window.matchMedia(\"(prefers-color-scheme: dark)\").matches);")
			.append("var e=document.documentElement;")
			.append("e.setAttribute(\"data-theme-mode\",m);")
			.append("e.setAttribute(\"data-theme\",d?\"dark\":\"light\");")
			.append("}catch(e){}})();")
			.append("</script>\n");
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
		appendThemeSwitcher(html, ctx.lang());
		appendVersionSwitcher(html, "versionswitch", "Version", ctx.currentDocPath(), ctx.versions());
		appendSwitcher(html, "langswitch", "Language", ctx.currentDocPath(), ctx.languages());
		html.append("<a href=\"https://github.com/making/yavi\" target=\"_blank\" rel=\"noopener\">GitHub</a>\n");
		html.append(
				"<button type=\"button\" class=\"reset-runtime\" title=\"Reset the in-page runtime\">Reset runtime</button>\n");
		html.append("<span class=\"runtime-status\" data-state=\"idle\"></span>\n");
		html.append("</nav>\n");
		html.append("</header>\n");
	}

	/**
	 * The version switcher is a menu like the theme switcher: the number of published
	 * versions grows, and a row of buttons does not. {@code assets/docs.js} opens and
	 * closes it; each entry is a link to the same page in that version.
	 */
	private static void appendVersionSwitcher(StringBuilder html, String className, String label, String currentDocPath,
			List<Switch> targets) {
		String current = targets.stream().filter(Switch::active).map(Switch::name).findFirst().orElse("");
		html.append("<div class=\"").append(className).append("\">\n");
		html.append("<button type=\"button\" class=\"version-open\" aria-label=\"")
			.append(esc(label))
			.append(": ")
			.append(esc(current))
			.append("\" aria-expanded=\"false\" aria-haspopup=\"menu\" title=\"")
			.append(esc(label))
			.append(": ")
			.append(esc(current))
			.append("\">")
			.append(esc(current))
			.append("<svg class=\"version-caret\" viewBox=\"0 0 16 16\" width=\"10\" height=\"10\" aria-hidden=\"true\" focusable=\"false\"><path d=\"M3.5 6l4.5 4.5L12.5 6\"></path></svg>")
			.append("</button>\n");
		html.append("<div class=\"version-menu\" role=\"menu\" aria-label=\"")
			.append(esc(label))
			.append("\" hidden>\n");
		for (Switch target : targets) {
			html.append("<a class=\"version-option")
				.append(target.active() ? " active" : "")
				.append("\" role=\"menuitem\" href=\"")
				.append(esc(rel(currentDocPath, target.docPath())))
				.append("\"")
				.append(target.active() ? " aria-current=\"true\"" : "")
				.append(">")
				.append(esc(target.name()))
				.append("</a>\n");
		}
		html.append("</div>\n</div>\n");
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

	/**
	 * The theme switcher. {@code html[data-theme-mode]} holds the visitor's choice and
	 * {@code html[data-theme]} the palette the page is actually wearing; the inline head
	 * script and {@code assets/docs.js} are the only writers of both.
	 */
	private static void appendThemeSwitcher(StringBuilder html, String lang) {
		ThemeLabels labels = themeLabels(lang);
		String initial = labels.trigger(labels.system());
		html.append("<div class=\"themeswitch\">\n");
		html.append("<button type=\"button\" class=\"theme-open\" aria-label=\"")
			.append(esc(initial))
			.append("\" aria-expanded=\"false\" aria-controls=\"theme-menu\" aria-haspopup=\"true\" title=\"")
			.append(esc(initial))
			.append("\" data-theme-label=\"")
			.append(esc(labels.group()))
			.append("\" data-label-system=\"")
			.append(esc(labels.system()))
			.append("\" data-label-light=\"")
			.append(esc(labels.light()))
			.append("\" data-label-dark=\"")
			.append(esc(labels.dark()))
			.append("\">\n");
		appendThemeIcon(html, "system");
		appendThemeIcon(html, "light");
		appendThemeIcon(html, "dark");
		html.append("</button>\n");
		html.append("<div class=\"theme-menu\" id=\"theme-menu\" role=\"group\" aria-label=\"")
			.append(esc(labels.group()))
			.append("\" hidden>\n");
		appendThemeOption(html, "system", labels.system(), true);
		appendThemeOption(html, "light", labels.light(), false);
		appendThemeOption(html, "dark", labels.dark(), false);
		html.append("</div>\n");
		html.append("</div>\n");
	}

	private static void appendThemeOption(StringBuilder html, String mode, String label, boolean pressed) {
		html.append("<button type=\"button\" class=\"theme-option\" data-theme-value=\"")
			.append(mode)
			.append("\" aria-pressed=\"")
			.append(pressed ? "true" : "false")
			.append("\" title=\"")
			.append(esc(label))
			.append("\">");
		appendThemeIcon(html, mode);
		html.append("<span class=\"theme-option-label\">").append(esc(label)).append("</span></button>\n");
	}

	private static void appendThemeIcon(StringBuilder html, String mode) {
		html.append("<svg class=\"theme-icon theme-icon-")
			.append(mode)
			.append("\" viewBox=\"0 0 24 24\" width=\"16\" height=\"16\" aria-hidden=\"true\"")
			.append(" focusable=\"false\">");
		switch (mode) {
			case "light" -> html.append(
					"<circle cx=\"12\" cy=\"12\" r=\"4\"></circle><path d=\"M12 2v2.5M12 19.5V22M2 12h2.5M19.5 12H22M4.9 4.9l1.8 1.8M17.3 17.3l1.8 1.8M19.1 4.9l-1.8 1.8M6.7 17.3l-1.8 1.8\"></path>");
			case "dark" -> html.append("<path d=\"M20.5 14.2A8.5 8.5 0 0 1 9.8 3.5a8.5 8.5 0 1 0 10.7 10.7Z\"></path>");
			default -> html.append(
					"<rect x=\"3\" y=\"4\" width=\"18\" height=\"12\" rx=\"1.5\"></rect><path d=\"M8 20h8M12 16v4\"></path>");
		}
		html.append("</svg>");
	}

	/**
	 * The labels of the theme switcher. The rest of the page shell is English, so only
	 * these are translated; an unpublished language falls back to English.
	 */
	private record ThemeLabels(String group, String system, String light, String dark) {

		String trigger(String mode) {
			return this.group + ": " + mode;
		}
	}

	private static ThemeLabels themeLabels(String lang) {
		if ("ja".equals(lang)) {
			return new ThemeLabels("カラーテーマ", "システム", "ライト", "ダーク");
		}
		return new ThemeLabels("Color theme", "System", "Light", "Dark");
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
		appendVersionSwitcher(html, "versionswitch sidebar-switch", "Version", ctx.currentDocPath(), ctx.versions());
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
