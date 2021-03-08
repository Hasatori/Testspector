package com.testspector.model.checking;

import com.intellij.openapi.project.Project;
import com.intellij.psi.PsiElementFactory;
import com.intellij.psi.PsiFileFactory;
import com.intellij.testFramework.fixtures.BasePlatformTestCase;

public abstract class TestBase {

    private static Fixture fixture =  new Fixture();

    protected static final PsiElementFactory psiElementFactory;
    protected static final PsiFileFactory psiFileFactory;
    static {
        try {
            fixture.init();

        } catch (Exception e) {
            e.printStackTrace();
        }
        psiFileFactory = PsiFileFactory.getInstance(fixture.provideProject());
        psiElementFactory = PsiElementFactory.getInstance(fixture.provideProject());
    }

   private static class Fixture extends BasePlatformTestCase {


        private Project provideProject(){
            return getProject();
        }

       private void init() throws Exception {
            super.setUp();
        }

        private void finish() throws Exception{
            super.tearDown();
        }
    }
}
