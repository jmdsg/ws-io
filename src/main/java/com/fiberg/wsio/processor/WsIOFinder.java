package com.fiberg.wsio.processor;

import com.fiberg.wsio.annotation.*;
import com.fiberg.wsio.util.WsIOUtil;
import io.vavr.*;
import io.vavr.collection.*;
import io.vavr.control.Option;
import org.apache.commons.lang3.StringUtils;

import javax.jws.WebMethod;
import javax.lang.model.element.*;
import javax.lang.model.util.ElementFilter;
import java.util.Objects;
import java.util.function.Predicate;

/**
 * Class to extract clone, wrapper, message and clone message annotations info.
 */
class WsIOFinder {

	/**
	 * Private empty constructor.
	 */
	private WsIOFinder() {  }

	/**
	 * Method that finds the metadata info of a set of type elements.
	 *
	 * @param elements set of type elements
	 * @return map identified by the element type containing a tuple of 3 elements with
	 * package name, cases and a set of field names
	 */
	static Map<TypeElement, Tuple3<String, Set<Case>, Map<String, Boolean>>> findMetadataRecursively(Set<TypeElement> elements) {

		/* Return the map for each element and finally fold the results with an empty map */
		return elements.map(WsIOFinder::findMetadataRecursively)
				.fold(HashMap.empty(), Map::merge);

	}

	/**
	 * Method that finds the metadata info of a single type elements.
	 *
	 * @param element type element
	 * @return map identified by the element type containing a tuple of 3 elements with
	 * package name, cases and a set of field names
	 */
	private static Map<TypeElement, Tuple3<String, Set<Case>, Map<String, Boolean>>> findMetadataRecursively(TypeElement element) {

		/* Check if the current element is not or not */
		if (Objects.nonNull(element)) {

			/* Stream of executable methods */
			Stream<VariableElement> fields = Stream.ofAll(element.getEnclosedElements())
					.filter(VariableElement.class::isInstance)
					.map(VariableElement.class::cast);

			/* Descriptor of the type element and optional metadata */
			WsIODescriptor typeDescriptor = WsIODescriptor.of(element);
			Option<WsIOMetadata> typeMetadataOption = typeDescriptor.getSingle(WsIOMetadata.class);


			Option<Tuple3<String, Set<Case>, Map<String, Boolean>>> currentInfo = typeMetadataOption.map(metadata -> {

				/* Get class name and package name */
				String className = element.getSimpleName().toString();
				String packageName = WsIOUtils.extractPackage(element).getQualifiedName().toString();

				/* Get final package name */
				String finalPackage = WsIOEngine.obtainPackage(StringUtils.EMPTY, className, packageName,
						metadata.packageName(), metadata.packagePath(), metadata.packagePrefix(),
						metadata.packageSuffix(), metadata.packageStart(), metadata.packageMiddle(),
						metadata.packageEnd(), metadata.packageNamer());

				/* Get enabled field names */
				Map<String, Boolean> fieldNames = fields.flatMap(field -> {

					/* Descriptor of the field element and optional metadata */
					WsIODescriptor fieldDescriptor = WsIODescriptor.of(field);
					Option<WsIOMetadata> fieldMetadataOption = fieldDescriptor.getSingle(WsIOMetadata.class);

					/* Check if the field is static and create is valid predicate.
					 * The predicate checks if the metadata is defined (not skipped) and the field
					 * is not static or the static fields are enabled */
					boolean isStatic = field.getModifiers().contains(Modifier.STATIC);
					Predicate<String> isValid = ign -> fieldMetadataOption.isDefined()
							&& (!isStatic || metadata.staticFields());

					/* Return an optional tuple of field name and flag indicating if is static or not */
					return Option.of(field)
							.map(VariableElement::getSimpleName)
							.map(Name::toString)
							.filter(isValid)
							.map(name -> Tuple.of(name, isStatic));

				}).toMap(tuple -> tuple);

				/* Create a hashset with all the enabled cases */
				Set<Case> cases = HashSet.of(metadata.cases());

				/* Return the tuple with all the info */
				return Tuple.of(finalPackage, cases, fieldNames);

			});

			/* Create zero map with current element, call recursively this function with each declared class
			 * and finally fold the results with zero map */
			Map<TypeElement, Tuple3<String, Set<Case>, Map<String, Boolean>>> zeroMap = currentInfo
					.toMap(ign -> element, identity -> identity);
			return Stream.ofAll(element.getEnclosedElements())
					.filter(TypeElement.class::isInstance)
					.map(TypeElement.class::cast)
					.map(WsIOFinder::findMetadataRecursively)
					.fold(zeroMap, Map::merge);

		} else {

			/* Return empty hashmap when element is null */
			return HashMap.empty();

		}

	}

