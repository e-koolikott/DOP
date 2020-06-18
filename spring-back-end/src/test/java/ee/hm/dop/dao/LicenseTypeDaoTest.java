package ee.hm.dop.dao;

import ee.hm.dop.common.test.DatabaseTestBase;
import ee.hm.dop.model.LicenseType;
import org.junit.Test;

import javax.inject.Inject;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.junit.Assert.*;

public class LicenseTypeDaoTest extends DatabaseTestBase {

    @Inject
    private LicenseTypeDao licenseTypeDao;

    @Test
    public void findAll() {
        List<LicenseType> licenseTypes = licenseTypeDao.findAll();

        assertEquals(3, licenseTypes.size());
        for (int i = 0; i < licenseTypes.size(); i++) {
            assertValidLicenseType(licenseTypes.get(i));
        }
    }

    @Test
    public void findAllByName_doesnt_fail() {
        licenseTypeDao.findByNameIgnoreCase("CCBY");

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
