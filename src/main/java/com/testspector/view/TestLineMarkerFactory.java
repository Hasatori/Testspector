package com.testspector.view;

import com.intellij.codeInsight.daemon.LineMarkerInfo;
import com.intellij.codeInsight.daemon.LineMarkerProvider;
import com.intellij.openapi.editor.markup.GutterIconRenderer;
import com.intellij.psi.PsiElement;
import com.sun.istack.NotNull;
import com.testspector.controller.TestspectorController;
import com.testspector.model.checking.TestLineCrate;

import java.util.Optional;

public class TestLineMarkerFactory implements LineMarkerProvider {

    @Override
    public LineMarkerInfo<PsiElement> getLineMarkerInfo(@NotNull PsiElement element) {
        Optional<TestLineCrate> optionalTestLineCrate = TestspectorController.resolveTestLineCrate(element);
        return optionalTestLineCrate.map(testLineCrate -> new LineMarkerInfo<>(
                testLineCrate.getLineElement(),
                testLineCrate.getLineElement().getTextRange(),
                CustomIcon.LOGO.getBasic(),
                psiElement -> "Invoke inspection",
                (mouseEvent, psiElement) -> TestspectorController.initializeTestspector(psiElement.getParent(), testLineCrate.getProgrammingLanguage(), testLineCrate.getUnitTestFramework()),
                GutterIconRenderer.Alignment.RIGHT
        )).orElse(null);
    }
}
