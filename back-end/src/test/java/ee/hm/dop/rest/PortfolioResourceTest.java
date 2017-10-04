package ee.hm.dop.rest;

import ee.hm.dop.common.test.ResourceIntegrationTestBase;
import ee.hm.dop.model.Chapter;
import ee.hm.dop.model.ChapterObject;
import ee.hm.dop.model.ContentRow;
import ee.hm.dop.model.LearningObject;
import ee.hm.dop.model.Material;
import ee.hm.dop.model.Portfolio;
import ee.hm.dop.model.Recommendation;
import ee.hm.dop.model.SearchResult;
import ee.hm.dop.model.Searchable;
import ee.hm.dop.model.enums.TargetGroupEnum;
import ee.hm.dop.model.User;
import ee.hm.dop.model.enums.Visibility;
import org.joda.time.DateTime;
import org.junit.Ignore;
import org.junit.Test;

import javax.ws.rs.client.Entity;
import javax.ws.rs.core.GenericType;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.Response.Status;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

import static java.lang.String.format;
import static org.junit.Assert.*;

public class PortfolioResourceTest extends ResourceIntegrationTestBase {

    private static final String CREATE_PORTFOLIO_URL = "portfolio/create";
    private static final String UPDATE_PORTFOLIO_URL = "portfolio/update";
    private static final String GET_PORTFOLIO_URL = "portfolio?id=%s";
    private static final String GET_BY_CREATOR_URL = "portfolio/getByCreator?username=%s";
    private static final String PORTFOLIO_INCREASE_VIEW_COUNT_URL = "portfolio/increaseViewCount";
    private static final String PORTFOLIO_COPY_URL = "portfolio/copy";
    private static final String DELETE_PORTFOLIO_URL = "portfolio/delete";
    private static final String PORTFOLIO_ADD_RECOMMENDATION_URL = "portfolio/recommend";
    private static final String PORTFOLIO_REMOVE_RECOMMENDATION_URL = "portfolio/removeRecommendation";
    private static final String CREATE_MATERIAL_URL = "material";

    @Test
    public void getPortfolio() {
        Portfolio portfolio = getPortfolio(101);
        assertPortfolio101(portfolio);
    }

    @Test
    public void getNotExistingPortfolio() {
        Response response = doGet(format(GET_PORTFOLIO_URL, 2000));
        assertEquals(Status.INTERNAL_SERVER_ERROR.getStatusCode(), response.getStatus());
    }

    @Test
    public void getPrivatePortfolioAsCreator() {
        login(USER_PEETER);
        Long id = 107L;

        Portfolio portfolio = getPortfolio(id);

        assertEquals(id, portfolio.getId());
        assertEquals("This portfolio is private. ", portfolio.getTitle());
    }

    @Test
    public void getPrivatePortfolioAsNotCreator() {
        login("15066990099");
        Long id = 7L;

        Response response = doGet(format(GET_PORTFOLIO_URL, id));
        assertEquals(Status.INTERNAL_SERVER_ERROR.getStatusCode(), response.getStatus());
    }

    @Test
    public void getPrivatePortfolioAsAdmin() {
        login(USER_ADMIN);
        Long id = 107L;

        Portfolio portfolio = getPortfolio(id);

        assertEquals(id, portfolio.getId());
        assertEquals("This portfolio is private. ", portfolio.getTitle());
    }

    @Test
    public void getByCreator() {
        String username = "mati.maasikas-vaarikas";
        SearchResult result = doGet(format(GET_BY_CREATOR_URL, username)).readEntity(SearchResult.class);
        List<Searchable> portfolios = result.getItems();

        assertEquals(3, portfolios.size());

        Set<Long> expectedPortfolios = new HashSet<>();
        expectedPortfolios.add(Long.valueOf(103));
        expectedPortfolios.add(Long.valueOf(101));
        expectedPortfolios.add(Long.valueOf(114));

        expectedPortfolios.remove(portfolios.get(0).getId());
        expectedPortfolios.remove(portfolios.get(1).getId());
        expectedPortfolios.remove(portfolios.get(2).getId());

        assertTrue(expectedPortfolios.isEmpty());
    }

