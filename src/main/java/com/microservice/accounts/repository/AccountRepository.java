package com.microservice.accounts.repository;

import com.microservice.accounts.documents.AccountsDocuments;
import org.springframework.data.mongodb.repository.MongoRepository;

import java.util.List;

public interface AccountRepository extends MongoRepository<AccountsDocuments, String> {

    List<AccountsDocuments> findByCustomerDocument(String customerDocument);
}
