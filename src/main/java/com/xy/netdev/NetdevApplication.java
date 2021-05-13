package com.xy.netdev;


import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.web.bind.annotation.RestController;
import springfox.documentation.swagger2.annotations.EnableSwagger2;

@SpringBootApplication
@EnableSwagger2
@RestController
@EnableAsync
public class NetdevApplication {
    public static void main(String[] args) {
        SpringApplication.run(NetdevApplication.class, args);
    }
}
