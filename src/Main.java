import com.sun.net.httpserver.HttpServer;
import com.sun.net.httpserver.HttpHandler;
import com.sun.net.httpserver.HttpExchange;

import javax.management.MBeanServer;
import javax.management.ObjectName;
import java.lang.management.ManagementFactory;
import java.io.*;
import java.net.InetSocketAddress;
import java.nio.charset.StandardCharsets;

public class Main {

    private static PointStats pointStats = new PointStats();
    private static AreaCalculator areaCalculator = new AreaCalculator();

    public static void main(String[] args) throws Exception {
        MBeanServer mbs = ManagementFactory.getPlatformMBeanServer();

        ObjectName pointStatsName = new ObjectName("ru.itmo.web.lab4:type=PointStats");
        mbs.registerMBean(pointStats, pointStatsName);
        System.out.println("MBean PointStats зарегистрирован");

        ObjectName areaName = new ObjectName("ru.itmo.web.lab4:type=AreaCalculator");
        mbs.registerMBean(areaCalculator, areaName);
        System.out.println("MBean AreaCalculator зарегистрирован");

        HttpServer server = HttpServer.create(new InetSocketAddress(8080), 0);
        server.createContext("/", new StaticHandler());
        server.createContext("/api/check", new CheckHandler());
        server.createContext("/api/stats", new StatsHandler());
        server.setExecutor(null);
        server.start();

        System.out.println("Сервер запущен на http://localhost:8080/");
        System.out.println("ОС: " + System.getProperty("os.name") + " " + System.getProperty("os.version"));
        System.out.println("Java: " + System.getProperty("java.version"));
    }

    static class CheckHandler implements HttpHandler {
        @Override
        public void handle(HttpExchange exchange) throws IOException {
            if ("POST".equals(exchange.getRequestMethod())) {
                String body = new String(exchange.getRequestBody().readAllBytes(), StandardCharsets.UTF_8);

                double x = 0, y = 0, r = 2;
                for (String param : body.split("&")) {
                    String[] parts = param.split("=");
                    if (parts.length == 2) {
                        switch (parts[0]) {
                            case "x": x = Double.parseDouble(parts[1]); break;
                            case "y": y = Double.parseDouble(parts[1]); break;
                            case "r": r = Double.parseDouble(parts[1]); break;
                        }
                    }
                }

                // Вызываем метод проверки из нашего класса AreaCalculator
                boolean hit = areaCalculator.checkHit(x, y, r);
                pointStats.addPoint(hit);
                areaCalculator.setCurrentR(r);

                System.out.println("Точка: (" + x + ", " + y + ", R=" + r + ") - " + (hit ? "ПОПАДАНИЕ" : "ПРОМАХ"));

                String response = "{\"x\":" + x + ",\"y\":" + y + ",\"r\":" + r + ",\"hit\":" + hit + "}";

                exchange.getResponseHeaders().set("Content-Type", "application/json");
                exchange.sendResponseHeaders(200, response.length());
                exchange.getResponseBody().write(response.getBytes());
            }
            exchange.close();
        }
    }

    static class StatsHandler implements HttpHandler {
        @Override
        public void handle(HttpExchange exchange) throws IOException {
            String response = "{" +
                    "\"totalPoints\":" + pointStats.getTotalPoints() + "," +
                    "\"hitPoints\":" + pointStats.getHitPoints() + "," +
                    "\"missPoints\":" + pointStats.getMissPoints() + "," +
                    "\"missPercentage\":" + pointStats.getMissPercentage() + "," +
                    "\"area\":" + areaCalculator.getCurrentArea() + "," +
                    "\"currentR\":" + areaCalculator.getCurrentR() +
                    "}";

            exchange.getResponseHeaders().set("Content-Type", "application/json");
            exchange.sendResponseHeaders(200, response.length());
            exchange.getResponseBody().write(response.getBytes());
            exchange.close();
        }
    }

    static class StaticHandler implements HttpHandler {
        @Override
        public void handle(HttpExchange exchange) throws IOException {
            String path = exchange.getRequestURI().getPath();
            if (path.equals("/")) path = "/main.html";

            String filePath = "src" + path;
            File file = new File(filePath);

            if (file.exists() && !file.isDirectory()) {
                exchange.sendResponseHeaders(200, file.length());
                try (OutputStream os = exchange.getResponseBody();
                     FileInputStream fis = new FileInputStream(file)) {
                    byte[] buffer = new byte[4096];
                    int bytesRead;
                    while ((bytesRead = fis.read(buffer)) != -1) {
                        os.write(buffer, 0, bytesRead);
                    }
                }
            } else {
                String response = "404 Not Found";
                exchange.sendResponseHeaders(404, response.length());
                exchange.getResponseBody().write(response.getBytes());
            }
            exchange.close();
        }
    }
}