	/**
	 * Method that finds the wrapper info of a set of type elements.
	 *
	 * @param elements set of type elements
	 * @return map identified by the element type containing wrapper annotations info of all elements
	 */
	static Map<TypeElement, Map<String, Tuple2<WsIOInfo, String>>> findWrapperRecursively(Set<TypeElement> elements) {

		/* Return the map for each element and finally fold the results with an empty map */
		return elements.map(WsIOFinder::findWrapperRecursively)
				.fold(HashMap.empty(), Map::merge);

	}

	/**
	 * Method that finds the wrapper info of a single type elements.
	 *
	 * @param element type element
	 * @return map identified by the element type containing wrapper annotations info of a single element
	 */
	private static Map<TypeElement, Map<String, Tuple2<WsIOInfo, String>>> findWrapperRecursively(TypeElement element) {

		/* Check if the current element is not or not */
		if (Objects.nonNull(element)) {

			/* Stream of executable methods */
			Stream<ExecutableElement> executables = Stream.ofAll(element.getEnclosedElements())
					.filter(ExecutableElement.class::isInstance)
					.map(ExecutableElement.class::cast)
					.filter(executable -> !"<init>".equals(executable.getSimpleName().toString()))
					.filter(executable -> Objects.nonNull(executable.getAnnotation(WebMethod.class)));

			/* Get current element info for every method */
			Map<String, Tuple2<WsIOInfo, String>> currentInfo = executables.flatMap(executable -> {

				/* Get simple name, current descriptor and annotation option */
				String executableName = executable.getSimpleName().toString();
				WsIODescriptor descriptor = WsIODescriptor.of(executable);
				Option<WsIOAnnotation> messageWrapperOption = descriptor.getSingle(WsIOMessageWrapper.class)
						.map(WsIOAnnotation::of);

				/* Return the tuple of method name, and another tuple with method info and package name */
				return messageWrapperOption.map(wrapper -> {

					/* Get class name and package name */
					String className = element.getSimpleName().toString();
					String packageName = WsIOUtils.extractPackage(element).getQualifiedName().toString();

					/* Get final package name */
					String finalPackage = WsIOEngine.obtainPackage(executableName, className, packageName,
							wrapper.getPackageName(), wrapper.getPackagePath(), wrapper.getPackagePrefix(),
							wrapper.getPackageSuffix(), wrapper.getPackageStart(), wrapper.getPackageMiddle(),
							wrapper.getPackageEnd(), wrapper.getPackageNamer());

					/* Get current info of the executable with descriptor annotations descriptor */
					WsIOInfo info = WsIOUtils.extractInfo(executable, descriptor);

					/* Return the tuple with executable name and
					 * other tuple with executable info and package name */
					return Tuple.of(executableName, Tuple.of(info, finalPackage));

				});

			}).toMap(tuple -> tuple);

			/* Create zero map with current element, call recursively this function with each declared class
			 * and finally fold the results with zero map */
			Map<TypeElement, Map<String, Tuple2<WsIOInfo, String>>> zeroMap = HashMap.of(element, currentInfo);
			return Stream.ofAll(element.getEnclosedElements())
					.filter(TypeElement.class::isInstance)
					.map(TypeElement.class::cast)
					.map(WsIOFinder::findWrapperRecursively)
					.fold(zeroMap, Map::merge);

		} else {

			/* Return empty hashmap when element is null */
			return HashMap.empty();

		}

	}

	/**
	 * Method that finds the message info of a set of type elements.
	 *
	 * @param elements set of type elements
	 * @return map identified by the element type containing message annotations info of all elements
	 */
	static Map<TypeElement, String> findMessageRecursively(Set<TypeElement> elements) {

		/* Return the map for each element and finally fold the results with an empty map */
		return elements.map(WsIOFinder::findMessageRecursively)
				.fold(HashMap.empty(), Map::merge);

	}

