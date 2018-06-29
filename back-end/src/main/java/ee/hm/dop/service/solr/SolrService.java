package ee.hm.dop.service.solr;

import ee.hm.dop.model.solr.SearchResponse;
import ee.hm.dop.service.SuggestionStrategy;
import ee.hm.dop.utils.tokenizer.DOPSearchStringTokenizer;
import org.apache.commons.configuration.Configuration;
import org.apache.commons.lang3.StringUtils;
import org.apache.solr.client.solrj.SolrClient;
import org.apache.solr.client.solrj.SolrQuery;
import org.apache.solr.client.solrj.SolrRequest;
import org.apache.solr.client.solrj.SolrServerException;
import org.apache.solr.client.solrj.impl.HttpSolrClient;
import org.apache.solr.client.solrj.response.QueryResponse;
import org.apache.solr.client.solrj.response.Suggestion;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.inject.Inject;
import javax.inject.Singleton;
import javax.ws.rs.client.Client;
import javax.ws.rs.client.WebTarget;
import javax.ws.rs.core.MediaType;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map.Entry;
import java.util.stream.Collectors;

import static ee.hm.dop.utils.ConfigurationProperties.SEARCH_SERVER;
import static java.lang.String.format;
import static java.nio.charset.StandardCharsets.UTF_8;
import static org.apache.commons.lang3.StringUtils.isNotBlank;

@Singleton
public class SolrService implements SolrEngineService {

    static final String SOLR_IMPORT_PARTIAL = "dataimport?command=delta-import&wt=json";
    static final String SOLR_DATAIMPORT_STATUS = "dataimport?command=status&wt=json";
    static final String SOLR_STATUS_BUSY = "busy";
    static final List<String> GROUPING_KEYS = Arrays.asList("title", "tag", "description", "author", "publisher");
    private static final Logger logger = LoggerFactory.getLogger(SolrService.class);
    private static final int RESULTS_PER_PAGE = 24;
    private static final int SUGGEST_COUNT = 5;
    private static final String SEARCH_PATH = "select?q=%1$s" +
            "&sort=%2$s" +
            "&wt=json" +
            "&start=%3$d" +
            "&rows=%4$d";
    private static final String SEARCH_PATH_GROUPING = "&group=true&group.format=simple";
    private static final String GROUP_QUERY = "&group.query=";
    private static final String SUGGEST_URL = "/suggest";
    private static final String SUGGEST_TAG_URL = "/suggest_tag";
    private static final String TYPE_MATERIAL = " AND type:\"material\"";
    private static final String TYPE_PORTFOLIO = " AND type:\"portfolio\"";
    @Inject
    private Client client;
    @Inject
    private Configuration configuration;
    private SolrClient solrClient;
    private SolrIndexThread indexThread;

    static String getTokenizedQueryString(String query) {
        StringBuilder sb = new StringBuilder();
        if (isNotBlank(query)) {
            query = query.replaceAll("\\+", " ");
            DOPSearchStringTokenizer tokenizer = new DOPSearchStringTokenizer(query);
            while (tokenizer.hasMoreTokens()) {
                sb.append(tokenizer.nextToken());
                if (tokenizer.hasMoreTokens()) sb.append(" ");
            }
        }
        return sb.toString();
    }

    @Inject
    public void postConstruct() {
        postConstruct(configuration.getString(SEARCH_SERVER));
    }

    void postConstruct(String url) {
        solrClient = new HttpSolrClient.Builder()
                .withBaseSolrUrl(url)
                .build();
        indexThread = new SolrIndexThread();
        indexThread.start();
    }

    @Override
    public SearchResponse search(SearchRequest searchRequest) {
        Long itemLimit = searchRequest.getItemLimit() == 0
                ? RESULTS_PER_PAGE
                : Math.min(searchRequest.getItemLimit(), RESULTS_PER_PAGE);

        return executeCommand(getSearchCommand(searchRequest, itemLimit));
    }

    @Override
    public SearchResponse limitlessSearch(SearchRequest searchRequest) {
        SearchGrouping initialGrouping = searchRequest.getGrouping();
        searchRequest.setGrouping(SearchGrouping.GROUP_NONE);
        SearchResponse response = executeCommand(getSearchCommand(searchRequest, (long) 2147483647));
        searchRequest.setGrouping(initialGrouping);
        return response;
    }

    private String getSearchCommand(SearchRequest searchRequest, Long itemLimit) {
        String searchPath = searchRequest.getGrouping().isAnyGrouping()
                ? SEARCH_PATH + SEARCH_PATH_GROUPING
                : SEARCH_PATH;
        String command = format(searchPath,
                encode(searchRequest.getSolrQuery()),
                searchRequest.getSort() != null ? encode(searchRequest.getSort()) : "",
                searchRequest.getFirstItem(),
                itemLimit);
        if (searchRequest.getGrouping().isAnyGrouping()) command += getGroupingCommand(searchRequest);
        return command;
    }

