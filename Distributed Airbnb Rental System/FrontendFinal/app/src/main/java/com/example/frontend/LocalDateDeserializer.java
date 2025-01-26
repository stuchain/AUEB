package com.example.frontend;

import com.google.gson.*;
import java.lang.reflect.Type;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
// Sets the appropriate format of the date input
public class LocalDateDeserializer implements JsonDeserializer<LocalDate> {
    private static final DateTimeFormatter FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd");
// Takes the date-element and returns it in the appropriate format
    @Override
    public LocalDate deserialize(JsonElement json, Type typeOfT, JsonDeserializationContext context) throws JsonParseException {
        String dateString = json.getAsString();
        return LocalDate.parse(dateString, FORMATTER);
    }
}