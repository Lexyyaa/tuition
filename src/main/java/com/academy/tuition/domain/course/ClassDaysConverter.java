package com.academy.tuition.domain.course;

import jakarta.persistence.AttributeConverter;
import jakarta.persistence.Converter;

/**
 * ClassDays ↔ varchar("MON,WED,FRI") 매핑 (02 §6 course.class_days).
 */
@Converter
public class ClassDaysConverter implements AttributeConverter<ClassDays, String> {

    @Override
    public String convertToDatabaseColumn(ClassDays attribute) {
        return attribute == null ? null : attribute.toStorageString();
    }

    @Override
    public ClassDays convertToEntityAttribute(String dbData) {
        return dbData == null ? null : ClassDays.fromStorageString(dbData);
    }
}
