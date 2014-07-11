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
package org.lunarray.common.generics.impl;

import java.lang.reflect.Field;
import java.lang.reflect.Type;

import org.lunarray.common.generics.Member;

/**
 * A member representing a field.
 * 
 * @author Pal Hargitai (pal@lunarray.org)
 */
public final class MemberField
		implements Member {

	/** The field. */
	private final transient Field field;

	/**
	 * Default constructor.
	 * 
	 * @param field
	 *            The field.
	 */
	public MemberField(final Field field) {
		this.field = field;
	}

	/** {@inheritDoc} */
	@Override
	public Class<?> getDeclaringType() {
		return this.field.getDeclaringClass();
	}

	/** {@inheritDoc} */
	@Override
	public Type getGenericType() {
		return this.field.getGenericType();
	}
}
