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

import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.lang.reflect.TypeVariable;
import java.lang.reflect.WildcardType;
import java.util.Deque;
import java.util.Iterator;
import java.util.LinkedList;

import org.lunarray.common.check.CheckUtil;

/**
 * A utility for generics.
 * 
 * @author Pal Hargitai (pal@lunarray.org)
 */
public enum GenericsUtil {

	/** Instance. */
	INSTANCE;

	/** The maximum scan depth. */
	private static final int MAX_SCAN_DEPTH = 5;

	/**
	 * Find the type path leading from the current type to the desired type.
	 * 
	 * @param currentType
	 *            The current type.
	 * @param clazz
	 *            The desired type.
	 * @return The list from current type to desired type.
	 */
	public static Deque<Type> findTypePath(final Type currentType, final Class<?> clazz) {
		final Deque<Type> path = new LinkedList<Type>();
		if (!clazz.equals(currentType)) {
			path.add(currentType);
			// If the clazz is a Class, look if we can find the clazz.
			if (currentType instanceof Class) {
				GenericsUtil.processPath((Class<?>) currentType, clazz, path);
			} else if (currentType instanceof ParameterizedType) {
				// Search the raw type.
				final ParameterizedType parameterizedType = (ParameterizedType) currentType;
				path.addAll(GenericsUtil.findTypePath(parameterizedType.getRawType(), clazz));
			}
		}
		return path;
	}

	/**
	 * Gets the clazz declaring the typevariable.
	 * 
	 * @param declaringClazz
	 *            The declaring clazz.
	 * @param target
	 *            The type variable we're looking for.
	 * @return The clazz or one of it's declaring classes.
	 */
	public static Class<?> getDeclaringClass(final Class<?> declaringClazz, final TypeVariable<?> target) {
		Class<?> source = declaringClazz;
		boolean equals = false;
		// Test if we can find the declaring clazz.
		for (final TypeVariable<?> typeVariable : declaringClazz.getTypeParameters()) {
			if (typeVariable.equals(target)) {
				equals = true;
			}
		}
		// Search it's declaring clazz.
		final Class<?> nextDeclaring = declaringClazz.getDeclaringClass();
		if (!equals && !CheckUtil.isNull(nextDeclaring)) {
			source = GenericsUtil.getDeclaringClass(nextDeclaring, target);
		}
		return source;
	}

	/**
	 * Gets the generic argument of an entity type.
	 * 
	 * @param clazz
	 *            The entity type.
	 * @param genericParameter
	 *            The generic parameter to get.
	 * @param targetClazz
	 *            The target of which the generic parameter is to be resolved.
	 * @return The type of the generic parameter of the target.
	 */
	public static Type getEntityGenericType(final Class<?> clazz, final int genericParameter, final Class<?> targetClazz) {
		// Get the type path.
		final Deque<Type> typePath = GenericsUtil.findTypePath(clazz, targetClazz);
		final Iterator<Type> iterator = typePath.descendingIterator();
		final ParameterResult result = new ParameterResult();
		result.setIndex(genericParameter);
		Type typeResult = result.getResult();
		while (iterator.hasNext() && !(typeResult instanceof Class)) {
			final Type type = iterator.next();
			if (CheckUtil.isNull(typeResult)) {
				result.setResult(GenericsUtil.processNoResult(genericParameter, result.getIndex(), type));
			} else {
				GenericsUtil.processExistingResult(result, type);
			}
			typeResult = result.getResult();
		}
		typeResult = result.getResult();
		return typeResult;
	}

