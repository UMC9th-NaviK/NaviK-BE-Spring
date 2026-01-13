package navik.domain.kpi.converter;

import navik.domain.kpi.enums.KpiCardType;
import org.springframework.core.convert.converter.Converter;
import org.springframework.stereotype.Component;

@Component
public class KpiCardTypeConverter implements Converter<String, KpiCardType> {

    @Override
    public KpiCardType convert(String source) {
        return KpiCardType.from(source);
    }
}
