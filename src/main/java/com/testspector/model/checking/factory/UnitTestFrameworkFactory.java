package com.testspector.model.checking.factory;

import com.intellij.psi.PsiElement;
import com.testspector.model.enums.UnitTestFramework;

import java.util.List;
import java.util.Optional;

public interface UnitTestFrameworkFactory {

 Optional<UnitTestFramework> getUnitTestFramework(PsiElement psiElement);

}
