package dev.xjade.tavern.maid.injection;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.enterprise.inject.Produces;
import jakarta.inject.Inject;
import javax.sql.DataSource;
import org.jooq.DSLContext;
import org.jooq.SQLDialect;
import org.jooq.impl.DSL;

@ApplicationScoped
public class DSLProvider {

  @Inject private DataSource dataSource;

  @Produces
  public DSLContext produceDSLContext() {
    return DSL.using(dataSource, SQLDialect.POSTGRES);
  }
}
