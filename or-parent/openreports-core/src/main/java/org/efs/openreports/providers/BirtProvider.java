/*
 * Copyright (C) 2006 by Open Source Software Solutions, LLC and Contributors
 * 
 * This program is free software; you can redistribute it and/or modify it under the terms of the GNU General Public
 * License as published by the Free Software Foundation; either version 2 of the License, or (at your option) any later
 * version.
 * 
 * This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied
 * warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU General Public License for more details.
 * 
 * You should have received a copy of the GNU General Public License along with this program; if not, write to the Free
 * Software Foundation, Inc., 59 Temple Place - Suite 330, Boston, MA 02111-1307, USA.
 * 
 * Original Author : Roberto Nibali - rnibali@pyx.ch Contributor(s) : Erik Swenson - erik@oreports.com
 */

package org.efs.openreports.providers;

import java.util.logging.Level;

import javax.servlet.ServletContext;

import org.apache.log4j.Logger;
import org.eclipse.birt.core.exception.BirtException;
import org.eclipse.birt.core.framework.IPlatformContext;
import org.eclipse.birt.core.framework.Platform;
import org.eclipse.birt.core.framework.PlatformServletContext;
import org.eclipse.birt.report.engine.api.EngineConfig;
import org.eclipse.birt.report.engine.api.HTMLActionHandler;
import org.eclipse.birt.report.engine.api.HTMLRenderOption;
import org.eclipse.birt.report.engine.api.HTMLServerImageHandler;
import org.eclipse.birt.report.engine.api.IReportEngine;
import org.eclipse.birt.report.engine.api.IReportEngineFactory;
import org.springframework.beans.factory.DisposableBean;

/**
 * Provides the init and startup of the birt engine and config.
 * 
 * mconner: The original version of this class was doing some double-duty. Handling both servlet context and file system
 * platforms. If you were to actually destroy() the engine, then restart it, it would restart as a file platform. If we
 * ever need file support, we'll put it in a different provider.
 * 
 * TODO: this (and the original version) is doing some non-spring-like things, using static instances. I've isolated it
 * to this class, at least.
 * 
 * @author Roberto Nibali
 * @author Erik Swenson
 * 
 */
public class BirtProvider implements DisposableBean {
    protected static Logger log = Logger.getLogger( BirtProvider.class );
    protected static BirtProvider instance = null;

    private IReportEngine birtEngine;

    
    /**
     * @return the provider instance, or null if none has been registered.
     */
    public static synchronized BirtProvider getInstance(  ) {
        return instance;
    }

    public BirtProvider( ServletContext servletContext, DirectoryProvider directoryProvider ) {
        synchronized( BirtProvider.class ) {
            if (getInstance() != null) {
                log.warn( "in BirtProvider ctor, Theres a provider already registered!, not registering, this provider will be null" );
            }
            
            log.info( "in BirtProvider(ServletContext servletContext)" );
            log.info( servletContext.toString() );
            IPlatformContext context = new PlatformServletContext( servletContext );
            log.info( "in BirtProvider(ServletContext servletContext), platform is " + context.getPlatform() );
            log.info( "in BirtProvider(ServletContext servletContext), before startBirtEngine " );
            startBirtEngine( context, directoryProvider );
            log.info( "in BirtProvider(ServletContext servletContext), Finished with startBirtEngine is " + context.getPlatform() );
            instance = this;
        }
    }

    public synchronized IReportEngine getBirtEngine() {
        return birtEngine;
    }

    @SuppressWarnings( "unchecked" )
    protected void startBirtEngine( IPlatformContext context, DirectoryProvider directoryProvider ) {
        log.info( "Starting BIRT Engine and OSGI Platform using: " + context.getClass().getName() );

        HTMLServerImageHandler imageHandler = new HTMLServerImageHandler();

        HTMLRenderOption emitterConfig = new HTMLRenderOption();
        emitterConfig.setActionHandler( new HTMLActionHandler() );
        emitterConfig.setImageHandler( imageHandler );

        EngineConfig config = new EngineConfig();
        config.setEngineHome( "" );
        config.setPlatformContext( context );
        config.setLogConfig( null, Level.ALL );
        config.getEmitterConfigs().put( "html", emitterConfig );
        config.setResourcePath( directoryProvider.getReportDirectory() + "birtresources" );

        try {
            Platform.startup( config );
        } catch( BirtException e ) {
            log.error( "BirtException", e );
        }

        IReportEngineFactory factory = (IReportEngineFactory) Platform
                .createFactoryObject( IReportEngineFactory.EXTENSION_REPORT_ENGINE_FACTORY );

        birtEngine = factory.createReportEngine( config );

        log.info( "BIRT Engine Started" );

        birtEngine.changeLogLevel( Level.SEVERE );
    }

    public void destroy() {
        deRegisterInstance();
    }

    protected synchronized void deRegisterInstance() {
        if( instance != this ) {
            // we didn't register, not unregistering
            return;
        }
        instance = null;
        birtEngine.destroy();
        Platform.shutdown();
        birtEngine = null;
        log.info( "BIRT Engine and OSGI Platform Shutdown" );
        instance = null;
    }
}
