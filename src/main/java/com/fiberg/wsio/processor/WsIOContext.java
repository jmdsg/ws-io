package com.fiberg.wsio.processor;

import com.fiberg.wsio.enumerate.WsIOGenerate;
import com.fiberg.wsio.enumerate.WsIOType;
import com.fiberg.wsio.util.WsIOUtil;
import com.squareup.javapoet.*;
import io.vavr.API;
import io.vavr.Tuple;
import io.vavr.Tuple2;
import io.vavr.collection.List;
import io.vavr.collection.Map;
import io.vavr.collection.Set;
import org.apache.commons.lang3.StringUtils;

import javax.lang.model.element.ElementKind;
import javax.lang.model.element.Name;
import javax.lang.model.element.TypeElement;
import javax.lang.model.type.DeclaredType;
import javax.lang.model.type.TypeMirror;
import java.util.Objects;

/**
 * Class used to hold context state and to perform context related operations.
 */
class WsIOContext {

	/** Main class identifier */
	private final WsIOIdentifier mainIdentifier;

	/** Wrapper class identifier */
	private final WsIOIdentifier wrapperIdentifier;

	/** Map of message classes and package name */
	private final Map<String, String> messageClasses;

	/** Map of clone classes and package name */
	private final Map<String, String> cloneClasses;

	/** Map of clone message classes and package name */
	private final Map<String, String> cloneMessageClasses;

	/** Map of type elements by name */
	private final Map<String, TypeElement> typeByName;

	/** Flag to indicate if the class is a clone, a message or a clone message */
	private final WsIOGenerate targetGenerate;

	/** Indicates the target type of the context */
	private final WsIOType targetType;

	/**
	 * Class constructor
	 *
	 * @param mainIdentifier identifier of the class
	 * @param wrapperIdentifier identifier of the wrapper
	 * @param messageClasses map of message classes and package name
	 * @param cloneClasses map of cloned classes and package name
	 * @param cloneMessageClasses map of cloned message classes and package name
	 * @param typeByName map of type elements by name
	 * @param targetGenerate indicates if the class is a clone, a message or a clone message
	 * @param targetType indicates the target type
	 */
	WsIOContext(WsIOIdentifier mainIdentifier,
				WsIOIdentifier wrapperIdentifier,
				Map<String, String> messageClasses,
				Map<String, String> cloneClasses,
				Map<String, String> cloneMessageClasses,
				Map<String, TypeElement> typeByName,
				WsIOGenerate targetGenerate,
				WsIOType targetType) {

		/* Assign fields */
		this.mainIdentifier = mainIdentifier;
		this.wrapperIdentifier = wrapperIdentifier;
		this.messageClasses = messageClasses;
		this.cloneClasses = cloneClasses;
		this.cloneMessageClasses = cloneMessageClasses;
		this.typeByName = typeByName;
		this.targetGenerate = targetGenerate;
		this.targetType = targetType;

	}

	public static WsIOContext of(WsIOIdentifier mainIdentifier,
								 WsIOIdentifier wrapperIdentifier,
								 Map<String, String> messageClasses,
								 Map<String, String> cloneClasses,
								 Map<String, String> cloneMessageClasses,
								 Map<String, TypeElement> typeByName,
								 WsIOGenerate targetGenerate,
								 WsIOType targetType) {
		return new WsIOContext(
				mainIdentifier,
				wrapperIdentifier,
				messageClasses,
				cloneClasses,
				cloneMessageClasses,
				typeByName,
				targetGenerate,
				targetType
		);
	}

	/**
	 * Getter method of field mainIdentifier.
	 *
	 * @return identifier of the class
	 */
	public WsIOIdentifier getMainIdentifier() {
		return mainIdentifier;
	}

	/**
	 * Getter method of field wrapperIdentifier.
	 *
	 * @return identifier of the wrapper
	 */
	public WsIOIdentifier getWrapperIdentifier() {
		return wrapperIdentifier;
	}

	/**
	 * Getter method of field messageClasses.
	 *
	 * @return map of message classes and package name
	 */
	public Map<String, String> getMessageClasses() {
		return messageClasses;
	}

	/**
	 * Getter method of field cloneClasses.
	 *
	 * @return map of clone classes and package name
	 */
	public Map<String, String> getCloneClasses() {
		return cloneClasses;
	}

	/**
	 * Getter method of field cloneMessageClasses.
	 *
	 * @return map of clone message classes and package name
	 */
	public Map<String, String> getCloneMessageClasses() {
		return cloneMessageClasses;
	}

	/**
	 * Getter method of field typeByName.
	 *
	 * @return map of type elements by name
	 */
	public Map<String, TypeElement> getTypeByName() {
		return typeByName;
	}

