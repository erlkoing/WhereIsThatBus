package pl.edu.agh.sm.whereisthatbus.app;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.util.Log;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Vector;

public class DataBaseRepository {
    BaseDatabaseRepository dataBase;

    /* zwraca liste nazw wszystkich przystanków */
    public List<String> getAllBusStopsNames() {
        dataBase.createDatabase();
        dataBase.openDatabase(SQLiteDatabase.OPEN_READONLY);
        Vector<HashMap<String, String>> queryResult = dataBase.executeQuery(SQLQueries.GET_ALL_BUS_STOPS, null);
        dataBase.closeDatabase();
        List<String> busStopsName = new ArrayList<String>();

        for (HashMap<String, String> element: queryResult) {
            busStopsName.add(element.get("stop_name"));
        }

        return busStopsName;
    }

    /* zwraca numery wszystkich linii */
    public List<String> getAllLineNames() {
        dataBase.createDatabase();
        dataBase.openDatabase(SQLiteDatabase.OPEN_READONLY);
        Vector<HashMap<String, String>> queryResult = dataBase.executeQuery(SQLQueries.GET_ALL_LINES, null);
        dataBase.closeDatabase();
        List<String> busStopsName = new ArrayList<String>();

        for (HashMap<String, String> element: queryResult) {
            busStopsName.add(element.get("line_number"));
        }

        return busStopsName;
    }

    /* zwraca numery linii dla danego przystanku */
    public List<String> getAllLinesForBusStop(int busStopId) {
        dataBase.createDatabase();
        dataBase.openDatabase(SQLiteDatabase.OPEN_READONLY);
        Vector<HashMap<String, String>> queryResult = dataBase.executeQuery(SQLQueries.GET_ALL_LINES_FOR_BUS_STOP, new String[] { Integer.toString(busStopId) });
        dataBase.closeDatabase();
        List<String> linesForBusStop = new ArrayList<String>();

        for (HashMap<String, String> element: queryResult) {
            linesForBusStop.add(element.get("line_number"));
        }

        return linesForBusStop;
    }

    public List<String> getEndStopIdsForLine(String line) {
        dataBase.createDatabase();
        dataBase.openDatabase(SQLiteDatabase.OPEN_READONLY);
        Vector<HashMap<String, String>> queryResult = dataBase.executeQuery(SQLQueries.GET_END_STOPS_NAMES_FOR_LINE, new String[] { line });
        dataBase.closeDatabase();
        List<String> endStopsForLine = new ArrayList<String>();

        for (HashMap<String, String> element: queryResult) {
            endStopsForLine.add(element.get("stop_name"));
        }

        return endStopsForLine;
    }

    public int getBusStopId(String busStopName) {
        dataBase.createDatabase();
        dataBase.openDatabase(SQLiteDatabase.OPEN_READONLY);
        String parameters[] = { busStopName };
        Vector<HashMap<String, String>> queryResult = dataBase.executeQuery(SQLQueries.GET_BUS_STOP_ID, parameters);
        dataBase.closeDatabase();

        if (queryResult.size() == 1) {
            return Integer.parseInt(queryResult.get(0).get("stop_id"));
        } else {
            return -1;
        }
    }

    public String getBusStopName(int busStopId) {
        dataBase.createDatabase();
        dataBase.openDatabase(SQLiteDatabase.OPEN_READONLY);
        String parameters[] = { Integer.toString(busStopId) };
        Vector<HashMap<String, String>> queryResult = dataBase.executeQuery(SQLQueries.GET_BUS_STOP_ID, parameters);
        dataBase.closeDatabase();

        if (queryResult.size() == 1) {
            return queryResult.get(0).get("stop_name");
        } else {
            return "";
        }
    }

    public String getLineId(String lineName, int lastBusStopId, int stopId) {
        String lineId = "";

        dataBase.createDatabase();
        dataBase.openDatabase(SQLiteDatabase.OPEN_READONLY);

        // pierwsze podejscie
        String parameters[] = { lineName, Integer.toString(lastBusStopId) };
        Vector<HashMap<String, String>> queryResult = dataBase.executeQuery(SQLQueries.GET_LINE_ID, parameters);

        if (queryResult.size() == 1) {

            lineId = queryResult.get(0).get("line_id");
        } else {
            //drugie podejscie
            parameters[2] += Integer.toString(stopId);
            queryResult = dataBase.executeQuery(SQLQueries.GET_LINE_ID2, parameters);
            if (queryResult.size() > 0) {
                lineId = queryResult.get(0).get("line_id");
            }
        }

        dataBase.closeDatabase();

        return lineId;
    }

    public List<BusStopCoords> getBusStopsCoords() {
        dataBase.createDatabase();
        dataBase.openDatabase(SQLiteDatabase.OPEN_READONLY);
        Vector<HashMap<String, String>> queryResult = dataBase.executeQuery(SQLQueries.GET_BUS_STOPS_COORDS, null);
        dataBase.closeDatabase();

        List<BusStopCoords> busStopsCoords = new ArrayList<BusStopCoords>();
        for (HashMap<String, String> element: queryResult) {
            if (element.get("longitude").isEmpty() || element.get("latitude").isEmpty())
                continue;

            String busStopName = element.get("stop_name");
            double lat = Double.parseDouble(element.get("latitude"));
            double lon = Double.parseDouble(element.get("longitude"));

            busStopsCoords.add(new BusStopCoords(busStopName, lat, lon));
        }

        return busStopsCoords;
    }

