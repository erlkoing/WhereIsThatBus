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

    DataBaseRepository(Context context) {
        dataBase = new BaseDatabaseRepository(context);
    }
}
