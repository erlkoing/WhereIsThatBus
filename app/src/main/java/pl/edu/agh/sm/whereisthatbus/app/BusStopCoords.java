package pl.edu.agh.sm.whereisthatbus.app;

/**
 * Klasa pomocnicza majaca za zadanie przechowywac wspolrzedne przystanku
 */
public class BusStopCoords {
    private final String busStopName;
    private final double lat;
    private final double lon;

    BusStopCoords(String busStopName, double lat, double lon) {
        this.busStopName = busStopName;
        this.lat = lat;
        this.lon = lon;
    }

    public String getBusStopName() {
        return busStopName;
    }

    public double getLat() {
        return lat;
    }

    public double getLon() {
        return lon;
    }
}
