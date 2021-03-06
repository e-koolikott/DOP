package ee.hm.dop.config.cronExecutorsProperties;

import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

@Getter
@Setter
@Component
@ConfigurationProperties(prefix = "cron.accept-reviewable-change")
public class AcceptReviewableChangeProperties {
    private String scheduledTime;
    private boolean enabled;
}