    private String getGroupingCommand(SearchRequest searchRequest) {
        String query = StringUtils.isBlank(searchRequest.getOriginalQuery()) ? "\"\"" : searchRequest.getOriginalQuery();
        String groupSearchPathMaterial;
        String groupSearchPathPortfolio;
        if (searchRequest.getGrouping().isPhraseGrouping()) {
            groupSearchPathMaterial = getGroupsForQuery("(" + query + ")" + TYPE_MATERIAL);
            groupSearchPathPortfolio = getGroupsForQuery("(" + query + ")" + TYPE_PORTFOLIO);
            groupSearchPathMaterial += getGroupsForQuery("\"" + query + "\"" + TYPE_MATERIAL);
            groupSearchPathPortfolio += getGroupsForQuery("\"" + query + "\"" + TYPE_PORTFOLIO);
        } else {
            groupSearchPathMaterial = getGroupsForQuery(query + TYPE_MATERIAL);
            groupSearchPathPortfolio = getGroupsForQuery(query + TYPE_PORTFOLIO);
        }
        return groupSearchPathMaterial + groupSearchPathPortfolio;
    }

    private String getGroupsForQuery(String query) {
        return GROUPING_KEYS.stream()
                .map(groupName -> GROUP_QUERY + groupName + ":" + encode(query))
                .collect(Collectors.joining());
    }

    @Override
    public List<String> suggest(String query, SuggestionStrategy suggestionStrategy) {
        if (query.isEmpty()) return null;
        SolrQuery solrQuery = new SolrQuery();
        solrQuery.setRequestHandler(suggestionStrategy.suggestTag() ? SUGGEST_TAG_URL : SUGGEST_URL);
        solrQuery.setQuery(query);

        QueryResponse qr;
        try {
            qr = solrClient.query(solrQuery, SolrRequest.METHOD.POST);
        } catch (SolrServerException | IOException e) {
            logger.error("The SolrServer encountered an error.");
            return null;
        }

        if (qr.getSuggesterResponse() == null) return null;
        List<Suggestion> combinedSuggestions = new ArrayList<>();

        if (suggestionStrategy.suggestTag()) {
            combinedSuggestions.addAll(qr.getSuggesterResponse().getSuggestions().get("dopTagSuggester"));
        } else {
            combinedSuggestions.addAll(qr.getSuggesterResponse().getSuggestions().get("linkSuggester"));
            combinedSuggestions.addAll(qr.getSuggesterResponse().getSuggestions().get("dopSuggester"));
        }

        List<String> suggestions = combinedSuggestions.stream().map(Suggestion::getTerm).collect(Collectors.toList());
        return suggestions.size() > SUGGEST_COUNT ? suggestions.subList(0, SUGGEST_COUNT - 1) : suggestions;
    }

    @Override
    public void updateIndex() {
        indexThread.updateIndex();
    }

    private boolean isIndexingInProgress() {
        SearchResponse response = executeCommand(SOLR_DATAIMPORT_STATUS);
        return response.getStatus().equals(SOLR_STATUS_BUSY);
    }

    SearchResponse executeCommand(String command) {
        SearchResponse searchResponse = getTarget(command).request(MediaType.APPLICATION_JSON).get(SearchResponse.class);
        logCommand(command, searchResponse);
        return searchResponse;
    }

    private void logCommand(String command, SearchResponse searchResponse) {
        long responseCode = searchResponse.getResponseHeader().getStatus();

        String statusMessages = "";
        if (searchResponse.getStatusMessages() != null) {
            statusMessages = "Status messages: " + searchResponse.getStatusMessages().entrySet().stream()
                    .map(Entry::toString)
                    .collect(Collectors.joining(";", "[", "]"));
        }

        String logMessage = String.format("Solr responded with code %s, url was %s %s", responseCode,
                configuration.getString(SEARCH_SERVER) + command, statusMessages);

        if (responseCode != 0) {
            logger.info(logMessage);
        } else {
            logger.debug(logMessage);
        }
    }

    private WebTarget getTarget(String path) {
        return client.target(getFullURL(path));
    }

    private String getFullURL(String path) {
        String serverUrl = configuration.getString(SEARCH_SERVER);
        return serverUrl + path;
    }

    private String encode(String query) {
        try {
            return URLEncoder.encode(query, UTF_8.name());
        } catch (UnsupportedEncodingException e) {
            throw new RuntimeException(e);
        }
    }

    private class SolrIndexThread extends Thread {
        public static final int _1_SEC = 1000;
        private final Object lock = new Object();
        private boolean updateIndex;

        public void updateIndex() {
            synchronized (lock) {
                updateIndex = true;
            }
        }

        @Override
        public void run() {
            try {
                while (true) {
                    if (updateIndex) {
                        synchronized (lock) {
                            updateIndex = false;
                            lock.notifyAll();
                            logger.info("Updating Solr index.");
                            executeCommand(SOLR_IMPORT_PARTIAL);
                            waitForCommandToFinish();
                        }
                    }

                    sleep(_1_SEC);
                }
            } catch (InterruptedException e) {
                logger.info("Solr indexing thread interrupted.");
            }
        }

        private void waitForCommandToFinish() throws InterruptedException {
            while (isIndexingInProgress()) {
                sleep(_1_SEC);
            }
        }
    }
}
