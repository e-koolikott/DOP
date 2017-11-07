package ee.hm.dop.dao;

import static java.util.Arrays.asList;
import static junit.framework.TestCase.assertNull;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import java.security.InvalidParameterException;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

import javax.inject.Inject;
import javax.persistence.RollbackException;

import com.google.common.collect.Lists;
import ee.hm.dop.common.test.DatabaseTestBase;
import ee.hm.dop.common.test.TestConstants;
import ee.hm.dop.common.test.TestLayer;
import ee.hm.dop.model.*;
import ee.hm.dop.model.enums.LanguageC;
import ee.hm.dop.model.enums.TargetGroupEnum;
import ee.hm.dop.model.enums.Visibility;
import ee.hm.dop.model.taxon.Subject;
import ee.hm.dop.model.taxon.Taxon;
import ee.hm.dop.service.content.enums.GetMaterialStrategy;
import ee.hm.dop.utils.DbUtils;
import org.joda.time.DateTime;
import org.junit.Test;

public class MaterialDaoTest extends DatabaseTestBase {

    @Inject
    private MaterialDao materialDao;

    @Test
    public void find() {
        Material material = materialDao.findByIdNotDeleted(MATERIAL_1);
        assertMaterial1(material, TestLayer.DAO);
    }

    @Test
    public void findDeletedMaterial() {
        Material material = materialDao.findByIdNotDeleted(MATERIAL_11);
        assertNull(material);
    }

    @Test
    public void assertMaterial2() {
        Material material = materialDao.findByIdNotDeleted(MATERIAL_2);
        assertEquals(2, material.getAuthors().size());
        assertEquals("Isaac", material.getAuthors().get(0).getName());
        assertEquals("John Newton", material.getAuthors().get(0).getSurname());
        assertEquals("Leonardo", material.getAuthors().get(1).getName());
        assertEquals("Fibonacci", material.getAuthors().get(1).getSurname());

        assertEquals(LanguageC.RUS, material.getLanguage().getCode());
        assertEquals(new DateTime("1995-07-12T09:00:01.000+00:00"), material.getUpdated());

        assertEquals(4, material.getTags().size());
        assertEquals("matemaatika", material.getTags().get(0).getName());
        assertEquals("mathematics", material.getTags().get(1).getName());
        assertEquals("Математика", material.getTags().get(2).getName());
        assertEquals("учебник", material.getTags().get(3).getName());
    }

    @Test
    public void materialViews() {
        Material material = materialDao.findByIdNotDeleted(MATERIAL_3);
        assertEquals(Long.valueOf(300), material.getViews());
    }

    @Test
    public void findAllById() {
        List<Long> expected = Lists.newArrayList(5L, 7L, 3L);
        List<LearningObject> result = materialDao.findAllById(expected);
        assertNotNull(result);
        List<Long> resultIds = result.stream().map(LearningObject::getId).collect(Collectors.toList());
        assertEquals(3, result.size());
        assertTrue(expected.containsAll(resultIds));
    }

    @Test
    public void findAllByIdNoResult() {
        List<LearningObject> result = materialDao.findAllById(Lists.newArrayList(NOT_EXISTS_ID));
        assertNotNull(result);
        assertEquals(0, result.size());
    }

    @Test
    public void findAllByIdEmptyList() {
        List<LearningObject> result = materialDao.findAllById(new ArrayList<>());
        assertNotNull(result);
        assertEquals(0, result.size());
    }

    @Test
    public void createMaterial() {
        Material material = new Material();
        material.setSource("asd");
        material.setAdded(new DateTime());
        material.setViews((long) 123);
        material.setVisibility(Visibility.PUBLIC);

        material.setPicture(picture());

        Material updated = materialDao.createOrUpdate(material);

        Material newMaterial = materialDao.findByIdNotDeleted(updated.getId());

        assertEquals(material.getSource(), newMaterial.getSource());
        assertEquals(material.getAdded(), newMaterial.getAdded());
        assertEquals(material.getViews(), newMaterial.getViews());
        assertEquals(material.getPicture().getId(), newMaterial.getPicture().getId());
        assertNull(newMaterial.getUpdated());

        materialDao.remove(newMaterial);
    }

    @Test
    public void findMaterialWith2Subjects() {
        Material material = materialDao.findByIdNotDeleted(MATERIAL_6);
        List<Taxon> taxons = material.getTaxons();
        assertNotNull(taxons);
        assertEquals(2, taxons.size());
        Subject biology = (Subject) taxons.get(0);
        assertEquals(new Long(20), biology.getId());
        assertEquals("Biology", biology.getName());
        Subject math = (Subject) taxons.get(1);
        assertEquals(new Long(21), math.getId());
        assertEquals("Mathematics", math.getName());
    }

    @Test
    public void findMaterialWithNoTaxon() {
        Material material = materialDao.findByIdNotDeleted(MATERIAL_8);
        List<Taxon> taxons = material.getTaxons();
        assertNotNull(taxons);
        assertEquals(0, taxons.size());
    }

