package pl.edu.agh.sm.whereisthatbus.app;

/**
 * Klasa pomocnicza majaca za zadanie przechowywac wspolrzedne przystanku oraz jego nazwe.
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

    /**
     * Funkcja dajaca dostep do nazy przystanku.
     *
     * @return nazwa przystanku.
     */
    public String getBusStopName() {
        return busStopName;
    }

    /**
     * Funkcja dajaca dostep do zapisanej szerokosci geograficznej.
     * @return szerokosc geograficzna.
     */
    public double getLat() {
        return lat;
    }

    /**
     * Funkcja dajaca dostep do zapisanej dlugosci geograficznej.
     * @return dlugosc geograficzna.
     */
    public double getLon() {
        return lon;
    }
}
