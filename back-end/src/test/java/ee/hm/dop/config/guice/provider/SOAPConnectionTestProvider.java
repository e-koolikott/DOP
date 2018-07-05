package ee.hm.dop.config.guice.provider;

import javax.inject.Inject;
import javax.xml.soap.SOAPConnection;

import com.google.inject.Provider;
import com.google.inject.Singleton;
import ee.hm.dop.config.guice.provider.mock.soap.SOAPConnectionMock;
import org.apache.commons.configuration2.Configuration;

/**
 * Guice provider of SOAPConnection.
 */
@Singleton
public class SOAPConnectionTestProvider implements Provider<SOAPConnection> {

    @Inject
    private Configuration configuration;

    @Override
    public synchronized SOAPConnection get() {
        return new SOAPConnectionMock(configuration);
    }
}