	/**
	 * Method that finds the message info of a type elements.
	 *
	 * @param element type elements
	 * @return map identified by the element type containing wrapper annotations info of a single element
	 */
	private static Map<TypeElement, String> findMessageRecursively(TypeElement element) {

		/* Check if the current element is not or not */
		if (Objects.nonNull(element) && isValid(element)) {

			/* Get current descriptor and the message option */
			WsIODescriptor descriptor = WsIODescriptor.of(element);
			Option<WsIOAnnotation> messageOption = descriptor.getSingle(WsIOMessage.class)
					.map(WsIOAnnotation::of);

			/* Get current map with message info */
			Map<TypeElement, String> current = messageOption.toMap(message -> element,
					message -> {

						/* Get current class and package names */
						String className = element.getSimpleName().toString();
						String packageName = WsIOUtils.extractPackage(element).getQualifiedName().toString();

						/* Return final package name */
						return WsIOEngine.obtainPackage(StringUtils.EMPTY, className, packageName,
								message.getPackageName(), message.getPackagePath(), message.getPackagePrefix(),
								message.getPackageSuffix(), message.getPackageStart(), message.getPackageMiddle(),
								message.getPackageEnd(), message.getPackageNamer());

					});

			/* Return the recursive call of the function for each enclosing element and fold with current map */
			return Stream.ofAll(element.getEnclosedElements())
					.filter(TypeElement.class::isInstance)
					.map(TypeElement.class::cast)
					.map(WsIOFinder::findMessageRecursively)
					.fold(current, Map::merge);

		} else {

			/* Return empty hashmap when element is null */
			return HashMap.empty();

		}

	}

	/**
	 * Method that finds the clone info of a set of type elements.
	 *
	 * @param elements set of type elements
	 * @return map identified by the prefix and suffix containing a set
	 * of tuples with the type element and the package name
	 */
	static Map<Tuple2<String, String>, Set<Tuple2<TypeElement, String>>> findCloneRecursively(Set<TypeElement> elements) {

		/* Return the map for each element and finally fold the results with an empty map */
		return elements.map(WsIOFinder::findCloneRecursively)
				.fold(HashMap.empty(), (map1, map2) -> map1.merge(map2, Set::addAll));

	}

	/**
	 * Method that finds the clone info of a type element.
	 *
	 * @param element type element
	 * @return map identified by the prefix and suffix containing a set
	 * of tuples with the type element and the package name
	 */
	private static Map<Tuple2<String, String>, Set<Tuple2<TypeElement, String>>> findCloneRecursively(TypeElement element) {

		/* Check if the current element is not or not */
		if (Objects.nonNull(element) && isValid(element)) {

			/* Get current element descriptor with annotations info */
			WsIODescriptor descriptor = WsIODescriptor.of(element);
			Map<Tuple2<String, String>, WsIOAnnotation> clones = descriptor.getMultiple(WsIOClone.class)
					.map(((comparable, clone) -> Tuple.of(Tuple.of(clone.prefix(), clone.suffix()),
							WsIOAnnotation.of(clone))));

			/* Get current clone annotations info */
			Map<Tuple2<String, String>, Set<Tuple2<TypeElement, String>>> current = clones.mapValues(clone -> {

				/* Get class, package and final package name */
				String className = element.getSimpleName().toString();
				String packageName = WsIOUtils.extractPackage(element).getQualifiedName().toString();
				String finalPackage = WsIOEngine.obtainPackage(StringUtils.EMPTY, className, packageName,
						clone.getPackageName(), clone.getPackagePath(), clone.getPackagePrefix(),
						clone.getPackageSuffix(), clone.getPackageStart(), clone.getPackageMiddle(),
						clone.getPackageEnd(), clone.getPackageNamer());

				/* Return the tuple of element and final package name */
				return Tuple.of(element, finalPackage);

			}).mapValues(HashSet::of);

			/* Call recursively the function with each enclosing type and the fold to the current values */
			return Stream.ofAll(element.getEnclosedElements())
					.filter(TypeElement.class::isInstance)
					.map(TypeElement.class::cast)
					.map(WsIOFinder::findCloneRecursively)
					.fold(current, (map1, map2) -> map1.merge(map2, Set::addAll));

		} else {

			/* Return empty hashmap when element is null */
			return HashMap.empty();

		}

	}

