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
import com.microservice.accounts.util.*;

import java.util.List;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;


/**
 * Esta clase implementa los métodos generados por open api.
 * */
@Service
public class AccountDelegateImpl implements AccountApiDelegate {

  @Autowired
  private AccountsService accountsService;

  @Override
  public Mono<ResponseEntity<Account>> createAccount(Mono<AccountRequest> accountRequest,
                                                     ServerWebExchange exchange) {

    return accountRequest.flatMap(account -> {

      if(account.getAccountType() == null){
        return Mono.just(ResponseEntity.badRequest().body(ErrorC.getInstance(Constants.ACCOUNT_TYPE_EMPTY)));
      }

      if(account.getAccountAmount() == null){
        return Mono.just(ResponseEntity.badRequest().body(ErrorC.getInstance(Constants.AMOUNT_EMPTY)));
      }

      if(account.getClientDocument() == null){
        return Mono.just(ResponseEntity.badRequest().body(ErrorC.getInstance(Constants.CLIENT_EMPTY)));
      }

      Mono<ClientDto> customer = accountsService.getClient(account.getClientDocument());

      return customer.flatMap(clientDto -> {

        if(clientDto.getDocument() == null){
          return Mono.just(ResponseEntity.status(HttpStatus.BAD_REQUEST).body(ErrorC.getInstance("ERROR WITH SERVICE CLIENT")));
        }

        if(clientDto.getDocument().equalsIgnoreCase("NOT_EXIST")){
          return Mono.just(ResponseEntity.status(HttpStatus.NOT_FOUND).body(ErrorC.getInstance(Constants.CLIENT_NOT_EXIST)));
        }

        if (!accountsService.listTitularIsCorrect(account.getTitulars())) {
          return Mono.just(ResponseEntity.badRequest().body(ErrorC.getInstance(Constants.TITULARS_EMPTY)));
        }

        if ((clientDto.getClientType().equalsIgnoreCase(Constants.PERSONAL)
                || clientDto.getClientType().equalsIgnoreCase(Constants.PERSONAL_VIP))
                && (!accountsService.justOneTitularPersonal(account.getTitulars()))) {

          return Mono.just(ResponseEntity.badRequest()
                  .body(ErrorC.getInstance(Constants.JUST_A_TITULAR)));
        }

        Flux<AccountsDocuments> accounts = accountsService.getAccounts(clientDto.getDocument());

        Mono<Boolean> existSavingAccount = accountsService.existAccountAhorro(accounts);
        Mono<Boolean> existOrdinaryAccount = accountsService.existAccountCorriente(accounts);

        return existSavingAccount.zipWith(existOrdinaryAccount).flatMap(tuple -> {

          Boolean savingAccountExist = tuple.getT1();
          Boolean ordinaryAccountExist = tuple.getT2();

          if(savingAccountExist && clientDto.getClientType().equalsIgnoreCase(Constants.PERSONAL) && account.getAccountType().getValue().equalsIgnoreCase(Constants.SAVING_ACCOUNT)){
            return Mono.just(ResponseEntity.badRequest()
                    .body(ErrorC.getInstance(Constants.EXIST_SAVING_ACCOUNT)));
          }

          if(ordinaryAccountExist && clientDto.getClientType().equalsIgnoreCase(Constants.PERSONAL) && account.getAccountType().getValue().equalsIgnoreCase(Constants.ORDINARY_ACCOUNT)){
            return Mono.just(ResponseEntity.badRequest()
                    .body(ErrorC.getInstance(Constants.HAS_ORDINARY_ACCOUNT)));
          }

          if ((clientDto.getClientType().equalsIgnoreCase(Constants.PERSONAL)
                  || clientDto.getClientType().equalsIgnoreCase(Constants.PERSONAL_VIP))
                  && account.getSigners() != null) {

            return Mono.just(ResponseEntity.badRequest()
                    .body(ErrorC.getInstance(Constants.CANT_HAVE_SIGNERS)));
          }

          if ((clientDto.getClientType().equalsIgnoreCase(Constants.COMPANY)
                  || clientDto.getClientType().equalsIgnoreCase(Constants.COMPANY_PYME))
                  && (account.getAccountType().getValue().equalsIgnoreCase(Constants.SAVING_ACCOUNT))) {

            return Mono.just(ResponseEntity.badRequest()
                    .body(ErrorC.getInstance(Constants.CANT_HAVE_SAVING_ACCOUNT)));
          }

          if ((clientDto.getClientType().equalsIgnoreCase(Constants.COMPANY)
                  || clientDto.getClientType().equalsIgnoreCase(Constants.COMPANY_PYME))
                  && account.getAccountType().getValue().equalsIgnoreCase(Constants.FIXED_TERM_ACCOUNT)) {

            return Mono.just(ResponseEntity.badRequest()
                    .body(ErrorC.getInstance(Constants.CANT_HAVE_FIXED_TERM_ACCOUNT)));
          }

          if ((clientDto.getClientType().equalsIgnoreCase(Constants.COMPANY)
                  || clientDto.getClientType().equalsIgnoreCase(Constants.COMPANY_PYME))
                  &&  account.getSigners() != null
                  && !accountsService.listSignersIsCorrect(account.getSigners())) {

            return Mono.just(ResponseEntity.badRequest().body(ErrorC.getInstance(Constants.SIGNERS_INCORRECT)));
          }

          if ((clientDto.getClientType().equalsIgnoreCase(Constants.COMPANY)
                  || clientDto.getClientType().equalsIgnoreCase(Constants.COMPANY_PYME))
                  && account.getSigners() != null
                  && accountsService.validateQuantitySignersCreationAccount(account.getSigners())) {

            return Mono.just(ResponseEntity.badRequest().body(ErrorC.getInstance(Constants.SIGNERS_MAXIMUM)));
          }

          if (clientDto.getClientType().equalsIgnoreCase(Constants.PERSONAL_VIP)
                  && account.getAccountAmount() < 500) {

            return Mono.just(ResponseEntity
                    .badRequest()
                    .body(ErrorC.getInstance(Constants.VIP_AMOUNT_MINOR)));
          }

          Flux<CardDto> cardDtoFlux = accountsService.getCreditCards(clientDto.getDocument());

          return cardDtoFlux.hasElements().flatMap(haItems -> {

            if(!haItems  && (clientDto.getClientType().equalsIgnoreCase(Constants.PERSONAL_VIP)
                    || clientDto.getClientType().equalsIgnoreCase(Constants.COMPANY_PYME))){

              return Mono.just(ResponseEntity.badRequest().body(ErrorC.getInstance(Constants.NOT_CREDIT_CARD)));
            }
            return accountsService.createAccount(account).map(ResponseEntity::ok);
          });
        });

      });

      });
    }


