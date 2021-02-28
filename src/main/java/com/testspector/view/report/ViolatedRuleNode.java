package com.testspector.view.report;


import com.intellij.psi.PsiElement;
import com.testspector.model.checking.BestPracticeViolation;
import com.testspector.model.enums.BestPractice;

public class ViolatedRuleNode extends BestPracticeViolationNode {


    private final BestPractice violatedBestPractice;

    public ViolatedRuleNode(PsiElement navigationElement,BestPractice violatedBestPractice) {
        super(navigationElement);
       this.violatedBestPractice = violatedBestPractice;
    }

    public ViolatedRuleNode(BestPractice violatedBestPractice) {
        super();
        this.violatedBestPractice = violatedBestPractice;
    }

    public BestPractice getViolatedBestPractice() {
        return violatedBestPractice;
    }
}
