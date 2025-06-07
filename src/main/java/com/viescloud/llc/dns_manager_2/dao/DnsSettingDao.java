package com.viescloud.llc.dns_manager_2.dao;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.viescloud.llc.dns_manager_2.model.DnsSetting;

@Repository
public interface DnsSettingDao extends JpaRepository<DnsSetting, Long> {
    
}
