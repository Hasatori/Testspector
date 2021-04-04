package com.testspector.model.checking.groovy.spock;

import com.intellij.psi.PsiClass;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiReference;
import com.testspector.model.checking.InspectionInvocationLineResolveStrategy;
import com.testspector.model.enums.UnitTestFramework;
import org.jetbrains.plugins.groovy.lang.psi.impl.GroovyFileImpl;
import org.jetbrains.plugins.groovy.lang.psi.impl.statements.typedef.GrClassDefinitionImpl;
import org.jetbrains.plugins.groovy.lang.psi.impl.statements.typedef.members.GrMethodImpl;

import java.util.Arrays;
import java.util.Optional;

public class SpockInspectionInvocationLineResolveStrategy extends InspectionInvocationLineResolveStrategy {
    @Override
    public Optional<PsiElement> resolveInspectionInvocationLine(PsiElement element) {
        Optional<GrClassDefinitionImpl> optionalMethodClass = Optional.empty();
        if (element instanceof GrClassDefinitionImpl) {
            optionalMethodClass = Optional.of((GrClassDefinitionImpl) element);
        } else if (element instanceof GrMethodImpl) {
            optionalMethodClass = Arrays.stream(((GroovyFileImpl) ((GrMethodImpl) element)
                    .getContainingFile()).getClasses())
                    .filter(psiClass -> Arrays.stream(psiClass.getMethods()).anyMatch(method -> method == element))
                    .map(psiClass -> (GrClassDefinitionImpl) psiClass)
                    .findFirst();

        }
        if (optionalMethodClass.isPresent()) {
            PsiClass extendedClass = (PsiClass) Optional.ofNullable(optionalMethodClass.get()
                    .getExtendsClause())
                    .flatMap(extendsClause -> Optional.ofNullable(extendsClause.getChildren()[0]
                            .getReference())
                            .map(PsiReference::resolve))
                    .orElse(null);
            if (extendedClass != null && "spock.lang.Specification".equals(extendedClass.getQualifiedName())) {
                return Optional.of(element);
            }
        }
       return Optional.empty();
    }
}
