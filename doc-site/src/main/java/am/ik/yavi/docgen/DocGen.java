/*
 * SPDX-FileCopyrightText: 2018-2026 Toshiaki Maki <makingx@gmail.com>
 * SPDX-License-Identifier: Apache-2.0
 */
package am.ik.yavi.docgen;

import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeSet;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Stream;

import com.vladsch.flexmark.html.HtmlRenderer;
import com.vladsch.flexmark.parser.Parser;

/**
 * Documentation site generator. Converts the Markdown under a source tree into a static
 * HTML site with a sidebar, previous/next links, version and language switchers, a
 * client-side search, and interactive runnable Java cells.
 *
 * <p>
 * Layout of the source tree:
 *
 * <pre>
 * content/
 *   versions.yaml            published versions, languages and the defaults
 *   &lt;version&gt;/&lt;lang&gt;/nav.yaml + **&#47;*.md
 * </pre>
 *
 * Every version listed in {@code versions.yaml} must have a directory with at least one
 * language directory carrying a {@code nav.yaml}; a listed version without one, an
 * unlisted version directory, or an unlisted language directory fails the build.
 *
 * <p>
 * The generated output:
 *
 * <pre>
 * &lt;out&gt;/index.html              redirect to the default version/language
 * &lt;out&gt;/&lt;page&gt;.html             legacy unversioned URL, redirect to the default tree
 * &lt;out&gt;/versions.json
 * &lt;out&gt;/&lt;version&gt;/&lt;lang&gt;/**&#47;*.html
 * &lt;out&gt;/&lt;version&gt;/&lt;lang&gt;/search-index.json, search-body.json
 * &lt;out&gt;/assets/**
 * </pre>
 *
 * <p>
 * The generator also fails the build on a relative Markdown link that names a page the
 * containing tree does not publish, so a moved page cannot ship a dead link.
 *
 * <p>
 * Usage:
 *
 * <pre>
 * java -jar yavi-docgen.jar --source content --out target/site
 * </pre>
 */
public final class DocGen {

	private static final Pattern MD_LINK = Pattern.compile("href=\"([^\"]*?)\\.md(#[^\"]*)?\"");

	private static final Pattern HEADING_ID = Pattern.compile("(<h[1-6]\\b[^>]*\\bid=\")([^\"]*)(\")");

	private static final List<String> ASSETS = List.of("docs.css", "docs.js", "yavi-logo.png", "favicon.svg");

	/** The runtime files that replace the stub when a runtime build is supplied. */
	private static final List<String> RUNTIME_FILES = List.of("yavi-runtime.js", "yavi-runtime-wasm.js",
			"yavi-runtime-wasm.js.wasm");

	private final Path source;

	private final Path out;

	/**
	 * Directory with one sub-directory per documentation version holding that tree's
	 * runtime (see doc-site/README.md), or {@code null} to publish the stub.
	 */
	private final Path runtime;

	private final Versions versions;

	private final Parser parser;

	private final HtmlRenderer renderer;

	/** version -> language -> tree, all loaded before anything is rendered. */
	private final Map<String, Map<String, Tree>> trees = new LinkedHashMap<>();

	/**
	 * The heading anchors of the reference language of each version, keyed by
	 * {@code <version>/<md file>}. A translated page keeps its own heading TEXT but takes
	 * these anchors: a cross-page link is written once as {@code page.md#anchor} and
	 * mirrored verbatim into every language tree, so an anchor slugged from the
	 * translated heading would resolve in one tree only.
	 */
	private final Map<String, List<String>> referenceHeadingIds = new LinkedHashMap<>();

	/**
	 * The client-side search index of the tree currently being generated. Every page
	 * rendered by this generator feeds it, and it is written out when the tree is done --
	 * one index per tree, because a search never crosses versions or languages.
	 */
	private SearchIndex searchIndex;

	public DocGen(Path source, Path out) throws IOException {
		this(source, out, null);
	}

	/**
	 * @param runtime directory with one sub-directory per version holding the built
	 * runtime, or {@code null} to publish the stub
	 */
	public DocGen(Path source, Path out, Path runtime) throws IOException {
		this.source = source;
		this.out = out;
		this.runtime = runtime;
		this.versions = Versions.load(source.resolve("versions.yaml"));
		this.parser = Parser.builder(Markdown.options()).build();
		this.renderer = HtmlRenderer.builder(Markdown.options())
			.attributeProviderFactory(RunnableBlockTransformer.marker())
			.build();
		loadTrees();
	}

