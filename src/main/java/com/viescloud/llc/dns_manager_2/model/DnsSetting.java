package com.viescloud.llc.dns_manager_2.model;

import java.io.Serializable;

import com.viescloud.eco.viesspringutils.config.jpa.BooleanConverter;
import com.viescloud.eco.viesspringutils.interfaces.annotation.Decoding;
import com.viescloud.eco.viesspringutils.model.DecodingType;
import com.viescloud.eco.viesspringutils.util.Booleans;

import jakarta.persistence.Column;
import jakarta.persistence.Convert;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.Id;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Entity
@Builder
public class DnsSetting implements Serializable {

    @Id
    @GeneratedValue(strategy = jakarta.persistence.GenerationType.IDENTITY)
    private Long id;
    
    @Column(columnDefinition = "TEXT", unique = true, nullable = false)
    private String domain;
    
    @Column(columnDefinition = "TEXT", nullable = false)
    private String cloudflareEmail;
    
    @Column(columnDefinition = "TEXT", nullable = false)
    @Decoding(DecodingType.NULL)
    private String cloudflareKey;
    
    @Column(columnDefinition = "TEXT", unique = true, nullable = false)
    @Decoding(DecodingType.NULL)
    private String cloudflareZoneId;

    @Builder.Default
    @Column(columnDefinition = "TEXT", nullable = false)
    @Convert(converter = BooleanConverter.class)
    private Boolean cloudflareProxied = true;

    @Column(columnDefinition = "TEXT", nullable = false)
    private String nginxBaseUrl;
    
    @Column(columnDefinition = "TEXT", nullable = false)
    private String nginxEmail;
    
    @Column(columnDefinition = "TEXT", nullable = false)
    @Decoding(DecodingType.NULL)
    private String nginxPassword;

    @Column(columnDefinition = "TEXT")
    private String nginxCustomConfiguration;

    public boolean isCloudflareProxied() {
        return Booleans.isTrue(cloudflareProxied);
    }
}