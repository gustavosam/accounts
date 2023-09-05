package com.microservice.accounts.feignclient;

import com.microservice.accounts.util.MovementDto;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.PostMapping;

/**
 * Esta interfaz nos permite conectarnos al microservicio movements.
 * */
@FeignClient(name = "ms-movements", url = "localhost:8084")
public interface MovementFeignClient {

  /**
   * Esta método permite guardar la información del movimiento generado.
   * */
  @PostMapping("/movement")
  void saveMovement(MovementDto movementDto);
}
