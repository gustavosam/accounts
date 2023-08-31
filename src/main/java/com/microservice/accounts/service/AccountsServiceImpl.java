package com.microservice.accounts.service;

import com.microservice.accounts.documents.AccountsDocuments;
import com.microservice.accounts.documents.MovementsDocuments;
import com.microservice.accounts.feignclient.CustomerFeignClient;
import com.microservice.accounts.model.*;
import com.microservice.accounts.repository.AccountRepository;
import com.microservice.accounts.repository.MovementsRepository;
import com.microservice.accounts.service.mapper.AccountsMapper;
import com.microservice.accounts.util.complementary.CustomersComplementary;
import com.microservice.accounts.util.complementary.SignersDocumentComplementary;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

@Service
public class AccountsServiceImpl implements AccountsService{

    @Autowired
    private AccountRepository accountRepository;

    @Autowired
    private CustomerFeignClient customerFeignClient;

    @Autowired
    private MovementsRepository movementsRepository;

    @Override
    public Account createAccount(AccountRequest accountRequest) {

        AccountsDocuments accountsDocuments = AccountsMapper.mapAccountRequestToAccountsDocuments(accountRequest);
        accountsDocuments.setAccountCreationDate(LocalDate.now());

        Account accountNew = AccountsMapper.mapAccountDocumentToAccount(  accountRepository.save(accountsDocuments) );

        //LA CREACIÓN DE UNA CUENTA GENERA UN MOVIMIENTO
        MovementsDocuments movementAccount = new MovementsDocuments();
        movementAccount.setMovementType("ALTA CUENTA");
        movementAccount.setCustomerDocument(accountRequest.getCustomerDocument());
        movementAccount.setAccountType(accountRequest.getAccountType().getValue());
        movementAccount.setAccountNumber(accountNew.getAccountNumber());
        movementAccount.setAmount(accountRequest.getAccountAmount());

        movementAccount.setMovementDate(LocalDate.now());

        movementsRepository.save(movementAccount);

        return accountNew;
    }

    @Override
    public CustomersComplementary getCustomer(String customerDocument) {

        CustomersComplementary customers = customerFeignClient.getCustomerById(customerDocument);

        return customers;
    }

    @Override
    public Boolean payCommission(String accountType) {

        switch (accountType){
            case "AHORRO":
            case "PLAZOFIJO":
                return false;

            case "CORRIENTE":
                return true;

            default:
                throw new IllegalArgumentException("NO RECONOCICO");
        }

    }

    @Override
    public Boolean ilimitMovements(String accountType) {
        switch (accountType){
            case "AHORRO":
            case "PLAZOFIJO":
                return false;

            case "CORRIENTE":
                return true;

            default:
                throw new IllegalArgumentException("NO RECONOCICO");
        }
    }

    @Override
    public List<AccountsDocuments> getAccountsByCustomer(String customerDocument) {
        return accountRepository.findByCustomerDocument(customerDocument);
    }

    @Override
    public Boolean existAccountAhorro(List<AccountsDocuments> accountsDocuments) {

        return accountsDocuments.stream().anyMatch(account -> account.getAccountType().equalsIgnoreCase("AHORRO"));
    }

    @Override
    public Boolean existAccountCorriente(List<AccountsDocuments> accountsDocuments) {
        return accountsDocuments.stream().anyMatch(account -> account.getAccountType().equalsIgnoreCase("CORRIENTE"));
    }

    @Override
    public Boolean titularsEmpty(List<TitularsIn> titulars) {

        return titulars.isEmpty();
    }

    @Override
    public Boolean justATitularPersonal(List<TitularsIn> titulars) {
        return titulars.size() == 1;
    }

    @Override
    public Boolean validateIfYouCanRetireCorriente(AccountsDocuments accountsDocuments, Double amount) {

        return accountsDocuments.getAccountAmount() > 0 && accountsDocuments.getAccountAmount() >= amount;
    }