	/**
	 * Method that takes the message classes and clone classes and return the message clone class info.
	 *
	 * @param messages map with message info
	 * @param clones   map with clone info
	 * @return map with message clone class info
	 */
	static Map<Tuple2<String, String>, Set<Tuple2<TypeElement, String>>> findCloneMessage(Map<TypeElement, String> messages,
	                                                                                      Map<Tuple2<String, String>, Set<Tuple2<TypeElement, String>>> clones) {

		/* Function that obtains the type element and package name given the prefix, suffix, type element and current package */
		Function4<String, String, TypeElement, String, Option<Tuple2<TypeElement, String>>> transformPackage =
				(prefix, suffix, element, currentPackage) -> {

					/* Get current element descriptor with annotations info */
					WsIODescriptor descriptor = WsIODescriptor.of(element);
					Map<Tuple2<String, String>, WsIOAnnotation> infos = descriptor.getMultiple(WsIOClone.class)
							.map(((comparable, clone) -> Tuple.of(Tuple.of(clone.prefix(), clone.suffix()),
									WsIOAnnotation.of(clone))));

					/* Get current clone by identifier and return tuple if option is defined */
					Tuple2<String, String> identifier = Tuple.of(prefix, suffix);
					return infos.get(identifier)
							.map(clone -> {

								/* Get class, package and final package name */
								String className = element.getSimpleName().toString();
								String packageName = WsIOUtils.extractPackage(element).getQualifiedName().toString();
								String finalPackage = WsIOEngine.obtainPackage(StringUtils.EMPTY, className, packageName,
										clone.getPackageName(), clone.getPackagePath(), clone.getPackagePrefix(),
										clone.getPackageSuffix(), clone.getPackageStart(), clone.getPackageMiddle(),
										clone.getPackageEnd(), clone.getPackageNamer());

								/* Return tuple with type element and package name */
								return Tuple.of(element, finalPackage);

							});

				};

		/* Get current clone that are present in message info map */
		Map<Tuple2<String, String>, Set<Tuple2<TypeElement, String>>> present = clones
				.mapValues(set -> set.filter(tuple -> messages.keySet().contains(tuple._1())))
				.filter((key, value) -> value.nonEmpty());

		/* Return the package name transformed of the clone messages that are present */
		return present.map((identifier, set) -> Tuple.of(identifier, set.flatMap(tuple ->
				transformPackage.apply(identifier._1(), identifier._2(), tuple._1(), tuple._2()))));

	}

	/**
	 * Method that checks is a type element is a message generated class.
	 *
	 * @param element type element to check
	 * @return {@code true} if type is a message generated class.
	 */
	static boolean isMessageGenerated(TypeElement element) {

		/* Extract the current type delegator type and check if is non null */
		TypeElement type = WsIOUtils.extractDelegatorType(element);
		if (Objects.nonNull(type)) {

			/* Get root type element */
			TypeElement root = WsIOUtils.extractTypes(type)
					.headOption()
					.getOrElse(type);

			/* Find the message recursively */
			Map<TypeElement, String> searchedType = findMessageRecursively(HashSet.of(root))
					.filterKeys(type::equals);

			/* Check if the searched element is in the message map */
			if (searchedType.nonEmpty()) {

				/* Get the searched class and package names */
				String searchedName = type.getSimpleName().toString();
				String searchedPackage = searchedType.get(type)
						.getOrNull();

				/* Get the current class and package names */
				String elementName = element.getSimpleName().toString();
				String elementPackage = WsIOUtils.extractPackage(element)
						.getQualifiedName()
						.toString();

				/* Get searched inner name of the class */
				String searchedInnerName = WsIOUtils.extractTypes(type)
						.map(TypeElement::getSimpleName)
						.map(Name::toString)
						.append(searchedName)
						.mkString();

				/* Add the prefixes and suffixes to the names to create request and response full names */
				String requestName = WsIOUtil.addWrap(searchedInnerName, WsIOConstant.REQUEST_PREFIX,
						WsIOConstant.REQUEST_SUFFIX);
				String responseName = WsIOUtil.addWrap(searchedInnerName, WsIOConstant.RESPONSE_PREFIX,
						WsIOConstant.RESPONSE_SUFFIX);

				/* Check if the searched and current package names are the same,
				 * and if the name is equal to the request or response name */
				return StringUtils.equals(searchedPackage, elementPackage)
						&& (StringUtils.equals(requestName, elementName)
						|| StringUtils.equals(responseName, elementName));

			}

		}

		/* Return false when delegator type could not be found */
		return false;

	}