  @Override
  public Mono<ResponseEntity<AccountRetireDeposit>> retireAccount(String accountNumber,
                                                                  Double amount,
                                                                  ServerWebExchange exchange) {

    if (accountNumber == null) {
      return Mono.just(ResponseEntity.badRequest().body(ErrorRetire.getInstance(Constants.ACCOUNT_EMPTY)));
    }

    if (amount == null) {
      return Mono.just(ResponseEntity.badRequest()
              .body(ErrorRetire.getInstance(Constants.AMOUNT_RETIRE_EMPTY)));
    }

    Mono<AccountsDocuments> accountsDocuments = accountsService.getAccount(accountNumber);

    return accountsDocuments.flatMap(account -> {

      if(account.getAccountNumber() == null){
        return Mono.just(ResponseEntity.badRequest().body(ErrorRetire.getInstance(Constants.ACCOUNT_NOT_EXIST)));
      }

      Boolean canRetire = accountsService.validateIfYouCanRetire(account, amount);

      if(!canRetire){
        return Mono.just(ResponseEntity.badRequest().body(ErrorRetire.getInstance(Constants.NOT_MONEY)));
      }

      return accountsService.retireAccount(account, amount).map(ResponseEntity::ok);

    });

  }

  @Override
  public Mono<ResponseEntity<AccountRetireDeposit>> depositAccount(String accountNumber,
                                                                   Double amount,
                                                                   ServerWebExchange exchange) {

    if (accountNumber == null) {
      return Mono.just(ResponseEntity
              .badRequest()
              .body(ErrorRetire.getInstance(Constants.ACCOUNT_EMPTY)));
    }

    if (amount == null) {
      return Mono.just(ResponseEntity
              .badRequest()
              .body(ErrorRetire.getInstance(Constants.AMOUNT_DEPOSIT_EMPTY)));
    }

    Mono<AccountsDocuments> accountsDocuments = accountsService.getAccount(accountNumber);

    return accountsDocuments.flatMap(account -> {

      if(account.getAccountNumber() == null){
        return Mono.just(ResponseEntity
                .badRequest()
                .body(ErrorRetire.getInstance(Constants.ACCOUNT_NOT_EXIST)));
      }

      if (account.getFreeMovements() == 0
              && !accountsService.validateIfYouCanDeposit(5.00, amount)) {

        return Mono.just(ResponseEntity
                .badRequest()
                .body(ErrorRetire.getInstance(Constants.DEPOSIT_INCORRECT)));
      }

      return accountsService.depositAccount(account, amount).map(ResponseEntity::ok);

    });
  }

