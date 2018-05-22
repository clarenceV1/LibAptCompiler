package com.cai.apt;

import java.util.HashMap;
import java.util.Map;

import javax.lang.model.element.PackageElement;
import javax.lang.model.element.TypeElement;
import javax.lang.model.element.VariableElement;
import javax.lang.model.util.Elements;

public class BindUtil {
    public String packageName;
    public String proxyClassName;
    public TypeElement typeElement;

    public Map<Integer, VariableElement> injectVariables = new HashMap<>();

    public static final String PROXY = "ViewInject";

    public BindUtil(Elements elementUtils, TypeElement classElement) {
        this.typeElement = classElement;
        PackageElement packageElement = elementUtils.getPackageOf(classElement);
        String packageName = packageElement.getQualifiedName().toString();
        //classname
        String className = getClassName(classElement, packageName);
        this.packageName = packageName;
        this.proxyClassName = className + "$$" + PROXY;
    }

    String getClassName(TypeElement type, String packageName) {
        int packageLen = packageName.length() + 1;
        return type.getQualifiedName().toString().substring(packageLen)
                .replace('.', '$');
    }

}
