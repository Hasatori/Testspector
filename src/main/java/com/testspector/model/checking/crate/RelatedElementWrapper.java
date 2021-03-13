package com.testspector.model.checking.crate;

import com.intellij.psi.PsiElement;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

public class RelatedElementWrapper {

    private final String name;
    private final Map<PsiElement,String> relatedElementNameHashMap;

    public RelatedElementWrapper(String name, HashMap<PsiElement, String> relatedElementNameHashMap) {
        this.name = name;
        this.relatedElementNameHashMap = Collections.unmodifiableMap(relatedElementNameHashMap);
    }


    public String getName() {
        return name;
    }

    public Map<PsiElement, String> getRelatedElementNameHashMap() {
        return relatedElementNameHashMap;
    }
}
