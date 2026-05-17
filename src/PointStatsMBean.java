public interface PointStatsMBean {
    int getTotalPoints();
    int getMissPoints();
    int getHitPoints();
    double getMissPercentage();
    void resetStats();
}