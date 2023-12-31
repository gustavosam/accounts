package com.microservice.accounts;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.client.discovery.EnableDiscoveryClient;
import org.springframework.cloud.openfeign.EnableFeignClients;

/**
 * La clase principal del microservicio accounts.
 * */

@EnableDiscoveryClient
@SpringBootApplication
//@EnableFeignClients
public class AccountsApplication {

  public static void main(String[] args) {
    SpringApplication.run(AccountsApplication.class, args);
  }

}
