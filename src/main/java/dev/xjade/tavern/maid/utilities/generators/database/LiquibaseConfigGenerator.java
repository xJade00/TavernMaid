package dev.xjade.tavern.maid.utilities.generators.database;

import com.typesafe.config.Config;
import com.typesafe.config.ConfigFactory;
import dev.xjade.tavern.maid.utilities.generators.Generator;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;

@Generator
public class LiquibaseConfigGenerator {
  public static void main(String[] args) throws IOException {
    // Load HOCON config from /config/DatabaseConfig.conf
    Config config = ConfigFactory.parseFile(Paths.get("./config/DatabaseConfig.conf").toFile());

    // Generate Liquibase properties
    String liquibaseConfig =
        """
        changeLogFile=src/main/resources/db/changelog/db.changelog-master.xml
        url=%s
        username=%s
        password=%s
        driver=org.postgresql.Driver
        """
            .formatted(
                config.getString("url"),
                config.getString("username"),
                config.getString("password"));

    // Write the generated config to a file
    Files.write(Paths.get("config/generated-liquibase.properties"), liquibaseConfig.getBytes());
    System.out.println("Liquibase config generated: config/generated-liquibase.properties");
  }
}
