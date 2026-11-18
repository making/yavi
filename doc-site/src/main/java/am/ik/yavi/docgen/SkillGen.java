/*
 * SPDX-FileCopyrightText: 2018-2026 Toshiaki Maki <makingx@gmail.com>
 * SPDX-License-Identifier: Apache-2.0
 */
package am.ik.yavi.docgen;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.UncheckedIOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Stream;
import java.util.zip.GZIPOutputStream;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

import com.vladsch.flexmark.html.HtmlRenderer;
import com.vladsch.flexmark.parser.Parser;

/**
 * Agent-skill generator. Packages one documentation tree into a Claude skill -- a
 * {@code SKILL.md} plus the whole tree as bundled references -- and publishes it beside
 * the HTML site.
 *
 * <p>
 * The skill is GENERATED rather than written, so that the documentation stays the single
 * source of truth: the only hand-written text is {@code skill/SKILL.template.md} on the
 * classpath, which holds the frontmatter and the working rules and pulls the quickstart
 * in through {@code {{include:...}}}.
 *
 * <p>
 * Layout of the output directory:
 *
 * <pre>
 * &lt;out&gt;/
 *   index.html              install page
 *   VERSION, version.json   what a client polls to see whether it is stale
 *   yavi/SKILL.md           the skill
 *   yavi/references/**      the documentation tree, plus the generated INDEX
 *   yavi-skill.tar.gz       -&gt; ~/.claude/skills/
 *   yavi.skill              the same tree as a zip
 *   yavi-full.md            everything concatenated, for agents without a skill loader
 *   yavi-plugin.zip         the skill as a Claude Code plugin
 *   marketplace.json        the marketplace listing that plugin
 * </pre>
 *
 * <p>
 * The skill mirrors the newest documented version (the highest version in
 * {@code versions.yaml}), in the reference language. Its version is given by the caller,
 * who counts the commits that can change it; when the bundle is byte for byte the one
 * already published, that version is kept, so an install is not told to re-download
 * itself for a change that does not reach it.
 *
 * <p>
 * Usage:
 *
 * <pre>
 * java -jar yavi-docgen.jar skill --source doc-site/content --out _site/skill \
 *      --version 0.18.42 --commit abc1234 --site-base https://yavi.ik.am
 * </pre>
 */
public final class SkillGen {

	/** Directory name of the skill inside the bundle; also the skill's name. */
	static final String SKILL_NAME = "yavi";

	static final String TEMPLATE_RESOURCE = "/skill/SKILL.template.md";

	static final String INDEX_HTML_RESOURCE = "/skill/index.html";

	/** The version used when nothing supplies one (local runs outside CI). */
	static final String DEV_VERSION = "0.0.0-dev";

	/** The generated page listing every documentation page, under {@code references/}. */
	static final String CONTENTS_PAGE = "contents.md";

	/**
	 * The documentation page that IS the install page, relative to the language
	 * directory.
	 */
	static final String INSTALL_GUIDE = "agent-skill.md";

	/**
	 * The skill packaged as a Claude Code plugin, for an {@code archive} marketplace
	 * source.
	 */
	static final String PLUGIN_ZIP = SKILL_NAME + "-plugin.zip";

	/** The one-file marketplace that `claude plugin marketplace add <url>` reads. */
	static final String MARKETPLACE_JSON = "marketplace.json";

	/** The GitHub user the site and the repository belong to. */
	static final String OWNER = "making";

	private static final Pattern MD_LINK = Pattern.compile("\\[([^\\]]*)]\\(([^)\\s]+)\\)");

	private static final Pattern INCLUDE = Pattern.compile("\\{\\{include:([^}]+)}}");

	private final Path source;

	private final Path out;

	private final String docsVersion;

	private final String lang;

	private final String version;

	private final String commit;

	private final String siteBase;

	private final String yaviRelease;

	private final String previousVersion;

	private final String previousHash;

