package validators;


import org.sunbird.common.exception.BaseException;

@FunctionalInterface
public interface RequestValidatorFunction<T, R> {
    R apply(T t) throws BaseException;
}