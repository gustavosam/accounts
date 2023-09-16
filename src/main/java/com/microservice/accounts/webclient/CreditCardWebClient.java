package com.microservice.accounts.webclient;

import com.microservice.accounts.util.CardDto;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Flux;

@Component
public class CreditCardWebClient {

  @Autowired
  private WebClient.Builder webClientBuilder;
  public Flux<CardDto> getCreditCards(String document){
    return webClientBuilder.build()
            .get()
            .uri("http://localhost:8082/card/customer/{document}", document)
            .retrieve()
            .bodyToFlux(CardDto.class);
  }


}
