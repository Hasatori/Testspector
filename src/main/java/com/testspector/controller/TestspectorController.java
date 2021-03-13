package com.testspector.controller;

import com.intellij.execution.ui.ConsoleView;
import com.intellij.execution.ui.ConsoleViewContentType;
import com.intellij.openapi.application.ApplicationManager;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.util.Computable;
import com.intellij.openapi.wm.ToolWindow;
import com.intellij.openapi.wm.ToolWindowAnchor;
import com.intellij.openapi.wm.ToolWindowManager;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiFile;
import com.intellij.ui.content.Content;
import com.intellij.ui.content.ContentFactory;
import com.testspector.model.checking.BestPracticeCheckingStrategy;
import com.testspector.model.checking.InspectionInvocationLineResolveStrategy;
import com.testspector.model.checking.BestPracticeViolation;
import com.testspector.model.checking.TestLineCrate;
import com.testspector.model.checking.factory.*;
import com.testspector.model.enums.BestPractice;
import com.testspector.model.enums.ProgrammingLanguage;
import com.testspector.model.enums.UnitTestFramework;
import com.testspector.view.CustomIcon;
import com.testspector.view.ToolWindowContent;

import java.awt.*;
import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.text.SimpleDateFormat;
import java.util.List;
import java.util.*;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.atomic.AtomicReference;
import java.util.stream.Collectors;

import static com.testspector.model.utils.Constants.WEB_PAGE_BEST_PRACTICES_ULR;

public final class TestspectorController {

    private static final InspectionInvocationLineResolveStrategyFactory INSPECTION_INVOCATION_LINE_RESOLVE_STRATEGY_FACTORY = new InspectionInvocationLineResolveStrategyFactory();
    private static final UnitTestFrameworkFactoryProvider UNIT_TEST_FRAMEWORK_FACTORY_PROVIDER = new UnitTestFrameworkFactoryProvider();
    private static final BestPracticeCheckingStrategyFactoryProvider BEST_PRACTICE_CHECKING_STRATEGY_PROVIDER = new BestPracticeCheckingStrategyFactoryProvider();
    private static final SimpleDateFormat LOG_MESSAGE_FORMAT = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss.SSS");
    private static final ProgrammingLanguageFactory PROGRAMMING_LANGUAGE_FACTORY = new ProgrammingLanguageFactory();

    private static final String TOOL_WINDOW_NAME = "Testspector";
    private static final HashMap<Project, ToolWindow> PROJECT_TOOL_WINDOW_HASH_MAP = new HashMap<>();

    private TestspectorController() {
    }

    public static void initializeTestspector(Project project, PsiFile file) {
        initializeTestspector(project, Collections.singletonList(file), file.getName());
    }

    public static void initializeTestspector(Project project, List<PsiFile> files, String name) {
        AtomicReference<ScheduledExecutorService> executorService = new AtomicReference<>(Executors.newSingleThreadScheduledExecutor());
        ToolWindow toolWindow = getToolWindow(project);
        ToolWindowContent toolWindowContent = new ToolWindowContent(project);
        addTabToToolWindow(toolWindow, toolWindowContent, name);
        executorService.get().submit(() -> {
            ApplicationManager.getApplication().runReadAction(() -> {
                toolWindowContent.start(newToolWindowContent -> {
                    executorService.set(Executors.newSingleThreadScheduledExecutor());
                    ApplicationManager.getApplication().runReadAction(() -> {
                        executorService.get().submit(() -> {
                            toolWindowContent.getConsoleView().print(getLoggingFormatMessage("Rerunning test inspection on " + name), ConsoleViewContentType.SYSTEM_OUTPUT);
                            List<BestPracticeViolation> bestPracticeViolations = ApplicationManager
                                    .getApplication()
                                    .runReadAction((Computable<List<BestPracticeViolation>>) () -> gatherBestPracticeViolations(toolWindowContent.getConsoleView(), files));
                            toolWindowContent.showReport(bestPracticeViolations);
                        });
                    });
                }, executorService.get()::shutdownNow);
                toolWindowContent.getConsoleView().print(getLoggingFormatMessage("Inspecting tests in "), ConsoleViewContentType.SYSTEM_OUTPUT);
                toolWindowContent.getConsoleView().print(name, ConsoleViewContentType.LOG_INFO_OUTPUT);
                List<BestPracticeViolation> bestPracticeViolations = gatherBestPracticeViolations(toolWindowContent.getConsoleView(), files);
                toolWindowContent.getConsoleView().print(getLoggingFormatMessage("Inspection done"), ConsoleViewContentType.LOG_INFO_OUTPUT);
                toolWindowContent.showReport(bestPracticeViolations);
            });
        });
    }


