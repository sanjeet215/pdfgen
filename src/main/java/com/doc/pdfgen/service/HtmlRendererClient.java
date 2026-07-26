package com.doc.pdfgen.service;

import com.doc.pdfgen.dto.HtmlToPdfDTO;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Duration;
import java.util.Base64;
import java.util.HashMap;
import java.util.Map;

@Component
public class HtmlRendererClient {
    private final HttpClient client = HttpClient.newBuilder()
            .connectTimeout(Duration.ofSeconds(3)).build();
    private final ObjectMapper objectMapper;
    private final String rendererUrl;

    public HtmlRendererClient(
            ObjectMapper objectMapper,
            @Value("${html.renderer.url:${HTML_RENDERER_URL:http://localhost:3001}}") String rendererUrl) {
        this.objectMapper = objectMapper;
        this.rendererUrl = rendererUrl;
    }

    public byte[] render(HtmlToPdfDTO options) {
        return post("/render", options);
    }

    public byte[] renderRtf(byte[] rtf, com.doc.pdfgen.dto.RtfToPdfDTO options) {
        Map<String, Object> request = new HashMap<>();
        request.put("rtfBase64", Base64.getEncoder().encodeToString(rtf));
        request.put("pageSize", options.getPageSize());
        request.put("orientation", options.getOrientation());
        request.put("marginMm", options.getMarginMm());
        return post("/render-rtf", request);
    }

    private byte[] post(String path, Object options) {
        try {
            HttpRequest request = HttpRequest.newBuilder()
                    .uri(URI.create(rendererUrl.replaceAll("/$", "") + path))
                    .timeout(Duration.ofSeconds(20))
                    .header("Content-Type", MediaType.APPLICATION_JSON_VALUE)
                    .POST(HttpRequest.BodyPublishers.ofByteArray(objectMapper.writeValueAsBytes(options)))
                    .build();
            HttpResponse<byte[]> response = client.send(request, HttpResponse.BodyHandlers.ofByteArray());
            if (response.statusCode() != 200) {
                throw new IllegalStateException("HTML renderer rejected the document");
            }
            return response.body();
        } catch (InterruptedException exception) {
            Thread.currentThread().interrupt();
            throw new IllegalStateException("HTML rendering was interrupted", exception);
        } catch (Exception exception) {
            throw new IllegalStateException("Unable to render HTML", exception);
        }
    }
}
