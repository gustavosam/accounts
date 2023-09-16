package com.microservice.accounts.service;

import com.microservice.accounts.documents.AccountsDocuments;
import com.microservice.accounts.model.*;
import com.microservice.accounts.util.*;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.util.List;

/**
 * Esta interfaz contiene los métodos que serán implementados por AccountServiceImpl.
 * */
public interface AccountsService {

  Mono<AccountDto> createAccount(AccountRequest accountRequest);

  Mono<ClientDto> getClient(String customerDocument);

  Flux<AccountsDocuments> getAccounts(String customerDocument);

  Mono<Boolean> existAccountAhorro(Flux<AccountsDocuments> accountsDocuments);

  Mono<Boolean> existAccountCorriente(Flux<AccountsDocuments> accountsDocuments);

  Boolean listTitularIsCorrect(List<TitularsIn> titulars);

  Boolean justOneTitularPersonal(List<TitularsIn> titulars);

  Boolean validateIfYouCanRetire(AccountsDocuments accountsDocuments, Double amount);

  Mono<AccountsDocuments> getAccount(String accountNumber);

  Mono<AccountRetireDepositDto> retireAccount(AccountsDocuments accountsDocuments, Double amountToRetire);

  Mono<TransferDto> transfer(AccountsDocuments accountOri, AccountsDocuments accountDest,
                       Double amountTransfer);

  Boolean validateIfYouCanDeposit(Double commission, Double amountDeposit);

  Mono<AccountRetireDepositDto> depositAccount(AccountsDocuments accounts, Double amountToDeposit);

  Boolean canAddSigners(AccountsDocuments accounts, List<SignerList> signers);

  Mono<Account> addSigner(AccountsDocuments accountsDocuments, List<SignerList> signersRequired);

  Boolean validateQuantitySignersCreationAccount(List<Signers> signersRequired);

  Boolean listSignersIsCorrect(List<Signers> signersRequired);

  Boolean listSignersRequiredIsCorrect(List<SignerList> signers);

  Flux<CardDto> getCreditCards(String clientDocument);

  Flux<Account> getAccountsByClient(String document);
}
