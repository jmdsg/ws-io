package com.fiberg.wsio.auto;

import com.fiberg.wsio.annotation.WsIOAnnotate;
import com.fiberg.wsio.annotation.WsIOMessageWrapper;
import com.fiberg.wsio.annotation.WsIOSkipMessageWrapper;
import com.fiberg.wsio.processor.WsIOConstant;
import com.fiberg.wsio.processor.WsIOJsEngine;
import com.fiberg.wsio.util.WsIOUtil;
import io.github.lukehutch.fastclasspathscanner.FastClasspathScanner;
import io.vavr.Tuple;
import io.vavr.Tuple2;
import javassist.*;
import javassist.bytecode.AnnotationsAttribute;
import javassist.bytecode.ClassFile;
import javassist.bytecode.ConstPool;
import javassist.bytecode.annotation.StringMemberValue;
import org.apache.commons.lang3.ObjectUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.mutable.Mutable;
import org.apache.commons.lang3.mutable.MutableObject;
import org.apache.commons.text.WordUtils;

import javax.jws.WebMethod;
import javax.xml.ws.RequestWrapper;
import javax.xml.ws.ResponseWrapper;
import java.util.List;
import java.util.Map;
import java.util.function.BiConsumer;
import java.util.stream.Collectors;

public final class WsIOAuto {

	private WsIOAuto() {}

	public static void annotate(String basePackage) {

		List<String> classNames = new FastClasspathScanner(basePackage)
				.scan()
				.getNamesOfAllClasses();

		ClassPool pool = ClassPool.getDefault();
		Map<String, CtClass> ctClasses = classNames.stream()
				.map(className -> {
					CtClass ctClass = null;
					if (className.startsWith(basePackage)) {
						try {
							ctClass = pool.getCtClass(className);
						} catch (NotFoundException e) {
							// ignore class not found
						}
					}
					return Tuple.of(className, ctClass);
				})
				.filter(tuple -> tuple._2() != null)
				.collect(Collectors.toMap(Tuple2::_1, Tuple2::_2));

		Map<String, CtClass> packages = ctClasses.values().stream()
				.map(CtClass::getPackageName)
				.collect(Collectors.toSet())
				.stream()
				.map(packageName -> {
					CtClass ctPackage;
					try {
						String packageClassName = WsIOUtil.addPrefixName("package-info", packageName);
						ctPackage = pool.getCtClass(packageClassName);
					} catch (NotFoundException e) {
						ctPackage = null;
					}
					return Tuple.of(packageName, ctPackage);
				})
				.filter(tuple -> tuple._2() != null)
				.collect(Collectors.toMap(Tuple2::_1, Tuple2::_2));

		ctClasses.forEach((className, ctClass) -> {

			Mutable<Boolean> annotated = new MutableObject<>(false);
			String classSimpleName = ctClass.getSimpleName();
			String packageName = ctClass.getPackageName();
			CtClass ctPackage = packages.get(packageName);

			WsIOAnnotate packageAnnotate = obtainAnnotation(ctPackage, WsIOAnnotate.class);
			WsIOAnnotate classAnnotate = obtainAnnotation(ctClass, WsIOAnnotate.class);

			WsIOMessageWrapper packageAnnotation = obtainAnnotation(ctPackage, WsIOMessageWrapper.class);
			WsIOMessageWrapper classAnnotation = obtainAnnotation(ctClass, WsIOMessageWrapper.class);
			WsIOSkipMessageWrapper skipClass = obtainAnnotation(ctClass, WsIOSkipMessageWrapper.class);

			for (CtMethod ctMethod : ctClass.getDeclaredMethods()) {
				WebMethod webMethod = obtainAnnotation(ctMethod, WebMethod.class);
				if (webMethod != null) {

					String methodName = ctMethod.getName();
					WsIOAnnotate methodAnnotate = obtainAnnotation(ctMethod, WsIOAnnotate.class);
					WsIOAnnotate annotate = ObjectUtils.firstNonNull(methodAnnotate, classAnnotate, packageAnnotate);
					if (annotate != null) {

						WsIOMessageWrapper methodAnnotation = obtainAnnotation(ctMethod, WsIOMessageWrapper.class);
						WsIOSkipMessageWrapper skipMethod = obtainAnnotation(ctMethod, WsIOSkipMessageWrapper.class);
						WsIOMessageWrapper annotation = ObjectUtils.firstNonNull(methodAnnotation,
								classAnnotation, packageAnnotation);
						WsIOSkipMessageWrapper skip = ObjectUtils.firstNonNull(skipMethod, skipClass);

						BiConsumer<String, String> addAnnotationWrapper = (wrapper, name) -> {
							try {
								Class.forName(wrapper);
								ClassFile ccFile = ctClass.getClassFile();
								ConstPool constpool = ccFile.getConstPool();
								AnnotationsAttribute attr = new AnnotationsAttribute(constpool, AnnotationsAttribute.visibleTag);
								javassist.bytecode.annotation.Annotation annot = new javassist.bytecode.annotation.Annotation(name, constpool);
								annot.addMemberValue("className", new StringMemberValue(wrapper, ccFile.getConstPool()));
								attr.addAnnotation(annot);
								ctMethod.getMethodInfo().addAttribute(attr);
								annotated.setValue(true);
							} catch (ClassNotFoundException e) {
								// ignore class does not exists
							}
						};

						if (skip == null && annotation != null) {

							String finalPackage = WsIOJsEngine.obtainPackage(methodName, classSimpleName, packageName,
									annotation.packageName(), annotation.packagePath(), annotation.packagePrefix(),
									annotation.packageSuffix(), annotation.packageStart(), annotation.packageMiddle(),
									annotation.packageEnd(), annotation.packageJs());

							if (StringUtils.isNotBlank(finalPackage)) {

								String upperName = WordUtils.capitalize(methodName);
								String wrappedResponseName = WsIOUtil.addWrap(upperName, WsIOConstant.RESPONSE_WRAPPER_PREFIX,
										WsIOConstant.RESPONSE_WRAPPER_SUFFIX);
								String wrappedRequestName = WsIOUtil.addWrap(upperName, WsIOConstant.REQUEST_WRAPPER_PREFIX,
										WsIOConstant.REQUEST_WRAPPER_SUFFIX);

								String responseName = WsIOUtil.addPrefixName(wrappedResponseName, finalPackage);
								String requestName = WsIOUtil.addPrefixName(wrappedRequestName, finalPackage);

								if (StringUtils.isNotBlank(responseName)) {
									addAnnotationWrapper.accept(responseName, ResponseWrapper.class.getCanonicalName());
								}
								if (StringUtils.isNotBlank(requestName)) {
									addAnnotationWrapper.accept(requestName, RequestWrapper.class.getCanonicalName());
								}

							}

						}

					}
				}
			}

			if (annotated.getValue()) {
				try {
					ctClass.toClass();
				} catch (CannotCompileException e) {
					throw new IllegalStateException(String.format("Could not compile the class %s", ctClass), e);
				}
			}

		});

	}

	private static <T> T obtainAnnotation(Object ct, Class<T> annotation) {
		if (ct != null) {
			try {
				if (ct instanceof CtClass) {
					@SuppressWarnings("unchecked")
					T annot = (T) ((CtClass) ct).getAnnotation(annotation);
					return annot;
				} else if (ct instanceof CtMethod) {
					@SuppressWarnings("unchecked")
					T annot = (T) ((CtMethod) ct).getAnnotation(annotation);
					return annot;
				}
			} catch (ClassNotFoundException e) {
				return null;
			}
		}
		return null;
	}

}
