package com.viescloud.llc.dns_manager_2;

import static org.junit.jupiter.api.Assertions.assertNotNull;

import java.net.URI;

import org.junit.jupiter.api.Test;
import org.springframework.http.HttpMethod;
import org.springframework.web.client.RestTemplate;

import com.viescloud.eco.viesspringutils.util.WebCall;

public class LiveTest {
    
    private RestTemplate restTemplate = new RestTemplate();

    // @Test
    public void testGetObjectStorage() {
        URI Uri = URI.create("http://localhost:8085/api/v1/object/storages/file?path=/1/Test.txt");

        var data = WebCall.of(restTemplate, byte[].class)
                          .request(HttpMethod.GET, Uri.toString())
                          .header("user_id", "1")
                          .skipRestClientError(true)
                          .logRequest(true)
                          .exchange()
                          .logResponseInFormat()
                          .getOptionalResponseBody()
                          .orElse(null);

        assertNotNull(data);
        String dataString = new String(data);
        System.out.println(dataString);
    }

    // @Test
    public void testTestMetadataStorage() {
        URI Uri = URI.create("http://localhost:8085/api/v1/object/storages/metadata/all");

        var data = WebCall.of(restTemplate, String.class)
                          .request(HttpMethod.GET, Uri.toString())
                          .header("user_id", "1")
                          .skipRestClientError(true)
                          .logRequest(true)
                          .exchange()
                          .logResponseInFormat()
                          .getOptionalResponseBody()
                          .orElse(null);

        assertNotNull(data);
        System.out.println(data);
    }

}
