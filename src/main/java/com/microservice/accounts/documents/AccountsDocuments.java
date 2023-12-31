package com.microservice.accounts.documents;

import com.microservice.accounts.util.complementary.SignersComplementary;
import com.microservice.accounts.util.complementary.TitularsComplementary;
import java.time.LocalDate;
import java.util.List;
import lombok.Getter;
import lombok.Setter;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;


/**
 * Esta clase representa a la colleción accounts en mongo db.
 * */
@Document(collection = "accounts")
@Getter
@Setter
public class AccountsDocuments {

  @Id
  private String accountNumber;

  private String accountType;

  private Double accountAmount;

  private Integer freeMovements;

  private String clientDocument;

  private List<TitularsComplementary> titulars;

  private List<SignersComplementary> signers;

  private LocalDate accountCreationDate;
}
