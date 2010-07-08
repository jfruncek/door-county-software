/*
 * Copyright (C) 2002 Erik Swenson - erik@oreports.com
 * 
 * This program is free software; you can redistribute it and/or modify it under the terms of the
 * GNU General Public License as published by the Free Software Foundation; either version 2 of the
 * License, or (at your option) any later version.
 * 
 * This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without
 * even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
 * General Public License for more details.
 * 
 * You should have received a copy of the GNU General Public License along with this program; if
 * not, write to the Free Software Foundation, Inc., 59 Temple Place - Suite 330, Boston, MA
 * 02111-1307, USA.
 */

package org.efs.openreports.actions;

import java.security.Principal;
import java.util.Map;

import org.apache.struts2.ServletActionContext;
import org.apache.struts2.interceptor.SessionAware;
import org.efs.openreports.ORStatics;
import org.efs.openreports.actions.admin.ActionHelper;
import org.efs.openreports.objects.ReportUser;
import org.efs.openreports.providers.UserProvider;

import com.opensymphony.xwork2.ActionContext;
import com.opensymphony.xwork2.ActionSupport;

public class LoginAction extends ActionSupport implements SessionAware {
    private static final long serialVersionUID = 1L;

    private Map<Object, Object> session;

    protected UserProvider userProvider;

    @Override
    public String execute() {
        Principal userPrincipal = ServletActionContext.getRequest().getUserPrincipal();

        if( userPrincipal == null ) {
            addActionError( "no access without authentication!" );
            return ERROR;
        }

        try {
            String userName = userPrincipal.getName();
            ReportUser user = userProvider.getUser( userName.toLowerCase() );
            if( user != null ) {
                session.put( "user", user );
                ActionContext.getContext().setLocale( user.getLocale() );

                if( user.isDashboardUser() && ( user.getDefaultReport() != null || user.getAlerts().size() > 0 ) ) {
                    return ORStatics.DASHBOARD_ACTION;
                }
                ServletActionContext.getRequest().getSession().removeAttribute( "InvalidUser" );

                return SUCCESS;
            } else {
                addActionError( String.format( "No user named \'%s\' is currently configured", userName ) );
                ServletActionContext.getRequest().getSession().setAttribute( "InvalidUser", userName );
                return INPUT;
            }

        } catch( Exception e ) {
            e.printStackTrace();
            ActionHelper.addExceptionAsError( this, e );
            return ERROR;
        }

    }

    @SuppressWarnings( "unchecked" )
    public void setSession( Map session ) {
        this.session = session;
    }

    public void setUserProvider( UserProvider userProvider ) {
        this.userProvider = userProvider;
    }

}