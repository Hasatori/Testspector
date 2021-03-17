package com.testspector.model.checking.java;

import com.testspector.HeavyTestBase;
import org.junit.jupiter.api.BeforeEach;

public abstract class JavaTest extends HeavyTestBase {

    protected JavaTestElementUtil javaTestElementUtil;

    @BeforeEach
    public final void javaTestSetup() {
        this.javaTestElementUtil = new JavaTestElementUtil(psiFileFactory, psiElementFactory);
    }

}
