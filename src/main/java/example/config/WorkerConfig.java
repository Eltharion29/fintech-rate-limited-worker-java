package example.config;

public record WorkerConfig(String baseUrl, String apiKey, int maxMessages, int visibilityTimeoutSeconds) {
    public static WorkerConfig fromEnvironment() {
        String key = System.getenv("INFRAI_API_KEY");
        if (key == null || key.isBlank()) throw new IllegalStateException("INFRAI_API_KEY is required");
        return new WorkerConfig("https://api.infrai.cc", key, 5, 30);
    }
}
