package com.microservice.accounts.service.mapper;

import com.microservice.accounts.documents.AccountsDocuments;
import com.microservice.accounts.model.*;
import com.microservice.accounts.util.complementary.SignersDocumentComplementary;
import com.microservice.accounts.util.complementary.TitularsDocumentComplementary;

import java.util.Objects;
import java.util.stream.Collectors;

public class AccountsMapper {


    public static AccountsDocuments mapAccountRequestToAccountsDocuments(AccountRequest accountRequest){
        AccountsDocuments accountsDocuments= new AccountsDocuments();

        accountsDocuments.setAccountAmount(accountRequest.getAccountAmount());
        accountsDocuments.setAccountCommission(accountRequest.getAccountCommission());
        accountsDocuments.setAccountType(accountRequest.getAccountType().getValue());
        accountsDocuments.setCustomerDocument(accountRequest.getCustomerDocument());
        accountsDocuments.setUnlimitedMovements(accountRequest.getUnlimitedMovements());
        accountsDocuments.setQuantityMovements(accountRequest.getQuantityMovements());

        if(accountRequest.getTitulars() != null){
            accountsDocuments.setTitulars(
                    accountRequest.getTitulars().stream().filter(Objects::nonNull).map(AccountsMapper::mapTitularInToTitularComplementary).collect(Collectors.toList())
            );
        }


        if(accountRequest.getSigners() != null){
            accountsDocuments.setSigners(
                    accountRequest.getSigners().stream().filter(Objects::nonNull).map(AccountsMapper::mapSignerToSignerComplementary).collect(Collectors.toList())
            );
        }


        return accountsDocuments;
    }

    public static TitularsDocumentComplementary mapTitularInToTitularComplementary(TitularsIn titulars){
        TitularsDocumentComplementary complementary = new TitularsDocumentComplementary();

        complementary.setDocument(titulars.getDocument());
        complementary.setName(titulars.getName());

        return complementary;
    }

    public static SignersDocumentComplementary mapSignerToSignerComplementary(Signers signers){
        SignersDocumentComplementary complementary = new SignersDocumentComplementary();

        complementary.setDocument(signers.getDocument());
        complementary.setFullName(signers.getFullName());

        return complementary;
    }

    public static SignersDocumentComplementary mapSignerRToSignerComplementary(SignersRequired signers){
        SignersDocumentComplementary complementary = new SignersDocumentComplementary();

        complementary.setDocument(signers.getDocument());
        complementary.setFullName(signers.getFullName());

        return complementary;
    }

    public static Account mapAccountDocumentToAccount(AccountsDocuments accountsDocuments){

        Account account = new Account();

        account.setAccountAmount(accountsDocuments.getAccountAmount());
        account.setAccountNumber(accountsDocuments.getAccountNumber());
        account.setAccountCommission(accountsDocuments.getAccountCommission());
        account.setAccountType(accountsDocuments.getAccountType());
        account.setUnlimitedMovements(accountsDocuments.getUnlimitedMovements());
        account.setQuantityMovements(accountsDocuments.getQuantityMovements());
        account.setCustomerDocument(accountsDocuments.getCustomerDocument());
        account.setCreationDate(accountsDocuments.getAccountCreationDate());

        if(accountsDocuments.getTitulars() != null){
            account.setTitulars(
                    accountsDocuments.getTitulars().stream().filter(Objects::nonNull).map(AccountsMapper::mapTitularsDocumentToTitularsOut).collect(Collectors.toList())
            );
        }

        if(accountsDocuments.getSigners() != null){
            account.setSigners(
                    accountsDocuments.getSigners().stream().filter(Objects::nonNull).map(AccountsMapper::mapSignersDocumentToSigners).collect(Collectors.toList())
            );
        }


        return account;
    }

    public static TitularsOut mapTitularsDocumentToTitularsOut(TitularsDocumentComplementary titularsDocumentComplementary){
        TitularsOut titulars = new TitularsOut();

        titulars.setDocument(titularsDocumentComplementary.getDocument());
        titulars.setName(titularsDocumentComplementary.getName());

        return titulars;

    }

    public static Signers mapSignersDocumentToSigners(SignersDocumentComplementary signersDocumentComplementary){

        Signers signers = new Signers();
        signers.setDocument(signersDocumentComplementary.getDocument());
        signers.setFullName(signersDocumentComplementary.getFullName());

        return signers;
    }


    public static AccountRetireDeposit mapAccountDocmentToAccountRetireDeposit(AccountsDocuments accountsDocuments){

        AccountRetireDeposit accountRetireDeposit = new AccountRetireDeposit();

        accountRetireDeposit.setAccountNumber(accountsDocuments.getAccountNumber());
        accountRetireDeposit.setAccountAmount(accountsDocuments.getAccountAmount());
        accountRetireDeposit.setAccountType(accountsDocuments.getAccountType());

        return accountRetireDeposit;
    }

}
