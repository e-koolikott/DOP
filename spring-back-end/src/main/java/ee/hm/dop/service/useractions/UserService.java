package ee.hm.dop.service.useractions;

import com.google.common.collect.Lists;
import ee.hm.dop.dao.UserDao;
import ee.hm.dop.model.*;
import ee.hm.dop.model.enums.Role;
import ee.hm.dop.model.enums.Visibility;
import ee.hm.dop.model.taxon.Taxon;
import ee.hm.dop.service.content.LearningObjectService;
import ee.hm.dop.service.content.MediaService;
import ee.hm.dop.service.files.PictureService;
import ee.hm.dop.service.metadata.TaxonService;
import ee.hm.dop.utils.UserUtil;
import org.apache.commons.text.WordUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.inject.Inject;
import java.text.Normalizer;
import java.util.List;
import java.util.stream.Collectors;

import static java.lang.String.format;

@Service
@Transactional
public class UserService {

    private static final Logger logger = LoggerFactory.getLogger(UserService.class);

    @Inject
    private UserDao userDao;
    @Inject
    private TaxonService taxonService;
    @Inject
    private MediaService mediaService;
    @Inject
    private LearningObjectService learningObjectService;
    @Inject
    private PictureService pictureService;

    public User getUserByIdCode(String idCode) {
        return userDao.findUserByIdCode(idCode);
    }

    public User getUserByUsername(String username) {
        return userDao.findUserByUsername(username);
    }

    public User create(String idCode, String name, String surname) {
        User user = new User();
        user.setIdCode(idCode);
        user.setName(name);
        user.setSurname(surname);
        return create(user);
    }

    public synchronized User create(User user) {
        user.setName(WordUtils.capitalizeFully(user.getName(), ' ', '-'));
        user.setSurname(WordUtils.capitalizeFully(user.getSurname(), ' ', '-'));
        String generatedUsername = generateUsername(user.getName(), user.getSurname());
        user.setUsername(generatedUsername);
        user.setRole(Role.USER);

        logger.info(format("Creating user: username = %s; name = %s; surname = %s; idCode = %s", user.getUsername(),
                user.getName(), user.getSurname(), user.getIdCode()));

        return userDao.createOrUpdate(user);
    }

    // Only users with role 'USER' can be restricted
    public User restrictUser(User user) {
        return setRole(user, Role.USER, Role.RESTRICTED);
    }

    //Only users with role 'RESTRICTED' can be set to role 'USER'
    public User removeRestriction(User user) {
        return setRole(user, Role.RESTRICTED, Role.USER);
    }

    public List<User> getModerators(User loggedInUser) {
        if (UserUtil.isAdmin(loggedInUser)) {
            return userDao.getUsersByRole(Role.MODERATOR);
        } else if (UserUtil.isModerator(loggedInUser)) {
            User byId = userDao.findById(loggedInUser.getId());
            return Lists.newArrayList(byId);
        } else  {
            return null;
        }
    }

    public Long getModeratorsCount(User loggedInUser) {
        return UserUtil.isAdmin(loggedInUser) ? userDao.getUsersCountByRole(Role.MODERATOR) : null;
    }

    public List<User> getRestrictedUsers(User loggedInUser) {
        return UserUtil.isAdmin(loggedInUser) ? userDao.getUsersByRole(Role.RESTRICTED) : null;
    }

    public Long getRestrictedUsersCount(User loggedInUser) {
        return UserUtil.isAdmin(loggedInUser) ? userDao.getUsersCountByRole(Role.RESTRICTED) : null;
    }

    public List<User> getAllUsers(User loggedInUser) {
        return UserUtil.isAdmin(loggedInUser) ? userDao.findAll() : null;
    }

    public String generateUsername(String name, String surname) {
        String username = name.trim().toLowerCase() + "." + surname.trim().toLowerCase();
        username = username.replaceAll("\\s+", ".");

        // Normalize the username and remove all non-ascii characters
        username = Normalizer.normalize(username, Normalizer.Form.NFD);
        username = username.replaceAll("[^\\p{ASCII}]", "");

        Long count = userDao.countUsersWithSameUsername(username);
        if (count > 0) {
            username += String.valueOf(count + 1);
        }
        return username;
    }

    private User setUserRole(User user, Role newRole) {
        user.setRole(newRole);
        logger.info(format("Setting user %s, with id code %s role to: %s", user.getUsername(), user.getIdCode(), newRole.toString()));
        return userDao.createOrUpdate(user);
    }

    public User update(User user, User loggedInUser) {
        UserUtil.mustBeAdmin(loggedInUser);

        User existingUser = getUserByUsername(user.getUsername());
        existingUser.setRole(Role.valueOf(user.getRole().toString()));

        List<Long> ids = user.getUserTaxons().stream().map(Taxon::getId).collect(Collectors.toList());
        List<Taxon> taxons = taxonService.getTaxonById(ids);

        existingUser.setUserTaxons(taxons);
        return userDao.createOrUpdate(existingUser);
    }

    public User updateUserLocation(User loggedInUser, String userLocation) {
        User existingUser = getUserById(loggedInUser.getId());
        existingUser.setLocation(userLocation);
        return userDao.createOrUpdate(existingUser);
    }

    private User getUserById(Long id) {
        return userDao.findById(id);
    }

    private User setRole(User user, Role from, Role to) {
        user = getUserByUsername(user.getUsername());
        return user.getRole().equals(from) ? setUserRole(user, to) : null;
    }

    public boolean areLicencesAcceptable(String username) {
        List<Media> allUserMedia = mediaService.getAllByCreator(getUserByUsername(username));
        List<LearningObject> allUserLearningObjects = learningObjectService.getAllByCreator(getUserByUsername(username));
        long unAcceptableMediaCount = allUserMedia.stream()
                .filter(this::mediaHasUnAcceptableLicence).count();
        long unAcceptableLearningObjectsCount = allUserLearningObjects.stream()
                .filter(this::learningObjectHasUnAcceptableLicence)
                .count();
        long unAcceptablePictureCount = allUserLearningObjects.stream()
                .filter(lo -> lo.getPicture() != null)
                .filter(lo -> pictureHasUnAcceptableLicence(lo.getPicture()))
                .count();
        return (unAcceptableLearningObjectsCount == 0) &&
                (unAcceptableMediaCount == 0) &&
                (unAcceptablePictureCount == 0);
    }

    public void setLearningObjectsPrivate(User user) {
        learningObjectService.getAllByCreator(user)
                .stream()
                .filter(lo -> learningObjectHasUnAcceptableLicence(lo) ||
                        (lo.getPicture() != null && pictureHasUnAcceptableLicence(lo.getPicture())))
                .forEach(learningObject -> learningObject.setVisibility(Visibility.PRIVATE));
    }

    private boolean learningObjectHasUnAcceptableLicence(LearningObject lo) {
        return !lo.getLicenseType().getName().equals("CCBY") &&
                !lo.getLicenseType().getName().equals("CCBYSA");
    }

    private boolean mediaHasUnAcceptableLicence(Media media) {
        return !media.getLicenseType().getName().equals("CCBY") &&
                !media.getLicenseType().getName().equals("CCBYSA");
    }

    private boolean pictureHasUnAcceptableLicence(Picture picture) {
        LicenseType pictureLicenceType = pictureService.getLicenceTypeById(picture.getId());
        return !pictureLicenceType.getName().equals("CCBY") &&
                !pictureLicenceType.getName().equals("CCBYSA");
    }
}
