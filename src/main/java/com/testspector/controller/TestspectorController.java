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
import com.testspector.model.TestLineCrate;
import com.testspector.model.checking.*;
import com.testspector.model.checking.java.junit.JUnitUnitTestFrameworkResolveIndicationStrategy;
import com.testspector.model.checking.java.junit.JUnitInspectionInvocationLineResolveStrategy;
import com.testspector.model.enums.BestPractice;
import com.testspector.model.enums.ProgrammingLanguage;
import com.testspector.model.enums.UnitTestFramework;
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
import java.util.stream.Collectors;

import static com.testspector.model.enums.ProgrammingLanguage.JAVA;
import static com.testspector.model.utils.Constants.WEB_PAGE_BEST_PRACTICES_ULR;

public final class TestspectorController {

    private static final Map<ProgrammingLanguage, List<InspectionInvocationLineResolveStrategy>> PROGRAMMING_LANGUAGE_TEST_LINE_RESOLVE_STRATEGY_HASH_MAP = Collections.unmodifiableMap(new HashMap<ProgrammingLanguage, List<InspectionInvocationLineResolveStrategy>>() {{
        put(JAVA, Arrays.asList(new JUnitInspectionInvocationLineResolveStrategy()));
    }});
    private static final Map<ProgrammingLanguage, List<UnitTestFrameworkResolveIndicationStrategy>> PROGRAMMING_LANGUAGE_UNIT_TEST_FRAMEWORK_RESOLVE_INDICATION_STRATEGY_HASH_MAP = Collections.unmodifiableMap(new HashMap<ProgrammingLanguage, List<UnitTestFrameworkResolveIndicationStrategy>>() {{
        put(JAVA, Arrays.asList(new JUnitUnitTestFrameworkResolveIndicationStrategy()));
    }});
   private static final SimpleDateFormat LOG_MESSAGE_FORMAT = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss.SSS");

