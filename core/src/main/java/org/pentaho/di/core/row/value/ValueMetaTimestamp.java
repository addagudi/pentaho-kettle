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


package org.pentaho.di.core.row.value;

import org.pentaho.di.core.Const;
import org.pentaho.di.core.database.DatabaseInterface;
import org.pentaho.di.core.database.DatabaseMeta;
import org.pentaho.di.core.exception.KettleDatabaseException;
import org.pentaho.di.core.exception.KettleEOFException;
import org.pentaho.di.core.exception.KettleFileException;
import org.pentaho.di.core.exception.KettleValueException;
import org.pentaho.di.core.row.ValueMetaInterface;
import org.pentaho.di.core.row.value.timestamp.SimpleTimestampFormat;
import org.pentaho.di.core.util.EnvUtil;
import org.pentaho.di.core.util.Utils;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.EOFException;
import java.io.IOException;
import java.math.BigDecimal;
import java.net.SocketTimeoutException;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;
import java.util.TimeZone;
import java.util.concurrent.TimeUnit;

public class ValueMetaTimestamp extends ValueMetaDate {
  private final String conversionMode = Const
    .NVL( EnvUtil.getSystemProperty( Const.KETTLE_TIMESTAMP_NUMBER_CONVERSION_MODE ),
      Const.KETTLE_TIMESTAMP_NUMBER_CONVERSION_MODE_DEFAULT );

  public ValueMetaTimestamp() {
    this( null );
  }

  public ValueMetaTimestamp( String name ) {
    super( name, ValueMetaInterface.TYPE_TIMESTAMP );
  }

  @Override
  public boolean isDate() {
    return true;
  }

  @Override
  public Date getDate( Object object ) throws KettleValueException {
    Timestamp timestamp = getTimestamp( object );
    if ( timestamp == null ) {
      return null;
    }
    return timestamp;
  }

  @Override
  public Long getInteger( Object object ) throws KettleValueException {
    Timestamp timestamp = getTimestamp( object );
    if ( timestamp == null ) {
      return null;
    }
    TimeZone defaultTimeZone = TimeZone.getDefault();
    TimeZone currentZone = getDateFormatTimeZone();

    long milliseconds = timestamp.getTime();
    int timezoneDifference = currentZone.getOffset(milliseconds) - defaultTimeZone.getOffset(milliseconds);
    if ( Const.KETTLE_TIMESTAMP_NUMBER_CONVERSION_MODE_NANOSECONDS.equalsIgnoreCase( conversionMode ) ) {
      long seconds = TimeUnit.SECONDS.convert( milliseconds, TimeUnit.MILLISECONDS );
      long nanos = timestamp.getNanos();
      nanos += TimeUnit.NANOSECONDS.convert( timezoneDifference, TimeUnit.MILLISECONDS );
      return seconds * 1000000000L + nanos;
    } else {
      return milliseconds + timezoneDifference;
    }
  }

  @Override
  public Double getNumber( Object object ) throws KettleValueException {
    Long timestampAsInteger = getInteger( object );
    if ( null != timestampAsInteger ) {
      return timestampAsInteger.doubleValue();
    } else {
      return null;
    }
  }

  @Override
  public BigDecimal getBigNumber( Object object ) throws KettleValueException {
    Long timestampAsInteger = getInteger( object );
    if ( null != timestampAsInteger ) {
      return BigDecimal.valueOf( timestampAsInteger );
    } else {
      return null;
    }
  }

  @Override
  public Boolean getBoolean( Object object ) throws KettleValueException {
    throw new KettleValueException( toStringMeta() + ": it's not possible to convert from Timestamp to Boolean" );
  }

  @Override
  public String getString( Object object ) throws KettleValueException {
    return convertTimestampToString( getTimestamp( object ) );
  }