    @Test
    public void getByCreatorWhenSomeArePrivateOrNotListed() {
        String username = "my.testuser";
        SearchResult result = doGet(format(GET_BY_CREATOR_URL, username), SearchResult.class);
        List<Searchable> portfolios = result.getItems();

        assertEquals(1, portfolios.size());
        assertEquals(Long.valueOf(109), portfolios.get(0).getId());
    }

    @Test
    public void getByCreatorWhenSomeArePrivateOrNotListedAsCreator() {
        login("78912378912");

        String username = "my.testuser";
        SearchResult result = doGet(format(GET_BY_CREATOR_URL, username), SearchResult.class);
        List<Searchable> portfolios = result.getItems();

        assertEquals(3, portfolios.size());
        List<Long> expectedIds = Arrays.asList(109L, 110L, 111L);
        List<Long> actualIds = portfolios.stream().map(Searchable::getId).collect(Collectors.toList());
        assertTrue(actualIds.containsAll(expectedIds));
    }

    @Test
    public void getByCreatorWhenSomeArePrivateOrNotListedAsAdmin() {
        login(USER_ADMIN);

        String username = "my.testuser";
        SearchResult result = doGet(format(GET_BY_CREATOR_URL, username), SearchResult.class);
        List<Searchable> portfolios = result.getItems();

        assertEquals(3, portfolios.size());
        List<Long> expectedIds = Arrays.asList(109L, 110L, 111L);
        List<Long> actualIds = portfolios.stream().map(Searchable::getId).collect(Collectors.toList());
        assertTrue(actualIds.containsAll(expectedIds));
    }

    @Test
    public void getByCreatorWithoutUsername() {
        Response response = doGet("portfolio/getByCreator");
        assertEquals(Response.Status.BAD_REQUEST.getStatusCode(), response.getStatus());
        assertEquals("Username parameter is mandatory", response.readEntity(String.class));
    }

    @Test
    public void getByCreatorWithBlankUsername() {
        Response response = doGet(format(GET_BY_CREATOR_URL, ""));
        assertEquals(Response.Status.BAD_REQUEST.getStatusCode(), response.getStatus());
        assertEquals("Username parameter is mandatory", response.readEntity(String.class));
    }

    @Test
    public void getByCreatorNotExistingUser() {
        String username = "notexisting.user";
        Response response = doGet(format(GET_BY_CREATOR_URL, username));
        assertEquals(Response.Status.BAD_REQUEST.getStatusCode(), response.getStatus());
        assertEquals("User does not exist with this username parameter", response.readEntity(String.class));
    }

    @Test
    public void getByCreatorNoMaterials() {
        String username = "voldemar.vapustav";
        SearchResult portfolios = doGet(format(GET_BY_CREATOR_URL, username)).readEntity(
                SearchResult.class);

        assertEquals(0, portfolios.getItems().size());
        assertEquals(0, portfolios.getStart());
        assertEquals(0, portfolios.getTotalResults());
    }

    @Test
    public void increaseViewCount() {
        long id = 103;
        Portfolio portfolioBefore = getPortfolio(id);

        Portfolio portfolio = new Portfolio();
        portfolio.setId(id);

        doPost(PORTFOLIO_INCREASE_VIEW_COUNT_URL, Entity.entity(portfolio, MediaType.APPLICATION_JSON_TYPE));

        Portfolio portfolioAfter = getPortfolio(id);

        assertEquals(Long.valueOf(portfolioBefore.getViews() + 1), portfolioAfter.getViews());
    }

    @Test
    public void increaseViewCountNoPortfolio() {
        Portfolio portfolio = new Portfolio();
        portfolio.setId(99999L);

        Response response = doPost(PORTFOLIO_INCREASE_VIEW_COUNT_URL,
                Entity.entity(portfolio, MediaType.APPLICATION_JSON_TYPE));

        assertEquals(500, response.getStatus());
    }

    @Test
    public void create() {
        login(USER_MATI);
        Long id = 1L;

        Portfolio createdPortfolio = createPortfolio();

        assertNotNull(createdPortfolio);
        assertNotNull(createdPortfolio.getId());
        assertEquals(id, createdPortfolio.getOriginalCreator().getId());
        assertEquals(id, createdPortfolio.getCreator().getId());
    }