	/**
	 * Method that checks is a type element is a metadata generated class.
	 *
	 * @param element type element to check
	 * @return {@code true} if type is a metadata generated class.
	 */
	static boolean isMetadataGenerated(TypeElement element) {

		/* Extract the current type delegate type and check if is non null */
		TypeElement type = WsIOUtils.extractMetadataType(element);
		if (Objects.nonNull(type)) {

			/* Check if the field is annotated for metadata */
			return findMetadataRecursively(type)
					.containsKey(type);

		}

		/* Return false when delegator type could not be found */
		return false;

	}

	/**
	 * Method that checks is a type element is a clone generated class.
	 *
	 * @param element type element to check
	 * @return {@code true} if type is a clone generated class.
	 */
	static boolean isCloneGenerated(TypeElement element) {

		/* Extract the current type delegator type and check if is non null */
		TypeElement type = WsIOUtils.extractDelegatorType(element);
		if (Objects.nonNull(type)) {

			/* Get root type element */
			TypeElement root = WsIOUtils.extractTypes(type)
					.headOption()
					.getOrElse(type);

			/* Find the clone recursively */
			Map<Tuple2<String, String>, Set<Tuple2<TypeElement, String>>> searchedType = findCloneRecursively(HashSet.of(root))
					.filterValues(set -> set.map(Tuple2::_1).contains(type));

			/* Iterate for each unique identifier { prefix - suffix } found */
			for (Tuple2<Tuple2<String, String>, Set<Tuple2<TypeElement, String>>> searched : searchedType) {

				/* Get the identifier and the set of values */
				Tuple2<String, String> identifier = searched._1();
				Set<Tuple2<TypeElement, String>> elements = searched._2();

				/* Get the prefix and suffix names */
				String prefix = identifier._1();
				String suffix = identifier._2();

				/* Find the current element and check if is defined */
				Option<Tuple2<TypeElement, String>> infoOpt = elements.find(tuple -> type.equals(tuple._1()));
				if (infoOpt.isDefined()) {

					/* Get the tuple with element and package info */
					Tuple2<TypeElement, String> info = infoOpt.get();

					/* Get the searched class and package names */
					String searchedName = type.getSimpleName().toString();
					String searchedPackage = info._2();

					/* Get the current class and package names */
					String elementName = element.getSimpleName().toString();
					String elementPackage = WsIOUtils.extractPackage(element)
							.getQualifiedName()
							.toString();

					/* Get searched inner name of the class */
					String searchedInnerName = WsIOUtils.extractTypes(type)
							.map(TypeElement::getSimpleName)
							.map(Name::toString)
							.append(searchedName)
							.mkString();

					/* Create the clone final name with prefix and suffix */
					String finalName = WsIOUtil.addWrap(searchedInnerName, prefix, suffix);

					/* Check if the searched and current package names are the same,
					 * and the clone class name is equal to current name to return true */
					if (StringUtils.equals(searchedPackage, elementPackage)
							&& StringUtils.equals(elementName, finalName)) {
						return true;
					}

				}

			}

		}

		/* Return false when delegator type could not be found */
		return false;

	}