	public SkillGen(Path source, Path out, String docsVersion, String lang, String version, String commit,
			String siteBase, String previousVersion, String previousHash) throws IOException {
		this.source = source;
		this.out = out;
		this.lang = lang;
		Versions versions = Versions.load(source.resolve("versions.yaml"));
		this.docsVersion = (docsVersion == null) ? newestVersion(versions) : docsVersion;
		this.yaviRelease = versions.versions()
			.stream()
			.filter(v -> v.version().equals(this.docsVersion))
			.map(Versions.Version::yaviRelease)
			.findFirst()
			.orElseThrow(() -> new IOException("versions.yaml lists no version '" + this.docsVersion + "'"));
		this.version = version;
		this.commit = commit;
		this.siteBase = siteBase;
		this.previousVersion = previousVersion;
		this.previousHash = previousHash;
	}

	public static void main(String[] args) throws IOException {
		Path source = Path.of("doc-site", "content");
		Path out = Path.of("_site", "skill");
		String docsVersion = null;
		String lang = "en";
		String version = DEV_VERSION;
		String commit = "";
		String siteBase = "https://yavi.ik.am";
		String previousVersion = null;
		String previousHash = null;
		for (int i = 0; i < args.length - 1; i++) {
			switch (args[i]) {
				case "--source" -> source = Path.of(args[++i]).toAbsolutePath().normalize();
				case "--out" -> out = Path.of(args[++i]).toAbsolutePath().normalize();
				case "--docs-version" -> docsVersion = args[++i];
				case "--lang" -> lang = args[++i];
				case "--version" -> version = args[++i];
				case "--commit" -> commit = args[++i];
				case "--site-base" -> siteBase = args[++i];
				case "--previous-version" -> previousVersion = args[++i];
				case "--previous-hash" -> previousHash = args[++i];
				default -> {
				}
			}
		}
		new SkillGen(source, out, docsVersion, lang, version, commit, siteBase, previousVersion, previousHash)
			.generate();
	}

	/** The highest documented version: the tree the skill mirrors. */
	static String newestVersion(Versions versions) throws IOException {
		return versions.versions()
			.stream()
			.map(Versions.Version::version)
			.max(SkillGen::compareVersions)
			.orElseThrow(() -> new IOException("versions.yaml lists no versions"));
	}

	/** Compares dotted numeric versions; a non-numeric segment compares as text. */
	static int compareVersions(String left, String right) {
		String[] a = left.split("\\.");
		String[] b = right.split("\\.");
		for (int i = 0; i < Math.min(a.length, b.length); i++) {
			int compared;
			try {
				compared = Integer.compare(Integer.parseInt(a[i]), Integer.parseInt(b[i]));
			}
			catch (NumberFormatException e) {
				compared = a[i].compareTo(b[i]);
			}
			if (compared != 0) {
				return compared;
			}
		}
		return Integer.compare(a.length, b.length);
	}

