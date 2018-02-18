/*
 * Copyright (C) 2018 Elias N Vasylenko <eliasvasylenko@strangeskies.co.uk>
 *      __   _______  ____           _       __     _      __       __
 *    ,`_ `,|__   __||  _ `.        / \     |  \   | |  ,-`__`¬  ,-`__`¬
 *   ( (_`-'   | |   | | ) |       / . \    | . \  | | / .`  `' / .`  `'
 *    `._ `.   | |   | |<. L      / / \ \   | |\ \ | || |    _ | '--.
 *   _   `. \  | |   | |  `.`.   / /   \ \  | | \ \| || |   | || +--'
 *  \ \__.' /  | |   | |    \ \ / /     \ \ | |  \ ` | \ `._' | \ `.__,.
 *   `.__.-`   |_|   |_|    |_|/_/       \_\|_|   \__|  `-.__.J  `-.__.J
 *                   __    _         _      __      __
 *                 ,`_ `, | |  _    | |  ,-`__`¬  ,`_ `,
 *                ( (_`-' | | ) |   | | / .`  `' ( (_`-'
 *                 `._ `. | L-' L   | || '--.     `._ `.
 *                _   `. \| ,.-^.`. | || +--'    _   `. \
 *               \ \__.' /| |    \ \| | \ `.__,.\ \__.' /
 *                `.__.-` |_|    |_||_|  `-.__.J `.__.-`
 *
 * This file is part of uk.co.strangeskies.reflection.
 *
 * uk.co.strangeskies.reflection is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * uk.co.strangeskies.reflection is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program. If not, see <http://www.gnu.org/licenses/>.
 */
package uk.co.strangeskies.reflection;

import static java.util.stream.Collectors.toMap;

import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.lang.reflect.TypeVariable;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import uk.co.strangeskies.utility.Isomorphism;

/**
 * A {@link CaptureConversion} is a special sort of bound which can be contained
 * within a {@link BoundSet}. It represents a capture conversion, as the process
 * is described in the Java 8 language specification.
 * <p>
 * The captures made by this capture conversion are not yet fully instantiated,
 * meaning that the types of the capturing variables, and the bounds on those
 * types, may involve {@link InferenceVariable}s.
 * 
 * @author Elias N Vasylenko
 */
public class CaptureConversion {
	private final ParameterizedType originalType;
	private final ParameterizedType captureType;

	private final Map<InferenceVariable, TypeVariable<?>> capturedParameters = new HashMap<>();
	private final Map<InferenceVariable, Type> capturedArguments = new HashMap<>();

	private CaptureConversion(ParameterizedType originalType, Map<TypeVariable<?>, InferenceVariable> parameterCaptures) {
		this.originalType = originalType;

		captureType = ParameterizedTypes.parameterize(Types.getErasedType(originalType), parameterCaptures::get);

		ParameterizedTypes.getAllTypeArguments(originalType).forEach(e -> {
			Type argument = e.getValue();
			InferenceVariable inferenceVariable = parameterCaptures.get(e.getKey());

			capturedArguments.put(inferenceVariable, argument);
			capturedParameters.put(inferenceVariable, e.getKey());
		});
	}

	/**
	 * Create a capture conversion over a given {@link ParameterizedType}.
	 * Arguments will be substituted with new {@link InferenceVariable}s, such
	 * that a new type is described which represents the result of capture
	 * conversion on the given type.
	 * 
	 * @param originalType
	 *          The type to capture.
	 */
	public CaptureConversion(ParameterizedType originalType) {
		this(
				originalType,
				ParameterizedTypes.getAllTypeParameters(Types.getErasedType(originalType)).collect(
						Collectors.toMap(Function.identity(), t -> new InferenceVariable())));
	}

	@Override
	public String toString() {
		return new StringBuilder()
				.append(getCaptureType().getTypeName())
				.append(" = capture(")
				.append(getOriginalType().getTypeName())
				.append(")")
				.toString();
	}

	/**
	 * @return The original type which has been captured.
	 */
	public ParameterizedType getOriginalType() {
		return originalType;
	}

	/**
	 * @return A {@link ParameterizedType} whose arguments are the same as those
	 *         in the {@link #getOriginalType() original type}, or in the case of
	 *         {@link WildcardTypes}, the {@link InferenceVariable}s which capture
	 *         those arguments.
	 */
	public ParameterizedType getCaptureType() {
		return captureType;
	}

	/**
	 * @return The set of inference variables created through this capture
	 *         conversion operation.
	 */
	public Set<InferenceVariable> getInferenceVariables() {
		return capturedParameters.keySet();
	}

	/**
	 * @param variable
	 *          An inference variable which may represent a capture which is part
	 *          of this capture conversion.
	 * @return The argument of the {@link #getOriginalType() original type}
	 *         captured by a given {@link InferenceVariable}.
	 */
	public Type getCapturedArgument(InferenceVariable variable) {
		return capturedArguments.get(variable);
	}

	/**
	 * @param variable
	 *          An inference variable which may represent a capture which is part
	 *          of this capture conversion.
	 * @return The parameter of the {@link #getOriginalType() original type}
	 *         captured by a given {@link InferenceVariable}.
	 */
	public TypeVariable<?> getCapturedParameter(InferenceVariable variable) {
		return capturedParameters.get(variable);
	}

	/**
	 * Substitute any mentions of the inference variables present as keys in the
	 * given map with their associated values in the map.
	 * 
	 * @param isomorphism
	 *          an isomorphism for inference variables
	 * @return A new {@link CaptureConversion} instance which is equal to the
	 *         receiving instance but for the substitutions made.
	 */
	public CaptureConversion withInferenceVariableSubstitution(Isomorphism isomorphism) {
		ParameterizedType newType = (ParameterizedType) new TypeSubstitution()
				.withIsomorphism(isomorphism)
				.resolve(getOriginalType());

		Map<TypeVariable<?>, InferenceVariable> newCaptures = ParameterizedTypes
				.getAllTypeArguments(getCaptureType())
				.collect(toMap(

						Entry::getKey,

						e -> isomorphism.byIdentity().getMapping(e.getValue(), t -> (InferenceVariable) t)));

		return new CaptureConversion(newType, newCaptures);
	}

	/**
	 * Find all inference variables mentioned by this capture conversion.
	 * 
	 * @return A set containing all inference variables mentioned on either side
	 *         of this capture conversion with respect to the given bound set.
	 */
	public Stream<InferenceVariable> getInferenceVariablesMentioned() {
		Set<InferenceVariable> allMentioned = new HashSet<>(getInferenceVariables());

		ParameterizedTypes.getAllTypeArguments(getOriginalType()).map(Map.Entry::getValue).forEach(
				captured -> InferenceVariable.getMentionedBy(captured).forEach(allMentioned::add));

		return allMentioned.stream();
	}
}
