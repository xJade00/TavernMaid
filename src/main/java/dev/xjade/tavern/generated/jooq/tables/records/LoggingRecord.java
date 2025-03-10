/*
 * This file is generated by jOOQ.
 */
package dev.xjade.tavern.generated.jooq.tables.records;

import dev.xjade.tavern.generated.jooq.tables.Logging;
import java.time.LocalDateTime;
import org.jooq.Field;
import org.jooq.JSONB;
import org.jooq.Record1;
import org.jooq.Record6;
import org.jooq.Row6;
import org.jooq.impl.UpdatableRecordImpl;

/** This class is generated by jOOQ. */
@SuppressWarnings({"all", "unchecked", "rawtypes"})
public class LoggingRecord extends UpdatableRecordImpl<LoggingRecord>
    implements Record6<Long, String, JSONB, String, LocalDateTime, Long> {

  private static final long serialVersionUID = 1L;

  /** Setter for <code>public.logging.id</code>. */
  public void setId(Long value) {
    set(0, value);
  }

  /** Getter for <code>public.logging.id</code>. */
  public Long getId() {
    return (Long) get(0);
  }

  /** Setter for <code>public.logging.category</code>. */
  public void setCategory(String value) {
    set(1, value);
  }

  /** Getter for <code>public.logging.category</code>. */
  public String getCategory() {
    return (String) get(1);
  }

  /** Setter for <code>public.logging.variables</code>. */
  public void setVariables(JSONB value) {
    set(2, value);
  }

  /** Getter for <code>public.logging.variables</code>. */
  public JSONB getVariables() {
    return (JSONB) get(2);
  }

  /** Setter for <code>public.logging.description</code>. */
  public void setDescription(String value) {
    set(3, value);
  }

  /** Getter for <code>public.logging.description</code>. */
  public String getDescription() {
    return (String) get(3);
  }

  /** Setter for <code>public.logging.time</code>. */
  public void setTime(LocalDateTime value) {
    set(4, value);
  }

  /** Getter for <code>public.logging.time</code>. */
  public LocalDateTime getTime() {
    return (LocalDateTime) get(4);
  }

  /** Setter for <code>public.logging.server</code>. */
  public void setServer(Long value) {
    set(5, value);
  }

  /** Getter for <code>public.logging.server</code>. */
  public Long getServer() {
    return (Long) get(5);
  }

  // -------------------------------------------------------------------------
  // Primary key information
  // -------------------------------------------------------------------------

  @Override
  public Record1<Long> key() {
    return (Record1) super.key();
  }

  // -------------------------------------------------------------------------
  // Record6 type implementation
  // -------------------------------------------------------------------------

  @Override
  public Row6<Long, String, JSONB, String, LocalDateTime, Long> fieldsRow() {
    return (Row6) super.fieldsRow();
  }

  @Override
  public Row6<Long, String, JSONB, String, LocalDateTime, Long> valuesRow() {
    return (Row6) super.valuesRow();
  }

  @Override
  public Field<Long> field1() {
    return Logging.LOGGING.ID;
  }

  @Override
  public Field<String> field2() {
    return Logging.LOGGING.CATEGORY;
  }

  @Override
  public Field<JSONB> field3() {
    return Logging.LOGGING.VARIABLES;
  }

  @Override
  public Field<String> field4() {
    return Logging.LOGGING.DESCRIPTION;
  }

  @Override
  public Field<LocalDateTime> field5() {
    return Logging.LOGGING.TIME;
  }

  @Override
  public Field<Long> field6() {
    return Logging.LOGGING.SERVER;
  }

  @Override
  public Long component1() {
    return getId();
  }

  @Override
  public String component2() {
    return getCategory();
  }

  @Override
  public JSONB component3() {
    return getVariables();
  }

  @Override
  public String component4() {
    return getDescription();
  }

  @Override
  public LocalDateTime component5() {
    return getTime();
  }

  @Override
  public Long component6() {
    return getServer();
  }

  @Override
  public Long value1() {
    return getId();
  }

  @Override
  public String value2() {
    return getCategory();
  }

  @Override
  public JSONB value3() {
    return getVariables();
  }

  @Override
  public String value4() {
    return getDescription();
  }

  @Override
  public LocalDateTime value5() {
    return getTime();
  }

  @Override
  public Long value6() {
    return getServer();
  }

  @Override
  public LoggingRecord value1(Long value) {
    setId(value);
    return this;
  }

  @Override
  public LoggingRecord value2(String value) {
    setCategory(value);
    return this;
  }

  @Override
  public LoggingRecord value3(JSONB value) {
    setVariables(value);
    return this;
  }

  @Override
  public LoggingRecord value4(String value) {
    setDescription(value);
    return this;
  }

  @Override
  public LoggingRecord value5(LocalDateTime value) {
    setTime(value);
    return this;
  }

  @Override
  public LoggingRecord value6(Long value) {
    setServer(value);
    return this;
  }

  @Override
  public LoggingRecord values(
      Long value1, String value2, JSONB value3, String value4, LocalDateTime value5, Long value6) {
    value1(value1);
    value2(value2);
    value3(value3);
    value4(value4);
    value5(value5);
    value6(value6);
    return this;
  }

  // -------------------------------------------------------------------------
  // Constructors
  // -------------------------------------------------------------------------

  /** Create a detached LoggingRecord */
  public LoggingRecord() {
    super(Logging.LOGGING);
  }

  /** Create a detached, initialised LoggingRecord */
  public LoggingRecord(
      Long id,
      String category,
      JSONB variables,
      String description,
      LocalDateTime time,
      Long server) {
    super(Logging.LOGGING);

    setId(id);
    setCategory(category);
    setVariables(variables);
    setDescription(description);
    setTime(time);
    setServer(server);
    resetChangedOnNotNull();
  }
}
