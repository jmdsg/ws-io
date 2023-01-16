package com.fiberg.wsio.processor;

import com.fiberg.wsio.annotation.WsIOClone;
import com.fiberg.wsio.annotation.WsIOMessage;
import com.fiberg.wsio.annotation.WsIOMessageWrapper;
import com.fiberg.wsio.util.WsIOUtil;
import io.vavr.Function2;
import io.vavr.Tuple;
import io.vavr.Tuple2;
import io.vavr.collection.*;
import io.vavr.control.Option;
import io.vavr.control.Try;
import jakarta.jws.WebMethod;
import net.bytebuddy.description.method.MethodDescription;
import net.bytebuddy.description.type.TypeDescription;
import org.apache.commons.lang3.StringUtils;

import java.util.Objects;
import java.util.function.Function;

/**
 * Class used to get the prefix, suffix and package name of the wrapper in a ct class.
 */
public final class WsIOWalker {

	/**
	 * Private empty constructor.
	 */
	private WsIOWalker() {  }

	/**
	 * Method that finds the wrapper info of a set of type elements.
	 *
	 * @param types list of ct classes
	 * @return map identified by the element type containing wrapper annotations info of all elements
	 */
	public static Map<String, Map<String, Tuple2<String, Map<WsIOType, Tuple2<String, String>>>>>
	findWrapperRecursively(final List<TypeDescription> types) {

		/* Return the map for each element and finally fold the results with an empty map */
		return types.map(WsIOWalker::findWrapperRecursively)
				.fold(HashMap.empty(), Map::merge);

	}

	/**
	 * Method that finds the wrapper info of a single type elements.
	 *
	 * @param type type element
	 * @return map identified by the element type containing wrapper annotations info of a single element
	 */
	private static Map<String, Map<String, Tuple2<String, Map<WsIOType, Tuple2<String, String>>>>>
	findWrapperRecursively(final TypeDescription type) {

		/* Check if the current class is null or not */
		if (Objects.nonNull(type)) {

			/* Stream of executable methods */
			final Stream<? extends MethodDescription> methods = Stream.of(type)
					.flatMap(TypeDescription::getDeclaredMethods)
					.filter(descriptor -> !descriptor.isConstructor())
					.filter(descriptor -> descriptor.getDeclaredAnnotations()
							.isAnnotationPresent(WebMethod.class));

			/* Get current element info for every method */
			final Map<String, Tuple2<String, Map<WsIOType, Tuple2<String, String>>>> currentInfo = methods.flatMap(method -> {

				/* Get simple name, long name, current descriptor and annotation option */
				final String methodName = method.getName();
				final String methodLongName = method.toString();
				final WsIODescriptor descriptor = WsIODescriptor.of(method);
				final Option<WsIOAnnotation> messageWrapperOption = descriptor.getSingle(WsIOMessageWrapper.class)
						.map(WsIOAnnotation::of);

				/* Return the tuple of method name, and another tuple with method info and package name */
				return messageWrapperOption.map(wrapper -> {

					/* Method, class and package names */
					final String className = type.getSimpleName();
					final String packageName = type.getPackage()
							.getName();

					/* Obtain the package name */
					final String finalPackage = WsIOEngine.obtainPackage(methodName, className, packageName,
							wrapper.getPackageName(), wrapper.getPackagePath(), wrapper.getPackagePrefix(),
							wrapper.getPackageSuffix(), wrapper.getPackageStart(), wrapper.getPackageMiddle(),
							wrapper.getPackageEnd(), wrapper.getPackageFunc());

					/* Map with the prefix and suffix for response and request wrappers */
					final Map<WsIOType, Tuple2<String, String>> messageMap = HashMap.of(
							WsIOType.RESPONSE, Tuple.of(
									WsIOConstant.RESPONSE_WRAPPER_PREFIX,
									WsIOConstant.RESPONSE_WRAPPER_SUFFIX
							),
							WsIOType.REQUEST, Tuple.of(
									WsIOConstant.REQUEST_WRAPPER_PREFIX,
									WsIOConstant.REQUEST_WRAPPER_SUFFIX
							)
					);

					/* Return the package name and the prefixes and suffixes of response and requests */
					return Tuple.of(methodLongName, Tuple.of(finalPackage, messageMap));

				});

			}).toMap(tuple -> tuple);

			/* Create zero map with current element */
			final Map<String, Map<String, Tuple2<String, Map<WsIOType, Tuple2<String, String>>>>> zeroMap =
					HashMap.of(type.getName(), currentInfo);

			/* Call recursively this function with each declared class
			 * and finally fold the results with zero map */
			return Try.of(type::getDeclaredTypes)
					.toStream()
					.flatMap(Function.identity())
					.map(WsIOWalker::findWrapperRecursively)
					.fold(zeroMap, Map::merge);

		} else {

			/* Return empty hash map when element is null */
			return HashMap.empty();

		}

	}

