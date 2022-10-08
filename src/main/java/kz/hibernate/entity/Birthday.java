package kz.hibernate.entity;

import javax.persistence.Converter;
import java.time.LocalDate;
import java.time.temporal.ChronoUnit;

@Converter
public record Birthday(LocalDate localDate) {
    public long getAge(){
        return ChronoUnit.YEARS.between(localDate, LocalDate.now());
    }
}
