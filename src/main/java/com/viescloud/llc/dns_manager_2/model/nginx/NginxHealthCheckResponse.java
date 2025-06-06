package com.viescloud.llc.dns_manager_2.model.nginx;

import java.io.Serializable;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class NginxHealthCheckResponse implements Serializable {
    private String status;
}