    public static void initializeTestspector(PsiElement element, ProgrammingLanguage programmingLanguage, UnitTestFramework unitTestFramework) {
        String name = element.toString();
        AtomicReference<ScheduledExecutorService> executorService = new AtomicReference<>(Executors.newSingleThreadScheduledExecutor());
        ToolWindow toolWindow = getToolWindow(element.getProject());
        ToolWindowContent toolWindowContent = new ToolWindowContent(element.getProject());
        addTabToToolWindow(toolWindow, toolWindowContent, name);

        executorService.get().submit(() -> {
            ApplicationManager.getApplication().runReadAction(() -> {
                toolWindowContent.start(newToolWindowContent -> {
                    executorService.set(Executors.newSingleThreadScheduledExecutor());
                    ApplicationManager.getApplication().runReadAction(() -> {
                        executorService.get().submit(() -> {
                            toolWindowContent.getConsoleView().print(getLoggingFormatMessage("Rerunning test inspection on " + name), ConsoleViewContentType.SYSTEM_OUTPUT);
                            Optional<BestPracticeCheckingStrategy<PsiElement>> optionalBestPracticeCheckingStrategy = BEST_PRACTICE_CHECKING_STRATEGY_PROVIDER
                                    .getBestPracticeCheckingStrategyFactory(programmingLanguage, unitTestFramework)
                                    .map(BestPracticeCheckingStrategyFactory::getBestPracticeCheckingStrategy);
                            if (optionalBestPracticeCheckingStrategy.isPresent()) {
                                List<BestPracticeViolation> bestPracticeViolations = optionalBestPracticeCheckingStrategy.get().checkBestPractices(element);
                                toolWindowContent.showReport(bestPracticeViolations);
                            } else {
                                logNoBestPracticeCheckingStrategyFound(toolWindowContent.getConsoleView(), programmingLanguage, unitTestFramework);
                            }
                        });
                    });
                }, executorService.get()::shutdownNow);

                toolWindowContent.getConsoleView().print(getLoggingFormatMessage("Inspecting tests in "), ConsoleViewContentType.SYSTEM_OUTPUT);
                toolWindowContent.getConsoleView().print(name, ConsoleViewContentType.LOG_INFO_OUTPUT);
                toolWindowContent.getConsoleView().print(getLoggingFormatMessage("Programming language: "), ConsoleViewContentType.SYSTEM_OUTPUT);
                toolWindowContent.getConsoleView().print(programmingLanguage.getDisplayName(), ConsoleViewContentType.LOG_INFO_OUTPUT);
                toolWindowContent.getConsoleView().print(" and unit testing framework: ", ConsoleViewContentType.SYSTEM_OUTPUT);
                toolWindowContent.getConsoleView().print(unitTestFramework.getDisplayName(), ConsoleViewContentType.LOG_INFO_OUTPUT);
                toolWindowContent.getConsoleView().print(" were detected", ConsoleViewContentType.SYSTEM_OUTPUT);
                toolWindowContent.getConsoleView().print(getLoggingFormatMessage("Initializing inspection.... This might take a while..."), ConsoleViewContentType.SYSTEM_OUTPUT);
                Optional<BestPracticeCheckingStrategy<PsiElement>> optionalBestPracticeCheckingStrategy = BEST_PRACTICE_CHECKING_STRATEGY_PROVIDER
                        .getBestPracticeCheckingStrategyFactory(programmingLanguage, unitTestFramework)
                        .map(BestPracticeCheckingStrategyFactory::getBestPracticeCheckingStrategy);
                List<BestPracticeViolation> foundViolations = new ArrayList<>();
                if (optionalBestPracticeCheckingStrategy.isPresent()) {
                    toolWindowContent.getConsoleView().print(getLoggingFormatMessage(String.format("checking for following best practices: %s", optionalBestPracticeCheckingStrategy
                            .get()
                            .getCheckedBestPractice()
                            .stream()
                            .map(BestPractice::getDisplayName)
                            .collect(Collectors.joining(",", "[", "]")))), ConsoleViewContentType.SYSTEM_OUTPUT);
                    foundViolations.addAll(optionalBestPracticeCheckingStrategy.get().checkBestPractices(element));
                    toolWindowContent.showReport(foundViolations);
                } else {
                    logNoBestPracticeCheckingStrategyFound(toolWindowContent.getConsoleView(), programmingLanguage, unitTestFramework);
                }
                toolWindowContent.getConsoleView().print(getLoggingFormatMessage(String.format("%d best practice violations found", foundViolations.size())), ConsoleViewContentType.SYSTEM_OUTPUT);
                toolWindowContent.getConsoleView().print(getLoggingFormatMessage("Inspection done "), ConsoleViewContentType.LOG_INFO_OUTPUT);
            });
        });
    }

