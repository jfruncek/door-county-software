package patterns.factory;

public class Crust {

    private String thickness;

    Crust(String name) {
        this.thickness = name;
    }
    
    public String getThickness() {
        return thickness;
    }
}