	public static void main(String[] args) throws IOException {
		Path source = Path.of("content");
		Path out = Path.of("target", "site");
		Path runtime = null;
		for (int i = 0; i < args.length - 1; i++) {
			switch (args[i]) {
				case "--source" -> source = Path.of(args[++i]).toAbsolutePath().normalize();
				case "--out" -> out = Path.of(args[++i]).toAbsolutePath().normalize();
				case "--runtime" -> runtime = Path.of(args[++i]).toAbsolutePath().normalize();
				default -> {
				}
			}
		}
		new DocGen(source, out, runtime).generate();
	}

	/** Generates the whole site for every configured version and language. */
	public void generate() throws IOException {
		Files.createDirectories(this.out);
		copyAssets();
		for (Versions.Version version : this.versions.versions()) {
			for (Tree tree : orderedTrees(version.version())) {
				generateTree(tree);
			}
		}
		writeVersionsJson();
		writeIndexRedirect();
		writeLegacyRedirects();
		System.out.println("Generated documentation site for " + this.trees.keySet() + " into " + this.out);
	}

	/**
	 * Loads and validates every tree the configuration names. The whole set is loaded
	 * before rendering because a version or language switch has to know whether the same
	 * page exists in the target tree.
	 */
	private void loadTrees() throws IOException {
		Map<String, Versions.Version> declaredVersions = new LinkedHashMap<>();
		for (Versions.Version version : this.versions.versions()) {
			declaredVersions.put(version.version(), version);
		}
		try (Stream<Path> entries = Files.list(this.source)) {
			for (Path entry : entries.filter(Files::isDirectory).sorted().toList()) {
				String name = entry.getFileName().toString();
				if (!declaredVersions.containsKey(name)) {
					throw new IOException("Content directory '" + entry + "' is not listed in versions.yaml");
				}
			}
		}
		for (Versions.Version version : this.versions.versions()) {
			Path versionDir = this.source.resolve(version.version());
			if (!Files.isDirectory(versionDir)) {
				throw new IOException("Missing content directory for version '" + version.version() + "'");
			}
			List<String> languages = presentLanguages(versionDir);
			if (languages.isEmpty()) {
				throw new IOException("Version '" + version.version() + "' has no language directory with a nav.yaml");
			}
			Map<String, Tree> byLanguage = new LinkedHashMap<>();
			for (String language : languages) {
				// Every language directory must carry a nav.yaml: a missing one is an
				// incomplete translation, not an optional extra.
				byLanguage.put(language, loadTree(version.version(), versionDir.resolve(language), language));
			}
			if (!byLanguage.containsKey(referenceLanguage(byLanguage))) {
				throw new IOException("Version '" + version.version() + "' has no reference language tree");
			}
			this.trees.put(version.version(), byLanguage);
		}
	}

	/** The language directories of a version, in configured order. */
	private List<String> presentLanguages(Path versionDir) throws IOException {
		Set<String> present = new TreeSet<>();
		try (Stream<Path> entries = Files.list(versionDir)) {
			for (Path entry : entries.filter(Files::isDirectory).toList()) {
				present.add(entry.getFileName().toString());
			}
		}
		List<String> ordered = new ArrayList<>();
		for (Versions.Language language : this.versions.languages()) {
			if (present.contains(language.code())) {
				ordered.add(language.code());
			}
		}
		for (String name : present) {
			if (!ordered.contains(name)) {
				throw new IOException("Language directory '" + name + "' is not listed in versions.yaml (version "
						+ versionDir.getFileName() + ")");
			}
		}
		return ordered;
	}

	private Tree loadTree(String version, Path langDir, String lang) throws IOException {
		Path navFile = langDir.resolve("nav.yaml");
		if (!Files.exists(navFile)) {
			throw new IOException("Missing nav.yaml in " + langDir + " (version " + version + ")");
		}
		Nav nav = Nav.load(navFile);
		if (nav.flatPages().isEmpty()) {
			throw new IOException("nav.yaml in " + langDir + " has no pages");
		}
		Set<String> pageFiles = new TreeSet<>();
		for (Nav.Page page : nav.allPages()) {
			Path mdPath = langDir.resolve(page.file()).normalize();
			if (!mdPath.startsWith(langDir.normalize())) {
				throw new IOException("Page '" + page.file() + "' in " + navFile + " leaves the language directory");
			}
			if (!Files.isRegularFile(mdPath)) {
				throw new IOException("Missing Markdown source: " + mdPath);
			}
			if (!pageFiles.add(page.file())) {
				throw new IOException("Duplicate page '" + page.file() + "' in " + navFile);
			}
		}
		String indexMdFile = nav.findPage("index.md")
			.map(Nav.Page::file)
			.orElseGet(() -> nav.flatPages().get(0).file());
		return new Tree(version, lang, nav, Set.copyOf(pageFiles), indexMdFile);
	}

