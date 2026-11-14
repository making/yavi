/*
 * SPDX-FileCopyrightText: 2018-2026 Toshiaki Maki <makingx@gmail.com>
 * SPDX-License-Identifier: Apache-2.0
 */
package am.ik.yavi.message;

import java.text.MessageFormat;
import java.util.Locale;
import java.util.MissingResourceException;
import java.util.Objects;
import java.util.ResourceBundle;

import org.jspecify.annotations.Nullable;

/**
 * A {@link MessageFormatter} that looks up the message of a violation by its message key
 * in a {@link ResourceBundle}, such as {@code messages_ja.properties} on the class path.
 *
 * <pre>{@code
 * // messages_ja.properties: numeric.lessThanOrEqual="{0}" は {1} 以下で入力してください
 * MessageFormatter formatter = new ResourceBundleMessageFormatter("messages");
 * validator.formattingWith(formatter).validate(input, Locale.JAPANESE);
 * }</pre>
 *
 * <p>
 * A message of the bundle is a {@link MessageFormat} pattern with the arguments of the
 * default message format: <code>{0}</code> is the name. A key that the bundle does not
 * have is formatted with the default message format, so a bundle holds only the messages
 * it replaces, and a bundle that does not exist replaces none.
 *
 * <p>
 * The bundle is the one of the locale of the validation ({@code messages_ja_JP}, then
 * {@code messages_ja}), then the base bundle ({@code messages}). Unlike
 * {@link ResourceBundle#getBundle(String, Locale)}, the bundle of the default locale of
 * the platform is not a fallback: a validation in English on a Japanese server is not
 * answered in Japanese because {@code messages_en} does not exist.
 *
 * @author Toshiaki Maki
 * @since 0.18.0
 */
public final class ResourceBundleMessageFormatter implements MessageFormatter {

	private final String baseName;

	private final @Nullable ClassLoader classLoader;

	/**
	 * Creates a formatter that reads the bundles of the given base name with the class
	 * loader of the caller's context.
	 * @param baseName the base name of the resource bundle, such as {@code messages} or
	 * {@code com.example.messages}
	 */
	public ResourceBundleMessageFormatter(String baseName) {
		this(baseName, null);
	}

	/**
	 * Creates a formatter that reads the bundles of the given base name with the given
	 * class loader.
	 * @param baseName the base name of the resource bundle
	 * @param classLoader the class loader that loads the bundles; {@code null} for the
	 * context class loader of the thread that formats a message
	 */
	public ResourceBundleMessageFormatter(String baseName, @Nullable ClassLoader classLoader) {
		this.baseName = Objects.requireNonNull(baseName, "'baseName' must not be null");
		this.classLoader = classLoader;
	}

	/**
	 * Formats the message of the bundle for the given key, or the default message format
	 * if the bundle has none.
	 * @param messageKey the message key that is looked up in the bundle
	 * @param defaultMessageFormat the message format used when the bundle has no message
	 * for the key
	 * @param args the arguments referenced by placeholders in the message format
	 * @param locale the locale of the bundle and of the formatting
	 * @return the formatted message
	 */
	@Override
	public String format(String messageKey, String defaultMessageFormat, Object[] args, Locale locale) {
		final String pattern = this.pattern(messageKey, locale);
		return new MessageFormat(pattern != null ? pattern : defaultMessageFormat, locale).format(args);
	}

	private @Nullable String pattern(String messageKey, Locale locale) {
		final ResourceBundle bundle = this.bundle(locale);
		if (bundle != null && bundle.containsKey(messageKey)) {
			return bundle.getString(messageKey);
		}
		return null;
	}

	/**
	 * Returns the bundle of the locale, or the base bundle if the one that was found is
	 * the bundle of the default locale of the platform.
	 */
	private @Nullable ResourceBundle bundle(Locale locale) {
		final ResourceBundle bundle = this.load(locale);
		if (bundle == null || isBundleOf(bundle, locale)) {
			return bundle;
		}
		final ResourceBundle base = this.load(Locale.ROOT);
		return base != null && isBundleOf(base, Locale.ROOT) ? base : null;
	}

	private static boolean isBundleOf(ResourceBundle bundle, Locale locale) {
		final Locale found = bundle.getLocale();
		return found.getLanguage().isEmpty() || found.getLanguage().equals(locale.getLanguage());
	}

	private @Nullable ResourceBundle load(Locale locale) {
		final ClassLoader loader = this.classLoader != null ? this.classLoader
				: Thread.currentThread().getContextClassLoader();
		try {
			return loader != null ? ResourceBundle.getBundle(this.baseName, locale, loader)
					: ResourceBundle.getBundle(this.baseName, locale);
		}
		catch (MissingResourceException e) {
			return null;
		}
	}

}
