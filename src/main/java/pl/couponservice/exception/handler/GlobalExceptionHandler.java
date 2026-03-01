package pl.couponservice.exception.handler;

import org.hibernate.exception.ConstraintViolationException;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import pl.couponservice.exception.CouponAlreadyUsedByUserException;
import pl.couponservice.exception.CouponCountryMismatchException;
import pl.couponservice.exception.CouponExhaustedException;
import pl.couponservice.exception.GeoLocationException;
import pl.couponservice.exception.NotFoundException;
import pl.couponservice.exception.constraint.ConstraintErrorMapperStrategy;
import pl.couponservice.exception.model.ExceptionDto;
import pl.couponservice.exception.model.ValidationErrorDetails;
import pl.couponservice.exception.model.ValidationErrorDto;

import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.function.Function;
import java.util.stream.Collectors;

@RestControllerAdvice
public class GlobalExceptionHandler {

    private final Map<String, ConstraintErrorMapperStrategy> constraintErrorMapper;

    public GlobalExceptionHandler(Set<ConstraintErrorMapperStrategy> handlers) {
        this.constraintErrorMapper = handlers.stream()
                .collect(Collectors.toMap(ConstraintErrorMapperStrategy::getType, Function.identity()));
    }

    @ExceptionHandler(ConstraintViolationException.class)
    @ResponseStatus(HttpStatus.CONFLICT)
    public ExceptionDto handleConstraintViolationException(ConstraintViolationException e) {
        String constraintName = e.getConstraintName();
        if (constraintErrorMapper.containsKey(constraintName)) {
            return constraintErrorMapper.get(constraintName).getExceptionDto(e.getMessage());
        }
        return new ExceptionDto("Database constraint violation: " + constraintName);
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public ValidationErrorDto handleValidationExceptions(MethodArgumentNotValidException ex) {
        List<ValidationErrorDetails> errorDetails = ex.getFieldErrors().stream()
                .map(fe -> new ValidationErrorDetails(fe.getField(), fe.getDefaultMessage()))
                .toList();
        return new ValidationErrorDto(errorDetails);
    }

    @ExceptionHandler(NotFoundException.class)
    @ResponseStatus(HttpStatus.NOT_FOUND)
    public ExceptionDto handleNotFoundException(NotFoundException e) {
        return new ExceptionDto(e.getMessage());
    }

    @ExceptionHandler(CouponExhaustedException.class)
    @ResponseStatus(HttpStatus.CONFLICT)
    public ExceptionDto handleCouponExhaustedException(CouponExhaustedException e) {
        return new ExceptionDto(e.getMessage());
    }

    @ExceptionHandler(CouponCountryMismatchException.class)
    @ResponseStatus(HttpStatus.FORBIDDEN)
    public ExceptionDto handleCouponCountryMismatchException(CouponCountryMismatchException e) {
        return new ExceptionDto(e.getMessage());
    }

    @ExceptionHandler(CouponAlreadyUsedByUserException.class)
    @ResponseStatus(HttpStatus.CONFLICT)
    public ExceptionDto handleCouponAlreadyUsedByUserException(CouponAlreadyUsedByUserException e) {
        return new ExceptionDto(e.getMessage());
    }

    @ExceptionHandler(GeoLocationException.class)
    @ResponseStatus(HttpStatus.SERVICE_UNAVAILABLE)
    public ExceptionDto handleGeoLocationException(GeoLocationException e) {
        return new ExceptionDto(e.getMessage());
    }
}