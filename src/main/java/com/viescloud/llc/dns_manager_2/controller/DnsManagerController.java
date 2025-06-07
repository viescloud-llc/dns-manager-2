package com.viescloud.llc.dns_manager_2.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.viescloud.eco.viesspringutils.exception.HttpResponseThrowers;
import com.viescloud.eco.viesspringutils.interfaces.annotation.Permission;
import com.viescloud.llc.dns_manager_2.model.DnsRecord;
import com.viescloud.llc.dns_manager_2.service.DnsService;
import com.viescloud.llc.dns_manager_2.service.DnsServiceFactory;
import com.viescloud.llc.dns_manager_2.service.DnsSettingService;

@RestController
@RequestMapping("/api/v1/dns/manager")
public class DnsManagerController {
    
    @Autowired
    private DnsSettingService dnsSettingService;

    @Autowired
    private DnsServiceFactory dnsServiceFactory;

    private DnsService getDnsService(Long dnsSettingId) {
        var dnsSetting = this.dnsSettingService.getByIdOptional(dnsSettingId)
                                               .orElseThrow(HttpResponseThrowers.throwNotFoundException("Dns setting not found"));
        return this.dnsServiceFactory.getDnsService(dnsSetting);
    }

    @GetMapping("{dnsSettingId}")
    public ResponseEntity<?> getAllDnsRecord(@PathVariable Long dnsSettingId, @RequestParam(required = false, defaultValue = "false") boolean map) {
        var records = map ? getDnsService(dnsSettingId).getDnsRecordMap() : getDnsService(dnsSettingId).getDnsRecordList();
        return ResponseEntity.ok(records);
    }

    @GetMapping("/nginx/certificates/{dnsSettingId}")
    public ResponseEntity<?> getAllNginxCertificate(@PathVariable Long dnsSettingId, @RequestParam String type) {
        return ResponseEntity.ok(getDnsService(dnsSettingId).getAllNginxCertificate(type));
    }

    @PutMapping("{dnsSettingId}")
    // @Permission("ADMIN")
    public ResponseEntity<?> putDnsRecord(@PathVariable Long dnsSettingId, @RequestBody DnsRecord record, @RequestParam(required = false, defaultValue = "false") boolean cleanUnusedCloudflareCnameDns) {
        getDnsService(dnsSettingId).putDnsRecord(record);
        if (cleanUnusedCloudflareCnameDns) {
            getDnsService(dnsSettingId).cleanUnusedCloudflareCnameDns();
        }
        return ResponseEntity.ok().build();
    }

    @DeleteMapping("/clear-unused-dns/{dnsSettingId}")
    // @Permission("ADMIN")
    public ResponseEntity<?> clearUnusedDnsRecordsCache(@PathVariable Long dnsSettingId) {
        getDnsService(dnsSettingId).cleanUnusedCloudflareCnameDns();
        return ResponseEntity.ok().build();
    }

    @DeleteMapping("/clear-cache/{dnsSettingId}")
    // @Permission("ADMIN")
    public ResponseEntity<?> clearDnsRecordsCache(@PathVariable Long dnsSettingId) {
        getDnsService(dnsSettingId).clearDnsRecordsCache();
        return ResponseEntity.ok().build();
    }

    @DeleteMapping("{dnsSettingId}")
    // @Permission("ADMIN")
    public ResponseEntity<?> deleteDnsRecord(@PathVariable Long dnsSettingId, @RequestParam String uri, @RequestParam(required = false, defaultValue = "false") boolean cleanUnusedCloudflareCnameDns) {
        getDnsService(dnsSettingId).deleteDnsRecord(uri);
        if (cleanUnusedCloudflareCnameDns) {
            getDnsService(dnsSettingId).cleanUnusedCloudflareCnameDns();
        }
        return ResponseEntity.ok().build();
    }
}
