package com.fiberg.wsio.processor;

import com.fiberg.wsio.annotation.*;
import com.fiberg.wsio.util.WsIOUtil;
import io.vavr.*;
import io.vavr.collection.*;
import io.vavr.control.Option;
import javassist.CtClass;
import org.apache.commons.lang3.ObjectUtils;
import org.apache.commons.lang3.StringUtils;

import javax.jws.WebMethod;
import javax.jws.WebService;
import javax.lang.model.element.ExecutableElement;
import javax.lang.model.element.Name;
import javax.lang.model.element.PackageElement;
import javax.lang.model.element.TypeElement;
import java.util.Objects;

class WsIOFinder {

	private WsIOFinder() {  }

	static Map<TypeElement, Map<String, Tuple2<WsIOInfo, String>>> findWrapperRecursively(Set<TypeElement> elements) {

		return elements.map(element -> findWrapperRecursively(element, WsIOAnnotation.ofNull(element.getEnclosingElement()
				.getAnnotation(WsIOMessageWrapper.class)), WsIOAdditional.of(element)))
				.fold(HashMap.empty(), Map::merge);

	}

	private static Map<TypeElement, Map<String, Tuple2<WsIOInfo, String>>> findWrapperRecursively(TypeElement element,
	                                                                                              WsIOAnnotation annotation,
	                                                                                              WsIOAdditional additional) {

		WsIOSkipMessageWrapper skip = element.getAnnotation(WsIOSkipMessageWrapper.class);
		boolean skipped = Objects.nonNull(skip);

		if (skipped && SkipType.ALL.equals(skip.skip())) {

			return HashMap.empty();

		} else {

			WsIOAnnotation annotationWrapper = WsIOAnnotation.ofNull(element.getAnnotation(WsIOMessageWrapper.class));
			WsIOAnnotation actualWrapper = ObjectUtils.firstNonNull(annotationWrapper, annotation);

			boolean service = Objects.nonNull(element.getAnnotation(WebService.class));
			boolean enabled = Objects.nonNull(actualWrapper);

			Map<TypeElement, Map<String, Tuple2<WsIOInfo, String>>> wrappers = HashMap.empty();
			if (!(skipped && SkipType.PARENT.equals(skip.skip())) && service) {

				Set<ExecutableElement> executables = Stream.ofAll(element.getEnclosedElements())
						.filter(ExecutableElement.class::isInstance)
						.map(ExecutableElement.class::cast)
						.filter(executable -> !"<init>".equals(executable.getSimpleName().toString()))
						.filter(executable -> Objects.isNull(executable.getAnnotation(WsIOSkipMessageWrapper.class)))
						.filter(executable -> enabled || Objects.nonNull(executable.getAnnotation(WsIOMessageWrapper.class)))
						.filter(executable -> Objects.nonNull(executable.getAnnotation(WebMethod.class)))
						.toSet();

				wrappers = wrappers.put(element,
						executables.toMap(Function1.of(ExecutableElement::getSimpleName)
								.andThen(Name::toString), executable -> {

							WsIOAnnotation currentAnnotation = WsIOAnnotation
									.ofNull(executable.getAnnotation(WsIOMessageWrapper.class));
							WsIOAnnotation annot = ObjectUtils.firstNonNull(currentAnnotation, actualWrapper);

							String methodName = executable.getSimpleName().toString();
							String className = element.getSimpleName().toString();
							String packageName = WsIOUtils.extractPackage(element).getQualifiedName().toString();

							String finalPackage = WsIOEngine.obtainPackage(methodName, className, packageName,
									annot.getPackageName(), annot.getPackagePath(), annot.getPackagePrefix(),
									annot.getPackageSuffix(), annot.getPackageStart(), annot.getPackageMiddle(),
									annot.getPackageEnd(), annot.getPackageJs());

							WsIOAdditional additionalExecutable = additional.update(executable);

							WsIOInfo info = WsIOUtils.extractInfo(executable, additionalExecutable);

							return Tuple.of(info, finalPackage);

						}));

			}

			return Stream.ofAll(element.getEnclosedElements())
					.filter(TypeElement.class::isInstance)
					.map(TypeElement.class::cast)
					.filter(type -> !(skipped && SkipType.CHILDS.equals(skip.skip())))
					.map(next -> findWrapperRecursively(next, actualWrapper, additional.update(next)))
					.fold(wrappers, Map::merge);

		}

	}