	/**
	 * Method that gets all the related names of a class.
	 *
	 * @param type class to extract the names
	 * @return the message, clone and clone message names
	 */
	public static Map<WsIOGenerate, Set<String>> getGeneratedNames(final Class<?> type) {

		/* Get the descriptor of the class */
		final WsIODescriptor descriptor = WsIODescriptor.of(type);

		/* Get the message annotation option */
		final Option<WsIOAnnotation> messageOpt = descriptor.getSingle(WsIOMessage.class)
				.map(WsIOAnnotation::of);

		/* Get the clone map */
		final Map<Tuple2<String, String>, WsIOAnnotation> cloneMap = descriptor.getMultiple(WsIOClone.class)
				.map((key, clone) -> Tuple.of(Tuple.of(clone.prefix(), clone.suffix()), WsIOAnnotation.of(clone)));

		/* Class and package names */
		final String currentClass = type.getSimpleName();
		final String currentPackage = type.getPackage().getName();

		/* Full class name */
		final String currentFullClass = Stream.<Class<?>>iterate(type, Class::getDeclaringClass)
				.takeWhile(Objects::nonNull)
				.reverse()
				.map(Class::getSimpleName)
				.mkString();

		/* Function to obtain the package name */
		final Function2<String, WsIOAnnotation, String> getPackageName = (packageName, annotation) ->
				WsIOEngine.obtainPackage(StringUtils.EMPTY, currentClass, currentPackage,
				annotation.getPackageName(), annotation.getPackagePath(), annotation.getPackagePrefix(),
				annotation.getPackageSuffix(), annotation.getPackageStart(), annotation.getPackageMiddle(),
				annotation.getPackageEnd(), annotation.getPackageFunc());

		/* Get the message names with the package generated and the response and request identifiers */
		final Set<String> messageNames = messageOpt.map(getPackageName.apply(currentPackage)).toSet()
				.flatMap(packageName -> Stream.of(
						WsIOUtil.addPrefixName(WsIOUtil.addWrap(currentFullClass,
								WsIOConstant.RESPONSE_PREFIX, WsIOConstant.RESPONSE_SUFFIX), packageName),
						WsIOUtil.addPrefixName(WsIOUtil.addWrap(currentFullClass,
								WsIOConstant.REQUEST_PREFIX, WsIOConstant.REQUEST_SUFFIX), packageName)));

		/* Get the clone names with the package generated and the clone identifiers */
		final Set<String> cloneNames = cloneMap.mapValues(getPackageName.apply(currentPackage))
				.map(tuple -> {

					/* Get identifier and package name */
					final Tuple2<String, String> identifier = tuple._1();
					final String packageName = tuple._2();

					/* Get clone prefix and suffix */
					final String prefix = identifier._1();
					final String suffix = identifier._2();

					/* Return the class name */
					return WsIOUtil.addPrefixName(WsIOUtil.addWrap(currentFullClass,
							prefix, suffix), packageName);

				}).toSet();

		/* Get the clone message names with the package generated and the clone, response and request identifiers */
		final Set<String> cloneMessageNames = cloneMap.mapValues(getPackageName.apply(currentPackage))
				.flatMap(tuple -> messageOpt.map(annotation -> Tuple.of(tuple._1(), tuple._2(), annotation)))
				.flatMap(tuple -> {

					/* Get identifier, package name and message annotation */
					final Tuple2<String, String> identifier = tuple._1();
					final String packageName = tuple._2();
					final WsIOAnnotation annotation = tuple._3();

					/* Get clone prefix and suffix */
					final String prefixClass = identifier._1();
					final String suffixClass = identifier._2();

					/* Get the prefix and suffix response names */
					final String prefixResponse = WsIOConstant.RESPONSE_PREFIX + prefixClass;
					final String suffixResponse = suffixClass + WsIOConstant.RESPONSE_SUFFIX;

					/* Get the prefix and suffix request names */
					final String prefixRequest = WsIOConstant.REQUEST_PREFIX + prefixClass;
					final String suffixRequest = suffixClass + WsIOConstant.REQUEST_SUFFIX;

					/* Create the final package name */
					final String finalPackage = getPackageName.apply(packageName, annotation);

					/* Return the names with the package generated and the clone, response and request identifiers */
					return Stream.of(
							WsIOUtil.addPrefixName(WsIOUtil.addWrap(currentFullClass,
									prefixResponse, suffixResponse), finalPackage),
							WsIOUtil.addPrefixName(WsIOUtil.addWrap(currentFullClass,
									prefixRequest, suffixRequest), finalPackage));

				}).toSet();

		/* Return the map with message, clone and clone message names */
		return HashMap.of(
				WsIOGenerate.MESSAGE, messageNames,
				WsIOGenerate.CLONE, cloneNames,
				WsIOGenerate.CLONE_MESSAGE, cloneMessageNames);

	}

}
