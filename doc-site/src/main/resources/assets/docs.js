/*
 * Runnable Java cells and the search dialog for the yavi documentation site.
 *
 * Each ".code-cell" is an editable textarea plus a Run button. The Java runtime
 * (assets/yavi-runtime.js) is loaded lazily on the first Run click anywhere on the
 * page and shared by every cell -- nothing heavy loads on page view. The runtime's
 * contract is documented in doc-site/README.md: YaviRuntime.run(source) receives a
 * complete compilation unit and resolves with the program's output text.
 *
 * The search (Ctrl+K or /) reads the two index files docgen writes beside the pages
 * of this version/language tree. Tier 1 (search-index.json -- titles and headings)
 * is prefetched when the browser goes idle, so the dominant "I know the name, take
 * me there" query answers with no round trip; tier 2 (search-body.json -- the
 * section bodies) is fetched on the first keystroke. Matching is plain substring on
 * both, which needs no tokenizer and therefore works the same in the Japanese tree
 * as in the English one.
 */
(function () {
	"use strict";

	var runtimePromise = null;
	var statusEl = null;

	function setStatus(state, text) {
		if (!statusEl) return;
		statusEl.setAttribute("data-state", state);
		statusEl.textContent = text || "";
	}

	// Lazily inject the runtime script named by data-runtime-src. Concurrent first
	// clicks all await the same promise.
	function ensureRuntime() {
		if (runtimePromise) return runtimePromise;
		var src = document.body.getAttribute("data-runtime-src");
		setStatus("loading", "loading runtime…");
		runtimePromise = new Promise(function (resolve, reject) {
			var script = document.createElement("script");
			script.src = src;
			script.onload = function () {
				if (!window.YaviRuntime || typeof window.YaviRuntime.run !== "function") {
					reject(new Error("runtime script loaded but YaviRuntime.run is unavailable"));
					return;
				}
				resolve(window.YaviRuntime);
			};
			script.onerror = function () {
				reject(new Error("failed to load " + src));
			};
			document.head.appendChild(script);
		});
		runtimePromise.then(
			function () { setStatus("ready", "runtime ready"); },
			function (e) {
				setStatus("error", "runtime failed");
				console.error(e);
			}
		);
		return runtimePromise;
	}

	function showOutput(cell, text, isErr) {
		var out = cell.querySelector(".cell-out");
		out.hidden = false;
		out.textContent = text;
		out.classList.toggle("err", !!isErr);
	}

	function runCell(cell) {
		var button = cell.querySelector(".run");
		var cellStatus = cell.querySelector(".cell-status");
		var src = cell.querySelector(".cell-src").value;
		button.disabled = true;
		cellStatus.textContent = "";
		cellStatus.classList.remove("err");
		ensureRuntime().then(function (runtime) {
			return Promise.resolve(runtime.run(src));
		}).then(
			function (text) {
				showOutput(cell, String(text == null ? "" : text).trim(), false);
				button.disabled = false;
			},
			function (e) {
				showOutput(cell, String(e && e.message ? e.message : e), true);
				button.disabled = false;
			}
		);
	}

	// Copy the given text to the clipboard, with a graceful fallback for
	// browsers/contexts where the async Clipboard API is unavailable.
	function copyText(text, onDone) {
		if (navigator.clipboard && navigator.clipboard.writeText) {
			navigator.clipboard.writeText(text).then(onDone, function () {
				fallbackCopy(text, onDone);
			});
		} else {
			fallbackCopy(text, onDone);
		}
	}

	function fallbackCopy(text, onDone) {
		var ta = document.createElement("textarea");
		ta.value = text;
		ta.setAttribute("readonly", "");
		ta.style.position = "fixed";
		ta.style.left = "-9999px";
		document.body.appendChild(ta);
		ta.select();
		try {
			document.execCommand("copy");
			onDone();
		} catch (e) {
			/* clipboard unavailable; leave the button label unchanged */
		}
		document.body.removeChild(ta);
	}

	function makeCopyButton() {
		var b = document.createElement("button");
		b.type = "button";
		b.className = "copy";
		b.textContent = "Copy";
		b.setAttribute("aria-label", "Copy code to clipboard");
		return b;
	}

	// Flash a "Copied" confirmation on the button, then restore its label.
	function flashCopied(button) {
		button.textContent = "Copied";
		button.classList.add("copied");
		window.setTimeout(function () {
			button.textContent = "Copy";
			button.classList.remove("copied");
		}, 1200);
	}

	// Add a copy button to every code block: runnable cells copy the editable
	// source; static blocks (bash, console, transcripts) copy their text. The
	// static block's wrapper also carries the fence language so the stylesheet can
	// print it as the block's specimen label.
	function wireCopy() {
		Array.prototype.forEach.call(document.querySelectorAll(".code-cell"), function (cell) {
			var toolbar = cell.querySelector(".cell-toolbar");
			var src = cell.querySelector(".cell-src");
			if (!toolbar || !src) return;
			toolbar.setAttribute("data-lang", cell.getAttribute("data-lang") || "java");
			var button = makeCopyButton();
			button.addEventListener("click", function () {
				copyText(src.value, function () { flashCopied(button); });
			});
			toolbar.appendChild(button);
		});

		Array.prototype.forEach.call(document.querySelectorAll(".markdown pre"), function (pre) {
			// The runnable cells' output area is also a <pre>; skip it.
			if (pre.classList.contains("cell-out")) return;
			var code = pre.querySelector("code");
			var wrap = document.createElement("div");
			wrap.className = "code-block";
			var language = code ? /(?:^|\s)language-([A-Za-z0-9+#_-]+)/.exec(code.className || "") : null;
			wrap.setAttribute("data-lang", language ? language[1] : "text");
			pre.parentNode.insertBefore(wrap, pre);
			wrap.appendChild(pre);
			var button = makeCopyButton();
			button.addEventListener("click", function () {
				copyText(code ? code.textContent : pre.textContent, function () {
					flashCopied(button);
				});
			});
			wrap.appendChild(button);
		});
	}

	// Highlight the TOC entry of the section currently scrolled into view.
	function wireToc() {
		var toc = document.querySelector(".toc");
		if (!toc) return;
		var entries = [];
		Array.prototype.forEach.call(toc.querySelectorAll("a"), function (a) {
			var id = decodeURIComponent((a.getAttribute("href") || "").replace(/^#/, ""));
			var heading = id && document.getElementById(id);
			if (heading) entries.push({ link: a, heading: heading });
		});
		if (!entries.length) return;

		var ticking = false;
		function update() {
			ticking = false;
			var offset = 80; // a little below the sticky top bar
			var activeIdx = 0;
			for (var i = 0; i < entries.length; i++) {
				if (entries[i].heading.getBoundingClientRect().top <= offset) {
					activeIdx = i;
				} else {
					break;
				}
			}
			// Near the very bottom, prefer the last entry (it may never reach the top).
			if (window.innerHeight + window.scrollY >= document.body.scrollHeight - 2) {
				activeIdx = entries.length - 1;
			}
			entries.forEach(function (e, i) {
				e.link.classList.toggle("active", i === activeIdx);
				if (i === activeIdx) {
					e.link.setAttribute("aria-current", "true");
				} else {
					e.link.removeAttribute("aria-current");
				}
			});
		}
		function onScroll() {
			if (!ticking) {
				ticking = true;
				window.requestAnimationFrame(update);
			}
		}
		window.addEventListener("scroll", onScroll, { passive: true });
		window.addEventListener("resize", onScroll, { passive: true });
		update();
	}

	// Hamburger toggle for the sidebar on narrow screens.
	function wireNavToggle() {
		var toggle = document.querySelector(".nav-toggle");
		var backdrop = document.querySelector(".sidebar-backdrop");
		var sidebar = document.querySelector(".sidebar");
		if (!toggle || !sidebar) return;
		var close = sidebar.querySelector(".nav-close");
		var firstLink = sidebar.querySelector(".nav-link");

		function setOpen(open, restoreFocus) {
			var wasOpen = document.body.classList.contains("nav-open");
			document.body.classList.toggle("nav-open", open);
			toggle.setAttribute("aria-expanded", open ? "true" : "false");
			if (backdrop) backdrop.hidden = !open;
			if (open && !wasOpen && firstLink) firstLink.focus();
			if (!open && wasOpen && restoreFocus !== false) toggle.focus();
		}
		toggle.addEventListener("click", function () {
			setOpen(!document.body.classList.contains("nav-open"));
		});
		if (close) {
			close.addEventListener("click", function () { setOpen(false); });
		}
		if (backdrop) {
			backdrop.addEventListener("click", function () { setOpen(false); });
		}
		sidebar.addEventListener("click", function (ev) {
			var a = ev.target.closest && ev.target.closest("a");
			if (a) setOpen(false);
		});
		document.addEventListener("keydown", function (ev) {
			if (ev.key === "Escape" && document.body.classList.contains("nav-open")) {
				setOpen(false);
			}
		});
		window.addEventListener("resize", function () {
			if (window.innerWidth > 900) setOpen(false, false);
		});
	}

	/* ------------------------------------------------------------------ *
	 * Search
	 * ------------------------------------------------------------------ */

	var MAX_HITS = 40;

	var search = {
		base: ".",
		tier1: null,
		tier1Promise: null,
		tier2: null,
		tier2Promise: null,
		overlay: null,
		input: null,
		list: null,
		foot: null,
		hits: [],
		active: 0,
		lastFocus: null
	};

	function fetchJson(url) {
		return fetch(url).then(function (res) {
			if (!res.ok) throw new Error(url + ": " + res.status);
			return res.json();
		});
	}

	// Tier 1 is small and is wanted before the first keystroke, so it is
	// prefetched on idle; tier 2 only when someone actually types.
	function loadTier1() {
		if (!search.tier1Promise) {
			search.tier1Promise = fetchJson(search.base + "/search-index.json").then(function (data) {
				search.tier1 = data;
				return data;
			});
			search.tier1Promise.catch(function (e) { console.error(e); });
		}
		return search.tier1Promise;
	}

	function loadTier2() {
		if (!search.tier2Promise) {
			search.tier2Promise = fetchJson(search.base + "/search-body.json").then(function (data) {
				search.tier2 = data;
				return data;
			});
			search.tier2Promise.catch(function (e) { console.error(e); });
		}
		return search.tier2Promise;
	}

	function escHtml(s) {
		return s.replace(/[&<>"]/g, function (c) {
			return { "&": "&amp;", "<": "&lt;", ">": "&gt;", '"': "&quot;" }[c];
		});
	}

	// Escape `text`, wrapping every occurrence of a query term in <mark>.
	function highlight(text, terms) {
		var lower = text.toLowerCase();
		var spans = [];
		terms.forEach(function (term) {
			if (!term) return;
			var at = lower.indexOf(term);
			while (at >= 0) {
				spans.push([at, at + term.length]);
				at = lower.indexOf(term, at + term.length);
			}
		});
		spans.sort(function (a, b) { return a[0] - b[0]; });
		var out = "";
		var pos = 0;
		spans.forEach(function (span) {
			if (span[0] < pos) return; // overlapping terms: keep the first
			out += escHtml(text.slice(pos, span[0])) + "<mark>" + escHtml(text.slice(span[0], span[1])) + "</mark>";
			pos = span[1];
		});
		return out + escHtml(text.slice(pos));
	}

	// A window of `text` around its first matching term.
	function snippet(text, terms) {
		var lower = text.toLowerCase();
		var at = -1;
		terms.forEach(function (term) {
			var i = term ? lower.indexOf(term) : -1;
			if (i >= 0 && (at < 0 || i < at)) at = i;
		});
		if (at < 0) at = 0;
		var start = Math.max(0, at - 60);
		var end = Math.min(text.length, at + 160);
		return (start > 0 ? "…" : "") + text.slice(start, end) + (end < text.length ? "…" : "");
	}

	function matchesAll(hay, terms) {
		for (var i = 0; i < terms.length; i++) {
			if (hay.indexOf(terms[i]) < 0) return false;
		}
		return true;
	}

	// Rank, cheapest signal first: a page title beats a heading, which beats a
	// body section. Within a rank the earlier offset wins.
	function rankTitle(title, term) {
		if (title === term) return 0;
		if (title.indexOf(term) === 0) return 1;
		if (title.indexOf(term) >= 0) return 2;
		return -1;
	}

	// Tier 1: page titles and headings.
	function searchTier1(terms, into) {
		if (!search.tier1) return;
		var term = terms[0];
		search.tier1.pages.forEach(function (page, index) {
			var title = page.t.toLowerCase();
			var headings = page.h || [];
			var hay = title + " " + page.p.toLowerCase();
			headings.forEach(function (h) { hay += " " + h[1].toLowerCase(); });
			if (!matchesAll(hay, terms)) return;

			var titleRank = rankTitle(title, term);
			if (titleRank >= 0) {
				add(into, {
					rank: titleRank,
					offset: title.indexOf(term),
					page: index,
					anchor: "",
					heading: "",
					text: ""
				});
			}
			headings.forEach(function (h) {
				var anchor = anchorOf(page, h);
				if (!anchor) return; // the page's own title, already ranked above
				var label = h[1].toLowerCase();
				var at = label.indexOf(term);
				if (at < 0) return;
				add(into, {
					rank: at === 0 ? 5 : 6,
					offset: at,
					page: index,
					anchor: anchor,
					heading: h[1],
					text: ""
				});
			});
		});
	}

	// Tier 2: the section bodies, keyed back to tier 1's pages and headings.
	function searchTier2(terms, into) {
		if (!search.tier2) return;
		var term = terms[0];
		var sections = search.tier2.s;
		for (var i = 0; i < sections.length; i++) {
			var pageIndex = sections[i][0];
			var headingIndex = sections[i][1];
			var text = sections[i][2];
			var lower = text.toLowerCase();
			if (!matchesAll(lower, terms)) continue;
			var page = search.tier1.pages[pageIndex];
			if (!page) continue;
			var heading = headingIndex >= 0 && page.h ? page.h[headingIndex] : null;
			var anchor = heading ? anchorOf(page, heading) : "";
			add(into, {
				rank: 7,
				offset: lower.indexOf(term),
				page: pageIndex,
				anchor: anchor,
				heading: anchor ? heading[1] : "",
				text: text
			});
		}
	}

	// One hit per target: a heading found in both tiers is the same destination,
	// so keep the better rank but let the body tier contribute the snippet.
	function add(into, hit) {
		var key = hit.page + "#" + hit.anchor;
		var existing = into[key];
		if (!existing) {
			into[key] = hit;
			return;
		}
		if (!existing.text && hit.text) existing.text = hit.text;
		if (hit.rank < existing.rank) {
			hit.text = hit.text || existing.text;
			into[key] = hit;
		}
	}

	function runSearch(query) {
		var terms = query.toLowerCase().split(/\s+/).filter(function (t) { return t.length > 0; });
		if (!terms.length) return [];
		var into = {};
		searchTier1(terms, into);
		// A one-character query is too broad in English but meaningful in Japanese,
		// where one character is a word.
		if (query.length >= 2 || /[^\x00-\x7F]/.test(query)) searchTier2(terms, into);
		var hits = Object.keys(into).map(function (k) { return into[k]; });
		hits.sort(function (a, b) {
			return a.rank - b.rank || a.offset - b.offset || (a.page - b.page) || (a.anchor < b.anchor ? -1 : 1);
		});
		return hits.slice(0, MAX_HITS);
	}

	// The <h1> of a page repeats its title, so a hit on it IS a hit on the page:
	// give it the page's own anchor so the two fold into one result.
	function anchorOf(page, heading) {
		return heading[1] === page.t ? "" : heading[0];
	}

	function hitHref(hit) {
		var page = search.tier1.pages[hit.page];
		return search.base + "/" + page.p + (hit.anchor ? "#" + hit.anchor : "");
	}

	function renderHits(terms) {
		search.list.innerHTML = "";
		if (!search.hits.length) {
			var empty = document.createElement("div");
			empty.className = "search-empty";
			empty.setAttribute("role", "option");
			empty.setAttribute("aria-disabled", "true");
			empty.setAttribute("aria-selected", "false");
			empty.textContent = search.input.value ? "No matches" : "Type to search this version's pages";
			search.list.appendChild(empty);
			return;
		}
		search.hits.forEach(function (hit, i) {
			var page = search.tier1.pages[hit.page];
			var a = document.createElement("a");
			a.className = "search-hit" + (i === search.active ? " active" : "");
			a.href = hitHref(hit);
			a.setAttribute("role", "option");
			a.setAttribute("aria-selected", i === search.active ? "true" : "false");
			var html = '<span class="hit-title">' + highlight(page.t, terms) + "</span>";
			if (hit.heading) {
				html += '<span class="hit-crumb">' + highlight(hit.heading, terms) + "</span>";
			}
			if (hit.text) {
				html += '<span class="hit-snippet">' + highlight(snippet(hit.text, terms), terms) + "</span>";
			}
			a.innerHTML = html;
			a.addEventListener("mousemove", function () { setActive(i, false); });
			search.list.appendChild(a);
		});
	}

	function setActive(i, scroll) {
		var items = search.list.querySelectorAll(".search-hit");
		if (!items.length) return;
		search.active = Math.max(0, Math.min(i, items.length - 1));
		Array.prototype.forEach.call(items, function (el, n) {
			el.classList.toggle("active", n === search.active);
			el.setAttribute("aria-selected", n === search.active ? "true" : "false");
		});
		if (scroll && items[search.active].scrollIntoView) {
			items[search.active].scrollIntoView({ block: "nearest" });
		}
	}

	function update() {
		var query = search.input.value.trim();
		var terms = query.toLowerCase().split(/\s+/).filter(function (t) { return t.length > 0; });
		if (!search.tier1) {
			search.list.innerHTML = '<div class="search-empty">Loading the index…</div>';
			loadTier1().then(update, function () {
				search.list.innerHTML = '<div class="search-empty">The search index could not be loaded.</div>';
			});
			return;
		}
		if (query && !search.tier2) {
			// Re-render once when the body tier lands, not once per keystroke typed
			// while it was in flight.
			var first = !search.tier2Promise;
			loadTier2();
			if (first) {
				search.tier2Promise.then(update, function () { setFoot("full-text index unavailable"); });
			}
			setFoot("loading full text…");
		}
		else {
			setFoot("");
		}
		search.active = 0;
		search.hits = query ? runSearch(query) : [];
		renderHits(terms);
	}

	function setFoot(text) {
		if (search.foot) search.foot.textContent = text;
	}

	function buildOverlay() {
		var overlay = document.createElement("div");
		overlay.className = "search-overlay";
		overlay.hidden = true;
		overlay.innerHTML =
			'<div class="search-panel" role="dialog" aria-modal="true" aria-label="Search the documentation">' +
			'<div class="search-head"><span class="search-head-title">Search</span>' +
			'<button class="search-close" type="button" aria-label="Close search">Close</button></div>' +
			'<input class="search-input" type="text" autocomplete="off" autocorrect="off" spellcheck="false"' +
			' placeholder="Search this documentation" aria-label="Search query"' +
			' role="combobox" aria-autocomplete="list" aria-expanded="true" aria-controls="yavi-search-results">' +
			'<div class="search-results" id="yavi-search-results" role="listbox" aria-label="Search results"></div>' +
			'<div class="search-legend"><span><kbd>↑</kbd><kbd>↓</kbd> move</span>' +
			'<span><kbd>Enter</kbd> open</span><span><kbd>Esc</kbd> close</span>' +
			'<span class="search-note" aria-live="polite"></span></div>' +
			"</div>";
		document.body.appendChild(overlay);
		search.overlay = overlay;
		search.input = overlay.querySelector(".search-input");
		search.list = overlay.querySelector(".search-results");
		search.foot = overlay.querySelector(".search-note");

		overlay.addEventListener("mousedown", function (ev) {
			if (ev.target === overlay) closeSearch();
		});
		var closeButton = overlay.querySelector(".search-close");
		closeButton.addEventListener("click", closeSearch);
		// Keep Tab inside the modal while it is open.
		overlay.addEventListener("keydown", function (ev) {
			if (ev.key !== "Tab") return;
			var focusable = overlay.querySelectorAll("input, button, a[href]");
			if (!focusable.length) return;
			var first = focusable[0];
			var last = focusable[focusable.length - 1];
			if (ev.shiftKey && document.activeElement === first) {
				ev.preventDefault();
				last.focus();
			} else if (!ev.shiftKey && document.activeElement === last) {
				ev.preventDefault();
				first.focus();
			}
		});
		search.input.addEventListener("input", update);
		search.input.addEventListener("keydown", function (ev) {
			if (ev.key === "ArrowDown") {
				ev.preventDefault();
				setActive(search.active + 1, true);
			} else if (ev.key === "ArrowUp") {
				ev.preventDefault();
				setActive(search.active - 1, true);
			} else if (ev.key === "Enter") {
				var items = search.list.querySelectorAll(".search-hit");
				if (items[search.active]) {
					ev.preventDefault();
					location.href = items[search.active].getAttribute("href");
					closeSearch();
				}
			} else if (ev.key === "Escape") {
				ev.preventDefault();
				closeSearch();
			}
		});
		return overlay;
	}

	function openSearch(seed) {
		if (!search.overlay) buildOverlay();
		if (!search.overlay.hidden) return;
		search.lastFocus = document.activeElement;
		search.overlay.hidden = false;
		document.body.classList.add("search-open-body");
		search.input.value = seed || "";
		search.input.focus();
		search.input.select();
		update();
	}

	function closeSearch() {
		if (!search.overlay || search.overlay.hidden) return;
		search.overlay.hidden = true;
		document.body.classList.remove("search-open-body");
		if (search.lastFocus && search.lastFocus.focus) search.lastFocus.focus();
	}

	// `/` opens the dialog only when nothing else is taking the keystroke -- a
	// runnable cell's textarea is an editor.
	function isTyping(target) {
		if (!target) return false;
		var tag = target.tagName;
		return tag === "INPUT" || tag === "TEXTAREA" || tag === "SELECT" || target.isContentEditable;
	}

	function wireSearch() {
		var base = document.body.getAttribute("data-search-base");
		if (base) search.base = base;
		var button = document.querySelector(".search-open");
		if (button) {
			button.addEventListener("click", function () { openSearch(""); });
			// A Mac keyboard has no Ctrl+K habit; show the modifier it does have.
			if (/Mac|iPhone|iPad/.test(navigator.platform || "")) {
				var key = button.querySelector(".search-open-key");
				if (key) {
					key.textContent = "⌘K";
					button.setAttribute("aria-label", "Search ⌘K");
				}
			}
		}
		document.addEventListener("keydown", function (ev) {
			if (!ev.key) return;
			if ((ev.ctrlKey || ev.metaKey) && !ev.altKey && ev.key.toLowerCase() === "k") {
				ev.preventDefault();
				openSearch("");
			} else if (ev.key === "/" && !ev.ctrlKey && !ev.metaKey && !ev.altKey && !isTyping(ev.target)) {
				ev.preventDefault();
				openSearch("");
			} else if (ev.key === "Escape") {
				closeSearch();
			}
		});
		// Warm tier 1 once the page is otherwise done, so the first keystroke
		// searches immediately.
		if (window.requestIdleCallback) {
			window.requestIdleCallback(function () { loadTier1(); });
		} else {
			window.setTimeout(loadTier1, 1500);
		}
	}

	function wire() {
		statusEl = document.querySelector(".runtime-status");
		Array.prototype.forEach.call(document.querySelectorAll(".switch.active"), function (el) {
			el.setAttribute("aria-current", "true");
		});
		wireSearch();
		wireNavToggle();
		wireToc();
		wireCopy();

		Array.prototype.forEach.call(document.querySelectorAll(".code-cell"), function (cell) {
			var button = cell.querySelector(".run");
			if (button) {
				button.addEventListener("click", function () { runCell(cell); });
			}
			// Ctrl/Cmd+Enter runs the focused cell.
			var src = cell.querySelector(".cell-src");
			if (src) {
				src.addEventListener("keydown", function (ev) {
					if ((ev.ctrlKey || ev.metaKey) && ev.key === "Enter") {
						ev.preventDefault();
						runCell(cell);
					}
				});
			}
		});

		var reset = document.querySelector(".reset-runtime");
		if (reset) {
			reset.addEventListener("click", function () {
				// The runtime holds per-page state; the simplest correct reset is a
				// full reload.
				if (runtimePromise) {
					location.reload();
				}
			});
		}
	}

	if (document.readyState === "loading") {
		document.addEventListener("DOMContentLoaded", wire);
	} else {
		wire();
	}
})();
