package com.thedamones.fusionauth.jotd.demo;

import java.io.IOException;
import java.io.InputStreamReader;
import java.io.Reader;
import java.io.UncheckedIOException;
import java.util.List;

import com.joehxblog.opencsv.RecordMappingStrategy;
import com.opencsv.bean.CsvToBean;
import com.opencsv.bean.CsvToBeanBuilder;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

@Service
@Profile("demo")
public class CsvParserService {

    public <T extends Record> List<T> parseRecords(MultipartFile file, Class<T> type) {
        if (file.isEmpty()) {
            throw new IllegalArgumentException("CSV file is empty.");
        }

        try (Reader reader = new InputStreamReader(file.getInputStream())) {

            RecordMappingStrategy<T> strategy = new RecordMappingStrategy<>(type);

            CsvToBean<T> csvToBean = new CsvToBeanBuilder<T>(reader)
                    .withMappingStrategy(strategy)
                    .withIgnoreEmptyLine(true)
                    .withIgnoreLeadingWhiteSpace(true)
                    .build();

            List<T> records = csvToBean.parse();

            if (records.isEmpty()) {
                throw new IllegalArgumentException("CSV file contains no jokes.");
            }
            return records;
        } catch (IOException e) {
            throw new UncheckedIOException(e);
        }
    }
}
