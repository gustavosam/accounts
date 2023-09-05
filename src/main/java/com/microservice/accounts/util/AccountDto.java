package com.microservice.accounts.util;

import com.microservice.accounts.model.Account;
import com.microservice.accounts.util.complementary.SignersComplementary;
import com.microservice.accounts.util.complementary.TitularsComplementary;
import java.time.LocalDate;
import java.util.List;
import lombok.Getter;
import lombok.Setter;

/**
 * Esta clase extiende de Account y añade atributos para mostrar al cliente.
 * */
@Getter
@Setter
public class AccountDto extends Account {

  private String accountNumber;

  private String accountType;

  private Double accountAmount;

  private Integer quantityMovements;

  private String clientDocument;

  private LocalDate creationDate;

  private List<TitularsComplementary> titulars;

  private List<SignersComplementary> signers;

}
