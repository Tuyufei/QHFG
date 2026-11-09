package com.cug.qhfg.RestTemplate;

import org.springframework.http.client.ClientHttpRequestInterceptor;
import org.springframework.http.HttpRequest;
import org.springframework.http.client.ClientHttpRequestExecution;
import org.springframework.http.client.ClientHttpResponse;

import java.io.IOException;
import java.util.logging.Logger;
/**
 * @Description TODO
 * @Author fjh
 * @Date 2025/1/2
 */
public class LoggingRequestInterceptor implements ClientHttpRequestInterceptor {
    private static final Logger log = Logger.getLogger(LoggingRequestInterceptor.class.getName());

    @Override
    public ClientHttpResponse intercept(HttpRequest request, byte[] body, ClientHttpRequestExecution execution) throws IOException {
        // Log the request details
        log.info("Request: " + request.getMethod() + " " + request.getURI());
        log.info("Headers: " + request.getHeaders().toString());

        // Execute the request
        ClientHttpResponse response = execution.execute(request, body);

        // Log the response details
        log.info("Response Status Code: " + response.getStatusCode());
        log.info("Response Headers: " + response.getHeaders().toString());

        return response;
    }
}
