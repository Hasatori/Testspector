package com.testspector.controller;

import com.intellij.execution.ui.ConsoleView;
import com.intellij.execution.ui.ConsoleViewContentType;
import com.intellij.openapi.application.ApplicationManager;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.util.Computable;
import com.intellij.openapi.util.IconLoader;
import com.intellij.openapi.wm.ToolWindow;
import com.intellij.openapi.wm.ToolWindowAnchor;
import com.intellij.openapi.wm.ToolWindowManager;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiFile;
import com.intellij.ui.content.Content;
import com.intellij.ui.content.ContentFactory;
import com.testspector.model.checking.*;
import com.testspector.model.checking.java.junit.JUnitUnitTestFrameworkResolveIndicationStrategy;
import com.testspector.model.checking.java.junit.JUnitUnitTestLineResolveStrategy;
import com.testspector.model.enums.ProgrammingLanguage;
import com.testspector.model.enums.UnitTestFramework;
import com.testspector.view.ToolWindowContent;

import java.awt.*;
import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.List;
import java.util.*;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;

import static com.testspector.model.enums.ProgrammingLanguage.JAVA;
import static com.testspector.model.utils.Constants.WEB_PAGE_BEST_PRACTICES_ULR;

public final class TestspectorController {

    public static final Map<ProgrammingLanguage, List<UnitTestLineResolveStrategy>> PROGRAMMING_LANGUAGE_TEST_LINE_RESOLVE_STRATEGY_HASH_MAP = Collections.unmodifiableMap(new HashMap<>(){{
        put(JAVA,Arrays.asList(new JUnitUnitTestLineResolveStrategy()));
    }});
    public static final Map<ProgrammingLanguage, List<UnitTestFrameworkResolveIndicationStrategy>> PROGRAMMING_LANGUAGE_UNIT_TEST_FRAMEWORK_RESOLVE_INDICATION_STRATEGY_HASH_MAP = Collections.unmodifiableMap(new HashMap<>(){{
        put(JAVA,Arrays.asList(new JUnitUnitTestFrameworkResolveIndicationStrategy()));
    }});
    public static final UnitTestFrameworkFactory UNIT_TEST_FRAMEWORK_RESOLVE_STRATEGY_FACTORY = new UnitTestFrameworkFactory(PROGRAMMING_LANGUAGE_UNIT_TEST_FRAMEWORK_RESOLVE_INDICATION_STRATEGY_HASH_MAP);
    public static final ProgrammingLanguageFactory PROGRAMMING_LANGUAGE_FACTORY = new ProgrammingLanguageFactory();
    public static final BestPracticeCheckingStrategyFactory BEST_PRACTICE_CHECKING_STRATEGY_FACTORY = new BestPracticeCheckingStrategyFactory();

    private static final String TOOL_WINDOW_NAME = "Testspector";
    private static final HashMap<Project, ToolWindow> PROJECT_TOOL_WINDOW_HASH_MAP = new HashMap<>();

    private TestspectorController() {
    }

    public static void initializeTestspector(Project project, PsiFile file) {
        initializeTestspector(project, Collections.singletonList(file), file.getName());
    }

    public static void initializeTestspector(Project project, List<PsiFile> files, String name) {
        ScheduledExecutorService executorService = Executors.newSingleThreadScheduledExecutor();
        ToolWindow toolWindow = getToolWindow(project);
        ToolWindowContent toolWindowContent = new ToolWindowContent(project);
        addTabToToolWindow(toolWindow, toolWindowContent, name);

        executorService.submit(() -> {
            ApplicationManager.getApplication().runReadAction(() -> {
                toolWindowContent.start(newToolWindowContent -> {
                    toolWindowContent.getConsoleView().print("\nRerunning test inspection on " + name, ConsoleViewContentType.SYSTEM_OUTPUT);
                    List<BestPracticeViolation> bestPracticeViolations = ApplicationManager
                            .getApplication()
                            .runReadAction((Computable<List<BestPracticeViolation>>) () -> gatherBestPracticeViolations(toolWindowContent.getConsoleView(), files));
                    toolWindowContent.showReport(bestPracticeViolations);
                }, executorService::shutdownNow);
                toolWindowContent.getConsoleView().print("\nInspecting tests in ", ConsoleViewContentType.SYSTEM_OUTPUT);
                toolWindowContent.getConsoleView().print(name, ConsoleViewContentType.LOG_INFO_OUTPUT);
                toolWindowContent.showReport(gatherBestPracticeViolations(toolWindowContent.getConsoleView(), files));
            });
        });
    }


