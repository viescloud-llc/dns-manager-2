package com.viescloud.llc.dns_manager_2.service;

import java.net.URI;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.BiFunction;

import org.springframework.util.ObjectUtils;

import com.viescloud.eco.viesspringutils.util.DateTime;
import com.viescloud.eco.viesspringutils.util.Streams;
import com.viescloud.llc.dns_manager_2.model.DnsRecord;
import com.viescloud.llc.dns_manager_2.model.DnsSetting;
import com.viescloud.llc.dns_manager_2.model.cloudflare.CloudflareRequest;
import com.viescloud.llc.dns_manager_2.model.cloudflare.CloudflareResult;
import com.viescloud.llc.dns_manager_2.model.nginx.NginxCertificateResponse;
import com.viescloud.llc.dns_manager_2.model.nginx.NginxProxyHostResponse;

import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
public class DnsService {
    
    private final DnsSetting dnsSetting;
    private final NginxService nginxService;
    private final CloudflareService cloudflareService;

    public void clearDnsRecordsCache() {
        nginxService.clearCache();
        cloudflareService.clearCache();
    }

    public List<DnsRecord> getDnsRecordList() {
        return new ArrayList<>(this.getDnsRecordMap().values());
    }

    public Map<String, DnsRecord> getDnsRecordMap() {
        this.syncDnsRecord();
        var recordMap = new HashMap<String, DnsRecord>();
        var dnsMap = new HashMap<String, String>();

        this.fetchAllNginxDnsRecords(recordMap, dnsMap);
        this.fetchAllCloudflareDnsRecords(recordMap, dnsMap);
        return recordMap;
    }

    public List<NginxCertificateResponse> getAllNginxCertificate(String type) {
        return this.nginxService.getAllCertificate();
    }

    private void fetchAllCloudflareDnsRecords(Map<String, DnsRecord> recordMap, Map<String, String> dnsMap) {
        this.fetchAllCloudflareDnsRecords(recordMap, dnsMap, this.cloudflareService, (dns, record) -> {
            record.getCloudflareViescloudRecord().add(dns);
            return record;
        });
    }

    private void fetchAllCloudflareDnsRecords(Map<String, DnsRecord> recordMap, Map<String, String> dnsMap,
            CloudflareService cloudflareService, BiFunction<CloudflareResult, DnsRecord, DnsRecord> function) {
        var list = cloudflareService.getAllCloudflareCnameRecord();

        list.forEach(dns -> {

            if (!dnsMap.containsKey(dns.getName())) {
                return;
            }

            var url = dnsMap.get(dns.getName());

            DnsRecord record = null;

            if (!recordMap.containsKey(url)) {
                record = new DnsRecord();
                record.setUri(URI.create(url));
                recordMap.put(url, record);
            } else {
                record = recordMap.get(url);
            }

            record = function.apply(dns, record);
        });
    }

    private void fetchAllNginxDnsRecords(Map<String, DnsRecord> recordMap, Map<String, String> dnsMap) {
        this.fetchAllNginxDnsRecords(recordMap, dnsMap, this.nginxService, (proxyHost, record) -> {
            record.setNginxRecord(proxyHost);
            return record;
        });
    }
    
    private void fetchAllNginxDnsRecords(Map<String, DnsRecord> recordMap, Map<String, String> dnsMap,
            NginxService service, BiFunction<NginxProxyHostResponse, DnsRecord, DnsRecord> function) {
        var proxyHosts = service.getAllProxyHost();

        proxyHosts.forEach(proxyHost -> {
            String url = String.format("%s://%s:%s", proxyHost.getForwardScheme(), proxyHost.getForwardHost(),
                    proxyHost.getForwardPort());
            DnsRecord record = null;

            if (!recordMap.containsKey(url)) {
                record = new DnsRecord();
                record.setUri(URI.create(url));
                recordMap.put(url, record);
            } else {
                record = recordMap.get(url);
            }

            record = function.apply(proxyHost, record);

            proxyHost.getDomainNames().forEach(domainName -> {
                if (!dnsMap.containsKey(domainName))
                    dnsMap.put(domainName, url);
            });
        });
    }

