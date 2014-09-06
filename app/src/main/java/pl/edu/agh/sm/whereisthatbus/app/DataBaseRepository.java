package pl.edu.agh.sm.whereisthatbus.app;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Vector;

/**
 * Klasa odpowiedzialna za kierowanie zapytan do bazy danych oraz obrobke ich wynikow
 */
public class DataBaseRepository {
    private BaseDatabaseRepository dataBase;

    DataBaseRepository(Context context) {
        dataBase = new BaseDatabaseRepository(context);
    }

    /**
     * Funkcja zwraca nazwy wszystkich przystankow znajdujace sie w bazie danych
     * @return nazwy wszystkich przystankow
     */
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

    /**
     * Funkcja zwraca nazwy wszystkich linii znajdujacych sie w bazie danych
     * @return nazwy wszystkich linii
     */
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

    /**
     * Funckja zwraca nazwy linii, ktore zatrzymuja sie na danym przystanku
     * @param busStopId id przystanku lda ktorego dokonujemy zapytania
     * @return numery linii, ktore zatrzymuja sie na przystanku
     */
    public List<String> getAllLinesForBusStop(int busStopId) {
        dataBase.createDatabase();
        dataBase.openDatabase(SQLiteDatabase.OPEN_READONLY);
        Vector<HashMap<String, String>> queryResult = dataBase.executeQuery(SQLQueries.GET_ALL_LINES_FOR_BUS_STOP, new String[]{Integer.toString(busStopId)});
        dataBase.closeDatabase();
        List<String> linesForBusStop = new ArrayList<String>();

        for (HashMap<String, String> element: queryResult) {
            linesForBusStop.add(element.get("line_number"));
        }

        return linesForBusStop;
    }

    /**
     * Zwraca id ostatniego przystanku dla podanej linii
     * @param line id linii
     * @return id przystanku
     */
    public List<String> getEndStopIdsForLine(String line) {
        dataBase.createDatabase();
        dataBase.openDatabase(SQLiteDatabase.OPEN_READONLY);
        Vector<HashMap<String, String>> queryResult = dataBase.executeQuery(SQLQueries.GET_END_STOPS_NAMES_FOR_LINE, new String[]{line});
        dataBase.closeDatabase();
        List<String> endStopsForLine = new ArrayList<String>();

        for (HashMap<String, String> element : queryResult) {
            endStopsForLine.add(element.get("stop_name"));
        }

        return endStopsForLine;
    }

    /**
     * Mapuje nazwe przystanku na jego id
     * @param busStopName nazwa przystanku
     * @return id przystanku
     */
    public int getBusStopId(String busStopName) {
        dataBase.createDatabase();
        dataBase.openDatabase(SQLiteDatabase.OPEN_READONLY);
        String parameters[] = {busStopName};
        Vector<HashMap<String, String>> queryResult = dataBase.executeQuery(SQLQueries.GET_BUS_STOP_ID, parameters);
        dataBase.closeDatabase();

        if (queryResult.size() == 1) {
            return Integer.parseInt(queryResult.get(0).get("stop_id"));
        } else {
            return -1;
        }
    }

    /**
     * Mapuje id przystanku na jego nazwe
     * @param busStopId id przystanku
     * @return nazwa przystanku
     */
    public String getBusStopName(int busStopId) {
        dataBase.createDatabase();
        dataBase.openDatabase(SQLiteDatabase.OPEN_READONLY);
        String parameters[] = {Integer.toString(busStopId)};
        Vector<HashMap<String, String>> queryResult = dataBase.executeQuery(SQLQueries.GET_BUS_STOP_ID, parameters);
        dataBase.closeDatabase();

        if (queryResult.size() == 1) {
            return queryResult.get(0).get("stop_name");
        } else {
            return "";
        }
    }

    /**
     * Funkcja zwraca id linii na podstawie nazwy linii, przystanku koncowego oraz przystanku po trasie.
     * Czasami nie wystarcza pierwsze dwa parametry i trzeba dolozyc jeszcze trzeci.
     * @param lineName nazwa linii
     * @param lastBusStopId id ostatniego przystanku dla linii
     * @param stopId przystanek po trasie linii
     * @return id linii lub pusty string
     */
    public String getLineId(String lineName, int lastBusStopId, int stopId) {
        String lineId = "";

        dataBase.createDatabase();
        dataBase.openDatabase(SQLiteDatabase.OPEN_READONLY);

        // pierwsze podejscie
        String parameters[] = {lineName, Integer.toString(lastBusStopId)};
        Vector<HashMap<String, String>> queryResult = dataBase.executeQuery(SQLQueries.GET_LINE_ID, parameters);
        dataBase.closeDatabase();

        if (queryResult.size() == 1) {
            lineId = queryResult.get(0).get("line_id");
        } else {
            //drugie podejscie gdy znaleziono wiecej linie id dla danego numeru linii i przystanka koncowego
            dataBase.openDatabase(SQLiteDatabase.OPEN_READONLY);
            String parameters2[] = {lineName, Integer.toString(lastBusStopId), Integer.toString(stopId)};
            queryResult = dataBase.executeQuery(SQLQueries.GET_LINE_ID2, parameters2);
            dataBase.closeDatabase();

            if (queryResult.size() > 0) {
                lineId = queryResult.get(0).get("line_id");
            }
        }

        return lineId;
    }

