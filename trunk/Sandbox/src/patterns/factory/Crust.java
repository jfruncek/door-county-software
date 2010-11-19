package patterns.factory;

public class Crust {

    private String thickness;

    public Crust(String name) {
        this.thickness = name;
    }
    
    public String getThickness() {
        return thickness;
    }
}
