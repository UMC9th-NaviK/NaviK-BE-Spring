package navik.global.apiPayload.exception;

import navik.global.apiPayload.exception.handler.GeneralExceptionHandler;

public final class ExceptionUtils {

    private ExceptionUtils() {}

    public static GeneralExceptionHandler findGeneralException(Throwable t) {
        while (t != null) {
            if (t instanceof GeneralExceptionHandler ghe) {
                return ghe;
            }
            t = t.getCause();
        }
        return null;
    }
}