package com.carmanagement.utils;

import java.net.http.HttpClient;

import dev.langchain4j.http.client.HttpClientBuilder;
import dev.langchain4j.http.client.jdk.JdkHttpClient;
import dev.langchain4j.http.client.jdk.JdkHttpClientBuilder;

import java.time.Duration;

/**
 * The Http11ClientBuilder creates an HTTP client that forces the use of HTTP 1.1.
 * 
 * This is required if your model is hosted by a runtime that does not support HTTP, such as LMStudio or vLLM, as
 * documented in the LangChain4J documentation:
 * 
 *     https://docs.langchain4j.dev/integrations/language-models/openai-compatible/#lm-studio
 * 
 * Attempting to send requests to the LLM hosted in such an environment using HTTP2 results in the following error
 * being returned:
 * 
 * {
 *   "error": {
 *     "message": "1 validation error:\n  {'type': 'missing', 'loc': ('body',), 'msg': 'Field required', 'input': None}\n\n  File \"/home/someuser/.venv/lib/python3.12/site-packages/vllm/entrypoints/utils.py\", line 34, in create_chat_completion\n    POST /v1/chat/completions [{'type': 'missing', 'loc': ('body',), 'msg': 'Field required', 'input': None}]",
 *     "type": "Bad Request",
 *     "param": null,
 *     "code": 400
 *   }
 * }
 * 
 */
public class Http11ClientBuilder implements HttpClientBuilder {

    private JdkHttpClientBuilder jdkHttpClientBuilder;

    public Http11ClientBuilder() {
        HttpClient.Builder httpClientBuilder = HttpClient.newBuilder()
            .version(HttpClient.Version.HTTP_1_1) ;

        jdkHttpClientBuilder = JdkHttpClient.builder()
            .httpClientBuilder(httpClientBuilder);
    }

    @Override
    public Duration connectTimeout() {
        return jdkHttpClientBuilder.connectTimeout();
    }

    @Override
    public HttpClientBuilder connectTimeout(Duration connectTimeout) {
        jdkHttpClientBuilder.connectTimeout(connectTimeout);
        return this;
    }

    @Override
    public Duration readTimeout() {
        return jdkHttpClientBuilder.readTimeout();
    }

    @Override
    public HttpClientBuilder readTimeout(Duration readTimeout) {
        jdkHttpClientBuilder.readTimeout(readTimeout);
        return this;
    }

    @Override
    public dev.langchain4j.http.client.HttpClient build() {
        return jdkHttpClientBuilder.build();
    }
}
