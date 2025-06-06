package com.viescloud.llc.dns_manager_2.model.nginx;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.time.OffsetDateTime;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class NginxLoginResponse implements Serializable {
    private String token;
    private OffsetDateTime expires;
}
