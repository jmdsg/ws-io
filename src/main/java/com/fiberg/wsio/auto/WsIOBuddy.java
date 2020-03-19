package com.fiberg.wsio.auto;

import net.bytebuddy.ByteBuddy;
import net.bytebuddy.asm.AsmVisitorWrapper;
import net.bytebuddy.asm.MemberAttributeExtension;
import net.bytebuddy.description.annotation.AnnotationDescription;
import net.bytebuddy.description.field.FieldDescription;
import net.bytebuddy.description.field.FieldList;
import net.bytebuddy.description.method.MethodDescription;
import net.bytebuddy.description.method.MethodList;
import net.bytebuddy.description.type.TypeDescription;
import net.bytebuddy.dynamic.ClassFileLocator;
import net.bytebuddy.dynamic.DynamicType;
import net.bytebuddy.dynamic.loading.ClassLoadingStrategy;
import net.bytebuddy.implementation.Implementation;
import net.bytebuddy.jar.asm.AnnotationVisitor;
import net.bytebuddy.jar.asm.MethodVisitor;
import net.bytebuddy.jar.asm.Opcodes;
import net.bytebuddy.jar.asm.Type;
import net.bytebuddy.matcher.ElementMatchers;
import net.bytebuddy.pool.TypePool;

import javax.annotation.Nonnull;
import javax.jws.WebMethod;
import javax.jws.WebService;
import java.lang.annotation.Annotation;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

public class WsIOBuddy {
    public static void main(String[] args) {

        ClassLoader classLoader = WsIOBuddy.class.getClassLoader();
        String nameA = "com.fiberg.wsio.auto.A";
        String packageA = "com.fiberg.wsio.auto";

        TypePool pool = TypePool.Default.of(classLoader);

        TypeDescription typeDescriptionA = pool.describe(nameA).resolve();

        DynamicType.Unloaded<Object> transformedA = new ByteBuddy()
                .redefine(typeDescriptionA, ClassFileLocator.ForClassLoader.of(classLoader))
                .visit(new MemberAttributeExtension.ForMethod()
                        .annotateMethod(AnnotationDescription.Builder.ofType(WebMethod.class)
                                .define("operationName", "juju")
                                .build())
                        .on(ElementMatchers.named("getB")))
                .visit(new AsmVisitorWrapper.ForDeclaredMethods()
                        .method(ElementMatchers.any(),
                                new AsmVisitorWrapper.ForDeclaredMethods.MethodVisitorWrapper() {
                                    @Override
                                    public MethodVisitor wrap(TypeDescription instrumentedType,
                                                              MethodDescription instrumentedMethod,
                                                              MethodVisitor methodVisitor,
                                                              Implementation.Context implementationContext,
                                                              TypePool typePool,
                                                              int writerFlags,
                                                              int readerFlags) {
                                        return new MethodVisitor(Opcodes.ASM5, methodVisitor) {
                                            @Override
                                            public AnnotationVisitor visitAnnotation(String desc, boolean visible) {
                                                if (Type.getDescriptor(WebMethod.class).equals(desc)) {
                                                    return null;
                                                }
                                                return super.visitAnnotation(desc, visible);
                                            }
                                        };
                                    }
                                }))

                .make();

        transformedA.load(classLoader, ClassLoadingStrategy.Default.INJECTION);

/*
        String nameA = "com.fiberg.wsio.auto.A";
        String nameB = "com.fiberg.wsio.auto.B";

        ClassLoader classLoader = WsIOBuddy.class.getClassLoader();
        TypePool pool = TypePool.Default.of(classLoader);

        TypeDescription typeDescriptionA = pool.describe(nameA).resolve();
        //TypeDescription typeDescriptionB = pool.describe(nameB).resolve();

        AnnotationDescription annotationDescription = AnnotationDescription.Builder.ofType(Nonnull.class)
                .build();

        DynamicType.Unloaded<Object> transformedA = new ByteBuddy()
                .redefine(typeDescriptionA, ClassFileLocator.ForClassLoader.of(classLoader))
                .visit(new AsmVisitorWrapper.ForDeclaredMethods()
                        .method(ElementMatchers.isAnnotatedWith(Deprecated.class),
                                new AsmVisitorWrapper.ForDeclaredMethods.MethodVisitorWrapper() {
                                    @Override
                                    public MethodVisitor wrap(TypeDescription instrumentedType,
                                                              MethodDescription instrumentedMethod,
                                                              MethodVisitor methodVisitor,
                                                              Implementation.Context implementationContext,
                                                              TypePool typePool,
                                                              int writerFlags,
                                                              int readerFlags) {
                                        return new MethodVisitor(Opcodes.ASM5, methodVisitor) {
                                            @Override
                                            public AnnotationVisitor visitAnnotation(String desc, boolean visible) {
                                                if (Type.getDescriptor(Deprecated.class).equals(desc)) {
                                                    return null;
                                                }
                                                return super.visitAnnotation(desc, visible);
                                            }
                                        };
                                    }
                                }))
                .make();

        DynamicType.Unloaded<Object> transformedAA = new ByteBuddy()
                .decorate(typeDescriptionA, ClassFileLocator.ForClassLoader.of(classLoader))
                .visit(new MemberAttributeExtension.ForMethod()
                        .annotateMethod(annotationDescription)
                        .on(ElementMatchers.named("getB")))
                .make();
*/
/*
        DynamicType.Unloaded<Object> transformedB = new ByteBuddy()
                .rebase(typeDescriptionB, ClassFileLocator.ForClassLoader.of(classLoader))
                .visit(new MemberAttributeExtension.ForMethod()
                        .annotateMethod(annotationDescription)
                        .on(ElementMatchers.named("getA")))
                .make();


        try {
            Method m = ClassLoader.class.getDeclaredMethod("findLoadedClass", String.class);
            m.setAccessible(true);
            System.out.println(m.invoke(classLoader, nameA));
            System.out.println(m.invoke(classLoader, nameB));
        } catch (NoSuchMethodException | IllegalAccessException | InvocationTargetException e) {
            e.printStackTrace();
        }

        transformedA
                //.include(transformedB)
                .load(
                        classLoader,
                        ClassLoadingStrategy.Default.INJECTION
                );
*/

        try {
            Method m = ClassLoader.class.getDeclaredMethod("findLoadedClass", String.class);
            m.setAccessible(true);
            System.out.println(m.invoke(classLoader, nameA));
        } catch (NoSuchMethodException | IllegalAccessException | InvocationTargetException e) {
            e.printStackTrace();
        }

        try {
            ReflectUtils.printClassAnnotations(Class.forName("com.fiberg.wsio.auto.A"));
        } catch (ClassNotFoundException e) {
            e.printStackTrace();
        }

    }

    @WebService(name = "Jojo")
    public static class C {

    }

}
