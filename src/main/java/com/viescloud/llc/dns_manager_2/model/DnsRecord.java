package com.viescloud.llc.dns_manager_2.model;

import java.io.Serializable;
import java.net.URI;
import java.util.ArrayList;
import java.util.List;

import com.viescloud.llc.dns_manager_2.model.cloudflare.CloudflareResult;
import com.viescloud.llc.dns_manager_2.model.nginx.NginxProxyHostResponse;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class DnsRecord implements Serializable {
    private URI uri;
    private NginxProxyHostResponse nginxRecord;
    @Builder.Default
    private List<CloudflareResult> cloudflareViescloudRecord = new ArrayList<>();
}
