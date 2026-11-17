/*
 * SPDX-FileCopyrightText: 2018-2026 Toshiaki Maki <makingx@gmail.com>
 * SPDX-License-Identifier: Apache-2.0
 */
package am.ik.yavi.docgen;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.Map;
import java.util.stream.Stream;

import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class DocGenTest {

	private static final Path FIXTURE = Path.of("src", "test", "resources", "fixture");

	@TempDir
	static Path site;

	@BeforeAll
	static void generateSite() throws IOException {
		new DocGen(FIXTURE, site).generate();
	}

	/**
	 * The output tree, the version and language switchers, nav parsing (including a
	 * sub-page that is rendered but is not a sidebar row), link rewriting and the search
	 * index files.
	 */
	@Test
	void buildsTheSiteAndSwitchesVersionAndLanguage() throws IOException {
		assertThat(Files.readString(site.resolve("index.html"))).contains("url=1.0/en/index.html");
		// Every top-level page keeps the unversioned root URL it had on the old site.
		assertThat(Files.readString(site.resolve("guide.html"))).contains("url=1.0/en/guide.html")
			.contains("<title>Guide - Fixture documentation</title>");

		Map<String, Object> versions = loadJson(site.resolve("versions.json"));
		assertThat(versions.get("default_version")).isEqualTo("1.0");
		assertThat(versions.get("default_language")).isEqualTo("en");
		assertThat((List<?>) versions.get("versions")).hasSize(2);
		assertThat((List<?>) versions.get("languages")).hasSize(2);

		String runtime = Files.readString(site.resolve("assets/runtime/1.0/yavi-runtime.js"), StandardCharsets.UTF_8);
		assertThat(runtime).contains("window.YaviRuntime").contains("run: function").contains("not available yet");
		assertThat(site.resolve("assets/docs.js")).exists();
		assertThat(site.resolve("assets/docs.css")).exists();

		String guide = Files.readString(site.resolve("1.0/en/guide.html"), StandardCharsets.UTF_8);
		String sidebar = guide.substring(guide.indexOf("<aside class=\"sidebar\""), guide.indexOf("</aside>"));
		// nav.yaml is parsed into the sidebar; the sub-page is rendered but is not a row.
		assertThat(sidebar).contains("<div class=\"nav-section-title\">Guide</div>")
			.contains("<a class=\"nav-link\" href=\"index.html\">Overview</a>")
			.contains("<a class=\"nav-link active\" href=\"guide.html\" aria-current=\"page\">Guide</a>")
			.contains("<a class=\"nav-link\" href=\"code.html\">Code</a>")
			.doesNotContain("deep");
		// A version switch prefers the same page and falls back to the version's index
		// when the page does not exist there; a language switch stays in this version.
		assertThat(guide).contains("<a class=\"switch\" href=\"../../2.0/en/index.html\">2.0</a>")
			.contains("<span class=\"switch active\">1.0</span>")
			.contains("<a class=\"switch\" href=\"../ja/guide.html\">日本語</a>")
			.contains("data-runtime-src=\"../../assets/runtime/1.0/yavi-runtime.js\"")
			.contains("data-search-base=\".\"")
			// Markdown links are rewritten to .html
			.contains("href=\"index.html\"")
			.contains("href=\"guide/deep.html\"");
		// The theme switcher sits in the header with an English label, and the head
		// script that replays the stored choice precedes the stylesheet.
		assertThat(guide).contains("<button type=\"button\" class=\"theme-open\" aria-label=\"Color theme: System\"")
			.contains("aria-expanded=\"false\" aria-controls=\"theme-menu\"")
			.contains("data-theme-value=\"system\" aria-pressed=\"true\" title=\"System\"")
			.contains("data-theme-value=\"light\" aria-pressed=\"false\" title=\"Light\"")
			.contains("data-theme-value=\"dark\" aria-pressed=\"false\" title=\"Dark\"");
		assertThat(guide.indexOf("<script>(function(){try{var m=localStorage.getItem(\"yavi-doc-theme\");"))
			.isGreaterThanOrEqualTo(0)
			.isLessThan(guide.indexOf("<link rel=\"stylesheet\""));

		String deep = Files.readString(site.resolve("1.0/en/guide/deep.html"), StandardCharsets.UTF_8);
		assertThat(deep).contains("<a class=\"backlink\" href=\"../guide.html\">&larr; Guide</a>")
			.contains("<a class=\"nav-link active\" href=\"../guide.html\" aria-current=\"page\">Guide</a>")
			.contains("data-search-base=\"..\"");

		// The Japanese page keeps its translated headings but adopts the reference
		// language's anchors, so a cross-page link works in every tree.
		String jaGuide = Files.readString(site.resolve("1.0/ja/guide.html"), StandardCharsets.UTF_8);
		assertThat(jaGuide).contains("<h2 id=\"options\">オプション</h2>")
			.contains("<h2 id=\"result\">結果</h2>")
			.contains("<a class=\"switch\" href=\"../../2.0/ja/index.html\">2.0</a>")
			// The theme control is translated for the Japanese tree.
			.contains("aria-label=\"カラーテーマ: システム\"")
			.contains("data-theme-value=\"dark\" aria-pressed=\"false\" title=\"ダーク\"")
			.contains("<span class=\"theme-option-label\">ライト</span>")
			.contains("localStorage.getItem(\"yavi-doc-theme\")");

		// A page that exists in the other version is linked directly, not by fallback.
		String code = Files.readString(site.resolve("1.0/en/code.html"), StandardCharsets.UTF_8);
		assertThat(code).contains("<a class=\"switch\" href=\"../../2.0/en/code.html\">2.0</a>");

		for (String lang : List.of("en", "ja")) {
			assertThat(loadJson(site.resolve("1.0/" + lang + "/search-index.json"))).containsKeys("pages");
			assertThat(loadJson(site.resolve("1.0/" + lang + "/search-body.json"))).containsKeys("s");
		}
	}

	/**
	 * A translation whose heading layout drifted would ship anchors that point at the
	 * wrong sections, so the build refuses it.
	 */
	@Test
	void aTranslationWithADifferentHeadingLayoutFailsTheBuild(@TempDir Path tmp) throws IOException {
		Path content = copyFixture(tmp.resolve("content"));
		Path jaGuide = content.resolve("1.0/ja/guide.md");
		Files.writeString(jaGuide, Files.readString(jaGuide, StandardCharsets.UTF_8) + "\n## 余分\n\n本文\n",
				StandardCharsets.UTF_8);
		assertThatThrownBy(() -> new DocGen(content, tmp.resolve("out")).generate()).isInstanceOf(IOException.class)
			.hasMessageContaining("1.0/ja/guide.md")
			.hasMessageContaining("4 headings vs 3");
	}

	/** A version listed in versions.yaml but missing a nav.yaml is a build error. */
	@Test
	void aListedVersionWithoutNavYamlFailsTheBuild(@TempDir Path tmp) throws IOException {
		Path content = copyFixture(tmp.resolve("content"));
		Files.delete(content.resolve("2.0/en/nav.yaml"));
		assertThatThrownBy(() -> new DocGen(content, tmp.resolve("out")).generate()).isInstanceOf(IOException.class)
			.hasMessageContaining("nav.yaml");
	}

	/**
	 * Only the {@code java runnable} fence becomes a cell; a plain java block stays
	 * static.
	 */
	@Test
	void runnableJavaBlocksBecomeCellsAndOtherBlocksStayStatic() throws IOException {
		String code = Files.readString(site.resolve("1.0/en/code.html"), StandardCharsets.UTF_8);
		assertThat(code).contains("<div class=\"code-cell\" data-lang=\"java\">")
			.contains("<button class=\"run\" type=\"button\">Run</button>")
			.contains("<textarea class=\"cell-src\" spellcheck=\"false\" wrap=\"off\" rows=\"7\""
					+ " aria-label=\"Editable Java source\">public class Main")
			.contains("<pre class=\"cell-out\" hidden></pre>")
			// A plain ```java block stays a static block, is highlighted at build time,
			// and gets its copy button from docs.js.
			.contains("<pre><code class=\"language-java\"><span class=\"tok-key\">int</span> x = "
					+ "<span class=\"tok-num\">1</span>;");
		// Exactly one cell on the page.
		assertThat(code.split("class=\"code-cell\"", -1)).hasSize(2);
		// A page without a cell carries only the lazy data attribute, never an eager
		// <script> tag for the runtime.
		String index = Files.readString(site.resolve("1.0/en/index.html"), StandardCharsets.UTF_8);
		assertThat(index).doesNotContain("class=\"code-cell\"")
			.doesNotContain("<script src=\"../../assets/runtime/1.0/yavi-runtime.js\"")
			.contains("data-runtime-src=\"../../assets/runtime/1.0/yavi-runtime.js\"");
	}

	/**
	 * A runtime built for a version replaces the stub for that version only; versions
	 * without a build keep the stub, whose cells explain that execution is unavailable.
	 */
	@Test
	void aBuiltRuntimeReplacesTheStubPerVersion(@TempDir Path tmp) throws IOException {
		Path runtime = Files.createDirectories(tmp.resolve("runtime/1.0"));
		Files.writeString(runtime.resolve("yavi-runtime.js"), "window.YaviRuntime = { real: true };");
		Files.writeString(runtime.resolve("yavi-runtime-wasm.js"), "// launcher");
		Files.writeString(runtime.resolve("yavi-runtime-wasm.js.wasm"), "wasm");
		Path out = tmp.resolve("site");
		new DocGen(FIXTURE, out, tmp.resolve("runtime")).generate();
		assertThat(Files.readString(out.resolve("assets/runtime/1.0/yavi-runtime.js"))).contains("real: true");
		assertThat(out.resolve("assets/runtime/1.0/yavi-runtime-wasm.js.wasm")).hasContent("wasm");
		assertThat(Files.readString(out.resolve("assets/runtime/2.0/yavi-runtime.js")))
			.contains("Code execution is not available yet");
	}

	@Test
	void markdownLinksAreRewrittenToHtml() {
		assertThat(DocGen.rewriteMarkdownLinks("<a href=\"guide.md\">guide</a>"))
			.isEqualTo("<a href=\"guide.html\">guide</a>");
		assertThat(DocGen.rewriteMarkdownLinks("<a href=\"guide/deep.md#options\">deep</a>"))
			.isEqualTo("<a href=\"guide/deep.html#options\">deep</a>");
		// An absolute URL ending in .md is a file elsewhere, with no .html beside it.
		String published = "<a href=\"https://example.com/notes.md\">notes</a>";
		assertThat(DocGen.rewriteMarkdownLinks(published)).isEqualTo(published);
		assertThat(DocGen.rewriteMarkdownLinks("<a href=\"//cdn.example/x.md\">x</a>"))
			.isEqualTo("<a href=\"//cdn.example/x.md\">x</a>");
	}

	@Test
	void searchIndexSplitsAPageIntoSectionsKeyedToItsAnchors() {
		SearchIndex index = new SearchIndex("en");
		index.addPage("guide.html", "Guide", """
				<h1 id="guide">Guide</h1>
				<p>lead <code>x</code></p>
				<h2 id="options">Options</h2>
				<p>option text</p>
				""");
		assertThat(index.tier1()).contains("\"p\":\"guide.html\"", "\"t\":\"Guide\"",
				"[[\"guide\",\"Guide\"],[\"options\",\"Options\"]]");
		assertThat(index.tier2()).contains("[0,0,\"lead x\"]", "[0,1,\"option text\"]");
		assertThat(SearchIndex.quote("a\"b\\c")).isEqualTo("\"a\\\"b\\\\c\"");
		assertThat(SearchIndex.plainText("<ul><li>one</li><li>two</li></ul>")).isEqualTo("one two");
	}

	/**
	 * The shipped 0.17 content builds: ten pages in each language, with the search index
	 * and the redirect to the default version.
	 */
	@Test
	void theShippedContentBuilds(@TempDir Path tmp) throws IOException {
		new DocGen(Path.of("content"), tmp.resolve("site")).generate();
		assertThat(countPages(tmp.resolve("site/0.17/en"))).isEqualTo(10);
		assertThat(countPages(tmp.resolve("site/0.17/ja"))).isEqualTo(10);
		assertThat(tmp.resolve("site/0.17/en/search-index.json")).exists();
		assertThat(tmp.resolve("site/0.17/ja/search-index.json")).exists();
		assertThat(Files.readString(tmp.resolve("site/index.html"))).contains("url=0.17/en/index.html");
		assertThat(Files.readString(tmp.resolve("site/getting-started.html")))
			.contains("url=0.17/en/getting-started.html");
		assertThat(Files.readString(tmp.resolve("site/built-in-constraints.html")))
			.contains("url=0.17/en/built-in-constraints.html");
		for (String lang : List.of("en", "ja")) {
			String page = Files.readString(tmp.resolve("site/0.17/" + lang + "/getting-started.html"),
					StandardCharsets.UTF_8);
			// The self-contained example is marked runnable in both languages.
			assertThat(page).contains("<div class=\"code-cell\" data-lang=\"java\">")
				.contains("<button class=\"run\" type=\"button\">Run</button>");
		}
	}

	/** A relative link to a page the tree does not publish fails the build. */
	@Test
	void aLinkToAMissingPageFailsTheBuild(@TempDir Path tmp) throws IOException {
		Path content = copyFixture(tmp.resolve("content"));
		Path index = content.resolve("1.0/en/index.md");
		Files.writeString(index, Files.readString(index, StandardCharsets.UTF_8) + "\n[gone](missing.md)\n",
				StandardCharsets.UTF_8);
		assertThatThrownBy(() -> new DocGen(content, tmp.resolve("out")).generate()).isInstanceOf(IOException.class)
			.hasMessageContaining("missing.md")
			.hasMessageContaining("1.0/en/index.md");
	}

	private static long countPages(Path dir) throws IOException {
		try (Stream<Path> pages = Files.walk(dir)) {
			return pages.filter((page) -> page.toString().endsWith(".html")).count();
		}
	}

	private static Path copyFixture(Path target) throws IOException {
		try (Stream<Path> files = Files.walk(FIXTURE)) {
			for (Path source : files.toList()) {
				Path dest = target.resolve(FIXTURE.relativize(source).toString());
				if (Files.isDirectory(source)) {
					Files.createDirectories(dest);
				}
				else {
					Files.createDirectories(dest.getParent());
					Files.copy(source, dest);
				}
			}
		}
		return target;
	}

	/** JSON is YAML, and snakeyaml is already here for the nav and the configuration. */
	private static Map<String, Object> loadJson(Path file) throws IOException {
		return new org.yaml.snakeyaml.Yaml().load(Files.readString(file, StandardCharsets.UTF_8));
	}

}
