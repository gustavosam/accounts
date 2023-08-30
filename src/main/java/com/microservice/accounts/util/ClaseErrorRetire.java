package com.microservice.accounts.util;


import com.microservice.accounts.model.Account;
import com.microservice.accounts.model.AccountRetireDeposit;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class ClaseErrorRetire extends AccountRetireDeposit {

    private  String mensajeError;
    private static ClaseErrorRetire instance;



    private ClaseErrorRetire(){

    }

    public static ClaseErrorRetire getInstance(String mensaje){
        if(instance == null){
            instance = new ClaseErrorRetire();
        }
        instance.mensajeError=mensaje;
        return instance;
    }
}
