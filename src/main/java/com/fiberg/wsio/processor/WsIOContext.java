package com.fiberg.wsio.processor;

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
import javax.lang.model.type.ReferenceType;
import javax.lang.model.type.TypeMirror;
import java.util.Objects;

/**
 * Class used to hold context state and to perform context related operations.
 */
class WsIOContext {

	/** Preffix class name */
	private String prefixClassName;

	/** Suffix class name */
	private String suffixClassName;

	/** Preffix wrapper name */
	private String prefixWrapperName;

	/** Suffix wrapper name */
	private String suffixWrapperName;

	/** Map of message classes and package name */
	private Map<String, String> messageClasses;

	/** Map of clone classes and package name */
	private Map<String, String> cloneClasses;

	/** Map of clone message classes and package name */
	private Map<String, String> cloneMessageClasses;

	/** Map of type elements by name */
	private Map<String, TypeElement> typeByName;

	/** Flag to indicate if the class is a clone, a message or a clone message */
	private WsIOGenerate generate;

	/**
	 * Class constructor
	 *
	 * @param messageClasses map of message classes and package name
	 * @param cloneClasses map of cloned classes and package name
	 * @param cloneMessageClasses map of cloned message classes and package name
	 * @param typeByName map of type elements by name
	 */
	WsIOContext(Map<String, String> messageClasses,
	            Map<String, String> cloneClasses,
	            Map<String, String> cloneMessageClasses,
	            Map<String, TypeElement> typeByName) {

		/* Assign fields */
		this.messageClasses = messageClasses;
		this.cloneClasses = cloneClasses;
		this.cloneMessageClasses = cloneMessageClasses;
		this.typeByName = typeByName;

	}

	/**
	 * Class constructor
	 *
	 * @param messageClasses map of message classes and package name
	 * @param cloneClasses map of cloned classes and package name
	 * @param cloneMessageClasses map of cloned message classes and package name
	 * @param typeByName map of type elements by name
	 * @param generate indicates if the class is a clone, a message or a clone message
	 */
	WsIOContext(Map<String, String> messageClasses,
	            Map<String, String> cloneClasses,
	            Map<String, String> cloneMessageClasses,
	            Map<String, TypeElement> typeByName,
	            WsIOGenerate generate) {

		/* Assign fields */
		this.messageClasses = messageClasses;
		this.cloneClasses = cloneClasses;
		this.cloneMessageClasses = cloneMessageClasses;
		this.typeByName = typeByName;
		this.generate = generate;

	}

	/**
	 * Class constructor
	 *
	 * @param prefixClassName prefix name of the class
	 * @param suffixClassName suffix name of the class
	 * @param prefixWrapperName prefix name of the wrapper
	 * @param suffixWrapperName suffix name of the wrapper
	 * @param messageClasses map of message classes and package name
	 * @param cloneClasses map of cloned classes and package name
	 * @param cloneMessageClasses map of cloned message classes and package name
	 * @param typeByName map of type elements by name
	 */
	WsIOContext(String prefixClassName,
	            String suffixClassName,
	            String prefixWrapperName,
	            String suffixWrapperName,
	            Map<String, String> messageClasses,
	            Map<String, String> cloneClasses,
	            Map<String, String> cloneMessageClasses,
	            Map<String, TypeElement> typeByName) {

		/* Assign fields */
		this.prefixClassName = prefixClassName;
		this.suffixClassName = suffixClassName;
		this.prefixWrapperName = prefixWrapperName;
		this.suffixWrapperName = suffixWrapperName;
		this.messageClasses = messageClasses;
		this.cloneClasses = cloneClasses;
		this.cloneMessageClasses = cloneMessageClasses;
		this.typeByName = typeByName;

	}

	/**
	 * Class constructor
	 *
	 * @param prefixClassName prefix name of the class
	 * @param suffixClassName suffix name of the class
	 * @param prefixWrapperName prefix name of the wrapper
	 * @param suffixWrapperName suffix name of the wrapper
	 * @param messageClasses map of message classes and package name
	 * @param cloneClasses map of cloned classes and package name
	 * @param cloneMessageClasses map of cloned message classes and package name
	 * @param typeByName map of type elements by name
	 * @param generate indicates if the class is a clone, a message or a clone message
	 */
	WsIOContext(String prefixClassName,
	            String suffixClassName,
	            String prefixWrapperName,
	            String suffixWrapperName,
	            Map<String, String> messageClasses,
	            Map<String, String> cloneClasses,
	            Map<String, String> cloneMessageClasses,
	            Map<String, TypeElement> typeByName,
	            WsIOGenerate generate) {

		/* Assign fields */
		this.prefixClassName = prefixClassName;
		this.suffixClassName = suffixClassName;
		this.prefixWrapperName = prefixWrapperName;
		this.suffixWrapperName = suffixWrapperName;
		this.messageClasses = messageClasses;
		this.cloneClasses = cloneClasses;
		this.cloneMessageClasses = cloneMessageClasses;
		this.typeByName = typeByName;
		this.generate = generate;

	}

