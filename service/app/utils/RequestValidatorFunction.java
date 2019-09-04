package utils;


import org.sunbird.BaseException;

@FunctionalInterface
public interface RequestValidatorFunction<T, R> {
    R apply(T t) throws BaseException;
}