	/**
	 * Gets the most specific type we can assign to the given genericParameter
	 * to the clazz in the known fieldHierarchy.
	 * 
	 * @param clazz
	 *            THe clazz we're looking for.
	 * @param genericParameter
	 *            The parameter number of the clazz we're looking for.
	 * @param propertyHierarchy
	 *            The filed hierarachy.
	 * @return The most specific type we can assign to the given parameter.
	 */
	public static Type getPropertyGenericType(final Class<?> clazz, final int genericParameter,
			final Deque<? extends Member> propertyHierarchy) {
		Type result = null;
		if (!propertyHierarchy.isEmpty()) {
			// First field.
			final Member property = propertyHierarchy.pop();
			if (!CheckUtil.isNull(property)) {
				result = GenericsUtil.processProperty(clazz, genericParameter, propertyHierarchy, property);
			}
		}
		return result;
	}

	/**
	 * Gets the most specific type we can assign to the given genericParameter
	 * to the clazz in the known fieldHierarchy.
	 * 
	 * @param clazz
	 *            THe clazz we're looking for.
	 * @param genericParameter
	 *            The parameter number of the clazz we're looking for.
	 * @param propertyHierarchy
	 *            The filed hierarchy.
	 * @return The most specific type we can assign to the given parameter.
	 */
	public static Type getPropertyGenericType(final Class<?> clazz, final int genericParameter, final Member... propertyHierarchy) {
		final Deque<Member> properties = new LinkedList<Member>();
		for (final Member property : propertyHierarchy) {
			properties.add(property);
		}
		return GenericsUtil.getPropertyGenericType(clazz, genericParameter, properties);
	}

	/**
	 * Gets the parameter index this variable is assigned to.
	 * 
	 * @param realType
	 *            The type to find.
	 * @return The index of this type variables.
	 */
	public static int getRealParameter(final TypeVariable<?> realType) {
		int realVariable = -1;
		final Type type = (Type) realType.getGenericDeclaration();
		if (type instanceof Class) {
			final Class<?> clazz = (Class<?>) type;
			final TypeVariable<?>[] tvs = clazz.getTypeParameters();
			for (int i = 0; i < tvs.length; i = i + 1) {
				if (realType.equals(tvs[i])) {
					realVariable = i;
				}
			}
		}
		return realVariable;
	}

	/**
	 * Finds originating type for this field.
	 * 
	 * @param properties
	 *            The fields to look through.
	 * @return The true type.
	 */
	public static Type getRealType(final Deque<? extends Member> properties) {
		final Member property = properties.pop();
		final Type fieldType = property.getGenericType();
		Type result = null;
		if (fieldType instanceof TypeVariable) {
			final TypeVariable<?> typeVariable = (TypeVariable<?>) fieldType;
			final Class<?> declaringType = GenericsUtil.getDeclaringClass(property.getDeclaringType(), typeVariable);
			final TypeVariable<?> superTypeVariable = GenericsUtil.getSuperDeclaration(property, typeVariable);
			final int param = GenericsUtil.getRealParameter(superTypeVariable);
			result = GenericsUtil.getPropertyGenericType(declaringType, param, properties);
		} else if (fieldType instanceof Class) {
			result = fieldType;
		} else if (fieldType instanceof ParameterizedType) {
			final ParameterizedType pType = (ParameterizedType) fieldType;
			result = pType.getRawType();
		}
		return result;
	}

	/**
	 * Finds originating type for this field.
	 * 
	 * @param propertyHierarchy
	 *            The fields to look through.
	 * @return The true type.
	 */
	public static Type getRealType(final Member... propertyHierarchy) {
		final Deque<Member> properties = new LinkedList<Member>();
		for (final Member property : propertyHierarchy) {
			properties.add(property);
		}
		return GenericsUtil.getRealType(properties);
	}

	/**
	 * Gets the variable of the class that represents the variable for the
	 * field.
	 * 
	 * @param property
	 *            The field to match the variable to.
	 * @param typeVariable
	 *            The variable to match.
	 * @return The variable of the declaring class.
	 */
	public static TypeVariable<?> getSuperDeclaration(final Member property, final TypeVariable<?> typeVariable) {
		TypeVariable<?> result = typeVariable;
		Class<?> declaring = property.getDeclaringType();
		while (!CheckUtil.isNull(declaring)) {
			for (final TypeVariable<?> declaringTypeVariable : declaring.getTypeParameters()) {
				if (result.equals(declaringTypeVariable)) {
					result = declaringTypeVariable;
				}
			}
			declaring = declaring.getDeclaringClass();
		}
		return result;
	}

