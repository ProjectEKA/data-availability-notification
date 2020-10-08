package in.projecteka.datanotificationsubscription.common;

import com.nimbusds.jose.JWSObject;
import in.projecteka.datanotificationsubscription.common.cache.CacheAdapter;
import in.projecteka.datanotificationsubscription.common.model.TokenValidationRequest;
import lombok.AllArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import reactor.core.publisher.Mono;

import java.text.ParseException;

import static in.projecteka.datanotificationsubscription.common.Constants.BLOCK_LIST;
import static in.projecteka.datanotificationsubscription.common.Constants.BLOCK_LIST_FORMAT;
import static java.lang.String.format;

@AllArgsConstructor
public class CMAccountServiceAuthenticator implements Authenticator {
    private final SessionServiceClient sessionServiceClient;
    private final CacheAdapter<String, String> blockListedTokens;
    private static final Logger logger = LoggerFactory.getLogger(CMAccountServiceAuthenticator.class);

    @Override
    public Mono<Caller> verify(String token) {
        try {
            var parts = token.split(" ");
            if (parts.length != 2)
                return Mono.empty();

            var jwsObject = JWSObject.parse(parts[1]);
            var credentials = parts[1];
            var jsonObject = jwsObject.getPayload().toJSONObject();
            return blockListedTokens.exists(String.format(BLOCK_LIST_FORMAT, BLOCK_LIST, credentials))
                    .filter(exists -> !exists)
                    .flatMap(uselessFalse -> sessionServiceClient.validateToken(TokenValidationRequest.builder()
                            .authToken(credentials).build())
                            .flatMap(isValid -> {
                                if (Boolean.TRUE.equals(isValid)) {
                                    return Mono.just(Caller.builder().username(jsonObject.getAsString("healthId"))
                                            .isServiceAccount(false).build());
                                }
                                return Mono.empty();
                            }).onErrorResume(error -> Mono.empty()));
        } catch (ParseException e) {
            logger.error(format("Unauthorized access with token: %s %s", token, e));
        }
        return Mono.empty();
    }
}
