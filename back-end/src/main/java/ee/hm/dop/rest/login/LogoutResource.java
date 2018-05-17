package ee.hm.dop.rest.login;

import static java.lang.String.format;

import javax.annotation.security.RolesAllowed;
import javax.inject.Inject;
import javax.ws.rs.POST;
import javax.ws.rs.Path;

import ee.hm.dop.model.AuthenticatedUser;
import ee.hm.dop.model.enums.RoleString;
import ee.hm.dop.rest.BaseResource;
import ee.hm.dop.service.login.LogoutService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@Path("logout")
@RolesAllowed({ RoleString.USER, RoleString.ADMIN, RoleString.RESTRICTED, RoleString.MODERATOR })
public class LogoutResource extends BaseResource {

    private static Logger logger = LoggerFactory.getLogger(LogoutResource.class);

    @Inject
    private LogoutService logoutService;

    @POST
    public void logout() {
        AuthenticatedUser authenticatedUser = getAuthenticatedUser();
        logoutService.logout(authenticatedUser);
        logger.info(format("User %s is logged out", authenticatedUser.getUser().getUsername()));
    }
}
