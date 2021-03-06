package ee.hm.dop.service.ehis;

import ee.hm.dop.common.test.DatabaseTestBase;
import ee.hm.dop.model.ehis.Institution;
import ee.hm.dop.model.ehis.Person;
import ee.hm.dop.model.ehis.Role;
import org.junit.Ignore;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Profile;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.junit4.SpringRunner;

import javax.inject.Inject;
import java.util.List;

import static ee.hm.dop.model.ehis.Role.InstitutionalRole.PRINCIPAL;
import static ee.hm.dop.model.ehis.Role.InstitutionalRole.STUDENT;
import static ee.hm.dop.model.ehis.Role.InstitutionalRole.TEACHER;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;

@Ignore
//todo use mock instead of real service
public class EhisSOAPServiceTest extends DatabaseTestBase {

    @Inject
    private EhisSOAPService ehisSOAPService;

    @Test
    public void getPersonInformationMultipleInstitutions() {
        Person person = ehisSOAPService.getPersonInformation("46212154899");

        List<Institution> institutions = person.getInstitutions();
        assertNotNull(institutions);
        assertEquals(2, institutions.size());

        Institution institution1 = institutions.get(0);
        Role roleInstitution1 = institution1.getRoles().get(0);
        assertEquals("388", institution1.getEhisId());
        assertEquals(TEACHER, roleInstitution1.getInstitutionalRole());
        assertNull(roleInstitution1.getSchoolYear());
        assertNull(roleInstitution1.getSchoolClass());

        Institution institution2 = institutions.get(1);
        Role roleInstitution2 = institution2.getRoles().get(0);
        assertEquals("568", institution2.getEhisId());
        assertEquals(TEACHER, roleInstitution2.getInstitutionalRole());
        assertNull(roleInstitution2.getSchoolYear());
        assertNull(roleInstitution2.getSchoolClass());
    }

    @Test
    public void getPersonInformationStudent() {
        Person person = ehisSOAPService.getPersonInformation("60104294277");

        List<Institution> institutions = person.getInstitutions();
        assertNotNull(institutions);
        assertEquals(1, institutions.size());

        Institution institution = institutions.get(0);
        Role role = institution.getRoles().get(0);
        assertEquals("388", institution.getEhisId());
        assertEquals(STUDENT, role.getInstitutionalRole());
        assertEquals("8", role.getSchoolYear());
        assertEquals("C", role.getSchoolClass());
    }

    @Test
    public void getPersonInformationMultipleRoles() {
        Person person = ehisSOAPService.getPersonInformation("45805217556");

        List<Institution> institutions = person.getInstitutions();
        assertNotNull(institutions);
        assertEquals(1, institutions.size());

        Institution institution = institutions.get(0);
        assertEquals("388", institution.getEhisId());

        Role role1 = institution.getRoles().get(0);
        assertEquals(PRINCIPAL, role1.getInstitutionalRole());
        assertNull(role1.getSchoolYear());
        assertNull(role1.getSchoolClass());

        Role role2 = institution.getRoles().get(1);
        assertEquals(TEACHER, role2.getInstitutionalRole());
        assertNull(role2.getSchoolYear());
        assertNull(role2.getSchoolClass());
    }

    @Test
    public void getPerson4444() {
        Person person = ehisSOAPService.getPersonInformation("4444");

        List<Institution> institutions = person.getInstitutions();
        assertNotNull(institutions);
        assertEquals(1, institutions.size());

        Institution institution = institutions.get(0);
        assertEquals("668", institution.getEhisId());

        Role role1 = institution.getRoles().get(0);
        assertEquals(TEACHER, role1.getInstitutionalRole());
        assertNull(role1.getSchoolYear());
        assertNull(role1.getSchoolClass());
    }
}
