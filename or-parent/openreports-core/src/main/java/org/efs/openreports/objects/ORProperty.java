/*
 * Copyright (C) 2005 Erik Swenson - erik@oreports.com
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

package org.efs.openreports.objects;

import java.io.Serializable;

public class ORProperty implements Serializable {
    public static final String BASE_DIRECTORY = "base.directory";
    public static final String DATE_FORMAT = "date.format";
    /** non-persisted */
    public static final String DEVELOPMENT_EMAIL = "development.email";
    /** non-persisted: */
    public static final String DEVELOPMENT_ENABLE_COMMAND_DELIVERY = "development.enable.command.delivery";
    /** non-persisted */
    public static final String DEVELOPMENT_FILE_ROOT = "development.file.root";
    /** non-persisted */
    public static final String IS_PROD_SERVER = "production.server";
    public static final String MAIL_AUTH_PASSWORD = "mail.auth.password";
    public static final String MAIL_AUTH_USER = "mail.auth.user";
    /** email address to use for from field when generating email */
    public static final String MAIL_REPLY_TO = "mail.replyto";
    public static final String MAIL_SMTP_AUTH = "mail.smtp.auth";
    public static final String MAIL_SMTP_HOST = "mail.smtp.host";
    
    public static final String RUN_STATUS_INACTIVITY_LOGGING = "runstatus.inactivity.logging";
    public static final String RUN_STATUS_INACTIVITY_WARNING = "runstatus.inactivity.warning";
    public static final String RUN_STATUS_INACTIVITY_TIMEOUT = "runstatus.inactivity.timeout";
    
    public static final String QUERYREPORT_MAXROWS = "queryreport.maxrows";
    public static final String QUERYREPORT_CACHE_BLOCKSIZE = "queryreport.cache.blocksize";
    public static final String QUERYREPORT_MAXROWS_SORTABLE = "queryreport.maxrows.sortable";
    public static final String REPORT_GENERATION_DIRECTORY = "report.generation.directory";

    public static final String TEMP_DIRECTORY = "temp.directory";

    public static final String XMLA_CATALOG = "xmla.catalog";
    public static final String XMLA_DATASOURCE = "xmla.datasource";

    public static final String XMLA_URL = "xmla.uri";
    
    public static final int DEFAULT_QUERYREPORT_CACHE_BLOCKSIZE = 8192; 
    public static final int DEFAULT_QUERYREPORT_MAX_ROWS_SORTABLE = 100000;

    private static final long serialVersionUID = 806285455871073093L;
    public static final int DEFAULT_MAX_ROWS = 500000;

    private Integer id;
    private String key;
    private String value;

    public ORProperty() {
    }

    public Integer getId() {
        return id;
    }

    public String getKey() {
        return key;
    }

    public String getValue() {
        return value;
    }

    public void setId( Integer id ) {
        this.id = id;
    }

    public void setKey( String key ) {
        this.key = key;
    }

    public void setValue( String value ) {
        this.value = value;
    }

}