	/** Writes the whole bundle. */
	public void generate() throws IOException {
		Path langDir = this.source.resolve(this.docsVersion).resolve(this.lang);
		if (!Files.exists(langDir.resolve("nav.yaml"))) {
			throw new IOException("No nav.yaml under " + langDir);
		}
		Nav nav = Nav.load(langDir.resolve("nav.yaml"));

		Path skillDir = this.out.resolve(SKILL_NAME);
		Path refDir = skillDir.resolve("references");
		deleteRecursively(skillDir);
		Files.createDirectories(refDir);

		Map<String, String> references = new LinkedHashMap<>();
		for (Map.Entry<String, String> page : readMarkdown(langDir).entrySet()) {
			references.put(page.getKey(), siteLinksForMirroredPage(page.getValue(), page.getKey()));
		}
		references.put(CONTENTS_PAGE, buildContents(nav));
		for (Map.Entry<String, String> entry : references.entrySet()) {
			Path target = refDir.resolve(entry.getKey());
			Files.createDirectories(target.getParent());
			Files.writeString(target, entry.getValue(), StandardCharsets.UTF_8);
		}

		// What the skill IS decides the version, not how many commits went by: a change
		// to a test, or to a page of another version, produces the same bytes here, and
		// an install that re-downloads for that is noise.
		String digest = contentDigest(references, renderSkill(langDir, nav, ""));
		String resolved = digest.equals(this.previousHash) ? this.previousVersion : this.version;

		String skill = renderSkill(langDir, nav, resolved);
		Files.writeString(skillDir.resolve("SKILL.md"), skill, StandardCharsets.UTF_8);

		Files.writeString(this.out.resolve("VERSION"), resolved + "\n", StandardCharsets.UTF_8);
		Files.writeString(this.out.resolve("version.json"), versionJson(resolved, digest), StandardCharsets.UTF_8);
		Files.writeString(this.out.resolve(SKILL_NAME + "-full.md"), buildSingleFile(skill, references, resolved),
				StandardCharsets.UTF_8);
		Files.writeString(this.out.resolve("index.html"), renderInstallPage(langDir, resolved), StandardCharsets.UTF_8);

		Map<String, byte[]> archive = new LinkedHashMap<>();
		archive.put(SKILL_NAME + "/SKILL.md", skill.getBytes(StandardCharsets.UTF_8));
		for (Map.Entry<String, String> entry : references.entrySet()) {
			archive.put(SKILL_NAME + "/references/" + entry.getKey(),
					entry.getValue().getBytes(StandardCharsets.UTF_8));
		}
		writeTarGz(this.out.resolve(SKILL_NAME + "-skill.tar.gz"), archive);
		writeZip(this.out.resolve(SKILL_NAME + ".skill"), archive);

		String summary = frontmatterDescription(skill);
		Map<String, byte[]> plugin = new LinkedHashMap<>();
		String homepage = homepage();
		plugin.put(".claude-plugin/plugin.json",
				pluginJson(SKILL_NAME, summary, resolved, homepage).getBytes(StandardCharsets.UTF_8));
		for (Map.Entry<String, byte[]> entry : archive.entrySet()) {
			plugin.put("skills/" + entry.getKey(), entry.getValue());
		}
		writeZip(this.out.resolve(PLUGIN_ZIP), plugin);
		Files.writeString(this.out.resolve(MARKETPLACE_JSON),
				marketplaceJson(resolved, marketplaceEntry(SKILL_NAME, summary, resolved, homepage)),
				StandardCharsets.UTF_8);

		System.out.println("Generated skill " + SKILL_NAME + " " + resolved
				+ (resolved.equals(this.version) ? "" : " (unchanged content; " + this.version + " not published)")
				+ " (" + references.size() + " reference files) into " + this.out);
	}

	// --- SKILL.md ---------------------------------------------------------

	private String renderSkill(Path langDir, Nav nav, String version) throws IOException {
		String template = readResource(TEMPLATE_RESOURCE);
		String body = substituteIncludes(template, langDir);
		body = body.replace("{{version}}", version)
			.replace("{{docs-version}}", this.docsVersion)
			.replace("{{lang}}", this.lang)
			.replace("{{site-base}}", this.siteBase)
			.replace("{{yavi-release}}", this.yaviRelease)
			.replace("{{contents-page}}", CONTENTS_PAGE)
			.replace("{{nav-table}}", buildNavTable(nav));
		if (body.contains("{{")) {
			throw new IOException("Unsubstituted placeholder in " + TEMPLATE_RESOURCE + ": "
					+ body.substring(body.indexOf("{{"), Math.min(body.length(), body.indexOf("{{") + 40)));
		}
		return body;
	}

	/**
	 * Inlines {@code {{include:PATH}}} with the documentation page at PATH. The page is
	 * demoted one heading level so it nests under the skill, and its relative links are
	 * retargeted at the bundled copy of whatever they pointed at -- which is what keeps
	 * the inlined text a view of the documentation rather than a fork of it.
	 */
	private String substituteIncludes(String template, Path langDir) throws IOException {
		Matcher matcher = INCLUDE.matcher(template);
		StringBuilder out = new StringBuilder();
		while (matcher.find()) {
			String relative = matcher.group(1).trim();
			Path page = langDir.resolve(relative);
			if (!Files.exists(page)) {
				throw new IOException("{{include:" + relative + "}} has no source page: " + page);
			}
			String markdown = substituteRelease(Files.readString(page, StandardCharsets.UTF_8));
			markdown = retargetLinks(demoteHeadings(markdown), relative);
			matcher.appendReplacement(out, Matcher.quoteReplacement(markdown.strip()));
		}
		matcher.appendTail(out);
		return out.toString();
	}

	/** A page may name the YAVI release its tree embeds; the skill names the same one. */
	private String substituteRelease(String markdown) {
		return markdown.replace("{{yavi_release}}", this.yaviRelease);
	}

