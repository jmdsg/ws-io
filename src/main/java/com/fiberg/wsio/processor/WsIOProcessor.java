package com.fiberg.wsio.processor;

import com.fiberg.wsio.annotation.Case;
import com.fiberg.wsio.annotation.WsIOClone;
import com.fiberg.wsio.annotation.WsIOMessage;
import com.fiberg.wsio.annotation.WsIOMessageWrapper;
import com.google.auto.service.AutoService;
import io.vavr.Function1;
import io.vavr.Predicates;
import io.vavr.Tuple2;
import io.vavr.Tuple3;
import io.vavr.collection.HashSet;
import io.vavr.collection.Map;
import io.vavr.collection.Set;
import io.vavr.collection.Stream;

import javax.annotation.processing.*;
import javax.lang.model.SourceVersion;
import javax.lang.model.element.Element;
import javax.lang.model.element.TypeElement;

/**
 * Annotation processor class used to generate clone, message and message wrapper classes.
 */
@AutoService(Processor.class)
public class WsIOProcessor extends AbstractProcessor {

	/** Filer to create java classes */
	private Filer filer;

	/** Messager to print annotation processor errors */
	private Messager messager;

	/**
	 * {@inheritDoc}
	 */
	@Override
	public synchronized void init(final ProcessingEnvironment processingEnv) {
		super.init(processingEnv);
		this.filer = processingEnv.getFiler();
		this.messager = processingEnv.getMessager();
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public boolean process(final java.util.Set<? extends TypeElement> annotations,
	                       final RoundEnvironment roundEnv) {

		/* Get all root elements */
		final Set<Element> rootElements = HashSet.ofAll(roundEnv.getRootElements());

		/* Function used to get type elements from a element iterable */
		final Function1<Iterable<? extends Element>, Set<TypeElement>> obtainTypeElements = elements ->
				Stream.ofAll(elements)
						.filter(TypeElement.class::isInstance)
						.map(TypeElement.class::cast)
				.toSet();

		/* Set of all root elements of type elements */
		final Set<TypeElement> rootTypeElements = obtainTypeElements.apply(rootElements);

		/* Set of root elements that are not clones, messages or messages of clone classes */
		final Set<TypeElement> rootTypeNotGeneratedElements = rootTypeElements
				.filter(Predicates.noneOf(WsIOFinder::isMetadataGenerated, WsIOFinder::isMessageGenerated,
						WsIOFinder::isCloneGenerated, WsIOFinder::isCloneMessageGenerated));

		/* Find message classes recursively */
		final Map<TypeElement, String> messageByType = WsIOFinder.findMessageRecursively(rootTypeNotGeneratedElements);

		/* Find clone classes recursively */
		final Map<Tuple2<String, String>, Set<Tuple2<TypeElement, String>>> cloneByGroup =
				WsIOFinder.findCloneRecursively(rootTypeNotGeneratedElements)
						.filterValues(Set::nonEmpty);

		/* Find wrapper classes recursively */
		final Map<TypeElement, Map<String, Tuple2<WsIOInfo, String>>> wrapperByType =
				WsIOFinder.findWrapperRecursively(rootTypeElements)
						.filterValues(Map::nonEmpty);

		/* Find message of cloned classes from the current clone and message classes */
		final Map<Tuple2<String, String>, Set<Tuple2<TypeElement, String>>> cloneMessageByGroup =
				WsIOFinder.findCloneMessage(messageByType, cloneByGroup)
						.filterValues(Set::nonEmpty);

		final Map<TypeElement, Tuple3<String, Set<Case>, Map<String, Boolean>>> metadataByType =
				WsIOFinder.findMetadataRecursively(rootTypeNotGeneratedElements)
						.filterValues(tuple -> tuple._2().nonEmpty() && tuple._3().nonEmpty());

		/* Create the generator class */
		final WsIOGenerator generator = new WsIOGenerator(messager, filer);

		/* Return the result of generating the classes */
		return generator.generateClasses(messageByType, cloneByGroup,
				cloneMessageByGroup, wrapperByType, metadataByType);

	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public java.util.Set<String> getSupportedAnnotationTypes() {
		final java.util.Set<String> annotataions = new java.util.LinkedHashSet<>();
		annotataions.add(WsIOMessage.class.getCanonicalName());
		annotataions.add(WsIOMessageWrapper.class.getCanonicalName());
		annotataions.add(WsIOClone.class.getCanonicalName());
		return annotataions;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public SourceVersion getSupportedSourceVersion() {
		return SourceVersion.latestSupported();
	}

}