	/**
	 * Method that checks is a type element is a clone message generated class.
	 *
	 * @param element type element to check
	 * @return {@code true} if type is a clone message generated class.
	 */
	static boolean isCloneMessageGenerated(TypeElement element) {

		/* Extract the current type delegator type and check if is non null */
		TypeElement type = WsIOUtils.extractDelegatorType(element);
		if (Objects.nonNull(type)) {

			/* Get root type element */
			TypeElement root = WsIOUtils.extractTypes(type)
					.headOption()
					.getOrElse(type);

			/* Find the message recursively */
			Map<TypeElement, String> searchedMessageType = findMessageRecursively(HashSet.of(root))
					.filterKeys(type::equals);

			/* Find the clone recursively */
			Map<Tuple2<String, String>, Set<Tuple2<TypeElement, String>>> searchedCloneType =
					findCloneRecursively(HashSet.of(root))
							.filterValues(set -> set.map(Tuple2::_1).contains(type));

			/* Find the clone message map */
			Map<Tuple2<String, String>, Set<Tuple2<TypeElement, String>>> searchedType =
					findCloneMessage(searchedMessageType, searchedCloneType)
							.filterValues(set -> set.map(Tuple2::_1).contains(type));

			/* Iterate for each unique identifier { prefix - suffix } found */
			for (Tuple2<Tuple2<String, String>, Set<Tuple2<TypeElement, String>>> searched : searchedType) {

				/* Get the identifier and the set of values */
				Tuple2<String, String> identifier = searched._1();
				Set<Tuple2<TypeElement, String>> elements = searched._2();

				/* Get the prefix and suffix class names */
				String prefixClass = identifier._1();
				String suffixClass = identifier._2();

				/* Get the prefix and suffix request names */
				String prefixRequest = WsIOConstant.REQUEST_PREFIX + prefixClass;
				String suffixRequest = suffixClass + WsIOConstant.REQUEST_SUFFIX;

				/* Get the prefix and suffix response names */
				String prefixResponse = WsIOConstant.RESPONSE_PREFIX + prefixClass;
				String suffixResponse = suffixClass + WsIOConstant.RESPONSE_SUFFIX;

				/* Find the current element and check if is defined */
				Option<Tuple2<TypeElement, String>> infoOpt = elements.find(tuple -> type.equals(tuple._1()));
				if (infoOpt.isDefined()) {

					/* Get the tuple with element and package info */
					Tuple2<TypeElement, String> info = infoOpt.get();

					/* Get the searched class and package names */
					String searchedName = type.getSimpleName().toString();
					String searchedPackage = info._2();

					/* Get the current class and package names */
					String elementName = element.getSimpleName().toString();
					String elementPackage = WsIOUtils.extractPackage(element)
							.getQualifiedName()
							.toString();

					/* Get searched inner name of the class */
					String searchedInnerName = WsIOUtils.extractTypes(type)
							.map(TypeElement::getSimpleName)
							.map(Name::toString)
							.append(searchedName)
							.mkString();

					/* Add the prefixes and suffixes to the names to create request and response full names */
					String finalRequestName = WsIOUtil.addWrap(searchedInnerName, prefixRequest, suffixRequest);
					String finalResponseName = WsIOUtil.addWrap(searchedInnerName, prefixResponse, suffixResponse);

					/* Check if the searched and current package names are the same,
					 * and the current class name is equal to response or request full names to return true */
					if (StringUtils.equals(searchedPackage, elementPackage)
							&& (StringUtils.equals(elementName, finalRequestName)
							|| StringUtils.equals(elementName, finalResponseName))) {
						return true;
					}

				}

			}

		}

		/* Return false when delegator type could not be found */
		return false;

	}

	/**
	 * Method that checks if a type element is valid for clone and/or message.
	 *
	 * @param typeElement type element to check
	 * @return {@code true} if the type element is valid or {@code false} otherwise
	 */
	private static boolean isValid(TypeElement typeElement) {
		return Objects.nonNull(typeElement)
				&& typeElement.getModifiers().contains(Modifier.PUBLIC)
				&& (ElementKind.INTERFACE.equals(typeElement.getKind())
				|| ElementKind.ENUM.equals(typeElement.getKind())
				|| (ElementKind.CLASS.equals(typeElement.getKind())) && hasPublicEmptyConstructor(typeElement));
	}

	/**
	 * Method that checks if a type element has an empty public constructor.
	 *
	 * @param typeElement type element to check
	 * @return {@code true} if the type element has an empty public constructor or {@code false} otherwise
	 */
	private static boolean hasPublicEmptyConstructor(TypeElement typeElement) {
		return List.ofAll(ElementFilter.constructorsIn(typeElement.getEnclosedElements()))
				.filter(contructor -> contructor.getParameters().isEmpty())
				.exists(constructor -> constructor.getModifiers().contains(Modifier.PUBLIC));
	}

}