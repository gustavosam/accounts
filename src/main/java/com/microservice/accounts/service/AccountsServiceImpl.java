package com.microservice.accounts.service;

import com.microservice.accounts.documents.AccountsDocuments;
import com.microservice.accounts.model.*;
import com.microservice.accounts.repository.AccountRepository;
import com.microservice.accounts.service.mapper.AccountMapper;
import com.microservice.accounts.service.mapper.MapperMovement;
import com.microservice.accounts.util.AccountDto;
import com.microservice.accounts.util.AccountRetireDepositDto;
import com.microservice.accounts.util.CardDto;
import com.microservice.accounts.util.ClientDto;
import com.microservice.accounts.util.Constants;
import com.microservice.accounts.util.TransferDto;
import com.microservice.accounts.util.complementary.SignersComplementary;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.concurrent.atomic.AtomicReference;
import java.util.stream.Collectors;
import com.microservice.accounts.webclient.ClientWebClient;
import com.microservice.accounts.webclient.CreditCardWebClient;
import com.microservice.accounts.webclient.MovementWebClient;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

/**
 * Esta clase contiene la lógica de negocio para las cuentas.
 * */

@Service
public class AccountsServiceImpl implements AccountsService {

  @Autowired
  private AccountRepository accountRepository;

  @Autowired
  private ClientWebClient clientWebClient;

  @Autowired
  private MovementWebClient movementWebClient;

  @Autowired
  private CreditCardWebClient creditCardWebClient;

  @Override
  public Mono<AccountDto> createAccount(AccountRequest accountRequest) {

    AccountsDocuments account = AccountMapper.mapAccountRequestToAccountsDocuments(accountRequest);
    account.setAccountCreationDate(LocalDate.now());
    account.setFreeMovements(20);

    Mono<AccountsDocuments> accountMono = accountRepository.save(account);

    return accountMono.map(accountsDocuments -> {
      AccountDto accountDto = AccountMapper.mapAccountDocToAccountDto(accountsDocuments);

      movementWebClient.saveMovement(MapperMovement.setValues(
              accountDto.getAccountAmount(), accountDto.getClientDocument(),
              accountDto.getAccountNumber(), accountDto.getAccountType(), Constants.ACCOUNT_CREATED, 0.0
      )).subscribe();

      return accountDto;

    });
  }

  @Override
  public Mono<ClientDto> getClient(String clientDocument) {

    return clientWebClient.getClient(clientDocument);
  }

  @Override
  public Flux<AccountsDocuments> getAccounts(String customerDocument) {
    return accountRepository.findByClientDocument(customerDocument);
  }

  @Override
  public Mono<Boolean> existAccountAhorro(Flux<AccountsDocuments> accountsDocuments) {

    return accountsDocuments
            .filter(account -> account.getAccountType().equalsIgnoreCase("AHORRO"))
            .hasElements()
            .defaultIfEmpty(false);
  }

  @Override
  public Mono<Boolean> existAccountCorriente(Flux<AccountsDocuments> accountsDocuments) {
    return accountsDocuments
            .filter(account -> account.getAccountType().equalsIgnoreCase("CORRIENTE"))
            .hasElements()
            .defaultIfEmpty(false);
  }

  @Override
  public Boolean listTitularIsCorrect(List<TitularsIn> titulars) {

    if (titulars.isEmpty()) {
      return false;
    }

    return titulars.stream()
            .allMatch(titular -> titular.getName() != null && titular.getDocument() != null);
  }

  @Override
  public Boolean justOneTitularPersonal(List<TitularsIn> titulars) {
    return titulars.size() == 1;
  }

  @Override
  public Boolean validateIfYouCanRetire(AccountsDocuments accountsDocuments, Double amount) {

    double commission = 0;

    if (accountsDocuments.getFreeMovements() == 0) {
      commission = 5.0;
    }

    return accountsDocuments.getAccountAmount() > 0
            && accountsDocuments.getAccountAmount() >= amount + commission;
  }

  @Override
  public Mono<AccountsDocuments> getAccount(String accountNumber) {
    return accountRepository.findById(accountNumber)
            .defaultIfEmpty(new AccountsDocuments());
  }

  @Override
  public Mono<AccountRetireDepositDto> retireAccount(AccountsDocuments account, Double amountToRetire) {
    AtomicReference<Double> comission = new AtomicReference<>(0.0);

    if (account.getFreeMovements() == 0) {
      comission.set(5.0);
    }

    if (account.getFreeMovements() != 0) {
      account.setFreeMovements(account.getFreeMovements() - 1);
    }

    account.setAccountAmount(account.getAccountAmount() - amountToRetire  - comission.get());

    Mono<AccountsDocuments> accountsDocumentsMono = accountRepository.save(account);

    return accountsDocumentsMono.map(accountDoc -> {

      AccountRetireDepositDto accountRetired = AccountMapper.mapAccountDocToAccountRetDep(accountDoc);

      movementWebClient.saveMovement(MapperMovement.setValues(
              amountToRetire, accountDoc.getClientDocument(),
              accountDoc.getAccountNumber(), accountDoc.getAccountType(), Constants.ACCOUNT_RETIRE, comission.get()
      )).subscribe();
      return accountRetired;
    });

  }