  public Timestamp getTimestamp( Object object ) throws KettleValueException {
    if ( object == null ) {
      return null;
    }
    switch ( type ) {
      case TYPE_TIMESTAMP:
        switch ( storageType ) {
          case STORAGE_TYPE_NORMAL:
            return (Timestamp) object;
          case STORAGE_TYPE_BINARY_STRING:
            return (Timestamp) convertBinaryStringToNativeType( (byte[]) object );
          case STORAGE_TYPE_INDEXED:
            return (Timestamp) index[ ( (Integer) object ).intValue() ];
          default:
            throw new KettleValueException( toString() + " : Unknown storage type " + storageType + " specified." );
        }
      case TYPE_STRING:
        switch ( storageType ) {
          case STORAGE_TYPE_NORMAL:
            return convertStringToTimestamp( (String) object );
          case STORAGE_TYPE_BINARY_STRING:
            return convertStringToTimestamp( (String) convertBinaryStringToNativeType( (byte[]) object ) );
          case STORAGE_TYPE_INDEXED:
            return convertStringToTimestamp( (String) index[ ( (Integer) object ).intValue() ] );
          default:
            throw new KettleValueException( toString() + " : Unknown storage type " + storageType + " specified." );
        }
      case TYPE_NUMBER:
        switch ( storageType ) {
          case STORAGE_TYPE_NORMAL:
            return convertNumberToTimestamp( (Double) object );
          case STORAGE_TYPE_BINARY_STRING:
            return convertNumberToTimestamp( (Double) convertBinaryStringToNativeType( (byte[]) object ) );
          case STORAGE_TYPE_INDEXED:
            return convertNumberToTimestamp( (Double) index[ ( (Integer) object ).intValue() ] );
          default:
            throw new KettleValueException( toString() + " : Unknown storage type " + storageType + " specified." );
        }
      case TYPE_INTEGER:
        switch ( storageType ) {
          case STORAGE_TYPE_NORMAL:
            return convertIntegerToTimestamp( (Long) object );
          case STORAGE_TYPE_BINARY_STRING:
            return convertIntegerToTimestamp( (Long) convertBinaryStringToNativeType( (byte[]) object ) );
          case STORAGE_TYPE_INDEXED:
            return convertIntegerToTimestamp( (Long) index[ ( (Integer) object ).intValue() ] );
          default:
            throw new KettleValueException( toString() + " : Unknown storage type " + storageType + " specified." );
        }
      case TYPE_BIGNUMBER:
        switch ( storageType ) {
          case STORAGE_TYPE_NORMAL:
            return convertBigNumberToTimestamp( (BigDecimal) object );
          case STORAGE_TYPE_BINARY_STRING:
            return convertBigNumberToTimestamp( (BigDecimal) convertBinaryStringToNativeType( (byte[]) object ) );
          case STORAGE_TYPE_INDEXED:
            return convertBigNumberToTimestamp( (BigDecimal) index[ ( (Integer) object ).intValue() ] );
          default:
            throw new KettleValueException( toString() + " : Unknown storage type " + storageType + " specified." );
        }
      case TYPE_BOOLEAN:
        throw new KettleValueException( toString() + " : I don't know how to convert a boolean to a timestamp." );
      case TYPE_BINARY:
        throw new KettleValueException( toString() + " : I don't know how to convert a binary value to timestamp." );
      case TYPE_SERIALIZABLE:
        throw new KettleValueException( toString()
          + " : I don't know how to convert a serializable value to timestamp." );

      default:
        throw new KettleValueException( toString() + " : Unknown type " + type + " specified." );
    }
  }

  public int compare( Object data1, Object data2 ) throws KettleValueException {
    Timestamp timestamp1 = getTimestamp( data1 );
    Timestamp timestamp2 = getTimestamp( data2 );
    int cmp = 0;
    if ( timestamp1 == null ) {
      if ( timestamp2 == null ) {
        cmp = 0;
      } else {
        cmp = -1;
      }
    } else if ( timestamp2 == null ) {
      cmp = 1;
    } else {
      cmp = timestamp1.compareTo( timestamp2 );
    }
    if ( isSortedDescending() ) {
      return -cmp;
    } else {
      return cmp;
    }
  }

  protected Timestamp convertBigNumberToTimestamp( BigDecimal bd ) {
    if ( bd == null ) {
      return null;
    }
    return convertIntegerToTimestamp( bd.longValue() );
  }

