package com.microservice.accounts.util;

/**
 * Esta clase contiene las constantes que serán usadas en el sistema.
 * */
public class Constants {

  public static final String ACCOUNT_EMPTY = "Ingrese el número de cuenta";

  public static final String ACCOUNT_TYPE_EMPTY = "Ingrese el tipo de cuenta";

  public static final String AMOUNT_EMPTY = "Ingrese el monto de la cuenta";

  public static final String PERSONAL = "PERSONAL";

  public static final String COMPANY = "COMPANY";

  public static final String SAVING_ACCOUNT = "AHORRO";

  public static final String ORDINARY_ACCOUNT = "CORRIENTE";

  public static final String FIXED_TERM_ACCOUNT = "PLAZOFIJO";

  public static final String EXIST_SAVING_ACCOUNT = "Personal Client tiene cuenta de ahorro";

  public static final String CANT_HAVE_SAVING_ACCOUNT = "Company no puede abrir cuenta de ahorro";

  public static final String CANT_HAVE_FIXED_TERM_ACCOUNT = "Company no puede abrir cuenta P.Fijo";

  public static final String SIGNERS_INCORRECT = "Ingresa correctamente los datos de los firmantes";

  public static final String SIGNERS_MAXIMUM = "No puedes agregar más de 4 firmantes";

  public static final String HAS_ORDINARY_ACCOUNT = "PersonalClient, ya posee una cuenta corriente";

  public static final String TITULARS_EMPTY = "Ingrese al/los titular/es de la cuenta";

  public static final String JUST_A_TITULAR = "Cliente personal solo puedes tener un titular";

  public static final String CLIENT_EMPTY = "Ingrese el documento del cliente";
  public static final String ACCOUNT_CREATED = "ALTA CUENTA";

  public static final String ACCOUNT_RETIRE = "RETIRO CUENTA";

  public static final String ACCOUNT_DEPOSIT = "DEPOSITO CUENTA";

  public static final String AMOUNT_RETIRE_EMPTY = "Ingrese el monto a retirar";

  public static final String AMOUNT_DEPOSIT_EMPTY = "Ingrese el monto a depositar";

  public static final String DEPOSIT_INCORRECT = "El deposito no puede ser menor a la comisión";

  public static final String CLIENT_NOT_EXIST = "El documento del cliente ingresado no existe";

  public static final String ACCOUNT_NOT_EXIST = "Este número de cuenta no existe";

  public static final String CANT_HAVE_SIGNERS = "CuentaPersonal, no puede agregar firmantes";

  public static final String MAX_SIGNERS = "No puedes registrar más de 4 firmantes";

  public static final String SIGNERS_NOT_EMPTY = "Tu lista de firmantes no puede estar vacía";

  public static final String SIGNERS_INFORMATION_INVALID = "Verifica los datos de tus firmantes";
  public static final String NOT_MONEY = "Saldo insuficiente";

  public static final String PERSONAL_VIP = "PERSONALVIP";

  public static final String COMPANY_PYME = "COMPANYPYME";

  public static final String NOT_CREDIT_CARD = "VIP Y PYME Deben tener una creditCard";

  public static final String VIP_AMOUNT_MINOR = "Eres un cliente vip el monto mínimo es 500";

  public static final double COMMISSION = 5.00;

}
