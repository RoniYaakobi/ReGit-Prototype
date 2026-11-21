package org.roniyaakobi;

import java.nio.file.Path;

public class Constants {

    public static final Path BASE_PATH = Path.of("playground/.regit").toAbsolutePath();
    public static final Path OBJECT_STORE_PATH = BASE_PATH.resolve("objectStore");

    public static final Path HEAD = BASE_PATH.resolve("HEAD");

    public static final Path CURRENT = BASE_PATH.resolve("CURRENT");
    public static final String NONE = "None";
}