  @Override
  public Mono<ResponseEntity<Account>> addSigners(String accountNumber,
                                                  Mono<SignersRequired> signersRequired,
                                                  ServerWebExchange exchange) {

    if (accountNumber == null) {
      return Mono.just(ResponseEntity
              .badRequest()
              .body(ErrorC.getInstance(Constants.ACCOUNT_EMPTY)));
    }

    Mono<AccountsDocuments> accountsDocuments = accountsService.getAccount(accountNumber);

    return accountsDocuments.flatMap(account -> {

      if (account.getAccountNumber() == null) {

        return Mono.just(ResponseEntity
                .badRequest()
                .body(ErrorC.getInstance(Constants.ACCOUNT_NOT_EXIST)));
      }

      Mono<ClientDto> customer = accountsService.getClient(account.getClientDocument());

      return customer.flatMap(clientDto -> {

        if(clientDto.getDocument() == null){
          return Mono.just(ResponseEntity.status(HttpStatus.BAD_REQUEST).body(ErrorC.getInstance("ERROR WITH SERVICE CLIENT")));
        }

        if(clientDto.getDocument().equalsIgnoreCase("NOT_EXIST")){
          return Mono.just(ResponseEntity.status(HttpStatus.NOT_FOUND).body(ErrorC.getInstance(Constants.CLIENT_NOT_EXIST)));
        }

        if (clientDto.getClientType().equalsIgnoreCase(Constants.PERSONAL)
                || clientDto.getClientType().equalsIgnoreCase(Constants.PERSONAL_VIP)) {

          return Mono.just(ResponseEntity
                  .badRequest()
                  .body(ErrorC.getInstance(Constants.CANT_HAVE_SIGNERS)));
        }

        if (account.getSigners() != null && account.getSigners().size() == 4) {
          return Mono.just(ResponseEntity
                  .badRequest()
                  .body(ErrorC.getInstance(Constants.MAX_SIGNERS)));
        }

        return signersRequired.flatMap(signerObject -> {

          if (signerObject.getSigners() == null) {

            return Mono.just(ResponseEntity
                    .badRequest()
                    .body(ErrorC.getInstance(Constants.SIGNERS_NOT_EMPTY)));
          }

          if (!accountsService.listSignersRequiredIsCorrect(signerObject.getSigners())) {

            return Mono.just(ResponseEntity.badRequest().body(ErrorC.getInstance(Constants.SIGNERS_INCORRECT)));
          }

          if (account.getSigners() != null
                  && !accountsService.canAddSigners(account, signerObject.getSigners())) {

            return Mono.just(ResponseEntity
                    .badRequest()
                    .body(ErrorC.getInstance(Constants.MAX_SIGNERS)));
          }

          if (account.getSigners() == null && signerObject.getSigners().size() > 4) {
            return Mono.just(ResponseEntity
                    .badRequest()
                    .body(ErrorC.getInstance(Constants.MAX_SIGNERS)));
          }

          return accountsService.addSigner(account, signerObject.getSigners()).map(ResponseEntity::ok);

        });
      });
    });
  }


