package com.microservice.accounts.configuration;

import com.microservice.accounts.model.Account;
import com.microservice.accounts.util.Constants;
import com.microservice.accounts.util.ErrorC;
import feign.FeignException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

/**
 * Esta clase permite capturar y mostrar un mensaje personalizado
 * cuando el microservice client arroja un 404.
 * */
@RestControllerAdvice
public class ClientExceptionHandler {

  /**
   * Este método retorna un mensaje y un código de estado 404
   * solo si, el microservicio client retorna un 404.
   * */
  @ExceptionHandler(FeignException.NotFound.class)
  public ResponseEntity<Account> handleFeignNotFound(FeignException.NotFound ex) {

    return ResponseEntity.status(HttpStatus.NOT_FOUND)
            .body(ErrorC.getInstance(Constants.CLIENT_NOT_EXIST));
  }
}