	/**
	 * Getter method of field targetGenerate.
	 *
	 * @return an enum that indicates if the class is a clone, a message or a clone message
	 */
	public WsIOGenerate getTargetGenerate() {
		return targetGenerate;
	}

	/**
	 * Getter method of field targetGenerate.
	 *
	 * @return the target type of the context
	 */
	public WsIOType getTargetType() {
		return targetType;
	}

	/**
	 * With method to create a new context with a specified generate.
	 *
	 * @param targetGenerate an enum that indicates if the class is a clone, a message or a clone message
	 * @return a new context with the new generate
	 */
	public WsIOContext withGenerate(WsIOGenerate targetGenerate) {

		/* Return new context with all fields */
		return WsIOContext.of(
				mainIdentifier, wrapperIdentifier,
				messageClasses, cloneClasses, cloneMessageClasses,
				typeByName, targetGenerate, targetType
		);

	}

	/**
	 * With method to create a new context with a specified names.
	 *
	 * @param mainIdentifier identifier of the class
	 * @param wrapperIdentifier identifier of the wrapper
	 * @return a new context with the new names
	 */
	public WsIOContext withIdentifier(WsIOIdentifier mainIdentifier,
									  WsIOIdentifier wrapperIdentifier) {

		/* Return new context with all fields */
		return WsIOContext.of(
				mainIdentifier, wrapperIdentifier,
				messageClasses, cloneClasses, cloneMessageClasses,
				typeByName, targetGenerate, targetType
		);

	}

	/**
	 * Method that checks if a type is internal or not.
	 *
	 * @param elementName name of the element
	 * @return {@code true} if a type is internal {@code false} otherwise;
	 */
	boolean isInternalType(String elementName) {

		/* Check if the element class name is in clone and message classes */
		boolean inMessage = messageClasses.keySet().contains(elementName);
		boolean inClone = cloneClasses.keySet().contains(elementName);

		/* Return if is cloned or message */
		return (WsIOGenerate.MESSAGE.equals(targetGenerate) && inMessage)
				|| (WsIOGenerate.CLONE.equals(targetGenerate) && inClone)
				|| (WsIOGenerate.CLONE_MESSAGE.equals(targetGenerate) && (inMessage || inClone));

	}

	/**
	 * Method that checks recursively if a generic type is internal or not;
	 *
	 * @param mirror mirror type
	 * @return {@code true} if a type is recursively internal type {@code false} otherwise;
	 */
	boolean isRecursiveGenericInternalType(TypeMirror mirror) {

		/* Get array info */
		Tuple2<TypeMirror, Integer> info = WsIOUtils.getArrayInfo(mirror);

		/* Get clear mirror type */
		TypeMirror clearMirror = info._1();

		/* Check clear mirror is declared */
		if (clearMirror instanceof DeclaredType) {

			/* Get declared type, element and name */
			DeclaredType declaredType = (DeclaredType) clearMirror;
			TypeElement typeElement = (TypeElement) declaredType.asElement();
			String elementName = typeElement.getQualifiedName().toString();

			/* Return true if this element is internal */
			if (isInternalType(elementName)) {
				return true;
			}

		}

		/* Get current level cleared generics */
		List<TypeMirror> firstLevelGenerics = WsIOUtils.getClearedGenericTypes(clearMirror);

		/* Get current level cleared generic names */
		List<String> firstLevelGenericNames = firstLevelGenerics.filter(DeclaredType.class::isInstance)
				.map(DeclaredType.class::cast)
				.map(DeclaredType::asElement)
				.filter(TypeElement.class::isInstance)
				.map(TypeElement.class::cast)
				.map(TypeElement::getQualifiedName)
				.map(Name::toString);

		/* Return true when type of current level is internal or recursively check the next levels */
		return firstLevelGenericNames.exists(this::isInternalType)
				|| firstLevelGenerics.exists(this::isRecursiveGenericInternalType);

	}

