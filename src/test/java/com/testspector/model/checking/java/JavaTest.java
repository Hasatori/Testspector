package com.testspector.model.checking.java;

import com.testspector.TestBase;
import com.testspector.WriteCommandActionTestInterceptor;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.extension.ExtendWith;

public abstract class JavaTest extends TestBase {

    protected JavaTestElementUtil javaTestElementUtil;

    @BeforeEach
    public final void javaTestSetup() {
        this.javaTestElementUtil = new JavaTestElementUtil(psiFileFactory, psiElementFactory);
    }

}
