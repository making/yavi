/*
 * SPDX-FileCopyrightText: 2018-2026 Toshiaki Maki <makingx@gmail.com>
 * SPDX-License-Identifier: Apache-2.0
 */
package am.ik.yavi.docgen;

import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.yaml.snakeyaml.Yaml;

/**
 * The site configuration and the published version list, loaded from
 * {@code versions.yaml}.
 *
 * <p>
 * Expected shape:
 *
 * <pre>
 * site:
 *   title: YAVI
 *   description: Yet Another Validation (lambda based type safe validation)
 * default_version: 0.17
 * default_language: en
 * languages:
 *   - code: en
 *     name: English
 *   - code: ja
 *     name: 日本語
 * versions:
 *   - version: 0.17
 *     label: 0.17
 *     yavi_release: 0.17.3
 * </pre>
 *
 * The content tree holds one directory per version ({@code content/<version>/}) and one
 * directory per language inside it. {@link DocGen} validates the list here against those
 * directories before it renders anything.
 *
 * <p>
 * {@code yavi_release} is the YAVI release whose classes are baked into that version's
 * browser runtime (see doc-site/README.md). It is required: the deployment workflow reads
 * it from here to build one runtime per version, so a version without a release would
 * silently publish a tree whose Run buttons are unavailable.
 */
public record Versions(String title, String description, String defaultVersion, String defaultLanguage,
		List<Language> languages, List<Version> versions) {

	/** A language of the site: its URL/directory code and its display name. */
	public record Language(String code, String name) {
	}

	/**
	 * A published version: its URL/directory name, the label shown in the switcher and
	 * the YAVI release its browser runtime embeds.
	 */
	public record Version(String version, String label, String yaviRelease) {
	}

	/** The display name of a language code, or the code itself when it is unknown. */
	public String languageName(String code) {
		return this.languages.stream()
			.filter((language) -> language.code().equals(code))
			.map(Language::name)
			.findFirst()
			.orElse(code);
	}

	/** Loads and validates a {@code versions.yaml}. */
	@SuppressWarnings("unchecked")
	public static Versions load(Path file) throws IOException {
		try (InputStream in = Files.newInputStream(file)) {
			Map<String, Object> root = new Yaml().load(in);
			if (root == null) {
				throw new IOException("Empty version file: " + file);
			}
			Map<String, Object> site = root.get("site") instanceof Map<?, ?>map ? (Map<String, Object>) map : Map.of();
			String title = String.valueOf(site.getOrDefault("title", "Documentation"));
			String description = String.valueOf(site.getOrDefault("description", ""));
			String defaultVersion = String.valueOf(root.get("default_version"));
			String defaultLanguage = String.valueOf(root.get("default_language"));
			List<Language> languages = new ArrayList<>();
			Object rawLanguages = root.get("languages");
			if (rawLanguages instanceof List<?>languageList) {
				for (Object rawLanguage : languageList) {
					Map<String, Object> languageMap = (Map<String, Object>) rawLanguage;
					String code = String.valueOf(languageMap.get("code"));
					String name = String.valueOf(languageMap.getOrDefault("name", code));
					languages.add(new Language(code, name));
				}
			}
			List<Version> versions = new ArrayList<>();
			Object rawVersions = root.get("versions");
			if (rawVersions instanceof List<?>versionList) {
				for (Object rawVersion : versionList) {
					Map<String, Object> versionMap = (Map<String, Object>) rawVersion;
					String version = String.valueOf(versionMap.get("version"));
					String label = String.valueOf(versionMap.getOrDefault("label", version));
					Object rawYaviRelease = versionMap.get("yavi_release");
					String yaviRelease = rawYaviRelease == null ? "" : String.valueOf(rawYaviRelease).trim();
					versions.add(new Version(version, label, yaviRelease));
				}
			}
			Versions result = new Versions(title, description, defaultVersion, defaultLanguage, List.copyOf(languages),
					List.copyOf(versions));
			result.validate(file);
			return result;
		}
	}

	private void validate(Path file) throws IOException {
		if (this.versions.isEmpty()) {
			throw new IOException("No versions listed in " + file);
		}
		if (this.languages.isEmpty()) {
			throw new IOException("No languages listed in " + file);
		}
		Set<String> versionNames = new HashSet<>();
		for (Version version : this.versions) {
			// The version is a directory name and a URL segment, so it cannot carry a
			// separator or step outside the content tree.
			if (version.version().isBlank() || version.version().contains("/") || version.version().contains("\\")
					|| version.version().equals(".") || version.version().equals("..")) {
				throw new IOException("Invalid version '" + version.version() + "' in " + file);
			}
			if (!versionNames.add(version.version())) {
				throw new IOException("Duplicate version '" + version.version() + "' in " + file);
			}
			// The workflow passes this release to the runtime build; a version without
			// one
			// would publish the stub.
			if (version.yaviRelease().isBlank()) {
				throw new IOException("Version '" + version.version() + "' has no yavi_release in " + file);
			}
		}
		Set<String> languageCodes = new HashSet<>();
		for (Language language : this.languages) {
			if (language.code().isBlank() || language.code().contains("/") || language.code().contains("\\")) {
				throw new IOException("Invalid language code '" + language.code() + "' in " + file);
			}
			if (!languageCodes.add(language.code())) {
				throw new IOException("Duplicate language '" + language.code() + "' in " + file);
			}
		}
		if (!versionNames.contains(this.defaultVersion)) {
			throw new IOException("default_version '" + this.defaultVersion + "' is not listed in " + file);
		}
		if (!languageCodes.contains(this.defaultLanguage)) {
			throw new IOException("default_language '" + this.defaultLanguage + "' is not listed in " + file);
		}
	}

}
