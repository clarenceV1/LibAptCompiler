package com.cai.apt.processor;

import com.cai.annotation.apt.Bind;
import com.cai.apt.BindUtil;
import com.cai.apt.utils.Utils;
import com.squareup.javapoet.ClassName;
import com.squareup.javapoet.CodeBlock;
import com.squareup.javapoet.JavaFile;
import com.squareup.javapoet.MethodSpec;
import com.squareup.javapoet.ParameterizedTypeName;
import com.squareup.javapoet.TypeSpec;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;

import javax.annotation.processing.RoundEnvironment;
import javax.lang.model.element.Modifier;
import javax.lang.model.element.TypeElement;
import javax.lang.model.element.VariableElement;
import javax.lang.model.util.ElementFilter;

import static com.squareup.javapoet.TypeSpec.classBuilder;
import static javax.lang.model.element.Modifier.PUBLIC;

public class BindProcessor implements IProcessor {
    AnnotationProcessor mAbstractProcessor;
    private Map<String, BindUtil> mProxyMap = new HashMap<String, BindUtil>();

    @Override
    public void process(RoundEnvironment roundEnv, AnnotationProcessor abstractProcessor) {
        this.mAbstractProcessor = abstractProcessor;
        mProxyMap.clear();
        processProtocol(roundEnv);
    }

    private void processProtocol(RoundEnvironment roundEnv) {
        Set<VariableElement> variableElements = ElementFilter.fieldsIn(roundEnv.getElementsAnnotatedWith(Bind.class));
        for (VariableElement variableElement : variableElements) {
            //class type
            TypeElement typeElement = (TypeElement) variableElement.getEnclosingElement();
            //class name
            String className = typeElement.getQualifiedName().toString();

            BindUtil proxyInfo = mProxyMap.get(className);
            if (proxyInfo == null) {
                proxyInfo = new BindUtil(mAbstractProcessor.mElements, typeElement);
                mProxyMap.put(className, proxyInfo);
            }
            Bind bindAnnotation = variableElement.getAnnotation(Bind.class);
            int id = bindAnnotation.value();
            proxyInfo.injectVariables.put(id, variableElement);
        }
        for (String key : mProxyMap.keySet()) {
            BindUtil proxyInfo = mProxyMap.get(key);
            try {
                TypeSpec.Builder tb= classBuilder(proxyInfo.proxyClassName)
                        .addModifiers(Modifier.PUBLIC)
                        .addJavadoc("@ 全局路由器 此类由apt自动生成")
                        .addSuperinterface(ParameterizedTypeName.get(ClassName.get("com.cai.framework.base", "ViewInject")
                                ,ClassName.get(proxyInfo.packageName, proxyInfo.typeElement.getSimpleName().toString())));

                MethodSpec.Builder methodBuilder1 = MethodSpec.methodBuilder("inject")
                        .addJavadoc("@此方法由apt自动生成")
                        .addModifiers(PUBLIC)
                        .addAnnotation(Override.class)
                        .addParameter(ClassName.get(proxyInfo.packageName, proxyInfo.typeElement.getSimpleName().toString()), "activity")
                        .addParameter(Object.class, "source");
                methodBuilder1.returns(void.class);

                CodeBlock.Builder codeBuilder = CodeBlock.builder();
                Map<Integer, VariableElement> injectVariables = proxyInfo.injectVariables;
                for (int id : injectVariables.keySet()) {
                    VariableElement element = injectVariables.get(id);
                    String name = element.getSimpleName().toString();
                    String type = element.asType().toString();
                    codeBuilder.add("activity." + name+" = (" + type + ")(((android.app.Activity)source).findViewById( " + id + "));\n");
                }
                methodBuilder1.addCode(codeBuilder.build());
                tb.addMethod(methodBuilder1.build());

                JavaFile javaFile = JavaFile.builder(Utils.PackageName, tb.build()).build();// 生成源代码
                javaFile.writeTo(mAbstractProcessor.mFiler);// 在 app module/build/generated/source/apt 生成一份源代码
            } catch (Exception e) {

            }
        }
    }
}