  protected Timestamp convertNumberToTimestamp( Double d ) {
    if ( d == null ) {
      return null;
    }

    return convertIntegerToTimestamp( d.longValue() );
  }

  protected Timestamp convertIntegerToTimestamp( Long longValue ) {
    if ( longValue == null ) {
      return null;
    }

    Long nanos = longValue;

    if ( Const.KETTLE_TIMESTAMP_NUMBER_CONVERSION_MODE_MILLISECONDS.equalsIgnoreCase( conversionMode ) ) {
      // Convert milliseconds to nanoseconds!
      nanos *= 1000000L;
    }

    long ss = TimeUnit.SECONDS.convert( nanos, TimeUnit.NANOSECONDS );
    long ms = TimeUnit.MILLISECONDS.convert( nanos, TimeUnit.NANOSECONDS );
    long ns = TimeUnit.NANOSECONDS.convert( ss, TimeUnit.SECONDS );
    int leftNs = (int) ( nanos - ns );

    Timestamp timestamp = new Timestamp( ms );
    timestamp.setNanos( leftNs );
    return timestamp;
  }

  protected synchronized Timestamp convertStringToTimestamp( String string ) throws KettleValueException {
    // See if trimming needs to be performed before conversion
    //
    string = Const.trimToType( string, getTrimType() );

    if ( Utils.isEmpty( string ) ) {
      return null;
    }
    Timestamp returnValue;
    try {
      returnValue = Timestamp.valueOf( string );
    } catch ( IllegalArgumentException e ) {
      try {
        returnValue = (Timestamp) getDateFormat().parse( string );
      } catch ( ParseException ex ) {
        throw new KettleValueException( toString() + " : couldn't convert string [" + string
          + "] to a timestamp, expecting format [yyyy-mm-dd hh:mm:ss.ffffff]", e );
      }
    }
    return returnValue;
  }

  protected synchronized String convertTimestampToString( Timestamp timestamp ) throws KettleValueException {

    if ( timestamp == null ) {
      return null;
    }

    return getDateFormat().format( timestamp );
  }

  @Override
  public Object convertDataFromString( String pol, ValueMetaInterface convertMeta, String nullIf, String ifNull,
                                       int trimType ) throws KettleValueException {
    // null handling and conversion of value to null
    //
    String nullValue = nullIf;
    if ( nullValue == null ) {
      switch ( convertMeta.getType() ) {
        case ValueMetaInterface.TYPE_BOOLEAN:
          nullValue = Const.NULL_BOOLEAN;
          break;
        case ValueMetaInterface.TYPE_STRING:
          nullValue = Const.NULL_STRING;
          break;
        case ValueMetaInterface.TYPE_BIGNUMBER:
          nullValue = Const.NULL_BIGNUMBER;
          break;
        case ValueMetaInterface.TYPE_NUMBER:
          nullValue = Const.NULL_NUMBER;
          break;
        case ValueMetaInterface.TYPE_INTEGER:
          nullValue = Const.NULL_INTEGER;
          break;
        case ValueMetaInterface.TYPE_DATE:
          nullValue = Const.NULL_DATE;
          break;
        case ValueMetaInterface.TYPE_BINARY:
          nullValue = Const.NULL_BINARY;
          break;
        default:
          nullValue = Const.NULL_NONE;
          break;
      }
    }

    // See if we need to convert a null value into a String
    // For example, we might want to convert null into "Empty".
    //
    if ( !Utils.isEmpty( ifNull ) ) {
      // Note that you can't pull the pad method up here as a nullComp variable
      // because you could get an NPE since you haven't checked isEmpty(pol)
      // yet!
      if ( Utils.isEmpty( pol )
        || pol.equalsIgnoreCase( Const.rightPad( new StringBuilder( nullValue ), pol.length() ) ) ) {
        pol = ifNull;
      }
    }

    // See if the polled value is empty
    // In that case, we have a null value on our hands...
    //
    if ( Utils.isEmpty( pol ) ) {
      return null;
    } else {
      // if the nullValue is specified, we try to match with that.
      //
      if ( !Utils.isEmpty( nullValue ) ) {
        // If the polled value is equal to the spaces right-padded nullValue,
        // we have a match
        //
        if ( ( nullValue.length() <= pol.length() ) && pol
          .equalsIgnoreCase( Const.rightPad( new StringBuilder( nullValue ), pol.length() ) ) ) {
          return null;
        }
      } else {
        // Verify if there are only spaces in the polled value...
        // We consider that empty as well...
        //
        if ( Const.onlySpaces( pol ) ) {
          return null;
        }
      }
    }

    // Trimming
    StringBuilder strpol;
    switch ( trimType ) {
      case ValueMetaInterface.TRIM_TYPE_LEFT:
        strpol = new StringBuilder( pol );
        while ( strpol.length() > 0 && strpol.charAt( 0 ) == ' ' ) {
          strpol.deleteCharAt( 0 );
        }
        pol = strpol.toString();

        break;
      case ValueMetaInterface.TRIM_TYPE_RIGHT:
        strpol = new StringBuilder( pol );
        while ( strpol.length() > 0 && strpol.charAt( strpol.length() - 1 ) == ' ' ) {
          strpol.deleteCharAt( strpol.length() - 1 );
        }
        pol = strpol.toString();
        break;
      case ValueMetaInterface.TRIM_TYPE_BOTH:
        strpol = new StringBuilder( pol );
        while ( strpol.length() > 0 && strpol.charAt( 0 ) == ' ' ) {
          strpol.deleteCharAt( 0 );
        }
        while ( strpol.length() > 0 && strpol.charAt( strpol.length() - 1 ) == ' ' ) {
          strpol.deleteCharAt( strpol.length() - 1 );
        }
        pol = strpol.toString();
        break;
      default:
        break;
    }

    // On with the regular program...
    // Simply call the ValueMeta routines to do the conversion
    // We need to do some effort here: copy all
    //
    return convertData( convertMeta, pol );
  }