	/**
	 * A digest of everything the bundle contains except the version itself, so that two
	 * runs over the same documentation agree no matter how many commits went by in
	 * between.
	 */
	static String contentDigest(Map<String, String> references, String skillWithoutVersion) {
		try {
			java.security.MessageDigest sha = java.security.MessageDigest.getInstance("SHA-256");
			for (Map.Entry<String, String> entry : new TreeMap<>(references).entrySet()) {
				sha.update(entry.getKey().getBytes(StandardCharsets.UTF_8));
				sha.update((byte) 0);
				sha.update(entry.getValue().getBytes(StandardCharsets.UTF_8));
				sha.update((byte) 0);
			}
			sha.update(skillWithoutVersion.getBytes(StandardCharsets.UTF_8));
			StringBuilder hex = new StringBuilder();
			for (byte b : sha.digest()) {
				hex.append(Character.forDigit((b >> 4) & 0xf, 16)).append(Character.forDigit(b & 0xf, 16));
			}
			return hex.toString();
		}
		catch (java.security.NoSuchAlgorithmException e) {
			throw new IllegalStateException("SHA-256 is unavailable", e);
		}
	}

	/** Adds one {@code #} to every ATX heading. */
	static String demoteHeadings(String markdown) {
		return markdown.replaceAll("(?m)^(#{1,5} )", "#$1");
	}

	/**
	 * Rewrites the relative links of a page being inlined into {@code SKILL.md}, so that
	 * they resolve from the bundle root instead of from the page's own directory.
	 * Absolute and anchor-only links are left alone.
	 */
	String retargetLinks(String markdown, String pageRelativePath) {
		return mapLinks(markdown, pageRelativePath,
				(inTree, resolved) -> inTree ? "references/" + resolved : siteUrl(resolved));
	}

	/**
	 * Rewrites the links of a page being MIRRORED into the bundle. The tree keeps its
	 * shape, so a link that stays inside it still resolves as written; one that escapes
	 * it only exists on the web, and is turned into the URL it means.
	 */
	String siteLinksForMirroredPage(String markdown, String pageRelativePath) {
		return mapLinks(substituteRelease(markdown), pageRelativePath,
				(inTree, resolved) -> inTree ? null : siteUrl(resolved));
	}

	/**
	 * Applies {@code mapper} to every real relative link. The mapper receives whether the
	 * target resolves inside the language tree and its path relative to that tree's root,
	 * and returns the new target (or null to leave the link alone).
	 */
	private String mapLinks(String markdown, String pageRelativePath,
			java.util.function.BiFunction<Boolean, String, String> mapper) {
		String pageDir = pageRelativePath.contains("/")
				? pageRelativePath.substring(0, pageRelativePath.lastIndexOf('/')) + "/" : "";
		boolean[] code = codeMask(markdown);
		Matcher matcher = MD_LINK.matcher(markdown);
		StringBuilder out = new StringBuilder();
		while (matcher.find()) {
			String target = matcher.group(2);
			String replacement = matcher.group(0);
			if (!code[matcher.start(2)] && !target.startsWith("#") && !target.contains("://")
					&& !target.startsWith("mailto:")) {
				int hash = target.indexOf('#');
				String file = hash < 0 ? target : target.substring(0, hash);
				String anchor = hash < 0 ? "" : target.substring(hash);
				String resolved = normalize(pageDir + file);
				boolean inTree = !resolved.startsWith("..");
				String mapped = mapper.apply(inTree, resolved);
				if (mapped != null) {
					replacement = "[" + matcher.group(1) + "](" + mapped + anchor + ")";
				}
			}
			matcher.appendReplacement(out, Matcher.quoteReplacement(replacement));
		}
		matcher.appendTail(out);
		return out.toString();
	}

	/** A Markdown source path as the site publishes it. */
	private static String asHtml(String path) {
		return path.endsWith(".md") ? path.substring(0, path.length() - 3) + ".html" : path;
	}

	/** The published URL of a path that climbed out of the language tree. */
	private String siteUrl(String resolvedFromLangRoot) {
		return this.siteBase + "/" + this.docsVersion + "/" + this.lang + "/" + asHtml(resolvedFromLangRoot);
	}

