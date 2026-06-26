package com.backend.config.jackson;

import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.JsonDeserializer;

import java.io.IOException;
import java.sql.Date;
import java.sql.Time;
import java.time.LocalDate;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class TimeRangeDeserializer {

    public static class DateFromTimeRange extends JsonDeserializer<Date> {
        private static final Pattern TIME_RANGE_PATTERN = Pattern.compile("(\\d{4}-\\d{2}-\\d{2})\\s+(\\d{2}:\\d{2}:\\d{2})\\s*-\\s*(\\d{2}:\\d{2}:\\d{2})");
        private static final DateTimeFormatter DATE_FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd");

        @Override
        public Date deserialize(JsonParser p, DeserializationContext ctxt) throws IOException {
            String value = p.getValueAsString();
            
            if (value == null || value.trim().isEmpty() || "-".equals(value.trim())) {
                return null;
            }

            Matcher matcher = TIME_RANGE_PATTERN.matcher(value.trim());
            if (matcher.matches()) {
                String dateStr = matcher.group(1);
                try {
                    LocalDate localDate = LocalDate.parse(dateStr, DATE_FORMATTER);
                    return Date.valueOf(localDate);
                } catch (Exception e) {
                    return null;
                }
            }

            try {
                LocalDate localDate = LocalDate.parse(value.trim(), DATE_FORMATTER);
                return Date.valueOf(localDate);
            } catch (Exception e) {
                return null;
            }
        }
    }

    public static class TimeFromTimeRange extends JsonDeserializer<Time> {
        private static final Pattern TIME_RANGE_PATTERN = Pattern.compile("(\\d{4}-\\d{2}-\\d{2})\\s+(\\d{2}:\\d{2}:\\d{2})\\s*-\\s*(\\d{2}:\\d{2}:\\d{2})");
        private static final DateTimeFormatter TIME_FORMATTER = DateTimeFormatter.ofPattern("HH:mm:ss");

        @Override
        public Time deserialize(JsonParser p, DeserializationContext ctxt) throws IOException {
            String value = p.getValueAsString();
            
            if (value == null || value.trim().isEmpty() || "-".equals(value.trim())) {
                return null;
            }

            Matcher matcher = TIME_RANGE_PATTERN.matcher(value.trim());
            if (matcher.matches()) {
                String startTimeStr = matcher.group(2);
                try {
                    LocalTime localTime = LocalTime.parse(startTimeStr, TIME_FORMATTER);
                    return Time.valueOf(localTime);
                } catch (Exception e) {
                    return null;
                }
            }

            try {
                LocalTime localTime = LocalTime.parse(value.trim(), TIME_FORMATTER);
                return Time.valueOf(localTime);
            } catch (Exception e) {
                return null;
            }
        }
    }
}