	/** The language that supplies the anchors every other language adopts. */
	private String referenceLanguage(Map<String, Tree> byLanguage) {
		if (byLanguage.containsKey(this.versions.defaultLanguage())) {
			return this.versions.defaultLanguage();
		}
		return byLanguage.get(byLanguage.keySet().iterator().next()).lang();
	}

	/** Trees in render order: the reference language first, then the remaining ones. */
	private List<Tree> orderedTrees(String version) {
		Map<String, Tree> byLanguage = this.trees.get(version);
		String reference = referenceLanguage(byLanguage);
		List<Tree> ordered = new ArrayList<>();
		ordered.add(byLanguage.get(reference));
		for (Tree tree : byLanguage.values()) {
			if (!tree.lang().equals(reference)) {
				ordered.add(tree);
			}
		}
		return ordered;
	}

	private void generateTree(Tree tree) throws IOException {
		Path langDir = this.source.resolve(tree.version()).resolve(tree.lang());
		this.searchIndex = new SearchIndex(tree.lang());
		List<Nav.Page> pages = tree.nav().flatPages();
		for (int i = 0; i < pages.size(); i++) {
			Nav.Page page = pages.get(i);
			HtmlTemplate.Crumb prev = (i > 0) ? crumb(tree, pages.get(i - 1)) : null;
			HtmlTemplate.Crumb next = (i < pages.size() - 1) ? crumb(tree, pages.get(i + 1)) : null;
			renderNavPage(tree, langDir, page, prev, next);
			renderSubpages(tree, langDir, page, tree.docPath(page.file()));
		}
		this.searchIndex.write(this.out.resolve(tree.version()).resolve(tree.lang()));
	}

	private void renderNavPage(Tree tree, Path langDir, Nav.Page page, HtmlTemplate.Crumb prev, HtmlTemplate.Crumb next)
			throws IOException {
		Path mdPath = langDir.resolve(page.file());
		String docPath = tree.docPath(page.file());
		String body = renderBody(tree, Files.readString(mdPath, StandardCharsets.UTF_8), page.file(), page.title());
		HtmlTemplate.PageContext ctx = context(tree, page.file(), page.title(), docPath, docPath, body, null, prev,
				next);
		writePage(docPath, HtmlTemplate.render(ctx));
	}

	/**
	 * Renders the sub-pages of a top-level nav page. They are reachable only through
	 * their ancestors, so they get a back link to their immediate parent and a
	 * previous/next chain of their own that starts there.
	 */
	private void renderSubpages(Tree tree, Path langDir, Nav.Page parent, String topDocPath) throws IOException {
		List<Nav.Page> subpages = parent.subpages();
		if (subpages.isEmpty()) {
			return;
		}
		HtmlTemplate.Crumb backlink = new HtmlTemplate.Crumb(tree.docPath(parent.file()), parent.title());
		for (int i = 0; i < subpages.size(); i++) {
			Nav.Page page = subpages.get(i);
			Path mdPath = langDir.resolve(page.file());
			String docPath = tree.docPath(page.file());
			HtmlTemplate.Crumb prev = (i > 0) ? crumb(tree, subpages.get(i - 1)) : backlink;
			HtmlTemplate.Crumb next = (i < subpages.size() - 1) ? crumb(tree, subpages.get(i + 1)) : null;
			String body = renderBody(tree, Files.readString(mdPath, StandardCharsets.UTF_8), page.file(), page.title());
			HtmlTemplate.PageContext ctx = context(tree, page.file(), page.title(), docPath, topDocPath, body, backlink,
					prev, next);
			writePage(docPath, HtmlTemplate.render(ctx));
			renderSubpages(tree, langDir, page, topDocPath);
		}
	}

