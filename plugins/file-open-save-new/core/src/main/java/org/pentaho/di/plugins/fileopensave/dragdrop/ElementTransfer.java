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

package org.pentaho.di.plugins.fileopensave.dragdrop;

import org.eclipse.swt.dnd.ByteArrayTransfer;
import org.eclipse.swt.dnd.TransferData;
import org.pentaho.di.core.bowl.Bowl;
import org.pentaho.di.plugins.fileopensave.api.providers.EntityType;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;

/**
 * Class for serializing elements to/from a byte array
 */
public class ElementTransfer extends ByteArrayTransfer {
  protected static final String TYPE_NAME = "pentaho-transfer_format";
  protected static final int TYPEID = registerType( TYPE_NAME );

  protected static boolean testMode; //Unit tests will set this mode
  protected static byte[] testPayload; //Unit test will place or load the byte array from here.

  private final Bowl bowl;

  /**
   * Returns the singleton element transfer instance.
   */
  public static ElementTransfer getInstance( Bowl bowl ) {
    return new ElementTransfer( bowl );
  }

  /**
   * Avoid explicit instantiation
   */
  private ElementTransfer( Bowl bowl ) {
    this.bowl = bowl;
  }

  protected Element[] fromByteArray( byte[] bytes ) {
    DataInputStream in = new DataInputStream( new ByteArrayInputStream( bytes ) );

    try {
      /* read number of elements */
      int n = in.readInt();
      /* read elements */
      Element[] elements = new Element[ n ];
      for ( int i = 0; i < n; i++ ) {
        Element element = readElement( in );
        elements[ i ] = element;
      }
      return elements;
    } catch ( IOException e ) {
      return new Element[ 0 ];
    }
  }

  @Override
  protected int[] getTypeIds() {
    return new int[] { TYPEID };
  }

  @Override
  protected String[] getTypeNames() {
    return new String[] { TYPE_NAME };
  }

  @Override
  @SuppressWarnings( "squid:S2696" )
  protected void javaToNative( Object object, TransferData transferData ) {
    Object[] objects = (Object[]) object;
    Element[] elements = new Element[ objects.length ];
    for ( int i = 0; i < objects.length; i++ ) {
      elements[ i ] = new Element( bowl, objects[ i ] );
    }
    byte[] bytes = toByteArray( elements );

    if ( testMode ) {
      testPayload = bytes;
    } else {
      if ( bytes != null ) {
        super.javaToNative( bytes, transferData );
      }
    }
  }

  @Override
  protected Object nativeToJava( TransferData transferData ) {
    byte[] bytes;
    if ( testMode ) {
      bytes = testPayload;
    } else {
      bytes = (byte[]) super.nativeToJava( transferData );
    }
    return fromByteArray( bytes );
  }

  /**
   * Reads and returns a single element from the given stream.  The Element is the minimum amount of data required to be
   * able to reconstitute the original object of the selected item(s).
   */
  private Element readElement( DataInputStream dataIn ) throws IOException {
    /**
     * Element serialization format is as follows:
     * (String) name of element
     * (int) EntityType
     * (String) Complete path to entity and identifier.
     * (String) provider
     * (String) repositoryName
     */
    String name = dataIn.readUTF();
    EntityType entityType = EntityType.fromValue( dataIn.readInt() );
    String path = dataIn.readUTF();
    String provider = dataIn.readUTF();
    String repositoryName = dataIn.readUTF();
    // This handles the fact the repository files do not store their extension on the end of the name.  I guess we
    // corrupt our data in the name of cosmetics.  Anyway it will break everything downstream if we don't fix it.
    if ( entityType == EntityType.REPOSITORY_FILE && ( path.endsWith( ".ktr" ) || path.endsWith( ".kjb" ) )
      && ( !name.endsWith( path.substring( path.length() - 4 ) ) ) ) {

      name += path.substring( path.length() - 4 );
    }
    return new Element( bowl, name, entityType, path, provider, repositoryName );
  }

  protected byte[] toByteArray( Element[] elements ) {
    /**
     * Transfer data is an array of elements.  Serialized version is:
     * (int) number of elements
     * (Element) element 1
     * (Element) element 2
     * ... repeat for each subsequent element
     * see writeElement for the (Element) format.
     */
    ByteArrayOutputStream byteOut = new ByteArrayOutputStream();
    DataOutputStream out = new DataOutputStream( byteOut );

    byte[] bytes = null;

    try {
      /* write number of markers */
      out.writeInt( elements.length );

      /* write markers */
      for ( int i = 0; i < elements.length; i++ ) {
        writeElement( elements[ i ], out );
      }
      out.close();
      bytes = byteOut.toByteArray();
    } catch ( IOException e ) {
      //when in doubt send nothing
    }
    return bytes;
  }

  /**
   * Writes the given element to the stream.
   */
  private void writeElement( Element element, DataOutputStream dataOut ) throws IOException {
    /**
     * Element serialization format is as follows:
     * (String) name of element
     * (int) EntityType
     * (String) Complete path to entity and identifier
     */
    dataOut.writeUTF( element.getName() );
    dataOut.writeInt( element.getEntityType().getValue() );
    dataOut.writeUTF( element.getPath() );
    dataOut.writeUTF( element.getProvider() );
    dataOut.writeUTF( element.getRepositoryName() == null ? "" : element.getRepositoryName() );
  }
}