  @Override
  public Mono<ResponseEntity<Account>> consultAccount(String accountNumber,
                                                      ServerWebExchange exchange) {

    Mono<AccountsDocuments> accountsDocuments = accountsService.getAccount(accountNumber);

    return accountsDocuments.flatMap(account -> {

      if(account.getAccountNumber() == null){
        return Mono.just(ResponseEntity.badRequest().body(ErrorC.getInstance(Constants.ACCOUNT_NOT_EXIST)));
      }

      return Mono.just(ResponseEntity.ok(AccountMapper.mapAccountDocToAccountDto(account)));

    });
  }

  @Override
  public Mono<ResponseEntity<Account>> transferAccount(Mono<TransferRequest> transferRequest,
                                                       ServerWebExchange exchange) {

    return transferRequest.flatMap(transfer -> {

      if (transfer.getAccountOrigin() == null) {
        return Mono.just(ResponseEntity.badRequest().body(ErrorC.getInstance(Constants.ACCOUNT_ORIGIN_EMPTY)));
      }

      if (transfer.getAccountDestination() == null) {
        return Mono.just(ResponseEntity.badRequest().body(ErrorC.getInstance(Constants.ACCOUNT_DEST_EMPTY)));
      }

      if (transfer.getAmount() == null || transfer.getAmount() == 0) {
        return Mono.just(ResponseEntity.badRequest().body(ErrorC.getInstance(Constants.AMOUNT_TRANS_EMPTY)));
      }

      Mono<AccountsDocuments> accountOri = accountsService.getAccount(transfer.getAccountOrigin());

      Mono<AccountsDocuments> accountDest = accountsService
              .getAccount(transfer.getAccountDestination());

      return accountOri.zipWith(accountDest).flatMap(tuple -> {

        AccountsDocuments accountO = tuple.getT1();
        AccountsDocuments accountD = tuple.getT2();

        if (accountO.getAccountNumber() == null) {
          return Mono.just(ResponseEntity.badRequest().body(ErrorC.getInstance(Constants.ACCOUNT_ORI_NOT_EXIST)));
        }

        if (accountD.getAccountNumber() == null) {
          return Mono.just(ResponseEntity.badRequest().body(ErrorC.getInstance(Constants.ACCOUNT_DEST_NOT_EXIST)));
        }

        if (!accountsService.validateIfYouCanRetire(accountO, transfer.getAmount())) {
          return Mono.just(ResponseEntity.badRequest().body(ErrorC.getInstance(Constants.NOT_MONEY)));
        }

        return accountsService.transfer(accountO, accountD, transfer.getAmount()).map(ResponseEntity::ok);
        //return Mono.just(ResponseEntity.ok(accountsService.transfer(accountO, accountD, transfer.getAmount())));
      });
    });
  }

  @Override
  public Mono<ResponseEntity<Flux<Account>>> getAccountsByClient(String document,
                                                                 ServerWebExchange exchange) {

    return Mono.just(ResponseEntity.status(HttpStatus.OK)
            .body(accountsService.getAccountsByClient(document)));
  }
}