	private HtmlTemplate.PageContext context(Tree tree, String mdFile, String title, String docPath,
			String activeNavDocPath, String body, HtmlTemplate.Crumb backlink, HtmlTemplate.Crumb prev,
			HtmlTemplate.Crumb next) {
		return new HtmlTemplate.PageContext(tree.nav(), this.versions.title(), this.versions.description(),
				tree.version(), tree.lang(), title, docPath, tree.indexPath(), activeNavDocPath, body,
				TocBuilder.build(body), backlink, prev, next, versionSwitches(tree, mdFile),
				languageSwitches(tree, mdFile));
	}

	/**
	 * Renders one Markdown page and feeds the search index with it. The index is built
	 * from the body after the anchors are aligned and the links rewritten, but BEFORE the
	 * runnable cells are built: the cell chrome is markup nobody searches for, and the
	 * section anchors have to be the ones the page will actually carry.
	 */
	private String renderBody(Tree tree, String markdown, String mdFile, String title) throws IOException {
		String body = this.renderer.render(this.parser.parse(markdown));
		body = alignHeadingIds(tree, body, mdFile);
		validateMarkdownLinks(tree, body, mdFile);
		body = rewriteMarkdownLinks(body);
		this.searchIndex.addPage(HtmlTemplate.replaceExtension(mdFile), title, body);
		return RunnableBlockTransformer.transform(body);
	}

	/**
	 * Gives a translated page the reference language's heading anchors, matched by
	 * position. The trees are required to have the same heading layout, so a page whose
	 * heading count differs from its reference counterpart is a translation that has
	 * drifted -- it fails the build here rather than shipping anchors that point at the
	 * wrong sections.
	 */
	private String alignHeadingIds(Tree tree, String body, String mdFile) throws IOException {
		String key = tree.version() + "/" + mdFile;
		if (tree.lang().equals(referenceLanguage(this.trees.get(tree.version())))) {
			this.referenceHeadingIds.put(key, headingIds(body));
			return body;
		}
		List<String> reference = this.referenceHeadingIds.get(key);
		if (reference == null) {
			return body;
		}
		List<String> own = headingIds(body);
		if (own.size() != reference.size()) {
			throw new IOException("Heading layout of " + tree.version() + "/" + tree.lang() + "/" + mdFile
					+ " differs from the reference language: " + own.size() + " headings vs " + reference.size()
					+ ". Every language tree must have the same headings in the same order.");
		}
		Matcher matcher = HEADING_ID.matcher(body);
		StringBuilder out = new StringBuilder();
		int i = 0;
		while (matcher.find()) {
			String replacement = matcher.group(1) + reference.get(i++) + matcher.group(3);
			matcher.appendReplacement(out, Matcher.quoteReplacement(replacement));
		}
		matcher.appendTail(out);
		return out.toString();
	}

	/** The {@code id} of every heading of a rendered body, in document order. */
	static List<String> headingIds(String bodyHtml) {
		List<String> ids = new ArrayList<>();
		Matcher matcher = HEADING_ID.matcher(bodyHtml);
		while (matcher.find()) {
			ids.add(matcher.group(2));
		}
		return ids;
	}

	/**
	 * Fails the build when a relative Markdown link names a page the containing tree does
	 * not publish. The link is resolved against the directory of the page that carries
	 * it, so a renamed or moved page is caught here instead of shipping a 404.
	 */
	private void validateMarkdownLinks(Tree tree, String html, String mdFile) throws IOException {
		Matcher matcher = MD_LINK.matcher(html);
		while (matcher.find()) {
			String target = matcher.group(1);
			// An absolute URL ending in .md names a file somewhere else.
			if (target.contains("://") || target.startsWith("//")) {
				continue;
			}
			Path from = Path.of(mdFile).getParent();
			String resolved = ((from == null) ? Path.of(target + ".md") : from.resolve(target + ".md")).normalize()
				.toString()
				.replace('\\', '/');
			if (!tree.pageFiles().contains(resolved)) {
				throw new IOException("Link to missing page '" + target + ".md' in " + tree.version() + "/"
						+ tree.lang() + "/" + mdFile);
			}
		}
	}