    @Test
    public void updateChanginMetadataNoChapters() {
        login(USER_MATI);

        Portfolio portfolio = getPortfolio(105);
        String originalTitle = portfolio.getTitle();
        portfolio.setTitle("New mega nice title that I come with Yesterday night!");

        Portfolio updatedPortfolio = doPost(UPDATE_PORTFOLIO_URL, portfolio, Portfolio.class);

        assertFalse(originalTitle.equals(updatedPortfolio.getTitle()));
        assertEquals("New mega nice title that I come with Yesterday night!", updatedPortfolio.getTitle());

    }

    @Test
    public void updateSomeoneElsesPortfolio() {
        login(USER_PEETER);

        Portfolio portfolio = getPortfolio(105);
        portfolio.setTitle("This is not my portfolio.");

        // Set creator to the current logged in user
        User creator = new User();
        creator.setId(2L);
        portfolio.setCreator(creator);

        Response response = doPost(UPDATE_PORTFOLIO_URL, Entity.entity(portfolio, MediaType.APPLICATION_JSON_TYPE));
        assertEquals(Status.INTERNAL_SERVER_ERROR.getStatusCode(), response.getStatus());
    }

    @Test
    public void updateCreatingChapter() {
        login(USER_MATI);

        List<Chapter> chapters = new ArrayList<>();

        Chapter newChapter = new Chapter();
        newChapter.setTitle("New chapter 1");
        chapters.add(newChapter);

        Portfolio portfolio = getPortfolio(105);
        portfolio.setChapters(chapters);

        Portfolio updatedPortfolio = doPost(UPDATE_PORTFOLIO_URL, portfolio, Portfolio.class);
        assertFalse(updatedPortfolio.getChapters().isEmpty());

    }

    @Test
    public void updateCreatingChapterWithSubchapterNoMaterials() {
        login(USER_MATI);

        List<Chapter> chapters = new ArrayList<>();
        List<Chapter> subchapters = new ArrayList<>();

        Chapter newChapter = new Chapter();
        newChapter.setTitle("New chapter 1");

        Chapter subChapter = new Chapter();
        subChapter.setTitle("New subchapter");
        subchapters.add(subChapter);
        newChapter.setSubchapters(subchapters);

        chapters.add(newChapter);

        Portfolio portfolio = getPortfolio(105);
        portfolio.setChapters(chapters);

        Portfolio updatedPortfolio = doPost(UPDATE_PORTFOLIO_URL, portfolio, Portfolio.class);

        assertFalse(updatedPortfolio.getChapters().isEmpty());
        assertFalse(updatedPortfolio.getChapters().get(0).getSubchapters().isEmpty());
    }

    @Test
    public void updateCreatingChapterWithExistingChapter() {
        login(USER_MATI);

        List<Chapter> subchapters = new ArrayList<>();

        Chapter newChapter = new Chapter();
        newChapter.setTitle("New chapter 1");

        Chapter subChapter = new Chapter();
        subChapter.setTitle("New cool subchapter");
        subchapters.add(subChapter);
        newChapter.setSubchapters(subchapters);

        Portfolio portfolio = getPortfolio(105);
        portfolio.getChapters().add(newChapter);

        Portfolio updatedPortfolio = doPost(UPDATE_PORTFOLIO_URL, portfolio, Portfolio.class);

        assertFalse(updatedPortfolio.getChapters().isEmpty());
        Chapter verify = updatedPortfolio.getChapters().get(updatedPortfolio.getChapters().size() - 1).getSubchapters()
                .get(0);
        assertEquals(verify.getTitle(), "New cool subchapter");

    }

    @Test
    public void updateChangingVisibility() {
        login(USER_PEETER);

        Portfolio portfolio = getPortfolio(106);
        portfolio.setVisibility(Visibility.NOT_LISTED);

        Portfolio updatedPortfolio = doPost(UPDATE_PORTFOLIO_URL, portfolio, Portfolio.class);

        assertEquals(Visibility.NOT_LISTED, updatedPortfolio.getVisibility());
    }

