public class AreaCalculator implements AreaCalculatorMBean {

    private double currentR = 2.0;

    @Override
    public double getCurrentArea() {
        return getSquareArea() + getTriangleArea() + getCircleQuarterArea();
    }

    @Override
    public double getSquareArea() {
        return currentR * (currentR / 2);
    }

    @Override
    public double getTriangleArea() {
        return (currentR * (currentR / 2)) / 2;
    }

    @Override
    public double getCircleQuarterArea() {
        return (Math.PI * (currentR/2) * (currentR/2)) / 4;
    }

    @Override
    public void setCurrentR(double r) {
        this.currentR = r;
    }

    @Override
    public double getCurrentR() {
        return currentR;
    }
    // Добавляем только этот метод для теста и Main.java
    public boolean checkHit(double x, double y, double r) {
        if (x >= 0 && y >= 0) return x <= r && y <= r / 2;
        if (x <= 0 && y >= 0) return false;
        if (x <= 0 && y <= 0) return x >= -r && y >= -r / 2 && y >= (0.5) * x - r / 2;
        if (x >= 0 && y <= 0) return (x * x + y * y) <= (r / 2) * (r / 2);
        return false;
    }
}