	/** Rewrites {@code href="foo.md"} / {@code href="foo.md#x"} to {@code .html}. */
	static String rewriteMarkdownLinks(String html) {
		Matcher matcher = MD_LINK.matcher(html);
		StringBuilder out = new StringBuilder();
		while (matcher.find()) {
			String target = matcher.group(1);
			// Only a link to a page of this site becomes .html. An absolute URL ending
			// in .md names a file somewhere else, with no .html beside it.
			String replacement = target.contains("://") || target.startsWith("//") ? matcher.group(0)
					: "href=\"" + target + ".html" + (matcher.group(2) == null ? "" : matcher.group(2)) + "\"";
			matcher.appendReplacement(out, Matcher.quoteReplacement(replacement));
		}
		matcher.appendTail(out);
		return out.toString();
	}

	/**
	 * The version switcher of one page. A switch prefers the same page in the other
	 * version and falls back to that version's language index when the page is absent.
	 */
	private List<HtmlTemplate.Switch> versionSwitches(Tree tree, String mdFile) {
		List<HtmlTemplate.Switch> switches = new ArrayList<>();
		for (Versions.Version version : this.versions.versions()) {
			boolean active = version.version().equals(tree.version());
			String docPath = active ? tree.docPath(mdFile) : switchTarget(version.version(), tree.lang(), mdFile);
			switches.add(new HtmlTemplate.Switch(version.version(), version.label(), docPath, active));
		}
		return switches;
	}

	/**
	 * The language switcher of one page, within its own version. A switch prefers the
	 * same page in the other language and falls back to that language's index when the
	 * translation lacks it.
	 */
	private List<HtmlTemplate.Switch> languageSwitches(Tree tree, String mdFile) {
		List<HtmlTemplate.Switch> switches = new ArrayList<>();
		for (Versions.Language language : this.versions.languages()) {
			Tree target = this.trees.get(tree.version()).get(language.code());
			if (target == null) {
				continue;
			}
			boolean active = language.code().equals(tree.lang());
			String docPath;
			if (active) {
				docPath = tree.docPath(mdFile);
			}
			else {
				docPath = target.pageFiles().contains(mdFile) ? target.docPath(mdFile) : target.indexPath();
			}
			switches.add(new HtmlTemplate.Switch(language.code(), language.name(), docPath, active));
		}
		return switches;
	}

	/**
	 * The page to open when switching to {@code version}. The same page is preferred; a
	 * version that does not translate it (or does not carry the language at all) falls
	 * back to its reference language index.
	 */
	private String switchTarget(String version, String lang, String mdFile) {
		Map<String, Tree> byLanguage = this.trees.get(version);
		Tree target = byLanguage.get(lang);
		if (target == null) {
			target = byLanguage.get(referenceLanguage(byLanguage));
		}
		return target.pageFiles().contains(mdFile) ? target.docPath(mdFile) : target.indexPath();
	}

	private void writePage(String docPath, String html) throws IOException {
		Path outPath = this.out.resolve(docPath);
		Files.createDirectories(outPath.getParent());
		Files.writeString(outPath, html, StandardCharsets.UTF_8);
	}

	private static HtmlTemplate.Crumb crumb(Tree tree, Nav.Page page) {
		return new HtmlTemplate.Crumb(tree.docPath(page.file()), page.title());
	}

	private void writeVersionsJson() throws IOException {
		StringBuilder json = new StringBuilder();
		json.append("{\"v\":1,\"title\":")
			.append(SearchIndex.quote(this.versions.title()))
			.append(",\"default_version\":")
			.append(SearchIndex.quote(this.versions.defaultVersion()))
			.append(",\"default_language\":")
			.append(SearchIndex.quote(this.versions.defaultLanguage()))
			.append(",\"languages\":[");
		for (int i = 0; i < this.versions.languages().size(); i++) {
			Versions.Language language = this.versions.languages().get(i);
			if (i > 0) {
				json.append(',');
			}
			json.append("{\"code\":")
				.append(SearchIndex.quote(language.code()))
				.append(",\"name\":")
				.append(SearchIndex.quote(language.name()))
				.append('}');
		}
		json.append("],\"versions\":[");
		for (int i = 0; i < this.versions.versions().size(); i++) {
			Versions.Version version = this.versions.versions().get(i);
			if (i > 0) {
				json.append(',');
			}
			json.append("{\"version\":")
				.append(SearchIndex.quote(version.version()))
				.append(",\"label\":")
				.append(SearchIndex.quote(version.label()))
				.append(",\"languages\":[");
			boolean first = true;
			for (String lang : this.trees.get(version.version()).keySet()) {
				if (!first) {
					json.append(',');
				}
				first = false;
				json.append(SearchIndex.quote(lang));
			}
			json.append("]}");
		}
		json.append("]}\n");
		Files.writeString(this.out.resolve("versions.json"), json.toString(), StandardCharsets.UTF_8);
	}