    @Test
    public void copyPortfolio() {
        login(USER_PEETER);

        Portfolio portfolio = new Portfolio();
        portfolio.setId(101L);

        Portfolio copiedPortfolio = doPost(PORTFOLIO_COPY_URL, portfolio, Portfolio.class);

        assertNotNull(copiedPortfolio);
        assertEquals(Long.valueOf(2), copiedPortfolio.getCreator().getId());
        assertEquals(Long.valueOf(6), copiedPortfolio.getOriginalCreator().getId());
    }

    @Test
    public void copyPrivatePortfolioNotLoggedIn() {
        Portfolio portfolio = new Portfolio();
        portfolio.setId(107L);

        Response response = doPost(PORTFOLIO_COPY_URL, Entity.entity(portfolio, MediaType.APPLICATION_JSON_TYPE));
        assertEquals(Status.UNAUTHORIZED.getStatusCode(), response.getStatus());
    }

    @Test
    public void copyPrivatePortfolioLoggedInAsNotCreator() {
        login(USER_MATI);

        Portfolio portfolio = new Portfolio();
        portfolio.setId(107L);

        Response response = doPost(PORTFOLIO_COPY_URL, Entity.entity(portfolio, MediaType.APPLICATION_JSON_TYPE));
        assertEquals(Status.INTERNAL_SERVER_ERROR.getStatusCode(), response.getStatus());
    }

    @Test
    public void copyPrivatePortfolioLoggedInAsCreator() {
        login(USER_PEETER);
        Long userId = 2L;

        Portfolio portfolio = new Portfolio();
        portfolio.setId(107L);

        Response response = doPost(PORTFOLIO_COPY_URL, Entity.entity(portfolio, MediaType.APPLICATION_JSON_TYPE));
        assertEquals(Status.OK.getStatusCode(), response.getStatus());

        Portfolio copiedPortfolio = response.readEntity(Portfolio.class);
        assertEquals(userId, copiedPortfolio.getOriginalCreator().getId());
    }

    @Test
    public void deletePortfolioAsCreator() {
        login(USER_SECOND);

        Portfolio portfolio = new Portfolio();
        portfolio.setId(112L);

        Response response = doPost(DELETE_PORTFOLIO_URL, Entity.entity(portfolio, MediaType.APPLICATION_JSON_TYPE));
        assertEquals(Status.NO_CONTENT.getStatusCode(), response.getStatus());
    }

    @Test
    public void deletePortfolioAsAdmin() {
        login(USER_ADMIN);

        Portfolio portfolio = new Portfolio();
        portfolio.setId(113L);

        Response response = doPost(DELETE_PORTFOLIO_URL, Entity.entity(portfolio, MediaType.APPLICATION_JSON_TYPE));
        assertEquals(Status.NO_CONTENT.getStatusCode(), response.getStatus());
    }

    @Test
    public void deletePortfolioAsNotCreator() {
        login(USER_SECOND);

        Portfolio portfolio = new Portfolio();
        portfolio.setId(101L);

        Response response = doPost(DELETE_PORTFOLIO_URL, Entity.entity(portfolio, MediaType.APPLICATION_JSON_TYPE));
        assertEquals(Status.INTERNAL_SERVER_ERROR.getStatusCode(), response.getStatus());
    }

    @Test
    public void deletePortfolioNotLoggedIn() {
        Portfolio portfolio = new Portfolio();
        portfolio.setId(101L);

        Response response = doPost(DELETE_PORTFOLIO_URL, Entity.entity(portfolio, MediaType.APPLICATION_JSON_TYPE));
        assertEquals(Status.UNAUTHORIZED.getStatusCode(), response.getStatus());
    }

    @Test
    public void recommendPortfolio() {
        long portfolioId = 103L;

        login(USER_PEETER);
        Portfolio portfolio = getPortfolio(portfolioId);
        Response response = doPost(PORTFOLIO_ADD_RECOMMENDATION_URL,
                Entity.entity(portfolio, MediaType.APPLICATION_JSON_TYPE));
        assertEquals(Status.FORBIDDEN.getStatusCode(), response.getStatus());
        logout();

        login(USER_ADMIN);
        Response responseAdmin = doPost(PORTFOLIO_ADD_RECOMMENDATION_URL,
                Entity.entity(portfolio, MediaType.APPLICATION_JSON_TYPE));
        assertEquals(Status.OK.getStatusCode(), responseAdmin.getStatus());
        assertNotNull(responseAdmin.readEntity(Recommendation.class));

        Portfolio portfolioAfterRecommend = getPortfolio(portfolioId);
        assertNotNull(portfolioAfterRecommend.getRecommendation());
    }

