package com.fiberg.wsio.processor;

import com.fiberg.wsio.annotation.*;
import io.vavr.Tuple;
import io.vavr.Tuple2;
import io.vavr.collection.*;
import io.vavr.control.Try;
import javassist.CtClass;
import javassist.CtMethod;
import org.apache.commons.lang3.ObjectUtils;

import javax.jws.WebMethod;
import javax.jws.WebService;
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
	 * Method that extracts the info of wrappers in the ct class list.
	 *
	 * @param ctClasses ct class list containing tuples with package ct and class ct
	 * @return map containing the info of wrappers in ct classes
	 */
	public static Map<String, Map<String, Tuple2<String, Map<WsIOType, Tuple2<String, String>>>>>
	findWrapperRecursively(List<Tuple2<CtClass, CtClass>> ctClasses) {

		/* Return the recursive call of the method, starting with package annotation */
		return ctClasses.map(tuple -> findWrapperRecursively(tuple._2(),
				WsIOAnnotation.ofNull(WsIOUtils.extractAnnotation(tuple._1(), WsIOMessageWrapper.class)),
				WsIOUtils.extractAnnotation(tuple._1(), WsIOAnnotate.class)))
				.fold(HashMap.empty(), Map::merge);

	}

	/**
	 * Method that extracts the info of wrappers in the ct class list.
	 *
	 * @param ctClass ct class to recursively check
	 * @param wrapper annotation of the class or package
	 * @return map containing the info of wrappers in ct classes
	 */
	private static Map<String, Map<String, Tuple2<String, Map<WsIOType, Tuple2<String, String>>>>>
	findWrapperRecursively(CtClass ctClass, WsIOAnnotation wrapper, WsIOAnnotate annotate) {

		/* Get skip message wrapper annotation and skipped flag */
		WsIOSkipMessageWrapper skipWrapper = WsIOUtils.extractAnnotation(ctClass, WsIOSkipMessageWrapper.class);
		boolean skippedWrapper = Objects.nonNull(skipWrapper);

		/* Get skip annotate annotation and skipped flag */
		WsIOSkipAnnotate skipAnnotate = WsIOUtils.extractAnnotation(ctClass, WsIOSkipAnnotate.class);
		boolean skippedAnnotate = Objects.nonNull(skipAnnotate);

		/* Check the skip and skip type */
		if ((skippedWrapper && SkipType.ALL.equals(skipWrapper.skip()))
				|| (skippedAnnotate && SkipType.ALL.equals(skipAnnotate.skip()))) {

			/* All skipped return empty map */
			return HashMap.empty();

		} else {

			/* Current annotation wrapper */
			WsIOAnnotation annotationWrapper = WsIOAnnotation.ofNull(WsIOUtils.extractAnnotation(ctClass, WsIOMessageWrapper.class));
			WsIOAnnotation actualWrapper = ObjectUtils.firstNonNull(annotationWrapper, wrapper);

			/* Current annotation wrapper */
			WsIOAnnotate annotationAnnotate = WsIOUtils.extractAnnotation(ctClass, WsIOAnnotate.class);
			WsIOAnnotate actualAnnotate = ObjectUtils.firstNonNull(annotationAnnotate, annotate);

			/* Service and enabled flags */
			boolean service = Objects.nonNull(WsIOUtils.extractAnnotation(ctClass, WebService.class));
			boolean enabledWrapper = Objects.nonNull(actualWrapper);
			boolean enabledAnnotate = Objects.nonNull(actualAnnotate);

			/* Map of wrappers and check condition for use as wrapper */
			Map<String, Map<String, Tuple2<String, Map<WsIOType, Tuple2<String, String>>>>> wrappers = HashMap.empty();
			if (service
					&& !(skippedWrapper && SkipType.PARENT.equals(skipWrapper.skip()))
					&& !(skippedAnnotate && SkipType.PARENT.equals(skipAnnotate.skip()))) {

				/* Get list of executables, with name different to <init>,
				 * message wrapper and annotate not skipped,
				 * and wrapper or annotate defined or enabled */
				List<CtMethod> executables = Try.success(ctClass)
						.mapTry(CtClass::getDeclaredMethods)
						.toList()
						.flatMap(Stream::of)
						.filter(executable -> !"<init>".equals(executable.getName()))
						.filter(executable -> Objects.isNull(WsIOUtils.extractAnnotation(executable,
								WsIOSkipMessageWrapper.class)))
						.filter(executable -> Objects.isNull(WsIOUtils.extractAnnotation(executable,
								WsIOSkipAnnotate.class)))
						.filter(executable -> enabledWrapper || Objects.nonNull(WsIOUtils.extractAnnotation(executable,
								WsIOMessageWrapper.class)))
						.filter(executable -> enabledAnnotate || Objects.nonNull(WsIOUtils.extractAnnotation(executable,
								WsIOAnnotate.class)))
						.filter(executable -> Objects.nonNull(WsIOUtils.extractAnnotation(executable,
								WebMethod.class)));

				/* Process each executable and add it to the map identified with ct class name */
				wrappers = wrappers.put(ctClass.getName(), executables.toMap(CtMethod::getLongName, executable -> {

					/* Current annotations */
					WsIOAnnotation currentAnnotation = WsIOAnnotation
							.ofNull(WsIOUtils.extractAnnotation(executable, WsIOMessageWrapper.class));
					WsIOAnnotation annot = ObjectUtils.firstNonNull(currentAnnotation, actualWrapper);

					/* Method, class and package names */
					String methodName = executable.getName();
					String className = ctClass.getName();
					String packageName = ctClass.getPackageName();

					/* Obtain the package name */
					String finalPackage = WsIOEngine.obtainPackage(methodName, className, packageName,
							annot.getPackageName(), annot.getPackagePath(), annot.getPackagePrefix(),
							annot.getPackageSuffix(), annot.getPackageStart(), annot.getPackageMiddle(),
							annot.getPackageEnd(), annot.getPackageJs());

					/* Map with the prefix and suffix for response and request wrappers */
					Map<WsIOType, Tuple2<String, String>> messageMap = HashMap.of(
							WsIOType.RESPONSE, Tuple.of(WsIOConstant.RESPONSE_WRAPPER_PREFIX,
									WsIOConstant.RESPONSE_WRAPPER_SUFFIX),
							WsIOType.REQUEST, Tuple.of(WsIOConstant.REQUEST_WRAPPER_PREFIX,
									WsIOConstant.REQUEST_WRAPPER_SUFFIX)
					);

					/* Return the package name and the prefixes and suffixes of response and requests */
					return Tuple.of(finalPackage, messageMap);

				}));

			}

			/* Return the recursive call for each declared class of the class */
			return Try.success(ctClass).mapTry(CtClass::getDeclaredClasses)
					.toList()
					.flatMap(Stream::of)
					.filter(type -> !(skippedWrapper && SkipType.CHILDS.equals(skipWrapper.skip())))
					.filter(type -> !(skippedAnnotate && SkipType.CHILDS.equals(skipAnnotate.skip())))
					.map(next -> findWrapperRecursively(next, actualWrapper, actualAnnotate))
					.fold(wrappers, Map::merge);

		}

	}

}
