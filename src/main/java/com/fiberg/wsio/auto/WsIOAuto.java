package com.fiberg.wsio.auto;

import com.fiberg.wsio.processor.*;
import com.fiberg.wsio.util.WsIOUtil;
import io.github.lukehutch.fastclasspathscanner.FastClasspathScanner;
import io.vavr.*;
import io.vavr.collection.HashMap;
import io.vavr.collection.List;
import io.vavr.collection.Map;
import io.vavr.control.Option;
import io.vavr.control.Try;
import javassist.*;
import javassist.bytecode.AnnotationsAttribute;
import javassist.bytecode.ClassFile;
import javassist.bytecode.ConstPool;
import javassist.bytecode.annotation.Annotation;
import javassist.bytecode.annotation.StringMemberValue;
import org.apache.commons.text.WordUtils;

import javax.xml.ws.RequestWrapper;
import javax.xml.ws.ResponseWrapper;
import java.util.Objects;

/**
 * Class used to annotate web service classes with response and request wrappers.
 */
public final class WsIOAuto {

	/**
	 * Empty private constructor.
	 */
	private WsIOAuto() {  }

	/**
	 * Method used to scan the package for annotate annotations.
	 *
	 * @param basePackage package to scan
	 */
	public static void annotate(String basePackage) {

		/* Obtain all class names of the base package */
		List<String> classNames = List.ofAll(new FastClasspathScanner(basePackage)
				.scan()
				.getNamesOfAllClasses());

		/* Get default class pool */
		ClassPool pool = ClassPool.getDefault();

		/* Get all ct classes when the name starts with base package */
		Map<String, CtClass> ctClasses = classNames.toMap(className -> className,
				className -> Try.success(className)
						.filter(name -> name.startsWith(basePackage))
						.mapTry(pool::getCtClass)
						.getOrNull())
				.filterValues(Objects::nonNull);

		/* Get all package info of every package */
		Map<String, CtClass> packages = ctClasses.values()
				.map(CtClass::getPackageName)
				.toSet()
				.toMap(packageName -> packageName,
						packageName -> Try.success(packageName)
								.map(name -> WsIOUtil.addPrefixName("package-info", name))
								.mapTry(pool::getCtClass)
								.getOrNull())
				.filterValues(Objects::nonNull);

		/* Map with root class name and a tuple with package ct and the ct root class */
		Map<String, Tuple2<CtClass, CtClass>> roots = ctClasses.values()
				.map(WsIOAuto::extractRootCtClass)
				.toMap(CtClass::getName,
						ctClass -> Tuple.of(packages.get(ctClass.getPackageName()).getOrNull(), ctClass));

		/* Get current wrappers of the class */
		Map<String, Map<String, Tuple2<String, Map<WsIOType, Tuple2<String, String>>>>> wrappers =
				WsIOWalker.findWrapperRecursively(roots.values().toList());

		/* Iterate for each wrapper class */
		for (String className : wrappers.keySet()) {

			/* Check ct class is defined */
			Option<CtClass> ctClassOpt = ctClasses.get(className);
			if (ctClassOpt.isDefined()) {

				/* Get main, root class and the wrappers */
				CtClass ctClass = ctClassOpt.get();
				CtClass root = WsIOAuto.extractRootCtClass(ctClass);
				Map<String, Tuple2<String, Map<WsIOType, Tuple2<String, String>>>> wrapper =
						wrappers.getOrElse(root.getName(), HashMap.empty());

				/* Iterate for each method */
				boolean annotationAdded = false;
				for (CtMethod ctMethod : ctClass.getDeclaredMethods()) {

					/* Get method info */
					Option<Tuple2<String, Map<WsIOType, Tuple2<String, String>>>> info = wrapper
							.get(ctMethod.getLongName());

					/* Get package, response and request options */
					Option<String> packageOpt = info.map(Tuple2::_1).filter(Objects::nonNull);
					Option<Tuple2<String, String>> responseOpt = info.map(Tuple2::_2)
							.flatMap(map -> map.get(WsIOType.RESPONSE));
					Option<Tuple2<String, String>> requestOpt = info.map(Tuple2::_2)
							.flatMap(map -> map.get(WsIOType.REQUEST));

					/* Check if all fields are present */
					if (packageOpt.isDefined() && responseOpt.isDefined() && requestOpt.isDefined()) {

						/* Get package name, response and request info with prefixes and suffixes */
						String packageName = packageOpt.get();
						Tuple2<String, String> response = responseOpt.get();
						Tuple2<String, String> request = requestOpt.get();

						/* Get method name and upper name to create wrapper names */
						String methodName = ctMethod.getName();
						String upperName = WordUtils.capitalize(methodName);

						/* Response and request names with prefix and suffix */
						String responseName = WsIOUtil.addWrap(upperName, response._1(), response._2());
						String requestName = WsIOUtil.addWrap(upperName, request._1(), request._2());

						/* Get full qualified response and request class names */
						String responseClass = WsIOUtil.addPrefixName(responseName, packageName);
						String requestClass = WsIOUtil.addPrefixName(requestName, packageName);

						/* Check is the response and request names are valid */
						boolean validResponse = classNames.contains(responseClass);
						boolean validRequest = classNames.contains(requestClass);
						if (validResponse || validRequest) {

							try {

								/* Define class file, class pool and attribute */
								ClassFile ccFile = ctClass.getClassFile();
								ConstPool constpool = ccFile.getConstPool();
								AnnotationsAttribute attribute = new AnnotationsAttribute(constpool,
										AnnotationsAttribute.visibleTag);

								/*  Check if response name is valid to be added */
								if (validResponse) {

									/* Define wrapper class name, create annotation, add the wrapper name
									 * and add the annotation to the attribute */
									String responseAnnotation = ResponseWrapper.class.getCanonicalName();
									Annotation annotation = new Annotation(responseAnnotation, constpool);
									annotation.addMemberValue("className",
											new StringMemberValue(responseClass, ccFile.getConstPool()));
									attribute.addAnnotation(annotation);

								}

								/*  Check if request name is valid to be added */
								if (validRequest) {

									/* Define wrapper class name, create annotation, add the wrapper name
									 * and add the annotation to the attribute */
									String requestAnnotation = RequestWrapper.class.getCanonicalName();
									Annotation annotation = new Annotation(requestAnnotation, constpool);
									annotation.addMemberValue("className",
											new StringMemberValue(requestClass, ccFile.getConstPool()));
									attribute.addAnnotation(annotation);

								}

								/* Add attribute to the method and set flag to true */
								ctMethod.getMethodInfo().addAttribute(attribute);
								annotationAdded = true;

							} catch (Exception e) {
								// ignore class does not exists
							}

						}

					}

				}

				/* Check if the class has been annotated or not */
				if (annotationAdded) {

					try {

						/* Compile the class */
						ctClass.toClass();

					} catch (CannotCompileException e) {

						/* Throw when the class could not be compiles */
						throw new IllegalStateException(String.format("Could not compile the class %s", ctClass), e);

					}

				}

			}

		}

	}

	/**
	 * Method that extracts the root ct class.
	 *
	 * @param ctClass ct class to extract the root class
	 * @return root ct class
	 */
	private static CtClass extractRootCtClass(CtClass ctClass) {
		try {

			/* Check ct class is not null */
			if (Objects.nonNull(ctClass)) {

				/* Get the declaring class */
				CtClass declaring = ctClass.getDeclaringClass();
				if (Objects.nonNull(declaring)) {

					/* Return recursive call to extract declaring class */
					return extractRootCtClass(declaring);

				}

			}

		} catch (NotFoundException e) {
			// ignore return current class
		}

		/* Return current class by default */
		return ctClass;

	}

}
