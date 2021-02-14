package com.testspector;

import com.intellij.execution.ui.ConsoleViewContentType;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.wm.ToolWindow;
import com.intellij.openapi.wm.ToolWindowAnchor;
import com.intellij.openapi.wm.ToolWindowManager;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiFile;
import com.intellij.psi.PsiIdentifier;
import com.intellij.ui.content.Content;
import com.intellij.ui.content.ContentFactory;
import com.testspector.checking.BestPracticeViolation;
import com.testspector.enums.BestPractice;
import com.testspector.enums.ProgrammingLanguage;
import com.testspector.enums.UnitTestFramework;
import com.testspector.gui.ToolWindowContent;

import java.util.*;

public class Testspector {

    private static final HashMap<Project, ToolWindow> PROJECT_TOOL_WINDOW_HASH_MAP = new HashMap<>();

    private Testspector() {
    }

    public static void initializeTestspector(PsiFile file) {

    }

    public static void initializeTestspector(List<PsiFile> files) {

    }

    public static void initializeTestspector(PsiElement element, ProgrammingLanguage programmingLanguage, UnitTestFramework unitTestFramework) {
        Project project = element.getProject();
        ToolWindow toolWindow = getToolWindow(project);
        ContentFactory contentFactory = ContentFactory.SERVICE.getInstance();
        ToolWindowContent toolWindowContent = new ToolWindowContent(project, (content) -> {
            Runnable runInspection = new RunInspectionTest(content, Collections.singletonList(element), programmingLanguage, unitTestFramework);
            content.getConsoleView().print("\nRerunning inspection on " + element.toString(), ConsoleViewContentType.LOG_INFO_OUTPUT);
            new Thread(runInspection).start();
        });
        Content content = contentFactory.createContent(toolWindowContent.getPanel1(), element.toString(), false);
        toolWindow.getContentManager().addContent(content);
        if (toolWindow.getContentManager().getContentCount() > 1) {
            toolWindow.getContentManager().selectNextContent();
        }
        toolWindowContent.getConsoleView().print("\nInspecting tests in " + element.toString(), ConsoleViewContentType.LOG_INFO_OUTPUT);
        toolWindowContent.getConsoleView().print(String.format("\nProgramming language:%s and unit testig framework:%s were detected", programmingLanguage, unitTestFramework), ConsoleViewContentType.LOG_INFO_OUTPUT);
        toolWindowContent.getConsoleView().print("\nInitializing inspection.... This might take a while...", ConsoleViewContentType.LOG_INFO_OUTPUT);
        Runnable runInspection = new RunInspectionTest(toolWindowContent, Collections.singletonList(element), programmingLanguage, unitTestFramework);
        new Thread(runInspection).start();

    }

    private static ToolWindow getToolWindow(Project project) {
        ToolWindow resultToolWindow = PROJECT_TOOL_WINDOW_HASH_MAP.get(project);
        if (resultToolWindow == null) {
            resultToolWindow = ToolWindowManager.getInstance(project)
                    .registerToolWindow("Testspector", true, ToolWindowAnchor.BOTTOM);
            PROJECT_TOOL_WINDOW_HASH_MAP.put(project, resultToolWindow);
        }
        return resultToolWindow;
    }

    public static class RunInspectionTest implements Runnable {

        private final ToolWindowContent toolWindowContent;
        private final ProgrammingLanguage programmingLanguage;
        private final UnitTestFramework unitTestFramework;
        private final List<PsiElement> psiElements;

        public RunInspectionTest(ToolWindowContent toolWindowContent, List<PsiElement> psiElements, ProgrammingLanguage programmingLanguage, UnitTestFramework unitTestFramework) {
            this.psiElements = psiElements;
            this.toolWindowContent = toolWindowContent;
            this.programmingLanguage = programmingLanguage;
            this.unitTestFramework = unitTestFramework;
        }

        @Override
        public void run() {
            try {
                Thread.sleep(3000);
                List<BestPracticeViolation> bestPracticeViolations = new ArrayList<>();
                for (PsiElement psiElement : psiElements) {
                    PsiElement psiIdentifierElement = Arrays.stream(psiElement.getChildren()).filter(el -> el instanceof PsiIdentifier).findFirst().get();
                    bestPracticeViolations.add(new BestPracticeViolation(psiIdentifierElement, psiIdentifierElement.getTextRange(), "No contion logic should be in a test, it makes it hard to understand", BestPractice.NO_CONDITIONAL_LOGIC));
                }

                toolWindowContent.getConsoleView().print("\nInspection finished. Found: ", ConsoleViewContentType.LOG_INFO_OUTPUT);
                ConsoleViewContentType numberType = ConsoleViewContentType.ERROR_OUTPUT;
                if (bestPracticeViolations.size() == 0) {
                    numberType = ConsoleViewContentType.LOG_DEBUG_OUTPUT;
                }
                toolWindowContent.getConsoleView().print(String.format("%d violations", bestPracticeViolations.size()), numberType);
                toolWindowContent.showReport(bestPracticeViolations);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
    }
}
