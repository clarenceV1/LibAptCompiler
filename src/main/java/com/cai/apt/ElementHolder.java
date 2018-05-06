package com.cai.apt;

import javax.lang.model.element.TypeElement;

public class ElementHolder {
    public TypeElement typeElement;
    public String valueName;
    public String clazzName;
    public String simpleName;

    public ElementHolder(TypeElement typeElement, String valueName, String clazzName, String simpleName) {
        this.typeElement = typeElement;
        this.valueName = valueName;
        this.clazzName = clazzName;
        this.simpleName = simpleName;
    }
}
