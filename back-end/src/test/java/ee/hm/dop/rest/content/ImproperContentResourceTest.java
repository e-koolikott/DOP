package ee.hm.dop.rest.content;

import com.google.common.collect.Lists;
import ee.hm.dop.common.test.ResourceIntegrationTestBase;
import ee.hm.dop.model.ImproperContent;
import ee.hm.dop.model.Material;
import ee.hm.dop.model.ReportingReason;
import ee.hm.dop.model.enums.ReportingReasonEnum;
import org.apache.commons.collections.CollectionUtils;
import org.junit.Test;

import javax.ws.rs.core.GenericType;
import javax.ws.rs.core.Response;
import java.util.List;

import static ee.hm.dop.model.enums.ReportingReasonEnum.LO_CONTENT;
import static ee.hm.dop.model.enums.ReportingReasonEnum.LO_FORM;
import static java.lang.String.format;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

public class ImproperContentResourceTest extends ResourceIntegrationTestBase {

    public static final String IMPROPERS = "impropers";
    public static final String GET_IMPROPERS_BY_ID = "impropers/%s";

    @Test
    public void setImproperNoData() {
        login(USER_SECOND);
        Response response = doPut(IMPROPERS, new ImproperContent());
        assertEquals(Response.Status.INTERNAL_SERVER_ERROR.getStatusCode(), response.getStatus());
    }

    @Test
    public void can_not_set_material_that_does_not_exist_to_improper() {
        login(USER_SECOND);
        Response response = doPut(IMPROPERS, improperMaterialContent(NOT_EXISTS_ID));
        assertEquals(Response.Status.INTERNAL_SERVER_ERROR.getStatusCode(), response.getStatus());
    }

    @Test
    public void user_can_set_material_improper_with_reporting_reason() throws Exception {
        login(USER_SECOND);
        Response response = doPut(IMPROPERS, improperMaterialContent(MATERIAL_13));
        assertEquals(Response.Status.OK.getStatusCode(), response.getStatus());

        ImproperContent improperContent = doPut(IMPROPERS, improperMaterialContent(MATERIAL_13), ImproperContent.class);
        assertTrue("Improper material has reporting reasons", CollectionUtils.isNotEmpty(improperContent.getReportingReasons()));
        Material improperMaterial = getMaterial(MATERIAL_13);
        assertTrue("Material is improper", improperMaterial.getImproper() > 0);
    }

    @Test
    public void getImpropers() {
        login(USER_SECOND);
        List<ImproperContent> improperContents = doGet(IMPROPERS, genericType());
        assertTrue(CollectionUtils.isNotEmpty(improperContents));
    }

    @Test
    public void getImproperByLearningObject() {
        login(USER_SECOND);

        List<ImproperContent> improperContents = doGet(format(GET_IMPROPERS_BY_ID, PORTFOLIO_3), genericType());
        assertTrue(CollectionUtils.isNotEmpty(improperContents));
        assertEquals(1, improperContents.size());
        assertEquals(new Long(5), improperContents.get(0).getId());
    }

    private GenericType<List<ImproperContent>> genericType() {
        return new GenericType<List<ImproperContent>>() {
        };
    }

    private ImproperContent improperMaterialContent(Long id) {
        ImproperContent improperContent = new ImproperContent();
        improperContent.setLearningObject(materialWithId(id));
        improperContent.setReportingReasons(Lists.newArrayList(reason(LO_CONTENT), reason(LO_FORM)));
        return improperContent;
    }

    private ReportingReason reason(ReportingReasonEnum content) {
        ReportingReason reason = new ReportingReason();
        reason.setReason(content);
        return reason;
    }
}