	/**
	 * Marks every character that sits inside a fenced block or an inline code span, so
	 * that link handling can skip text that only LOOKS like a link -- these pages quote
	 * Java that contains {@code ](...)}. A code span inside a link label is left alone:
	 * what disqualifies a match is its target being inside code, not its label.
	 */
	static boolean[] codeMask(String markdown) {
		boolean[] mask = new boolean[markdown.length()];
		boolean inFence = false;
		int lineStart = 0;
		while (lineStart <= markdown.length()) {
			int newline = markdown.indexOf('\n', lineStart);
			int lineEnd = newline < 0 ? markdown.length() : newline;
			String line = markdown.substring(lineStart, lineEnd);
			if (line.stripLeading().startsWith("```") || line.stripLeading().startsWith("~~~")) {
				inFence = !inFence;
				java.util.Arrays.fill(mask, lineStart, lineEnd, true);
			}
			else if (inFence) {
				java.util.Arrays.fill(mask, lineStart, lineEnd, true);
			}
			else {
				for (int i = lineStart; i < lineEnd; i++) {
					if (markdown.charAt(i) != '`') {
						continue;
					}
					int close = markdown.indexOf('`', i + 1);
					if (close < 0 || close >= lineEnd) {
						break;
					}
					java.util.Arrays.fill(mask, i, close + 1, true);
					i = close;
				}
			}
			if (newline < 0) {
				break;
			}
			lineStart = newline + 1;
		}
		return mask;
	}

	/** Collapses {@code a/b/../c} to {@code a/c} without touching the filesystem. */
	static String normalize(String path) {
		List<String> parts = new ArrayList<>();
		for (String segment : path.split("/")) {
			if (segment.isEmpty() || segment.equals(".")) {
				continue;
			}
			if (segment.equals("..") && !parts.isEmpty() && !parts.getLast().equals("..")) {
				parts.removeLast();
			}
			else {
				parts.add(segment);
			}
		}
		return String.join("/", parts);
	}

	// --- generated reference pages ---------------------------------------

	/** Every documentation page by title, in navigation order. */
	static String buildContents(Nav nav) {
		StringBuilder md = new StringBuilder("""
				# Documentation index

				Every page bundled with this skill, in the order the documentation site
				presents it. Paths are relative to this file. The site publishes the same
				pages, with the runnable examples live.
				""");
		for (Nav.Section section : nav.sections()) {
			md.append("\n## ").append(section.title()).append("\n\n");
			appendLinks(md, section.pages());
		}
		return md.toString();
	}

	private static void appendLinks(StringBuilder md, List<Nav.Page> pages) {
		for (Nav.Page page : pages) {
			md.append("- [").append(page.title()).append("](").append(page.file()).append(")\n");
			appendLinks(md, page.subpages());
		}
	}

	/**
	 * The pages as a table for SKILL.md, grouped by the section the site shows them in.
	 */
	private static String buildNavTable(Nav nav) {
		StringBuilder md = new StringBuilder("| Section | Page | Reference |\n| --- | --- | --- |\n");
		for (Nav.Section section : nav.sections()) {
			appendSectionRows(md, section, section.pages());
		}
		return md.toString().stripTrailing();
	}

	private static void appendSectionRows(StringBuilder md, Nav.Section section, List<Nav.Page> pages) {
		for (Nav.Page page : pages) {
			md.append("| ")
				.append(section.title())
				.append(" | ")
				.append(page.title())
				.append(" | `references/")
				.append(page.file())
				.append("` |\n");
			appendSectionRows(md, section, page.subpages());
		}
	}

	// --- other outputs ----------------------------------------------------

	private String buildSingleFile(String skill, Map<String, String> references, String version) {
		StringBuilder md = new StringBuilder();
		md.append("<!-- yavi skill ").append(version).append(" -- ").append(this.siteBase).append(" -->\n\n");
		md.append(skill);
		for (Map.Entry<String, String> entry : references.entrySet()) {
			md.append("\n\n---\n\n# FILE: references/").append(entry.getKey()).append("\n\n").append(entry.getValue());
		}
		return md.toString();
	}

