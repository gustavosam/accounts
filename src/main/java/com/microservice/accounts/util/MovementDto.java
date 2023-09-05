package com.microservice.accounts.util;

import lombok.Getter;
import lombok.Setter;

/**
 * Esta clase contendrá los atributos que se enviarán
 * al microservicio de movements.
 * */
@Getter
@Setter
public class MovementDto {

  private Double amount;

  private String clientDocument;

  private String accountNumber;

  private String movementType;

  private Double commission;

}
