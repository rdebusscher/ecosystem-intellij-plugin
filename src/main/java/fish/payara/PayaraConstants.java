package fish.payara;

import java.io.File;
import java.util.regex.Pattern;

public interface PayaraConstants {

    Pattern PAYARA_JAR_PATTERN = Pattern.compile("payara-.*\\.jar");

    String PAYARA_MODULES_DIRECTORY_NAME = "glassfish" + File.separator + "modules";

    String PAYARA_BIN_DIRECTORY_NAME = "bin";
}