    /**
     * Funckja zwraca liste wspolrzednych dla przystankow
     * @return lista wspolrzednych przystankow
     */
    public List<BusStopCoords> getBusStopsCoords() {
        dataBase.createDatabase();
        dataBase.openDatabase(SQLiteDatabase.OPEN_READONLY);
        Vector<HashMap<String, String>> queryResult = dataBase.executeQuery(SQLQueries.GET_BUS_STOPS_COORDS, null);
        dataBase.closeDatabase();

        List<BusStopCoords> busStopsCoords = new ArrayList<BusStopCoords>();
        for (HashMap<String, String> element : queryResult) {
            if (element.get("longitude").isEmpty() || element.get("latitude").isEmpty())
                continue;

            String busStopName = element.get("stop_name");
            double lat = Double.parseDouble(element.get("latitude"));
            double lon = Double.parseDouble(element.get("longitude"));

            busStopsCoords.add(new BusStopCoords(busStopName, lat, lon));
        }

        return busStopsCoords;
    }

    /**
     * Funkcja zwraca id najblizszego polaczenia dla zadanych argumentow.
     * @param time czas wyrazony w minutach, ktore uplynely od polnocy
     * @param lineId id linii
     * @param stopId id przystanku
     * @param dayType flaga okreslajaca dzien tygodnia
     * @return id najblizszego polaczenia
     */
    public String getNearestConnectionId(int time, String lineId, int stopId, String dayType) {
        dataBase.createDatabase();
        dataBase.openDatabase(SQLiteDatabase.OPEN_READONLY);

        String parameters[] = {Integer.toString(time), Integer.toString(time), lineId, Integer.toString(stopId), dayType};
        Vector<HashMap<String, String>> queryResult = dataBase.executeQuery(SQLQueries.GET_NEAREST_CONNECTION_ID_FOR_LINE_ID_ON_STOP_ID, parameters);
        dataBase.closeDatabase();

        String connectionId = "";

        if (queryResult.size() == 1) {
            connectionId = queryResult.get(0).get("connection_id");
        }

        return connectionId;
    }

    /**
     * Funkcja zwraca pozycje przystanku dla podanej linie id
     * @param lineId id linii
     * @param stopId id przystanku
     * @return pozycja przystanku dla line id
     */
    public int getStopPlacement(String lineId, int stopId) {
        dataBase.createDatabase();
        dataBase.openDatabase(SQLiteDatabase.OPEN_READONLY);

        String parameters[] = {lineId, Integer.toString(stopId)};
        Vector<HashMap<String, String>> queryResult = dataBase.executeQuery(SQLQueries.GET_STOP_ID_PLACEMENT_IN_LINE_ID, parameters);
        dataBase.closeDatabase();

        int stopIdPlacement = -1;

        if (queryResult.size() == 1) {
            stopIdPlacement = Integer.parseInt(queryResult.get(0).get("line_stop_no"));
        }

        return stopIdPlacement;
    }

    /**
     * Zwraca czas pomiedzy pierwszym i aktualnym przystankiem dla podanego polaczenia
     * @param connectionId id polaczenia
     * @param currentBusStopId id aktualnego przystanku
     * @return czas w minutach pomiedzy przystankami. W przypadku niepowodzenia -1.
     */
    public int getTimeBeetweenFirstAndCurrentBusStopForConnection(String connectionId, int currentBusStopId) {
        dataBase.createDatabase();
        dataBase.openDatabase(SQLiteDatabase.OPEN_READONLY);

        String parameters[] = {connectionId, Integer.toString(currentBusStopId)};
        Vector<HashMap<String, String>> queryResult = dataBase.executeQuery(SQLQueries.GET_CONNECTION_ID_TIMES_BETWEEN_FIRST_AND_CURRENT_STOP, parameters);
        dataBase.closeDatabase();

        int timeBetweenBusStops = -1;

        if (queryResult.size() == 2) {
            timeBetweenBusStops = Math.abs(Integer.parseInt(queryResult.get(0).get("time")) - Integer.parseInt(queryResult.get(1).get("time")));
        }

        return timeBetweenBusStops;
    }

