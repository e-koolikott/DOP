package ee.hm.dop.service.useractions;

import ee.hm.dop.dao.CustomerSupportDao;
import ee.hm.dop.model.CustomerSupport;
import ee.hm.dop.model.User;
import ee.hm.dop.service.MailBuilder;
import ee.hm.dop.service.MailSender;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.validator.routines.EmailValidator;

import java.time.LocalDateTime;

import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import javax.inject.Inject;

import static org.apache.commons.lang3.StringUtils.isBlank;
import static org.apache.commons.lang3.StringUtils.isNotBlank;

@Service
@Transactional
public class CustomerSupportService {

    @Inject
    private CustomerSupportDao customerSupportDao;
    @Inject
    private MailSender mailSender;
    @Inject
    private MailBuilder mailBuilder;

    public CustomerSupport save(CustomerSupport customerSupport, User user) {

        customerSupport.setName(validateName(customerSupport));
        customerSupport.setEmail(validateEmail(customerSupport));
        if (isBlank(customerSupport.getSubject())) throw badRequest("Subject is empty");
        if (isBlank(customerSupport.getMessage())) throw badRequest("Message is empty");

        customerSupport.setCreatedAt(LocalDateTime.now());
        customerSupport.setUser(user);
        customerSupport.setSentTries(0);

        if (mailSender.sendEmail(mailBuilder.composeEmailToUser(customerSupport))) {
            mailSender.sendEmail(mailBuilder.composeEmailToSupport(customerSupport));
            customerSupport.setSentAt(LocalDateTime.now());
            customerSupport.setSentSuccessfully(true);
            customerSupport.setSentTries(1);
            customerSupport.setErrorMessage("Sent successfully");
        } else {
            customerSupport.setSentTries(2);
            customerSupport.setSentAt(LocalDateTime.now());
            customerSupport.setErrorMessage("Failed to send mail to user");
            if (!mailSender.sendEmail(mailBuilder.composeEmailToSupportWhenSendFailed(customerSupport))) {
                customerSupport.setErrorMessage("Failed to send email to HITSA support");
                customerSupport.setSentTries(3);
            }
        }

        return customerSupportDao.createOrUpdate(customerSupport);
    }

    private ResponseStatusException badRequest(String s) {
        return new ResponseStatusException(HttpStatus.BAD_REQUEST, s);
    }

    private String validateName(CustomerSupport customerSupport) {
        if (isNotBlank(customerSupport.getName())) {
            return StringUtils.normalizeSpace(customerSupport.getName());
        }
        throw badRequest("Name is missing");
    }

    private String validateEmail(CustomerSupport customerSupport) {
        if (isNotBlank(customerSupport.getEmail())) {
            String email = StringUtils.trim(customerSupport.getEmail());
            if (EmailValidator.getInstance().isValid(email)) {
                return email;
            }
            throw badRequest("Invalid email address");
        }
        throw badRequest("Email is empty");
    }
}
