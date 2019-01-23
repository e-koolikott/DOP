package ee.hm.dop.service.useractions;

import com.fasterxml.jackson.databind.ObjectMapper;
import ee.hm.dop.model.AuthenticatedUser;
import ee.hm.dop.model.ehis.Institution;
import ee.hm.dop.model.ehis.Person;
import ee.hm.dop.model.ehis.Role;
import ee.hm.dop.utils.EncryptionUtils;
import ee.hm.dop.utils.security.KeyStoreUtils;
import org.apache.commons.codec.binary.Base64;
import org.apache.commons.configuration2.Configuration;
import org.easymock.EasyMockRunner;
import org.easymock.Mock;
import org.easymock.TestSubject;
import java.time.LocalDateTime;
import org.json.JSONObject;
import org.junit.Test;
import org.junit.runner.RunWith;

import java.time.temporal.ChronoField;
import java.time.temporal.ChronoUnit;
import java.time.temporal.TemporalField;
import java.util.ArrayList;
import java.util.List;

import static ee.hm.dop.model.ehis.Role.InstitutionalRole.STUDENT;
import static ee.hm.dop.utils.ConfigurationProperties.KEYSTORE_FILENAME;
import static ee.hm.dop.utils.ConfigurationProperties.KEYSTORE_PASSWORD;
import static ee.hm.dop.utils.ConfigurationProperties.KEYSTORE_SIGNING_ENTITY_ID;
import static ee.hm.dop.utils.ConfigurationProperties.KEYSTORE_SIGNING_ENTITY_PASSWORD;
import static org.easymock.EasyMock.expect;
import static org.easymock.EasyMock.replay;
import static org.easymock.EasyMock.verify;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

@RunWith(EasyMockRunner.class)
public class AuthenticatedUserServiceTest {

    @TestSubject
    private AuthenticatedUserService authenticatedUserService = new AuthenticatedUserService();
    @Mock
    private Configuration configuration;

    @Test
    public void service_returns_signed_user_data() throws Exception {
        Role role = new Role();
        role.setInstitutionalRole(STUDENT);
        role.setSchoolYear("2");
        role.setSchoolClass("S");

        List<Role> roles = new ArrayList<>();
        roles.add(role);

        Institution institution = new Institution();
        institution.setEhisId("123");
        institution.setRoles(roles);

        List<Institution> institutions = new ArrayList<>();
        institutions.add(institution);

        Person person = new Person();
        person.setId(412L);
        person.setInstitutions(institutions);

        AuthenticatedUser authenticatedUser = new AuthenticatedUser();
        authenticatedUser.setToken("uniqueToken");
        authenticatedUser.setPerson(person);

        expect(configuration.getString(KEYSTORE_FILENAME)).andReturn("test.keystore").anyTimes();
        expect(configuration.getString(KEYSTORE_PASSWORD)).andReturn("newKeyStorePass").anyTimes();
        expect(configuration.getString(KEYSTORE_SIGNING_ENTITY_ID)).andReturn("testAlias").anyTimes();
        expect(configuration.getString(KEYSTORE_SIGNING_ENTITY_PASSWORD)).andReturn("newKeyPass").anyTimes();

        replayAll();
        String signedUserData = authenticatedUserService.signUserData(authenticatedUser);

        assertNotNull(signedUserData);

        byte[] bytes = Base64.decodeBase64(signedUserData);
        String userData = EncryptionUtils.decrypt(bytes, KeyStoreUtils.getDOPSigningCredential(configuration)
                .getPublicKey());
        verifyAll();

        JSONObject userDataObject = new JSONObject(userData);

        //todo time
        //LocalDateTime dateTime = LocalDateTime.parse(userDataObject.getString("createdAt"));
        //assertTrue(dateTime.isBefore(LocalDateTime.now()) && dateTime.isAfter(LocalDateTime.now().minusSeconds(5)));

        JSONObject authenticationContext = userDataObject.getJSONObject("authCtx");
        ObjectMapper mapper = new ObjectMapper();
        Person authenticatedPerson = mapper.readValue(authenticationContext.toString(), Person.class);
        assertNull(authenticatedPerson.getId());
        assertEquals(person.getInstitutions().size(), authenticatedPerson.getInstitutions().size());

        Institution authenticatedInstitution = authenticatedPerson.getInstitutions().get(0);
        assertNull(authenticatedInstitution.getId());
        assertEquals(institution.getEhisId(), authenticatedInstitution.getEhisId());
        assertEquals(authenticatedInstitution.getRoles().size(), authenticatedInstitution.getRoles().size());

        Role authenticatedRole = authenticatedInstitution.getRoles().get(0);
        assertEquals(role.getInstitutionalRole(), authenticatedRole.getInstitutionalRole());
        assertEquals(role.getSchoolClass(), authenticatedRole.getSchoolClass());
        assertEquals(role.getSchoolYear(), authenticatedRole.getSchoolYear());
    }

    private void replayAll(Object... mocks) {
        replay(configuration);

        if (mocks != null) {
            for (Object object : mocks) {
                replay(object);
            }
        }
    }

    private void verifyAll(Object... mocks) {
        verify(configuration);

        if (mocks != null) {
            for (Object object : mocks) {
                verify(object);
            }
        }
    }
}
