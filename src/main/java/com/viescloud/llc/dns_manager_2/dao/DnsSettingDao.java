package com.viescloud.llc.dns_manager_2.dao;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.viescloud.llc.dns_manager_2.model.DnsSetting;

@Repository
public interface DnsSettingDao extends JpaRepository<DnsSetting, Long> {
    public List<String> findAllByDomain(String domain);
    public List<String> findAllByCloudflareEmail(String email);
    public List<String> findAllByCloudflareKey(String key);
    public List<String> findAllByCloudflareZoneId(String zoneId);

    public List<String> findAllByNginxBaseUrl(String baseUrl);
    public List<String> findAllByNginxEmail(String email);
    public List<String> findAllByNginxPassword(String password);
}
