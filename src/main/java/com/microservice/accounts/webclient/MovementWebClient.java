package com.microservice.accounts.webclient;

import com.microservice.accounts.util.MovementDto;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.BodyInserters;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;

@Component
public class MovementWebClient {

  @Autowired
  private WebClient.Builder webClientBuilder;

  public Mono<Void> saveMovement(MovementDto movementDto){
    return webClientBuilder.build()
            .post()
            .uri("http://localhost:8084/movement")
            .body(BodyInserters.fromValue(movementDto))
            .retrieve()
            .bodyToMono(Void.class);
  }
}
