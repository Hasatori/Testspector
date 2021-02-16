package com.testspector.gui;

import com.intellij.codeInsight.daemon.LineMarkerInfo;
import com.intellij.codeInsight.daemon.LineMarkerProvider;
import com.intellij.openapi.editor.markup.GutterIconRenderer;
import com.intellij.openapi.util.IconLoader;
import com.intellij.psi.PsiElement;
import com.sun.istack.NotNull;
import com.testspector.Testspector;
import com.testspector.checking.ProgrammingLanguageFactory;
import com.testspector.checking.TestResolveStrategy;
import com.testspector.checking.UnitTestFrameworkFactory;
import com.testspector.checking.java.junit.JUnitTestResolveStrategy;
import com.testspector.enums.ProgrammingLanguage;
import com.testspector.enums.UnitTestFramework;

import java.util.Optional;

public class TestLineMarkerFactory implements LineMarkerProvider {


    @Override
    public LineMarkerInfo<PsiElement> getLineMarkerInfo(@NotNull PsiElement element) {
        Optional<ProgrammingLanguage> optionalProgrammingLanguage = new ProgrammingLanguageFactory().resolveProgrammingLanguage(element);
        if (optionalProgrammingLanguage.isPresent()) {
            Optional<UnitTestFramework> optionalUnitTestFramework = new UnitTestFrameworkFactory().getUnitTestFramework(optionalProgrammingLanguage.get(), element);
            if (optionalUnitTestFramework.isPresent()) {
                Optional<TestResolveStrategy> optionalTestResolveStrategy = selectStrategy(optionalProgrammingLanguage.get(), optionalUnitTestFramework.get());
                if (optionalTestResolveStrategy.isPresent()) {
                    PsiElement test = optionalTestResolveStrategy.get().resolveTest(element);
                    if (test != null) {
                        return new LineMarkerInfo<>(
                                test,
                                test.getTextRange(),
                                IconLoader.getIcon("/icons/logo.svg"),
                                psiElement -> "Invoke inspection",
                                (mouseEvent, psiElement) -> Testspector.initializeTestspector(psiElement.getParent(), optionalProgrammingLanguage.get(), optionalUnitTestFramework.get()),
                                GutterIconRenderer.Alignment.RIGHT
                        );
                    }
                }
            }
        }
        return null;
    }

    private Optional<TestResolveStrategy> selectStrategy(ProgrammingLanguage programmingLanguage, UnitTestFramework unitTestFramework) {
        if (programmingLanguage == ProgrammingLanguage.JAVA && unitTestFramework == UnitTestFramework.JUNIT) {
            return Optional.of(new JUnitTestResolveStrategy());
        }
        return Optional.empty();
    }

}
