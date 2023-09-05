package com.microservice.accounts.util;

import com.microservice.accounts.model.AccountRetireDeposit;
import lombok.Getter;
import lombok.Setter;

/**
 * Esta clase extiende de AccountRetireDeposit.
 * */
@Getter
@Setter
public class ErrorRetire extends AccountRetireDeposit {

  private static ErrorRetire instance;

  private ErrorRetire() {

  }

  /**
   * Este método genera una sola instancia y setea un mensaje de error.
   * */
  public static ErrorRetire getInstance(String mensaje) {
    if (instance == null) {
      instance = new ErrorRetire();
    }
    instance.setMessage(mensaje);
    return instance;
  }
}
