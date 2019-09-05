package ee.hm.dop.service.content;

import ee.hm.dop.dao.PortfolioDao;
import ee.hm.dop.dao.PortfolioLogDao;
import ee.hm.dop.dao.ReducedLearningObjectDao;
import ee.hm.dop.model.*;
import ee.hm.dop.service.permission.PortfolioPermission;
import ee.hm.dop.utils.UserUtil;
import ee.hm.dop.utils.ValidatorUtil;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.inject.Inject;
import java.util.ArrayList;
import java.util.List;
import java.util.function.Function;

import static java.util.stream.Collectors.toList;

@Service
@Transactional
public class PortfolioGetter {

    @Inject
    private PortfolioDao portfolioDao;
    @Inject
    private ReducedLearningObjectDao reducedLearningObjectDao;
    @Inject
    private PortfolioPermission portfolioPermission;
    @Inject
    private PortfolioLogDao portfolioLogDao;
    @Inject
    private LearningObjectService learningObjectService;

    public Portfolio get(Long portfolioId, User loggedInUser) {
        if (UserUtil.isAdminOrModerator(loggedInUser)) {
            Portfolio portfolio = portfolioDao.findById(portfolioId);
            if (portfolio == null) return null;
            learningObjectService.setTaxonPosition(portfolio);
            return portfolio;
        }
        Portfolio portfolio = portfolioDao.findByIdNotDeleted(portfolioId);
        if (!portfolioPermission.canView(loggedInUser, portfolio)) {
            throw ValidatorUtil.permissionError();
        }
        learningObjectService.setTaxonPosition(portfolio);
        return portfolio;
    }

    public SearchResult getByCreatorResult(User creator, User loggedInUser, int start, int maxResults) {
        List<Searchable> searchables = new ArrayList<>(getByCreator(creator, loggedInUser, start, maxResults));
        Long size = getCountByCreator(creator);
        return new SearchResult(searchables, size, start);
    }

    public List<PortfolioLog> getPortfolioHistoryAll(Long portfolioId) {
        return portfolioLogDao.findAllPortfolioLogsByLoId(portfolioId);
    }

    public List<PortfolioLog> findByIdAndCreatorAllPortfolioLogs(Long portfolioId, User user) {
        return portfolioLogDao.findPortfolioLogsByLoIdAndUserId(portfolioId, user);
    }

    public PortfolioLog getPortfolioHistory(long portfolioHistoryId) {
        return portfolioLogDao.findById(portfolioHistoryId);
    }

    public List<ReducedLearningObject> getByCreator(User creator, User loggedInUser, int start, int maxResults) {
        return reducedLearningObjectDao.findPortfolioByCreator(creator, start, maxResults).stream()
                .filter(p -> portfolioPermission.canInteract(loggedInUser, p))
                .collect(toList());
    }

    public Long getCountByCreator(User creator) {
        return portfolioDao.findCountByCreator(creator);
    }

    public Portfolio findValid(Portfolio portfolio) {
        return ValidatorUtil.findValid(portfolio, (Function<Long, Portfolio>) portfolioDao::findByIdNotDeleted);
    }
}