    public static Optional<TestLineCrate> resolveTestLineCrate(PsiElement element) {
        Optional<ProgrammingLanguage> optionalProgrammingLanguage = PROGRAMMING_LANGUAGE_FACTORY.resolveProgrammingLanguage(element);
        if (optionalProgrammingLanguage.isPresent()) {
            List<UnitTestFramework> unitTestFrameworks = UNIT_TEST_FRAMEWORK_FACTORY_PROVIDER.geUnitTestFrameworkFactory(optionalProgrammingLanguage.get())
                    .stream()
                    .map(unitTestFrameworkFactory -> unitTestFrameworkFactory.getUnitTestFramework(element))
                    .filter(Optional::isPresent)
                    .map(Optional::get)
                    .collect(Collectors.toList());
            if (!unitTestFrameworks.isEmpty()) {
                UnitTestFramework unitTestFramework = unitTestFrameworks.get(0);
                Optional<InspectionInvocationLineResolveStrategy> optionalUnitTestLineResolveStrategy = INSPECTION_INVOCATION_LINE_RESOLVE_STRATEGY_FACTORY.getInspectionInvocationLineResolveStrategy(unitTestFramework);
                if (optionalUnitTestLineResolveStrategy.isPresent()) {
                    Optional<PsiElement> optionalLineElement = optionalUnitTestLineResolveStrategy.get().resolveInspectionInvocationLine(element);
                    if (optionalLineElement.isPresent()) {
                        return Optional.of(new TestLineCrate(optionalLineElement.get(), optionalProgrammingLanguage.get(), unitTestFramework));
                    }
                }
            }
        }
        return Optional.empty();
    }