	/**
	 * The skill's own frontmatter description, collapsed to one line. Reused as the
	 * plugin's and the marketplace's description so that what the skill says it is for is
	 * stated once.
	 */
	static String frontmatterDescription(String skill) throws IOException {
		Matcher folded = Pattern.compile("(?ms)^description:[ \t]*>?-?[ \t]*\r?\n(.*?)^\\w").matcher(skill);
		Matcher inline = Pattern.compile("(?m)^description:[ \t]*([^>|\\s].*)$").matcher(skill);
		String text;
		if (inline.find()) {
			text = inline.group(1);
		}
		else if (folded.find()) {
			text = folded.group(1);
		}
		else {
			throw new IOException("The skill has no `description:` in its frontmatter");
		}
		return text.replaceAll("\\s+", " ").trim();
	}

	/** The install page's URL, and the plugin's homepage. */
	private String homepage() {
		return this.siteBase + "/" + this.docsVersion + "/" + this.lang + "/" + asHtml(INSTALL_GUIDE);
	}

	private String pluginJson(String name, String summary, String version, String homepage) {
		return """
				{
				  "name": "%s",
				  "description": "%s",
				  "version": "%s",
				  "author": {
				    "name": "%s"
				  },
				  "license": "Apache-2.0",
				  "homepage": "%s"
				}
				""".formatted(name, jsonEscape(summary), version, OWNER, homepage);
	}

	/**
	 * A marketplace served as one static file. Because it is added by URL, Claude Code
	 * downloads nothing but this JSON -- a relative plugin source would have no checkout
	 * to be relative to -- so the plugin is an absolute {@code archive} URL. That URL is
	 * deliberately unversioned: a client holding a stale copy of this file still
	 * resolves, and the version it then installs is the one inside the archive's
	 * {@code plugin.json}.
	 */
	private String marketplaceJson(String version, String entry) {
		return """
				{
				  "name": "%s",
				  "owner": {
				    "name": "%s"
				  },
				  "description": "Agent skills from the YAVI repository",
				  "version": "%s",
				  "plugins": [
				%s
				  ]
				}
				""".formatted(SKILL_NAME, OWNER, version, entry);
	}

	/** One plugin of the marketplace, fetched as the archive of that name next to it. */
	private String marketplaceEntry(String name, String summary, String version, String homepage) {
		return """
				{
				  "name": "%s",
				  "description": "%s",
				  "version": "%s",
				  "homepage": "%s",
				  "source": {
				    "source": "archive",
				    "url": "%s/skill/%s-plugin.zip"
				  }
				}""".formatted(name, jsonEscape(summary), version, homepage, this.siteBase, name)
			.indent(4)
			.stripTrailing();
	}

	private static String jsonEscape(String text) {
		return text.replace("\\", "\\\\").replace("\"", "\\\"");
	}

	private String versionJson(String version, String digest) {
		return """
				{
				  "name": "%s",
				  "version": "%s",
				  "commit": "%s",
				  "contentHash": "%s",
				  "docs": "%s/%s/%s/"
				}
				""".formatted(SKILL_NAME, version, this.commit, digest, this.siteBase, this.docsVersion, this.lang);
	}

	/**
	 * The install page is the manual's own Agent Skill guide, rendered into the bundle's
	 * chrome: the instructions someone follows to install the skill exist once, as a
	 * documentation page, not once here and once there.
	 */
	private String renderInstallPage(Path langDir, String version) throws IOException {
		Path guide = langDir.resolve(INSTALL_GUIDE);
		if (!Files.exists(guide)) {
			throw new IOException("The install page needs " + guide);
		}
		// The page sits at /skill/, not among the docs, so what it links to has to
		// be named by URL rather than by neighbourhood.
		String markdown = mapLinks(substituteRelease(Files.readString(guide, StandardCharsets.UTF_8)), INSTALL_GUIDE,
				(inTree, resolved) -> inTree
						? this.siteBase + "/" + this.docsVersion + "/" + this.lang + "/" + asHtml(resolved)
						: siteUrl(resolved));
		String body = HtmlRenderer.builder(Markdown.options())
			.build()
			.render(Parser.builder(Markdown.options()).build().parse(markdown));
		return readResource(INDEX_HTML_RESOURCE).replace("{{version}}", version)
			.replace("{{site-base}}", this.siteBase)
			.replace("{{docs-version}}", this.docsVersion)
			.replace("{{lang}}", this.lang)
			.replace("{{body}}", body);
	}

	// --- filesystem helpers ----------------------------------------------