	/**
	 * Guesses the clazz of a type.
	 * 
	 * @param type
	 *            The type.
	 * @return The clazz.
	 */
	public static Class<?> guessClazz(final Type type) {
		return GenericsUtil.guessClazz(type, 0);
	}

	/**
	 * Trace to the most specific type of the field.
	 * 
	 * @param property
	 *            The field to look for.
	 * @param typePath
	 *            The type path.
	 * @param originatingIndex
	 *            The originating index.
	 * @return The most specific type.
	 */
	public static Type traceType(final Member property, final Deque<Type> typePath, final int originatingIndex) {
		final Iterator<Type> iterator = typePath.descendingIterator();
		final ParameterResult result = new ParameterResult();
		result.setIndex(originatingIndex);
		Type typeResult = result.getResult();
		while (iterator.hasNext() && !(typeResult instanceof Class)) {
			final Type type = iterator.next();
			if (CheckUtil.isNull(typeResult)) {
				result.setResult(GenericsUtil.processNoResult(originatingIndex, result.getIndex(), type));
			} else {
				GenericsUtil.processExistingResult(result, type);
			}
		}
		typeResult = result.getResult();
		if (typeResult instanceof TypeVariable<?>) {
			final Type type = property.getGenericType();
			if (type instanceof ParameterizedType) {
				final ParameterizedType parameterizedType = (ParameterizedType) type;
				result.setResult(parameterizedType.getActualTypeArguments()[result.getIndex()]);
			}
		}
		return result.getResult();
	}

	/**
	 * Decide if we continue on guessing the class or we stick to Object.
	 * 
	 * @param looped
	 *            The amount of times we looped.
	 * @param type
	 *            The guessed type.
	 * @return The guessed type.
	 */
	private static Class<?> decideGuessClazz(final int looped, final Type type) {
		Class<?> result;
		if (looped > GenericsUtil.MAX_SCAN_DEPTH) {
			result = Object.class;
		} else {
			result = GenericsUtil.guessClazz(type, looped);
		}
		return result;
	}

	/**
	 * Gets the type variable match.
	 * 
	 * @param clazz
	 *            The type.
	 * @param types
	 *            The super variables.
	 * @param result
	 *            The result.
	 */
	private static void getClassMatch(final Class<?> clazz, final Type[] types, final ParameterResult result) {
		for (int i = 0; i < types.length; i = i + 1) {
			if (types[i].equals(result.getResult())) {
				result.setIndex(i);
				result.setResult(types[i]);
			}
		}
	}

	/**
	 * Gets the type variable match.
	 * 
	 * @param clazz
	 *            The type.
	 * @param typeVariableSuper
	 *            The super variables.
	 * @param result
	 *            The result.
	 */
	private static void getClassMatch(final Class<?> clazz, final TypeVariable<?>[] typeVariableSuper, final ParameterResult result) {
		for (int i = 0; i < typeVariableSuper.length; i = i + 1) {
			if (typeVariableSuper[i].equals(result.getResult())) {
				result.setIndex(i);
				result.setResult(typeVariableSuper[i]);
			}
		}
	}