	/**
	 * Method that create a recursively generic type
	 *
	 * @param mirror mirror type
	 * @param useInternalTypes indicates if internal types are going to be used or not
	 * @param useCollectionImplementations indicates if collection interfaces should be replaced by implementations
	 * @param useWildcards indicates if wildcard types should be used or not
	 * @return recursively generic type
	 */
	TypeName getRecursiveFullTypeName(TypeMirror mirror,
	                                  boolean useInternalTypes,
	                                  boolean useCollectionImplementations,
	                                  boolean useWildcards) {

		/* Get array info */
		Tuple2<TypeMirror, Integer> info = WsIOUtils.getArrayInfo(mirror);

		/* Mirror type and array count */
		TypeMirror type = info._1();
		int arrayCount = info._2();

		/* Declare empty list for generic types */
		List<TypeName> genericTypes = List.empty();

		/* Check type is declared type */
		if (type instanceof DeclaredType) {

			/* Declared type */
			DeclaredType declaredType = (DeclaredType) type;

			/* Get all type arguments of declared type and type element*/
			List<TypeMirror> generics = List.ofAll(declaredType.getTypeArguments());
			TypeElement element = (TypeElement) declaredType.asElement();

			/* Get the element simple class name */
			String elementName = element.getQualifiedName().toString();

			/* Iterate for each generic type */
			for (TypeMirror typeMirror : generics) {

				/* Unwild the generic and check if is instance of declared type */
				Tuple2<TypeMirror, WsIOWild> unwildType = WsIOUtils.getUnwildTypeWithBounds(typeMirror);
				TypeMirror wildMirror = unwildType._1();
				WsIOWild wildType = unwildType._2();

				/* Recursively call full type name and add result to list */
				TypeName typeName = getRecursiveFullTypeName(wildMirror, useInternalTypes,
						useCollectionImplementations, useWildcards);

				/* Check the use of wildcards */
				if (useWildcards) {

					/* Check the wild type */
					if (WsIOWild.EXTENDS.equals(wildType)) {

						/* Create a subtype wild card name */
						typeName = WildcardTypeName.subtypeOf(typeName);

					} else if (WsIOWild.SUPER.equals(wildType)) {

						/* Create a supertype wild card name */
						typeName = WildcardTypeName.supertypeOf(typeName);

					}

				}

				/* Add the generic to list */
				genericTypes = genericTypes.append(typeName);

			}

			/* Get the generic array */
			TypeName[] genericsArray = genericTypes.toJavaArray(TypeName[]::new);

			/* Check if is internal type or not */
			ClassName className;
			if (useInternalTypes && isInternalType(elementName)) {

				/* Name by generate */
				WsIOGenerate realType = getTypeByGenerate(elementName);
				WsIOIdentifier nameByGenerate = getNameByGenerate(elementName);

				/* Prefix and suffix names */
				String prefixName = nameByGenerate.getIdentifierPrefix();
				String suffixName = nameByGenerate.getIdentifierSuffix();

				/* Package class name */
				String packageName = StringUtils.EMPTY;
				if (WsIOGenerate.MESSAGE.equals(realType)) {

					/* Package name of the message */
					packageName = messageClasses.get(elementName).getOrNull();

				} else if (WsIOGenerate.CLONE.equals(realType)) {

					/* Package name of the clone */
					packageName = cloneClasses.get(elementName).getOrNull();

				} else if (WsIOGenerate.CLONE_MESSAGE.equals(realType)) {

					/* Package name of the clone message */
					packageName = cloneMessageClasses.get(elementName).getOrNull();

				}

				/* Full class name and class name with package */
				String fullName = WsIOUtils.getFullSimpleInnerName(element);
				String fullWrapperName = WsIOUtil.addWrap(fullName, prefixName, suffixName);

				/* Assign class name */
				className = ClassName.get(packageName, fullWrapperName);

			} else {

				/* Declare the class name and check if is a collection */
				if (useCollectionImplementations && WsIOCollection.IMPLEMENTATIONS.containsKey(elementName)) {

					/* Assign the class name of the collection */
					className = WsIOUtils.getClassName(WsIOCollection.IMPLEMENTATIONS.get(elementName));

				} else {

					/* Assign the class name */
					className = ClassName.get(element);

				}

			}

			/* Declare type name and check if it has generics */
			TypeName typeName;
			if (genericsArray.length > 0) {

				/* A parametrized type name when generics are present */
				typeName = ParameterizedTypeName.get(className, genericsArray);

			} else {

				/* Set class name as type name */
				typeName = className;

			}

			/* Check if type is an array */
			if (arrayCount > 0) {

				/* Return the array of the type name */
				TypeName array = typeName;
				for (int index = 0; index < arrayCount; index++) {
					array = ArrayTypeName.of(array);
				}

				/* Return array */
				return array;

			} else {

				/* Return type name when is not an array */
				return typeName;

			}

		} else {

			/* Return the type name when type is not mirror nor array */
			return TypeName.get(mirror);

		}

	}

	/**
	 * Method that gets the current prefix and suffix.
	 *
	 * @param elementName name of the element
	 * @return a tuple containing the prefix and suffix name
	 */
	WsIOIdentifier getNameByGenerate(String elementName) {

		/* Get generate real type */
		WsIOGenerate type = getTypeByGenerate(elementName);

		/* Check type is not ull */
		if (Objects.nonNull(type)) {

			String prefixClassName = mainIdentifier.getIdentifierPrefix();
			String suffixClassName = mainIdentifier.getIdentifierSuffix();
			String prefixWrapperName = wrapperIdentifier.getIdentifierPrefix();
			String suffixWrapperName = wrapperIdentifier.getIdentifierSuffix();

			/* Switch the current type and return tuple */
			switch (type) {

				case CLONE_MESSAGE:

					/* Return full prefix name and suffix */
					return WsIOIdentifier.of(prefixWrapperName + prefixClassName, suffixClassName + suffixWrapperName);

				case MESSAGE:

					/* Return wrapper prefix and suffix name because element is not in clone classes */
					return WsIOIdentifier.of(prefixWrapperName, suffixWrapperName);

				case CLONE:

					/* Return class prefix and suffix name because element is not in message classes */
					return WsIOIdentifier.of(prefixClassName, suffixClassName);

				default:

					break;
			}

		}

		/* Throw illegal state exception */
		throw new IllegalStateException("The generate names could not be determined");

	}

