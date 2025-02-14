package dev.xjade.tavern.maid.injection;

import dev.xjade.tavern.maid.config.DatabaseConfig;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.enterprise.inject.Produces;
import jakarta.inject.Inject;
import javax.sql.DataSource;
import org.postgresql.ds.PGSimpleDataSource;

@ApplicationScoped
public class DataSourceProvider {

  @Inject private DatabaseConfig databaseConfig;

  @Produces
  public DataSource provideDataSource() {
    PGSimpleDataSource dataSource = new PGSimpleDataSource();
    dataSource.setURL(databaseConfig.url());
    dataSource.setUser(databaseConfig.username());
    dataSource.setPassword(databaseConfig.password());
    return dataSource;
  }
}
