public interface AreaCalculatorMBean {
    double getCurrentArea();
    double getSquareArea();
    double getTriangleArea();
    double getCircleQuarterArea();
    void setCurrentR(double r);
    double getCurrentR();
}