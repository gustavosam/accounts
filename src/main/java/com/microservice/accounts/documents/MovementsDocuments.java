package com.microservice.accounts.documents;

import lombok.Getter;
import lombok.Setter;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import java.time.LocalDate;

@Document(collection = "movements")
@Getter
@Setter
public class MovementsDocuments {

    @Id
    private String movementNumber;

    private String movementType;

    private String customerDocument;

    private String accountType;

    private String accountNumber;

    private String cardNumber;

    private String creditId;

    private Double amount;

    private LocalDate movementDate;
}
