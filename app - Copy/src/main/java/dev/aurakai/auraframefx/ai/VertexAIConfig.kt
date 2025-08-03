package dev.aurakai.auraframefx.ai

/**
 * Configuration for Vertex AI client with comprehensive settings.
 * Supports production-ready deployment with security, performance, and reliability features.
 */
data class VertexAIConfig(
    // Core connection settings
    val projectId: String,
    val location: String,
    val endpoint: String,
    val modelName: String,
    val apiVersion: String = "v1",

    // Authentication settings
    val serviceAccountPath: String? = null,
    val apiKey: String? = null,
    val useApplicationDefaultCredentials: Boolean = true,

    // Security settings
    val enableSafetyFilters: Boolean = true,
    val safetySettings: Map<String, String> = emptyMap(),
    val maxContentLength: Int = 1000000, // 1MB

    // Performance settings
    val timeoutMs: Long = 30000, // 30 seconds
    val maxRetries: Int = 3,
    val retryDelayMs: Long = 1000,
    val maxConcurrentRequests: Int = 10,

    // Caching settings
    val enableCaching: Boolean = true,
    val cacheExpiryMs: Long = 3600000, // 1 hour
    val maxCacheSize: Int = 100,

    // Generation settings
    val defaultTemperature: Double = 0.7,
    val defaultTopP: Double = 0.9,
    val defaultTopK: Int = 40,
    val defaultMaxTokens: Int = 1024,

    // Monitoring settings
    val enableMetrics: Boolean = true,
    val enableLogging: Boolean = true,
    val logLevel: String = "INFO",

    // Feature flags
    val enableStreamingResponses: Boolean = true,
    val enableBatchProcessing: Boolean = true,
    val enableFunctionCalling: Boolean = true,
) {
    /**
     * Validates the configuration for required fields and acceptable parameter ranges.
     *
     * Checks that essential string fields are not blank and numeric parameters are within valid limits. Returns a list of error messages for any invalid fields, or an empty list if the configuration is valid.
     *
     * @return A list of error messages for invalid fields, or an empty list if all values are valid.
     */
    fun validate(): List<String> {
        val errors = mutableListOf<String>()

        if (projectId.isBlank()) errors.add("Project ID is required")
        if (location.isBlank()) errors.add("Location is required")
        if (endpoint.isBlank()) errors.add("Endpoint is required")
        if (modelName.isBlank()) errors.add("Model name is required")

        if (timeoutMs <= 0) errors.add("Timeout must be positive")
        if (maxRetries < 0) errors.add("Max retries cannot be negative")
        if (maxConcurrentRequests <= 0) errors.add("Max concurrent requests must be positive")

        if (defaultTemperature < 0.0 || defaultTemperature > 1.0) {
            errors.add("Temperature must be between 0.0 and 1.0")
        }

        if (defaultTopP < 0.0 || defaultTopP > 1.0) {
            errors.add("TopP must be between 0.0 and 1.0")
        }

        return errors
    }

    /**
     * Builds the base URL for Vertex AI API requests using the current configuration.
     *
     * @return The constructed base endpoint URL.
     */
    fun getFullEndpoint(): String {
        return "https://$endpoint/$apiVersion/projects/$projectId/locations/$location"
    }

    /**
     * Builds the complete URL for the content generation endpoint of the configured Vertex AI model.
     *
     * The URL includes the base endpoint, API version, project ID, location, and model name, and targets the `generateContent` method.
     *
     * @return The full URL for sending content generation requests to the specified model.
     */
    fun getModelEndpoint(): String {
        return "${getFullEndpoint()}/publishers/google/models/$modelName:generateContent"
    }

    /**
     * Returns a copy of the configuration with settings tailored for production environments.
     *
     * The production configuration enables safety filters, increases retry count and timeout, activates caching, metrics, and logging, and sets the log level to "WARN" to enhance reliability and security.
     *
     * @return A new `VertexAIConfig` instance optimized for production use.
     */
    fun forProduction(): VertexAIConfig {
        return copy(
            enableSafetyFilters = true,
            maxRetries = 5,
            timeoutMs = 60000, // 1 minute for production
            enableCaching = true,
            enableMetrics = true,
            enableLogging = true,
            logLevel = "WARN" // Less verbose in production
        )
    }

    /**
     * Returns a configuration copy optimized for development environments.
     *
     * The development configuration disables safety filters and caching, reduces retries and timeout for faster iteration, and enables metrics and verbose logging.
     *
     * @return A new `VertexAIConfig` instance configured for development use.
     */
    fun forDevelopment(): VertexAIConfig {
        return copy(
            enableSafetyFilters = false, // More permissive for testing
            maxRetries = 1,
            timeoutMs = 15000, // Faster feedback
            enableCaching = false, // Always fresh responses
            enableMetrics = true,
            enableLogging = true,
            logLevel = "DEBUG" // Verbose for development
        )
    }
}
