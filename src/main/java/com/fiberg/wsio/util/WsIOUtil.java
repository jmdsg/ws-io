package com.fiberg.wsio.util;

import com.google.common.collect.Multimap;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.text.WordUtils;

import java.util.HashSet;
import java.util.Map.Entry;
import java.util.Set;

/**
 *
 */
public final class WsIOUtil {

	/**
	 * Private empty constructor.
	 */
	private WsIOUtil() {  }

	/** Java name separator */
	public static final String NAME_SEPARATOR = ".";

	/**
	 * Method that adds to a name a suffix with the given separator.
	 *
	 * @param name      name to process
	 * @param suffix    suffix to add
	 * @param separator separator to use
	 * @return the name concatenated with the separator and the suffix
	 */
	public static String addSuffixOf(String name, String suffix, String separator) {
		return StringUtils.isNotBlank(suffix) ? name + separator + suffix : name;
	}

	/**
	 * Method that adds to a name a suffix with an empty separator.
	 *
	 * @param name      name to process
	 * @param suffix    suffix to add
	 * @return the name concatenated with the suffix
	 */
	public static String addSuffix(String name, String suffix) {
		return addSuffixOf(name, suffix, StringUtils.EMPTY);
	}

	/**
	 * Method that adds to a name a suffix with a name separator.
	 *
	 * @param name      name to process
	 * @param suffix    suffix to add
	 * @return the name concatenated with the name separator and the suffix
	 */
	public static String addSuffixName(String name, String suffix) {
		return addSuffixOf(name, suffix, NAME_SEPARATOR);
	}

	/**
	 * Method that adds to a name a prefix with the given separator.
	 *
	 * @param name      name to process
	 * @param prefix    prefix to add
	 * @param separator separator to use
	 * @return the prefix concatenated with the separator and the name
	 */
	public static String addPrefixOf(String name, String prefix, String separator) {
		return StringUtils.isNotBlank(prefix) ? prefix + separator + name : name;
	}

	/**
	 * Method that adds to a name a prefix with an empty separator.
	 *
	 * @param name      name to process
	 * @param prefix    prefix to add
	 * @return the prefix concatenated with the name
	 */
	public static String addPrefix(String name, String prefix) {
		return addPrefixOf(name, prefix, StringUtils.EMPTY);
	}

	/**
	 * Method that adds to a name a prefix with the name separator.
	 *
	 * @param name      name to process
	 * @param prefix    prefix to add
	 * @return the prefix concatenated with the name separator and the name
	 */
	public static String addPrefixName(String name, String prefix) {
		return addPrefixOf(name, prefix, NAME_SEPARATOR);
	}

	/**
	 * Method that adds to a name a suffix and prefix with the given separator.
	 *
	 * @param name      name to process
	 * @param prefix    prefix to add
	 * @param suffix    suffix to add
	 * @param separator separator to use
	 * @return the prefix concatenated with the separator, the name, the separator again and the suffix
	 */
	public static String addWrapOf(String name, String prefix, String suffix, String separator) {
		return addSuffixOf(addPrefixOf(name, prefix, separator), suffix, separator);
	}

	/**
	 * Method that adds to a name a suffix and prefix with an empty separator.
	 *
	 * @param name      name to process
	 * @param prefix    prefix to add
	 * @param suffix    suffix to add
	 * @return the prefix concatenated with the name and the suffix
	 */
	public static String addWrap(String name, String prefix, String suffix) {
		return addWrapOf(name, prefix, suffix, StringUtils.EMPTY);
	}

	/**
	 * Method that adds to a name a suffix and prefix with the name separator.
	 *
	 * @param name      name to process
	 * @param prefix    prefix to add
	 * @param suffix    suffix to add
	 * @return the prefix concatenated with the name separator, the name, the separator again and the suffix
	 */
	public static String addWrapName(String name, String prefix, String suffix) {
		return addWrapOf(name, prefix, suffix, NAME_SEPARATOR);
	}

	public static <K, V> Set<K> inverseLookUps(Multimap<K, V> map, V value) {
		Set<K> keys = new HashSet<>();
		if (map.containsValue(value)) {
			for (Entry<K, V> entry : map.entries()) {
				if (entry.getValue() != null && entry.getValue().equals(value)) {
					keys.add(entry.getKey());
				}
			}
		}
		return keys;
	}

	public static <IFACE> IFACE forceDelegate(Object o,
	                                          Class<? extends IFACE> iface,
	                                          java.util.List<String> candidates) {

		try {

			Class<?> type = o.getClass();
			while (type != null && type != Object.class) {

				for (String fullName : candidates) {

					try {

						@SuppressWarnings({ "unchecked" })
						IFACE delegate = (IFACE) Class.forName(fullName)
								.getConstructor(type)
								.newInstance(o);

						if (iface.isInstance(delegate)) {
							return delegate;
						}

					} catch (Exception e) {
						// nothing to do
					}

				}

				type = type.getSuperclass();

			}

		} catch (Throwable t) {
			// nothing to do return null
		}

		return null;

	}

	/**
	 * Method to transform a getter name to property name
	 *
	 * @param getter name of the getter
	 * @return property name
	 */
	public static String getterToProperty(String getter) {

        /* Remove get from the method name and replace first char with lower */
		String name = getter.replaceAll("^get", "");
		return WordUtils.uncapitalize(name);

	}

	/**
	 * Method to transform a setter name to property name
	 *
	 * @param setter name of the setter
	 * @return property name
	 */
	public static String setterToProperty(String setter) {

        /* Remove set from the method name and replace first char with lower */
		String name = setter.replaceAll("^set", "");
		return WordUtils.uncapitalize(name);

	}

}
