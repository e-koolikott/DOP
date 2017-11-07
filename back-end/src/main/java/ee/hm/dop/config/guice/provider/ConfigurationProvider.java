package ee.hm.dop.config.guice.provider;

import static java.lang.String.format;

import java.io.File;
import java.net.URL;

import com.google.inject.Provider;
import com.google.inject.Singleton;
import ee.hm.dop.utils.DOPFileUtils;
import org.apache.commons.configuration.CompositeConfiguration;
import org.apache.commons.configuration.Configuration;
import org.apache.commons.configuration.PropertiesConfiguration;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Guice provider of application configuration.
 */
@Singleton
public class ConfigurationProvider implements Provider<Configuration> {

    private Logger logger = LoggerFactory.getLogger(getClass());

    private CompositeConfiguration configuration;

    @Override
    public synchronized Configuration get() {

        if (configuration == null) {
            init();
        }

        return configuration;
    }

    private void init() {
        configuration = new CompositeConfiguration();

        Configuration customConfiguration = loadCustomConfiguration();
        if (customConfiguration != null) {
            configuration.addConfiguration(customConfiguration);
        }

        configuration.addConfiguration(loadDefaultConfiguration());
    }

    private Configuration loadCustomConfiguration() {
        Configuration configuration = null;

        String configurationPath = getCustonConfigurationFilePath();
        if (configurationPath != null) {
            logger.info(format("Loading custom configuration file from [%s].", configurationPath));

            try {
                File config = DOPFileUtils.getFile(configurationPath);
                configuration = new PropertiesConfiguration(config);
                logger.info(format("Custom configuration loaded from [%s]", config.getAbsolutePath()));
            } catch (Exception e) {
                throw new RuntimeException("Unable to load custom configuration!", e);
            }
        } else {
            logger.info("No custom configuration file set.");
        }

        return configuration;
    }

    private Configuration loadDefaultConfiguration() {
        Configuration configuration;

        try {
            URL resource = getClass().getClassLoader().getResource(getConfigurationFileName());
            configuration = new PropertiesConfiguration(resource);
            logger.info(String.format("Default configuration loaded from [%s]", resource.toExternalForm()));

        } catch (Exception e) {
            throw new RuntimeException("Unable to load default configuration!", e);
        }

        return configuration;
    }

    private String getConfigurationFileName() {
        return "default.properties";
    }

    protected String getCustonConfigurationFilePath() {
        return System.getProperty("config");
    }
}
