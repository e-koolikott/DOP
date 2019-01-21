package ee.hm.dop.service.atom;

import static ee.hm.dop.service.metadata.TranslationService.filterByLanguage;
import static ee.hm.dop.utils.ConfigurationProperties.MAX_FEED_ITEMS;
import static ee.hm.dop.utils.ConfigurationProperties.SERVER_ADDRESS;
import static java.lang.String.format;

import java.time.ZoneId;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.Date;
import java.util.List;

import javax.inject.Inject;

import ee.hm.dop.dao.LanguageDao;
import ee.hm.dop.dao.MaterialDao;
import ee.hm.dop.dao.PortfolioDao;
import ee.hm.dop.dao.TranslationGroupDao;
import ee.hm.dop.dao.VersionDao;
import ee.hm.dop.model.Author;
import ee.hm.dop.model.LanguageString;
import ee.hm.dop.model.Material;
import ee.hm.dop.model.Portfolio;
import ee.hm.dop.model.Version;
import ee.hm.dop.model.enums.LanguageC;
import ee.hm.dop.rest.VersionResource;
import org.apache.abdera.Abdera;
import org.apache.abdera.factory.Factory;
import org.apache.abdera.model.Entry;
import org.apache.abdera.model.Feed;
import org.apache.commons.configuration2.Configuration;
import java.time.LocalDateTime;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional
public class AtomFeedService {

    public static final String FEED_ID = "FEED_ID";
    public static final String FEED_TITLE = "FEED_TITLE";
    public static final String FEED_VERSION_TITLE = "FEED_VERSION_TITLE";
    public static final String FEED_PORTFOLIO_TITLE = "FEED_PORTFOLIO_TITLE";
    public static final String FEED_MATERIAL_TITLE = "FEED_MATERIAL_TITLE";

    @Inject
    private Configuration configuration;
    @Inject
    private MaterialDao materialDao;
    @Inject
    private PortfolioDao portfolioDao;
    @Inject
    private VersionDao versionDao;
    @Inject
    private TranslationGroupDao translationGroupDao;
    @Inject
    private LanguageDao languageDao;
    @Inject
    private VersionResource versionResource;

    private static Logger logger = LoggerFactory.getLogger(AtomFeedService.class);
    private int maxFeedItems;
    private Factory factory;
    private Feed feed;
    private String lang;

    public AtomFeedService() {
        Abdera abdera = new Abdera();
        factory = abdera.getFactory();
        feed = factory.newFeed();
    }

    public Feed getFeed(String lang) {
        this.lang = lang;
        maxFeedItems = configuration.getInt(MAX_FEED_ITEMS);

        feed.setId(translateString(FEED_ID));
        feed.setTitle(translateString(FEED_TITLE));
        feed.setIcon(format("%s/favicon.ico", configuration.getString(SERVER_ADDRESS)));
        feed.setLogo(format("%s/koolikott.png", configuration.getString(SERVER_ADDRESS)));

        checkVersion();

        return getFeedEntries();
    }

    private Feed getFeedEntries() {
        List<Entry> entryList = new ArrayList<>();

        for (Material material : materialDao.findNewestMaterials(maxFeedItems, 0)) {
            Entry entry = materialToEntry(material);
            if (entry != null)
                entryList.add(entry);
        }

        for (Version version : versionDao.getAllVersions()) {
            Entry entry = versionToEntry(version);
            if (entry != null)
                entryList.add(entry);
        }

        for (Portfolio portfolio : portfolioDao.findNewestPortfolios(maxFeedItems, 0)) {
            Entry entry = portfolioToEntry(portfolio);
            if (entry != null)
                entryList.add(entry);
        }

        entryList.sort(Comparator.comparing(Entry::getUpdated).reversed());

        for (Entry entry : entryList.subList(0, Math.min(maxFeedItems, entryList.size()))) {
            feed.addEntry(entry);
        }

        return feed;
    }

    private Entry materialToEntry(Material material) {
        Entry entry = factory.newEntry();

        entry.setId(format("material:%d", material.getId()));
        String title = translateMaterialTitle(material.getTitles());

        if (title != null)
            entry.setTitle(title);
        else
            return null;

        for (Author author : material.getAuthors()) {
            entry.addAuthor(format("%s %s", author.getName(), author.getSurname()));
        }

        entry.setUpdated(Date.from(material.getAdded().atZone(ZoneId.systemDefault()).toInstant()));
        entry.addLink(format("%s/material?id=%s", configuration.getString(SERVER_ADDRESS), material.getId().toString()));

        return entry;
    }

    private Entry portfolioToEntry(Portfolio portfolio) {
        Entry entry = factory.newEntry();

        entry.setId(format("portfolio:%d", portfolio.getId()));
        entry.setTitle(format(translateString(FEED_PORTFOLIO_TITLE), portfolio.getTitle()));
        entry.addAuthor(format("%s %s", portfolio.getOriginalCreator().getName(), portfolio.getOriginalCreator().getSurname()));
        entry.addLink(format("%s/portfolio?id=%s", configuration.getString(SERVER_ADDRESS), portfolio.getId()));
        entry.setUpdated(Date.from(portfolio.getAdded().atZone(ZoneId.systemDefault()).toInstant()));

        return entry;
    }

    private Entry versionToEntry(Version version) {
        Entry entry = factory.newEntry();

        entry.setId(format("version:%d", version.getId()));
        entry.setTitle(format(translateString(FEED_VERSION_TITLE), version.getVersion()));
        entry.addLink("https://github.com/hariduspilv/koolikott/blob/master/CHANGELOG.md");
        entry.setUpdated(Date.from(version.getReleased().atZone(ZoneId.systemDefault()).toInstant()));

        return entry;
    }

    private void checkVersion() {
        String projectVersion = versionResource.getVersion();
        Version persistedVersion = versionDao.getLatestVersion();

        if(projectVersion == null){
            logger.error("Project version could not be obtained!");
            return;
        }

        if (persistedVersion == null || !projectVersion.equals(persistedVersion.getVersion())){
            Version version = new Version();
            version.setVersion(projectVersion);
            version.setReleased(LocalDateTime.now());
            versionDao.addVersion(version);
        }
    }

    private String translateMaterialTitle(List<LanguageString> titles) {
        String titleTranslation = translateString(FEED_MATERIAL_TITLE);

        if(titleTranslation == null){
            return null;
        }

        LanguageString translation = filterByLanguage(titles, lang);
        if (translation != null && translation.getText() != null) {
            return format(titleTranslation, translation.getText());
        }

        LanguageString fallbackTranslation = filterByLanguage(titles, LanguageC.EST);
        if (fallbackTranslation != null && fallbackTranslation.getText() != null) {
            return format(titleTranslation, fallbackTranslation.getText());
        }

        if (!titles.isEmpty() && titles.get(0).getText() != null) {
            return format(titleTranslation, titles.get(0).getText());
        }

        return null;
    }

    private String translateString(String toTranslate) {
        Long langCode = languageDao.findByCode(lang).getId();
        return translationGroupDao.getTranslationByKeyAndLangcode(toTranslate, langCode);
    }

}