  public Timestamp convertDateToTimestamp( Date date ) throws KettleValueException {
    if ( date == null ) {
      return null;
    }
    Timestamp result = null;
    if ( date instanceof Timestamp ) {
      result = (Timestamp) date;
    } else {
      result = new Timestamp( date.getTime() );
    }
    return result;
  }

  /**
   * Convert the specified data to the data type specified in this object.
   *
   * @param meta2 the metadata of the object to be converted
   * @param data2 the data of the object to be converted
   * @return the object in the data type of this value metadata object
   * @throws KettleValueException in case there is a data conversion error
   */
  @Override
  public Object convertData( ValueMetaInterface meta2, Object data2 ) throws KettleValueException {
    switch ( meta2.getType() ) {
      case TYPE_TIMESTAMP:
        return ( (ValueMetaTimestamp) meta2 ).getTimestamp( data2 );
      case TYPE_STRING:
        return convertStringToTimestamp( meta2.getString( data2 ) );
      case TYPE_INTEGER:
        return convertIntegerToTimestamp( meta2.getInteger( data2 ) );
      case TYPE_NUMBER:
        return convertNumberToTimestamp( meta2.getNumber( data2 ) );
      case TYPE_DATE:
        return convertDateToTimestamp( meta2.getDate( data2 ) );
      case TYPE_BIGNUMBER:
        return convertBigNumberToTimestamp( meta2.getBigNumber( data2 ) );
      default:
        throw new KettleValueException( meta2.toStringMeta() + " : can't be converted to a timestamp" );
    }
  }

  @Override
  public Object cloneValueData( Object object ) throws KettleValueException {
    Timestamp timestamp = getTimestamp( object );
    if ( timestamp == null ) {
      return null;
    }

    Timestamp clone = new Timestamp( timestamp.getTime() );
    clone.setNanos( timestamp.getNanos() );
    return clone;
  }

