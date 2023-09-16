package com.microservice.accounts.repository;

import com.microservice.accounts.documents.AccountsDocuments;
import java.util.List;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.data.mongodb.repository.ReactiveMongoRepository;
import reactor.core.publisher.Flux;

/**
 * Esta interfaz es la encargada de contener los métodos para
 * el crud de cuentas.
 * */
public interface AccountRepository extends ReactiveMongoRepository<AccountsDocuments, String> {

  Flux<AccountsDocuments> findByClientDocument(String customerDocument);
}
