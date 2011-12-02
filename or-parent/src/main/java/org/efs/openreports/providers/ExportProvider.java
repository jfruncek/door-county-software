package org.efs.openreports.providers;

import java.io.OutputStream;
import java.util.Iterator;

import org.apache.commons.beanutils.DynaBean;
import org.efs.openreports.util.DisplayProperty;

public interface ExportProvider {
    /**
     * 
     * @param data
     * @param properties
     * @param output
     * @param exportId  Used to identify the export for logging 
     * @throws ProviderException
     */
    public void export(Iterator<DynaBean> data, DisplayProperty[] properties, OutputStream output, String exportId) throws ProviderException;

}
