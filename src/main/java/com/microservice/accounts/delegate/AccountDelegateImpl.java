package com.microservice.accounts.delegate;

import com.microservice.accounts.api.AccountApiDelegate;
import com.microservice.accounts.documents.AccountsDocuments;
import com.microservice.accounts.model.Account;
import com.microservice.accounts.model.AccountRequest;
import com.microservice.accounts.model.AccountRetireDeposit;
import com.microservice.accounts.model.SignersRequired;
import com.microservice.accounts.model.TransferRequest;
import com.microservice.accounts.service.AccountsService;
import com.microservice.accounts.service.mapper.AccountMapper;
import com.microservice.accounts.util.ClientDto;
import com.microservice.accounts.util.Constants;
import com.microservice.accounts.util.ErrorC;
import com.microservice.accounts.util.ErrorRetire;
import java.util.List;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;


/**
 * Esta clase implementa los métodos generados por open api.
 * */
@Service
public class AccountDelegateImpl implements AccountApiDelegate {

  @Autowired
  private AccountsService accountsService;

  @Override
  public ResponseEntity<Account> createAccount(AccountRequest account) {

    if (account.getAccountType() == null) {
      return ResponseEntity.badRequest().body(ErrorC.getInstance(Constants.ACCOUNT_TYPE_EMPTY));
    }

    if (account.getAccountAmount() == null) {
      return ResponseEntity.badRequest().body(ErrorC.getInstance(Constants.AMOUNT_EMPTY));
    }

    if (account.getClientDocument() == null) {
      return ResponseEntity.badRequest().body(ErrorC.getInstance(Constants.CLIENT_EMPTY));
    }

    ClientDto customer = accountsService.getClient(account.getClientDocument());

    if (! accountsService.listTitularIsCorrect(account.getTitulars())) {
      return ResponseEntity.badRequest().body(ErrorC.getInstance(Constants.TITULARS_EMPTY));
    }

    if ((customer.getClientType().equalsIgnoreCase(Constants.PERSONAL)
            || customer.getClientType().equalsIgnoreCase(Constants.PERSONAL_VIP))
            && (!accountsService.justOneTitularPersonal(account.getTitulars()))) {

      return ResponseEntity.badRequest()
              .body(ErrorC.getInstance(Constants.JUST_A_TITULAR));
    }

    List<AccountsDocuments> accounts = accountsService.getAccounts(customer.getDocument());

    if (customer.getClientType().equalsIgnoreCase(Constants.PERSONAL)
            && account.getAccountType().getValue().equalsIgnoreCase(Constants.SAVING_ACCOUNT)
            && accountsService.existAccountAhorro(accounts)) {

      return ResponseEntity.badRequest()
              .body(ErrorC.getInstance(Constants.EXIST_SAVING_ACCOUNT));
    }

    if (customer.getClientType().equalsIgnoreCase(Constants.PERSONAL)
            && account.getAccountType().getValue().equalsIgnoreCase(Constants.ORDINARY_ACCOUNT)
            && accountsService.existAccountCorriente(accounts)) {

      return ResponseEntity.badRequest()
              .body(ErrorC.getInstance(Constants.HAS_ORDINARY_ACCOUNT));
    }

    if ((customer.getClientType().equalsIgnoreCase(Constants.PERSONAL)
            || customer.getClientType().equalsIgnoreCase(Constants.PERSONAL_VIP))
            && account.getSigners() != null) {

      return ResponseEntity.badRequest()
              .body(ErrorC.getInstance(Constants.CANT_HAVE_SIGNERS));
    }

    if ((customer.getClientType().equalsIgnoreCase(Constants.COMPANY)
            || customer.getClientType().equalsIgnoreCase(Constants.COMPANY_PYME))
            && (account.getAccountType().getValue().equalsIgnoreCase(Constants.SAVING_ACCOUNT))) {

      return ResponseEntity.badRequest()
              .body(ErrorC.getInstance(Constants.CANT_HAVE_SAVING_ACCOUNT));
    }

    if ((customer.getClientType().equalsIgnoreCase(Constants.COMPANY)
            || customer.getClientType().equalsIgnoreCase(Constants.COMPANY_PYME))
            && account.getAccountType().getValue().equalsIgnoreCase(Constants.FIXED_TERM_ACCOUNT)) {

      return ResponseEntity.badRequest()
              .body(ErrorC.getInstance(Constants.CANT_HAVE_FIXED_TERM_ACCOUNT));
    }

    if ((customer.getClientType().equalsIgnoreCase(Constants.COMPANY)
            || customer.getClientType().equalsIgnoreCase(Constants.COMPANY_PYME))
            &&  account.getSigners() != null
            && !accountsService.listSignersIsCorrect(account.getSigners())) {

      return ResponseEntity.badRequest().body(ErrorC.getInstance(Constants.SIGNERS_INCORRECT));
    }

    if ((customer.getClientType().equalsIgnoreCase(Constants.COMPANY)
            || customer.getClientType().equalsIgnoreCase(Constants.COMPANY_PYME))
            && account.getSigners() != null
            && accountsService.validateQuantitySignersCreationAccount(account.getSigners())) {

      return ResponseEntity.badRequest().body(ErrorC.getInstance(Constants.SIGNERS_MAXIMUM));
    }

    if (customer.getClientType().equalsIgnoreCase(Constants.PERSONAL_VIP)
            && account.getAccountAmount() < 500) {

      return ResponseEntity
              .badRequest()
              .body(ErrorC.getInstance(Constants.VIP_AMOUNT_MINOR));
    }

    if ((customer.getClientType().equalsIgnoreCase(Constants.PERSONAL_VIP)
            || customer.getClientType().equalsIgnoreCase(Constants.COMPANY_PYME))
            && accountsService.getCreditCards(customer.getDocument()).isEmpty()) {

      return ResponseEntity.badRequest().body(ErrorC.getInstance(Constants.NOT_CREDIT_CARD));
    }


    return ResponseEntity.status(HttpStatus.OK).body(accountsService.createAccount(account));
  }