    @Test
    public void removedPortfolioRecommendation() {
        long portfolioId = 103L;

        login(USER_PEETER);
        Portfolio portfolio = getPortfolio(portfolioId);
        Response response = doPost(PORTFOLIO_ADD_RECOMMENDATION_URL,
                Entity.entity(portfolio, MediaType.APPLICATION_JSON_TYPE));
        assertEquals(Status.FORBIDDEN.getStatusCode(), response.getStatus());
        logout();

        login(USER_ADMIN);
        Response responseAdmin = doPost(PORTFOLIO_ADD_RECOMMENDATION_URL,
                Entity.entity(portfolio, MediaType.APPLICATION_JSON_TYPE));
        assertEquals(Status.OK.getStatusCode(), responseAdmin.getStatus());
        assertNotNull(responseAdmin.readEntity(Recommendation.class));

        Portfolio portfolioAfterRecommend = getPortfolio(portfolioId);
        assertNotNull(portfolioAfterRecommend.getRecommendation());
        logout();

        login(USER_PEETER);
        Response responseRemoveRecommendation = doPost(PORTFOLIO_REMOVE_RECOMMENDATION_URL,
                Entity.entity(portfolio, MediaType.APPLICATION_JSON_TYPE));
        assertEquals(Status.FORBIDDEN.getStatusCode(), responseRemoveRecommendation.getStatus());
        logout();

        login(USER_ADMIN);
        Response responseRemoveRecommendationAdmin = doPost(PORTFOLIO_REMOVE_RECOMMENDATION_URL,
                Entity.entity(portfolio, MediaType.APPLICATION_JSON_TYPE));
        assertEquals(Status.NO_CONTENT.getStatusCode(), responseRemoveRecommendationAdmin.getStatus());

        Portfolio portfolioAfterRemoveRecommend = getPortfolio(portfolioId);
        assertNull(portfolioAfterRemoveRecommend.getRecommendation());
    }

    @Test
    public void createWithContent() {
        login(USER_MATI);

        Portfolio portfolio = new Portfolio();
        portfolio.setTitle("With chapters");

        List<Chapter> chapters = new ArrayList<>();
        Chapter firstChapter = new Chapter();
        firstChapter.setTitle("First chapter");
        firstChapter.setText("Description text");

        List<LearningObject> learningObjects = new ArrayList<>();
        ChapterObject chapterObject = new ChapterObject();
        chapterObject.setText("Random textbox content");
        learningObjects.add(chapterObject);

        Material material = new Material();
        material.setSource("http://www.november.juliet.ru");

        Response createMaterialResponse = doPut(CREATE_MATERIAL_URL, Entity.entity(material, MediaType.APPLICATION_JSON_TYPE));
        Material createdMaterial = createMaterialResponse.readEntity(Material.class);
        learningObjects.add(createdMaterial);

        List<ContentRow> contentRows = new ArrayList<>();
        contentRows.add(new ContentRow(learningObjects));

        firstChapter.setContentRows(contentRows);
        chapters.add(firstChapter);
        portfolio.setChapters(chapters);

        Portfolio createdPortfolio = doPost(CREATE_PORTFOLIO_URL, portfolio, Portfolio.class);

        assertNotNull(createdPortfolio);
        assertNotNull(createdPortfolio.getId());
        assertEquals(((ChapterObject) createdPortfolio.getChapters().get(0).getContentRows().get(0).getLearningObjects().get(0)).getText(), chapterObject.getText());
        assertEquals(((Material) createdPortfolio.getChapters().get(0).getContentRows().get(0).getLearningObjects().get(1)).getSource(), createdMaterial.getSource());
    }

    private Portfolio createPortfolio() {
        Portfolio portfolio = new Portfolio();
        portfolio.setTitle("Tere");

        return doPost(CREATE_PORTFOLIO_URL, portfolio, Portfolio.class);
    }

    private Portfolio getPortfolio(long id) {
        return doGet(format(GET_PORTFOLIO_URL, id), Portfolio.class);
    }