	/** The tree the site root and the legacy unversioned URLs forward to. */
	private Tree defaultTree() {
		Map<String, Tree> byLanguage = this.trees.get(this.versions.defaultVersion());
		Tree home = byLanguage.get(this.versions.defaultLanguage());
		if (home == null) {
			home = byLanguage.get(referenceLanguage(byLanguage));
		}
		return home;
	}

	private void writeIndexRedirect() throws IOException {
		Tree home = defaultTree();
		writeRedirect(this.out.resolve("index.html"), home.indexPath(), this.versions.title() + " documentation");
	}

	/**
	 * Writes a redirect at the site root for every top-level page of the default tree.
	 * The AsciiDoc site this generator replaces published those pages at the site root;
	 * keeping the stubs means the links that already exist keep resolving to the
	 * versioned page.
	 */
	private void writeLegacyRedirects() throws IOException {
		Tree home = defaultTree();
		for (Nav.Page page : home.nav().flatPages()) {
			String stub = HtmlTemplate.replaceExtension(page.file());
			// The root index is already the redirect to the tree's index page.
			if (stub.equals("index.html")) {
				continue;
			}
			String target = home.docPath(page.file());
			writeRedirect(this.out.resolve(stub), target,
					page.title() + " - " + this.versions.title() + " documentation");
		}
	}

	private static void writeRedirect(Path outPath, String target, String title) throws IOException {
		String html = """
				<!DOCTYPE html>
				<html lang="en">
				<head>
				<meta charset="UTF-8">
				<meta http-equiv="refresh" content="0; url=%s">
				<link rel="canonical" href="%s">
				<title>%s</title>
				</head>
				<body>
				<p>Redirecting to <a href="%s">the documentation</a>.</p>
				</body>
				</html>
				""".formatted(target, target, HtmlTemplate.esc(title), target);
		Files.createDirectories(outPath.getParent());
		Files.writeString(outPath, html, StandardCharsets.UTF_8);
	}

	private void copyAssets() throws IOException {
		Path target = this.out.resolve("assets");
		Files.createDirectories(target);
		for (String name : ASSETS) {
			try (InputStream in = DocGen.class.getResourceAsStream("/assets/" + name)) {
				if (in == null) {
					throw new IOException("Missing classpath asset: assets/" + name);
				}
				Files.copy(in, target.resolve(name), StandardCopyOption.REPLACE_EXISTING);
			}
		}
		for (Versions.Version version : this.versions.versions()) {
			copyRuntime(version.version(), target.resolve("runtime").resolve(version.version()));
		}
	}

	/**
	 * Publishes one documentation tree's Java runtime. A runtime built for that version
	 * replaces the stub; without one the stub (which reports that execution is not
	 * available) keeps the page working.
	 */
	private void copyRuntime(String version, Path target) throws IOException {
		Files.createDirectories(target);
		Path built = this.runtime == null ? null : this.runtime.resolve(version);
		if (built != null && Files.isDirectory(built)) {
			for (String name : RUNTIME_FILES) {
				Path file = built.resolve(name);
				if (!Files.exists(file)) {
					throw new IOException("Runtime for version '" + version + "' is missing " + name + " in " + built);
				}
				Files.copy(file, target.resolve(name), StandardCopyOption.REPLACE_EXISTING);
			}
			return;
		}
		try (InputStream in = DocGen.class.getResourceAsStream("/assets/yavi-runtime.js")) {
			if (in == null) {
				throw new IOException("Missing classpath asset: assets/yavi-runtime.js");
			}
			Files.copy(in, target.resolve("yavi-runtime.js"), StandardCopyOption.REPLACE_EXISTING);
		}
	}

	/**
	 * One version/language tree: its navigation and the page paths it defines, used for
	 * switch-target resolution.
	 */
	private record Tree(String version, String lang, Nav nav, Set<String> pageFiles, String indexMdFile) {

		String docPath(String mdFile) {
			return this.version + "/" + this.lang + "/" + HtmlTemplate.replaceExtension(mdFile);
		}

		String indexPath() {
			return docPath(this.indexMdFile);
		}
	}

}
