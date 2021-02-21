package com.testspector.model.checking;

import com.intellij.psi.PsiElement;
import com.testspector.model.enums.ProgrammingLanguage;
import com.testspector.model.enums.UnitTestFramework;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

public class UnitTestFrameworkFactory {

    private final Map<ProgrammingLanguage, List<UnitTestFrameworkResolveIndicationStrategy>> unitTestResolveIndicationStrategiesMap;

    public UnitTestFrameworkFactory(Map<ProgrammingLanguage, List<UnitTestFrameworkResolveIndicationStrategy>> unitTestResolveIndicationStrategiesMap) {
        this.unitTestResolveIndicationStrategiesMap = unitTestResolveIndicationStrategiesMap;
    }


    public List<UnitTestFramework> getUnitTestFrameworks(ProgrammingLanguage programmingLanguage, PsiElement psiElement) {
        List<UnitTestFrameworkResolveIndicationStrategy> unitTestFrameworkResolveIndicationStrategies = this.unitTestResolveIndicationStrategiesMap.get(programmingLanguage);
        if (unitTestFrameworkResolveIndicationStrategies != null) {
            return unitTestFrameworkResolveIndicationStrategies.stream()
                    .filter(unitTestFrameworkResolveIndicationStrategy -> unitTestFrameworkResolveIndicationStrategy.canResolveFromPsiElement(psiElement))
                    .map(UnitTestFrameworkResolveIndicationStrategy::getUnitTestFramework)
                    .collect(Collectors.toList());
        }
        return new ArrayList<>();
    }

}
