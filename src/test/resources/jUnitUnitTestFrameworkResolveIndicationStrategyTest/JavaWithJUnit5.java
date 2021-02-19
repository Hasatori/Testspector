package com.testspector.model.checking;

import com.intellij.lang.Language;
import com.intellij.lang.java.JavaLanguage;
import com.intellij.lang.javascript.dialects.ECMA6LanguageDialect;
import com.intellij.lang.javascript.dialects.TypeScriptLanguageDialect;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiFile;
import com.jetbrains.php.lang.PhpLanguage;
import com.testspector.model.enums.ProgrammingLanguage;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.junit.platform.runner.JUnitPlatform;
import org.junit.runner.RunWith;

import java.util.Optional;
import java.util.stream.Stream;

import static org.easymock.EasyMock.*;


@RunWith(JUnitPlatform.class)
public class ProgrammingLanguageFactoryTest {

    @DisplayName("Programming language:{0} | test data file:{1}")
    @org.junit.jupiter.params.ParameterizedTest
    @MethodSource(value = {"provideSupportedProgrammingLanguageAndCorrespondingFileCombinations"})
    public void resolveProgrammingLanguage_FilesWithSupportedProgrammingLanguages_ShouldReturnExpectedLanguage(Language fileLanguage, ProgrammingLanguage expectedLanguage) {
        PsiElement psiElement = mock(PsiElement.class);
        PsiFile languageFile = mock(PsiFile.class);
        expect(psiElement.getContainingFile()).andReturn(languageFile).times(1);
        expect(languageFile.getLanguage()).andReturn(fileLanguage).times(1);
        replay(psiElement, languageFile);
        ProgrammingLanguageFactory programmingLanguageFactory = new ProgrammingLanguageFactory();

        ProgrammingLanguage returnedProgrammingLanguage = programmingLanguageFactory.resolveProgrammingLanguage(psiElement).get();

        Assertions.assertSame(expectedLanguage, returnedProgrammingLanguage, "Invalid programming language returned!");

    }

    private static Stream<Arguments> provideSupportedProgrammingLanguageAndCorrespondingFileCombinations() {
        return Stream.of(
                // Expected language | file name
                Arguments.of(JavaLanguage.INSTANCE, ProgrammingLanguage.JAVA),
                Arguments.of(PhpLanguage.INSTANCE, ProgrammingLanguage.PHP),
                Arguments.of(new TypeScriptLanguageDialect(), ProgrammingLanguage.TYPESCRIPT)
        );
    }

    @Test
    public void resolveProgrammingLanguage_NullElement_ShouldNoReturnLanguage() {
        ProgrammingLanguageFactory programmingLanguageFactory = new ProgrammingLanguageFactory();

        Optional<ProgrammingLanguage> optionalProgrammingLanguage = programmingLanguageFactory.resolveProgrammingLanguage(null);

        Assertions.assertFalse(optionalProgrammingLanguage.isPresent(), "No programming language should be returned!");

    }

    @Test
    public void resolveProgrammingLanguage_NotSupportedLanguage_ShouldNoReturnLanguage() {
        PsiElement psiElement = mock(PsiElement.class);
        PsiFile languageFile = mock(PsiFile.class);
        expect(psiElement.getContainingFile()).andReturn(languageFile).times(1);
        expect(languageFile.getLanguage()).andReturn(new ECMA6LanguageDialect()).times(1);
        replay(psiElement, languageFile);
        ProgrammingLanguageFactory programmingLanguageFactory = new ProgrammingLanguageFactory();

        Optional<ProgrammingLanguage> optionalProgrammingLanguage = programmingLanguageFactory.resolveProgrammingLanguage(psiElement);

        Assertions.assertFalse(optionalProgrammingLanguage.isPresent(), "No programming language should be returned!");

    }
}
