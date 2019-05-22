package uk.oczadly.karl.csgsi.state.components;

public class Coordinate {

    private final double x, y, z;
    
    public Coordinate(double x, double y, double z) {
        this.x = x;
        this.y = y;
        this.z = z;
    }
    
    
    public double getX() {
        return x;
    }
    
    public double getY() {
        return y;
    }
    
    public double getZ() {
        return z;
    }
    
    
    
    @Override
    public int hashCode() {
        return (int)(x * y * z);
    }
    
    @Override
    public boolean equals(Object obj) {
        if (obj == this) return true;
        if (obj instanceof Coordinate) {
            Coordinate cObj = (Coordinate)obj;
            return cObj.x == this.x &&
                    cObj.y == this.y &&
                    cObj.z == this.z;
        }
        return false;
    }
    
    @Override
    public String toString() {
        return new StringBuilder("{X=")
                .append(x)
                .append(", Y=")
                .append(y)
                .append(", Z=")
                .append(z)
                .append("}")
                .toString();
    }
    
}
