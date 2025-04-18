/*! ******************************************************************************
 *
 * Pentaho
 *
 * Copyright (C) 2024 by Hitachi Vantara, LLC : http://www.pentaho.com
 *
 * Use of this software is governed by the Business Source License included
 * in the LICENSE.TXT file.
 *
 * Change Date: 2029-07-20
 ******************************************************************************/


package org.pentaho.di.engine.api.reporting;

import java.io.Serializable;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

/**
 * LogEntries are the payload for Logging Events. They contain information which is suitable to be outputed into a
 * traditional logging environment: message, Exception, Timestamp. It's also possible to communicate extra values to
 * inform the consuming logging system of extra information about the context where the LogEntry was produced.
 * <p>
 * Created by nbaker on 3/23/17.
 */
public class LogEntry implements Serializable {

  private static final long serialVersionUID = 5399802987623128546L;
  private String message;
  private LogLevel logLogLevel;
  private Map<String, String> extras;
  private Throwable throwable;
  private Date timestamp;

  protected LogEntry() {

  }

  public String getMessage() {
    return message;
  }

  public LogLevel getLogLogLevel() {
    return logLogLevel;
  }

  public Map<String, String> getExtras() {
    return extras;
  }

  public Throwable getThrowable() {
    return throwable;
  }

  public Date getTimestamp() {
    return timestamp;
  }

  public static final class LogEntryBuilder {
    private static long serialVersionUID = 5399802987623128546L;
    private String message;
    private LogLevel logLogLevel;
    private Map<String, String> extras = new HashMap<>();
    private Throwable throwable;
    private Date timestamp;

    public LogEntryBuilder() {
    }

    public static LogEntryBuilder aLogEntry() {
      return new LogEntryBuilder();
    }

    public LogEntryBuilder withMessage( String message ) {
      this.message = message;
      return this;
    }

    public LogEntryBuilder withLogLevel( LogLevel logLogLevel ) {
      this.logLogLevel = logLogLevel;
      return this;
    }

    public LogEntryBuilder withExtras( Map<String, String> extras ) {
      this.extras.putAll( extras );
      return this;
    }

    public LogEntryBuilder withThrowable( Throwable throwable ) {
      this.throwable = throwable;
      return this;
    }

    public LogEntryBuilder withTimestamp( Date timestamp ) {
      this.timestamp = timestamp;
      return this;
    }

    public LogEntry build() {
      LogEntry logEntry = new LogEntry();
      logEntry.logLogLevel = this.logLogLevel;
      logEntry.message = this.message;
      logEntry.timestamp = this.timestamp;
      logEntry.extras = this.extras;
      logEntry.throwable = this.throwable;
      return logEntry;
    }
  }

  @Override public boolean equals( Object o ) {
    if ( this == o ) {
      return true;
    }
    if ( o == null || getClass() != o.getClass() ) {
      return false;
    }

    LogEntry logEntry = (LogEntry) o;

    if ( logLogLevel != logEntry.logLogLevel ) {
      return false;
    }
    if ( extras != null ? !extras.equals( logEntry.extras ) : logEntry.extras != null ) {
      return false;
    }
    return timestamp.equals( logEntry.timestamp );
  }

  @Override public int hashCode() {
    int result = message.hashCode();
    result = 31 * result + logLogLevel.hashCode();
    result = 31 * result + ( extras != null ? extras.hashCode() : 0 );
    result = 31 * result + timestamp.hashCode();
    return result;
  }
}