	static Map<Tuple2<String, String>, Set<Tuple2<TypeElement, String>>> findCloneMessage(Map<TypeElement, String> messages,
	                                                                                      Map<Tuple2<String, String>, Set<Tuple2<TypeElement, String>>> clones) {

		Function4<String, String, TypeElement, String, Tuple2<TypeElement, String>> transformPackage =
				(prefix, suffix, element, currentPackage) -> {

					WsIOClone clone = getPriorityClone(element, prefix, suffix);
					if (Objects.nonNull(clone)) {

						String packageName = WsIOEngine.obtainPackage(StringUtils.EMPTY, element.getSimpleName().toString(), currentPackage,
								clone.packageName(), clone.packagePath(), clone.packagePrefix(),
								clone.packageSuffix(), clone.packageStart(), clone.packageMiddle(),
								clone.packageEnd(), clone.packageJs());

						return Tuple.of(element, packageName);

					} else {
						return null;
					}

				};

		return clones.mapValues(set -> set.filter(tuple -> messages.keySet().contains(tuple._1())))
				.filter((key, value) -> value.nonEmpty())
				.map((identifier, set) ->
						Tuple.of(identifier, set.map(tuple -> tuple.map(transformPackage.apply(identifier._1(), identifier._2())))
								.filter(Objects::nonNull)));

	}

	private static WsIOClone getPriorityClone(TypeElement element, String prefix, String suffix) {
		WsIOClone[] clones = getPriorityClones(element);
		if (Objects.nonNull(clones)) {
			for (WsIOClone current : clones) {
				if (current.prefix().equals(prefix)
						&& current.suffix().equals(suffix)) {
					return current;
				}
			}
		}
		return null;
	}

	private static WsIOClone[] getPriorityClones(TypeElement element) {
		WsIOClone[] clones = element.getAnnotationsByType(WsIOClone.class);
		if (Objects.nonNull(clones) && clones.length > 0) {
			return clones;
		} else if (element.getEnclosingElement() instanceof PackageElement) {
			return element.getEnclosingElement().getAnnotationsByType(WsIOClone.class);
		} else if (element.getEnclosingElement() instanceof TypeElement) {
			return getPriorityClones((TypeElement) element.getEnclosingElement());
		}
		return null;
	}

	static Map<Tuple2<String, String>, Set<Tuple2<TypeElement, String>>> findCloneRecursively(Set<TypeElement> elements) {

		return elements.map(element -> findCloneRecursively(element,
				HashSet.of(element.getEnclosingElement().getAnnotationsByType(WsIOClone.class))
						.toMap(clone -> Tuple.of(clone.prefix(), clone.suffix()), e -> e),
				HashSet.empty()))
				.fold(HashMap.empty(), (map1, map2) -> map1.merge(map2, Set::addAll));

	}

	private static Map<Tuple2<String, String>, Set<Tuple2<TypeElement, String>>> findCloneRecursively(TypeElement element,
	                                                                                                  Map<Tuple2<String, String>, WsIOClone> clones,
	                                                                                                  Set<Tuple2<String, String>> skips) {

		Map<Tuple2<String, String>, WsIOClone> ioClones = HashSet.of(element.getAnnotationsByType(WsIOClone.class))
				.toMap(clone -> Tuple.of(clone.prefix(), clone.suffix()), e -> e);

		Map<Tuple2<String, String>, WsIOClone> nextClones = ioClones.merge(clones);

		Set<Tuple3<String, String, SkipType>> ioSkips = HashSet.of(element.getAnnotationsByType(WsIOSkipClone.class))
				.map(clone -> Tuple.of(clone.prefix(), clone.suffix(), clone.skip()));

		Set<Tuple2<String, String>> parentSkips = ioSkips.filter(tuple -> SkipType.PARENT.equals(tuple._3()))
				.map(tuple -> Tuple.of(tuple._1(), tuple._2()));

		Set<Tuple2<String, String>> childSkips = ioSkips.filter(tuple -> SkipType.CHILDS.equals(tuple._3()))
				.map(tuple -> Tuple.of(tuple._1(), tuple._2()));

		Set<Tuple2<String, String>> allSkips = ioSkips.filter(tuple -> SkipType.ALL.equals(tuple._3()))
				.map(tuple -> Tuple.of(tuple._1(), tuple._2()));

		Set<Tuple2<String, String>> currentSkips = skips.addAll(allSkips);

		Function3<String, String, WsIOClone, Tuple2<TypeElement, String>> transformToTuple = (prefix, suffix, clone) -> {

			String packageName = WsIOEngine.obtainPackage(StringUtils.EMPTY, element.getSimpleName().toString(),
					WsIOUtils.extractPackage(element).getQualifiedName().toString(),
					clone.packageName(), clone.packagePath(), clone.packagePrefix(),
					clone.packageSuffix(), clone.packageStart(), clone.packageMiddle(),
					clone.packageEnd(), clone.packageJs());

			return Tuple.of(element, packageName);

		};

		Map<Tuple2<String, String>, Set<Tuple2<TypeElement, String>>> groups = nextClones
				.filterKeys(Predicates.noneOf(parentSkips::contains, currentSkips::contains))
				.toMap(Tuple2::_1, tuple -> HashSet.of(transformToTuple.apply(
						tuple._1()._1(), tuple._1()._2(), tuple._2())));

		Set<Tuple2<String, String>> nextSkips = currentSkips.addAll(childSkips);

		return Stream.ofAll(element.getEnclosedElements())
				.filter(TypeElement.class::isInstance)
				.map(TypeElement.class::cast)
				.map(next -> findCloneRecursively(next, nextClones, nextSkips))
				.fold(groups, (map1, map2) -> map1.merge(map2, Set::addAll));

	}

