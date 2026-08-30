package example.infra;

import example.config.WorkerConfig;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Duration;

public final class InfraiQueueClient {
    private static final String QUEUE = "course-events";
    private final WorkerConfig config;
    private final HttpClient http = HttpClient.newHttpClient();
    public InfraiQueueClient(WorkerConfig config) { this.config = config; }

    public String publish(String payload) { return post("/v1/queue/publish", "{\"queue\":" + quote(QUEUE) + ",\"payload\":" + quote(payload) + "}"); }
    public String consume() { return post("/v1/queue/consume", "{\"queue\":" + quote(QUEUE) + ",\"max_messages\":" + config.maxMessages() + ",\"visibility_timeout\":" + config.visibilityTimeoutSeconds() + "}"); }
    public String ack(String messageId) { return post("/v1/queue/ack", "{\"queue\":" + quote(QUEUE) + ",\"message_id\":" + quote(messageId) + "}"); }

    private String post(String path, String body) {
        try {
            for (int attempt = 0; attempt < 4; attempt++) {
                HttpRequest request = HttpRequest.newBuilder(URI.create(config.baseUrl() + path))
                    .timeout(Duration.ofSeconds(30)).header("Authorization", "Bearer " + config.apiKey())
                    .header("Content-Type", "application/json").method("POST", HttpRequest.BodyPublishers.ofString(body)).build();
                HttpResponse<String> response = http.send(request, HttpResponse.BodyHandlers.ofString());
                String envelope = response.body();
                if (!envelope.contains("\"ok\":true")) throw new IllegalStateException("Infrai error: " + envelope);
                if (response.statusCode() != 429) return envelope;
                Thread.sleep(500L * (1L << attempt));
            }
            throw new IllegalStateException("queue request retry limit reached");
        } catch (InterruptedException e) { Thread.currentThread().interrupt(); throw new IllegalStateException(e); }
        catch (Exception e) { throw new IllegalStateException(e); }
    }
    private static String quote(String value) { return "\"" + value.replace("\\", "\\\\").replace("\"", "\\\"") + "\""; }
}
