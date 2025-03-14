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


package org.pentaho.di.ui.repo.extension;

import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.MessageBox;
import org.pentaho.di.core.util.Utils;
import org.pentaho.di.core.exception.KettleException;
import org.pentaho.di.core.extension.ExtensionPoint;
import org.pentaho.di.core.extension.ExtensionPointInterface;
import org.pentaho.di.core.logging.LogChannelInterface;
import org.pentaho.di.i18n.BaseMessages;
import org.pentaho.di.pan.CommandLineOption;
import org.pentaho.di.repository.RepositoryMeta;
import org.pentaho.di.ui.repo.controller.RepositoryConnectController;
import org.pentaho.di.ui.repo.dialog.RepositoryConnectionDialog;
import org.pentaho.di.ui.spoon.Spoon;

@ExtensionPoint(
  id = "RepositorySpoonStartExtensionPoint",
  extensionPointId = "SpoonStart",
  description = "Do or display login for default repository"
  )
public class RepositorySpoonStartExtensionPoint implements ExtensionPointInterface {

  private static Class<?> PKG = RepositorySpoonStartExtensionPoint.class;

  private RepositoryConnectController repositoryConnectController;

  public RepositorySpoonStartExtensionPoint() {
    this.repositoryConnectController = RepositoryConnectController.getInstance();
  }

  public static CommandLineOption getCommandLineOption( CommandLineOption[] options, String opt ) {
    for ( CommandLineOption option : options ) {
      if ( option.getOption().equals( opt ) ) {
        return option;
      }
    }
    return null;
  }

  @SuppressWarnings( "squid:S3776" )
  @Override
  public void callExtensionPoint( LogChannelInterface log, Object object ) throws KettleException {
    if ( !( object instanceof CommandLineOption[] ) ) {
      return;
    }
    CommandLineOption[] options = (CommandLineOption[]) object;
    StringBuilder optionRepname = getCommandLineOption( options, "rep" ).getArgument();
    StringBuilder optionFilename = getCommandLineOption( options, "file" ).getArgument();
    StringBuilder optionUsername = getCommandLineOption( options, "user" ).getArgument();
    StringBuilder optionPassword = getCommandLineOption( options, "pass" ).getArgument();

    if ( !Utils.isEmpty( optionRepname ) && Utils.isEmpty( optionFilename ) ) {
      RepositoryMeta repositoryMeta = repositoryConnectController.getRepositoryMetaByName( optionRepname.toString() );
      if ( repositoryMeta != null && !Utils.isEmpty( optionUsername ) && !Utils.isEmpty( optionPassword ) ) {
        repositoryConnectController
          .connectToRepository( repositoryMeta, optionUsername.toString(), optionPassword.toString() );
      } else if ( repositoryMeta != null ) {
        repositoryConnectController
          .connectToRepository( repositoryMeta, null, null );
      }
      if ( !repositoryConnectController.isConnected() ) {
        String msg = BaseMessages.getString( PKG, "Repository.NoConnected.Message" );
        log.logError( msg ); // "No repositories defined on this system."
        MessageBox mb = new MessageBox( getSpoon().getShell(), SWT.OK | SWT.ICON_ERROR );
        mb.setMessage( BaseMessages.getString( PKG, "Repository.NoConnected.Message", optionRepname
          .toString() ) );
        mb.setText( BaseMessages.getString( PKG, "Repository.NoConnected.Message.Title" ) );
        mb.open();
      }
    } else if ( Utils.isEmpty( optionFilename ) ) {
      RepositoryMeta repositoryMeta = repositoryConnectController.getDefaultRepositoryMeta();
      if ( repositoryMeta != null ) {
        if ( repositoryMeta.getId().equals( "KettleFileRepository" ) ) {
          repositoryConnectController.connectToRepository( repositoryMeta );
        } else {
          new RepositoryConnectionDialog( getSpoon().getShell() ).createDialog( repositoryMeta.getName() );
        }
      }
    }
  }

  private Spoon getSpoon() {
    return Spoon.getInstance();
  }

}
