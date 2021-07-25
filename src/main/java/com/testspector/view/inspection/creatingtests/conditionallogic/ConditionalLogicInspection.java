package com.testspector.view.inspection.creatingtests.conditionallogic;

import com.testspector.view.inspection.creatingtests.CreatingTestsInspection;
import org.apache.commons.lang.ArrayUtils;
import org.jetbrains.annotations.Nls;
import org.jetbrains.annotations.NotNull;

public abstract class ConditionalLogicInspection extends CreatingTestsInspection {

    @Nls(capitalization = Nls.Capitalization.Sentence)
    @Override
    public String[] getGroupPath() {
        return (String[]) ArrayUtils.addAll(super.getGroupPath(),new String[]{"Conditional logic"});
    }

    @Override
    public @Nls(capitalization = Nls.Capitalization.Sentence)
    @NotNull
    String getGroupDisplayName() {
        return "Conditional logic";
    }
}
