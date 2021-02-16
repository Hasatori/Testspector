package com.testspector.controller;

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
import com.testspector.model.BestPracticeCheckingStrategyFactory;
import com.testspector.model.BestPracticeViolation;
import com.testspector.model.ProgrammingLanguageFactory;
import com.testspector.model.UnitTestFrameworkFactory;
import com.testspector.model.enums.BestPractice;
import com.testspector.model.enums.ProgrammingLanguage;
import com.testspector.model.enums.UnitTestFramework;
import com.testspector.view.ToolWindowContent;

import java.util.*;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.stream.Collectors;
public class TestspectorController {

    private static final HashMap<Project, ToolWindow> PROJECT_TOOL_WINDOW_HASH_MAP = new HashMap<>();
    private static final UnitTestFrameworkFactory UNIT_TEST_FRAMEWORK_RESOLVE_STRATEGY_FACTORY = new UnitTestFrameworkFactory();
    private static final ProgrammingLanguageFactory PROGRAMMING_LANGUAGE_FACTORY = new ProgrammingLanguageFactory();
    private static final BestPracticeCheckingStrategyFactory BEST_PRACTICE_CHECKING_STRATEGY_FACTORY = new BestPracticeCheckingStrategyFactory();

    private TestspectorController() {
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
        List<Thread> threads = new CopyOnWriteArrayList<>();
        for (PsiFile file : files) {

            Thread t = new Thread(() -> {
                Optional<ProgrammingLanguage> optionalProgrammingLanguage = PROGRAMMING_LANGUAGE_FACTORY.resolveProgrammingLanguage(file);
                if (optionalProgrammingLanguage.isPresent()) {
                    toolWindowContent.getConsoleView().print("\n[" + file.getName() + "] Programming language " + optionalProgrammingLanguage.get() + " detected", ConsoleViewContentType.LOG_INFO_OUTPUT);

                    Optional<UnitTestFramework> optionalUnitTestFramework = ApplicationManager.getApplication().runReadAction((Computable<Optional<UnitTestFramework>>) () -> UNIT_TEST_FRAMEWORK_RESOLVE_STRATEGY_FACTORY.getUnitTestFramework(optionalProgrammingLanguage.get(), file));
                    if (optionalUnitTestFramework.isPresent()) {
                        toolWindowContent.getConsoleView().print("\n[" + file.getName() + "] Tests from unit testing framework " + optionalUnitTestFramework.get() + " detected", ConsoleViewContentType.LOG_INFO_OUTPUT);
                        toolWindowContent.getConsoleView().print("\n[" + file.getName() + "] Initializing inspection.... This might take a while...", ConsoleViewContentType.LOG_INFO_OUTPUT);
                        elements.addAll(ApplicationManager.getApplication().runReadAction((Computable<List<PsiElement>>) () -> {
                            List<PsiElement> resultElements = new ArrayList<>();
                            PsiClass psiClass = (PsiClass) Arrays.stream(file.getChildren()).filter(child -> child instanceof PsiClass).findFirst().orElse(null);
                            if (psiClass != null) {
                                resultElements.addAll(Arrays.stream(psiClass.getChildren()).filter(child -> child instanceof PsiMethod).collect(Collectors.toList()));
                            }
                            return resultElements;
                        }));
                    } else {
                        toolWindowContent.getConsoleView().print("\n[" + file.getName() + "] Not a test class or test are written in a unit testing framework which is not supported yet!", ConsoleViewContentType.ERROR_OUTPUT);
                    }

                } else {
                    toolWindowContent.getConsoleView().print("\n[" + file.getName() + "] Not supported programming language!", ConsoleViewContentType.ERROR_OUTPUT);
                }

            });
            threads.add(t);
            t.start();
        }
        toolWindowContent.start(toolWindowContent1 -> {
            threads.forEach(Thread::interrupt);
            threads.clear();
            List<PsiElement> elements1 = new ArrayList<>();
            for (PsiFile file : files) {

                Thread t = new Thread(() -> {
                    try {
                        Thread.sleep(1000);
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                    Optional<ProgrammingLanguage> optionalProgrammingLanguage = PROGRAMMING_LANGUAGE_FACTORY.resolveProgrammingLanguage(file);
                    if (optionalProgrammingLanguage.isPresent()) {
                        toolWindowContent.getConsoleView().print("\n[" + file.getName() + "] Programming language " + optionalProgrammingLanguage.get() + " detected", ConsoleViewContentType.LOG_INFO_OUTPUT);
                        Optional<UnitTestFramework> optionalUnitTestFramework = ApplicationManager.getApplication().runReadAction((Computable<Optional<UnitTestFramework>>) () -> UNIT_TEST_FRAMEWORK_RESOLVE_STRATEGY_FACTORY.getUnitTestFramework(optionalProgrammingLanguage.get(), file));
                        if (optionalUnitTestFramework.isPresent()) {
                            toolWindowContent.getConsoleView().print("\n[" + file.getName() + "] Tests from unit testing framework " + optionalUnitTestFramework.get() + " detected", ConsoleViewContentType.LOG_INFO_OUTPUT);
                            toolWindowContent.getConsoleView().print("\n[" + file.getName() + "] Initializing inspection.... This might take a while...", ConsoleViewContentType.LOG_INFO_OUTPUT);
                            elements.addAll(ApplicationManager.getApplication().runReadAction((Computable<List<PsiElement>>) () -> {
                                List<PsiElement> resultElements = new ArrayList<>();
                                PsiClass psiClass = (PsiClass) Arrays.stream(file.getChildren()).filter(child -> child instanceof PsiClass).findFirst().orElse(null);
                                if (psiClass != null) {
                                    resultElements.addAll(Arrays.stream(psiClass.getChildren()).filter(child -> child instanceof PsiMethod).collect(Collectors.toList()));
                                }
                                return resultElements;
                            }));
                        } else {
                            toolWindowContent.getConsoleView().print("\n[" + file.getName() + "] Not a test class or test are written in a unit testing framework which is not supported yet!", ConsoleViewContentType.ERROR_OUTPUT);
                        }
                    } else {
                        toolWindowContent.getConsoleView().print("\n[" + file.getName() + "] Not supported programming language!", ConsoleViewContentType.ERROR_OUTPUT);
                    }
                });
                threads.add(t);
                t.start();
            }
            for (Thread thread : threads) {
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

        }, () -> {
            threads.forEach(Thread::interrupt);
            threads.clear();
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
        List<Thread> threads = new CopyOnWriteArrayList<>();
        toolWindowContent.start((content) -> {
            threads.forEach(Thread::interrupt);
            threads.clear();
            Runnable runInspection = new RunInspectionTest(content, Collections.singletonList(element));
            content.getConsoleView().print("\nRerunning inspection on " + element.toString(), ConsoleViewContentType.LOG_INFO_OUTPUT);
            threads.add(new Thread(runInspection));
            threads.forEach(Thread::start);
        }, () -> {
            threads.forEach(Thread::interrupt);
            threads.clear();
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
        Thread thread = new Thread(runInspection);
        threads.add(thread);
        thread.start();
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
