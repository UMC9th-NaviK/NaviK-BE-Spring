package navik.domain.kpi.exception;

import navik.domain.kpi.exception.code.KpiCardErrorCode;
import navik.global.apiPayload.exception.handler.GeneralExceptionHandler;

public class InvalidKpiCardTypeException extends GeneralExceptionHandler {

    public InvalidKpiCardTypeException() {
        super(KpiCardErrorCode.INVALID_KPI_CARD_TYPE);
    }
}