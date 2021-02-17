package com.testspector.view;

import com.intellij.codeInsight.daemon.LineMarkerInfo;
import com.intellij.codeInsight.daemon.LineMarkerProvider;
import com.intellij.openapi.editor.markup.GutterIconRenderer;
import com.intellij.psi.PsiElement;
import com.sun.istack.NotNull;
import com.testspector.controller.TestspectorController;
import com.testspector.model.checking.ProgrammingLanguageFactory;
import com.testspector.model.checking.TestResolveStrategy;
import com.testspector.model.checking.UnitTestFrameworkFactory;
import com.testspector.model.checking.java.junit.JUnitTestResolveStrategy;
import com.testspector.model.enums.ProgrammingLanguage;
import com.testspector.model.enums.UnitTestFramework;

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
                                Icons.LOGO,
                                psiElement -> "Invoke inspection",
                                (mouseEvent, psiElement) -> TestspectorController.initializeTestspector(element.getProject(), psiElement.getParent(), optionalProgrammingLanguage.get(), optionalUnitTestFramework.get()),
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
