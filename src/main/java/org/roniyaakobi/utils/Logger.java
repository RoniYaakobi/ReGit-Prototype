package org.roniyaakobi.utils;

import java.io.IOException;
import java.io.Writer;
import java.nio.file.Files;
import java.nio.file.OpenOption;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;

public class Logger {
    private static final boolean IS_DEBUG = true;
    private static final Path logDirectory =
            Path.of("C:/Users/roniy/software_engeeniering/12th/ReGit_POC/ReGit-Prototype/logs");

    private static final Path currentLog = logDirectory.resolve(LocalDateTime.now().format(
            DateTimeFormatter.ofPattern("yyyy-MM-dd_HH-mm-ss")
    ) + ".log");

    public static void log(String message) throws IOException {
        String formatted = getFormatted(message);

        if (!Files.exists(currentLog)){
            Files.createFile(currentLog);
        }

        Files.write(currentLog, formatted.getBytes(), StandardOpenOption.APPEND);
        if (IS_DEBUG){
            System.out.println(formatted);
        }

    }

    private static String getFormatted(String message){
        return message;
    }
}