    public void putDnsRecordList(List<DnsRecord> recordList) {
        recordList.forEach(this::putDnsRecord);
    }

    public void putDnsRecord(DnsRecord record) {
        String uri = record.getUri().toString();
        var currentDnsRecord = this.getDnsRecordMap().get(uri);
        var publicNginxRecord = record.getNginxRecord();

        if (publicNginxRecord != null)
            this.putDnsRecord(uri, publicNginxRecord);

        if (currentDnsRecord != null && currentDnsRecord.getNginxRecord() != null && publicNginxRecord == null)
            this.nginxService.deleteProxyHostByUri(uri);
    }

    private void putDnsRecord(String uri, NginxProxyHostResponse response) {
        this.putNginxRecord(uri, response, this.nginxService);
        this.putCloudflareRecord(response, this.cloudflareService, this.dnsSetting.getDomain(), this.dnsSetting.isCloudflareProxied());
    }

    private void putNginxRecord(String uri, NginxProxyHostResponse record, NginxService service) {
        if (service.getProxyHostByUri(uri) == null)
            service.createProxyHost(record);
        else {
            service.putProxyHost(record);
        }
    }

    private void putCloudflareRecord(NginxProxyHostResponse record, CloudflareService cloudflareService, String dns, boolean proxied) {
        if(ObjectUtils.isEmpty(record.getDomainNames())) {
            return;
        }

        record.getDomainNames().forEach(domainName -> {
            putCloudflareDns(cloudflareService, dns, proxied, domainName);
        });
    }

    private void putCloudflareDns(String domainName) {
        this.putCloudflareDns(this.cloudflareService, this.dnsSetting.getDomain(), this.dnsSetting.isCloudflareProxied(), domainName);
    }

    private void putCloudflareDns(CloudflareService cloudflareService, String dns, boolean proxied, String domainName) {
        if (cloudflareService.getCloudflareCnameRecordByName(domainName) == null) {
            var now = DateTime.now();
            var dateTime = String.format("%s-%s-%s at %s-%s-%s ETC", now.getMonth(), now.getDay(), now.getYear(), now.getHour(), now.getMinute(), now.getSecond());
            var request = CloudflareRequest.builder()
                    .name(domainName)
                    .content(dns)
                    .proxied(proxied)
                    .ttl(1)
                    .type("CNAME")
                    .comment(String.format("Auto-added by DNS Manager on: %s", dateTime))
                    .build();
   
            cloudflareService.postCloudflareRecord(request);
        }
    }

    public void deleteDnsRecord(String uri) {
        this.nginxService.deleteProxyHostByUri(uri);
    }

    public void cleanUnusedCloudflareCnameDns() { 
        cleanUnusedCloudflareCnameDns(this.cloudflareService, this.nginxService);
    }

    private void cleanUnusedCloudflareCnameDns(CloudflareService cloudflareService, NginxService nginxService) {
        var cloudflareDnsResult = new ArrayList<>(cloudflareService.getAllCloudflareCnameRecord());
        var domainNames = new ArrayList<>(nginxService.getAllDomainNameList());
        
        for (String domainName : domainNames) {
            cloudflareDnsResult.removeIf(cloudflareDns -> cloudflareDns.getName().equalsIgnoreCase(domainName));
        }
        
        cloudflareDnsResult.forEach(e -> {
            cloudflareService.deleteCloudflareRecord(e.getId());
        });
    }

    private void syncDnsRecord() {
        this.syncDnsRecord(this.cloudflareService, this.nginxService);
    }

    private void syncDnsRecord(CloudflareService cloudflareService, NginxService nginxService) {
        var cloudflareDnsResult = new ArrayList<>(cloudflareService.getAllCloudflareCnameRecord());
        var domainNames = new ArrayList<>(nginxService.getAllDomainNameList());

        for (String domainName : domainNames) {
            var cloudflareDns = Streams.stream(cloudflareDnsResult).filter(e -> e.getName().equalsIgnoreCase(domainName)).findFirst().orElse(null);
            if (cloudflareDns == null) {
                this.putCloudflareDns(domainName);
            }
        }
    }
}
