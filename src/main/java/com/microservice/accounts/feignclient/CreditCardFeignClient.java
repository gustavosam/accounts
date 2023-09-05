package com.microservice.accounts.feignclient;

import com.microservice.accounts.util.CardDto;
import java.util.List;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

/**
 * Esta interfaz es la encargada de comunicarse con el microservicio de tarjetas de créditos.
 * */
@FeignClient(name = "ms-creditcard", url = "localhost:8082")
public interface CreditCardFeignClient {

  /**
   * Este método permite obtener la información de la tarjeta de crédito.
   * */
  @GetMapping("/card/customer/{clientDocument}")
  List<CardDto> getCreditCards(@PathVariable String clientDocument);
}
