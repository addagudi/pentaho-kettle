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


package org.pentaho.di.trans.step;

import java.util.List;

import org.pentaho.di.core.exception.KettleException;

/**
 * This interface allows an external program to inject metadata using a standard flat set of metadata attributes.
 *
 * @author matt
 * @deprecated Use annotation-based injection instead
 */
@Deprecated
public interface StepMetaInjectionInterface {
  /**
   * @return A list of step injection metadata entries. In case the data type of the entry is NONE (0) you will get at
   *         least one entry in the details section. You can use this list to see a description of all the metadata.
   */
  public List<StepInjectionMetaEntry> getStepInjectionMetadataEntries() throws KettleException;

  /**
   * Inject the metadata specified into the step metadata
   * @param metadata the metadata to inject.
   * @throws KettleException
   */
  public void injectStepMetadataEntries( List<StepInjectionMetaEntry> metadata ) throws KettleException;

  /**
   * Extracts the step metadata in a standard fashion.  Enables unit testing more easily by extracting the step
   * metadata and then doing the re-injection into an empty step.
   * @return A list of step injection metadata entries in a standard fashion.  Includes both the entry and the value
   *         for the entry.
   * @throws KettleException
   */
  public List<StepInjectionMetaEntry> extractStepMetadataEntries() throws KettleException;
}