  @Override
  public ValueMetaInterface getMetadataPreview( DatabaseMeta databaseMeta, ResultSet rs )
    throws KettleDatabaseException {

    try {
      if ( java.sql.Types.TIMESTAMP == rs.getInt( "COLUMN_TYPE" ) ) {
        ValueMetaInterface vmi = super.getMetadataPreview( databaseMeta, rs );
        ValueMetaInterface valueMeta;
        if ( databaseMeta.supportsTimestampDataType() ) {
          valueMeta = new ValueMetaTimestamp( name );
        } else {
          valueMeta = new ValueMetaDate( name );
        }
        valueMeta.setLength( vmi.getLength() );
        valueMeta.setOriginalColumnType( vmi.getOriginalColumnType() );
        valueMeta.setOriginalColumnTypeName( vmi.getOriginalColumnTypeName() );
        valueMeta.setOriginalNullable( vmi.getOriginalNullable() );
        valueMeta.setOriginalPrecision( vmi.getOriginalPrecision() );
        valueMeta.setOriginalScale( vmi.getOriginalScale() );
        valueMeta.setOriginalSigned( vmi.getOriginalSigned() );
        return valueMeta;
      }
    } catch ( SQLException e ) {
      throw new KettleDatabaseException( e );
    }
    return null;
  }

  @Override
  public ValueMetaInterface getValueFromSQLType( DatabaseMeta databaseMeta, String name, ResultSetMetaData rm,
                                                 int index, boolean ignoreLength, boolean lazyConversion )
    throws KettleDatabaseException {

    try {
      int type = rm.getColumnType( index );
      if ( type == java.sql.Types.TIMESTAMP ) {
        int length = rm.getScale( index );
        ValueMetaInterface valueMeta;
        if ( databaseMeta.supportsTimestampDataType() ) {
          valueMeta = new ValueMetaTimestamp( name );
        } else {
          valueMeta = new ValueMetaDate( name );
        }
        valueMeta.setLength( length );

        // Also get original column details, comment, etc.
        //
        getOriginalColumnMetadata( valueMeta, rm, index, ignoreLength );

        return valueMeta;
      }

      return null;
    } catch ( Exception e ) {
      throw new KettleDatabaseException( "Error evaluating timestamp value metadata", e );
    }
  }

  @Override
  public Object getValueFromResultSet( DatabaseInterface databaseInterface, ResultSet resultSet, int index )
    throws KettleDatabaseException {

    try {

      return resultSet.getTimestamp( index + 1 );

    } catch ( Exception e ) {
      throw new KettleDatabaseException(
        toStringMeta() + " : Unable to get timestamp from resultset at index " + index, e );
    }
  }

  @Override
  public void setPreparedStatementValue( DatabaseMeta databaseMeta, PreparedStatement preparedStatement, int index,
                                         Object data ) throws KettleDatabaseException {

    try {
      if ( data != null ) {
        preparedStatement.setTimestamp( index, getTimestamp( data ) );
      } else {
        preparedStatement.setNull( index, java.sql.Types.TIMESTAMP );
      }
    } catch ( Exception e ) {
      throw new KettleDatabaseException( toStringMeta() + " : Unable to set value on prepared statement on index "
        + index, e );
    }
  }

  @Override
  public Object convertDataUsingConversionMetaData( Object data2 ) throws KettleValueException {
    if ( conversionMetadata == null ) {
      throw new KettleValueException(
        "API coding error: please specify the conversion metadata before attempting to convert value " + name );
    }

    return super.convertDataUsingConversionMetaData( data2 );
  }

  @Override
  public byte[] getBinaryString( Object object ) throws KettleValueException {

    if ( object == null ) {
      return null;
    }

    if ( isStorageBinaryString() && identicalFormat ) {
      return (byte[]) object; // shortcut it directly for better performance.
    }

    switch ( storageType ) {
      case STORAGE_TYPE_NORMAL:
        return convertStringToBinaryString( getString( object ) );
      case STORAGE_TYPE_BINARY_STRING:
        return convertStringToBinaryString( (String) convertBinaryStringToNativeType( (byte[]) object ) );
      case STORAGE_TYPE_INDEXED:
        return convertStringToBinaryString( getString( index[ ( (Integer) object ).intValue() ] ) );
      default:
        throw new KettleValueException( toString() + " : Unknown storage type " + storageType + " specified." );
    }
  }