  @Override
  public ResponseEntity<AccountRetireDeposit> retireAccount(String accountNumber, Double amount) {

    if (accountNumber == null) {
      return ResponseEntity.badRequest().body(ErrorRetire.getInstance(Constants.ACCOUNT_EMPTY));
    }

    if (amount == null) {
      return ResponseEntity.badRequest()
              .body(ErrorRetire.getInstance(Constants.AMOUNT_RETIRE_EMPTY));
    }

    AccountsDocuments accountsDocuments = accountsService.getAccount(accountNumber);

    if (accountsDocuments.getAccountNumber() == null) {
      return ResponseEntity.badRequest().body(ErrorRetire.getInstance(Constants.ACCOUNT_NOT_EXIST));
    }

    if (!accountsService.validateIfYouCanRetire(accountsDocuments, amount)) {

      return ResponseEntity.badRequest().body(ErrorRetire.getInstance(Constants.NOT_MONEY));
    }

    /*if ((accountsDocuments.getAccountType().equalsIgnoreCase(Constants.SAVING_ACCOUNT)
            || accountsDocuments.getAccountType().equalsIgnoreCase(Constants.FIXED_TERM_ACCOUNT))
            && !accountsService.validateIfYouCanRetire(accountsDocuments, amount)) {

      return ResponseEntity.badRequest().body(ErrorRetire.getInstance(Constants.NOT_MONEY));
    }*/


    return ResponseEntity
            .status(HttpStatus.OK)
            .body(accountsService.retireAccount(accountsDocuments, amount));
  }

  @Override
  public ResponseEntity<AccountRetireDeposit> depositAccount(String accountNumber, Double amount) {

    if (accountNumber == null) {
      return ResponseEntity
              .badRequest()
              .body(ErrorRetire.getInstance(Constants.ACCOUNT_EMPTY));
    }

    if (amount == null) {
      return ResponseEntity
              .badRequest()
              .body(ErrorRetire.getInstance(Constants.AMOUNT_DEPOSIT_EMPTY));
    }

    AccountsDocuments accountsDocuments = accountsService.getAccount(accountNumber);

    if (accountsDocuments.getAccountNumber() == null) {
      return ResponseEntity
              .badRequest()
              .body(ErrorRetire.getInstance(Constants.ACCOUNT_NOT_EXIST));
    }

    if (accountsDocuments.getFreeMovements() == 0
            && !accountsService.validateIfYouCanDeposit(5.00, amount)) {
      return ResponseEntity
              .badRequest()
              .body(ErrorRetire.getInstance(Constants.DEPOSIT_INCORRECT));
    }

    return ResponseEntity
            .status(HttpStatus.OK)
            .body(accountsService.depositAccount(accountsDocuments, amount));
  }