    @Test
    public void findByRepositoryAndrepositoryIdentifier() {
        Material material = materialDao.findByRepository(repository(1L), "isssiiaawej");
        assertMaterial1(material, TestLayer.DAO);
    }

    @Test
    public void findByRepositoryAndrepositoryIdentifierWhenRepositoryDoesNotExists() {
        Material material = materialDao.findByRepository(repository(10L), "isssiiaawej");
        assertNull(material);
    }

    @Test
    public void findByRepositoryAndrepositoryIdentifierWhenMaterialIsDeleted() {
        Material material = materialDao.findByRepository(repository(1L), "isssiiaawejdsada4564");
        assertNotNull(material);
    }

    @Test
    public void findByRepositoryAndrepositoryIdentifierWhenRepositoryIdentifierDoesNotExist() {
        Material material = materialDao.findByRepository(repository(1L), "SomeRandomIdenetifier");
        assertNull(material);
    }

    @Test
    public void findByRepositoryAndrepositoryIdentifierNullRepositoryIdAndNullRepositoryIdentifier() {
        Material material = materialDao.findByRepository(new Repository(), null);
        assertNull(material);
    }

    @Test
    public void findByCreator() {
        List<LearningObject> materials = materialDao.findByCreator(userWithId(1L), 0, Integer.MAX_VALUE);
        List<Long> collect = materials.stream().map(Searchable::getId).collect(Collectors.toList());
        assertTrue(collect.containsAll(asList(MATERIAL_8, MATERIAL_4, MATERIAL_1)));

        assertMaterial1((Material) materials.stream().filter(m -> m.getId().equals(MATERIAL_1)).findAny().orElseThrow(RuntimeException::new), TestLayer.DAO);
    }

    @Test
    public void update() {
        Material changedMaterial = new Material();
        changedMaterial.setId(MATERIAL_9);
        changedMaterial.setSource("http://www.chaged.it.com");
        DateTime now = new DateTime();
        changedMaterial.setAdded(now);
        Long views = 234L;
        changedMaterial.setViews(views);
        changedMaterial.setUpdated(now);
        changedMaterial.setVisibility(Visibility.PUBLIC);

        materialDao.createOrUpdate(changedMaterial);

        Material material = materialDao.findByIdNotDeleted(MATERIAL_9);
        assertEquals("http://www.chaged.it.com", changedMaterial.getSource());
        assertEquals(now, changedMaterial.getAdded());
        DateTime updated = changedMaterial.getUpdated();
        assertTrue(updated.isEqual(now) || updated.isAfter(now));
        assertEquals(views, changedMaterial.getViews());

        // Restore to original values
        material.setSource("http://www.chaging.it.com");
        material.setAdded(new DateTime("1911-09-01T00:00:01"));
        material.setViews(0L);
        material.setUpdated(null);

        materialDao.createOrUpdate(changedMaterial);
    }

    @Test
    public void updateCreatingNewLanguage() {
        Material originalMaterial = materialDao.findByIdNotDeleted(MATERIAL_1);

        Language newLanguage = new Language();
        newLanguage.setName("Newlanguage");
        newLanguage.setCode("nlg");

        originalMaterial.setLanguage(newLanguage);

        try {
            materialDao.createOrUpdate(originalMaterial);

            // Have to close the transaction to get the error
            DbUtils.closeTransaction();
            fail("Exception expected.");
        } catch (RollbackException e) {
            String expectedMessage = "org.hibernate.TransientPropertyValueException: "
                    + "object references an unsaved transient instance - "
                    + "save the transient instance before flushing : "
                    + "ee.hm.dop.model.Material.language -> ee.hm.dop.model.Language";
            assertEquals(expectedMessage, e.getCause().getMessage());
        }
    }

    @Test
    public void updateCreatingNewResourceType() {
        Material originalMaterial = materialDao.findByIdNotDeleted(MATERIAL_1);

        ResourceType newResourceType = new ResourceType();
        newResourceType.setName("NewType");

        List<ResourceType> newResourceTypes = new ArrayList<>();
        newResourceTypes.add(newResourceType);

        originalMaterial.setResourceTypes(newResourceTypes);

        try {
            materialDao.createOrUpdate(originalMaterial);

            // Have to close the transaction to get the error
            DbUtils.closeTransaction();
            fail("Exception expected.");
        } catch (RollbackException e) {
            String expectedMessage = "org.hibernate.TransientObjectException: "
                    + "object references an unsaved transient instance - "
                    + "save the transient instance before flushing: ee.hm.dop.model.ResourceType";
            assertEquals(expectedMessage, e.getCause().getMessage());
        }
    }

