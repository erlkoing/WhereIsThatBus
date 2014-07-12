package pl.edu.agh.sm.whereisthatbus.app;

/**
 * Created by piotrek on 29.05.14.
 */
public class SQLQueries {
    public final static String GET_ALL_BUS_STOPS = "SELECT name FROM Stops";
    public final static String GET_BUS_STOP_ID = "SELECT id FROM Stops WHERE stop_name=?";
    public final static String GET_BUS_STOPS_COORDS = "SELECT stop_name, latitude, longitude FROM Stops";
    public final static String GET_LINE_ID = "SELECT line_id FROM Lines WHERE line_number=? AND last_stop_id=?";


    public final static String GET_ALL_LINES = "SELECT DISTINCT line_number FROM Lines";

    public final static String GET_END_STOPS_NAMES_FOR_LINE = "SELECT DISTINCT last_stop_name FROM Lines WHERE line_number=?";


    public final static String GET_ALL_BUS_STOPS_FOR_LINE = "SELECT DISTINCT name FROM Stops INNER JOIN StopDepartures ON Stops.id=StopDepartures.stop_id WHERE line_name=?";
    public final static String GET_ALL_LINES_FOR_BUS_STOP = "SELECT DISTINCT line_name FROM StopDepartures WHERE stop_id=?";

}
