/*
 * Copyright (C) 2003 Erik Swenson - erik@oreports.com
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

package org.efs.openreports.util;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

import javax.activation.DataSource;

import org.efs.openreports.engine.output.ReportEngineOutput;

public class ReportEngineOutputDataSource implements DataSource {
    ReportEngineOutput reportEngineOutput;
    String name;

    public ReportEngineOutputDataSource( ReportEngineOutput reportEngineOutput, String name ) {
        this.reportEngineOutput = reportEngineOutput;
        this.name = name;
    }

    public InputStream getInputStream() throws IOException {
        return reportEngineOutput.getContentManager().createInputStream();
    }

    public OutputStream getOutputStream() throws IOException {
        throw new IOException( "Not supported." );
    }

    public String getContentType() {
        return reportEngineOutput.getContentType();
    }

    public String getName() {
        return name;
    }

}
