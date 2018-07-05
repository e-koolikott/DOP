package ee.hm.dop.rest.useractions;

import ee.hm.dop.common.test.ResourceIntegrationTestBase;
import ee.hm.dop.model.Portfolio;
import ee.hm.dop.model.TagUpVote;
import ee.hm.dop.model.enums.Visibility;
import ee.hm.dop.rest.TagUpVoteResource.TagUpVoteForm;
import org.junit.Test;

import javax.ws.rs.core.GenericType;
import javax.ws.rs.core.Response;
import java.util.List;

import static org.junit.Assert.*;

public class TagUpVoteResourceTest extends ResourceIntegrationTestBase {

    public static final String TAG_UP_VOTES = "tagUpVotes";
    public static final String DELETE_TAG_UP_VOTES = "tagUpVotes/delete";
    public static final String MATEMAATIKA = "matemaatika";
    public static final String NOT_EXISTING_TAG = "keemia";
    public static final String REPORT = "tagUpVotes/report?learningObject=";

    @Test
    public void upVote() {
        login(USER_SECOND);

        TagUpVote tagUpVote = new TagUpVote();
        tagUpVote.setTag(tag(MATEMAATIKA));
        tagUpVote.setLearningObject(materialWithId(MATERIAL_1));

        TagUpVote returnedTagUpVote = doPut(TAG_UP_VOTES, tagUpVote, TagUpVote.class);

        assertNotNull(returnedTagUpVote);
        assertNotNull(returnedTagUpVote.getId());
        assertNull(returnedTagUpVote.getUser().getIdCode());
        assertEquals(MATEMAATIKA, returnedTagUpVote.getTag().getName());
        assertEquals(MATERIAL_1, returnedTagUpVote.getLearningObject().getId());

        Response response = doPost(DELETE_TAG_UP_VOTES, returnedTagUpVote);
        assertEquals(Response.Status.NO_CONTENT.getStatusCode(), response.getStatus());
    }

    @Test
    public void reportNotLoggedIn() {
        List<TagUpVoteForm> tagUpVoteForms = doGet(REPORT + MATERIAL_1, list());

        assertEquals(5, tagUpVoteForms.size());

        for (TagUpVoteForm form : tagUpVoteForms) {
            assertNotNull(form.getTag());
            assertNull(form.getTagUpVote());

            if (form.getTag().getId() == 1) {
                assertEquals(1, form.getUpVoteCount());
            } else {
                assertEquals(0, form.getUpVoteCount());
            }
        }
    }

    @Test
    public void report() {
        login(USER_MATI);

        Response response = doGet(REPORT + MATERIAL_1);
        List<TagUpVoteForm> tagUpVoteForms = response.readEntity(list());

        assertEquals(5, tagUpVoteForms.size());

        for (TagUpVoteForm form : tagUpVoteForms) {
            assertNotNull(form.getTag());

            if (form.getTag().getId() == 1) {
                assertEquals(1, form.getUpVoteCount());
                assertNotNull(form.getTagUpVote());
                assertEquals(Long.valueOf(2), form.getTagUpVote().getId());
            } else {
                assertEquals(0, form.getUpVoteCount());
                assertNull(form.getTagUpVote());
            }
        }
    }

    @Test
    public void reportNoLearningObject() {
        Response response = doGet(REPORT + NOT_EXISTS_ID);
        List<TagUpVoteForm> tagUpVoteForms = response.readEntity(list());

        assertEquals(0, tagUpVoteForms.size());
    }

    @Test
    public void getTagUpVotesNoTags() {
        Response response = doGet(REPORT + MATERIAL_3);
        List<TagUpVoteForm> tagUpVoteForms = response.readEntity(list());

        assertEquals(0, tagUpVoteForms.size());
    }

    @Test
    public void removeUpVote() {
        login(USER_SECOND);

        Portfolio portfolio = new Portfolio();
        portfolio.setId(PORTFOLIO_1);
        portfolio.setVisibility(Visibility.PUBLIC);

        TagUpVote tagUpVote = new TagUpVote();
        tagUpVote.setTag(tag(MATEMAATIKA));
        tagUpVote.setLearningObject(portfolio);

        TagUpVote returnedTagUpVote = doPut(TAG_UP_VOTES, tagUpVote, TagUpVote.class);

        assertNotNull(returnedTagUpVote);
        assertNotNull(returnedTagUpVote.getId());

        Response response = doPost(DELETE_TAG_UP_VOTES, returnedTagUpVote);
        assertEquals(Response.Status.NO_CONTENT.getStatusCode(), response.getStatus());
    }

    @Test
    public void can_not_remove_tagUpVote_that_does_not_exist() throws Exception {
        login(USER_SECOND);
        Response response = doPost(DELETE_TAG_UP_VOTES, null);
        assertEquals("No tagUpVote", Response.Status.NOT_FOUND.getStatusCode(), response.getStatus());
    }

    @Test
    public void can_not_get_tagUpVote_without_learningObject_id() throws Exception {
        login(USER_SECOND);
        Response response = doGet(REPORT);
        assertEquals("LearningObject query param is required", Response.Status.BAD_REQUEST.getStatusCode(), response.getStatus());
    }

    @Test
    public void can_not_insert_tagUpVote_to_tag_that_does_not_exist() throws Exception {
        login(USER_SECOND);

        TagUpVote tagUpVote = new TagUpVote();
        tagUpVote.setTag(tag(NOT_EXISTING_TAG));
        tagUpVote.setLearningObject(portfolioWithId(PORTFOLIO_1));

        Response response = doPut(TAG_UP_VOTES, tagUpVote);
        assertEquals("No tag", Response.Status.BAD_REQUEST.getStatusCode(), response.getStatus());
    }

    @Test
    public void can_not_insert_tagUpVote_that_already_exists() throws Exception {
        login(USER_SECOND);

        Portfolio portfolio = new Portfolio();
        portfolio.setId(PORTFOLIO_1);
        portfolio.setVisibility(Visibility.PUBLIC);

        TagUpVote tagUpVote = new TagUpVote();
        tagUpVote.setTag(tag(MATEMAATIKA));
        tagUpVote.setLearningObject(portfolio);

        TagUpVote returnedTagUpVote = doPut(TAG_UP_VOTES, tagUpVote, TagUpVote.class);
        Response response = doPut(TAG_UP_VOTES, returnedTagUpVote);
        assertEquals("TagUpVote already exists", Response.Status.BAD_REQUEST.getStatusCode(), response.getStatus());

        doPost(DELETE_TAG_UP_VOTES, returnedTagUpVote);
    }

    @Test
    public void upVoteGettingPrivatePortfolio() {
        login(USER_SECOND);

        TagUpVote tagUpVote = new TagUpVote();
        tagUpVote.setTag(tag(MATEMAATIKA));
        tagUpVote.setLearningObject(portfolioWithId(PORTFOLIO_10));

        Response response = doPut(TAG_UP_VOTES, tagUpVote);
        assertEquals(Response.Status.BAD_REQUEST.getStatusCode(), response.getStatus());
    }

    private GenericType<List<TagUpVoteForm>> list() {
        return new GenericType<List<TagUpVoteForm>>() {
        };
    }
}