    public String getNearestConnectionId(int time, String lineId, int stopId, String dayType) {
        dataBase.createDatabase();
        dataBase.openDatabase(SQLiteDatabase.OPEN_READONLY);

        String parameters[] = { Integer.toString(time), Integer.toString(time), lineId, Integer.toString(stopId), dayType };
        Vector<HashMap<String, String>> queryResult = dataBase.executeQuery(SQLQueries.GET_NEAREST_CONNECTION_ID_FOR_LINE_ID_ON_STOP_ID, parameters);
        dataBase.closeDatabase();

        String connectionId = "";

        if (queryResult.size() == 1) {
            connectionId = queryResult.get(0).get("connection_id");
        }

        return connectionId;
    }

    public int getStopPlacement(String lineId, int stopId) {
        dataBase.createDatabase();
        dataBase.openDatabase(SQLiteDatabase.OPEN_READONLY);

        String parameters[] = { lineId, Integer.toString(stopId) };
        Vector<HashMap<String, String>> queryResult = dataBase.executeQuery(SQLQueries.GET_STOP_ID_PLACEMENT_IN_LINE_ID, parameters);
        dataBase.closeDatabase();

        int stopIdPlacement = -1;

        if (queryResult.size() == 1) {
            stopIdPlacement = Integer.parseInt(queryResult.get(0).get("line_stop_no"));
        }

        return stopIdPlacement;
    }

    public int getTimeBeenFirstAndCurrentBusStopForConnection(String connectionId, int currentBusStopId) {
        dataBase.createDatabase();
        dataBase.openDatabase(SQLiteDatabase.OPEN_READONLY);

        String parameters[] = { connectionId, Integer.toString(currentBusStopId) };
        Vector<HashMap<String, String>> queryResult = dataBase.executeQuery(SQLQueries.GET_CONNECTION_ID_TIMES_BETWEEN_FIRST_AND_CURRENT_STOP, parameters);
        dataBase.closeDatabase();

        int timeBetweenBusStops = -1;

        if (queryResult.size() == 2) {
            timeBetweenBusStops = Math.abs(Integer.parseInt(queryResult.get(0).get("time")) - Integer.parseInt(queryResult.get(1).get("time")));
        }

        return timeBetweenBusStops;
    }

    public int getTimeBeetweenBusStops(String connectionId, int busStopId1, int busStopId2) {
        dataBase.createDatabase();
        dataBase.openDatabase(SQLiteDatabase.OPEN_READONLY);

        String parameters[] = { connectionId, Integer.toString(busStopId1), Integer.toString(busStopId2) };
        Vector<HashMap<String, String>> queryResult = dataBase.executeQuery(SQLQueries.GET_CONNECTION_ID_TIMES_BETWEEN_BUS_STOPS, parameters);
        dataBase.closeDatabase();

        int timeBetweenBusStops = -1;

        if (queryResult.size() == 2) {
            timeBetweenBusStops = Math.abs(Integer.parseInt(queryResult.get(0).get("time")) - Integer.parseInt(queryResult.get(1).get("time")));
        }

        return timeBetweenBusStops;
    }

    public int getDeltaTimeBetweenNearestConnections(int time, String lineId, int stopId, String dayType) {
        dataBase.createDatabase();
        dataBase.openDatabase(SQLiteDatabase.OPEN_READONLY);

        String parameters[] = { Integer.toString(time), Integer.toString(time), lineId, Integer.toString(stopId), dayType };
        Vector<HashMap<String, String>> queryResult = dataBase.executeQuery(SQLQueries.GET_TWO_NEAREST_FUTURE_CONNECTIONS_TIME, parameters);
        dataBase.closeDatabase();

        int deltaBetweenConnections = 5;

        if (queryResult.size() == 2) {
            deltaBetweenConnections = Math.abs(Integer.parseInt(queryResult.get(0).get("time")) - Integer.parseInt(queryResult.get(1).get("time")));
            if (deltaBetweenConnections > 4) {
                deltaBetweenConnections /= 2;
            }
        }

        return deltaBetweenConnections;
    }

    public int getNearestConnectionTimeArrival(int time, String lineId, int stopId, String dayType) {
        dataBase.createDatabase();
        dataBase.openDatabase(SQLiteDatabase.OPEN_READONLY);

        String parameters[] = { Integer.toString(time), lineId, Integer.toString(stopId), dayType };
        Vector<HashMap<String, String>> queryResult = dataBase.executeQuery(SQLQueries.GET_NEAREST_CONNECTION_TIME, parameters);
        dataBase.closeDatabase();

        int minutes = -1;

        if (queryResult.size() == 1) {
            minutes = Integer.parseInt(queryResult.get(0).get("minumum"));
        }

        return minutes;
    }


    DataBaseRepository(Context context) {
        dataBase = new BaseDatabaseRepository(context);
    }
}
