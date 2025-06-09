package com.viescloud.llc.dns_manager_2;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.domain.EntityScan;
import org.springframework.cloud.openfeign.EnableFeignClients;
import org.springframework.context.annotation.EnableAspectJAutoProxy;
import org.springframework.context.annotation.Import;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;

import com.viescloud.eco.viesspringutils.auto.config.ViesBeanConfig;

@EnableAspectJAutoProxy(proxyTargetClass=true)
@EnableFeignClients
@EnableJpaRepositories
@EntityScan
@Import(ViesBeanConfig.class)
@SpringBootApplication
public class DnsManager2Application {

	public static void main(String[] args) {
		SpringApplication.run(DnsManager2Application.class, args);
	}
}