  @Override
  public ResponseEntity<Account> addSigners(String accountNumber, List<SignersRequired> signers) {

    if (accountNumber == null) {
      return ResponseEntity
              .badRequest()
              .body(ErrorC.getInstance(Constants.ACCOUNT_EMPTY));
    }

    AccountsDocuments accountsDocuments = accountsService.getAccount(accountNumber);

    if (accountsDocuments.getAccountNumber() == null) {
      return ResponseEntity
              .badRequest()
              .body(ErrorC.getInstance(Constants.ACCOUNT_NOT_EXIST));
    }

    ClientDto customer = accountsService.getClient(accountsDocuments.getClientDocument());

    if (customer.getClientType().equalsIgnoreCase(Constants.PERSONAL)
            || customer.getClientType().equalsIgnoreCase(Constants.PERSONAL_VIP)) {

      return ResponseEntity
              .badRequest()
              .body(ErrorC.getInstance(Constants.CANT_HAVE_SIGNERS));
    }

    if (accountsDocuments.getSigners() != null && accountsDocuments.getSigners().size() == 4) {
      return ResponseEntity
              .badRequest()
              .body(ErrorC.getInstance(Constants.MAX_SIGNERS));
    }

    if (accountsDocuments.getSigners() != null
            && !accountsService.listSignersRequiredIsCorrect(signers)) {

      return ResponseEntity.badRequest().body(ErrorC.getInstance(Constants.SIGNERS_INCORRECT));
    }

    if (accountsDocuments.getSigners() != null
            && !accountsService.canAddSigners(accountsDocuments, signers)) {
      return ResponseEntity
              .badRequest()
              .body(ErrorC.getInstance(Constants.MAX_SIGNERS));
    }

    if (accountsDocuments.getSigners() == null && signers.isEmpty()) {
      return ResponseEntity
              .badRequest()
              .body(ErrorC.getInstance(Constants.SIGNERS_NOT_EMPTY));
    }

    if (accountsDocuments.getSigners() == null
            && !accountsService.listSignersRequiredIsCorrect(signers)) {
      return ResponseEntity
              .badRequest()
              .body(ErrorC.getInstance(Constants.SIGNERS_INFORMATION_INVALID));
    }

    if (accountsDocuments.getSigners() == null && signers.size() > 4) {
      return ResponseEntity
              .badRequest()
              .body(ErrorC.getInstance(Constants.MAX_SIGNERS));
    }

    return ResponseEntity
            .status(HttpStatus.OK)
            .body(accountsService.addSigner(accountsDocuments, signers));
  }

  @Override
  public ResponseEntity<Account> consultAccount(String accountNumber) {

    AccountsDocuments accountsDocuments = accountsService.getAccount(accountNumber);

    if (accountsDocuments.getAccountNumber() == null) {
      return ResponseEntity.badRequest().body(ErrorC.getInstance(Constants.ACCOUNT_NOT_EXIST));
    }

    return ResponseEntity
            .status(HttpStatus.OK)
            .body(AccountMapper
                    .mapAccountDocToAccountDto(accountsService.getAccount(accountNumber)));
  }

  @Override
  public ResponseEntity<Account> transferAccount(TransferRequest transferRequest) {

    if (transferRequest.getAccountOrigin() == null) {
      return ResponseEntity.badRequest().body(ErrorC.getInstance(Constants.ACCOUNT_ORIGIN_EMPTY));
    }

    if (transferRequest.getAccountDestination() == null) {
      return ResponseEntity.badRequest().body(ErrorC.getInstance(Constants.ACCOUNT_DEST_EMPTY));
    }

    if (transferRequest.getAmount() == null || transferRequest.getAmount() == 0) {
      return ResponseEntity.badRequest().body(ErrorC.getInstance(Constants.AMOUNT_TRANS_EMPTY));
    }

    AccountsDocuments accountOri = accountsService.getAccount(transferRequest.getAccountOrigin());

    AccountsDocuments accountDest = accountsService
            .getAccount(transferRequest.getAccountDestination());

    if (accountOri.getAccountNumber() == null) {
      return ResponseEntity.badRequest().body(ErrorC.getInstance(Constants.ACCOUNT_ORI_NOT_EXIST));
    }

    if (accountDest.getAccountNumber() == null) {
      return ResponseEntity.badRequest().body(ErrorC.getInstance(Constants.ACCOUNT_DEST_NOT_EXIST));
    }

    if (!accountsService.validateIfYouCanRetire(accountOri, transferRequest.getAmount())) {
      return ResponseEntity.badRequest().body(ErrorC.getInstance(Constants.NOT_MONEY));
    }

    return ResponseEntity
            .status(HttpStatus.OK)
            .body(accountsService.transfer(accountOri, accountDest, transferRequest.getAmount()));
  }

  @Override
  public ResponseEntity<List<Account>> getAccountsByClient(String document) {
    return ResponseEntity.status(HttpStatus.OK).body(accountsService.getAccountsByClient(document));
  }
}
