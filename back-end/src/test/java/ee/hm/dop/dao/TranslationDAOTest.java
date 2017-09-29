package ee.hm.dop.dao;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;

import java.util.Map;

import javax.inject.Inject;

import ee.hm.dop.common.test.DatabaseTestBase;
import ee.hm.dop.model.Language;
import ee.hm.dop.model.TranslationGroup;
import ee.hm.dop.model.enums.LanguageC;
import org.junit.Test;

public class TranslationDAOTest extends DatabaseTestBase {

    @Inject
    private TranslationDAO translationDAO;

    @Test
    public void getTranslationGroupForEstonian() {
        Language language = new Language();
        language.setId((long) 1);
        language.setCode(LanguageC.EST);

        TranslationGroup translationGroup = translationDAO.findTranslationGroupFor(language);

        assertNotNull(translationGroup);
        assertEquals(language.getCode(), translationGroup.getLanguage().getCode());
        assertEquals(language.getId(), translationGroup.getLanguage().getId());
        Map<String, String> translations = translationGroup.getTranslations();
        assertEquals(3, translations.size());
        assertEquals("FOO sõnum", translations.get("FOO"));
        assertEquals("Eesti keeles", translations.get("Estonian"));
        assertEquals("Vene keel", translations.get("Russian"));
    }

    @Test
    public void getTranslationKeyByTranslation() {
        Language language = new Language();
        language.setId((long) 1);
        language.setCode(LanguageC.EST);

        String translationKey = translationDAO.getTranslationKeyByTranslation("FOO sõnum");
        assertNotNull(translationKey);
        assertEquals("FOO", translationKey);
        String translationKey2 = translationDAO.getTranslationKeyByTranslation("FOO");
        assertNull(translationKey2);
    }

    @Test
    public void getTranslationByKeyAndLangcode() {
        Language language = new Language();
        language.setId((long) 1);
        language.setCode(LanguageC.EST);

        String translation = translationDAO.getTranslationByKeyAndLangcode("FOO", 1L);
        assertNotNull(translation);
        assertEquals("FOO sõnum", translation);
        String translation2 = translationDAO.getTranslationByKeyAndLangcode("P", 1L);
        assertNull(translation2);
    }



    @Test
    public void getTranslationGroupForRussian() {
        Language language = new Language();
        language.setId((long) 2);
        language.setCode(LanguageC.RUS);

        TranslationGroup translationGroup = translationDAO.findTranslationGroupFor(language);

        assertNotNull(translationGroup);
        assertEquals(language.getCode(), translationGroup.getLanguage().getCode());
        assertEquals(language.getId(), translationGroup.getLanguage().getId());
        Map<String, String> translations = translationGroup.getTranslations();
        assertEquals(3, translations.size());
        assertEquals("FOO сообщение", translations.get("FOO"));
        assertEquals("Эстонский язык", translations.get("Estonian"));
        assertEquals("русский язык", translations.get("Russian"));
    }
}
