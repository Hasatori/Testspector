package com.testspector.model.checking;

import com.intellij.openapi.project.Project;
import com.intellij.psi.PsiElementFactory;
import com.intellij.psi.PsiFileFactory;
import com.intellij.testFramework.fixtures.BasePlatformTestCase;

public abstract class TestBase {

    private static Fixture fixture =  new Fixture();

    protected static PsiElementFactory psiElementFactory;
    protected static PsiFileFactory psiFileFactory;
    static {
        try {
            fixture.init();
            psiFileFactory = PsiFileFactory.getInstance(fixture.provideProject());
            psiElementFactory = PsiElementFactory.getInstance(fixture.provideProject());
        } catch (Exception e) {
            e.printStackTrace();
        }
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
