package dev.xjade.tavern.maid.utilities.generators.database;

import com.typesafe.config.Config;
import com.typesafe.config.ConfigFactory;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;

public class JooqConfigGenerator {
  public static void main(String[] args) throws IOException {
    // Load HOCON file from /config/DatabaseConfig.conf
    Config config = ConfigFactory.parseFile(Paths.get("./config/DatabaseConfig.conf").toFile());

    // Generate XML content using values from HOCON
    String xmlConfig =
        """
        <configuration>
            <jdbc>
                <driver>org.postgresql.Driver</driver>
                <url>%s</url>
                <user>%s</user>
                <password>%s</password>
            </jdbc>
            <generator>
                <database>
                    <name>org.jooq.meta.postgres.PostgresDatabase</name>
                    <includes>.*</includes>
                    <inputSchema>public</inputSchema>
                </database>
                <target>
                    <packageName>dev.xjade.tavern.generated.jooq</packageName>
                    <directory>src/main/java</directory>
                </target>
            </generator>
        </configuration>
        """
            .formatted(
                config.getString("url"),
                config.getString("username"),
                config.getString("password"));

    // Write the generated config to a file
    Files.write(Paths.get("config/generated-jooq-config.xml"), xmlConfig.getBytes());
    System.out.println("jOOQ config generated: config/generated-jooq-config.xml");
  }
}
