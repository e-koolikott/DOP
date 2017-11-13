package ee.hm.dop.rest.administration;

import ee.hm.dop.model.AdminLearningObject;
import ee.hm.dop.model.LearningObject;
import ee.hm.dop.model.LearningObjectMiniDto;
import ee.hm.dop.model.enums.ReviewStatus;
import ee.hm.dop.model.enums.ReviewType;
import ee.hm.dop.model.enums.RoleString;
import ee.hm.dop.rest.BaseResource;
import ee.hm.dop.service.reviewmanagement.FirstReviewAdminService;
import ee.hm.dop.service.reviewmanagement.ReviewManager;

import javax.annotation.security.RolesAllowed;
import javax.inject.Inject;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;
import java.util.List;

@Path("admin/firstReview/")
public class FirstReviewAdminResource extends BaseResource {

    @Inject
    private FirstReviewAdminService firstReviewAdminService;
    @Inject
    private ReviewManager reviewManager;

    @GET
    @Path("unReviewed")
    @RolesAllowed({RoleString.ADMIN, RoleString.MODERATOR})
    @Produces(MediaType.APPLICATION_JSON)
    public List<AdminLearningObject> getUnReviewed() {
        return firstReviewAdminService.getUnReviewed(getLoggedInUser());
    }

    @GET
    @Path("unReviewed/count")
    @RolesAllowed({RoleString.ADMIN, RoleString.MODERATOR})
    @Produces(MediaType.APPLICATION_JSON)
    public Long getUnReviewedCount() {
        return firstReviewAdminService.getUnReviewedCount(getLoggedInUser());
    }

    @POST
    @Path("setReviewed")
    @Produces(MediaType.APPLICATION_JSON)
    @RolesAllowed({RoleString.ADMIN, RoleString.MODERATOR})
    public LearningObject setReviewed(LearningObjectMiniDto learningObject) {
        return reviewManager.setEverythingReviewedRefreshLO(getLoggedInUser(), learningObject.convert(), ReviewStatus.ACCEPTED, ReviewType.FIRST);
    }
}
