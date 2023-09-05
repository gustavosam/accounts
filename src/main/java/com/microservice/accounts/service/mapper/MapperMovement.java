package com.microservice.accounts.service.mapper;


import com.microservice.accounts.util.MovementDto;

/**
 * Esta clase contiene un método que genera un objeto MovementsDocuments
 * Para mandar esa información al microservicio de movements
 * y posteriormente guardar el movimiento en mongo db.
 * */
public class MapperMovement {

  /**
   * Esta método recibe como parámetro información para guardar movimientos
   * Se obtienen todos los valores y se asignan a un objeto MovementsDocuments.
   * */
  public static MovementDto setValues(Double amount, String clientDocument,
                                      String accountNumber, String movementType, Double commision) {

    MovementDto movement = new MovementDto();
    movement.setAmount(amount);
    movement.setClientDocument(clientDocument);
    movement.setAccountNumber(accountNumber);
    movement.setMovementType(movementType);
    movement.setCommission(commision);

    return movement;
  }
}
