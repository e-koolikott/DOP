package ee.hm.dop.config.guice.provider;

import static ee.hm.dop.utils.ConfigurationProperties.*;
import static org.easymock.EasyMock.createNiceMock;
import static org.easymock.EasyMock.expect;
import static org.easymock.EasyMock.replay;

import javax.inject.Inject;
import javax.ws.rs.client.Client;

import com.google.inject.Provider;
import com.google.inject.Singleton;
import ee.hm.dop.config.guice.provider.mock.ekool.person.EkoolPersonWebTarget;
import ee.hm.dop.config.guice.provider.mock.ekool.token.EkoolTokenWebTarget;
import ee.hm.dop.config.guice.provider.mock.harid.HaridPersonWebTarget;
import ee.hm.dop.config.guice.provider.mock.harid.HaridTokenWebTarget;
import ee.hm.dop.config.guice.provider.mock.stuudium.stuudiumuser.StuudiumUserWebTarget;
import org.apache.commons.configuration2.Configuration;

/**
 * Provider for Client.
 */
@Singleton
public class HttpClientTestProvider implements Provider<Client> {

    @Inject
    private Configuration configuration;

    private Client client;

    @Override
    public Client get() {
        if (client == null) {
            initClient();
        }

        return client;
    }

    /**
     * Protected for test purpose
     */
    protected void initClient() {
        client = createNiceMock(Client.class);

        expect(client.target(configuration.getString(EKOOL_URL_TOKEN))).andReturn(new EkoolTokenWebTarget()).anyTimes();
        expect(client.target(configuration.getString(EKOOL_URL_GENERALDATA))).andReturn(new EkoolPersonWebTarget());
        expect(client.target(configuration.getString(HARID_URL_TOKEN))).andReturn(new HaridTokenWebTarget()).anyTimes();
            expect(client.target(configuration.getString(HARID_URL_GENERALDATA))).andReturn(new HaridPersonWebTarget()).anyTimes();
        expect(client.target(configuration.getString(STUUDIUM_URL_GENERALDATA))).andReturn(new StuudiumUserWebTarget()).anyTimes();

        replay(client);
    }

}
