import javax.persistence.*;
import javax.management.Notification;
import javax.management.NotificationBroadcasterSupport;
import java.io.Serializable;
import java.util.concurrent.atomic.AtomicInteger;

@Entity
@Table(name = "points_history")
public class PointStats extends NotificationBroadcasterSupport implements PointStatsMBean, Serializable {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    // Поля для базы данных
    private double x;
    private double y;
    private double r;
    private boolean hit;
    private String executionTime;

    // Поля для JMX (бизнес-логика), Hibernate их игнорирует
    @Transient
    private AtomicInteger totalPoints = new AtomicInteger(0);
    @Transient
    private AtomicInteger missPoints = new AtomicInteger(0);
    @Transient
    private AtomicInteger consecutiveMisses = new AtomicInteger(0);
    @Transient
    private long notificationSequence = 1;

    // Пустой конструктор для Hibernate
    public PointStats() {
    }

    // Конструктор для создания записей
    public PointStats(double x, double y, double r, boolean hit, String executionTime) {
        this.x = x;
        this.y = y;
        this.r = r;
        this.hit = hit;
        this.executionTime = executionTime;
    }

    // Логика MBean и уведомлений
    public void addPoint(boolean hit) {
        totalPoints.incrementAndGet();

        if (!hit) {
            missPoints.incrementAndGet();
            int misses = consecutiveMisses.incrementAndGet();

            if (misses == 4) {
                sendNotification(new Notification(
                        "consecutive.misses",
                        this,
                        notificationSequence++,
                        System.currentTimeMillis(),
                        "4 промаха подряд! Всего промахов: " + missPoints.get()
                ));
                System.out.println("JMX: 4 промаха подряд!");
            }
        } else {
            consecutiveMisses.set(0);
        }
    }

    // Геттеры и сеттеры для Hibernate
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public double getX() { return x; }
    public void setX(double x) { this.x = x; }

    public double getY() { return y; }
    public void setY(double y) { this.y = y; }

    public double getR() { return r; }
    public void setR(double r) { this.r = r; }

    public boolean isHit() { return hit; }
    public void setHit(boolean hit) { this.hit = hit; }

    public String getExecutionTime() { return executionTime; }
    public void setExecutionTime(String executionTime) { this.executionTime = executionTime; }

    // Реализация методов PointStatsMBean
    @Override
    public int getTotalPoints() {
        return totalPoints.get();
    }

    @Override
    public int getMissPoints() {
        return missPoints.get();
    }

    @Override
    public int getHitPoints() {
        return totalPoints.get() - missPoints.get();
    }

    @Override
    public double getMissPercentage() {
        if (totalPoints.get() == 0) return 0;
        return (double) missPoints.get() / totalPoints.get() * 100;
    }

    @Override
    public void resetStats() {
        totalPoints.set(0);
        missPoints.set(0);
        consecutiveMisses.set(0);
    }
}