	static Map<TypeElement, String> findMessageRecursively(Set<TypeElement> elements) {

		return elements.map(element -> findMessageRecursively(element,
				WsIOAnnotation.ofNull(element.getEnclosingElement().getAnnotation(WsIOMessage.class))))
				.fold(HashMap.empty(), Map::merge);

	}

	private static Map<TypeElement, String> findMessageRecursively(TypeElement element, WsIOAnnotation annotation) {

		WsIOSkipMessage skip = element.getAnnotation(WsIOSkipMessage.class);
		boolean skipped = Objects.nonNull(skip);

		if (skipped && SkipType.ALL.equals(skip.skip())) {

			return HashMap.empty();

		} else {

			WsIOAnnotation annotationMessage = WsIOAnnotation.ofNull(element.getAnnotation(WsIOMessage.class));
			WsIOAnnotation actualMessage = ObjectUtils.firstNonNull(annotationMessage, annotation);
			boolean enabled = Objects.nonNull(actualMessage);

			Map<TypeElement, String> messages = HashMap.empty();
			if (!(skipped && SkipType.PARENT.equals(skip.skip())) && enabled) {

				Function2<TypeElement, WsIOAnnotation, String> extractor = (elem, annot) -> {

					String methodName = StringUtils.EMPTY;
					String className = elem.getSimpleName().toString();
					String packageName = WsIOUtils.extractPackage(elem).getQualifiedName().toString();

					return WsIOEngine.obtainPackage(methodName, className, packageName,
							annot.getPackageName(), annot.getPackagePath(), annot.getPackagePrefix(),
							annot.getPackageSuffix(), annot.getPackageStart(), annot.getPackageMiddle(),
							annot.getPackageEnd(), annot.getPackageJs());

				};

				messages = messages.put(element, extractor.apply(element, actualMessage));

			}

			return Stream.ofAll(element.getEnclosedElements())
					.filter(TypeElement.class::isInstance)
					.map(TypeElement.class::cast)
					.filter(type -> !(skipped && SkipType.CHILDS.equals(skip.skip())))
					.map(next -> findMessageRecursively(next, actualMessage))
					.fold(messages, Map::merge);

		}

	}

	static boolean isMessageGenerated(TypeElement element) {

		TypeElement type = WsIOUtils.extractDelegatorType(element);

		if (Objects.nonNull(type)) {

			TypeElement root = WsIOUtils.extractTypes(type)
					.headOption()
					.getOrElse(type);

			Map<TypeElement, String> searchedType = findMessageRecursively(HashSet.of(root))
					.filterKeys(type::equals);

			if (searchedType.nonEmpty()) {

				String searchedName = type.getSimpleName().toString();
				String searchedPackage = searchedType.get(type)
						.getOrNull();

				String elementName = element.getSimpleName().toString();
				String elementPackage = WsIOUtils.extractPackage(element)
						.getQualifiedName()
						.toString();

				String searchedInnerName = WsIOUtils.extractTypes(type)
						.map(TypeElement::getSimpleName)
						.map(Name::toString)
						.append(searchedName)
						.mkString();

				String requestName = WsIOUtil.addWrap(searchedInnerName, WsIOConstant.REQUEST_PREFIX,
						WsIOConstant.REQUEST_SUFFIX);
				String responseName = WsIOUtil.addWrap(searchedInnerName, WsIOConstant.RESPONSE_PREFIX,
						WsIOConstant.RESPONSE_SUFFIX);

				return StringUtils.equals(searchedPackage, elementPackage)
						&& (StringUtils.equals(requestName, elementName)
								|| StringUtils.equals(responseName, elementName));

			}

		}

		return false;
	}

