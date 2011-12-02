/*
 * org.efs.openreports.engine.ReportPostProcessor.java
 * Created on Mar 4, 2011 by jfruncek
 * Copyright 2011, TeraMedica Healthcare Technology, Inc. All Rights Reserved.
 */
package org.efs.openreports.engine;

import java.util.Map;

/**
 * TODO add  
 * 
 * @author jfruncek
 */
public interface ReportPostProcessor {

    byte[] process(byte[] byteArray, Map<String, Object> parameters);

}
