package ee.hm.dop.rest;

import ee.hm.dop.model.Comment;
import ee.hm.dop.model.LearningObject;
import ee.hm.dop.model.Material;
import ee.hm.dop.model.Portfolio;
import ee.hm.dop.model.enums.RoleString;
import ee.hm.dop.service.useractions.CommentService;

import javax.annotation.security.RolesAllowed;
import javax.inject.Inject;
import javax.ws.rs.Consumes;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;

@Path("comment")
@RolesAllowed({ RoleString.USER, RoleString.ADMIN, RoleString.MODERATOR })
public class CommentResource extends BaseResource {

    @Inject
    private CommentService commentService;

    @POST
    @Consumes("application/json")
    @Produces("application/json")
    public LearningObject addComment(AddComment form) {
        return commentService.addComment(form.getComment(), form.getLearningObject(), getLoggedInUser());
    }

    @POST
    @Path("portfolio")
    @Consumes("application/json")
    public void addPortfolioComment(AddCommentForm form) {
        Comment comment = form.getComment();
        commentService.addComment(comment, form.getPortfolio(), getLoggedInUser());
    }

    @POST
    @Path("material")
    @Consumes("application/json")
    public void addMaterialComment(AddCommentForm form) {
        Comment comment = form.getComment();
        commentService.addComment(comment, form.getMaterial(), getLoggedInUser());
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

    public static class AddComment {
        private Comment comment;
        private LearningObject learningObject;

        public Comment getComment() {
            return comment;
        }

        public void setComment(Comment comment) {
            this.comment = comment;
        }

        public LearningObject getLearningObject() {
            return learningObject;
        }

        public void setLearningObject(LearningObject learningObject) {
            this.learningObject = learningObject;
        }
    }

}
