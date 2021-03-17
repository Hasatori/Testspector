package com.testspector.controller;

import com.intellij.openapi.project.Project;
import com.intellij.openapi.wm.ToolWindow;
import com.intellij.openapi.wm.ToolWindowManager;
import com.intellij.psi.PsiElement;
import com.intellij.ui.content.Content;
import com.intellij.ui.content.ContentManager;
import com.testspector.HeavyTestBase;
import com.testspector.model.checking.BestPracticeCheckingStrategy;
import com.testspector.model.checking.BestPracticeViolation;
import com.testspector.model.checking.factory.*;
import com.testspector.model.enums.BestPractice;
import com.testspector.model.enums.ProgrammingLanguage;
import com.testspector.model.enums.UnitTestFramework;
import com.testspector.view.ToolWindowContent;
import com.testspector.view.ToolWindowContentFactory;
import org.easymock.EasyMock;
import org.jetbrains.annotations.NotNull;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.*;
import java.util.concurrent.*;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;

public class TestspectorControllerTest extends HeavyTestBase {

    private Project project;
    private ToolWindowContentFactory toolWindowContentFactory;
    private ToolWindowContent toolWindowContent;
    private BestPracticeCheckingStrategyFactoryProvider bestPracticeCheckingStrategyFactoryProvider;
    private InspectionInvocationLineResolveStrategyFactory inspectionInvocationLineResolveStrategyFactory;
    private ProgrammingLanguageFactory programmingLanguageFactory;
    private UnitTestFrameworkFactoryProvider unitTestFrameworkFactoryProvider;
    private ToolWindowManager toolWindowManager;
    private ToolWindow toolWindow;
    private ContentManager contentManger;
    private static final String TAB_NAME = "TAB_NAME";
    private TestspectorController testspectorController;
    private TestSpectorExecutorServiceFactory testSpectorExecutorFactory;
    private ExecutorServiceSpy executorService;

    @BeforeEach
    public void beforeEach() {
        this.project = EasyMock.mock(Project.class);
        this.testspectorController = new TestspectorController(project);
        this.bestPracticeCheckingStrategyFactoryProvider = EasyMock.mock(BestPracticeCheckingStrategyFactoryProvider.class);
        this.inspectionInvocationLineResolveStrategyFactory = EasyMock.mock(InspectionInvocationLineResolveStrategyFactory.class);
        this.programmingLanguageFactory = EasyMock.mock(ProgrammingLanguageFactory.class);
        this.unitTestFrameworkFactoryProvider = EasyMock.mock(UnitTestFrameworkFactoryProvider.class);
        this.toolWindowContentFactory = EasyMock.mock(ToolWindowContentFactory.class);
        this.toolWindowContent = EasyMock.mock(ToolWindowContent.class);
        this.toolWindowManager = EasyMock.mock(ToolWindowManager.class);
        this.toolWindow = EasyMock.mock(ToolWindow.class);
        this.contentManger = EasyMock.mock(ContentManager.class);
        this.testSpectorExecutorFactory = EasyMock.mock(TestSpectorExecutorServiceFactory.class);
        this.executorService = new ExecutorServiceSpy();
        EasyMock.expect(project.getComponent(ToolWindowContentFactory.class)).andReturn(toolWindowContentFactory);
        EasyMock.expect(project.getComponent(BestPracticeCheckingStrategyFactoryProvider.class)).andReturn(bestPracticeCheckingStrategyFactoryProvider).anyTimes();
        EasyMock.expect(project.getComponent(InspectionInvocationLineResolveStrategyFactory.class)).andReturn(inspectionInvocationLineResolveStrategyFactory).anyTimes();
        EasyMock.expect(project.getComponent(ProgrammingLanguageFactory.class)).andReturn(programmingLanguageFactory).anyTimes();
        EasyMock.expect(project.getComponent(UnitTestFrameworkFactoryProvider.class)).andReturn(unitTestFrameworkFactoryProvider).anyTimes();
        EasyMock.expect(project.getComponent(ToolWindowManager.class)).andReturn(toolWindowManager).anyTimes();
        EasyMock.expect(project.getComponent(TestSpectorExecutorServiceFactory.class)).andReturn(testSpectorExecutorFactory).anyTimes();
        EasyMock.expect(toolWindowContentFactory.getToolWindowContent(EasyMock.anyObject())).andReturn(toolWindowContent).once();
        EasyMock.expect(toolWindowManager.getToolWindow("Testspector")).andReturn(toolWindow).anyTimes();
        EasyMock.expect(toolWindow.getContentManager()).andReturn(contentManger).anyTimes();
        EasyMock.expect(contentManger.getContentCount()).andReturn(0).anyTimes();
        EasyMock.expect(testSpectorExecutorFactory.getTestSpectorExecutorService(10)).andReturn(executorService);
        contentManger.addContent(EasyMock.anyObject(Content.class));
        EasyMock.expectLastCall();
        EasyMock.replay(project, toolWindowContentFactory, toolWindowManager, toolWindow, contentManger, testSpectorExecutorFactory);
    }


