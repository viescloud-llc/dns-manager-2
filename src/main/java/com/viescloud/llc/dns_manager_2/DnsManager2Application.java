package com.viescloud.llc.dns_manager_2;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.EnableAspectJAutoProxy;

import com.viescloud.eco.viesspringutils.ViesApplication;

@SpringBootApplication
@EnableAspectJAutoProxy(proxyTargetClass=true)
public class DnsManager2Application extends ViesApplication {

	public static void main(String[] args) {
		SpringApplication.run(DnsManager2Application.class, args);
	}

	@Override
	public String getApplicationName() {
		return "dns-manager-2";
	}

}
