package navik.domain.kpi.enums;

import java.util.Arrays;
import navik.domain.kpi.dto.res.KpiCardResponseDTO;
import navik.domain.kpi.entity.KpiCard;
import navik.domain.kpi.exception.InvalidKpiCardTypeException;
import navik.domain.kpi.exception.code.KpiErrorCode;
import navik.global.apiPayload.exception.handler.GeneralExceptionHandler;

public enum KpiCardType {

    STRONG("strong") {
        @Override
        public KpiCardResponseDTO.Content toContent(KpiCard card) {
            return new KpiCardResponseDTO.Content(
                    card.getStrongTitle(),
                    card.getStrongContent()
            );
        }
    },

    WEAK("weak") {
        @Override
        public KpiCardResponseDTO.Content toContent(KpiCard card) {
            return new KpiCardResponseDTO.Content(
                    card.getWeakTitle(),
                    card.getWeakContent()
            );
        }
    };

    private final String value;

    KpiCardType(String value) {
        this.value = value;
    }

    public abstract KpiCardResponseDTO.Content toContent(KpiCard card);

    public static KpiCardType from(String value) {
        return Arrays.stream(values())
                .filter(t -> t.value.equalsIgnoreCase(value))
                .findFirst()
                .orElseThrow(InvalidKpiCardTypeException::new);
    }
}
