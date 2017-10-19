package ee.hm.dop.rest.content;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

import javax.ws.rs.core.Response;
import javax.ws.rs.core.Response.Status;

import ee.hm.dop.common.test.ResourceIntegrationTestBase;
import ee.hm.dop.model.Page;
import ee.hm.dop.model.enums.LanguageC;
import org.junit.Test;

public class PageResourceTest extends ResourceIntegrationTestBase {

    @Test
    public void getAboutPageInEstonian() {
        String name = "About";
        String languageCode = LanguageC.EST;
        Page page = doGet("page?name=" + name + "&language=" + languageCode, Page.class);

        assertNotNull(page);
        assertEquals(new Long(1L), page.getId());
        assertEquals("About", page.getName());
        assertEquals("<h1>Meist</h1><p>Tekst siin</p>", page.getContent());
        assertEquals(languageCode, page.getLanguage().getCode());
    }

    @Test
    public void getHelpPageInEnglish() {
        String name = "Help";
        String languageCode = LanguageC.ENG;
        Page page = doGet("page?name=" + name + "&language=" + languageCode, Page.class);

        assertNotNull(page);
        assertEquals(new Long(6), page.getId());
        assertEquals("Help", page.getName());
        assertEquals("<h1>Help</h1><p>Text here</p>", page.getContent());
        assertEquals(languageCode, page.getLanguage().getCode());
    }

    @Test
    public void getPageWithoutParam() {
        Response response = doGet("page");
        assertEquals(Status.BAD_REQUEST.getStatusCode(), response.getStatus());
    }

    @Test
    public void getNotExistingPage() {
        Response response = doGet("page?name=doesnotExist&language=eng");
        assertEquals(Status.NO_CONTENT.getStatusCode(), response.getStatus());
    }

    @Test
    public void getPageWithNotSupportedLanguage() {
        Response response = doGet("page?name=About&language=notSupported");
        assertEquals(Status.BAD_REQUEST.getStatusCode(), response.getStatus());
    }

    @Test
    public void getPageWithBlankName() {
        Response response = doGet("page?name=&language=eng");
        assertEquals(Status.BAD_REQUEST.getStatusCode(), response.getStatus());
    }

    @Test
    public void getPageWithBlankLanguage() {
        Response response = doGet("page?name=About&language=");
        assertEquals(Status.BAD_REQUEST.getStatusCode(), response.getStatus());
    }

}
