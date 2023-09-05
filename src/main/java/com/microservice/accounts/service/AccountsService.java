package com.microservice.accounts.service;

import com.microservice.accounts.documents.AccountsDocuments;
import com.microservice.accounts.model.Account;
import com.microservice.accounts.model.AccountRequest;
import com.microservice.accounts.model.Signers;
import com.microservice.accounts.model.SignersRequired;
import com.microservice.accounts.model.TitularsIn;
import com.microservice.accounts.util.AccountRetireDepositDto;
import com.microservice.accounts.util.CardDto;
import com.microservice.accounts.util.ClientDto;
import java.util.List;

/**
 * Esta interfaz contiene los métodos que serán implementados por AccountServiceImpl.
 * */
public interface AccountsService {

  Account createAccount(AccountRequest accountRequest);

  ClientDto getClient(String customerDocument);

  List<AccountsDocuments> getAccounts(String customerDocument);

  Boolean existAccountAhorro(List<AccountsDocuments> accountsDocuments);

  Boolean existAccountCorriente(List<AccountsDocuments> accountsDocuments);

  Boolean listTitularIsCorrect(List<TitularsIn> titulars);

  Boolean justOneTitularPersonal(List<TitularsIn> titulars);

  Boolean validateIfYouCanRetire(AccountsDocuments accountsDocuments, Double amount);

  AccountsDocuments getAccount(String accountNumber);

  AccountRetireDepositDto retireAccount(AccountsDocuments accountsDocuments, Double amountToRetire);

  Boolean validateIfYouCanDeposit(Double commission, Double amountDeposit);

  AccountRetireDepositDto depositAccount(AccountsDocuments accounts, Double amountToDeposit);

  Boolean canAddSigners(AccountsDocuments accounts, List<SignersRequired> signers);

  Account addSigner(AccountsDocuments accountsDocuments, List<SignersRequired> signersRequired);

  Boolean validateQuantitySignersCreationAccount(List<Signers> signersRequired);

  Boolean listSignersIsCorrect(List<Signers> signersRequired);

  Boolean listSignersRequiredIsCorrect(List<SignersRequired> signers);

  List<CardDto> getCreditCards(String clientDocument);
}
