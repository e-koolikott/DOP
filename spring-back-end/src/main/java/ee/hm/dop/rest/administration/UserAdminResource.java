package ee.hm.dop.rest.administration;

import ee.hm.dop.dao.UserDao;
import ee.hm.dop.model.User;
import ee.hm.dop.model.enums.RoleString;
import ee.hm.dop.rest.BaseResource;
import ee.hm.dop.service.useractions.UserService;
import org.springframework.security.access.annotation.Secured;
import org.springframework.web.bind.annotation.*;

import javax.inject.Inject;
import java.util.List;

@RestController
@RequestMapping("user")
public class UserAdminResource extends BaseResource {

    @Inject
    private UserService userService;
    @Inject
    private UserDao userDao;

    @GetMapping
    @RequestMapping("all")
    @Secured(RoleString.ADMIN)
    public List<User> getAll() {
        return userService.getAllUsers(getLoggedInUser());
    }

    @PostMapping
    @Secured(RoleString.ADMIN)
    public User updateUser(@RequestBody User user) {
        mustHaveUser(user);
        return userService.update(user, getLoggedInUser());
    }

    @PostMapping
    @RequestMapping("restrictUser")
    @Secured({RoleString.ADMIN, RoleString.MODERATOR})
    public User restrictUser(@RequestBody User user) {
        mustHaveUser(user);
        return userService.restrictUser(user);
    }

    @PostMapping
    @RequestMapping("removeRestriction")
    @Secured({RoleString.ADMIN, RoleString.MODERATOR})
    public User removeRestriction(@RequestBody User user) {
        mustHaveUser(user);
        return userService.removeRestriction(user);
    }

    @GetMapping
    @RequestMapping("getUser")
    @Secured({RoleString.ADMIN})
    public User getUser (@RequestParam("id") Long userId) {
        return userDao.findUserById(userId);
    }

    private void mustHaveUser(User user) {
        if (user == null) throw badRequest("No user received!");
    }
}
