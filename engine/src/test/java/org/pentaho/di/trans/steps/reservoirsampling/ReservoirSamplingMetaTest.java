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


package org.pentaho.di.trans.steps.reservoirsampling;

import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.junit.ClassRule;
import org.junit.Test;
import org.pentaho.di.core.exception.KettleException;
import org.pentaho.di.junit.rules.RestorePDIEngineEnvironment;
import org.pentaho.di.trans.steps.loadsave.LoadSaveTester;

public class ReservoirSamplingMetaTest {
  @ClassRule public static RestorePDIEngineEnvironment env = new RestorePDIEngineEnvironment();

  @Test
  public void testLoadSaveMeta() throws KettleException {

    List<String> attributes = Arrays.asList( "sample_size", "seed" );

    Map<String, String> getterMap = new HashMap<String, String>();
    getterMap.put( "sample_size", "getSampleSize" );
    getterMap.put( "seed", "getSeed" );

    Map<String, String> setterMap = new HashMap<String, String>();
    setterMap.put( "sample_size", "setSampleSize" );
    setterMap.put( "seed", "setSeed" );

    LoadSaveTester tester = new LoadSaveTester( ReservoirSamplingMeta.class, attributes, getterMap, setterMap );
    tester.testSerialization();
  }
}
