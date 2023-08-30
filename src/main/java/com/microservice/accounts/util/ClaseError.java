package com.microservice.accounts.util;

import com.microservice.accounts.model.Account;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class ClaseError extends Account {

    private  String mensajeError;
    private static ClaseError instance;



    private ClaseError(){

    }

    public static ClaseError getInstance(String mensaje){
        if(instance == null){
            instance = new ClaseError();
        }
        instance.mensajeError=mensaje;
        return instance;
    }
}
