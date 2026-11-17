/*
 * SPDX-FileCopyrightText: 2018-2026 Toshiaki Maki <makingx@gmail.com>
 * SPDX-License-Identifier: Apache-2.0
 */
package am.ik.yavi.docgen;

import java.util.List;

import com.vladsch.flexmark.ext.autolink.AutolinkExtension;
import com.vladsch.flexmark.ext.gfm.strikethrough.StrikethroughExtension;
import com.vladsch.flexmark.ext.tables.TablesExtension;
import com.vladsch.flexmark.html.HtmlRenderer;
import com.vladsch.flexmark.parser.Parser;
import com.vladsch.flexmark.util.data.MutableDataSet;

/**
 * The Markdown dialect of the documentation site: GFM tables, autolinks, strikethrough,
 * and generated heading ids (for the "On this page" TOC and intra-page links).
 */
final class Markdown {

	private Markdown() {
	}

	static MutableDataSet options() {
		MutableDataSet options = new MutableDataSet();
		options.set(Parser.EXTENSIONS,
				List.of(TablesExtension.create(), AutolinkExtension.create(), StrikethroughExtension.create()));
		options.set(HtmlRenderer.GENERATE_HEADER_ID, true);
		options.set(HtmlRenderer.RENDER_HEADER_ID, true);
		options.set(TablesExtension.COLUMN_SPANS, false);
		options.set(TablesExtension.APPEND_MISSING_COLUMNS, true);
		options.set(TablesExtension.DISCARD_EXTRA_COLUMNS, true);
		return options;
	}

}
