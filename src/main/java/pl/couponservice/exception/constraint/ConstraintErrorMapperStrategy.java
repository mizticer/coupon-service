package pl.couponservice.exception.constraint;


import pl.couponservice.exception.model.ExceptionDto;

public interface ConstraintErrorMapperStrategy {

    ExceptionDto getExceptionDto(String message);

    String getType();
}
