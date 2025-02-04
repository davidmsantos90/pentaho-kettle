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


package org.pentaho.di.trans.steps.file;

import java.util.List;

import com.google.common.base.Preconditions;
import org.pentaho.di.core.bowl.Bowl;
import org.pentaho.di.core.bowl.DefaultBowl;
import org.pentaho.di.core.fileinput.FileInputList;
import org.pentaho.di.core.injection.InjectionDeep;
import org.pentaho.di.core.variables.VariableSpace;
import org.pentaho.di.i18n.BaseMessages;
import org.pentaho.di.resource.ResourceReference;
import org.pentaho.di.trans.TransMeta;
import org.pentaho.di.trans.step.StepMeta;

/**
 * Base meta for file-based input steps.
 *
 * @author Alexander Buloichik
 */
public abstract class BaseFileInputMeta<A extends BaseFileInputAdditionalField, I extends BaseFileInputFiles, F extends BaseFileField>
    extends BaseFileMeta {
  private static Class<?> PKG = BaseFileInputMeta.class; // for i18n purposes, needed by Translator2!!

  public static final String[] RequiredFilesCode = new String[] { "N", "Y" };

  public static final String NO = "N";

  public static final String YES = "Y";

  public static final String[] RequiredFilesDesc =
      new String[] { BaseMessages.getString( PKG, "System.Combo.No" ), BaseMessages.getString( PKG,
          "System.Combo.Yes" ) };

  @InjectionDeep
  public I inputFiles;

  /** The fields to import... */
  @InjectionDeep
  public F[] inputFields;

  /**
   * @return the input fields.
   */
  public F[] getInputFields() {
    return inputFields;
  }

  @InjectionDeep
  public BaseFileErrorHandling errorHandling = new BaseFileErrorHandling();
  @InjectionDeep
  public A additionalOutputFields;

  public Object clone() {
    BaseFileInputMeta<BaseFileInputAdditionalField, BaseFileInputFiles, BaseFileField> retval = (BaseFileInputMeta<BaseFileInputAdditionalField, BaseFileInputFiles, BaseFileField>) super.clone();

    retval.inputFiles = (BaseFileInputFiles) inputFiles.clone();
    retval.errorHandling = (BaseFileErrorHandling) errorHandling.clone();
    retval.additionalOutputFields = (BaseFileInputAdditionalField) additionalOutputFields.clone();

    return retval;
  }

  /**
   * @param fileRequiredin The fileRequired to set.
   */
  public void inputFiles_fileRequired( String[] fileRequiredin ) {
    for ( int i = 0; i < fileRequiredin.length; i++ ) {
      inputFiles.fileRequired[i] = getRequiredFilesCode( fileRequiredin[i] );
    }
  }

  public String[] inputFiles_includeSubFolders() {
    return inputFiles.includeSubFolders;
  }

  public void inputFiles_includeSubFolders( String[] includeSubFoldersin ) {
    for ( int i = 0; i < includeSubFoldersin.length; i++ ) {
      inputFiles.includeSubFolders[i] = getRequiredFilesCode( includeSubFoldersin[i] );
    }
  }

  public static String getRequiredFilesCode( String tt ) {
    if ( tt == null ) {
      return RequiredFilesCode[0];
    }
    if ( tt.equals( RequiredFilesDesc[1] ) ) {
      return RequiredFilesCode[1];
    } else {
      return RequiredFilesCode[0];
    }
  }

  public FileInputList getFileInputList( VariableSpace space ) {
    return getFileInputList( DefaultBowl.getInstance(), space );
  }

  public FileInputList getFileInputList( Bowl bowl, VariableSpace space ) {
    inputFiles.normalizeAllocation( inputFiles.fileName.length );
    return FileInputList.createFileList( bowl, space, inputFiles.fileName, inputFiles.fileMask, inputFiles.excludeFileMask,
        inputFiles.fileRequired, inputFiles.includeSubFolderBoolean() );
  }

  @Override
  public List<ResourceReference> getResourceDependencies( TransMeta transMeta, StepMeta stepInfo ) {
    return inputFiles.getResourceDependencies( transMeta, stepInfo );
  }

  public abstract String getEncoding();

  public boolean isAcceptingFilenames() {
    Preconditions.checkNotNull( inputFiles );
    return inputFiles.acceptingFilenames;
  }

  public String getAcceptingStepName() {
    return inputFiles == null ? null : inputFiles.acceptingStepName;
  }

  public String getAcceptingField() {
    return inputFiles == null ? null : inputFiles.acceptingField;
  }

  @Override
  public String[] getFilePaths( final boolean showSamples ) {
    final StepMeta parentStepMeta = getParentStepMeta();
    if ( parentStepMeta != null ) {
      final TransMeta parentTransMeta = parentStepMeta.getParentTransMeta();
      if ( parentTransMeta != null ) {
        final FileInputList inputList = getFileInputList( parentTransMeta.getBowl(), parentTransMeta );
        if ( inputList != null ) {
          return inputList.getFileStrings();
        }
      }
    }
    return new String[]{};
  }
}
