package ee.hm.dop.service.synchronizer.oaipmh;

import ORG.oclc.oai.harvester2.verb.ListIdentifiers;
import ee.hm.dop.utils.DateUtils;
import java.time.LocalDateTime;
import org.springframework.stereotype.Component;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;

import java.util.Iterator;

@Component
public class ListIdentifiersConnector {

    private String resumptionToken;
    private NodeList headers;
    private String baseURL;

    public ListIdentifiersConnector connect(String baseURL, LocalDateTime from, String metadataPrefix) throws Exception {
        ListIdentifiers listIdentifiers = newListIdentifier(baseURL, from, metadataPrefix);
        headers = getHeaders(listIdentifiers);
        resumptionToken = listIdentifiers.getResumptionToken();
        this.baseURL = baseURL;
        return this;
    }

    public Iterator<Element> iterator() {
        return new IdentifierIterator(headers, baseURL, resumptionToken);
    }

    protected ListIdentifiers newListIdentifier(String baseURL, LocalDateTime from, String metadataPrefix) throws Exception {
        String fromDate = from != null ? DateUtils.toStringWithoutMillis(from) : null;
        return new ListIdentifiers(baseURL, fromDate, null, null, metadataPrefix);
    }

    private NodeList getHeaders(ListIdentifiers listIdentifiers) {
        Document doc = listIdentifiers.getDocument();
        doc.getDocumentElement().normalize();
        return doc.getElementsByTagName("header");
    }
}
