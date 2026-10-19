package client;

import ewm.dto.EndpointHitDto;
import org.springframework.cloud.client.ServiceInstance;
import org.springframework.cloud.client.discovery.DiscoveryClient;
import org.springframework.http.ResponseEntity;
import org.springframework.retry.backoff.FixedBackOffPolicy;
import org.springframework.retry.policy.MaxAttemptsRetryPolicy;
import org.springframework.retry.support.RetryTemplate;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;
import org.springframework.web.util.UriComponentsBuilder;

import java.net.URI;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;

@Component
public class StatsClient {

    private final DiscoveryClient discoveryClient;
    private final RetryTemplate retryTemplate;
    private final String statsServiceId = "stats-server";
    private static final DateTimeFormatter FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

    public StatsClient(DiscoveryClient discoveryClient) {
        this.discoveryClient = discoveryClient;

        this.retryTemplate = new RetryTemplate();
        FixedBackOffPolicy fixedBackOffPolicy = new FixedBackOffPolicy();
        fixedBackOffPolicy.setBackOffPeriod(3000L);
        retryTemplate.setBackOffPolicy(fixedBackOffPolicy);

        MaxAttemptsRetryPolicy retryPolicy = new MaxAttemptsRetryPolicy();
        retryPolicy.setMaxAttempts(3);
        retryTemplate.setRetryPolicy(retryPolicy);
    }

    private ServiceInstance getInstance() {
        try {
            List<ServiceInstance> instances = discoveryClient.getInstances(statsServiceId);
            if (instances == null || instances.isEmpty()) {
                throw new RuntimeException("No instances found for " + statsServiceId);
            }
            return instances.getFirst();
        } catch (Exception exception) {
            throw new StatsServerUnavailable(
                    "Ошибка обнаружения адреса сервиса статистики с id: " + statsServiceId,
                    exception
            );
        }
    }

    private URI makeUri(String path) {
        ServiceInstance instance = retryTemplate.execute(cxt -> getInstance());
        return URI.create("http://" + instance.getHost() + ":" + instance.getPort() + path);
    }

    public ResponseEntity<Object> saveHit(EndpointHitDto hitDto) {
        URI uri = makeUri("/hit");
        return RestClient.create()
                .post()
                .uri(uri)
                .body(hitDto)
                .retrieve()
                .toEntity(Object.class);
    }

    public ResponseEntity<Object> getStats(LocalDateTime start,
                                           LocalDateTime end,
                                           List<String> uris,
                                           Boolean unique) {
        URI baseUri = makeUri("/stats");

        UriComponentsBuilder builder = UriComponentsBuilder
                .fromUri(baseUri)
                .queryParam("start", start.format(FORMATTER))
                .queryParam("end", end.format(FORMATTER))
                .queryParam("unique", unique);

        if (uris != null && !uris.isEmpty()) {
            uris.forEach(uri -> builder.queryParam("uris", uri));
        }

        URI uri = builder.build().encode().toUri();

        return RestClient.create()
                .get()
                .uri(uri)
                .retrieve()
                .toEntity(Object.class);
    }
}