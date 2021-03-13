package com.testspector.model.checking;

import com.testspector.model.checking.crate.BestPracticeViolation;
import com.testspector.model.enums.BestPractice;

import java.util.List;

public interface  BestPracticeCheckingStrategy<T> {

  List<BestPracticeViolation> checkBestPractices(T checkedElement);

  List<BestPracticeViolation> checkBestPractices(List<T> checkedElements);

  List<BestPractice> getCheckedBestPractice();

}