	/**
	 * Method that gets the current generate type.
	 *
	 * @param elementName name of the element
	 * @return generate type
	 */
	private WsIOGenerate getTypeByGenerate(String elementName) {

		/* Check generate and if the element is in clone and message classes */
		if (WsIOGenerate.CLONE_MESSAGE.equals(targetGenerate)
				&& cloneMessageClasses.containsKey(elementName)) {

			/* Return clone message type */
			return WsIOGenerate.CLONE_MESSAGE;

		} else if (WsIOGenerate.MESSAGE.equals(targetGenerate)
				|| (WsIOGenerate.CLONE_MESSAGE.equals(targetGenerate)
				&& messageClasses.containsKey(elementName))) {

			/* Return message type */
			return WsIOGenerate.MESSAGE;

		} else if (WsIOGenerate.CLONE.equals(targetGenerate)
				|| (WsIOGenerate.CLONE_MESSAGE.equals(targetGenerate)
				&& cloneClasses.containsKey(elementName))) {

			/* Return clone type */
			return WsIOGenerate.CLONE;

		} else {

			/* Return null when no match is found */
			return null;

		}

	}

	/**
	 * Method that generates the candidates class names and package separated
	 * joined by dot and them separated with commas.
	 *
	 * @param element type element
	 * @return set of candidates class full names
	 */
	Set<String> getCandidateForceClasses(TypeElement element) {

		/* Candidates class types that implements the interface */
		Set<TypeElement> candidates = typeByName.values()
				.filter(type -> ElementKind.CLASS.equals(type.getKind()))
				.filter(type -> WsIOUtils.implementsSuperType(type, element))
				.toSet();

		/* Real type and name by generate */
		String elementName = element.getQualifiedName().toString();
		WsIOGenerate realType = getTypeByGenerate(elementName);
		WsIOIdentifier nameByGenerate = getNameByGenerate(elementName);

		/* Prefix and suffix names */
		String prefixName = nameByGenerate.getIdentifierPrefix();
		String suffixName = nameByGenerate.getIdentifierSuffix();

		/* Return candidates class names and package separated joined by dot and them separated with commas */
		return API.Match(realType).option(
				API.Case(API.$(WsIOGenerate.MESSAGE), messageClasses),
				API.Case(API.$(WsIOGenerate.CLONE), cloneClasses),
				API.Case(API.$(WsIOGenerate.CLONE_MESSAGE), cloneMessageClasses))
				.toStream()
				.flatMap(map -> candidates
						.filter(type -> map.keySet().contains(type.getQualifiedName().toString()))
						.map(type -> Tuple.of(WsIOUtils.getFullSimpleInnerName(type),
								map.get(type.getQualifiedName().toString()).getOrNull())))
				.map(tuple -> tuple.map1(className -> WsIOUtil.addWrap(className, prefixName, suffixName)))
				.map(tuple -> WsIOUtil.addPrefixName(tuple._1(), tuple._2()))
				.toSet();

	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public boolean equals(Object o) {
		if (this == o) return true;
		if (o == null || getClass() != o.getClass()) return false;
		WsIOContext that = (WsIOContext) o;
		return Objects.equals(mainIdentifier, that.mainIdentifier) && Objects.equals(wrapperIdentifier, that.wrapperIdentifier) && Objects.equals(messageClasses, that.messageClasses) && Objects.equals(cloneClasses, that.cloneClasses) && Objects.equals(cloneMessageClasses, that.cloneMessageClasses) && Objects.equals(typeByName, that.typeByName) && targetGenerate == that.targetGenerate && targetType == that.targetType;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public int hashCode() {
		return Objects.hash(mainIdentifier, wrapperIdentifier, messageClasses, cloneClasses, cloneMessageClasses, typeByName, targetGenerate, targetType);
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public String toString() {
		return "WsIOContext{" +
				"mainIdentifier=" + mainIdentifier +
				", wrapperIdentifier=" + wrapperIdentifier +
				", messageClasses=" + messageClasses +
				", cloneClasses=" + cloneClasses +
				", cloneMessageClasses=" + cloneMessageClasses +
				", typeByName=" + typeByName +
				", targetGenerate=" + targetGenerate +
				", targetType=" + targetType +
				'}';
	}

}
