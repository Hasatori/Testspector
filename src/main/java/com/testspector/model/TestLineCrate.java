package com.testspector.model;

import com.intellij.psi.PsiElement;
import com.testspector.model.enums.ProgrammingLanguage;
import com.testspector.model.enums.UnitTestFramework;

public class TestLineCrate {

    private final PsiElement lineElement;

    private final ProgrammingLanguage programmingLanguage;

    private final  UnitTestFramework unitTestFramework;


    public TestLineCrate(PsiElement lineElement, ProgrammingLanguage programmingLanguage, UnitTestFramework unitTestFramework) {
        this.lineElement = lineElement;
        this.programmingLanguage = programmingLanguage;
        this.unitTestFramework = unitTestFramework;
    }

    public PsiElement getLineElement() {
        return lineElement;
    }

    public ProgrammingLanguage getProgrammingLanguage() {
        return programmingLanguage;
    }

    public UnitTestFramework getUnitTestFramework() {
        return unitTestFramework;
    }
}
