package com.viescloud.llc.dns_manager_2.service;

import java.time.Duration;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

import com.viescloud.eco.viesspringutils.exception.HttpResponseThrowers;
import com.viescloud.eco.viesspringutils.util.DynamicRedisExpirableMapCache;
import com.viescloud.eco.viesspringutils.util.ExpirableValue;
import com.viescloud.eco.viesspringutils.util.Json;
import com.viescloud.eco.viesspringutils.util.Streams;
import com.viescloud.llc.dns_manager_2.client.NginxClient;
import com.viescloud.llc.dns_manager_2.model.nginx.NginxCertificateResponse;
import com.viescloud.llc.dns_manager_2.model.nginx.NginxLoginRequest;
import com.viescloud.llc.dns_manager_2.model.nginx.NginxProxyHostRequest;
import com.viescloud.llc.dns_manager_2.model.nginx.NginxProxyHostResponse;

import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
public abstract class NginxService {
    protected abstract String nginxEmail();
    protected abstract String nginxPassword();

    protected final NginxClient nginxClient;
    protected final DynamicRedisExpirableMapCache<String, NginxProxyHostResponse> proxyHostCacheMap;
    protected ExpirableValue<String> jwtCache = new ExpirableValue<String>().expireTimerIn(Duration.ofDays(1));

    public void clearCache() {
        proxyHostCacheMap.clear();
    }

    public List<NginxCertificateResponse> getAllCertificate() {
        return this.nginxClient.getAllCertificate(this.getJwtHeader())
                               .orElseThrow(HttpResponseThrowers.throwServerErrorException("Failed to get nginx certificates"));
    }

    public List<NginxProxyHostResponse> getAllProxyHost() {
        if(this.proxyHostCacheMap.size() > 0) {
            return Streams.stream(this.proxyHostCacheMap.values()).collect(Collectors.toCollection(ArrayList::new));
        }

        var proxyHosts = this.nginxClient.getAllProxyHost(this.getJwtHeader())
                                         .orElseThrow(HttpResponseThrowers.throwServerErrorException("Failed to get nginx proxy hosts"));

        Streams.stream(proxyHosts).forEach(this::saveProxyHostCache);
        return proxyHosts;
    }

    public List<String> getAllDomainNameList() {
        return Streams.stream(this.getAllProxyHost()).map(r -> r.getDomainNames()).flatMap(List::stream).toList();
    }

    public NginxProxyHostResponse getProxyHostById(int id) {
        if (this.proxyHostCacheMap.containsKey(String.valueOf(id))) {
            return this.proxyHostCacheMap.get(String.valueOf(id));
        }
        else {
            return Streams.stream(this.getAllProxyHost()).filter(r -> id == r.getId()).findFirst().orElse(null);
        }
    }

    public NginxProxyHostResponse getProxyHostByUri(String uri) {
        if (this.proxyHostCacheMap.containsKey(uri)) {
            return this.proxyHostCacheMap.get(uri);
        }
        else {
            return Streams.stream(this.getAllProxyHost()).filter(r -> uri.equals(getUri(r))).findFirst().orElse(null);
        }
    }

    public boolean enableProxyHost(int id) {
        var proxyHost = this.getProxyHostById(id);
        this.nginxClient.enableProxyHost(this.getJwtHeader(), String.valueOf(id))
                        .orElseThrow(HttpResponseThrowers.throwServerErrorException("Failed to enable nginx proxy host"));
        
        this.deleteProxyHostCache(proxyHost);
        return true;
    }

    public boolean disableProxyHost(int id) {
        var proxyHost = this.getProxyHostById(id);
        this.nginxClient.disableProxyHost(this.getJwtHeader(), String.valueOf(id))
                        .orElseThrow(HttpResponseThrowers.throwServerErrorException("Failed to disable nginx proxy host"));
        
        this.deleteProxyHostCache(proxyHost);
        return true;
    }

    public void createProxyHost(NginxProxyHostRequest request) {
        var response = this.nginxClient.createProxyHost(this.getJwtHeader(), request)
                                       .orElseThrow(HttpResponseThrowers.throwServerErrorException("Failed to create nginx proxy host"));

        this.saveProxyHostCache(response);
    }

    public void createProxyHost(NginxProxyHostResponse response) {
        NginxProxyHostRequest request = Optional.ofNullable(Json.tryClone(response, NginxProxyHostRequest.class))
                                                .orElseThrow(HttpResponseThrowers.throwServerErrorException("Failed to cast request"));
        this.createProxyHost(request);
    }

    public void putProxyHost(NginxProxyHostRequest request, int id) {
        var proxyHost = this.getProxyHostById(id);
        var response = this.nginxClient.updateProxyHost(this.getJwtHeader(), id, request)
                                        .orElseThrow(HttpResponseThrowers.throwServerErrorException("Failed to update nginx proxy host"));
        
        this.deleteProxyHostCache(proxyHost);
        this.saveProxyHostCache(response);
    }

    public void putProxyHost(NginxProxyHostResponse response) {
        if(response.getId() == 0) {
            HttpResponseThrowers.throwBadRequest("Can't update nginx proxy host without id");
        }

        NginxProxyHostRequest request = Optional.ofNullable(Json.tryClone(response, NginxProxyHostRequest.class))
                                                .orElseThrow(HttpResponseThrowers.throwServerErrorException("Failed to cast request"));
        this.putProxyHost(request, response.getId());
    }

    public void deleteProxyHost(int id) {
        var proxyHost = this.getProxyHostById(id);

        if (proxyHost != null && proxyHost.getId() != 0) {
            this.nginxClient.deleteProxyHost(this.getJwtHeader(), id)
                            .orElseThrow(HttpResponseThrowers.throwServerErrorException("Failed to delete nginx proxy host"));
            
            this.deleteProxyHostCache(proxyHost);
        }
    }

    public void deleteProxyHostByUri(String uri) {
        var proxyHost = this.getProxyHostByUri(uri);
        if(proxyHost != null && proxyHost.getId() != 0) {
            this.nginxClient.deleteProxyHost(this.getJwtHeader(), proxyHost.getId())
                        .orElseThrow(HttpResponseThrowers.throwServerErrorException("Failed to delete nginx proxy host"));
        
            this.deleteProxyHostCache(proxyHost);
        }
    }

    // ------------------Helper-------------------

    private void saveProxyHostCache(NginxProxyHostResponse response) {
        this.proxyHostCacheMap.put(getUri(response), response);
        this.proxyHostCacheMap.put(String.valueOf(response.getId()), response);
    }

    private void deleteProxyHostCache(NginxProxyHostResponse response) {
        this.proxyHostCacheMap.remove(getUri(response));
        this.proxyHostCacheMap.remove(String.valueOf(response.getId()));
    }

    private String getUri(NginxProxyHostResponse response) {
        NginxProxyHostRequest request = response;
        return this.getUri(request);
    }

    private String getUri(NginxProxyHostRequest request) {
        return String.format("%s://%s:%s", request.getForwardScheme(), request.getForwardHost(), request.getForwardPort());
    }

    private String getJwtHeader() {
        var jwt = this.jwtCache.getValue();
        if(jwt != null) {
            return String.format("Bearer %s", jwt);
        }

        jwt = this.nginxClient.login(new NginxLoginRequest(this.nginxEmail(), this.nginxPassword()))
                              .orElseThrow(HttpResponseThrowers.throwServerErrorException("Failed to login to nginx"))
                              .getToken();

        this.jwtCache.value(jwt);
        return String.format("Bearer %s", jwt);
    }
}