    private void assertPortfolio101(Portfolio portfolio) {
        assertNotNull(portfolio);
        assertEquals(Long.valueOf(101), portfolio.getId());
        assertEquals("The new stock market", portfolio.getTitle());
        assertEquals(new DateTime("2000-12-29T08:00:01.000+02:00"), portfolio.getAdded());
        assertEquals(new DateTime("2004-12-29T08:00:01.000+02:00"), portfolio.getUpdated());
        assertEquals("Mathematics", portfolio.getTaxons().get(0).getName());
        assertEquals(new Long(6), portfolio.getCreator().getId());
        assertEquals("mati.maasikas-vaarikas", portfolio.getCreator().getUsername());
        assertEquals(new Long(5), portfolio.getOriginalCreator().getId());
        assertEquals("The changes after 2008.", portfolio.getSummary());
        assertEquals(new Long(95455215), portfolio.getViews());
        assertEquals(5, portfolio.getTags().size());

        List<Chapter> chapters = portfolio.getChapters();
        assertEquals(3, chapters.size());
        Chapter chapter = chapters.get(0);
        assertEquals(new Long(1), chapter.getId());
        assertEquals("The crisis", chapter.getTitle());
        assertNull(chapter.getText());
        List<LearningObject> materials = chapter.getContentRows().get(0).getLearningObjects();
        assertEquals(1, materials.size());
        assertEquals(new Long(1), materials.get(0).getId());
        assertEquals(2, chapter.getSubchapters().size());
        Chapter subchapter1 = chapter.getSubchapters().get(0);
        assertEquals(new Long(4), subchapter1.getId());
        assertEquals("Subprime", subchapter1.getTitle());
        assertNull(subchapter1.getText());
        materials = subchapter1.getContentRows().get(0).getLearningObjects();
        assertEquals(1, materials.size());
        assertEquals(new Long(8), materials.get(0).getId());
        Chapter subchapter2 = chapter.getSubchapters().get(1);
        assertEquals(new Long(5), subchapter2.getId());
        assertEquals("The big crash", subchapter2.getTitle());
        assertEquals("Bla bla bla\nBla bla bla bla bla bla bla", subchapter2.getText());
        materials = subchapter2.getContentRows().get(0).getLearningObjects();
        assertEquals(1, materials.size());
        assertEquals(new Long(3), materials.get(0).getId());

        chapter = chapters.get(1);
        assertEquals(new Long(3), chapter.getId());
        assertEquals("Chapter 2", chapter.getTitle());
        assertEquals("Paragraph 1\n\nParagraph 2\n\nParagraph 3\n\nParagraph 4", chapter.getText());
        assertEquals(1, chapter.getContentRows().get(0).getLearningObjects().size());
        assertEquals(0, chapter.getSubchapters().size());

        chapter = chapters.get(2);
        assertEquals(new Long(2), chapter.getId());
        assertEquals("Chapter 3", chapter.getTitle());
        assertEquals("This is some text that explains what is the Chapter 3 about.\nIt can have many lines\n\n\n"
                + "And can also have    spaces   betwenn    the words on it", chapter.getText());
        assertEquals(1, chapter.getContentRows().get(0).getLearningObjects().size());
        assertEquals(0, chapter.getSubchapters().size());

        assertEquals(2, portfolio.getTargetGroups().size());
        assertTrue(TargetGroupEnum.containsTargetGroup(portfolio.getTargetGroups(), TargetGroupEnum.ZERO_FIVE));
        assertTrue(TargetGroupEnum.containsTargetGroup(portfolio.getTargetGroups(), TargetGroupEnum.SIX_SEVEN));
        assertEquals("Lifelong_learning_and_career_planning", portfolio.getCrossCurricularThemes().get(0).getName());
        assertEquals("Cultural_and_value_competence", portfolio.getKeyCompetences().get(0).getName());
        assertEquals(Visibility.PUBLIC, portfolio.getVisibility());
        assertFalse(portfolio.isDeleted());

        Recommendation recommendation = portfolio.getRecommendation();
        assertNotNull(recommendation);
        assertEquals(Long.valueOf(3), recommendation.getId());
    }
}
