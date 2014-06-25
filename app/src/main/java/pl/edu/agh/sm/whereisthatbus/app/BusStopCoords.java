package pl.edu.agh.sm.whereisthatbus.app;

/**
 * Created by piotrek on 16.06.14.
 */
public class BusStopCoords {
    final String busStopName;
    final double lat;
    final double lon;

    BusStopCoords(String busStopName, double lat, double lon) {
        this.busStopName = busStopName;
        this.lat = lat;
        this.lon = lon;
    }
}