  @Override
  public void writeData( DataOutputStream outputStream, Object object ) throws KettleFileException {
    try {
      // Is the value NULL?
      outputStream.writeBoolean( object == null );

      if ( object != null ) {
        switch ( storageType ) {
          case STORAGE_TYPE_NORMAL:
            // Handle Content -- only when not NULL
            Timestamp timestamp = convertDateToTimestamp( (Date) object );

            outputStream.writeLong( timestamp.getTime() );
            outputStream.writeInt( timestamp.getNanos() );
            break;

          case STORAGE_TYPE_BINARY_STRING:
            // Handle binary string content -- only when not NULL
            // In this case, we opt not to convert anything at all for speed.
            // That way, we can save on CPU power.
            // Since the streams can be compressed, volume shouldn't be an issue
            // at all.
            //
            writeBinaryString( outputStream, (byte[]) object );
            break;

          case STORAGE_TYPE_INDEXED:
            writeInteger( outputStream, (Integer) object ); // just an index
            break;

          default:
            throw new KettleFileException( toString() + " : Unknown storage type " + getStorageType() );
        }
      }
    } catch ( ClassCastException e ) {
      throw new RuntimeException( toString() + " : There was a data type error: the data type of "
        + object.getClass().getName() + " object [" + object + "] does not correspond to value meta ["
        + toStringMeta() + "]" );
    } catch ( IOException e ) {
      throw new KettleFileException( toString() + " : Unable to write value timestamp data to output stream", e );
    } catch ( KettleValueException e ) {
      throw new RuntimeException( toString() + " : There was a data type error: the data type of "
        + object.getClass().getName() + " object [" + object + "] does not correspond to value meta ["
        + toStringMeta() + "]" );
    }
  }

  @Override
  public Object readData( DataInputStream inputStream ) throws KettleFileException, KettleEOFException,
    SocketTimeoutException {
    try {
      // Is the value NULL?
      if ( inputStream.readBoolean() ) {
        return null; // done
      }

      switch ( storageType ) {
        case STORAGE_TYPE_NORMAL:
          // Handle Content -- only when not NULL
          long time = inputStream.readLong();
          int nanos = inputStream.readInt();
          Timestamp timestamp = new Timestamp( time );
          timestamp.setNanos( nanos );
          return timestamp;

        case STORAGE_TYPE_BINARY_STRING:
          return readBinaryString( inputStream );

        case STORAGE_TYPE_INDEXED:
          return readSmallInteger( inputStream ); // just an index: 4-bytes should be enough.

        default:
          throw new KettleFileException( toString() + " : Unknown storage type " + getStorageType() );
      }
    } catch ( EOFException e ) {
      throw new KettleEOFException( e );
    } catch ( SocketTimeoutException e ) {
      throw e;
    } catch ( IOException e ) {
      throw new KettleFileException( toString() + " : Unable to read value timestamp data from input stream", e );
    }
  }

  @Override
  public synchronized SimpleDateFormat getDateFormat() {
    return getDateFormat( getType() );
  }

  private synchronized SimpleDateFormat getDateFormat( int valueMetaType ) {
    if ( conversionMetadata != null ) {
      return new SimpleTimestampFormat( conversionMetadata.getDateFormat().toPattern() );
    }

    if ( dateFormat == null || dateFormatChanged ) {
      // This may not become static as the class is not thread-safe!
      dateFormat = new SimpleTimestampFormat( new SimpleDateFormat().toPattern() );

      String mask = getMask( valueMetaType );

      // Do we have a locale?
      //
      if ( dateFormatLocale == null || dateFormatLocale.equals( Locale.getDefault() ) ) {
        dateFormat = new SimpleTimestampFormat( mask );
      } else {
        dateFormat = new SimpleTimestampFormat( mask, dateFormatLocale );
      }

      // Do we have a time zone?
      //
      if ( dateFormatTimeZone != null ) {
        dateFormat.setTimeZone( dateFormatTimeZone );
      }

      // Set the conversion leniency as well
      //
      dateFormat.setLenient( dateFormatLenient );

      dateFormatChanged = false;
    }

    return dateFormat;
  }

  @Override
  public String getFormatMask() {
    return getTimestampFormatMask();
  }

  @Override
  public Class<?> getNativeDataTypeClass() throws KettleValueException {
    return Timestamp.class;
  }
}
