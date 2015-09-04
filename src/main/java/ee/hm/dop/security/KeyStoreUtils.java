package ee.hm.dop.security;

import java.io.File;
import java.io.FileInputStream;
import java.net.URI;
import java.net.URISyntaxException;
import java.security.KeyStore;
import java.util.HashMap;
import java.util.Map;

import org.opensaml.xml.security.Criteria;
import org.opensaml.xml.security.CriteriaSet;
import org.opensaml.xml.security.credential.Credential;
import org.opensaml.xml.security.credential.KeyStoreCredentialResolver;
import org.opensaml.xml.security.criteria.EntityIDCriteria;
import org.opensaml.xml.security.x509.X509Credential;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class KeyStoreUtils {

    private static final Logger logger = LoggerFactory.getLogger(KeyStoreUtils.class);

    public static KeyStore loadKeystore(String filename, String password) {
        KeyStore keyStore = null;
        FileInputStream inputStream = null;

        // Try to load keystore file from outside of the application
        try {
            File inputFile = new File(filename);
            inputStream = new FileInputStream(inputFile);
        } catch (Exception e) {
            logger.info("Custom keystore " + filename + " could not be loaded. ");
        }

        // Load keystore from application resources
        try {
            if (inputStream == null) {
                File inputFile = getResourceAsFile(filename);
                inputStream = new FileInputStream(inputFile);
            }
            keyStore = KeyStore.getInstance(KeyStore.getDefaultType());
            keyStore.load(inputStream, password.toCharArray());
            inputStream.close();
        } catch (Exception e) {
            logger.error("Error loading keystore: " + filename);
        }

        return keyStore;
    }

    public static Credential getSigningCredential(KeyStore keystore, String entityId, String entityPassword) {
        X509Credential credential = null;

        try {
            Map<String, String> passwords = new HashMap<>();
            passwords.put(entityId, entityPassword);
            KeyStoreCredentialResolver resolver = new KeyStoreCredentialResolver(keystore, passwords);

            Criteria criteria = new EntityIDCriteria(entityId);
            CriteriaSet criteriaSet = new CriteriaSet(criteria);

            credential = (X509Credential) resolver.resolveSingle(criteriaSet);
        } catch (Exception e) {
            logger.error("Error while getting signing credential.");
        }

        return credential;
    }

    protected static File getResourceAsFile(String resourcePath) throws URISyntaxException {
        URI resource = KeyStoreUtils.class.getClassLoader().getResource(resourcePath).toURI();
        return new File(resource);
    }

}
