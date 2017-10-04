package ee.hm.dop.rest;

import ee.hm.dop.common.test.ResourceIntegrationTestBase;
import ee.hm.dop.model.CrossCurricularTheme;
import ee.hm.dop.model.KeyCompetence;
import ee.hm.dop.model.Language;
import ee.hm.dop.model.LicenseType;
import ee.hm.dop.model.ResourceType;
import ee.hm.dop.model.TargetGroup;
import ee.hm.dop.model.enums.EducationalContextC;
import ee.hm.dop.model.enums.TargetGroupEnum;
import ee.hm.dop.model.taxon.Domain;
import ee.hm.dop.model.taxon.EducationalContext;
import ee.hm.dop.model.taxon.Subject;
import ee.hm.dop.model.taxon.Taxon;
import ee.hm.dop.model.taxon.Topic;
import org.junit.Test;

import javax.ws.rs.core.GenericType;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import static org.junit.Assert.*;

public class LearningMaterialMetadataResourceTest extends ResourceIntegrationTestBase {

    private static final String GET_EDUCATIONAL_CONTEXT_URL = "learningMaterialMetadata/educationalContext";
    private static final String GET_TAXON_URL = "learningMaterialMetadata/taxon?taxonId=%s";
    private static final String GET_LANGUAGES_URL = "learningMaterialMetadata/language";
    private static final String GET_TARGET_GROUPS_URL = "learningMaterialMetadata/targetGroup";
    private static final String GET_RESOURCE_TYPES_URL = "learningMaterialMetadata/resourceType";
    private static final String GET_USED_RESOURCE_TYPES_URL = "learningMaterialMetadata/resourceType/used";
    private static final String GET_LICENSE_TYPES_URL = "learningMaterialMetadata/licenseType";
    private static final String GET_CROSS_CURRICULAR_THEMES_URL = "learningMaterialMetadata/crossCurricularTheme";
    private static final String GET_KEY_COMPETENCES_URL = "learningMaterialMetadata/keyCompetence";

    @Test
    public void getEducationalContext() {
        List<EducationalContext> educationalContexts = doGet(GET_EDUCATIONAL_CONTEXT_URL,
                new GenericType<List<EducationalContext>>() {
                });

        assertEquals(9, educationalContexts.stream().distinct().count());

        int domains = 0, subjects = 0;

        for (EducationalContext educationalContext : educationalContexts) {
            if (educationalContext.getName().equals(EducationalContextC.PRESCHOOLEDUCATION)) {
                for (Domain domain : educationalContext.getDomains()) {
                    domains++;
                    if (domain.getName().equals("Mathematics")) {
                        for (Subject subject : domain.getSubjects()) {
                            subjects++;
                            if (subject.getName().equals("Mathematics")) {
                                assertEquals(2, subject.getTopics().size());
                                Topic[] topics = new Topic[2];
                                subject.getTopics().toArray(topics);
                                assertTrue(topics[0].getName().equals("Algebra")
                                        || topics[0].getName().equals("Trigonometria"));
                                assertTrue(topics[1].getName().equals("Algebra")
                                        || topics[1].getName().equals("Trigonometria"));
                            }
                        }
                    }
                }
            }
        }

        assertEquals(2, domains);
        assertEquals(2, subjects);
    }

    @Test
    public void getTaxon() {
        Domain taxon = (Domain) doGet(String.format(GET_TAXON_URL, (Long) 10L), Taxon.class);
        assertEquals((Long) 10L, taxon.getId());
        assertTrue((taxon.getName().equals("Mathematics")));
    }

    @Test
    public void getAllLanguages() {
        List<Language> languages = doGet(GET_LANGUAGES_URL, new GenericType<List<Language>>() {
        });

        assertEquals(6, languages.stream().distinct().count());

        List<String> expectedNames = Arrays.asList("Estonian", "Russian", "English", "Arabic", "Portuguese", "French");
        List<String> actualNames = languages.stream().map(Language::getName).collect(Collectors.toList());
        assertTrue(actualNames.containsAll(expectedNames));
    }

    @Test
    public void getTargetGroups() {
        List<TargetGroup> result = doGet(GET_TARGET_GROUPS_URL, new GenericType<List<TargetGroup>>() {
        });

        assertEquals(12, result.size());

        checkIfAllTargetGroups(result);
    }

    private void checkIfAllTargetGroups(List<TargetGroup> targetGroups) {
        if (targetGroups.size() != TargetGroupEnum.values().length) {
            fail();
        }

        for (TargetGroup targetGroup : targetGroups) {
            TargetGroupEnum.valueOf(targetGroup.getName());
        }

    }

    @Test
    public void getUsedResourceTypes() {
        List<ResourceType> result = doGet(GET_USED_RESOURCE_TYPES_URL, new GenericType<List<ResourceType>>() {
        });

        assertEquals(5, result.size());
    }

    @Test
    public void getResourceTypesGroups() {
        List<ResourceType> result = doGet(GET_RESOURCE_TYPES_URL, new GenericType<List<ResourceType>>() {
        });

        assertEquals(7, result.size());

        List<String> expected = Arrays.asList("TEXTBOOK1", "EXPERIMENT1", "COURSE");
        List<String> actual = result.stream().map(ResourceType::getName).collect(Collectors.toList());

        assertTrue(actual.containsAll(expected));
    }

    @Test
    public void getAllLicenseTypes() {
        List<LicenseType> licenseTypes = doGet(GET_LICENSE_TYPES_URL, new GenericType<List<LicenseType>>() {
        });

        assertEquals(3, licenseTypes.size());
        licenseTypes.forEach(this::assertValidLicenseType);
    }

    @Test
    public void getCrossCurricularThemes() {
        List<CrossCurricularTheme> result = doGet(GET_CROSS_CURRICULAR_THEMES_URL,
                new GenericType<List<CrossCurricularTheme>>() {
                });

        assertEquals(2, result.size());

        List<String> expected = Arrays.asList("Lifelong_learning_and_career_planning",
                "Environment_and_sustainable_development");
        List<String> actual = result.stream().map(CrossCurricularTheme::getName).collect(Collectors.toList());

        assertTrue(actual.containsAll(expected));
    }

    @Test
    public void getKeyCompetences() {
        List<KeyCompetence> result = doGet(GET_KEY_COMPETENCES_URL, new GenericType<List<KeyCompetence>>() {
        });

        assertEquals(2, result.size());

        List<String> expected = Arrays.asList("Cultural_and_value_competence", "Social_and_citizenship_competence");
        List<String> actual = result.stream().map(KeyCompetence::getName).collect(Collectors.toList());

        assertTrue(actual.containsAll(expected));
    }

    private void assertValidLicenseType(LicenseType licenseType) {
        Map<Long, String> licenseTypes = new HashMap<>();
        licenseTypes.put(1L, "CCBY");
        licenseTypes.put(2L, "CCBYSA");
        licenseTypes.put(3L, "CCBYND");

        assertNotNull(licenseType.getId());
        assertNotNull(licenseType.getName());
        if (licenseTypes.containsKey(licenseType.getId())) {
            assertEquals(licenseTypes.get(licenseType.getId()), licenseType.getName());
        } else {
            fail("LicenseType with unexpected id.");
        }
    }

}
