package ee.hm.dop.dao;

import ee.hm.dop.model.PortfolioLog;

import javax.inject.Inject;
import java.util.List;

import static org.joda.time.DateTime.now;

public class PortfolioLogDao extends AbstractDao<PortfolioLog> {

    public Class<PortfolioLog> entity() {
        return PortfolioLog.class;
    }

    @Inject
    private LearningObjectDao learningObjectDao;

    public PortfolioLog createOrUpdate(PortfolioLog entity) {
        entity.setPublishedAt(now());
        return super.createOrUpdate(entity);
    }

    public List<PortfolioLog> findByIdAllPortfolioLOgs(Long learningObjectId) {
        return getEntityManager()
                .createQuery("" +
                                "SELECT p FROM PortfolioLog p \n" +
                                "   WHERE p.learningObject = :id "
                        , PortfolioLog.class)
                .setParameter("id", learningObjectId)
                .getResultList();

    }

    //    public List<Portfolio> findDeletedPortfolios() {
//        return getEntityManager()
//                .createQuery("SELECT p FROM Portfolio p WHERE p.deleted = true", entity())
//                .getResultList();
//    }


//    public Portfolio findByIdNotDeleted(Long objectId) {
//        TypedQuery<Portfolio> findByCode = getEntityManager()
//                .createQuery("SELECT lo FROM Portfolio lo " +
//                        "WHERE lo.id = :id AND lo.deleted = false", entity())
//                .setParameter("id", objectId);
//
//        return getSingleResult(findByCode);
//    }
//
//    public Portfolio findDeletedById(Long portfolioId) {
//        TypedQuery<Portfolio> findByCode = getEntityManager()
//                .createQuery("SELECT lo FROM Portfolio lo " +
//                        "WHERE lo.id = :id AND lo.deleted = true ", entity())
//                .setParameter("id", portfolioId);
//        return getSingleResult(findByCode);
//    }
//
//
//    public List<Portfolio> findDeletedPortfolios() {
//        return getEntityManager()
//                .createQuery("SELECT p FROM Portfolio p WHERE p.deleted = true", entity())
//                .getResultList();
//    }
//
//    public Long findCountByCreator(User creator) {
//        Query query = getEntityManager()
//                .createQuery("SELECT count(*) FROM Portfolio p WHERE p.creator = :creator AND p.deleted = false")
//                .setParameter("creator", creator);
//        return (Long) query.getSingleResult();
//    }
//
//    /**
//     * Finds all portfolios contained in the idList. There is no guarantee about
//     * in which order the portfolios will be in the result list.
//     *
//     * @param idList the list with portfolio identifiers
//     * @return a list of portfolios specified by idList
//     */
//    public List<LearningObject> findAllById(List<Long> idList) {
//        if (isEmpty(idList)){
//            return new ArrayList<>();
//        }
//        return getEntityManager()
//                .createQuery("SELECT lo FROM LearningObject lo" +
//                        " WHERE type(lo) = Portfolio " +
//                        "AND lo.deleted = false " +
//                        "AND lo.id in :idList", LearningObject.class)
//                .setParameter("idList", idList)
//                .getResultList();
//    }
//
//    public List<LearningObject> findByCreator(User creator, int start, int maxResults) {
//        String queryString = "SELECT lo FROM LearningObject lo WHERE " +
//                "type(lo) = Portfolio AND " +
//                "lo.creator.id = :creatorId AND lo.deleted = false order by added desc";
//        return learningObjectDao.findByCreatorInner(creator, start, maxResults, queryString);
//    }
//
//    public Long findDeletedPortfoliosCount() {
//        return (Long) getEntityManager()
//                .createQuery("SELECT count(*) FROM Portfolio p " +
//                        "WHERE p.deleted = true").getSingleResult();
//    }
//
//    public List<Portfolio> findNewestPortfolios(int numberOfMaterials, int startPosition) {
//        return getEntityManager()
//                .createQuery("FROM Portfolio port WHERE port.deleted = false ORDER BY added DESC, id DESC", entity())
//                .setFirstResult(startPosition)
//                .setMaxResults(numberOfMaterials)
//                .getResultList();
//    }
//
//    public List<Portfolio> getAllPortfoliosDeletedExcluded() {
//        return getEntityManager()
//                .createQuery("SELECT p FROM Portfolio p WHERE p.deleted = false", entity())
//                .getResultList();
//    }
//
//    public void delete(LearningObject learningObject) {
//        learningObjectDao.delete(learningObject);
//    }
//
//    public void restore(LearningObject learningObject) {
//        learningObjectDao.restore(learningObject);
//    }
//
//
//    public List<Portfolio> getRelatedPortfolios(Long materialId) {
//        return getEntityManager().
//                createQuery("SELECT pm.portfolio " +
//                        "FROM PortfolioMaterial pm " +
//                        "WHERE pm.material.id =:materialId " +
//                        "ORDER BY lower(pm.portfolio.title) ASC" , entity())
//                .setParameter("materialId",materialId)
//                .getResultList();
//    }

}
