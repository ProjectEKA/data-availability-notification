package in.projecteka.datanotificationsubscription;

import in.projecteka.datanotificationsubscription.common.RabbitMQOptions;
import in.projecteka.datanotificationsubscription.common.model.AccountServiceProperties;
import in.projecteka.datanotificationsubscription.subscription.model.SubscriptionProperties;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.scheduling.annotation.EnableScheduling;

@SpringBootApplication
@EnableScheduling
@EnableAsync
@EnableConfigurationProperties({RabbitMQOptions.class,
                                DbOptions.class,
                                IdentityServiceProperties.class,
                                GatewayServiceProperties.class,
                                SubscriptionProperties.class,
                                AccountServiceProperties.class})
public class DataNotificationSubscriptionApplication {

    public static void main(String[] args) {
        SpringApplication.run(DataNotificationSubscriptionApplication.class, args);
    }

}