    public static void initializeTestspector(Project project, PsiElement element, ProgrammingLanguage programmingLanguage, UnitTestFramework unitTestFramework) {
        String name = element.toString();
        ScheduledExecutorService executorService = Executors.newSingleThreadScheduledExecutor();
        ToolWindow toolWindow = getToolWindow(project);
        ToolWindowContent toolWindowContent = new ToolWindowContent(project);
        addTabToToolWindow(toolWindow, toolWindowContent, name);

        executorService.submit(() -> {
            ApplicationManager.getApplication().runReadAction(() -> {
                toolWindowContent.start(newToolWindowContent -> {
                    toolWindowContent.getConsoleView().print("\nRerunning test inspection on " + name, ConsoleViewContentType.SYSTEM_OUTPUT);
                    Optional<BestPracticeCheckingStrategy> optionalBestPracticeCheckingStrategy = BEST_PRACTICE_CHECKING_STRATEGY_FACTORY.getBestPracticeCheckingStrategy(programmingLanguage, unitTestFramework);
                    if (optionalBestPracticeCheckingStrategy.isPresent()) {
                        List<BestPracticeViolation> bestPracticeViolations = optionalBestPracticeCheckingStrategy.get().checkBestPractices(element);
                        toolWindowContent.showReport(bestPracticeViolations);
                    } else {
                        logNoBestPracticeCheckingStrategyFound(toolWindowContent.getConsoleView(), programmingLanguage, unitTestFramework);
                    }
                }, executorService::shutdownNow);

                toolWindowContent.getConsoleView().print("\nInspecting tests in ", ConsoleViewContentType.SYSTEM_OUTPUT);
                toolWindowContent.getConsoleView().print(name, ConsoleViewContentType.LOG_INFO_OUTPUT);
                toolWindowContent.getConsoleView().print("\nProgramming language: ", ConsoleViewContentType.SYSTEM_OUTPUT);
                toolWindowContent.getConsoleView().print(programmingLanguage.getDisplayName(), ConsoleViewContentType.LOG_INFO_OUTPUT);
                toolWindowContent.getConsoleView().print(" and unit testing framework: ", ConsoleViewContentType.SYSTEM_OUTPUT);
                toolWindowContent.getConsoleView().print(unitTestFramework.getDisplayName(), ConsoleViewContentType.LOG_INFO_OUTPUT);
                toolWindowContent.getConsoleView().print(" were detected", ConsoleViewContentType.SYSTEM_OUTPUT);
                toolWindowContent.getConsoleView().print("\nInitializing inspection.... This might take a while...", ConsoleViewContentType.SYSTEM_OUTPUT);
                Optional<BestPracticeCheckingStrategy> optionalBestPracticeCheckingStrategy = BEST_PRACTICE_CHECKING_STRATEGY_FACTORY.getBestPracticeCheckingStrategy(programmingLanguage, unitTestFramework);
                if (optionalBestPracticeCheckingStrategy.isPresent()) {
                    toolWindowContent.showReport(optionalBestPracticeCheckingStrategy.get().checkBestPractices(element));
                } else {
                    logNoBestPracticeCheckingStrategyFound(toolWindowContent.getConsoleView(), programmingLanguage, unitTestFramework);
                }
            });
        });
    }

