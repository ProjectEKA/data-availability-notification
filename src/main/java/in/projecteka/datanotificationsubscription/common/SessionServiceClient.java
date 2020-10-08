package in.projecteka.datanotificationsubscription.common;

import in.projecteka.datanotificationsubscription.common.model.TokenValidationRequest;
import lombok.AllArgsConstructor;
import org.slf4j.Logger;
import org.springframework.http.HttpStatus;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;

import java.util.Properties;
import java.util.function.Supplier;

import static org.slf4j.LoggerFactory.getLogger;
import static org.springframework.http.HttpHeaders.AUTHORIZATION;
import static org.springframework.http.HttpHeaders.CONTENT_TYPE;
import static org.springframework.http.MediaType.APPLICATION_JSON;
import static org.springframework.http.MediaType.APPLICATION_JSON_VALUE;
import static reactor.core.publisher.Mono.error;

@AllArgsConstructor
public class SessionServiceClient {
    private final WebClient webClient;
    private final Supplier<Mono<String>> tokenGenerator;
    private static final String X_TOKEN_HEADER_NAME = "X-Token";
    private static final String BEARER = "Bearer ";
    private static final Logger logger = getLogger(SessionServiceClient.class);
    public static final String VALIDATE_TOKEN = "/v1/account/token";

    public SessionServiceClient(WebClient.Builder webClient, String baseUrl, Supplier<Mono<String>> tokenGenerator) {
        this.webClient = webClient.baseUrl(baseUrl).build();
        this.tokenGenerator = tokenGenerator;
    }

    public Mono<Boolean> validateToken(TokenValidationRequest request) {
        return tokenGenerator.get()
                .flatMap(hasGatewayToken -> webClient
                        .post()
                        .uri(uriBuilder -> uriBuilder.path(VALIDATE_TOKEN).build())
                        .header(X_TOKEN_HEADER_NAME, BEARER + request.getAuthToken())
                        .header(CONTENT_TYPE, APPLICATION_JSON_VALUE)
                        .header(AUTHORIZATION, hasGatewayToken)
                        .body(Mono.just(request), TokenValidationRequest.class)
                        .accept(APPLICATION_JSON)
                        .retrieve()
                        .onStatus(HttpStatus::is4xxClientError, clientResponse -> clientResponse.bodyToMono(Properties.class)
                                .doOnNext(properties -> logger.error(properties.toString()))
                                .then(error(ClientError.unAuthorized())))
                        .onStatus(HttpStatus::isError, clientResponse -> clientResponse.bodyToMono(Properties.class)
                                .doOnNext(properties -> logger.error(properties.toString()))
                                .then(error(ClientError.networkServiceCallFailed())))
                        .bodyToMono(Boolean.class)
                );
    }
}
