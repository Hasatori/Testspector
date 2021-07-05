package com.testspector.view.inspection.creatingtests;

import com.testspector.view.inspection.BestPracticeInspection;
import org.apache.commons.lang.ArrayUtils;
import org.jetbrains.annotations.Nls;

public abstract class CreatingTestsInspection extends BestPracticeInspection {

    @Nls(capitalization = Nls.Capitalization.Sentence)
    @Override
    public String[] getGroupPath() {
        return (String[]) ArrayUtils.addAll(super.getGroupPath(),new String[]{"Creating tests"});
    }
}
