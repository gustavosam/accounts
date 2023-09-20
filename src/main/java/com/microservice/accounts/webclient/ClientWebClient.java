package com.microservice.accounts.webclient;

import com.microservice.accounts.util.ClientDto;
import io.github.resilience4j.circuitbreaker.annotation.CircuitBreaker;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.web.reactive.function.client.WebClientResponseException;
import reactor.core.publisher.Mono;

@Component
public class ClientWebClient {

  @Autowired
  private WebClient.Builder webClientBuilder;

  @CircuitBreaker(name = "circuitBreakerClient", fallbackMethod = "fallbackMethod")
  public Mono<ClientDto> getClient(String document){
    return webClientBuilder.build()
            .get()
            .uri("http://localhost:8080/client/{document}", document)
            .retrieve()
            .bodyToMono(ClientDto.class)

            .onErrorResume(WebClientResponseException.class, ex -> {
              if (ex.getStatusCode().equals(HttpStatus.NOT_FOUND)) {
                ClientDto clientDto = new ClientDto();
                clientDto.setDocument("NOT_EXIST");
                return Mono.just(clientDto);
              }
              return Mono.error(ex);
            });
  }

  public Mono<ClientDto> fallbackMethod(String document, Exception ex) {

    ClientDto clientDto = new ClientDto();
    return Mono.just(clientDto);
  }
}
