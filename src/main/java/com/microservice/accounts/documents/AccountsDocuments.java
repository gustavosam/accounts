package com.microservice.accounts.documents;

import com.microservice.accounts.util.complementary.SignersDocumentComplementary;
import com.microservice.accounts.util.complementary.TitularsDocumentComplementary;
import lombok.Getter;
import lombok.Setter;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import java.time.LocalDate;
import java.util.List;

@Document(collection = "accounts")
@Getter
@Setter
public class AccountsDocuments {

    @Id
    private String accountNumber;

    private String accountType;

    private Double accountAmount;

    private Double accountCommission;

    private Boolean unlimitedMovements;

    private Integer quantityMovements;

    private String customerDocument;

    private List<TitularsDocumentComplementary> titulars;

    private List<SignersDocumentComplementary> signers;

    private LocalDate accountCreationDate;
}
