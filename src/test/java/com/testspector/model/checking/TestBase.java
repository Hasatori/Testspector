package com.testspector.model.checking;

import com.intellij.psi.PsiElementFactory;
import com.intellij.psi.PsiFileFactory;
import com.intellij.testFramework.fixtures.*;
import com.intellij.testFramework.fixtures.impl.LightTempDirTestFixtureImpl;

public abstract class TestBase {

    protected static final PsiElementFactory psiElementFactory;
    protected static final PsiFileFactory psiFileFactory;

    static {
        IdeaTestFixtureFactory factory = IdeaTestFixtureFactory.getFixtureFactory();
        TestFixtureBuilder<IdeaProjectTestFixture> fixtureBuilder = factory.createLightFixtureBuilder(null);
        IdeaProjectTestFixture fixture = fixtureBuilder.getFixture();

        TempDirTestFixture tempDirFixture = createTempDirTestFixture();
        CodeInsightTestFixture myFixture = IdeaTestFixtureFactory.getFixtureFactory().createCodeInsightFixture(fixture, tempDirFixture);

        try {
            myFixture.setUp();
        } catch (Exception e) {
            e.printStackTrace();
        }
        psiFileFactory = PsiFileFactory.getInstance(myFixture.getProject());
        psiElementFactory = PsiElementFactory.getInstance(myFixture.getProject());
    }

    private static TempDirTestFixture createTempDirTestFixture() {
        IdeaTestExecutionPolicy policy = IdeaTestExecutionPolicy.current();
        return policy != null
                ? policy.createTempDirTestFixture()
                : new LightTempDirTestFixtureImpl(true);
    }
}
