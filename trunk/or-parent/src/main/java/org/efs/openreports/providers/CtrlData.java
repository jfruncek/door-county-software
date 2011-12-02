package org.efs.openreports.providers;

import java.util.Date;

import org.efs.openreports.util.CtrlDataObject;

/**
 * Stores Ctrl Data for some object.  On a few objects, we want the user and date of 
 * modification set before saving.

 * @author mconner
 */
public class CtrlData {
    String ctrlUser;
    Date ctrlDate;

    public CtrlData( CtrlDataObject object ) {
        this.ctrlDate = object.getCtrlDate();
        this.ctrlUser = object.getCtrlUser();
    }

    public void apply( CtrlDataObject object ) {
        object.setCtrlDate( ctrlDate );
        object.setCtrlUser( ctrlUser );
    }

    public String getCtrlUser() {
        return ctrlUser;
    }

    public Date getCtrlDate() {
        return ctrlDate;
    }

}
