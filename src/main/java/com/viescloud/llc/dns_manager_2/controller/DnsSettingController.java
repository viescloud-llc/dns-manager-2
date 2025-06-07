package com.viescloud.llc.dns_manager_2.controller;

import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.viescloud.eco.viesspringutils.auto.controller.ViesAutoController;
import com.viescloud.llc.dns_manager_2.model.DnsSetting;
import com.viescloud.llc.dns_manager_2.service.DnsSettingService;

@RestController
@RequestMapping("/api/v1/dns/settings")
public class DnsSettingController extends ViesAutoController<Long, DnsSetting, DnsSettingService> {

    public DnsSettingController(DnsSettingService service) {
        super(service);
    }

    @Override
    protected boolean enabled() {
        return true;
    }
    
}
