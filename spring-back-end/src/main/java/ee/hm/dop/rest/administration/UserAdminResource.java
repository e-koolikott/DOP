package ee.hm.dop.rest.administration;

import ee.hm.dop.model.User;
import ee.hm.dop.model.enums.RoleString;
import ee.hm.dop.rest.BaseResource;
import ee.hm.dop.service.useractions.UserService;
import org.springframework.security.access.annotation.Secured;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.annotation.security.RolesAllowed;
import javax.inject.Inject;
import javax.ws.rs.*;
import javax.ws.rs.core.MediaType;
import java.util.List;

@RestController
@RequestMapping("user")
public class UserAdminResource extends BaseResource {

    @Inject
    private UserService userService;

    @GetMapping
    @RequestMapping("all")
    @Secured(RoleString.ADMIN)
    @Produces(MediaType.APPLICATION_JSON)
    public List<User> getAll() {
        return userService.getAllUsers(getLoggedInUser());
    }

    @PostMapping
    @Secured(RoleString.ADMIN)
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    public User updateUser(User user) {
        mustHaveUser(user);
        return userService.update(user, getLoggedInUser());
    }

    @PostMapping
    @RequestMapping("restrictUser")
    @Secured({RoleString.ADMIN, RoleString.MODERATOR})
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    public User restrictUser(User user) {
        mustHaveUser(user);
        return userService.restrictUser(user);
    }

    @PostMapping
    @RequestMapping("removeRestriction")
    @Secured({RoleString.ADMIN, RoleString.MODERATOR})
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    public User removeRestriction(User user) {
        mustHaveUser(user);
        return userService.removeRestriction(user);
    }

    private void mustHaveUser(User user) {
        if (user == null) throw badRequest("No user received!");
    }
}
