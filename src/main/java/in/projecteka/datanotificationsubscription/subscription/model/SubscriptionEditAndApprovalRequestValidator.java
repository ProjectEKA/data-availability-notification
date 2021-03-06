package in.projecteka.datanotificationsubscription.subscription.model;

import in.projecteka.datanotificationsubscription.common.ClientError;
import org.springframework.util.CollectionUtils;
import org.springframework.util.StringUtils;
import reactor.core.publisher.Mono;

import java.util.List;

/*TODO: Should be registered as spring hook instead of manual trigger
 */
public class SubscriptionEditAndApprovalRequestValidator {

    public Mono<Void> validateRequest(SubscriptionEditAndApprovalRequest approvalRequest) {
        if (CollectionUtils.isEmpty(approvalRequest.getIncludedSources())) {
            return Mono.error(ClientError.invalidSubscriptionApprovalRequest("Sources are not specified"));
        }
        if (approvalRequest.isApplicableForAllHIPs()) {
            if (approvalRequest.getIncludedSources().size() > 1) {
                return Mono.error(ClientError.invalidSubscriptionApprovalRequest("Only one source needed when applicable for all HIPs"));
            }
            if (approvalRequest.getIncludedSources().get(0).getHip() != null) {
                return Mono.error(ClientError.invalidSubscriptionApprovalRequest("HIP details are not allowed in sources when applicable for all HIPs"));
            }
            if (!CollectionUtils.isEmpty(approvalRequest.getExcludedSources()) && hasEmptyHIPs(approvalRequest.getExcludedSources())) {
                return Mono.error(ClientError.invalidSubscriptionApprovalRequest("HIP details cannot be empty in exclude list"));
            }
        }
        else {
            if (hasEmptyHIPs(approvalRequest.getIncludedSources())){
                return Mono.error(ClientError.invalidSubscriptionApprovalRequest("HIP details cannot be empty in source list"));
            }
            if (!CollectionUtils.isEmpty(approvalRequest.getExcludedSources())){
                return Mono.error(ClientError.invalidSubscriptionApprovalRequest("excludeSources is not allowed for individual HIPs"));
            }
        }
        return Mono.empty();
    }

    private boolean hasEmptyHIPs(List<GrantedSubscription> subscriptions) {
        return subscriptions
                .stream()
                .anyMatch(grantedSubscription -> grantedSubscription.getHip() == null || StringUtils.isEmpty(grantedSubscription.getHip().getId()));
    }
}
