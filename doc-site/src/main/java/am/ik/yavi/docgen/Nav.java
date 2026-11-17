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
import java.util.List;
import java.util.Map;
import java.util.Optional;

import org.yaml.snakeyaml.Yaml;

/**
 * Navigation model for one version/language tree, loaded from a {@code nav.yaml}. The
 * file lists the documentation structure as ordered sections of pages; the flat top-level
 * page order drives the previous/next links rendered on each page.
 *
 * <p>
 * Expected {@code nav.yaml} shape:
 *
 * <pre>
 * title: YAVI
 * lang_name: English
 * sections:
 *   - title: Getting Started
 *     pages:
 *       - file: index.md
 *         title: Overview
 *         subpages:
 *           - file: guides/deep.md
 *             title: A Sub-page
 * </pre>
 *
 * A page {@code title} is required by the generator: it is the sidebar label, the
 * {@code <title>} of the page, and the label of its previous/next links.
 */
public record Nav(String title, String langName, List<Section> sections) {

	/** A titled group of pages shown as one block in the sidebar. */
	public record Section(String title, List<Page> pages) {
	}

	/**
	 * A single documentation page.
	 *
	 * @param file the Markdown source path relative to the language directory (e.g.
	 * {@code getting-started.md})
	 * @param title the human-readable title shown in the sidebar and {@code <title>}
	 * @param subpages pages that belong to this one and are reachable only THROUGH it:
	 * they are rendered, chained to each other with previous/next links and given a back
	 * link to their parent, but they are not sidebar rows
	 */
	public record Page(String file, String title, List<Page> subpages) {

		public Page(String file, String title) {
			this(file, title, List.of());
		}
	}

	/**
	 * Returns every top-level page in document order (used for prev/next navigation).
	 * Subpages are absent: they are not sidebar rows and are rendered from their parent.
	 */
	public List<Page> flatPages() {
		List<Page> all = new ArrayList<>();
		for (Section section : this.sections) {
			all.addAll(section.pages());
		}
		return all;
	}

	/** Every page of every section, including subpages. */
	public List<Page> allPages() {
		List<Page> all = new ArrayList<>();
		for (Section section : this.sections) {
			collect(section.pages(), all);
		}
		return all;
	}

	private static void collect(List<Page> pages, List<Page> into) {
		for (Page page : pages) {
			into.add(page);
			collect(page.subpages(), into);
		}
	}

	/** Finds a page by its Markdown file path, including every nesting of subpages. */
	public Optional<Page> findPage(String file) {
		for (Section section : this.sections) {
			Optional<Page> found = findPage(section.pages(), file);
			if (found.isPresent()) {
				return found;
			}
		}
		return Optional.empty();
	}

	private static Optional<Page> findPage(List<Page> pages, String file) {
		for (Page page : pages) {
			if (page.file().equals(file)) {
				return Optional.of(page);
			}
			Optional<Page> found = findPage(page.subpages(), file);
			if (found.isPresent()) {
				return found;
			}
		}
		return Optional.empty();
	}

	/** Loads a {@code nav.yaml} into a {@link Nav}. */
	@SuppressWarnings("unchecked")
	public static Nav load(Path navYaml) throws IOException {
		if (!Files.exists(navYaml)) {
			throw new IOException("Missing navigation file: " + navYaml);
		}
		try (InputStream in = Files.newInputStream(navYaml)) {
			Map<String, Object> root = new Yaml().load(in);
			if (root == null) {
				throw new IOException("Empty nav file: " + navYaml);
			}
			String title = String.valueOf(root.getOrDefault("title", "Documentation"));
			String langName = root.get("lang_name") == null ? null : String.valueOf(root.get("lang_name"));
			List<Section> sections = new ArrayList<>();
			Object rawSections = root.get("sections");
			if (rawSections instanceof List<?>sectionList) {
				for (Object rawSection : sectionList) {
					Map<String, Object> sectionMap = (Map<String, Object>) rawSection;
					String sectionTitle = String.valueOf(sectionMap.getOrDefault("title", ""));
					sections.add(new Section(sectionTitle, parsePages(sectionMap.get("pages"), navYaml)));
				}
			}
			return new Nav(title, langName, List.copyOf(sections));
		}
	}

	/** Parses a {@code pages:} / {@code subpages:} list; subpages nest arbitrarily. */
	@SuppressWarnings("unchecked")
	private static List<Page> parsePages(Object rawPages, Path navYaml) throws IOException {
		List<Page> pages = new ArrayList<>();
		if (rawPages instanceof List<?>pageList) {
			for (Object rawPage : pageList) {
				Map<String, Object> pageMap = (Map<String, Object>) rawPage;
				if (pageMap.get("file") == null || pageMap.get("title") == null) {
					throw new IOException("A page in " + navYaml + " is missing 'file' or 'title'");
				}
				String file = String.valueOf(pageMap.get("file"));
				String title = String.valueOf(pageMap.get("title"));
				pages.add(new Page(file, title, parsePages(pageMap.get("subpages"), navYaml)));
			}
		}
		return List.copyOf(pages);
	}

}
