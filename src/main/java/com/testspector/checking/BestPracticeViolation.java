package com.testspector.checking;

import com.intellij.openapi.util.TextRange;
import com.intellij.psi.PsiFile;
import com.testspector.enums.BestPractice;

public class BestPracticeViolation {

	private final  PsiFile file;

	private final  TextRange textRange;

	private final  String description;

	private final  BestPractice violatedRule;

	public BestPracticeViolation(PsiFile file, TextRange textRange, String description, BestPractice violatedRule) {
		this.file = file;
		this.textRange = textRange;
		this.description = description;
		this.violatedRule = violatedRule;
	}

	public PsiFile getFile() {
		return file;
	}

	public TextRange getTextRange() {
		return textRange;
	}

	public String getDescription() {
		return description;
	}

	public BestPractice getViolatedRule() {
		return violatedRule;
	}
}
