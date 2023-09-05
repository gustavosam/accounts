package com.microservice.accounts.feignclient;

import com.microservice.accounts.util.ClientDto;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

/**
 * Esta interfaz permite comunicarse con el microservicio de client.
 * */
@FeignClient(name = "ms-customers", url = "localhost:8080")
public interface CustomerFeignClient {

  /**
   * Este método permite obtener la información del cliente.
   * */
  @GetMapping("/client/{document}")
  ClientDto getClient(@PathVariable String document);
}