    private static final UnitTestFrameworkFactory UNIT_TEST_FRAMEWORK_RESOLVE_STRATEGY_FACTORY = new UnitTestFrameworkFactory(PROGRAMMING_LANGUAGE_UNIT_TEST_FRAMEWORK_RESOLVE_INDICATION_STRATEGY_HASH_MAP);
    private static final ProgrammingLanguageFactory PROGRAMMING_LANGUAGE_FACTORY = new ProgrammingLanguageFactory();
    private static final BestPracticeCheckingStrategyFactory BEST_PRACTICE_CHECKING_STRATEGY_FACTORY = new BestPracticeCheckingStrategyFactory();

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
                    toolWindowContent.getConsoleView().print(getLoggingFormatMessage("Rerunning test inspection on " + name), ConsoleViewContentType.SYSTEM_OUTPUT);
                    List<BestPracticeViolation> bestPracticeViolations = ApplicationManager
                            .getApplication()
                            .runReadAction((Computable<List<BestPracticeViolation>>) () -> gatherBestPracticeViolations(toolWindowContent.getConsoleView(), files));
                    toolWindowContent.showReport(bestPracticeViolations);
                }, executorService::shutdownNow);
                toolWindowContent.getConsoleView().print(getLoggingFormatMessage("Inspecting tests in "), ConsoleViewContentType.SYSTEM_OUTPUT);
                toolWindowContent.getConsoleView().print(name, ConsoleViewContentType.LOG_INFO_OUTPUT);
                List<BestPracticeViolation> bestPracticeViolations = gatherBestPracticeViolations(toolWindowContent.getConsoleView(), files);
                toolWindowContent.getConsoleView().print(getLoggingFormatMessage("Inspection done"), ConsoleViewContentType.LOG_INFO_OUTPUT);
                toolWindowContent.showReport(bestPracticeViolations);

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
                    toolWindowContent.getConsoleView().print(getLoggingFormatMessage("Rerunning test inspection on " + name), ConsoleViewContentType.SYSTEM_OUTPUT);
                    Optional<BestPracticeCheckingStrategy<PsiElement>> optionalBestPracticeCheckingStrategy = BEST_PRACTICE_CHECKING_STRATEGY_FACTORY.getBestPracticeCheckingStrategy(programmingLanguage, unitTestFramework);
                    if (optionalBestPracticeCheckingStrategy.isPresent()) {
                        List<BestPracticeViolation> bestPracticeViolations = optionalBestPracticeCheckingStrategy.get().checkBestPractices(element);
                        toolWindowContent.showReport(bestPracticeViolations);
                    } else {
                        logNoBestPracticeCheckingStrategyFound(toolWindowContent.getConsoleView(), programmingLanguage, unitTestFramework);
                    }
                }, executorService::shutdownNow);

                toolWindowContent.getConsoleView().print(getLoggingFormatMessage("Inspecting tests in "), ConsoleViewContentType.SYSTEM_OUTPUT);
                toolWindowContent.getConsoleView().print(name, ConsoleViewContentType.LOG_INFO_OUTPUT);
                toolWindowContent.getConsoleView().print(getLoggingFormatMessage("Programming language: "), ConsoleViewContentType.SYSTEM_OUTPUT);
                toolWindowContent.getConsoleView().print(programmingLanguage.getDisplayName(), ConsoleViewContentType.LOG_INFO_OUTPUT);
                toolWindowContent.getConsoleView().print(" and unit testing framework: ", ConsoleViewContentType.SYSTEM_OUTPUT);
                toolWindowContent.getConsoleView().print(unitTestFramework.getDisplayName(), ConsoleViewContentType.LOG_INFO_OUTPUT);
                toolWindowContent.getConsoleView().print(" were detected", ConsoleViewContentType.SYSTEM_OUTPUT);
                toolWindowContent.getConsoleView().print(getLoggingFormatMessage("Initializing inspection.... This might take a while..."), ConsoleViewContentType.SYSTEM_OUTPUT);
                Optional<BestPracticeCheckingStrategy<PsiElement>> optionalBestPracticeCheckingStrategy = BEST_PRACTICE_CHECKING_STRATEGY_FACTORY.getBestPracticeCheckingStrategy(programmingLanguage, unitTestFramework);
                List<BestPracticeViolation> foundViolations = new ArrayList<>();
                if (optionalBestPracticeCheckingStrategy.isPresent()) {
                    toolWindowContent.getConsoleView().print(getLoggingFormatMessage(String.format("checking for following best practices: %s", optionalBestPracticeCheckingStrategy
                            .get()
                            .getCheckedBestPractice()
                            .stream()
                            .map(BestPractice::getDisplayName)
                            .collect(Collectors.joining(",","[","]")))), ConsoleViewContentType.SYSTEM_OUTPUT);
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
            List<UnitTestFramework> unitTestFrameworks = UNIT_TEST_FRAMEWORK_RESOLVE_STRATEGY_FACTORY.getUnitTestFrameworks(optionalProgrammingLanguage.get(), element);
            if (!unitTestFrameworks.isEmpty()) {
                UnitTestFramework unitTestFramework = unitTestFrameworks.get(0);
                Optional<InspectionInvocationLineResolveStrategy> optionalUnitTestLineResolveStrategy = selectTestLineResolveStrategy(optionalProgrammingLanguage.get(), unitTestFramework);
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
                List<UnitTestFramework> unitTestFrameworks = UNIT_TEST_FRAMEWORK_RESOLVE_STRATEGY_FACTORY.getUnitTestFrameworks(optionalProgrammingLanguage.get(), file);
                consoleView.print(getLoggingFormatMessage("Programming language: "), ConsoleViewContentType.SYSTEM_OUTPUT);
                consoleView.print(optionalProgrammingLanguage.get().getDisplayName(), ConsoleViewContentType.LOG_INFO_OUTPUT);
                consoleView.print(" detected", ConsoleViewContentType.SYSTEM_OUTPUT);
                if (unitTestFrameworks != null && unitTestFrameworks.size() > 0) {
                    consoleView.print(getLoggingFormatMessage("Unit test frameworks: "), ConsoleViewContentType.SYSTEM_OUTPUT);
                    consoleView.print(unitTestFrameworks.stream().map(UnitTestFramework::getDisplayName).collect(Collectors.joining(", ", "[", "]")), ConsoleViewContentType.LOG_INFO_OUTPUT);
                    consoleView.print(" detected", ConsoleViewContentType.SYSTEM_OUTPUT);
                    for (UnitTestFramework unitTestFramework : unitTestFrameworks) {
                        Optional<BestPracticeCheckingStrategy<PsiElement>> optionalBestPracticeCheckingStrategy = BEST_PRACTICE_CHECKING_STRATEGY_FACTORY.getBestPracticeCheckingStrategy(optionalProgrammingLanguage.get(), unitTestFramework);
                        if (optionalBestPracticeCheckingStrategy.isPresent()) {
                            List<BestPracticeViolation> foundViolations = optionalBestPracticeCheckingStrategy.get().checkBestPractices(file);
                            consoleView.print(getLoggingFormatMessage(String.format("checking for following best practices: %s", optionalBestPracticeCheckingStrategy
                                    .get()
                                    .getCheckedBestPractice()
                                    .stream()
                                    .map(BestPractice::getDisplayName)
                                    .collect(Collectors.joining(",","[","]")))), ConsoleViewContentType.SYSTEM_OUTPUT);
                            bestPracticeViolations.addAll(foundViolations);
                        } else {
                            logNoBestPracticeCheckingStrategyFound(consoleView, optionalProgrammingLanguage.get(), unitTestFramework);
                        }
                    }
                } else {
                    consoleView.print(getLoggingFormatMessage(String.format("No known unit test framework detected. Currently supported unit test frameworks: %s", BEST_PRACTICE_CHECKING_STRATEGY_FACTORY.getSupportedUnitTestFrameworks()
                            .stream()
                            .map(UnitTestFramework::getDisplayName)
                            .collect(Collectors.toList()))), ConsoleViewContentType.LOG_INFO_OUTPUT);
                }
            } else {
                consoleView.print(getLoggingFormatMessage(String.format("No known programming language detected. Currently supported programming languages are: %s", BEST_PRACTICE_CHECKING_STRATEGY_FACTORY.getSupportedLanguages()
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
                                BEST_PRACTICE_CHECKING_STRATEGY_FACTORY.getSupportedLanguages(),
                                BEST_PRACTICE_CHECKING_STRATEGY_FACTORY.getSupportedUnitTestFrameworks()
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
            resultToolWindow.setIcon(IconLoader.getIcon("/icons/logo.svg"));
            PROJECT_TOOL_WINDOW_HASH_MAP.put(project, resultToolWindow);
        }
        return resultToolWindow;
    }


    private static Optional<InspectionInvocationLineResolveStrategy> selectTestLineResolveStrategy(ProgrammingLanguage programmingLanguage, UnitTestFramework unitTestFramework) {
        List<InspectionInvocationLineResolveStrategy> unitTestLineResolveStrategies = PROGRAMMING_LANGUAGE_TEST_LINE_RESOLVE_STRATEGY_HASH_MAP.get(programmingLanguage);
        if (unitTestLineResolveStrategies != null) {
            return unitTestLineResolveStrategies
                    .stream()
                    .filter(inspectionInvocationLineResolveStrategy -> inspectionInvocationLineResolveStrategy.getUnitTestFramework() == unitTestFramework)
                    .findFirst();
        }
        return Optional.empty();
    }

    private static String getLoggingFormatMessage(String message){
        return String.format("\n[ %s ] %s",LOG_MESSAGE_FORMAT.format(new Date()),message);
    }
}
