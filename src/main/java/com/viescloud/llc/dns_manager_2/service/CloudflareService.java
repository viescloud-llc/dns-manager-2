package com.viescloud.llc.dns_manager_2.service;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

import com.viescloud.eco.viesspringutils.exception.HttpResponseThrowers;
import com.viescloud.eco.viesspringutils.util.DynamicMapCache;
import com.viescloud.eco.viesspringutils.util.Json;
import com.viescloud.eco.viesspringutils.util.Streams;
import com.viescloud.llc.dns_manager_2.feign.CloudflareClient;
import com.viescloud.llc.dns_manager_2.model.cloudflare.CloudflareRequest;
import com.viescloud.llc.dns_manager_2.model.cloudflare.CloudflareResult;

import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
public abstract class CloudflareService {
    public static final String CNAME = "CNAME";

    protected abstract String cloudflareEmail();
    protected abstract String cloudflareKey();
    protected abstract String cloudflareZoneId();
    protected abstract String content();

    protected final CloudflareClient cloudflareClient;
    protected final DynamicMapCache<String, CloudflareResult> dnsCacheMap;

    public void clearCache() {
        dnsCacheMap.clear();
    }

    public List<CloudflareResult> getAllCloudflareCnameRecord() {
        return this.getAllCloudflareRecord(CNAME, this.content());
    }

    public List<CloudflareResult> getAllCloudflareRecord(String type, String content) {
        if(this.dnsCacheMap.size() > 0) {
            return Streams.stream(this.dnsCacheMap.values()).collect(Collectors.toCollection(ArrayList::new));
        }
        else {
            var result = this.cloudflareClient.getDNSList(this.cloudflareZoneId(), this.cloudflareEmail(), this.cloudflareKey(), 1, 99999)
                                              .orElseThrow(HttpResponseThrowers.throwServerErrorException("Failed to get dns list from cloudflare with zone id: " + this.cloudflareZoneId()))
                                              .getResult();
    
            // filter only CNAME
            result = Streams.stream(result).filter(r -> r.getType().toUpperCase().equals(type) && r.getContent().equalsIgnoreCase(content)).toList();
            Streams.stream(result).forEach(this::saveDnsCache);
            return result;
        }
    }

    public CloudflareResult getCloudflareCnameRecordByName(String name) {
        if(this.dnsCacheMap.containsKey(name)) {
            return this.dnsCacheMap.get(name);
        }
        else {
            return Streams.stream(this.getAllCloudflareCnameRecord()).filter(r -> r.getName().equalsIgnoreCase(name)).findFirst().orElse(null);
        }
    }

    public CloudflareResult getCloudflareCnameRecordById(String id) {
        if(this.dnsCacheMap.containsKey(id)) {
            return this.dnsCacheMap.get(id);
        }
        else {
            return Streams.stream(this.getAllCloudflareCnameRecord()).filter(r -> r.getId().equals(id)).findFirst().orElse(null);
        }
    }

    public void postCloudflareRecord(CloudflareRequest request) {
        var response = this.cloudflareClient.createDNS(this.cloudflareZoneId(), this.cloudflareEmail(), this.cloudflareKey(), request)
                                            .orElseThrow(HttpResponseThrowers.throwServerErrorException("Failed to create dns in cloudflare"));

        this.saveDnsCache(response.getResult());
    }

    public void postCloudflareRecord(CloudflareResult result) {
        CloudflareRequest request = Optional.ofNullable(Json.tryClone(result, CloudflareRequest.class))
                                            .orElseThrow(HttpResponseThrowers.throwServerErrorException("Failed to cast request"));
        this.postCloudflareRecord(request);
    }

    public void putCloudflareRecord(CloudflareRequest request, String id) {
        var result = this.getCloudflareCnameRecordById(id);
        var response = this.cloudflareClient.putDNS(this.cloudflareZoneId(), this.cloudflareEmail(), this.cloudflareKey(), id, request)
                                            .orElseThrow(HttpResponseThrowers.throwServerErrorException("Failed to update dns in cloudflare"));

        this.deleteDnsCache(result);
        this.saveDnsCache(response.getResult());
    }

    public void putCloudflareRecord(CloudflareResult result) {
        if(result.getId() == null) {
            HttpResponseThrowers.throwBadRequest("Can't update cloudflare record without id");
        }
        CloudflareRequest request = Optional.ofNullable(Json.tryClone(result, CloudflareRequest.class))
                                            .orElseThrow(HttpResponseThrowers.throwServerErrorException("Failed to cast request"));
        this.putCloudflareRecord(request, result.getId());
    }

    public void deleteCloudflareRecord(String id) {
        var result = this.getCloudflareCnameRecordById(id);
        this.cloudflareClient.deleteDNS(this.cloudflareZoneId(), this.cloudflareEmail(), this.cloudflareKey(), id)
                             .orElseThrow(HttpResponseThrowers.throwServerErrorException("Failed to delete dns in cloudflare"));
        this.deleteDnsCache(result);
    }

    public void saveDnsCache(CloudflareResult result) {
        this.dnsCacheMap.put(result.getId(), result);
        this.dnsCacheMap.put(result.getName(), result);
    }

    public void deleteDnsCache(CloudflareResult result) {
        this.dnsCacheMap.remove(result.getId());
        this.dnsCacheMap.remove(result.getName());
    }
}
