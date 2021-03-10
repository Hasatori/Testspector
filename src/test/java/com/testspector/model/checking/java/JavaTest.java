package com.testspector.model.checking.java;

import com.testspector.model.checking.TestBase;
import org.junit.jupiter.api.BeforeEach;

public abstract class JavaTest extends TestBase {

    protected JavaTestElementUtil javaTestElementUtil;

    @BeforeEach
    public final  void javaTestSetup() {
        this.javaTestElementUtil = new JavaTestElementUtil(psiFileFactory, psiElementFactory);
    }

}