    @Test
    public void initializeTestSpector_SomeViolationsFound_ShouldUseFactoriesAndProvidersToGetViolationsAndAddThemToTheToolWindowContent() {
        PsiElement element = EasyMock.mock(PsiElement.class);
        UnitTestFrameworkFactory unitTestFrameworkFactory = EasyMock.mock(UnitTestFrameworkFactory.class);
        BestPracticeCheckingStrategyFactory bestPracticeCheckingStrategyFactory = EasyMock.mock(BestPracticeCheckingStrategyFactory.class);
        BestPracticeCheckingStrategy<PsiElement> bestPracticeCheckingStrategy = EasyMock.mock(BestPracticeCheckingStrategy.class);
        List<BestPracticeViolation> expectedViolations = getSomeViolations();
        EasyMock.expect(programmingLanguageFactory.getProgrammingLanguage(element)).andReturn(Optional.of(ProgrammingLanguage.JAVA)).times(1);
        EasyMock.expect(unitTestFrameworkFactoryProvider.geUnitTestFrameworkFactory(ProgrammingLanguage.JAVA)).andReturn(Collections.singletonList(unitTestFrameworkFactory)).times(1);
        EasyMock.expect(unitTestFrameworkFactory.getUnitTestFramework(element)).andReturn(Optional.of(UnitTestFramework.JUNIT)).times(1);
        EasyMock.expect(bestPracticeCheckingStrategyFactoryProvider.getBestPracticeCheckingStrategyFactory(ProgrammingLanguage.JAVA, UnitTestFramework.JUNIT)).andReturn(Optional.of(bestPracticeCheckingStrategyFactory)).times(1);
        EasyMock.expect(bestPracticeCheckingStrategyFactory.getBestPracticeCheckingStrategy()).andReturn(bestPracticeCheckingStrategy).times(1);
        EasyMock.expect(bestPracticeCheckingStrategy.checkBestPractices(element)).andReturn(expectedViolations).once();
        toolWindowContent.setData(expectedViolations);
        EasyMock.expectLastCall();
        EasyMock.expect(toolWindowContent.getPanel1()).andReturn(null).once();
        EasyMock.replay(toolWindowContent, programmingLanguageFactory, unitTestFrameworkFactory, bestPracticeCheckingStrategyFactoryProvider, unitTestFrameworkFactoryProvider, bestPracticeCheckingStrategy, bestPracticeCheckingStrategyFactory);

        testspectorController.initializeTestspector(element, TAB_NAME);

        assertDoesNotThrow(() -> {
            EasyMock.verify(toolWindowContent);
        });
    }

    private List<BestPracticeViolation> getSomeViolations() {
        List<BestPracticeViolation> bestPracticeViolations = new ArrayList<>();
        bestPracticeViolations.add(new BestPracticeViolation("Name", null, null, "TEST", BestPractice.AT_LEAST_ONE_ASSERTION));
        bestPracticeViolations.add(new BestPracticeViolation("Name2", null, null, "TEST2", BestPractice.AT_LEAST_ONE_ASSERTION));
        return bestPracticeViolations;
    }

    private class ExecutorServiceSpy implements ExecutorService {

        @Override
        public void shutdown() {

        }

        @NotNull
        @Override
        public List<Runnable> shutdownNow() {
            return new ArrayList<>();
        }

        @Override
        public boolean isShutdown() {
            return false;
        }

        @Override
        public boolean isTerminated() {
            return false;
        }

        @Override
        public boolean awaitTermination(long timeout, @NotNull TimeUnit unit) throws InterruptedException {
            return false;
        }

        @NotNull
        @Override
        public <T> Future<T> submit(@NotNull Callable<T> task) {
            return null;
        }

        @NotNull
        @Override
        public <T> Future<T> submit(@NotNull Runnable task, T result) {
            return null;
        }

        @NotNull
        @Override
        public Future<?> submit(@NotNull Runnable task) {
            return null;
        }

        @NotNull
        @Override
        public <T> List<Future<T>> invokeAll(@NotNull Collection<? extends Callable<T>> tasks) {
            List<Future<T>> result = new ArrayList<>();
            for (Callable<T> task : tasks) {
                try {
                    result.add(new Future<T>() {
                        @Override
                        public boolean cancel(boolean mayInterruptIfRunning) {
                            return false;
                        }

                        @Override
                        public boolean isCancelled() {
                            return false;
                        }

                        @Override
                        public boolean isDone() {
                            return false;
                        }

                        @Override
                        public T get() throws InterruptedException, ExecutionException {
                            try {
                                return task.call();
                            } catch (Exception e) {
                                e.printStackTrace();
                            }
                            return null;
                        }

                        @Override
                        public T get(long timeout, @NotNull TimeUnit unit) throws InterruptedException, ExecutionException, TimeoutException {
                            return null;
                        }
                    });
                } catch (Exception e) {
                    e.printStackTrace();
                }

            }
            return result;
        }

        @NotNull
        @Override
        public <T> List<Future<T>> invokeAll(@NotNull Collection<? extends Callable<T>> tasks, long timeout, @NotNull TimeUnit unit) throws InterruptedException {
            return null;
        }

        @NotNull
        @Override
        public <T> T invokeAny(@NotNull Collection<? extends Callable<T>> tasks) throws InterruptedException, ExecutionException {
            return null;
        }

        @Override
        public <T> T invokeAny(@NotNull Collection<? extends Callable<T>> tasks, long timeout, @NotNull TimeUnit unit) throws InterruptedException, ExecutionException, TimeoutException {
            return null;
        }

        @Override
        public void execute(@NotNull Runnable command) {

        }
    }
}