	/**
	 * Guesses the clazz of a type.
	 * 
	 * @param type
	 *            The type.
	 * @param loops
	 *            Amount of loops.
	 * @return The clazz.
	 */
	private static Class<?> guessClazz(final Type type, final int loops) {
		final int looped = loops + 1;
		Class<?> result;
		if (type instanceof Class) {
			result = (Class<?>) type;
		} else if (type instanceof ParameterizedType) {
			final ParameterizedType paramType = (ParameterizedType) type;
			result = GenericsUtil.decideGuessClazz(looped, paramType.getRawType());
		} else if (type instanceof TypeVariable<?>) {
			final TypeVariable<?> typeVar = (TypeVariable<?>) type;
			final Type[] bounds = typeVar.getBounds();
			result = GenericsUtil.decideGuessClazz(looped, bounds[0]);
		} else if (type instanceof WildcardType) {
			final WildcardType wildType = (WildcardType) type;
			final Type[] bounds = wildType.getUpperBounds();
			result = GenericsUtil.decideGuessClazz(looped, bounds[0]);
		} else {
			result = Object.class;
		}
		return result;
	}

	/**
	 * Handle type variable.
	 * 
	 * @param propertyHierarchy
	 *            The hierarchy.
	 * @param property
	 *            The handle property.
	 * @param currentType
	 *            The current type.
	 * @return The property type.
	 */
	private static Type handleTypeVariable(final Deque<? extends Member> propertyHierarchy, final Member property, final Type currentType) {
		Type result = currentType;
		// If it's a variable.
		if ((result instanceof TypeVariable) && !propertyHierarchy.isEmpty()) {
			// Match to parent variable
			final TypeVariable<?> typeVariable = (TypeVariable<?>) result;
			final Class<?> declaringClass = GenericsUtil.getDeclaringClass(property.getDeclaringType(), typeVariable);
			int index = -1;
			final TypeVariable<?>[] typeVariables = declaringClass.getTypeParameters();
			for (int iterator = 0; iterator < typeVariables.length; iterator = iterator + 1) {
				if (typeVariables[iterator].equals(typeVariable)) {
					index = iterator;
				}
			}
			if (GenericsUtil.isPositive(index)) {
				// If resolvable, resolve.
				result = GenericsUtil.getPropertyGenericType(declaringClass, index, propertyHierarchy);
			}
		}
		return result;
	}

	/**
	 * Make sure the number is positive.
	 * 
	 * @param number
	 *            The number.
	 * @return True if the number is positive.
	 */
	private static boolean isPositive(final int number) {
		return number >= 0;
	}

	/**
	 * Process an existing result.
	 * 
	 * @param result
	 *            The result.
	 * @param type
	 *            The result type.
	 */
	private static void processExistingResult(final ParameterResult result, final Type type) {
		if (type instanceof Class) {
			final Class<?> clazz = (Class<?>) type;
			final TypeVariable<?>[] typeVariableSuper = clazz.getTypeParameters();
			GenericsUtil.getClassMatch(clazz, typeVariableSuper, result);
		} else if (type instanceof ParameterizedType) {
			final ParameterizedType parameterizedType = (ParameterizedType) type;
			final Class<?> clazz = (Class<?>) parameterizedType.getRawType();
			Type[] types = clazz.getTypeParameters();
			GenericsUtil.getClassMatch(clazz, types, result);
			types = parameterizedType.getActualTypeArguments();
			GenericsUtil.getClassMatch(clazz, types, result);
			final Type actualType = parameterizedType.getActualTypeArguments()[result.getIndex()];
			if (actualType instanceof Class) {
				result.setResult(actualType);
			} else if (actualType instanceof ParameterizedType) {
				final ParameterizedType actualParameterizedType = (ParameterizedType) actualType;
				result.setResult(actualParameterizedType.getRawType());
			}
		}
	}

	/**
	 * Processes the interfaces.
	 * 
	 * @param clazz
	 *            The super clazz.
	 * @param path
	 *            The declaration path.
	 * @param currentClazz
	 *            The current clazz.
	 */
	private static void processInterfaces(final Class<?> clazz, final Deque<Type> path, final Class<?> currentClazz) {
		for (final Type type : currentClazz.getGenericInterfaces()) {
			if (type instanceof ParameterizedType) {
				final ParameterizedType parameterizedType = (ParameterizedType) type;
				if (clazz.equals(parameterizedType.getRawType())) {
					path.add(parameterizedType);
				}
			}
		}
	}

