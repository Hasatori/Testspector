package com.testspector.model.checking.groovy.spock;

import com.intellij.psi.PsiClass;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiReference;
import com.testspector.model.checking.factory.UnitTestFrameworkFactory;
import com.testspector.model.enums.UnitTestFramework;
import org.jetbrains.plugins.groovy.lang.psi.impl.GroovyFileImpl;
import org.jetbrains.plugins.groovy.lang.psi.impl.statements.typedef.GrClassDefinitionImpl;
import org.jetbrains.plugins.groovy.lang.psi.impl.statements.typedef.members.GrMethodImpl;

import java.util.Arrays;
import java.util.Optional;

public class SpockUnitTestFrameworkFactory implements UnitTestFrameworkFactory {

    private static final String SPOCK_TEST_PARENT_QUALIFIED_NAME = "spock.lang.Specification";

    @Override
    public Optional<UnitTestFramework> getUnitTestFramework(PsiElement element) {
        boolean resolved = false;
        if (element instanceof GroovyFileImpl) {
            GroovyFileImpl file = (GroovyFileImpl) element;
            resolved = Arrays.stream(file.getClasses())
                    .anyMatch(groovyClass -> isSpockTestClass((GrClassDefinitionImpl) groovyClass));
        } else if (element instanceof GrClassDefinitionImpl) {
            resolved = isSpockTestClass((GrClassDefinitionImpl) element);
        } else if (element instanceof GrMethodImpl) {
            resolved = getGroovyClass((GrMethodImpl) element)
                    .map(this::isSpockTestClass)
                    .orElse(false);
        }
        if (resolved) {
            return Optional.of(UnitTestFramework.SPOCK);
        }
        return Optional.empty();
    }

    private Optional<GrClassDefinitionImpl> getGroovyClass(GrMethodImpl method) {
        return Arrays.stream(((GroovyFileImpl) (method)
                .getContainingFile()).getClasses())
                .filter(psiClass -> Arrays.stream(psiClass.getMethods()).anyMatch(classMethod -> classMethod == method))
                .map(psiClass -> (GrClassDefinitionImpl) psiClass)
                .findFirst();
    }

    private boolean isSpockTestClass(GrClassDefinitionImpl spockClass) {
        return Optional.ofNullable(spockClass.getExtendsClause())
                .flatMap(extendsClause -> Optional.ofNullable(extendsClause.getChildren()[0]
                        .getReference())
                        .map(PsiReference::resolve))
                .map(element -> (PsiClass) element)
                .map(extendedClass -> SPOCK_TEST_PARENT_QUALIFIED_NAME.equals(extendedClass.getQualifiedName()))
                .orElse(false);
    }
}
