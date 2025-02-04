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


package org.pentaho.di.ui.spoon.delegates;

import java.util.List;

import org.eclipse.swt.SWT;
import org.pentaho.di.cluster.SlaveServer;
import org.pentaho.di.core.Const;
import org.pentaho.di.core.exception.KettleException;
import org.pentaho.di.i18n.BaseMessages;
import org.pentaho.di.repository.Repository;
import org.pentaho.di.trans.HasSlaveServersInterface;
import org.pentaho.di.ui.cluster.dialog.SlaveServerDialog;
import org.pentaho.di.ui.core.PropsUI;
import org.pentaho.di.ui.core.dialog.ErrorDialog;
import org.pentaho.di.ui.spoon.Spoon;
import org.pentaho.di.ui.spoon.SpoonSlave;
import org.pentaho.di.ui.spoon.TabMapEntry;
import org.pentaho.di.ui.spoon.TabMapEntry.ObjectType;
import org.pentaho.di.ui.spoon.tree.provider.SlavesFolderProvider;
import org.pentaho.xul.swt.tab.TabItem;
import org.pentaho.xul.swt.tab.TabSet;

public class SpoonSlaveDelegate extends SpoonSharedObjectDelegate {
  private static Class<?> PKG = Spoon.class; // for i18n purposes, needed by Translator2!!

  public SpoonSlaveDelegate( Spoon spoon ) {
    super( spoon );
  }

  public void addSpoonSlave( SlaveServer slaveServer ) {
    TabSet tabfolder = spoon.tabfolder;

    // See if there is a SpoonSlave for this slaveServer...
    String tabName = spoon.delegates.tabs.makeSlaveTabName( slaveServer );
    TabMapEntry tabMapEntry = spoon.delegates.tabs.findTabMapEntry( tabName, ObjectType.SLAVE_SERVER );
    if ( tabMapEntry == null ) {
      SpoonSlave spoonSlave = new SpoonSlave( tabfolder.getSwtTabset(), SWT.NONE, spoon, slaveServer );
      PropsUI props = PropsUI.getInstance();
      TabItem tabItem = new TabItem( tabfolder, tabName, tabName, props.getSashWeights() );
      tabItem.setToolTipText( "Status of slave server : "
        + slaveServer.getName() + " : " + slaveServer.getServerAndPort() );
      tabItem.setControl( spoonSlave );

      tabMapEntry = new TabMapEntry( tabItem, null, tabName, null, null, spoonSlave, ObjectType.SLAVE_SERVER );
      spoon.delegates.tabs.addTab( tabMapEntry );
    }
    int idx = tabfolder.indexOf( tabMapEntry.getTabItem() );
    tabfolder.setSelected( idx );
  }

  public void delSlaveServer( HasSlaveServersInterface hasSlaveServersInterface, SlaveServer slaveServer )
    throws KettleException {

    Repository rep = spoon.getRepository();
    if ( rep != null && slaveServer.getObjectId() != null ) {
      // remove the slave server from the repository too...
      rep.deleteSlave( slaveServer.getObjectId() );
      if ( sharedObjectSyncUtil != null ) {
        sharedObjectSyncUtil.deleteSlaveServer( slaveServer );
      }
    }
    hasSlaveServersInterface.getSlaveServers().remove( slaveServer );
    refreshTree();

  }


  public void newSlaveServer( HasSlaveServersInterface hasSlaveServersInterface ) {
    SlaveServer slaveServer = new SlaveServer();

    SlaveServerDialog dialog =
        new SlaveServerDialog( spoon.getShell(), slaveServer, hasSlaveServersInterface.getSlaveServers() );
    if ( dialog.open() ) {
      slaveServer.verifyAndModifySlaveServerName( hasSlaveServersInterface.getSlaveServers(), null );
      hasSlaveServersInterface.getSlaveServers().add( slaveServer );
      if ( spoon.rep != null ) {
        try {
          if ( !spoon.rep.getSecurityProvider().isReadOnly() ) {
            spoon.rep.save( slaveServer, Const.VERSION_COMMENT_INITIAL_VERSION, null );
            // repository objects are "global"
            if ( sharedObjectSyncUtil != null ) {
              sharedObjectSyncUtil.reloadJobRepositoryObjects( false );
              sharedObjectSyncUtil.reloadTransformationRepositoryObjects( false );
            }
          } else {
            showSaveErrorDialog( slaveServer,
                new KettleException( BaseMessages.getString( PKG, "Spoon.Dialog.Exception.ReadOnlyRepositoryUser" ) ) );
          }
        } catch ( KettleException e ) {
          showSaveErrorDialog( slaveServer, e );
        }
      }

      refreshTree();
    }
  }

  public boolean edit( SlaveServer slaveServer, List<SlaveServer> existingServers ) {
    String originalName = slaveServer.getName();
    SlaveServerDialog dialog = new SlaveServerDialog( spoon.getShell(), slaveServer, existingServers );
    if ( dialog.open() ) {
      if ( spoon.rep != null ) {
        try {
          saveSharedObjectToRepository( slaveServer, null );
        } catch ( KettleException e ) {
          showSaveErrorDialog( slaveServer, e );
        }
      }
      if ( sharedObjectSyncUtil != null ) {
        sharedObjectSyncUtil.synchronizeSlaveServers( slaveServer, originalName );
      }
      refreshTree();
      spoon.refreshGraph();
      return true;
    }
    return false;
  }

  private void showSaveErrorDialog( SlaveServer slaveServer, KettleException e ) {
    new ErrorDialog(
        spoon.getShell(), BaseMessages.getString( PKG, "Spoon.Dialog.ErrorSavingSlave.Title" ),
        BaseMessages.getString( PKG, "Spoon.Dialog.ErrorSavingSlave.Message", slaveServer.getName() ), e );
  }

  private void refreshTree() {
    spoon.refreshTree( SlavesFolderProvider.STRING_SLAVES );
  }
}
