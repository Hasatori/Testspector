package com.testspector;

import a.f.I;
import com.intellij.execution.ui.ConsoleViewContentType;
import com.intellij.openapi.application.ApplicationManager;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.util.Computable;
import com.intellij.openapi.util.IconLoader;
import com.intellij.openapi.wm.ToolWindow;
import com.intellij.openapi.wm.ToolWindowAnchor;
import com.intellij.openapi.wm.ToolWindowManager;
import com.intellij.psi.*;
import com.intellij.ui.content.Content;
import com.intellij.ui.content.ContentFactory;
import com.testspector.checking.BestPracticeViolation;
import com.testspector.enums.BestPractice;
import com.testspector.enums.ProgrammingLanguage;
import com.testspector.enums.UnitTestFramework;
import com.testspector.gui.RerunToolWindowContentAction;
import com.testspector.gui.ToolWindowContent;
import com.testspector.utils.ProgrammingLanguageResolver;
import com.testspector.utils.UnitTestFrameworkResolver;

import java.util.*;
import java.util.stream.Collectors;

public class Testspector {

    private static final HashMap<Project, ToolWindow> PROJECT_TOOL_WINDOW_HASH_MAP = new HashMap<>();

    private Testspector() {
    }

    public static void initializeTestspector(PsiFile file) {
        initializeTestspector(Collections.singletonList(file), file.getName());
    }

