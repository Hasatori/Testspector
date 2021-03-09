package com.testspector.model.checking.java;

import com.testspector.model.checking.TestBase;
import org.junit.jupiter.api.BeforeEach;

public class JavaTest extends TestBase {

    protected JavaTestElementUtil javaTestElementUtil;

    @BeforeEach
    public void beforeEach() {
        this.javaTestElementUtil = new JavaTestElementUtil(psiFileFactory, psiElementFactory);
    }

}