	static boolean isCloneGenerated(TypeElement element) {

		TypeElement type = WsIOUtils.extractDelegatorType(element);

		if (Objects.nonNull(type)) {

			TypeElement root = WsIOUtils.extractTypes(type)
					.headOption()
					.getOrElse(type);

			Map<Tuple2<String, String>, Set<Tuple2<TypeElement, String>>> searchedType = findCloneRecursively(HashSet.of(root))
					.filterValues(set -> set.map(Tuple2::_1).contains(type));

			for (Tuple2<Tuple2<String, String>, Set<Tuple2<TypeElement, String>>> searched : searchedType) {

				Tuple2<String, String> identifier = searched._1();
				Set<Tuple2<TypeElement, String>> elements = searched._2();

				String prefix = identifier._1();
				String suffix = identifier._2();

				Option<Tuple2<TypeElement, String>> infoOpt = elements.find(tuple -> type.equals(tuple._1()));

				if (infoOpt.isDefined()) {

					Tuple2<TypeElement, String> info = infoOpt.get();

					String searchedName = type.getSimpleName().toString();
					String searchedPackage = info._2();

					String elementName = element.getSimpleName().toString();
					String elementPackage = WsIOUtils.extractPackage(element)
							.getQualifiedName()
							.toString();

					String searchedInnerName = WsIOUtils.extractTypes(type)
							.map(TypeElement::getSimpleName)
							.map(Name::toString)
							.append(searchedName)
							.mkString();

					String finalName = WsIOUtil.addWrap(searchedInnerName, prefix, suffix);

					if (StringUtils.equals(searchedPackage, elementPackage)
							&& StringUtils.equals(elementName, finalName)) {
						return true;
					}

				}

			}

		}

		return false;

	}

	static boolean isCloneMessageGenerated(TypeElement element) {

		TypeElement type = WsIOUtils.extractDelegatorType(element);

		if (Objects.nonNull(type)) {

			TypeElement root = WsIOUtils.extractTypes(type)
					.headOption()
					.getOrElse(type);

			Map<TypeElement, String> searchedMessageType = findMessageRecursively(HashSet.of(root))
					.filterKeys(type::equals);

			Map<Tuple2<String, String>, Set<Tuple2<TypeElement, String>>> searchedCloneType =
					findCloneRecursively(HashSet.of(root))
							.filterValues(set -> set.map(Tuple2::_1).contains(type));

			Map<Tuple2<String, String>, Set<Tuple2<TypeElement, String>>> searchedType =
					findCloneMessage(searchedMessageType, searchedCloneType)
							.filterValues(set -> set.map(Tuple2::_1).contains(type));

			for (Tuple2<Tuple2<String, String>, Set<Tuple2<TypeElement, String>>> searched : searchedType) {

				Tuple2<String, String> identifier = searched._1();
				Set<Tuple2<TypeElement, String>> elements = searched._2();

				String prefixClass = identifier._1();
				String suffixClass = identifier._2();

				String prefixRequest = WsIOConstant.REQUEST_PREFIX + prefixClass;
				String suffixRequest = suffixClass + WsIOConstant.REQUEST_SUFFIX;

				String prefixResponse = WsIOConstant.RESPONSE_PREFIX + prefixClass;
				String suffixResponse = suffixClass + WsIOConstant.RESPONSE_SUFFIX;

				Option<Tuple2<TypeElement, String>> infoOpt = elements.find(tuple -> type.equals(tuple._1()));

				if (infoOpt.isDefined()) {

					Tuple2<TypeElement, String> info = infoOpt.get();

					String searchedName = type.getSimpleName().toString();
					String searchedPackage = info._2();

					String elementName = element.getSimpleName().toString();
					String elementPackage = WsIOUtils.extractPackage(element)
							.getQualifiedName()
							.toString();

					String searchedInnerName = WsIOUtils.extractTypes(type)
							.map(TypeElement::getSimpleName)
							.map(Name::toString)
							.append(searchedName)
							.mkString();

					String finalRequestName = WsIOUtil.addWrap(searchedInnerName, prefixRequest, suffixRequest);
					String finalResponseName = WsIOUtil.addWrap(searchedInnerName, prefixResponse, suffixResponse);

					if (StringUtils.equals(searchedPackage, elementPackage)
							&& (StringUtils.equals(elementName, finalRequestName)
									|| StringUtils.equals(elementName, finalResponseName))) {
						return true;
					}

				}

			}

		}

		return false;

	}

}