	/** Reads every Markdown page under the language directory, keyed by relative path. */
	private static Map<String, String> readMarkdown(Path langDir) throws IOException {
		Map<String, String> pages = new LinkedHashMap<>();
		try (Stream<Path> files = Files.walk(langDir)) {
			files.filter(Files::isRegularFile)
				.filter(p -> p.getFileName().toString().endsWith(".md"))
				.sorted()
				.forEach(p -> {
					try {
						pages.put(langDir.relativize(p).toString().replace('\\', '/'),
								Files.readString(p, StandardCharsets.UTF_8));
					}
					catch (IOException e) {
						throw new UncheckedIOException(e);
					}
				});
		}
		return pages;
	}

	private static String readResource(String name) throws IOException {
		try (InputStream in = SkillGen.class.getResourceAsStream(name)) {
			if (in == null) {
				throw new IOException("Missing classpath resource: " + name);
			}
			return new String(in.readAllBytes(), StandardCharsets.UTF_8);
		}
	}

	private static void deleteRecursively(Path dir) throws IOException {
		if (!Files.exists(dir)) {
			return;
		}
		try (Stream<Path> paths = Files.walk(dir)) {
			paths.sorted(java.util.Comparator.reverseOrder()).forEach(p -> {
				try {
					Files.delete(p);
				}
				catch (IOException e) {
					throw new UncheckedIOException(e);
				}
			});
		}
	}

	// --- archives ---------------------------------------------------------
	//
	// Both archives are written with a fixed timestamp so that regenerating an unchanged
	// bundle produces identical bytes: a client comparing checksums should see movement
	// only when the documentation actually moved.

	private static void writeZip(Path target, Map<String, byte[]> entries) throws IOException {
		try (ZipOutputStream zip = new ZipOutputStream(Files.newOutputStream(target))) {
			for (Map.Entry<String, byte[]> entry : entries.entrySet()) {
				ZipEntry zipEntry = new ZipEntry(entry.getKey());
				zipEntry.setTime(0L);
				zip.putNextEntry(zipEntry);
				zip.write(entry.getValue());
				zip.closeEntry();
			}
		}
	}

	private static void writeTarGz(Path target, Map<String, byte[]> entries) throws IOException {
		ByteArrayOutputStream tar = new ByteArrayOutputStream();
		for (Map.Entry<String, byte[]> entry : entries.entrySet()) {
			writeTarEntry(tar, entry.getKey(), entry.getValue());
		}
		tar.write(new byte[1024]); // two zero blocks terminate the archive
		try (OutputStream gzip = new GZIPOutputStream(Files.newOutputStream(target))) {
			gzip.write(tar.toByteArray());
		}
	}

	/** Writes one ustar header plus the padded file content. */
	private static void writeTarEntry(OutputStream out, String name, byte[] content) throws IOException {
		byte[] nameBytes = name.getBytes(StandardCharsets.UTF_8);
		if (nameBytes.length > 100) {
			throw new IOException("Path too long for a ustar header (100 bytes max): " + name);
		}
		byte[] header = new byte[512];
		System.arraycopy(nameBytes, 0, header, 0, nameBytes.length);
		putField(header, 100, 8, "0000644");
		putField(header, 108, 8, "0000000");
		putField(header, 116, 8, "0000000");
		putField(header, 124, 12, String.format("%011o", content.length));
		putField(header, 136, 12, "00000000000");
		header[156] = '0'; // regular file
		System.arraycopy("ustar 00".getBytes(StandardCharsets.US_ASCII), 0, header, 257, 8);
		// The checksum is computed with its own field read as spaces.
		java.util.Arrays.fill(header, 148, 156, (byte) ' ');
		int checksum = 0;
		for (byte b : header) {
			checksum += b & 0xff;
		}
		putField(header, 148, 8, String.format("%06o", checksum));
		header[154] = 0;
		header[155] = ' ';
		out.write(header);
		out.write(content);
		int padding = (512 - (content.length % 512)) % 512;
		out.write(new byte[padding]);
	}

	private static void putField(byte[] header, int offset, int length, String value) {
		byte[] bytes = value.getBytes(StandardCharsets.US_ASCII);
		System.arraycopy(bytes, 0, header, offset, Math.min(bytes.length, length - 1));
	}

}
