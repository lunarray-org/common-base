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
package org.lunarray.common.generics;

import java.lang.reflect.Type;

/**
 * The member.
 * 
 * @author Pal Hargitai (pal@lunarray.org)
 */
public interface Member {

	/**
	 * Gets the declaring member type.
	 * 
	 * @return The declaring type.
	 */
	Class<?> getDeclaringType();

	/**
	 * Gets the generic type.
	 * 
	 * @return The generic type.
	 */
	Type getGenericType();
}
