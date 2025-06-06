package com.viescloud.llc.dns_manager_2.model.cloudflare;

import java.io.Serializable;
import java.util.List;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class CloudflareWarper implements Serializable {
    private CloudflareResult result;
    private boolean success;
    private List<Object> errors;
    private List<Object> messages;
}
