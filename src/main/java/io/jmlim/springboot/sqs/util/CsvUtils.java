package io.jmlim.springboot.sqs.util;

import com.opencsv.CSVWriter;
import io.jmlim.springboot.sqs.exception.CsvFileWriterException;
import lombok.extern.slf4j.Slf4j;

import java.io.FileWriter;
import java.io.IOException;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;

@Slf4j
public class CsvUtils {
    public static void csvWriter(String filename, String writerData) {
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyyMMdd");
        LocalDate now = LocalDate.now();
        String formatDate = now.format(formatter);
        FileWriter fileWriter = null;
        try {
            fileWriter = new FileWriter("/Users/jmlim/" + filename + "_" + formatDate + ".csv", true);
            CSVWriter writer = new CSVWriter(fileWriter);
            String[] record = writerData.split(",");
            writer.writeNext(record);
            writer.close();
        } catch (IOException e) {
            log.error(filename + " make failed", e);
            throw new CsvFileWriterException(filename + " make failed");
        }
    }
}