package ee.hm.dop.service.login;

import ee.hm.dop.dao.AgreementDao;
import ee.hm.dop.model.Agreement;
import ee.hm.dop.model.User;
import org.joda.time.DateTime;

import javax.inject.Inject;
import java.util.List;

import static ee.hm.dop.utils.UserUtil.mustBeAdmin;
import static org.apache.commons.collections4.CollectionUtils.isEmpty;
import static org.apache.commons.collections4.CollectionUtils.isNotEmpty;

public class AgreementService {

    @Inject
    private AgreementDao agreementDao;

    public List<Agreement> findAllValid(User user){
        mustBeAdmin(user);
        return agreementDao.getValidAgreements();
    }

    public boolean isValid(Agreement agreement, User user) {
        mustBeAdmin(user);
        if (agreement.getVersion() == null || agreement.getValidFrom() == null){
            return false;
        }
        agreement.setValidFrom(agreement.getValidFrom().withTimeAtStartOfDay());
        return isEmpty(agreementDao.findMatchingAgreements(agreement));
    }

    public Agreement save(Agreement agreement, User user) {
        mustBeAdmin(user);
        if (agreement.getVersion() == null || agreement.getValidFrom() == null){
            throw new RuntimeException("missing data");
        }
        agreement.setValidFrom(agreement.getValidFrom().withTimeAtStartOfDay());
        agreement.setVersion(agreement.getVersion().trim());
        agreement.setCreatedAt(DateTime.now());
        agreement.setCreatedBy(user);
        Agreement newAgreement = agreementDao.createOrUpdate(agreement);
        if (newAgreement != null){
            List<Agreement> previousDeletedAgreements = agreementDao.findMatchingDeletedAgreements(newAgreement);
            if (isNotEmpty(previousDeletedAgreements)){
                agreementDao.updateUserAgreementsForUsersWhoAgreedToPreviousVersion(previousDeletedAgreements, newAgreement);
            }
        }
        return newAgreement;
    }

    public void deleteAgreement(Agreement agreement, User user) {
        mustBeAdmin(user);
        Agreement dbAgreement = agreementDao.findById(agreement.getId());
        if (dbAgreement != null){
            dbAgreement.setDeleted(true);
            agreementDao.createOrUpdate(dbAgreement);
        }
    }
}
