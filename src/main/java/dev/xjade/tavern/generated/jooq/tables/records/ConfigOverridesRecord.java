/*
 * This file is generated by jOOQ.
 */
package dev.xjade.tavern.generated.jooq.tables.records;

import dev.xjade.tavern.generated.jooq.tables.ConfigOverrides;
import org.jooq.Field;
import org.jooq.JSONB;
import org.jooq.Record2;
import org.jooq.Record3;
import org.jooq.Row3;
import org.jooq.impl.UpdatableRecordImpl;

/** This class is generated by jOOQ. */
@SuppressWarnings({"all", "unchecked", "rawtypes"})
public class ConfigOverridesRecord extends UpdatableRecordImpl<ConfigOverridesRecord>
    implements Record3<Long, String, JSONB> {

  private static final long serialVersionUID = 1L;

  /** Setter for <code>public.config_overrides.server</code>. */
  public void setServer(Long value) {
    set(0, value);
  }

  /** Getter for <code>public.config_overrides.server</code>. */
  public Long getServer() {
    return (Long) get(0);
  }

  /** Setter for <code>public.config_overrides.key</code>. */
  public void setKey(String value) {
    set(1, value);
  }

  /** Getter for <code>public.config_overrides.key</code>. */
  public String getKey() {
    return (String) get(1);
  }

  /** Setter for <code>public.config_overrides.value</code>. */
  public void setValue(JSONB value) {
    set(2, value);
  }

  /** Getter for <code>public.config_overrides.value</code>. */
  public JSONB getValue() {
    return (JSONB) get(2);
  }

  // -------------------------------------------------------------------------
  // Primary key information
  // -------------------------------------------------------------------------

  @Override
  public Record2<Long, String> key() {
    return (Record2) super.key();
  }

  // -------------------------------------------------------------------------
  // Record3 type implementation
  // -------------------------------------------------------------------------

  @Override
  public Row3<Long, String, JSONB> fieldsRow() {
    return (Row3) super.fieldsRow();
  }

  @Override
  public Row3<Long, String, JSONB> valuesRow() {
    return (Row3) super.valuesRow();
  }

  @Override
  public Field<Long> field1() {
    return ConfigOverrides.CONFIG_OVERRIDES.SERVER;
  }

  @Override
  public Field<String> field2() {
    return ConfigOverrides.CONFIG_OVERRIDES.KEY;
  }

  @Override
  public Field<JSONB> field3() {
    return ConfigOverrides.CONFIG_OVERRIDES.VALUE;
  }

  @Override
  public Long component1() {
    return getServer();
  }

  @Override
  public String component2() {
    return getKey();
  }

  @Override
  public JSONB component3() {
    return getValue();
  }

  @Override
  public Long value1() {
    return getServer();
  }

  @Override
  public String value2() {
    return getKey();
  }

  @Override
  public JSONB value3() {
    return getValue();
  }

  @Override
  public ConfigOverridesRecord value1(Long value) {
    setServer(value);
    return this;
  }

  @Override
  public ConfigOverridesRecord value2(String value) {
    setKey(value);
    return this;
  }

  @Override
  public ConfigOverridesRecord value3(JSONB value) {
    setValue(value);
    return this;
  }

  @Override
  public ConfigOverridesRecord values(Long value1, String value2, JSONB value3) {
    value1(value1);
    value2(value2);
    value3(value3);
    return this;
  }

  // -------------------------------------------------------------------------
  // Constructors
  // -------------------------------------------------------------------------

  /** Create a detached ConfigOverridesRecord */
  public ConfigOverridesRecord() {
    super(ConfigOverrides.CONFIG_OVERRIDES);
  }

  /** Create a detached, initialised ConfigOverridesRecord */
  public ConfigOverridesRecord(Long server, String key, JSONB value) {
    super(ConfigOverrides.CONFIG_OVERRIDES);

    setServer(server);
    setKey(key);
    setValue(value);
    resetChangedOnNotNull();
  }
}
