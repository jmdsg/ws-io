package com.fiberg.wsio.processor;

import com.fiberg.wsio.annotation.*;
import io.vavr.Tuple;
import io.vavr.Tuple2;
import io.vavr.collection.*;
import io.vavr.control.Option;
import io.vavr.control.Try;
import javassist.CtClass;
import javassist.CtMethod;

import javax.jws.WebMethod;
import java.util.Objects;

/**
 * Class used to get the prefix, suffix and package name of the wrapper in a ct class.
 */
public final class WsIOWalker {

	/**
	 * Private empty contructor.
	 */
	private WsIOWalker() {  }

	/**
	 * Method that finds the wrapper info of a set of type elements.
	 *
	 * @param pool pool of classes to search
	 * @param ctClasses list of ct classes
	 * @return map identified by the element type containing wrapper annotations info of all elements
	 */
	public static Map<String, Map<String, Tuple2<String, Map<WsIOType, Tuple2<String, String>>>>>
	findWrapperRecursively(Map<String, CtClass> pool, List<CtClass> ctClasses) {

		/* Return the map for each element and finally fold the results with an empty map */
		return ctClasses.map(ctClass -> WsIOWalker.findWrapperRecursively(pool, ctClass))
				.fold(HashMap.empty(), Map::merge);

	}

	/**
	 * Method that finds the wrapper info of a single type elements.
	 *
	 * @param pool pool of classes to search
	 * @param ctClass type element
	 * @return map identified by the element type containing wrapper annotations info of a single element
	 */
	private static Map<String, Map<String, Tuple2<String, Map<WsIOType, Tuple2<String, String>>>>>
	findWrapperRecursively(Map<String, CtClass> pool, CtClass ctClass) {

		/* Check if the current class is null or not */
		if (Objects.nonNull(ctClass)) {

			/* Stream of executable methods */
			Stream<CtMethod> methods = Stream.of(ctClass.getMethods())
					.filter(method -> !"<init>".equals(method.getName()))
					.filter(method -> Try.success(WebMethod.class)
							.mapTry(method::getAnnotation)
							.isSuccess());

			/* Get current element info for every method */
			Map<String, Tuple2<String, Map<WsIOType, Tuple2<String, String>>>> currentInfo = methods.flatMap(method -> {

				/* Get simple name, long name, current descriptor and annotation option */
				String methodName = method.getName();
				String methodLongName = method.getLongName();
				WsIODescriptor descriptor = WsIODescriptor.of(pool, method);
				Option<WsIOAnnotation> messageWrapperOption = descriptor.getSingle(WsIOMessageWrapper.class)
						.map(WsIOAnnotation::of);

				/* Return the tuple of method name, and another tuple with method info and package name */
				return messageWrapperOption.map(wrapper -> {

					/* Method, class and package names */
					String className = ctClass.getSimpleName();
					String packageName = ctClass.getPackageName();

					/* Obtain the package name */
					String finalPackage = WsIOEngine.obtainPackage(methodName, className, packageName,
							wrapper.getPackageName(), wrapper.getPackagePath(), wrapper.getPackagePrefix(),
							wrapper.getPackageSuffix(), wrapper.getPackageStart(), wrapper.getPackageMiddle(),
							wrapper.getPackageEnd(), wrapper.getPackageJs());

					/* Map with the prefix and suffix for response and request wrappers */
					Map<WsIOType, Tuple2<String, String>> messageMap = HashMap.of(
							WsIOType.RESPONSE, Tuple.of(WsIOConstant.RESPONSE_WRAPPER_PREFIX,
									WsIOConstant.RESPONSE_WRAPPER_SUFFIX),
							WsIOType.REQUEST, Tuple.of(WsIOConstant.REQUEST_WRAPPER_PREFIX,
									WsIOConstant.REQUEST_WRAPPER_SUFFIX)
					);

					/* Return the package name and the prefixes and suffixes of response and requests */
					return Tuple.of(methodLongName, Tuple.of(finalPackage, messageMap));

				});

			}).toMap(tuple -> tuple);

			/* Create zero map with current element */
			Map<String, Map<String, Tuple2<String, Map<WsIOType, Tuple2<String, String>>>>> zeroMap =
					HashMap.of(ctClass.getName(), currentInfo);

			/* Call recursively this function with each declared class
			 * and finally fold the results with zero map */
			return Try.of(ctClass::getDeclaredClasses)
					.toStream().flatMap(Stream::of)
					.map(ct -> WsIOWalker.findWrapperRecursively(pool, ct))
					.fold(zeroMap, Map::merge);

		} else {

			/* Return empty hashmap when element is null */
			return HashMap.empty();

		}

	}

}
