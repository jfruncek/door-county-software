package org.efs.openreports.util;

import java.util.Date;


/**
 * Supports CtrlUser and CtrlDate attributes.
 * @author mconner
 */
public interface CtrlDataObject {
    Date getCtrlDate();
    String getCtrlUser();

    void setCtrlDate(Date modifyDate);
    void setCtrlUser(String user);
}