    private static List<BestPracticeViolation> gatherBestPracticeViolations(ConsoleView consoleView, List<PsiFile> files) {
        List<BestPracticeViolation> bestPracticeViolations = new ArrayList<>();
        for (PsiFile file : files) {
            consoleView.print(getLoggingFormatMessage("Entering "), ConsoleViewContentType.SYSTEM_OUTPUT);
            consoleView.print(file.getName(), ConsoleViewContentType.LOG_INFO_OUTPUT);
            Optional<ProgrammingLanguage> optionalProgrammingLanguage = PROGRAMMING_LANGUAGE_FACTORY.resolveProgrammingLanguage(file);
            if (optionalProgrammingLanguage.isPresent()) {
                List<UnitTestFramework> unitTestFrameworks = UNIT_TEST_FRAMEWORK_FACTORY_PROVIDER.geUnitTestFrameworkFactory(optionalProgrammingLanguage.get())
                        .stream()
                        .map(unitTestFrameworkFactory -> unitTestFrameworkFactory.getUnitTestFramework(file))
                        .filter(Optional::isPresent)
                        .map(Optional::get)
                        .collect(Collectors.toList());
                consoleView.print(getLoggingFormatMessage("Programming language: "), ConsoleViewContentType.SYSTEM_OUTPUT);
                consoleView.print(optionalProgrammingLanguage.get().getDisplayName(), ConsoleViewContentType.LOG_INFO_OUTPUT);
                consoleView.print(" detected", ConsoleViewContentType.SYSTEM_OUTPUT);
                if (unitTestFrameworks != null && unitTestFrameworks.size() > 0) {
                    consoleView.print(getLoggingFormatMessage("Unit test frameworks: "), ConsoleViewContentType.SYSTEM_OUTPUT);
                    consoleView.print(unitTestFrameworks.stream().map(UnitTestFramework::getDisplayName).collect(Collectors.joining(", ", "[", "]")), ConsoleViewContentType.LOG_INFO_OUTPUT);
                    consoleView.print(" detected", ConsoleViewContentType.SYSTEM_OUTPUT);
                    for (UnitTestFramework unitTestFramework : unitTestFrameworks) {
                        Optional<BestPracticeCheckingStrategy<PsiElement>> optionalBestPracticeCheckingStrategy = BEST_PRACTICE_CHECKING_STRATEGY_PROVIDER
                                .getBestPracticeCheckingStrategyFactory(optionalProgrammingLanguage.get(), unitTestFramework)
                                .map(BestPracticeCheckingStrategyFactory::getBestPracticeCheckingStrategy);
                        if (optionalBestPracticeCheckingStrategy.isPresent()) {
                            List<BestPracticeViolation> foundViolations = optionalBestPracticeCheckingStrategy.get().checkBestPractices(file);
                            consoleView.print(getLoggingFormatMessage(String.format("checking for following best practices: %s", optionalBestPracticeCheckingStrategy
                                    .get()
                                    .getCheckedBestPractice()
                                    .stream()
                                    .map(BestPractice::getDisplayName)
                                    .collect(Collectors.joining(",", "[", "]")))), ConsoleViewContentType.SYSTEM_OUTPUT);
                            bestPracticeViolations.addAll(foundViolations);
                        } else {
                            logNoBestPracticeCheckingStrategyFound(consoleView, optionalProgrammingLanguage.get(), unitTestFramework);
                        }
                    }
                } else {
                    consoleView.print(getLoggingFormatMessage(String.format("No known unit test framework detected. Currently supported unit test frameworks: %s", BEST_PRACTICE_CHECKING_STRATEGY_PROVIDER.getSupportedUnitTestFrameworks()
                            .stream()
                            .map(UnitTestFramework::getDisplayName)
                            .collect(Collectors.toList()))), ConsoleViewContentType.LOG_INFO_OUTPUT);
                }
            } else {
                consoleView.print(getLoggingFormatMessage(String.format("No known programming language detected. Currently supported programming languages are: %s", BEST_PRACTICE_CHECKING_STRATEGY_PROVIDER.getsupportedProgrammingLanguages()
                        .stream()
                        .map(ProgrammingLanguage::getDisplayName)
                        .collect(Collectors.toList()))), ConsoleViewContentType.LOG_INFO_OUTPUT);
            }
        }
        consoleView.print(getLoggingFormatMessage(String.format("%d best practice violations found", bestPracticeViolations.size())), ConsoleViewContentType.SYSTEM_OUTPUT);
        return bestPracticeViolations;
    }

    private static void logNoBestPracticeCheckingStrategyFound(ConsoleView consoleView, ProgrammingLanguage programmingLanguage, UnitTestFramework unitTestFramework) {
        consoleView
                .print(getLoggingFormatMessage(
                        String.format("No best practice checking strategy found for programming language:%s and unit testing framework:%s found. " +
                                        "Currently supported programming languages are:%s, currently supported unit tests frameworks are:%s." +
                                        "It may be soon implemented. Check out ",
                                programmingLanguage,
                                unitTestFramework,
                                BEST_PRACTICE_CHECKING_STRATEGY_PROVIDER.getsupportedProgrammingLanguages(),
                                BEST_PRACTICE_CHECKING_STRATEGY_PROVIDER.getSupportedUnitTestFrameworks()
                        )),
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
        toolWindow.show(() -> {
        });
    }

    private static ToolWindow getToolWindow(Project project) {
        ToolWindow resultToolWindow = PROJECT_TOOL_WINDOW_HASH_MAP.get(project);
        if (resultToolWindow == null) {
            resultToolWindow = ToolWindowManager.getInstance(project)
                    .registerToolWindow(TOOL_WINDOW_NAME, true, ToolWindowAnchor.BOTTOM);
            resultToolWindow.setIcon(CustomIcon.LOGO_BOTTOM_TOOL_BOX.getBasic());
            PROJECT_TOOL_WINDOW_HASH_MAP.put(project, resultToolWindow);
        }
        return resultToolWindow;
    }

    private static String getLoggingFormatMessage(String message) {
        return String.format("\n[ %s ] %s", LOG_MESSAGE_FORMAT.format(new Date()), message);
    }
}
