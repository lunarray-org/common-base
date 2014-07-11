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

import java.lang.reflect.Method;
import java.lang.reflect.Type;

import org.lunarray.common.check.CheckUtil;
import org.lunarray.common.generics.Member;

/**
 * A member representing a field.
 * 
 * @author Pal Hargitai (pal@lunarray.org)
 */
public final class MemberMethod
		implements Member {

	/** The method. */
	private final transient Method method;
	/** The parameter. */
	private final transient int param;

	/**
	 * Default constructor.
	 * 
	 * @param method
	 *            The method.
	 * @param param
	 *            The parameter. If less than 0, uses the return type.
	 */
	public MemberMethod(final Method method, final int param) {
		this.method = method;
		this.param = param;
	}

	/** {@inheritDoc} */
	@Override
	public Class<?> getDeclaringType() {
		return this.method.getDeclaringClass();
	}

	/** {@inheritDoc} */
	@Override
	public Type getGenericType() {
		Type resultType;
		if (CheckUtil.checkBounds(this.param, this.method.getGenericParameterTypes())) {
			resultType = this.method.getGenericParameterTypes()[this.param];
		} else {
			resultType = this.method.getGenericReturnType();
		}
		return resultType;
	}
}
