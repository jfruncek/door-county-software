package org.efs.openreports.engine.javareport;

/**
 * Defines a property used by a JavaReportEngine. Report properties are config-time, and not to be
 * confused with parameters, which are run-time. The intent here is that the report will be able to
 * generate its required configuration that someone can then edit.
 * 
 * @author mconner
 */
public class PropertyDef {
    String name;
    String description;
    String example;

    public PropertyDef( String name, String description, String example ) {
        this.name = name;
        this.description = description;
        this.example = example;
    }

    public String getName() {
        return name;
    }

    public String getDescription() {
        return description;
    }

    public String getExample() {
        return example;
    }

}