    public static void initializeTestspector(List<PsiFile> files, String name) {
        Project project = files.get(0).getProject();
        ToolWindow toolWindow = getToolWindow(project);
        ContentFactory contentFactory = ContentFactory.SERVICE.getInstance();


        ToolWindowContent toolWindowContent = new ToolWindowContent(project);
        Content content = contentFactory.createContent(toolWindowContent.getPanel1(), name, false);
        toolWindow.getContentManager().addContent(content);
        if (toolWindow.getContentManager().getContentCount() > 1) {
            toolWindow.getContentManager().selectNextContent();
        }
        toolWindowContent.getConsoleView().print("\nInspecting tests in " + name, ConsoleViewContentType.LOG_INFO_OUTPUT);
        List<PsiElement> elements = new ArrayList<>();
        List<Thread> threads = new ArrayList<>();
        for (PsiFile file : files) {

            Thread t = new Thread(() -> {
                ProgrammingLanguage programmingLanguage = ProgrammingLanguageResolver.resolveProgrammingLanguage(file);
                if (programmingLanguage != null) {
                    toolWindowContent.getConsoleView().print("\n["+file.getName()+"] Programming language " + programmingLanguage + " detected", ConsoleViewContentType.LOG_INFO_OUTPUT);

                    UnitTestFramework unitTestFramework = ApplicationManager.getApplication().runReadAction((Computable<UnitTestFramework>) () -> UnitTestFrameworkResolver.resolveUnitTestFramework(programmingLanguage, file));
                    if (unitTestFramework != null) {
                        toolWindowContent.getConsoleView().print("\n["+file.getName()+"] Tests from unit testing framework " + unitTestFramework + " detected", ConsoleViewContentType.LOG_INFO_OUTPUT);
                        toolWindowContent.getConsoleView().print("\n["+file.getName()+"] Initializing inspection.... This might take a while...", ConsoleViewContentType.LOG_INFO_OUTPUT);
                        elements.addAll(ApplicationManager.getApplication().runReadAction((Computable<List<PsiElement>>) () -> {
                            List<PsiElement> resultElements = new ArrayList<>();
                            PsiClass psiClass = (PsiClass) Arrays.stream(file.getChildren()).filter(child -> child instanceof PsiClass).findFirst().orElse(null);
                            if (psiClass != null) {
                                resultElements.addAll(Arrays.stream(psiClass.getChildren()).filter(child -> child instanceof PsiMethod).collect(Collectors.toList()));
                            }
                            return resultElements;
                        }));
                    } else {
                        toolWindowContent.getConsoleView().print("\n["+file.getName()+"] Not a test class or test are written in a unit testing framework which is not supported yet!", ConsoleViewContentType.ERROR_OUTPUT);
                    }
                } else {
                    toolWindowContent.getConsoleView().print("\n["+file.getName()+"] Not supported programming language!", ConsoleViewContentType.ERROR_OUTPUT);
                }
            });
            threads.add(t);
            t.start();
        }
        toolWindowContent.setRerunToolWindowContentAction(toolWindowContent1 -> {
            List<PsiElement> elements1 = new ArrayList<>();
            List<Thread> threads1 = new ArrayList<>();
            for (PsiFile file : files) {

                Thread t = new Thread(() -> {
                    try {
                        Thread.sleep(1000);
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                    ProgrammingLanguage programmingLanguage = ProgrammingLanguageResolver.resolveProgrammingLanguage(file);
                    if (programmingLanguage != null) {
                        toolWindowContent1.getConsoleView().print("\n[" + file.getName() + "] Programming language " + programmingLanguage + " detected", ConsoleViewContentType.LOG_INFO_OUTPUT);

                        UnitTestFramework unitTestFramework = ApplicationManager.getApplication().runReadAction((Computable<UnitTestFramework>) () -> UnitTestFrameworkResolver.resolveUnitTestFramework(programmingLanguage, file));
                        if (unitTestFramework != null) {
                            toolWindowContent1.getConsoleView().print("\n[" + file.getName() + "] Tests from unit testing framework " + unitTestFramework + " detected", ConsoleViewContentType.LOG_INFO_OUTPUT);
                            toolWindowContent1.getConsoleView().print("\n[" + file.getName() + "] Initializing inspection.... This might take a while...", ConsoleViewContentType.LOG_INFO_OUTPUT);
                            elements1.addAll(ApplicationManager.getApplication().runReadAction((Computable<List<PsiElement>>) () -> {
                                List<PsiElement> resultElements = new ArrayList<>();
                                PsiClass psiClass = (PsiClass) Arrays.stream(file.getChildren()).filter(child -> child instanceof PsiClass).findFirst().orElse(null);
                                if (psiClass != null) {
                                    resultElements.addAll(Arrays.stream(psiClass.getChildren()).filter(child -> child instanceof PsiMethod).collect(Collectors.toList()));
                                }
                                return resultElements;
                            }));
                        } else {
                            toolWindowContent1.getConsoleView().print("\n[" + file.getName() + "] Not a test class or test are written in a unit testing framework which is not supported yet!", ConsoleViewContentType.ERROR_OUTPUT);
                        }
                    } else {
                        toolWindowContent1.getConsoleView().print("\n[" + file.getName() + "] Not supported programming language!", ConsoleViewContentType.ERROR_OUTPUT);
                    }
                });
                threads1.add(t);
                t.start();
            }
                for (Thread thread : threads1) {
                    try {
                        thread.join();
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                }
                if (!elements1.isEmpty()) {
                    Runnable runInspection = new RunInspectionTest(toolWindowContent1, elements1);
                    new Thread(runInspection).start();
                } else {
                    toolWindowContent1.getConsoleView().print("\nNo testing methods present!", ConsoleViewContentType.ERROR_OUTPUT);
                }

        });
        for (Thread thread : threads) {
            try {
                thread.join();
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
        if (!elements.isEmpty()) {
            Runnable runInspection = new RunInspectionTest(toolWindowContent, elements);
            new Thread(runInspection).start();
        } else {
            toolWindowContent.getConsoleView().print("\nNo testing methods present!", ConsoleViewContentType.ERROR_OUTPUT);
        }

    }

    public static void initializeTestspector(PsiElement element, ProgrammingLanguage programmingLanguage, UnitTestFramework unitTestFramework) {
        Project project = element.getProject();
        ToolWindow toolWindow = getToolWindow(project);
        ContentFactory contentFactory = ContentFactory.SERVICE.getInstance();
        ToolWindowContent toolWindowContent = new ToolWindowContent(project);
        toolWindowContent.setRerunToolWindowContentAction((content) -> {
            Runnable runInspection = new RunInspectionTest(content, Collections.singletonList(element));
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
        Runnable runInspection = new RunInspectionTest(toolWindowContent, Collections.singletonList(element));
        new Thread(runInspection).start();

    }


    private static ToolWindow getToolWindow(Project project) {
        ToolWindow resultToolWindow = PROJECT_TOOL_WINDOW_HASH_MAP.get(project);
        if (resultToolWindow == null) {
            resultToolWindow = ToolWindowManager.getInstance(project)
                    .registerToolWindow("Testspector", true, ToolWindowAnchor.BOTTOM);
            resultToolWindow.setIcon(IconLoader.getIcon("/icons/logo.svg"));
            PROJECT_TOOL_WINDOW_HASH_MAP.put(project, resultToolWindow);
        }
        return resultToolWindow;
    }

    public static class RunInspectionTest implements Runnable {

        private final ToolWindowContent toolWindowContent;
        private final List<PsiElement> psiElements;

        public RunInspectionTest(ToolWindowContent toolWindowContent, List<PsiElement> psiElements) {
            this.psiElements = psiElements;
            this.toolWindowContent = toolWindowContent;
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
