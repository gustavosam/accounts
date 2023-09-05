package com.microservice.accounts.repository;

import com.microservice.accounts.documents.AccountsDocuments;
import java.util.List;
import org.springframework.data.mongodb.repository.MongoRepository;

/**
 * Esta interfaz es la encargada de contener los métodos para
 * el crud de cuentas.
 * */
public interface AccountRepository extends MongoRepository<AccountsDocuments, String> {

  List<AccountsDocuments> findByClientDocument(String customerDocument);
}
