package navik.domain.users;

import jakarta.persistence.AttributeConverter;
import jakarta.persistence.Converter;
import navik.domain.users.enums.EducationLevel;

@Converter(autoApply = true)
public class EducationLevelConverter
	implements AttributeConverter<EducationLevel, Integer> {

	@Override
	public Integer convertToDatabaseColumn(EducationLevel attribute) {
		return attribute == null ? null : attribute.getOrder();
	}

	@Override
	public EducationLevel convertToEntityAttribute(Integer dbData) {
		return dbData == null ? null : EducationLevel.fromOrder(dbData);
	}
}

