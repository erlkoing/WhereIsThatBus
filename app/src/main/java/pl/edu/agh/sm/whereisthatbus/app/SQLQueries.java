package pl.edu.agh.sm.whereisthatbus.app;

/**
 * Created by piotrek on 29.05.14.
 */
public class SQLQueries {
    public final static String GET_ALL_BUS_STOPS = "SELECT stop_name FROM Stops";
    public final static String GET_BUS_STOPS_COORDS = "SELECT stop_name, latitude, longitude FROM Stops";
    public final static String GET_ALL_LINES_FOR_BUS_STOP = "SELECT DISTINCT line_number FROM StopDepartures INNER JOIN Lines ON StopDepartures.line_id = Lines.line_id  WHERE stop_id=?";
    public final static String GET_BUS_STOP_ID = "SELECT stop_id FROM Stops WHERE stop_name=?";


    public final static String GET_LINE_ID = "SELECT line_id FROM Lines WHERE line_number=? AND last_stop_id=?";
    public final static String GET_LINE_ID2 = "SELECT DISTINCT Lines.line_id  FROM Lines INNER JOIN StopDepartures ON Lines.line_id = StopDepartures.line_id  WHERE line_number=? AND last_stop_id=? AND stop_id=?";


    public final static String GET_ALL_LINES = "SELECT DISTINCT line_number FROM Lines";

    public final static String GET_END_STOPS_NAMES_FOR_LINE = "SELECT DISTINCT stop_name FROM Lines INNER JOIN Stops ON Lines.last_stop_id = Stops.stop_id WHERE line_number=?";


    public final static String GET_ALL_BUS_STOPS_FOR_LINE = "SELECT DISTINCT name FROM Stops INNER JOIN StopDepartures ON Stops.id=StopDepartures.stop_id WHERE line_name=?";

}