    /**
     * Funkcja zwraca czas pomiedzy przystankami dla danego polaczenia
     * @param connectionId id polaczenia
     * @param busStopId1 id pierwszego przystanku
     * @param busStopId2 id drugiego przystanku
     * @return czas w minutach pomiedzy przystankami. W przypadku niepowodzenia -1.
     */
    public int getTimeBeetweenBusStops(String connectionId, int busStopId1, int busStopId2) {
        dataBase.createDatabase();
        dataBase.openDatabase(SQLiteDatabase.OPEN_READONLY);

        String parameters[] = {connectionId, Integer.toString(busStopId1), Integer.toString(busStopId2)};
        Vector<HashMap<String, String>> queryResult = dataBase.executeQuery(SQLQueries.GET_CONNECTION_ID_TIMES_BETWEEN_BUS_STOPS, parameters);
        dataBase.closeDatabase();

        int timeBetweenBusStops = -1;

        if (queryResult.size() == 2) {
            timeBetweenBusStops = Math.abs(Integer.parseInt(queryResult.get(0).get("time")) - Integer.parseInt(queryResult.get(1).get("time")));
        }

        return timeBetweenBusStops;
    }

    /**
     * Funckaj oblicza roznice czasowa pomiedzy dwoma kolejnymi polaczeniami
     * @param time czas po ktorym beda wyszukiwane polaczenia
     * @param lineId id linii
     * @param stopId id przystanku
     * @param dayType rodzaj dnia
     * @return czas pomiedzy dwoma kolejnymi planowanymi polaczeniami podzielony na 3
     */
    public int getDeltaTimeBetweenNearestConnections(int time, String lineId, int stopId, String dayType) {
        dataBase.createDatabase();
        dataBase.openDatabase(SQLiteDatabase.OPEN_READONLY);

        String parameters[] = {Integer.toString(time), Integer.toString(time), lineId, Integer.toString(stopId), dayType};
        Vector<HashMap<String, String>> queryResult = dataBase.executeQuery(SQLQueries.GET_TWO_NEAREST_FUTURE_CONNECTIONS_TIME, parameters);
        dataBase.closeDatabase();

        int deltaBetweenConnections = 5;

        if (queryResult.size() == 2) {
            deltaBetweenConnections = Math.abs(Integer.parseInt(queryResult.get(0).get("time")) - Integer.parseInt(queryResult.get(1).get("time")));
            if (deltaBetweenConnections > 5)
                deltaBetweenConnections /= 2;
        }

        return deltaBetweenConnections;
    }

    /**
     * Funkcja zwraca za ile minut planowo autobus powienien przyjechac na przystanek
     *
     * @param time    aktualny czas
     * @param lineId  id linii
     * @param stopId  id przystanku
     * @param dayType rodzaj dnia
     * @return liczba minut do planowego przyjazdu autobusu na przystanek
     */
    public int getNearestConnectionTimeArrival(int time, String lineId, int stopId, String dayType) {
        dataBase.createDatabase();
        dataBase.openDatabase(SQLiteDatabase.OPEN_READONLY);

        String parameters[] = {Integer.toString(time), lineId, Integer.toString(stopId), dayType};
        Vector<HashMap<String, String>> queryResult = dataBase.executeQuery(SQLQueries.GET_NEAREST_CONNECTION_TIME, parameters);
        dataBase.closeDatabase();

        int minutes = -1;
        if (queryResult.size() == 1)
            minutes = Integer.parseInt(queryResult.get(0).get("minumum"));

        return minutes;
    }

    /**
     * Funkcja sprawdzajaca czy podany zestaw parametrow jest prawidlowy
     *
     * @param lineId        id linii
     * @param stopId        id przystanku
     * @param stopPlacement umiejscowienie przystanku
     * @return true jezeli dane sa poprawne, false w przeciwnym razie
     */
    public boolean validate(String lineId, int stopId, int stopPlacement) {
        dataBase.createDatabase();
        dataBase.openDatabase(SQLiteDatabase.OPEN_READONLY);

        String parameters[] = {lineId, Integer.toString(stopId), Integer.toString(stopPlacement)};
        Vector<HashMap<String, String>> queryResult = dataBase.executeQuery(SQLQueries.VALIDATE_QUERY, parameters);
        dataBase.closeDatabase();

        if (queryResult.size() == 1) {
            int rows = Integer.parseInt(queryResult.get(0).get("rows"));
            if (rows > 0)
                return true;

        }

        return false;
    }
}