  @Override
  public Mono<TransferDto> transfer(AccountsDocuments accountOri, AccountsDocuments accountDest,
                                    Double amountTransfer) {
    AtomicReference<Double> comission = new AtomicReference<>(0.0);

    if (accountOri.getFreeMovements() == 0) {
      comission.set(5.0);
    }

    if (accountOri.getFreeMovements() != 0) {
      accountOri.setFreeMovements(accountOri.getFreeMovements() - 1);
    }

    accountOri.setAccountAmount(accountOri.getAccountAmount() - amountTransfer  - comission.get());
    accountDest.setAccountAmount(accountDest.getAccountAmount() + amountTransfer);

    Mono<AccountsDocuments> accountOriMono = accountRepository.save(accountOri);
    Mono<AccountsDocuments> accountDesMono = accountRepository.save(accountDest);

    return accountOriMono.zipWith(accountDesMono).map(tuple -> {

      AccountsDocuments ori = tuple.getT1();
      AccountsDocuments des = tuple.getT2();

      movementWebClient.saveMovement(MapperMovement.setValues(
              amountTransfer, ori.getClientDocument(),
              ori.getAccountNumber(), ori.getAccountType(),
              Constants.TRANSFER_RET, comission.get()
      )).subscribe();

      movementWebClient.saveMovement(MapperMovement.setValues(
              amountTransfer, des.getClientDocument(),
              des.getAccountNumber(), des.getAccountType(),
              Constants.TRANSFER_DEP, 0.0
      )).subscribe();

      TransferDto transferDto = new TransferDto();
      transferDto.setAmount(amountTransfer);
      transferDto.setAccountOrigin(accountOri.getAccountNumber());
      transferDto.setAccountDestination(accountDest.getAccountNumber());

      return transferDto;

    });

  }

  @Override
  public Boolean validateIfYouCanDeposit(Double commission, Double amountDeposit) {

    return amountDeposit > commission;
  }

  @Override
  public Mono<AccountRetireDepositDto> depositAccount(AccountsDocuments account, Double amountToDeposit) {

    AtomicReference<Double> comission = new AtomicReference<>(0.0);

    if (account.getFreeMovements() == 0) {
      comission.set(5.0);
    }

    if (account.getFreeMovements() != 0) {
      account.setFreeMovements(account.getFreeMovements() - 1);
    }

    account.setAccountAmount(account.getAccountAmount() + amountToDeposit - comission.get());

    Mono<AccountsDocuments> accountsDocumentsMono = accountRepository.save(account);

    return accountsDocumentsMono.map(accountDoc -> {

      AccountRetireDepositDto accountDeposit = AccountMapper.mapAccountDocToAccountRetDep(accountDoc);

      movementWebClient.saveMovement(MapperMovement.setValues(
              amountToDeposit, accountDoc.getClientDocument(),
              accountDoc.getAccountNumber(), accountDoc.getAccountType(), Constants.ACCOUNT_DEPOSIT, comission.get()
      )).subscribe();

      return accountDeposit;

    });

  }

  @Override
  public Boolean canAddSigners(AccountsDocuments accounts, List<SignerList> signers) {

    return accounts.getSigners().size() + signers.size() <= 4;
  }

  @Override
  public Mono<Account> addSigner(AccountsDocuments account, List<SignerList> signersRequired) {

    List<SignersComplementary> newSigners = signersRequired.stream()
            .filter(Objects::nonNull)
            .map(AccountMapper::mapSignerReToSignerComplementary)
            .collect(Collectors.toList());

    List<SignersComplementary> signers = account.getSigners();

    if (signers == null) {
      signers = new ArrayList<>();
    }

    List<SignersComplementary> unionSigners = new ArrayList<>();
    unionSigners.addAll(signers);
    unionSigners.addAll(newSigners);

    account.setSigners(unionSigners);

    Mono<AccountsDocuments> accountsDocumentsMono = accountRepository.save(account);

    return accountsDocumentsMono.map(AccountMapper::mapAccountDocToAccountDto);
  }

  @Override
  public Boolean validateQuantitySignersCreationAccount(List<Signers> signersRequired) {
    return signersRequired.size() > 4;
  }

  @Override
  public Boolean listSignersIsCorrect(List<Signers> signersRequired) {
    if (signersRequired.isEmpty()) {
      return false;
    }

    return signersRequired.stream()
            .allMatch(signer -> signer.getFullName() != null && signer.getDocument() != null);
  }

  @Override
  public Boolean listSignersRequiredIsCorrect(List<SignerList> signers) {
    if (signers.isEmpty()) {
      return false;
    }

    return signers.stream()
            .allMatch(signer -> signer.getFullName() != null && signer.getDocument() != null);
  }

  @Override
  public Flux<CardDto> getCreditCards(String clientDocument) {
    return creditCardWebClient.getCreditCards(clientDocument);
  }

  @Override
  public Flux<Account> getAccountsByClient(String document) {

    Flux<AccountsDocuments> accounts = accountRepository.findByClientDocument(document);

    return accounts
            .filter(Objects::nonNull)
            .map(AccountMapper::mapAccountDocToAccountDto);
  }
}
