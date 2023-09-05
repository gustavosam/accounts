package com.microservice.accounts.service;

import com.microservice.accounts.documents.AccountsDocuments;
import com.microservice.accounts.feignclient.CreditCardFeignClient;
import com.microservice.accounts.feignclient.CustomerFeignClient;
import com.microservice.accounts.feignclient.MovementFeignClient;
import com.microservice.accounts.model.Account;
import com.microservice.accounts.model.AccountRequest;
import com.microservice.accounts.model.Signers;
import com.microservice.accounts.model.SignersRequired;
import com.microservice.accounts.model.TitularsIn;
import com.microservice.accounts.repository.AccountRepository;
import com.microservice.accounts.service.mapper.AccountMapper;
import com.microservice.accounts.service.mapper.MapperMovement;
import com.microservice.accounts.util.AccountDto;
import com.microservice.accounts.util.AccountRetireDepositDto;
import com.microservice.accounts.util.CardDto;
import com.microservice.accounts.util.ClientDto;
import com.microservice.accounts.util.Constants;
import com.microservice.accounts.util.complementary.SignersComplementary;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

/**
 * Esta clase contiene la lógica de negocio para las cuentas.
 * */

@Service
public class AccountsServiceImpl implements AccountsService {

  @Autowired
  private AccountRepository accountRepository;

  @Autowired
  private CustomerFeignClient customerFeignClient;

  @Autowired
  private MovementFeignClient movementFeignClient;

  @Autowired
  private CreditCardFeignClient creditCardFeignClient;

  @Override
  public Account createAccount(AccountRequest accountRequest) {

    AccountsDocuments account = AccountMapper.mapAccountRequestToAccountsDocuments(accountRequest);
    account.setAccountCreationDate(LocalDate.now());
    account.setFreeMovements(4);

    AccountDto accountNew = AccountMapper
            .mapAccountDocToAccountDto(accountRepository.save(account));

    movementFeignClient.saveMovement(MapperMovement.setValues(
            accountNew.getAccountAmount(), accountNew.getClientDocument(),
            accountNew.getAccountNumber(), Constants.ACCOUNT_CREATED, 0.0
    ));

    return accountNew;
  }

  @Override
  public ClientDto getClient(String clientDocument) {

    return customerFeignClient.getClient(clientDocument);
  }

  @Override
  public List<AccountsDocuments> getAccounts(String customerDocument) {
    return accountRepository.findByClientDocument(customerDocument);
  }

  @Override
  public Boolean existAccountAhorro(List<AccountsDocuments> accountsDocuments) {

    return accountsDocuments.stream()
            .anyMatch(account -> account.getAccountType().equalsIgnoreCase("AHORRO"));
  }

  @Override
  public Boolean existAccountCorriente(List<AccountsDocuments> accountsDocuments) {
    return accountsDocuments.stream()
            .anyMatch(account -> account.getAccountType().equalsIgnoreCase("CORRIENTE"));
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
  public AccountsDocuments getAccount(String accountNumber) {
    return accountRepository.findById(accountNumber).orElse(new AccountsDocuments());
  }

  @Override
  public AccountRetireDepositDto retireAccount(AccountsDocuments account, Double amountToRetire) {
    double comission = 0.0;

    if (account.getFreeMovements() == 0) {
      comission = 5.0;
    }

    if (account.getFreeMovements() != 0) {
      account.setFreeMovements(account.getFreeMovements() - 1);
    }


    account.setAccountAmount(account.getAccountAmount() - amountToRetire  - comission);

    AccountRetireDepositDto accountRetired = AccountMapper
            .mapAccountDocToAccountRetDep(accountRepository.save(account));

    movementFeignClient.saveMovement(MapperMovement.setValues(
        amountToRetire, account.getClientDocument(),
        account.getAccountNumber(), Constants.ACCOUNT_RETIRE, comission
    ));

    return accountRetired;
  }

  @Override
  public Boolean validateIfYouCanDeposit(Double commission, Double amountDeposit) {

    return amountDeposit > commission;
  }

  @Override
  public AccountRetireDepositDto depositAccount(AccountsDocuments account, Double amountToDeposit) {

    double comission = 0.0;

    if (account.getFreeMovements() == 0) {
      comission = 5.0;
    }

    if (account.getFreeMovements() != 0) {
      account.setFreeMovements(account.getFreeMovements() - 1);
    }

    account.setAccountAmount(account.getAccountAmount() + amountToDeposit - comission);

    AccountRetireDepositDto accountDeposit = AccountMapper
            .mapAccountDocToAccountRetDep(accountRepository.save(account));

    movementFeignClient.saveMovement(MapperMovement.setValues(
        amountToDeposit, account.getClientDocument(),
        account.getAccountNumber(), Constants.ACCOUNT_DEPOSIT, comission
    ));

    return accountDeposit;
  }

  @Override
  public Boolean canAddSigners(AccountsDocuments accounts, List<SignersRequired> signers) {

    return accounts.getSigners().size() + signers.size() <= 4;
  }

  @Override
  public Account addSigner(AccountsDocuments account, List<SignersRequired> signersRequired) {

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

    return AccountMapper.mapAccountDocToAccountDto(accountRepository.save(account));
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
  public Boolean listSignersRequiredIsCorrect(List<SignersRequired> signers) {
    if (signers.isEmpty()) {
      return false;
    }

    return signers.stream()
            .allMatch(signer -> signer.getFullName() != null && signer.getDocument() != null);
  }

  @Override
  public List<CardDto> getCreditCards(String clientDocument) {
    return creditCardFeignClient.getCreditCards(clientDocument);
  }
}