	/**
	 * Getter method of field prefixClassName.
	 *
	 * @return prefix name of the class
	 */
	public String getPrefixClassName() {
		return prefixClassName;
	}

	/**
	 * Getter method of field suffixClassName.
	 *
	 * @return suffix name of the class
	 */
	public String getSuffixClassName() {
		return suffixClassName;
	}

	/**
	 * Getter method of field prefixWrapperName.
	 *
	 * @return prefix name of the wrapper
	 */
	public String getPrefixWrapperName() {
		return prefixWrapperName;
	}

	/**
	 * Getter method of field suffixWrapperName.
	 *
	 * @return suffix name of the wrapper
	 */
	public String getSuffixWrapperName() {
		return suffixWrapperName;
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
	 * Getter method of field generate.
	 *
	 * @return a enum that indicates if the class is a clone, a message or a clone message
	 */
	public WsIOGenerate getGenerate() {
		return generate;
	}

	/**
	 * With method to create a new context with a specified generate.
	 *
	 * @param generate a enum that indicates if the class is a clone, a message or a clone message
	 * @return a new context with the new generate
	 */
	public WsIOContext withGenerate(WsIOGenerate generate) {

		/* Return new context with all fields */
		return new WsIOContext(prefixClassName, suffixClassName, prefixWrapperName, suffixWrapperName,
				messageClasses, cloneClasses, cloneMessageClasses, typeByName, generate);

	}

	/**
	 * With method to create a new context with a specified names.
	 *
	 * @param prefixClassName prefix name of the class
	 * @param suffixClassName suffix name of the class
	 * @param prefixWrapperName prefix name of the wrapper
	 * @param suffixWrapperName suffix name of the wrapper
	 * @return a new context with the new names
	 */
	public WsIOContext withName(String prefixClassName,
	                            String suffixClassName,
	                            String prefixWrapperName,
	                            String suffixWrapperName) {

		/* Return new context with all fields */
		return new WsIOContext(prefixClassName, suffixClassName, prefixWrapperName, suffixWrapperName,
				messageClasses, cloneClasses, cloneMessageClasses, typeByName, generate);

	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public boolean equals(Object o) {
		if (this == o) return true;
		if (o == null || getClass() != o.getClass()) return false;

		WsIOContext that = (WsIOContext) o;

		if (messageClasses != null ? !messageClasses.equals(that.messageClasses) : that.messageClasses != null)
			return false;
		if (cloneClasses != null ? !cloneClasses.equals(that.cloneClasses) : that.cloneClasses != null) return false;
		if (cloneMessageClasses != null ? !cloneMessageClasses.equals(that.cloneMessageClasses) : that.cloneMessageClasses != null)
			return false;
		return generate == that.generate;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public int hashCode() {
		int result = messageClasses != null ? messageClasses.hashCode() : 0;
		result = 31 * result + (cloneClasses != null ? cloneClasses.hashCode() : 0);
		result = 31 * result + (cloneMessageClasses != null ? cloneMessageClasses.hashCode() : 0);
		result = 31 * result + (generate != null ? generate.hashCode() : 0);
		return result;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public String toString() {
		return "WsIOContext{" +
				"messageClasses=" + messageClasses +
				", cloneClasses=" + cloneClasses +
				", cloneMessageClasses=" + cloneMessageClasses +
				", generate=" + generate +
				'}';
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
		return (WsIOGenerate.MESSAGE.equals(generate) && inMessage)
				|| (WsIOGenerate.CLONE.equals(generate) && inClone)
				|| (WsIOGenerate.CLONE_MESSAGE.equals(generate) && (inMessage || inClone));

	}

	/**
	 * Method that checks recursively if a generic type is internal or not;
	 *
	 * @param referenceType reference type
	 * @return {@code true} if a type is recursively internal type {@code false} otherwise;
	 */
	boolean isRecursiveGenericInternalType(ReferenceType referenceType) {

		/* Get array info */
		Tuple2<ReferenceType, Integer> info = WsIOUtils.getArrayInfo(referenceType);

		/* Get clear reference type */
		ReferenceType clearReference = info._1();

		/* Check clear reference is declared */
		if (clearReference instanceof DeclaredType) {

			/* Get declared type, element and name */
			DeclaredType declaredType = (DeclaredType) clearReference;
			TypeElement typeElement = (TypeElement) declaredType.asElement();
			String elementName = typeElement.getQualifiedName().toString();

			/* Return true if this element is internal */
			if (isInternalType(elementName)) {
				return true;
			}

		}

		/* Get current level cleared generics */
		List<ReferenceType> firstLevelGenerics = WsIOUtils.getClearedGenericTypes(clearReference);

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
	 * @param reference reference type
	 * @param useCollectionImpl indicates if collection interfaces should be replaced by implementations
	 * @param useInternalTypes indicates if internal types are going to be used or not
	 * @return recursively generic type
	 */
	TypeName getRecursiveFullTypeName(ReferenceType reference,
	                                  boolean useCollectionImpl,
	                                  boolean useInternalTypes) {

		/* Get array info */
		Tuple2<ReferenceType, Integer> info = WsIOUtils.getArrayInfo(reference);

		/* Declared type and array count */
		ReferenceType type = info._1();
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

				if (wildMirror instanceof ReferenceType) {

					/* Recursively call full type name and add result to list */
					ReferenceType wildReferenceType = (ReferenceType) wildMirror;
					TypeName typeName = getRecursiveFullTypeName(wildReferenceType, useCollectionImpl, useInternalTypes);

					/* Check the wild type */
					if (WsIOWild.EXTENDS.equals(wildType)) {

						/* Create a subtype wild card name */
						typeName = WildcardTypeName.subtypeOf(typeName);

					} else if (WsIOWild.SUPER.equals(wildType)) {

						/* Create a supertype wild card name */
						typeName = WildcardTypeName.supertypeOf(typeName);

					}

					/* Add the generic to list */
					genericTypes = genericTypes.append(typeName);

				} else {

					/* Add type name to list */
					TypeName typeName = TypeName.get(typeMirror);
					genericTypes = genericTypes.append(typeName);

				}

			}

			/* Get the generic array */
			TypeName[] genericsArray = genericTypes.toJavaArray(TypeName.class);

			/* Check if is internal type or not */
			ClassName className;
			if (useInternalTypes && isInternalType(elementName)) {

				/* Name by generate */
				WsIOGenerate realType = getTypeByGenerate(elementName);
				Tuple2<String, String> nameByGenerate = getNameByGenerate(elementName);

				/* Prefix and suffix names */
				String prefixName = nameByGenerate._1();
				String suffixName = nameByGenerate._2();

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
				if (useCollectionImpl && WsIOCollection.IMPLEMENTATIONS.containsKey(elementName)) {

					/* Assign the class name of the collection */
					className = WsIOUtils.getClassName(WsIOCollection.IMPLEMENTATIONS.get(elementName));

				} else {

					/* Assign the class name */
					className = ClassName.get(element);

				}

			}

			/* Declare type name and check if has generics */
			TypeName typeName;
			if (genericsArray.length > 0) {

				/* A parametrized type name when generics is present */
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

			/* Return the type name when type is not declared nor array */
			return TypeName.get(reference);

		}

	}

	/**
	 * Method that gets the current prefix and suffix.
	 *
	 * @param elementName name of the element
	 * @return a tuple containing the prefix and suffix name
	 */
	Tuple2<String,String> getNameByGenerate(String elementName) {

		/* Get generate real type */
		WsIOGenerate type = getTypeByGenerate(elementName);

		/* Check type is not ull */
		if (Objects.nonNull(type)) {

			/* Switch the current type and return tuple */
			switch (type) {

				case CLONE_MESSAGE:

					/* Return full prefix name and suffix */
					return Tuple.of(prefixWrapperName + prefixClassName, suffixClassName + suffixWrapperName);

				case MESSAGE:

					/* Return wrapper prefix and suffix name because element is not in clone classes */
					return Tuple.of(prefixWrapperName, suffixWrapperName);

				case CLONE:

					/* Return class prefix and suffix name because element is not in message classes */
					return Tuple.of(prefixClassName, suffixClassName);

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
		if (WsIOGenerate.CLONE_MESSAGE.equals(generate)
				&& cloneMessageClasses.containsKey(elementName)) {

			/* Return clone message type */
			return WsIOGenerate.CLONE_MESSAGE;

		} else if (WsIOGenerate.MESSAGE.equals(generate)
				|| (WsIOGenerate.CLONE_MESSAGE.equals(generate)
				&& messageClasses.containsKey(elementName))) {

			/* Return message type */
			return WsIOGenerate.MESSAGE;

		} else if (WsIOGenerate.CLONE.equals(generate)
				|| (WsIOGenerate.CLONE_MESSAGE.equals(generate)
				&& cloneClasses.containsKey(elementName))) {

			/* Return clone type */
			return WsIOGenerate.CLONE;

		} else {

			/* Return null when no match is found */
			return null;

		}

	}

	/**
	 * Method that generates the candidates class names and package separeted
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
		Tuple2<String, String> nameByGenerate = getNameByGenerate(elementName);

		/* Prefix and suffix names */
		String prefixName = nameByGenerate._1();
		String suffixName = nameByGenerate._2();

		/* Return candidates class names and package separeted joined by dot and them separated with commas */
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

}