    @Test
    public void updateCreatingNewTaxon() {
        Material originalMaterial = materialDao.findByIdNotDeleted(MATERIAL_1);

        Subject newSubject = new Subject();
        newSubject.setName("New Subject");

        List<Taxon> newTaxons = new ArrayList<>();
        newTaxons.add(newSubject);

        originalMaterial.setTaxons(newTaxons);

        try {
            materialDao.createOrUpdate(originalMaterial);

            // Have to close the transaction to get the error
            DbUtils.closeTransaction();
            fail("Exception expected.");
        } catch (RollbackException e) {
            String expectedMessage = "org.hibernate.TransientObjectException: "
                    + "object references an unsaved transient instance - "
                    + "save the transient instance before flushing: ee.hm.dop.model.taxon.Taxon";
            assertEquals(expectedMessage, e.getCause().getMessage());
        }
    }

    @Test
    public void updateCreatingNewLicenseType() {
        Material originalMaterial = materialDao.findByIdNotDeleted(MATERIAL_1);

        LicenseType newLicenseType = new LicenseType();
        newLicenseType.setName("NewLicenseTypeTpFail");
        originalMaterial.setLicenseType(newLicenseType);

        try {
            materialDao.createOrUpdate(originalMaterial);

            // Have to close the transaction to get the error
            DbUtils.closeTransaction();
            fail("Exception expected.");
        } catch (RollbackException e) {
            String expectedMessage = "org.hibernate.TransientPropertyValueException: "
                    + "object references an unsaved transient instance - "
                    + "save the transient instance before flushing : "
                    + "ee.hm.dop.model.Material.licenseType -> ee.hm.dop.model.LicenseType";
            assertEquals(expectedMessage, e.getCause().getMessage());
        }
    }

    @Test
    public void updateCreatingNewRepository() {
        Material originalMaterial = materialDao.findByIdNotDeleted(MATERIAL_1);

        Repository newRepository = new Repository();
        newRepository.setBaseURL("www.url.com");
        newRepository.setSchema("newSchema");
        originalMaterial.setRepository(newRepository);

        try {
            materialDao.createOrUpdate(originalMaterial);

            // Have to close the transaction to get the error
            DbUtils.closeTransaction();
            fail("Exception expected.");
        } catch (RollbackException e) {
            String expectedMessage = "org.hibernate.TransientPropertyValueException: "
                    + "object references an unsaved transient instance - "
                    + "save the transient instance before flushing : "
                    + "ee.hm.dop.model.Material.repository -> ee.hm.dop.model.Repository";
            assertEquals(expectedMessage, e.getCause().getMessage());
        }
    }

    @Test
    public void delete() {
        Material material = materialDao.findByIdNotDeleted(MATERIAL_10);
        materialDao.delete(material);

        Material deletedMaterial = materialDao.findByIdNotDeleted(MATERIAL_10);
        assertNull(deletedMaterial);
    }

    @Test
    public void deleteMaterialDoesNotExist() {
        Material material = new Material();

        try {
            materialDao.delete(material);
            fail("Exception expected");
        } catch (InvalidParameterException e) {
            assertEquals("LearningObject does not exist.", e.getMessage());
        }
    }

    @Test
    public void isPaidTrue() {
        Material material = materialDao.findByIdNotDeleted(MATERIAL_1);
        assertTrue(material.isPaid());
    }

    @Test
    public void isPaidFalse() {
        Material material = materialDao.findByIdNotDeleted(MATERIAL_9);
        assertFalse(material.isPaid());
    }

    @Test
    public void isEmbeddedWhenNoRepository() {
        Material material = materialDao.findByIdNotDeleted(MATERIAL_3);
        assertFalse(material.isEmbeddable());
    }

    @Test
    public void isEmbeddedWhenEstonianRepo() {
        Material material = materialDao.findByIdNotDeleted(MATERIAL_12);
        assertTrue(material.isEmbeddable());
    }

    @Test
    public void isEmbeddedWhenNotEstonianRepo() {
        Material material = materialDao.findByIdNotDeleted(MATERIAL_1);
        assertFalse(material.isEmbeddable());
    }

    @Test
    public void getMaterialsBySource1() {
        List<Material> materials = materialDao.findBySource("en.wikipedia.org/wiki/Power_Architecture", GetMaterialStrategy.ONLY_EXISTING);
        assertEquals(2, materials.size());
    }

    @Test
    public void getMaterialsBySource2() {
        List<Material> materials = materialDao.findBySource("youtube.com/watch?v=gSWbx3CvVUk", GetMaterialStrategy.ONLY_EXISTING);
        assertEquals(1, materials.size());
    }

    @Test
    public void getMaterialsBySource3() {
        List<Material> materials = materialDao.findBySource("www.youtube.com/watch?v=gSWbx3CvVUk", GetMaterialStrategy.ONLY_EXISTING);
        assertEquals(1, materials.size());
    }

    @Test
    public void getMaterialsBySource4() {
        List<Material> materials = materialDao.findBySource("https://en.wikipedia.org/wiki/List_of_ISO_639-1_codes", GetMaterialStrategy.ONLY_EXISTING);
        assertEquals(1, materials.size());
    }

    public Picture picture() {
        Picture picture = new OriginalPicture();
        picture.setId(1);
        return picture;
    }

    public Repository repository(long id) {
        Repository repository = new Repository();
        repository.setId(id);
        return repository;
    }
}
