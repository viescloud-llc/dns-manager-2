package com.viescloud.llc.dns_manager_2.client;

import java.util.List;
import java.util.Optional;

import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.HttpMethod;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.client.RestTemplate;

import com.viescloud.eco.viesspringutils.util.WebCall;
import com.viescloud.llc.dns_manager_2.model.nginx.NginxCertificateResponse;
import com.viescloud.llc.dns_manager_2.model.nginx.NginxHealthCheckResponse;
import com.viescloud.llc.dns_manager_2.model.nginx.NginxLoginRequest;
import com.viescloud.llc.dns_manager_2.model.nginx.NginxLoginResponse;
import com.viescloud.llc.dns_manager_2.model.nginx.NginxProxyHostRequest;
import com.viescloud.llc.dns_manager_2.model.nginx.NginxProxyHostResponse;

import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
public abstract class NginxClient {

    private final RestTemplate restTemplate;

    protected abstract String getBaseUrl();

    protected String getUrl(String... paths) {
        var baseUrl = this.getBaseUrl();

        if (!baseUrl.startsWith("http://") || !baseUrl.startsWith("https://")) {
            baseUrl = "https://" + baseUrl;
        }

        while (baseUrl.endsWith("/")) {
            baseUrl = baseUrl.substring(0, baseUrl.length() - 1);
        }

        return String.format("%s/%s", baseUrl, String.join("/", paths));
    }

    public Optional<NginxHealthCheckResponse> healthCheck(String token) {
        return WebCall.of(restTemplate, NginxHealthCheckResponse.class)
                      .request(HttpMethod.GET, getUrl("api"))
                      .header("Authorization", token)
                      .exchange()
                      .getOptionalResponseBody();
    }

    public Optional<List<NginxCertificateResponse>> getAllCertificate(String token) {
        return WebCall.of(restTemplate, new ParameterizedTypeReference<List<NginxCertificateResponse>>() {})
                      .request(HttpMethod.GET, getUrl("api", "nginx", "certificates"))
                      .header("Authorization", token)
                      .exchange()
                      .getOptionalResponseBody();
    }

    public Optional<NginxProxyHostResponse> getProxyHost(String token, String id) {
        return WebCall.of(restTemplate, NginxProxyHostResponse.class)
                      .request(HttpMethod.GET, getUrl("api", "nginx", "proxy-hosts", id))
                      .header("Authorization", token)
                      .exchange()
                      .getOptionalResponseBody();
    }

    public Optional<List<NginxProxyHostResponse>> getAllProxyHost(String token) {
        return WebCall.of(restTemplate, new ParameterizedTypeReference<List<NginxProxyHostResponse>>() {})
                      .request(HttpMethod.GET, getUrl("api", "nginx", "proxy-hosts"))
                      .header("Authorization", token)
                      .exchange()
                      .getOptionalResponseBody();
    }

    public Optional<NginxLoginResponse> getTokens(String token) {
        return WebCall.of(restTemplate, NginxLoginResponse.class)
                      .request(HttpMethod.GET, getUrl("api", "tokens"))
                      .header("Authorization", token)
                      .exchange()
                      .getOptionalResponseBody();
    }
    
    public Optional<NginxLoginResponse> login(@RequestBody NginxLoginRequest request) {
        return WebCall.of(restTemplate, NginxLoginResponse.class)
                      .request(HttpMethod.POST, getUrl("api", "tokens"))
                      .header("Content-Type", "application/json")
                      .body(request)
                      .exchange()
                      .getOptionalResponseBody();
    }

    public Optional<NginxProxyHostResponse> createProxyHost(String token, NginxProxyHostRequest request) {
        return WebCall.of(restTemplate, NginxProxyHostResponse.class)
                      .request(HttpMethod.POST, getUrl("api", "nginx", "proxy-hosts"))
                      .header("Authorization", token)
                      .header("Content-Type", "application/json")
                      .body(request)
                      .exchange()
                      .getOptionalResponseBody();
    }

    public Optional<Boolean> enableProxyHost(String token, String id) {
        return WebCall.of(restTemplate, Boolean.class)
                      .request(HttpMethod.POST, getUrl("api", "nginx", "proxy-hosts", id, "enable"))
                      .header("Authorization", token)
                      .exchange()
                      .getOptionalResponseBody();
    }

    public Optional<Boolean> disableProxyHost(String token, String id) {
        return WebCall.of(restTemplate, Boolean.class)
                      .request(HttpMethod.POST, getUrl("api", "nginx", "proxy-hosts", id, "disable"))
                      .header("Authorization", token)
                      .exchange()
                      .getOptionalResponseBody();
    }

    public Optional<NginxProxyHostResponse> updateProxyHost(String token, int id, NginxProxyHostRequest request) {
        return WebCall.of(restTemplate, NginxProxyHostResponse.class)
                      .request(HttpMethod.PUT, getUrl("api", "nginx", "proxy-hosts", String.valueOf(id)))
                      .header("Authorization", token)
                      .header("Content-Type", "application/json")
                      .body(request)
                      .exchange()
                      .getOptionalResponseBody();
    }

    public Optional<Boolean> deleteProxyHost(String token, int id) {
        return WebCall.of(restTemplate, Boolean.class)
                      .request(HttpMethod.DELETE, getUrl("api", "nginx", "proxy-hosts", String.valueOf(id)))
                      .header("Authorization", token)
                      .exchange()
                      .getOptionalResponseBody();
    }
}
