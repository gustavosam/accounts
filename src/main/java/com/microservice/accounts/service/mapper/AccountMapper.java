package com.microservice.accounts.service.mapper;

import com.microservice.accounts.documents.AccountsDocuments;
import com.microservice.accounts.model.*;
import com.microservice.accounts.util.AccountDto;
import com.microservice.accounts.util.AccountRetireDepositDto;
import com.microservice.accounts.util.complementary.SignersComplementary;
import com.microservice.accounts.util.complementary.TitularsComplementary;
import java.util.Objects;
import java.util.stream.Collectors;


/**
 * Esta clase contiene métodos mappers para convertir una clase a otra.
 * */
public class AccountMapper {


  /**
   * Este método convierte la clase AccountRequest en AccountDocuments.
   * */
  public static AccountsDocuments mapAccountRequestToAccountsDocuments(AccountRequest account) {
    AccountsDocuments accountsDocuments = new AccountsDocuments();

    accountsDocuments.setAccountAmount(account.getAccountAmount());
    accountsDocuments.setAccountType(account.getAccountType().getValue());
    accountsDocuments.setClientDocument(account.getClientDocument());

    if (account.getTitulars() != null) {
      accountsDocuments.setTitulars(
              account.getTitulars().stream()
                      .filter(Objects::nonNull)
                      .map(AccountMapper::mapTitularInToTitularComplementary)
                      .collect(Collectors.toList())
      );
    }

    if (account.getSigners() != null) {
      accountsDocuments.setSigners(
              account.getSigners().stream()
                      .filter(Objects::nonNull)
                      .map(AccountMapper::mapSignerToSignerComplementary)
                      .collect(Collectors.toList())
      );
    }
    return accountsDocuments;
  }

  /**
   * Este método convierte la clase TitularIn en TitularComplementary.
   * */
  public static TitularsComplementary mapTitularInToTitularComplementary(TitularsIn titulars) {
    TitularsComplementary complementary = new TitularsComplementary();

    complementary.setDocument(titulars.getDocument());
    complementary.setName(titulars.getName());

    return complementary;
  }

  /**
   * Este método convierte la clase Signers en SignersComplementary.
   * */
  public static SignersComplementary mapSignerToSignerComplementary(Signers signers) {
    SignersComplementary complementary = new SignersComplementary();

    complementary.setDocument(signers.getDocument());
    complementary.setFullName(signers.getFullName());

    return complementary;
  }

  /**
   * Esta método convierte la clase SignersRequired en SignersComplementary.
   * */
  public static SignersComplementary mapSignerReToSignerComplementary(SignerList signers) {
    SignersComplementary complementary = new SignersComplementary();

    complementary.setDocument(signers.getDocument());
    complementary.setFullName(signers.getFullName());

    return complementary;
  }

  /**
   * Esta método convierte la clase AccountsDocuments en AccountDto.
   * */
  public static AccountDto mapAccountDocToAccountDto(AccountsDocuments accountsDocuments) {

    AccountDto account = new AccountDto();

    account.setAccountAmount(accountsDocuments.getAccountAmount());
    account.setAccountNumber(accountsDocuments.getAccountNumber());
    account.setAccountType(accountsDocuments.getAccountType());
    account.setQuantityMovements(accountsDocuments.getFreeMovements());
    account.setClientDocument(accountsDocuments.getClientDocument());
    account.setCreationDate(accountsDocuments.getAccountCreationDate());

    if (accountsDocuments.getTitulars() != null) {
      account.setTitulars(
              accountsDocuments.getTitulars()
      );
    }

    if (accountsDocuments.getSigners() != null) {
      account.setSigners(
              accountsDocuments.getSigners()
      );
    }
    return account;
  }



  /**
   * Este método convierte la clase AccountsDocuments a AccountRetireDepositDto.
   * */
  public static AccountRetireDepositDto mapAccountDocToAccountRetDep(AccountsDocuments account) {

    AccountRetireDepositDto accountRetireDeposit = new AccountRetireDepositDto();

    accountRetireDeposit.setAccountNumber(account.getAccountNumber());
    accountRetireDeposit.setAccountAmount(account.getAccountAmount());
    accountRetireDeposit.setAccountType(account.getAccountType());

    return accountRetireDeposit;
  }

}
