package com.microservice.accounts.util;

import com.microservice.accounts.model.Account;
import lombok.Getter;
import lombok.Setter;

/**
 * Esta clase extiende de Account y genera una sola instancia
 * Solo recibe un parametro que será el message de error.
 * */
@Getter
@Setter
public class ErrorC extends Account {

  private static ErrorC instance;

  private ErrorC() {

  }

  /**
   * Este método genera una sola instancia y se asigna un mensaje de error.
   * */
  public static ErrorC getInstance(String mensaje) {
    if (instance == null) {
      instance = new ErrorC();
    }
    instance.setMessage(mensaje);
    return instance;
  }
}