    private static List<BestPracticeViolation> gatherBestPracticeViolations(ConsoleView consoleView, List<PsiFile> files) {
        List<BestPracticeViolation> bestPracticeViolations = new ArrayList<>();
        for (PsiFile file : files) {
            consoleView.print("\nEntering ", ConsoleViewContentType.SYSTEM_OUTPUT);
            consoleView.print(file.getName(), ConsoleViewContentType.LOG_INFO_OUTPUT);
            Optional<ProgrammingLanguage> optionalProgrammingLanguage = PROGRAMMING_LANGUAGE_FACTORY.resolveProgrammingLanguage(file);
            if (optionalProgrammingLanguage.isPresent()) {
                List<UnitTestFramework> unitTestFrameworks = UNIT_TEST_FRAMEWORK_RESOLVE_STRATEGY_FACTORY.getUnitTestFrameworks(optionalProgrammingLanguage.get(), file);
                consoleView.print("\nProgramming language: ", ConsoleViewContentType.SYSTEM_OUTPUT);
                consoleView.print(optionalProgrammingLanguage.get().getDisplayName(), ConsoleViewContentType.LOG_INFO_OUTPUT);
                consoleView.print(" detected", ConsoleViewContentType.SYSTEM_OUTPUT);
                unitTestFrameworks.forEach(unitTestFramework -> {
                    BEST_PRACTICE_CHECKING_STRATEGY_FACTORY.getBestPracticeCheckingStrategy(optionalProgrammingLanguage.get(), unitTestFramework)
                            .ifPresent(bestPracticeCheckingStrategy -> {
                                consoleView.print("\nUnit test framework: ", ConsoleViewContentType.SYSTEM_OUTPUT);
                                consoleView.print(unitTestFramework.getDisplayName(), ConsoleViewContentType.LOG_INFO_OUTPUT);
                                consoleView.print(" detected", ConsoleViewContentType.SYSTEM_OUTPUT);
                                List<BestPracticeViolation> foundViolations = bestPracticeCheckingStrategy.checkBestPractices(file);
                                consoleView.print(String.format("\n%d best practice violations found", foundViolations.size()), ConsoleViewContentType.SYSTEM_OUTPUT);
                                bestPracticeViolations.addAll(foundViolations);
                            });
                });

            }
        }
        return bestPracticeViolations;
    }

    private static void logNoBestPracticeCheckingStrategyFound(ConsoleView consoleView, ProgrammingLanguage programmingLanguage, UnitTestFramework unitTestFramework) {
        consoleView
                .print(
                        String.format("\nNo best practice checking strategy found for programming language:%s and unit testing framework:%s found. It may be soon implemented. Check out ",
                                programmingLanguage,
                                unitTestFramework),
                        ConsoleViewContentType.ERROR_OUTPUT);
        consoleView.printHyperlink(WEB_PAGE_BEST_PRACTICES_ULR, project1 -> {
            try {
                Desktop.getDesktop().browse(new URI(WEB_PAGE_BEST_PRACTICES_ULR));
            } catch (IOException | URISyntaxException e) {
                e.printStackTrace();
            }
        });
        consoleView.print(" to get more information", ConsoleViewContentType.SYSTEM_OUTPUT);
    }

    private static void addTabToToolWindow(ToolWindow toolWindow, ToolWindowContent toolWindowContent, String tabName) {
        ContentFactory contentFactory = ContentFactory.SERVICE.getInstance();
        Content content = contentFactory.createContent(toolWindowContent.getPanel1(), tabName, false);
        toolWindow.getContentManager().addContent(content);
        if (toolWindow.getContentManager().getContentCount() > 1) {
            toolWindow.getContentManager().selectNextContent();
        }
    }

    private static ToolWindow getToolWindow(Project project) {
        ToolWindow resultToolWindow = PROJECT_TOOL_WINDOW_HASH_MAP.get(project);
        if (resultToolWindow == null) {
            resultToolWindow = ToolWindowManager.getInstance(project)
                    .registerToolWindow(TOOL_WINDOW_NAME, true, ToolWindowAnchor.BOTTOM);
            resultToolWindow.setIcon(IconLoader.getIcon("/icons/logo.svg"));
            PROJECT_TOOL_WINDOW_HASH_MAP.put(project, resultToolWindow);
        }
        return resultToolWindow;
    }
}
