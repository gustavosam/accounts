package com.microservice.accounts.util;

import com.microservice.accounts.model.Account;
import lombok.Getter;
import lombok.Setter;

/**
 * Esta clase muestra los datos de la transferencia.
 * */
@Getter
@Setter
public class TransferDto extends Account {

  private String accountOrigin;

  private String accountDestination;

  private double amount;
}
