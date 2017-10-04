package ee.hm.dop.rest;

import javax.annotation.security.RolesAllowed;
import javax.inject.Inject;
import javax.ws.rs.Consumes;
import javax.ws.rs.POST;
import javax.ws.rs.Path;

import ee.hm.dop.model.*;
import ee.hm.dop.model.enums.RoleString;
import ee.hm.dop.service.content.MaterialService;
import ee.hm.dop.service.content.PortfolioService;

@Path("comment")
@RolesAllowed({ RoleString.USER, RoleString.ADMIN, RoleString.MODERATOR })
public class CommentResource extends BaseResource {

    @Inject
    private PortfolioService portfolioService;
    @Inject
    private MaterialService materialService;

    @POST
    @Path("portfolio")
    @Consumes("application/json")
    public void addPortfolioComment(AddCommentForm form) {
        Comment comment = form.getComment();
        User loggedInUser = getLoggedInUser();
        comment.setCreator(loggedInUser);

        portfolioService.addComment(comment, form.getPortfolio(), loggedInUser);
    }

    @POST
    @Path("material")
    @Consumes("application/json")
    public void addMaterialComment(AddCommentForm form) {
        Comment comment = form.getComment();
        comment.setCreator(getLoggedInUser());

        materialService.addComment(comment, form.getMaterial());
    }

    public static class AddCommentForm {
        private Comment comment;
        private Portfolio portfolio;
        private Material material;

        public Comment getComment() {
            return comment;
        }

        public void setComment(Comment comment) {
            this.comment = comment;
        }

        public Portfolio getPortfolio() {
            return portfolio;
        }

        public void setPortfolio(Portfolio portfolio) {
            this.portfolio = portfolio;
        }

        public Material getMaterial() {
            return material;
        }

        public void setMaterial(Material material) {
            this.material = material;
        }
    }

}
