package in.projecteka.datanotificationsubscription.hipLink;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import in.projecteka.consentmanager.common.TraceableMessage;
import in.projecteka.datanotificationsubscription.HIUSubscriptionManager;
import in.projecteka.datanotificationsubscription.ListenerProperties;
import in.projecteka.datanotificationsubscription.common.ClientError;
import in.projecteka.datanotificationsubscription.common.Serializer;
import lombok.AllArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.slf4j.MDC;
import reactor.core.publisher.Mono;
import reactor.rabbitmq.AcknowledgableDelivery;
import reactor.rabbitmq.Receiver;
import reactor.util.retry.Retry;
import reactor.util.retry.RetryBackoffSpec;

import javax.annotation.PostConstruct;
import javax.annotation.PreDestroy;
import java.time.Duration;

import static in.projecteka.datanotificationsubscription.common.Constants.CORRELATION_ID;
import static in.projecteka.datanotificationsubscription.common.Constants.HIP_LINK_QUEUE;

@AllArgsConstructor
public class HipLinkNotificationListener {
    private static final Logger logger = LoggerFactory.getLogger(HipLinkNotificationListener.class);
    private final Receiver receiver;
    private final HIUSubscriptionManager subscriptionManager;
    private final ListenerProperties listenerProperties;

    @PostConstruct
    public void subscribe() {
        receiver.consumeManualAck(HIP_LINK_QUEUE)
                .subscribe(delivery -> {
                            TraceableMessage traceableMessage = Serializer.to(delivery.getBody(), TraceableMessage.class);
                            Mono.just(traceableMessage)
                                    .map(this::extractLinkEvent)
                                    .doOnNext(linkEvent -> logger.info("Received link event for health-id-number {} from HIP {}", linkEvent.getHealthNumber(), linkEvent.getHipId()))
                                    .flatMap(newCCLinkEvent -> subscriptionManager.notifySubscribers(newCCLinkEvent).then())
                                    .doOnSuccess(unused -> delivery.ack())
                                    .doOnError(throwable -> logger.error("Error while processing link event", throwable))
                                    .doFinally(signalType -> MDC.clear())
                                    .retryWhen(retryConfig(delivery))
                                    .subscriberContext(ctx -> ctx.put(CORRELATION_ID, traceableMessage.getCorrelationId()))
                                    .subscribe();
                        }
                );

    }

    @PreDestroy
    public void closeConnection() {
        receiver.close();
    }

    private NewCCLinkEvent extractLinkEvent(TraceableMessage traceableMessage) {
        ObjectMapper mapper = new ObjectMapper();
        mapper.registerModule(new JavaTimeModule());
        return mapper.convertValue(traceableMessage.getMessage(), NewCCLinkEvent.class);
    }

    private RetryBackoffSpec retryConfig(AcknowledgableDelivery delivery) {
        return Retry
                .fixedDelay(listenerProperties.getLinkEventMaximumRetries(), Duration.ofMillis(listenerProperties.getLinkEventMaximumRetries()))
                .onRetryExhaustedThrow((retryBackoffSpec, retrySignal) -> {
                    logger.info("Exhausted Retries");
                    delivery.nack(false);
                    return ClientError.unknownErrorOccurred();
                });
    }
}
