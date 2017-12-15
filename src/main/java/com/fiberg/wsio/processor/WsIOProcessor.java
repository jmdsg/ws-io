package com.fiberg.wsio.processor;

import com.fiberg.wsio.annotation.WsIOClone;
import com.fiberg.wsio.annotation.WsIOMessage;
import com.fiberg.wsio.annotation.WsIOMessageWrapper;
import com.google.auto.service.AutoService;
import io.vavr.Function1;
import io.vavr.Predicates;
import io.vavr.Tuple2;
import io.vavr.Tuple3;
import io.vavr.collection.*;

import javax.annotation.processing.*;
import javax.lang.model.SourceVersion;
import javax.lang.model.element.*;

@AutoService(Processor.class)
public class WsIOProcessor extends AbstractProcessor {

	private Filer filer;

	private Messager messager;

	@Override
	public synchronized void init(ProcessingEnvironment processingEnv) {
		super.init(processingEnv);
		this.filer = processingEnv.getFiler();
		this.messager = processingEnv.getMessager();
	}

	@Override
	public boolean process(java.util.Set<? extends TypeElement> annotations, RoundEnvironment roundEnv) {

		Set<Element> rootElements = HashSet.ofAll(roundEnv.getRootElements());

		Function1<Iterable<? extends Element>, Set<TypeElement>> obtainTypeElements = elements ->
				Stream.ofAll(elements)
						.filter(TypeElement.class::isInstance)
						.map(TypeElement.class::cast)
				.toSet();

		Set<TypeElement> rootTypeElements = obtainTypeElements.apply(rootElements);

		Set<TypeElement> rootTypeNotGeneratedElements = rootTypeElements
				.filter(Predicates.noneOf(WsIOFinder::isMessageGenerated,
						WsIOFinder::isCloneGenerated, WsIOFinder::isCloneMessageGenerated));

		Map<TypeElement, String> messageByType = WsIOFinder.findMessageRecursively(rootTypeNotGeneratedElements);

		Map<Tuple2<String, String>, Set<Tuple2<TypeElement, String>>> cloneByGroup =
				WsIOFinder.findCloneRecursively(rootTypeNotGeneratedElements);

		Map<TypeElement, Map<String, Tuple2<WsIOInfo, String>>> wrapperByType =
				WsIOFinder.findWrapperRecursively(rootTypeElements);

		Map<Tuple2<String, String>, Set<Tuple2<TypeElement, String>>> cloneMessageByGroup =
				WsIOFinder.findCloneMessage(messageByType, cloneByGroup);

		WsIOGenerator generator = new WsIOGenerator(messager, filer);

		return generator.generateClasses(messageByType, cloneByGroup, cloneMessageByGroup, wrapperByType);

	}

	@Override
	public java.util.Set<String> getSupportedAnnotationTypes() {
		java.util.Set<String> annotataions = new java.util.LinkedHashSet<>();
		annotataions.add(WsIOMessage.class.getCanonicalName());
		annotataions.add(WsIOMessageWrapper.class.getCanonicalName());
		annotataions.add(WsIOClone.class.getCanonicalName());
		return annotataions;
	}

	@Override
	public SourceVersion getSupportedSourceVersion() {
		return SourceVersion.latestSupported();
	}

}
