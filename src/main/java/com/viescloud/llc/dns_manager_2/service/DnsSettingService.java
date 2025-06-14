package com.viescloud.llc.dns_manager_2.service;

import org.springframework.stereotype.Service;

import com.viescloud.eco.viesspringutils.repository.DatabaseCall;
import com.viescloud.eco.viesspringutils.service.ViesService;
import com.viescloud.llc.dns_manager_2.dao.DnsSettingDao;
import com.viescloud.llc.dns_manager_2.model.DnsSetting;

@Service
public class DnsSettingService extends ViesService<Long, DnsSetting, DnsSettingDao> {

    public DnsSettingService(DatabaseCall<Long, DnsSetting> databaseCall, DnsSettingDao repositoryDao) {
        super(databaseCall, repositoryDao);
    }

    @Override
    public Long getIdFieldValue(DnsSetting object) {
        return object.getId();
    }

    @Override
    public void setIdFieldValue(DnsSetting object, Long id) {
        object.setId(id);
    }

    // @Override
    // protected boolean disableCheckNotNullField() {
    //     return true;
    // }

    // @Override
    // protected boolean disableCheckValidUniqueField() {
    //     return true;
    // }
    
}
