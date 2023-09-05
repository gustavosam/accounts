package com.microservice.accounts.util;

import com.microservice.accounts.model.AccountRetireDeposit;
import lombok.Getter;
import lombok.Setter;

/**
 * Esta clase extiende de AccountRetireDeposit y añade atributos para mostrar
 * al cliente.
 * */
@Getter
@Setter
public class AccountRetireDepositDto extends AccountRetireDeposit {

  private String accountNumber;

  private String accountType;

  private Double accountAmount;
}