    @Override
    public Boolean validateIfYouCanRetire(AccountsDocuments accountsDocuments, Double amount) {
        return accountsDocuments.getAccountAmount() > 0 && accountsDocuments.getQuantityMovements() > 0 && accountsDocuments.getAccountAmount() >=amount;
    }

    @Override
    public AccountsDocuments getAccount(String accountNumber) {
        return accountRepository.findById(accountNumber).orElse(new AccountsDocuments());
    }

    @Override
    public AccountRetireDeposit retireAccount(AccountsDocuments accountsDocuments, Double amountToRetire) {

        if(accountsDocuments.getAccountType().equalsIgnoreCase("AHORRO") || accountsDocuments.getAccountType().equalsIgnoreCase("PLAZOFIJO")){
            accountsDocuments.setQuantityMovements(accountsDocuments.getQuantityMovements() -1);
        }

        accountsDocuments.setAccountAmount(accountsDocuments.getAccountAmount() - amountToRetire );

        AccountRetireDeposit accountRetired = AccountsMapper.mapAccountDocmentToAccountRetireDeposit( accountRepository.save(accountsDocuments) );

        //EL RETIRO DE DINERO DE UNA CUENTA GENERA UN MOVIMIENTO
        MovementsDocuments movementAccount = new MovementsDocuments();
        movementAccount.setMovementType("RETIRO CUENTA");
        movementAccount.setCustomerDocument(accountsDocuments.getCustomerDocument());
        movementAccount.setAccountType(accountsDocuments.getAccountType());
        movementAccount.setAccountNumber(accountsDocuments.getAccountNumber());
        movementAccount.setAmount(amountToRetire);
        movementAccount.setMovementDate(LocalDate.now());

        movementsRepository.save(movementAccount);

        return accountRetired;

    }

    @Override
    public Boolean validateIfYouCanDeposit(AccountsDocuments accountsDocuments) {

        return accountsDocuments.getQuantityMovements()>0;
    }

    @Override
    public AccountRetireDeposit depositAccount(AccountsDocuments accountsDocuments, Double amountToDeposit) {

        accountsDocuments.setAccountAmount(  accountsDocuments.getAccountAmount() + amountToDeposit );
        accountsDocuments.setQuantityMovements( accountsDocuments.getQuantityMovements() -1 );

        AccountRetireDeposit accountDeposit = AccountsMapper.mapAccountDocmentToAccountRetireDeposit( accountRepository.save(accountsDocuments) );

        //EL DEPOSITO DE DINERO A UNA CUENTA GENERA UN MOVIMIENTO
        MovementsDocuments movementAccount = new MovementsDocuments();
        movementAccount.setMovementType("DEPOSITO CUENTA");
        movementAccount.setCustomerDocument(accountsDocuments.getCustomerDocument());
        movementAccount.setAccountType(accountsDocuments.getAccountType());
        movementAccount.setAccountNumber(accountsDocuments.getAccountNumber());
        movementAccount.setAmount(amountToDeposit);
        movementAccount.setMovementDate(LocalDate.now());

        movementsRepository.save(movementAccount);

        return accountDeposit;
    }

    @Override
    public Boolean validateIfYouCanAddSigners(AccountsDocuments accountsDocuments, List<SignersRequired> signersRequired) {

        return accountsDocuments.getSigners().size() + signersRequired.size() <= 4;
    }

    @Override
    public Account addSigner(AccountsDocuments accountsDocuments, List<SignersRequired> signersRequired) {
        List<SignersDocumentComplementary> signers = signersRequired.stream().filter(Objects::nonNull).map(AccountsMapper::mapSignerRToSignerComplementary).collect(Collectors.toList());

        accountsDocuments.setSigners(signers);

        return AccountsMapper.mapAccountDocumentToAccount( accountRepository.save(accountsDocuments) );

    }
}
