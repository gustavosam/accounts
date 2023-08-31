package com.microservice.accounts.repository;

import com.microservice.accounts.documents.MovementsDocuments;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface MovementsRepository extends MongoRepository<MovementsDocuments, String> {
}
