package com.microservice.accounts.util;

import java.time.LocalDate;
import lombok.Getter;
import lombok.Setter;

/**
 * Esta clase contendrá la información que recibiría del microservicio
 * cliente.
 * */
@Getter
@Setter
public class ClientDto {

  private String document;

  private String name;

  private String clientType;

  private String email;

  private Boolean isActive;

  private LocalDate clientCreationDate;
}
