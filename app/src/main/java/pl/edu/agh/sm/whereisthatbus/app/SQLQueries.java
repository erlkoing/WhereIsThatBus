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

    public final static String GET_NEAREST_CONNECTION_ID_FOR_LINE_ID_ON_STOP_ID = "SELECT connection_id , MIN(ABS(time - ?), ABS(1440 - (time - ?))) AS minumum FROM StopDepartures WHERE line_id=? AND stop_id=? AND day_type=? ORDER BY minumum LIMIT 1";

    public final static String GET_STOP_ID_PLACEMENT_IN_LINE_ID = "SELECT DISTINCT line_stop_no FROM StopDepartures WHERE line_id=? and stop_id=?";

    public final static String GET_CONNECTION_ID_TIMES_BETWEEN_FIRST_AND_CURRENT_STOP = "SELECT time FROM StopDepartures WHERE connection_id=? AND ( stop_id=? OR line_stop_no=0)";

    public final static String GET_CONNECTION_ID_TIMES_BETWEEN_BUS_STOPS = "SELECT time FROM StopDepartures WHERE connection_id=? AND ( stop_id=? OR stop_id=?)";

    public final static String GET_TWO_NEAREST_FUTURE_CONNECTIONS_TIME = "SELECT time, MIN((time - ?), (1440 - (time - ?))) AS minumum FROM StopDepartures WHERE line_id=? AND stop_id=? AND day_type=?  and minumum > 0 ORDER BY minumum LIMIT 2";

    public final static String GET_NEAREST_CONNECTION_TIME = "SELECT (time - ?) AS minumum FROM StopDepartures WHERE line_id=? AND stop_id=? AND day_type=? AND minumum > 0 ORDER BY minumum LIMIT 1";
}
