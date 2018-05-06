package ee.hm.dop.config.guice.provider.mock.ekool.person;

import javax.ws.rs.client.Entity;
import javax.ws.rs.core.MultivaluedMap;
import javax.ws.rs.core.Response;

import ee.hm.dop.config.guice.provider.mock.ekool.EkoolResponse;
import ee.hm.dop.config.guice.provider.mock.rs.client.Builder;
import ee.hm.dop.model.ekool.Person;

import java.util.Base64;

import static org.junit.Assert.assertEquals;

public class EkoolPersonBuilder extends Builder {

    private static final String AUTH_HEADER_HASH = "Basic "
            + Base64.getEncoder().encodeToString("koolikott:9rIxgey74Ke87OVYhCZfezyJ6g95UeLI9YxIhY0FuH8m".getBytes());

    @Override
    public Builder header(String name, Object value) {
        assertEquals("Authorization", name);
        assertEquals(AUTH_HEADER_HASH, value);
        return this;
    }

    @Override
    public Response post(Entity<?> entity) {
        MultivaluedMap<String, String> tokenRequestParams = (MultivaluedMap<String, String>) entity.getEntity();

        String token = tokenRequestParams.get("access_token").get(0);
      
        if (token == null) {
            throw new RuntimeException("Invalid token: " + token);
        }
        if(!token.equals("shdsajhfuh5484618") && !token.equals("54fdsgffs4566fds51dsds4g") ) {
        	throw new RuntimeException("Invalid token: " + token);
        }
        
        Person person = new Person();
        if(token.equals("shdsajhfuh5484618")) {
        	person.setFirstName("firstname1");
        	person.setLastName("lastname1");
        	person.setIdCode("111111");
        }
        if(token.equals("54fdsgffs4566fds51dsds4g")) {
        	person.setFirstName("firstname2");
        	person.setLastName("lastname2");
        	person.setIdCode("222222");
        }

        return new EkoolResponse(person);
    }
}
