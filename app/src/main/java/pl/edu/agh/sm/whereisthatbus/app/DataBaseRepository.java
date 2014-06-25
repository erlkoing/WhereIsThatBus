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

    public List<String> getAllBusStopsNames() {
        dataBase.createDatabase();
        dataBase.openDatabase(SQLiteDatabase.OPEN_READONLY);
        Vector<HashMap<String, String>> queryResult = dataBase.executeQuery(SQLQueries.GET_ALL_BUS_STOPS, null);
        dataBase.closeDatabase();
        List<String> busStopsName = new ArrayList<String>();

        for (HashMap<String, String> element: queryResult) {
            busStopsName.add(element.get("name"));
        }

        return busStopsName;
    }

    public List<String> getAllLineNames() {
        dataBase.createDatabase();
        dataBase.openDatabase(SQLiteDatabase.OPEN_READONLY);
        Vector<HashMap<String, String>> queryResult = dataBase.executeQuery(SQLQueries.GET_ALL_LINES, null);
        dataBase.closeDatabase();
        List<String> busStopsName = new ArrayList<String>();

        for (HashMap<String, String> element: queryResult) {
            busStopsName.add(element.get("line_name"));
        }

        return busStopsName;
    }

    public List<String> getAllLinesForBusStop(String busStopId) {
        dataBase.createDatabase();
        dataBase.openDatabase(SQLiteDatabase.OPEN_READONLY);
        Vector<HashMap<String, String>> queryResult = dataBase.executeQuery(SQLQueries.GET_ALL_LINES_FOR_BUS_STOP, new String[] { busStopId });
        dataBase.closeDatabase();
        List<String> linesForBusStop = new ArrayList<String>();

        for (HashMap<String, String> element: queryResult) {
            linesForBusStop.add(element.get("line_name"));
        }

        return linesForBusStop;
    }

    public List<String> getEndStopsForLine(String line) {
        dataBase.createDatabase();
        dataBase.openDatabase(SQLiteDatabase.OPEN_READONLY);
        Vector<HashMap<String, String>> queryResult = dataBase.executeQuery(SQLQueries.GET_END_STOPS_FOR_LINE, new String[] { line });
        dataBase.closeDatabase();
        List<String> endStopsForLine = new ArrayList<String>();

        for (HashMap<String, String> element: queryResult) {
            endStopsForLine.add(element.get("last_stop_name"));
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
            return Integer.parseInt(queryResult.get(0).get("id"));
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
            return queryResult.get(0).get("name");
        } else {
            return "";
        }
    }

    public String getLineId(int lineName, int lastBusStopId) {
        dataBase.createDatabase();
        dataBase.openDatabase(SQLiteDatabase.OPEN_READONLY);
        String parameters[] = { Integer.toString(lineName), Integer.toString(lastBusStopId) };
        Vector<HashMap<String, String>> queryResult = dataBase.executeQuery(SQLQueries.GET_LINE_ID, parameters);
        dataBase.closeDatabase();

        if (queryResult.size() == 1) {
            return queryResult.get(0).get("line_id");
        } else {
            return "";
        }
    }

    public List<BusStopCoords> getBusStopsCoords() {
        dataBase.createDatabase();
        dataBase.openDatabase(SQLiteDatabase.OPEN_READONLY);
        Vector<HashMap<String, String>> queryResult = dataBase.executeQuery(SQLQueries.GET_BUS_STOPS_COORDS, null);
        dataBase.closeDatabase();

        List<BusStopCoords> busStopsCoords = new ArrayList<BusStopCoords>();
        for (HashMap<String, String> element: queryResult) {
            if (element.get("lon").isEmpty() || element.get("lat").isEmpty())
                continue;

            String busStopName = element.get("name");
            double lat = Double.parseDouble(element.get("lat"));
            double lon = Double.parseDouble(element.get("lon"));

            busStopsCoords.add(new BusStopCoords(busStopName, lat, lon));
        }

        return busStopsCoords;
    }

    DataBaseRepository(Context context) {
        dataBase = new BaseDatabaseRepository(context);
    }
}
