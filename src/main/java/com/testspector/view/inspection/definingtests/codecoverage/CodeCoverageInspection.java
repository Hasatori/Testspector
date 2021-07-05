package com.testspector.view.inspection.definingtests.codecoverage;

import com.testspector.view.inspection.creatingtests.CreatingTestsInspection;
import com.testspector.view.inspection.definingtests.DefiningTestsInspection;
import org.apache.commons.lang.ArrayUtils;
import org.jetbrains.annotations.Nls;
import org.jetbrains.annotations.NotNull;

public abstract class CodeCoverageInspection extends DefiningTestsInspection {

    @Nls(capitalization = Nls.Capitalization.Sentence)
    @Override
    public String[] getGroupPath() {
        return (String[]) ArrayUtils.addAll(super.getGroupPath(),new String[]{"Code coverage"});
    }

    @Override
    public @Nls(capitalization = Nls.Capitalization.Sentence)
    @NotNull
    String getGroupDisplayName() {
        return "Code coverage";
    }
}
