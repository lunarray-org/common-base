/* 
 * Commons.
 * Copyright (C) 2013 Christian van der Stap (christian@lunarray.org)
 * 
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Lesser General Public License as
 * published by the Free Software Foundation, either version 3 of the License,
 * or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
package org.lunarray.common.check;

import java.util.List;

/**
 * An utility class with common checks.
 * 
 * @author Christian van der Stap (christian@lunarray.org)
 * @author Pal Hargitai (pal@lunarray.org)
 */
public enum CheckUtil {
	/** The instance. */
	INSTANCE;

	/** A reference null value. */
	private static final Object NULL = null;
	/** The range lower bound. */
	private static final int RANGE_LOWER_BOUND = 0;

	/** Constructor. */
	private CheckUtil() {
		/* No implementation. */
	}

	/**
	 * Check whether the index is within the bounds of the list.
	 * 
	 * @param index
	 *            The index.
	 * @param list
	 *            The list.
	 * @return True if the index is in the list range.
	 */
	public static boolean checkBounds(final int index, final List<?> list) {
		return (index >= CheckUtil.RANGE_LOWER_BOUND) && (index < list.size());
	}

	/**
	 * Check whether the index is within the bounds of the array.
	 * 
	 * @param index
	 *            The index.
	 * @param array
	 *            The array.
	 * @return True if the index is in the array range.
	 * @param <T>
	 *            The array type.
	 */
	public static <T> boolean checkBounds(final int index, final T[] array) {
		return (index >= CheckUtil.RANGE_LOWER_BOUND) && (index < array.length);
	}

	/**
	 * Check if the number is a positive number.
	 * 
	 * @param number
	 *            The number to check.
	 * @return True if and only if the number is a positive number.
	 */
	public static boolean checkPositive(final int number) {
		return number >= CheckUtil.RANGE_LOWER_BOUND;
	}

	/**
	 * Checks if the given object is null.
	 * 
	 * @param objectParam
	 *            The object to check.
	 * @return True if and only if the given object is null.
	 */
	public static boolean isNull(final Object objectParam) {
		return objectParam == CheckUtil.NULL;
	}

	/**
	 * Checks if the given object is NOT null.
	 * 
	 * @param objectParam
	 *            The object to check
	 * @return True if and only if the given object is NOT null.
	 */
	public static boolean notNull(final Object objectParam) {
		return !CheckUtil.isNull(objectParam);
	}

	/**
	 * Checks if both objects are equal or null.
	 * 
	 * @param firstParam
	 *            The first object to check.
	 * @param secondParam
	 *            The second obejct to check.
	 * @return True if both object are equal or null, false otherwise.
	 */
	public static boolean objectEquals(final Object firstParam, final Object secondParam) {
		return (firstParam == secondParam) || (CheckUtil.notNull(firstParam) && firstParam.equals(secondParam));
	}
}
