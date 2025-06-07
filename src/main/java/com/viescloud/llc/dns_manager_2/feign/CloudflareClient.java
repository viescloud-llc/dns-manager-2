package com.viescloud.llc.dns_manager_2.feign;

import java.util.Map;
import java.util.Optional;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestParam;

import com.viescloud.llc.dns_manager_2.model.cloudflare.CloudflareListWarper;
import com.viescloud.llc.dns_manager_2.model.cloudflare.CloudflareRequest;
import com.viescloud.llc.dns_manager_2.model.cloudflare.CloudflareWarper;

import io.swagger.v3.oas.annotations.parameters.RequestBody;

@FeignClient(name = "cloudflareClient", url = "${cloudflare.uri}")
public interface CloudflareClient {
    
    @GetMapping(value = "client/v4/zones/{zoneId}/dns_records", produces = "application/json")
    public Optional<CloudflareListWarper> getDNSList(
        @PathVariable("zoneId") String zoneId, 
        @RequestHeader("X-Auth-Email") String email, 
        @RequestHeader("X-Auth-Key") String key,
        @RequestParam(value = "page", required = false) Integer page, // Optional parameter
        @RequestParam(value = "per_page", required = false) Integer perPage // Optional parameter
    );

    @GetMapping(value = "client/v4/zones/{zoneId}/dns_records/{recordId}", produces = "application/json")
    public Optional<CloudflareWarper> getDNS(
            @PathVariable("zoneId") String zoneId, 
            @PathVariable("recordId") String recordId, 
            @RequestHeader("X-Auth-Email") String email, 
            @RequestHeader("X-Auth-Key") String key
    );

    @PostMapping(value = "client/v4/zones/{zoneId}/dns_records", produces = "application/json", consumes = "application/json")
    public Optional<CloudflareWarper> createDNS(
            @PathVariable("zoneId") String zoneId, 
            @RequestHeader("X-Auth-Email") String email, 
            @RequestHeader("X-Auth-Key") String key,
            @RequestBody CloudflareRequest request
    );

    @PutMapping(value = "client/v4/zones/{zoneId}/dns_records/{recordId}", produces = "application/json", consumes = "application/json")
    public Optional<CloudflareWarper> putDNS(
            @PathVariable("zoneId") String zoneId, 
            @RequestHeader("X-Auth-Email") String email, 
            @RequestHeader("X-Auth-Key") String key,
            @PathVariable("recordId") String recordId, 
            @RequestBody CloudflareRequest request
    );

    @PatchMapping(value = "client/v4/zones/{zoneId}/dns_records/{recordId}", produces = "application/json", consumes = "application/json")
    public Optional<CloudflareWarper> patchDNS(
            @PathVariable("zoneId") String zoneId, 
            @RequestHeader("X-Auth-Email") String email, 
            @RequestHeader("X-Auth-Key") String key,
            @PathVariable("recordId") String recordId, 
            @RequestBody CloudflareRequest request
    );

    @DeleteMapping(value = "client/v4/zones/{zoneId}/dns_records/{recordId}", produces = "application/json")
    public Optional<Map<String, Object>> deleteDNS(
            @PathVariable("zoneId") String zoneId, 
            @RequestHeader("X-Auth-Email") String email, 
            @RequestHeader("X-Auth-Key") String key,
            @PathVariable("recordId") String recordId
    );
}
