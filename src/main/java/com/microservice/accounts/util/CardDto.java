package com.microservice.accounts.util;

import java.time.LocalDate;
import lombok.Getter;
import lombok.Setter;


/**
 * Esta clase contendrá información de la tarjeta que se obtendrá
 * en el microservicio de creditcard.
 * */
@Getter
@Setter
public class CardDto {

  private String cardNumber;

  private Double cardAmount;

  private Double consumed;

  private Double available;

  private String clientDocument;

  private LocalDate creationDateCard;
}
