package com.testspector.model.checking;

import com.intellij.psi.PsiElement;
import com.testspector.model.enums.ProgrammingLanguage;
import com.testspector.model.enums.UnitTestFramework;
import org.junit.Assert;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.junit.platform.runner.JUnitPlatform;
import org.junit.runner.RunWith;

import java.util.Arrays;
import java.util.HashMap;
import java.util.List;

import static org.easymock.EasyMock.*;

@RunWith(JUnitPlatform.class)
public class UnitTestFrameworkFactoryTest {

    @Test
    public void getUnitTestFramework_FactoryHasStrategyForJavaToResolveJUnitFromPsiElement_ShouldReturnJUnit() {
        UnitTestFrameworkResolveIndicationStrategy mockedUnitTestIndicationStrategy = mock(UnitTestFrameworkResolveIndicationStrategy.class);
        PsiElement mockedPsiElement = createNiceMock(PsiElement.class);
        expect(mockedUnitTestIndicationStrategy.canResolveFromPsiElement(anyObject())).andReturn(true).times(1);
        expect(mockedUnitTestIndicationStrategy.getUnitTestFramework()).andReturn(UnitTestFramework.JUNIT);
        replay(mockedUnitTestIndicationStrategy,mockedPsiElement);
        HashMap<ProgrammingLanguage, List<UnitTestFrameworkResolveIndicationStrategy>> programmingLanguageListHashMap = new HashMap<>() {{
            put(ProgrammingLanguage.JAVA, Arrays.asList(mockedUnitTestIndicationStrategy));
        }};
        UnitTestFrameworkFactory unitTestFrameworkFactory = new UnitTestFrameworkFactory(programmingLanguageListHashMap);

        List<UnitTestFramework> returnedUnitTestFrameworks = unitTestFrameworkFactory.getUnitTestFrameworks(ProgrammingLanguage.JAVA, mockedPsiElement);

        Assertions.assertSame(UnitTestFramework.JUNIT, returnedUnitTestFrameworks.get(0));

    }

    @Test
    public void getUnitTestFramework_FactoryHasStrategyForJavaButStrategyCannotResolveFrameworkFromPsiElement_ShouldReturnEmptyList() {
        UnitTestFrameworkResolveIndicationStrategy mockedUnitTestIndicationStrategy = mock(UnitTestFrameworkResolveIndicationStrategy.class);
        PsiElement mockedPsiElement = createNiceMock(PsiElement.class);
        expect(mockedUnitTestIndicationStrategy.canResolveFromPsiElement(anyObject())).andReturn(false).times(1);
        expect(mockedUnitTestIndicationStrategy.getUnitTestFramework()).andReturn(UnitTestFramework.JUNIT);
        replay(mockedUnitTestIndicationStrategy,mockedPsiElement);
        HashMap<ProgrammingLanguage, List<UnitTestFrameworkResolveIndicationStrategy>> programmingLanguageListHashMap = new HashMap<>() {{
            put(ProgrammingLanguage.JAVA, Arrays.asList(mockedUnitTestIndicationStrategy));
        }};
        UnitTestFrameworkFactory unitTestFrameworkFactory = new UnitTestFrameworkFactory(programmingLanguageListHashMap);

        List<UnitTestFramework> returnedUnitTestFrameworks = unitTestFrameworkFactory.getUnitTestFrameworks(ProgrammingLanguage.JAVA, mockedPsiElement);

        Assertions.assertTrue(returnedUnitTestFrameworks.isEmpty());

    }

    @Test
    public void getUnitTestFramework_FactoryDoestNotHaveStrategyToResolveFrameworkForPhp_ShouldReturnEmptyList() {
        UnitTestFrameworkResolveIndicationStrategy mockedUnitTestIndicationStrategy = mock(UnitTestFrameworkResolveIndicationStrategy.class);
        PsiElement mockedPsiElement = createNiceMock(PsiElement.class);
        expect(mockedUnitTestIndicationStrategy.canResolveFromPsiElement(anyObject())).andReturn(true).times(1);
        expect(mockedUnitTestIndicationStrategy.getUnitTestFramework()).andReturn(UnitTestFramework.PHP_UNIT);
        replay(mockedUnitTestIndicationStrategy,mockedPsiElement);
        HashMap<ProgrammingLanguage, List<UnitTestFrameworkResolveIndicationStrategy>> programmingLanguageListHashMap = new HashMap<>() {{
            put(ProgrammingLanguage.PHP, Arrays.asList(mockedUnitTestIndicationStrategy));
        }};
        UnitTestFrameworkFactory unitTestFrameworkFactory = new UnitTestFrameworkFactory(programmingLanguageListHashMap);

        List<UnitTestFramework> returnedUnitTestFrameworks = unitTestFrameworkFactory.getUnitTestFrameworks(ProgrammingLanguage.JAVA, mockedPsiElement);

        Assertions.assertTrue(returnedUnitTestFrameworks.isEmpty());
    }
}