	/**
	 * Processing step with no results.
	 * 
	 * @param originatingIndex
	 *            The first index.
	 * @param currentIndex
	 *            The current index.
	 * @param type
	 *            The type.
	 * @return The new type.
	 */
	private static Type processNoResult(final int originatingIndex, final int currentIndex, final Type type) {
		Type result = null;
		if (type instanceof Class) {
			final Class<?> clazz = (Class<?>) type;
			if (clazz.getTypeParameters().length > originatingIndex) {
				result = clazz.getTypeParameters()[currentIndex];
			}
		} else if (type instanceof ParameterizedType) {
			final ParameterizedType parameterizedType = (ParameterizedType) type;
			final Type[] types = parameterizedType.getActualTypeArguments();
			if (types.length > originatingIndex) {
				result = types[currentIndex];
			}
		}
		return result;
	}

	/**
	 * Process the path.
	 * 
	 * @param currentType
	 *            The current type.
	 * @param clazz
	 *            The type
	 * @param path
	 *            The path.
	 */
	private static void processPath(final Class<?> currentType, final Class<?> clazz, final Deque<Type> path) {
		final Class<?> currentClazz = currentType;
		GenericsUtil.processInterfaces(clazz, path, currentClazz);
		// We haven't found it yet, try the superclasses.
		final Type superClass = currentClazz.getGenericSuperclass();
		if ((path.size() < 2) && !CheckUtil.isNull(superClass)) {
			path.addAll(GenericsUtil.findTypePath(superClass, clazz));
		}
		// Still haven't found it, try the declaring classes.
		final Class<?> declaringClass = currentClazz.getDeclaringClass();
		if ((path.size() < 2) && !CheckUtil.isNull(declaringClass)) {
			path.addAll(GenericsUtil.findTypePath(declaringClass, clazz));
		}
	}

	/**
	 * Process the property.
	 * 
	 * @param clazz
	 *            The type.
	 * @param genericParameter
	 *            The generic parameter number.
	 * @param propertyHierarchy
	 *            The property hierachy.
	 * @param property
	 *            The property.
	 * @return The property type.
	 */
	private static Type processProperty(final Class<?> clazz, final int genericParameter, final Deque<? extends Member> propertyHierarchy,
			final Member property) {
		Type result = null;
		// Get the type path.
		final Type genericType = property.getGenericType();
		final Deque<Type> typePath = GenericsUtil.findTypePath(genericType, clazz);
		// This field is part of the path.
		if (genericType instanceof ParameterizedType) {
			typePath.push(genericType);
		}
		if (!typePath.isEmpty()) {
			// Trace to root type.
			result = GenericsUtil.traceType(property, typePath, genericParameter);
			result = GenericsUtil.handleTypeVariable(propertyHierarchy, property, result);
		}
		return result;
	}

	/**
	 * A parameter result.
	 * 
	 * @author Pal Hargitai (pal@lunarray.org)
	 */
	private static class ParameterResult {
		/** The index type. */
		private transient int index;
		/** The result type. */
		private transient Type result;

		/**
		 * Default constructor.
		 */
		public ParameterResult() {
			// Default constructor.
		}

		/**
		 * Gets the value for the index field.
		 * 
		 * @return The value for the index field.
		 */
		public final int getIndex() {
			return this.index;
		}

		/**
		 * Gets the value for the result field.
		 * 
		 * @return The value for the result field.
		 */
		public final Type getResult() {
			return this.result;
		}

		/**
		 * Sets a new value for the index field.
		 * 
		 * @param index
		 *            The new value for the index field.
		 */
		public final void setIndex(final int index) {
			this.index = index;
		}

		/**
		 * Sets a new value for the result field.
		 * 
		 * @param result
		 *            The new value for the result field.
		 */
		public final void setResult(final Type result) {
			this.result = result;
		}
	}
}
