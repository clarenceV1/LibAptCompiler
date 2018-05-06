package com.cai.apt.processor;

import com.cai.annotation.apt.Protocol;
import com.cai.annotation.apt.ProtocolShadow;
import com.cai.apt.ElementHolder;
import com.cai.annotation.apt.ProtocolUtil;

import java.io.IOException;
import java.io.Writer;
import java.lang.annotation.Annotation;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.HashMap;
import java.util.Map;

import javax.annotation.processing.RoundEnvironment;
import javax.lang.model.element.Element;
import javax.lang.model.element.ElementKind;
import javax.lang.model.element.TypeElement;
import javax.tools.JavaFileObject;

public class ProtocolProcessor implements IProcessor {
    AnnotationProcessor mAbstractProcessor;

    @Override
    public void process(RoundEnvironment roundEnv, AnnotationProcessor abstractProcessor) {
        this.mAbstractProcessor = abstractProcessor;
        processProtocol(roundEnv);
    }

    private void processProtocol(RoundEnvironment roundEnv) {
        Map<String, ElementHolder> shadowMap = collectClassInfo(roundEnv, ProtocolShadow.class, ElementKind.INTERFACE);
        for (String value : shadowMap.keySet()) {
            ProtocolUtil protocolDataClass = new ProtocolUtil();
            try {
                String simpleName = shadowMap.get(value).simpleName;
                JavaFileObject fileObject = mAbstractProcessor.mFiler.createSourceFile(ProtocolUtil.getClassNameForPackage(simpleName), (Element[]) null);
                Writer writer = fileObject.openWriter();
                writer.write(protocolDataClass.generateMiddleClass(simpleName, value));
                writer.flush();
                writer.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

        Map<String, ElementHolder> protocolMap = collectClassInfo(roundEnv, Protocol.class, ElementKind.CLASS);
        for (String value : protocolMap.keySet()) {
            ProtocolUtil protocolDataClass = new ProtocolUtil();
            try {
                JavaFileObject fileObject = mAbstractProcessor.mFiler.createSourceFile(ProtocolUtil.getClassNameForPackage(value), (Element[]) null);
                Writer writer = fileObject.openWriter();
                writer.write(protocolDataClass.generateMiddleClass(value, protocolMap.get(value).clazzName ));
                writer.flush();
                writer.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    private Map<String, ElementHolder> collectClassInfo(RoundEnvironment roundEnv, Class<? extends Annotation> clazz, ElementKind kind) {
        Map<String, ElementHolder> map = new HashMap<>();
        for (Element element : roundEnv.getElementsAnnotatedWith(clazz)) {
            if (element.getKind() != kind) {
                throw new IllegalStateException(
                        String.format("@%s annotation must be on a  %s.", element.getSimpleName(), kind.name()));
            }
            try {
                Annotation annotation = element.getAnnotation(clazz);
                Method annotationMethod = clazz.getDeclaredMethod("value");
                String name = (String) annotationMethod.invoke(annotation);
                TypeElement typeElement = (TypeElement) element;
                String clazzName = typeElement.getQualifiedName().toString();
                String simpleName = typeElement.getSimpleName().toString();
                map.put(name, new ElementHolder(typeElement, name, clazzName, simpleName));
            } catch (NoSuchMethodException e) {
                e.printStackTrace();
            } catch (InvocationTargetException e) {
                e.printStackTrace();
            } catch (IllegalAccessException e) {
                e.printStackTrace();
            }

        }
        return map;
    }
}
