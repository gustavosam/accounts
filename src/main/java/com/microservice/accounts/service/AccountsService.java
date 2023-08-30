package com.microservice.accounts.service;

import com.microservice.accounts.documents.AccountsDocuments;
import com.microservice.accounts.model.*;
import com.microservice.accounts.util.complementary.CustomersComplementary;

import java.util.List;

public interface AccountsService {

    Account createAccount(AccountRequest accountRequest);

    CustomersComplementary getCustomer(String customerDocument);

    Boolean payCommission(String accountType);

    Boolean ilimitMovements(String accountType);

    List<AccountsDocuments> getAccountsByCustomer(String customerDocument);

    Boolean existAccountAhorro(List<AccountsDocuments> accountsDocuments);

    Boolean existAccountCorriente(List<AccountsDocuments> accountsDocuments);

    Boolean titularsEmpty(List<TitularsIn> titulars);

    Boolean justATitularPersonal(List<TitularsIn> titulars);

    Boolean validateIfYouCanRetireCorriente(AccountsDocuments accountsDocuments, Double amount);

    Boolean validateIfYouCanRetire(AccountsDocuments accountsDocuments, Double amount);

    AccountsDocuments getAccount(String accountNumber);

    AccountRetireDeposit retireAccount(AccountsDocuments accountsDocuments, Double amountToRetire);

    Boolean validateIfYouCanDeposit(AccountsDocuments accountsDocuments);

    AccountRetireDeposit depositAccount(AccountsDocuments accountsDocuments, Double amountToDeposit);

    Boolean validateIfYouCanAddSigners(AccountsDocuments accountsDocuments, List<SignersRequired> signersRequired);

    Account addSigner(AccountsDocuments accountsDocuments, List<SignersRequired> signersRequired);
}
