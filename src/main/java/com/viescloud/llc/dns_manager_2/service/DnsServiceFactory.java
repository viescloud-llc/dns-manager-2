package com.viescloud.llc.dns_manager_2.service;

import java.time.Duration;
import java.util.HashMap;
import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import com.viescloud.eco.viesspringutils.factory.RedisTemplateFactory;
import com.viescloud.eco.viesspringutils.util.DynamicRedisExpirableMapCache;
import com.viescloud.eco.viesspringutils.util.Json;
import com.viescloud.llc.dns_manager_2.client.NginxClient;
import com.viescloud.llc.dns_manager_2.feign.CloudflareClient;
import com.viescloud.llc.dns_manager_2.model.DnsSetting;
import com.viescloud.llc.dns_manager_2.model.cloudflare.CloudflareResult;
import com.viescloud.llc.dns_manager_2.model.nginx.NginxProxyHostResponse;

@Service
public class DnsServiceFactory {
    
    @Autowired
    private RestTemplate restTemplate;
    @Autowired
    private CloudflareClient cloudflareClient;
    @Autowired(required = false)
    private RedisTemplateFactory redisTemplateFactory;

    private final HashMap<String, DnsService> dnsServiceMap = new HashMap<>();
    private static final Duration TTL = Duration.ofMinutes(10);

    public DnsService getDnsService(DnsSetting dnsSetting) {
        var id = getId(dnsSetting);

        if(this.dnsServiceMap.containsKey(id)) {
            return this.dnsServiceMap.get(id);
        }
        else {
            RedisTemplate<String, NginxProxyHostResponse> nginxProxyRedisTemplate = Optional.ofNullable(redisTemplateFactory).map(e-> e.<String, NginxProxyHostResponse>getDefault()).orElse(null);
            RedisTemplate<String, CloudflareResult> cloudFlareRedisTemplate = Optional.ofNullable(redisTemplateFactory).map(e-> e.<String, CloudflareResult>getDefault()).orElse(null);

            var dynamicNginxRedisExpirableMapCache = new DynamicRedisExpirableMapCache<String, NginxProxyHostResponse>(nginxProxyRedisTemplate).ttl(TTL);
            var dynamicCloudflareRedisExpirableMapCache = new DynamicRedisExpirableMapCache<String, CloudflareResult>(cloudFlareRedisTemplate).ttl(TTL);
            
            NginxClient nginxClient = new NginxClient(restTemplate) {
                @Override
                protected String getBaseUrl() {
                    return dnsSetting.getNginxBaseUrl();
                }
            };

            NginxService nginxService = new NginxService(nginxClient, dynamicNginxRedisExpirableMapCache) {
                @Override
                protected String nginxEmail() {
                    return dnsSetting.getNginxEmail();
                }

                @Override
                protected String nginxPassword() {
                    return dnsSetting.getNginxPassword();
                }
            };

            CloudflareService cloudflareService = new CloudflareService(this.cloudflareClient, dynamicCloudflareRedisExpirableMapCache) {
                @Override
                protected String cloudflareEmail() {
                    return dnsSetting.getCloudflareEmail();
                }

                @Override
                protected String cloudflareKey() {
                    return dnsSetting.getCloudflareKey();
                }

                @Override
                protected String cloudflareZoneId() {
                    return dnsSetting.getCloudflareZoneId();
                }

                @Override
                protected String content() {
                    return dnsSetting.getDomain();
                }
            };

            DnsService dnsService = new DnsService(dnsSetting, nginxService, cloudflareService);
            this.dnsServiceMap.put(id, dnsService);
            return dnsService;
        }
    }

    private String getId(DnsSetting dnsSetting) {
        var cloneDnsSetting = Json.clone(dnsSetting, DnsSetting.class);
        cloneDnsSetting.setId(null);
        cloneDnsSetting.setNginxCustomConfiguration(null);
        return Json.toJson(cloneDnsSetting);
    }
}
