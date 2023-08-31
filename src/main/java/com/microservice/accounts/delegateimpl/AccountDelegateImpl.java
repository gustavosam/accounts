package com.microservice.accounts.delegateimpl;

import com.microservice.accounts.api.AccountApiDelegate;
import com.microservice.accounts.documents.AccountsDocuments;
import com.microservice.accounts.model.Account;
import com.microservice.accounts.model.AccountRequest;
import com.microservice.accounts.model.AccountRetireDeposit;
import com.microservice.accounts.model.SignersRequired;
import com.microservice.accounts.service.AccountsService;
import com.microservice.accounts.service.mapper.AccountsMapper;
import com.microservice.accounts.util.ClaseError;
import com.microservice.accounts.util.ClaseErrorRetire;
import com.microservice.accounts.util.complementary.CustomersComplementary;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class AccountDelegateImpl implements AccountApiDelegate {

    @Autowired
    private AccountsService accountsService;

    @Override
    public ResponseEntity<Account> createAccount(AccountRequest accountRequest){

        if(accountRequest == null){
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(ClaseError.getInstance("Ingrese información"));
        }

        if(accountRequest.getAccountType() == null){
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(ClaseError.getInstance("Ingrese el tipo de cuenta"));
        }

        if(accountRequest.getAccountAmount() == null){
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(ClaseError.getInstance("Ingrese el monto de la cuenta"));
        }

        if( accountRequest.getAccountCommission() != null && accountRequest.getAccountCommission() > 0 && !accountsService.payCommission(accountRequest.getAccountType().getValue())){
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(ClaseError.getInstance("Este tipo de cuenta no debe pagar comisión"));
        }

        if( accountRequest.getAccountCommission() == null && accountRequest.getAccountType().getValue().equalsIgnoreCase("CORRIENTE")){
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(ClaseError.getInstance("Las cuentas corrientes si pagan comisión"));
        }

        if(accountRequest.getUnlimitedMovements() == null){
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(ClaseError.getInstance("Indique si la cuenta posee movimientos ilimitados o no"));
        }

        if(accountRequest.getAccountType().getValue().equalsIgnoreCase("CORRIENTE") && accountRequest.getUnlimitedMovements() && accountRequest.getQuantityMovements() != null){
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(ClaseError.getInstance("Las cuentas corrientes tienen movimientos ilimitados, no es necesario colocar la cantidad de movimientos"));
        }



        if(accountRequest.getUnlimitedMovements() != (accountsService.ilimitMovements(accountRequest.getAccountType().getValue()))){
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(ClaseError.getInstance("Las cuentas ahorro y plazo fijo, tienen un limite de movimientos"));
        }

        if(accountRequest.getQuantityMovements() == null && (accountRequest.getAccountType().getValue().equalsIgnoreCase("AHORRO") || accountRequest.getAccountType().getValue().equalsIgnoreCase("PLAZOFIJO") )){
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(ClaseError.getInstance("Indicar la cantidad de movimientos para la cuenta de ahorro o plazo fijo"));
        }

        if(accountRequest.getQuantityMovements() != null && accountRequest.getQuantityMovements() > 1 && (accountRequest.getAccountType().getValue().equalsIgnoreCase("PLAZOFIJO"))){
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(ClaseError.getInstance("Las cuentas plazo fijo solo deben tener 1 movimiento"));
        }

        if(accountRequest.getCustomerDocument() == null){
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(ClaseError.getInstance("Ingrese el cliente que apertura la cuenta"));
        }

        CustomersComplementary customer = accountsService.getCustomer(accountRequest.getCustomerDocument());

        if(customer.getCustomerDocument() == null){
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(ClaseError.getInstance("El cliente no existe"));
        }

        List<AccountsDocuments> listAccountsByCustomer = accountsService.getAccountsByCustomer(customer.getCustomerDocument());

        if(customer.getCustomerType().equalsIgnoreCase("PERSONAL") && accountRequest.getAccountType().getValue().equalsIgnoreCase("AHORRO") && accountsService.existAccountAhorro(listAccountsByCustomer)){
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(ClaseError.getInstance("Eres un cliente PERSONAL, ya posees una cuenta de ahorros"));
        }

        if(customer.getCustomerType().equalsIgnoreCase("PERSONAL") && accountRequest.getAccountType().getValue().equalsIgnoreCase("CORRIENTE") && accountsService.existAccountCorriente(listAccountsByCustomer)){
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(ClaseError.getInstance("Eres un cliente PERSONAL, ya posees una cuenta corriente"));
        }

        if(! accountsService.listTitularIsCorrect(accountRequest.getTitulars())){
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(ClaseError.getInstance("Ingrese el titular de la cuenta"));
        }

        if(customer.getCustomerType().equalsIgnoreCase("PERSONAL") && (!accountsService.justATitularPersonal(accountRequest.getTitulars()))){
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(ClaseError.getInstance("Eres un cliente personal solo puedes tener un titular"));
        }

        if(customer.getCustomerType().equalsIgnoreCase("COMPANY") && (accountRequest.getAccountType().getValue().equalsIgnoreCase("AHORRO"))){
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(ClaseError.getInstance("Los clientes empresariales no pueden aperturar cuentas de ahorro"));
        }

        if(customer.getCustomerType().equalsIgnoreCase("COMPANY") && (accountRequest.getAccountType().getValue().equalsIgnoreCase("PLAZOFIJO"))){
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(ClaseError.getInstance("Los clientes empresariales no pueden aperturar cuentas de plazo fijo"));
        }

        if(customer.getCustomerType().equalsIgnoreCase("COMPANY") &&  accountRequest.getSigners() != null && !accountsService.listSignersIsCorrect(accountRequest.getSigners())){
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(ClaseError.getInstance("Ingresa correctamente los datos de los firmantes"));
        }

        if(customer.getCustomerType().equalsIgnoreCase("COMPANY")  && accountRequest.getSigners() != null && accountsService.validateQuantitySignersCreationAccount(accountRequest.getSigners())){
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(ClaseError.getInstance("No puedes agregar más de 4 firmantes en una cuenta bancaria empresarial"));
        }

        return ResponseEntity.status(HttpStatus.OK).body(accountsService.createAccount(accountRequest));


    }

    @Override
    public ResponseEntity<AccountRetireDeposit> retireAccount(String accountNumber, Double amount){

        if(accountNumber == null){
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(ClaseErrorRetire.getInstance("Ingrese el número de cuenta"));
        }

        if(amount == null){
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(ClaseErrorRetire.getInstance("Ingrese el monto a retirar"));
        }

        AccountsDocuments accountsDocuments = accountsService.getAccount(accountNumber);

        if(accountsDocuments.getAccountNumber() == null){
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(ClaseErrorRetire.getInstance("Este número de cuenta no existe"));
        }

        if(accountsDocuments.getAccountType().equalsIgnoreCase("CORRIENTE") && !accountsService.validateIfYouCanRetireCorriente(accountsDocuments, amount)){
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(ClaseErrorRetire.getInstance("Saldo insuficiente"));
        }

        if( (accountsDocuments.getAccountType().equalsIgnoreCase("AHORRO") || accountsDocuments.getAccountType().equalsIgnoreCase("PLAZOFIJO")) && !accountsService.validateIfYouCanRetire(accountsDocuments, amount) ){
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(ClaseErrorRetire.getInstance("Tu cuenta no posee saldo suficiente o no tienes más movimientos durante el mes"));
        }



        return ResponseEntity.status(HttpStatus.OK).body(accountsService.retireAccount(accountsDocuments, amount));

    }

    @Override
    public ResponseEntity<AccountRetireDeposit> depositAccount(String accountNumber, Double amount){

        if(accountNumber == null){
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(ClaseErrorRetire.getInstance("Ingrese el número de cuenta"));
        }

        if(amount == null){
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(ClaseErrorRetire.getInstance("Ingrese el monto a retirar"));
        }

        AccountsDocuments accountsDocuments = accountsService.getAccount(accountNumber);

        if(accountsDocuments.getAccountNumber() == null){
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(ClaseErrorRetire.getInstance("Este número de cuenta no existe"));
        }

        if( (accountsDocuments.getAccountType().equalsIgnoreCase("AHORRO") || accountsDocuments.getAccountType().equalsIgnoreCase("PLAZOFIJO")) && !accountsService.validateIfYouCanDeposit(accountsDocuments) ){
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(ClaseErrorRetire.getInstance("No puedes realizar el deposito ya que no cuentas con movimientos"));
        }

        return ResponseEntity.status(HttpStatus.OK).body(accountsService.depositAccount(accountsDocuments, amount));
    }

    @Override
    public ResponseEntity<Account> addSignersAccount(String accountNumber, List<SignersRequired> signersRequired){

        if(accountNumber == null){
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(ClaseError.getInstance("Ingrese el número de cuenta"));
        }

        AccountsDocuments accountsDocuments = accountsService.getAccount(accountNumber);

        if(accountsDocuments.getAccountNumber() == null){
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(ClaseError.getInstance("Este número de cuenta no existe"));
        }

        CustomersComplementary customer = accountsService.getCustomer(accountsDocuments.getCustomerDocument());

        if(customer.getCustomerType().equalsIgnoreCase("PERSONAL")){
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(ClaseError.getInstance("Esta es una cuenta personal, no puede agregar firmantes"));
        }

        if(accountsDocuments.getSigners() != null && accountsDocuments.getSigners().size() == 4){
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(ClaseError.getInstance("Alcanzaste el máximo de firmantes"));
        }

        if(accountsDocuments.getSigners() != null && !accountsService.validateIfYouCanAddSigners(accountsDocuments, signersRequired)){
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(ClaseError.getInstance("No puedes ingresar más de 4 firmantes a la cuenta"));
        }

        if(accountsDocuments.getSigners() == null && signersRequired.isEmpty()){
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(ClaseError.getInstance("Tu lista de firmantes no puede estar vacía"));
        }

        if(accountsDocuments.getSigners() == null && !accountsService.listAddedSignersIsCorrect(signersRequired)){
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(ClaseError.getInstance("Verifica los datos de tus firmantes"));
        }

        if(accountsDocuments.getSigners() == null && signersRequired.size() > 4){
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(ClaseError.getInstance("No puedes ingresar más de 4 firmantes"));
        }

        return ResponseEntity.status(HttpStatus.OK).body(accountsService.addSigner(accountsDocuments, signersRequired));


    }

    @Override
    public ResponseEntity<Account> consultAccount(String accountNumber){

        AccountsDocuments accountsDocuments = accountsService.getAccount(accountNumber);

        if(accountsDocuments.getAccountNumber() == null){
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(ClaseError.getInstance("Este número de cuenta no existe"));
        }

        return ResponseEntity.status(HttpStatus.OK).body(AccountsMapper.mapAccountDocumentToAccount(accountsService.getAccount(accountNumber)));
    }
}
