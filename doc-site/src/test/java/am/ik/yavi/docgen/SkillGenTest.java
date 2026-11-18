/*
 * SPDX-FileCopyrightText: 2018-2026 Toshiaki Maki <makingx@gmail.com>
 * SPDX-License-Identifier: Apache-2.0
 */
package am.ik.yavi.docgen;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import static org.assertj.core.api.Assertions.assertThat;

class SkillGenTest {

	private static final Path CONTENT = Path.of("content");

	private static final String SITE = "https://example.test";

	/**
	 * The skill, its bundled documentation, the plugin and the marketplace, built from
	 * the documentation that ships with the site: the quickstart is inlined with the
	 * release of the tree, and every page is mirrored at its own path.
	 */
	@Test
	void packagesTheShippedDocumentationAsASkill(@TempDir Path out) throws IOException {
		String release = Versions.load(CONTENT.resolve("versions.yaml"))
			.versions()
			.stream()
			.filter(version -> version.version().equals("0.18"))
			.findFirst()
			.orElseThrow()
			.yaviRelease();
		new SkillGen(CONTENT, out, "0.18", "en", "0.18.7", "abc1234", SITE, null, null).generate();

		String skill = Files.readString(out.resolve("yavi/SKILL.md"), StandardCharsets.UTF_8);
		assertThat(skill).startsWith("---\nname: yavi\n")
			.contains("<version>" + release + "</version>")
			.contains("## Getting Started")
			.contains("references/getting-started.md")
			.contains("| Agent Skill | `references/agent-skill.md` |")
			.doesNotContain("{{");
		assertThat(out.resolve("yavi/references/built-in-constraints.md")).exists();
		assertThat(Files.readString(out.resolve("yavi/references/contents.md"), StandardCharsets.UTF_8))
			.contains("(agent-skill.md)");
		// The mirrored page names the same release as the site.
		assertThat(Files.readString(out.resolve("yavi/references/getting-started.md"), StandardCharsets.UTF_8))
			.contains("<version>" + release + "</version>")
			.doesNotContain("{{yavi_release}}");

		assertThat(out.resolve("yavi-skill.tar.gz")).exists();
		assertThat(out.resolve("yavi.skill")).exists();
		assertThat(out.resolve("yavi-plugin.zip")).exists();
		assertThat(Files.readString(out.resolve("VERSION"), StandardCharsets.UTF_8)).isEqualTo("0.18.7\n");
		assertThat(Files.readString(out.resolve("marketplace.json"), StandardCharsets.UTF_8))
			.contains("\"name\": \"yavi\"")
			.contains("\"url\": \"" + SITE + "/skill/yavi-plugin.zip\"");
		// The install page is the documentation page of the bundle, not a copy of it.
		assertThat(Files.readString(out.resolve("index.html"), StandardCharsets.UTF_8))
			.contains(SITE + "/0.18/en/agent-skill.html")
			.contains("claude plugin install yavi@yavi");
	}

	/**
	 * A rebuild that produces the same bytes keeps the published version, so an unchanged
	 * documentation does not make every installation download itself again.
	 */
	@Test
	void anUnchangedBundleKeepsItsPublishedVersion(@TempDir Path tmp) throws IOException {
		Path first = tmp.resolve("first");
		new SkillGen(CONTENT, first, "0.18", "en", "0.18.7", "abc1234", SITE, null, null).generate();
		String digest = jsonField(first.resolve("version.json"), "contentHash");
		String published = jsonField(first.resolve("version.json"), "version");

		Path second = tmp.resolve("second");
		new SkillGen(CONTENT, second, "0.18", "en", "0.18.8", "def5678", SITE, published, digest).generate();

		assertThat(Files.readString(second.resolve("VERSION"), StandardCharsets.UTF_8)).isEqualTo(published + "\n");
		assertThat(Files.readString(second.resolve("yavi/SKILL.md"), StandardCharsets.UTF_8))
			.contains("references/getting-started.md");
	}

	private static String jsonField(Path file, String name) throws IOException {
		String json = Files.readString(file, StandardCharsets.UTF_8);
		String marker = "\"" + name + "\": \"";
		int start = json.indexOf(marker);
		assertThat(start).as(name + " in " + file).isNotNegative();
		int from = start + marker.length();
		return json.substring(from, json.indexOf('"', from));
	}

}
