package pl.edu.agh.sm.whereisthatbus.app;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AutoCompleteTextView;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.Spinner;
import android.widget.Toast;

import com.parse.FindCallback;
import com.parse.ParseException;
import com.parse.ParseObject;
import com.parse.ParseQuery;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.List;


public class SearchActivity extends BaseActivityFunctions {
    final TimeTool timeTool = new TimeTool();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_search);

        setUIActivityElements();
    }

    /**
     * Funkcja mapuje odpwoeidnie elementu interfejsu na obiekty + ustawia ich zachowanie
     */
    @Override
    protected void setUIActivityElements() {
        busStopsName = (AutoCompleteTextView) findViewById(R.id.busStopsNameSearch);
        lineNameSpinner = (Spinner) findViewById(R.id.lineNameSpinnerSearch);
        directionSpinner = (Spinner) findViewById(R.id.directionSpinnerSearch);
        actionButton = (Button) findViewById(R.id.searchButton);
        refreshButton = (ImageButton) findViewById(R.id.refreshButtonSearch);

        setBusStopsNameAdapter();
        setLineNameSpinnerAdapter(-1);
        setDirectionSpinnerAdapter();

        setBusStopsNameListeners();
        setLineNameSpinnerListeners();
        setActionButtonListeners();
        setRefreshButtonListeners();
    }

    /**
     * Ustawia zachowanie przycisku send
     */
    protected void setActionButtonListeners() {
        actionButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                getInformation();
            }
        });
    }

    /**
     * Funkcja pobiera dane z serwera, przetwarza je i wyswietla
     */
    private void getInformation() {
        if (isInternetConnection(getApplicationContext())) {
            final QueryRawData queryRawData = createQueryRawData();

            ParseQuery<ParseObject> query = createQuery(queryRawData);
            if (query == null)
                Toast.makeText(getApplicationContext(), getString(R.string.invalid_input_data), Toast.LENGTH_LONG).show();
            else {
                Toast.makeText(getApplicationContext(), getString(R.string.please_wait_fetching_data), Toast.LENGTH_SHORT).show();
                sendQuery(query, queryRawData);
            }
        } else {
            Toast.makeText(getApplicationContext(), getString(R.string.no_internet_connection), Toast.LENGTH_LONG).show();
        }
    }

    /**
     * Funckja tworzy obiekt zawirajacy wszystkie dane potrzebne przy przetwarzaniu zaytania. Stworzona glownie po to by nie przesylac wielu argumentow
     *
     * @return obiekt zawierajacy wszystkie potrzebne informacje
     */
    private QueryRawData createQueryRawData() {
        // pobranie danych z interfejsu i bazy danych
        final String busStopName = busStopsName.getText().toString();
        final int stopId = db.getBusStopId(busStopName);

        String lineName = lineNameSpinner.getSelectedItem().toString();

        final String lastStopName = directionSpinner.getSelectedItem().toString();
        int lineDirectionId = db.getBusStopId(lastStopName);

        final String lineId = db.getLineId(lineName, lineDirectionId, stopId);

        final int currentTime = timeTool.getActualDayTimeInMinutes();
        final String dayLabel = timeTool.getCurrentDayLabel();

        final String connectionId = db.getNearestConnectionId(currentTime, lineId, stopId, dayLabel);

        int timeDifferenceBetweenFirstAndCurrentBusStop = db.getTimeBeetweenFirstAndCurrentBusStopForConnection(connectionId, stopId);
        int stopPlacement = db.getStopPlacement(lineId, stopId);

        // utworzenie obiektu z pobranymi danymi
        QueryRawData queryRawData = new QueryRawData();
        queryRawData.setBusStopName(busStopName);
        queryRawData.setBusStopId(stopId);
        queryRawData.setLineName(lineName);
        queryRawData.setLastStopName(lastStopName);
        queryRawData.setLineDirectionId(lineDirectionId);
        queryRawData.setLineId(lineId);
        queryRawData.setCurrentTime(currentTime);
        queryRawData.setDayLabel(dayLabel);
        queryRawData.setConnectionId(connectionId);
        queryRawData.setTimeDifferenceBetweenFirstAndCurrentBusStop(timeDifferenceBetweenFirstAndCurrentBusStop);
        queryRawData.setStopPlacement(stopPlacement);

        return queryRawData;
    }

    /**
     * Jezeli wprowadzone dane sa prawidlowe funkcja tworzy odpowiedni obiekt zapytania
     *
     * @param queryRawData obiekt z danymi potrzebnymi do wyslania zapytania
     * @return obiekt zaytania jesli dane sa prawidlowie w przeciwnym razie null
     */
    private ParseQuery<ParseObject> createQuery(QueryRawData queryRawData) {
        if (isQueryDataValid(queryRawData.getLineId(), queryRawData.getBusStopId(), queryRawData.getStopPlacement())) {
            return createParseQuery(queryRawData.getLineName(), queryRawData.getLineId(), queryRawData.getLineDirectionId(),
                    queryRawData.getStopPlacement(), queryRawData.getTimeDifferenceBetweenFirstAndCurrentBusStop());
        } else {
            return null;
        }
    }

    /**
     * Funkcja za zadanie wyslanie zapytania do serwera
     *
     * @param query        zapyttanie
     * @param queryRawData obiekt zawierajacy dane z zapytania potrzebny do obliczen
     */
    private void sendQuery(ParseQuery<ParseObject> query, final QueryRawData queryRawData) {
        query.findInBackground(new FindCallback<ParseObject>() {
            @Override
            public void done(List<ParseObject> parseObjects, ParseException e) {
                // przetwarzamy odpowiedz parsa i dostajemy liczbe minut (1, wiele lub zadnej) do przyjazdu
                List<Integer> minutesToArrive = processParseResponse(parseObjects, queryRawData);

                String message = createMessage(minutesToArrive, queryRawData);

                showMessage(message);
            }
        });
    }

    /**
     * Funckja sprawdza czy wprowadzony zewstaw danych jest prawidlowy
     *
     * @param lineId        id linii
     * @param stopId        id przystanku
     * @param stopPlacement umiejscowienie przystanku w polaczeniu
     * @return true jesli dane sa poprawne, false w przeciwnym razie
     */
    private boolean isQueryDataValid(String lineId, int stopId, int stopPlacement) {
        return db.validate(lineId, stopId, stopPlacement);
    }

    /**
     * funkcja tworzy odpwoiedni obiekt zapytania, ktory ma zostac wyslany do serwera
     *
     * @param lineName                                    nazwa linii
     * @param lineId                                      id linii
     * @param lineDirectionId                             id ostatniego przystanku
     * @param stopPlacement                               umiejscowienie przystanku w polaczeniu
     * @param timeDifferenceBetweenFirstAndCurrentBusStop czas pomiedzy pierwszym i aktualnym przystankiem
     * @return obiekt zapytania do serwera
     */
    private ParseQuery<ParseObject> createParseQuery(String lineName, String lineId, int lineDirectionId, int stopPlacement, int timeDifferenceBetweenFirstAndCurrentBusStop) {
        ParseQuery<ParseObject> query = ParseQuery.getQuery(getString(R.string.parse_object_name));
        query.whereEqualTo("line_number", lineName);
        query.whereEqualTo("line_id", lineId);
        query.whereEqualTo("line_direction_id", lineDirectionId);
        query.whereLessThan("stop_placement", stopPlacement); // zeby nie brac pod uwage polaczen rejestrowanych na kolejnych przystankach na trasie
        query.whereGreaterThan("report_time", (System.currentTimeMillis() - 1000 * 60 * timeDifferenceBetweenFirstAndCurrentBusStop) / 1000); // zeby zawedzic czas wyszukiwania

        return query;
    }

    /**
     * Funkcja przetwarza odpowiedz z serwera i zwraca liste minut
     *
     * @param parseObjects odpowiedz z serwera
     * @param queryRawData obiekt zawierajacy dane z zapytania potrzebny do obliczen
     * @return lista minut najblizszych polaczen
     */
    private List<Integer> processParseResponse(List<ParseObject> parseObjects, final QueryRawData queryRawData) {
        List<ReportData> reportDataList = createReportDataList(parseObjects, queryRawData);

        List<Integer> minutesToArrive = getMinutesToArrive(reportDataList, queryRawData);

        return minutesToArrive;
    }

    /**
     * Funkcja odpwiedzialna za stworzenie wiadomosci z infomacja o najblizszych polaczeniach, ktora ma zostac wyswietlona uzytkownikowi
     *
     * @param minutesToArrive lista z minutami do przyjazdu autobusu
     * @param queryRawData    obiekt zawierajacy dane z zapytania potrzebny do obliczen
     * @return wiadomosc dla uzytkownika
     */
    private String createMessage(List<Integer> minutesToArrive, QueryRawData queryRawData) {
        StringBuilder sb = new StringBuilder();

        // w przypadku gdzy mamy jakies zgloszenie
        if (minutesToArrive.size() >= 1) {
            int nearestConnection = minutesToArrive.get(0);
            sb.append(getString(R.string.nearest_connection_in) + nearestConnection + " ");
            if (nearestConnection == 1) {
                sb.append(getString(R.string.one_minute) + "\n");
            } else {
                sb.append(getString(R.string.minutes) + "\n");
            }

            // dodatkowa informacja jesli zgloszen jest wiecej
            if (minutesToArrive.size() > 1) {
                sb.append(getString(R.string.next_connections_in));
                for (int i = 1; i < minutesToArrive.size() - 1; i++) {
                    sb.append(minutesToArrive.get(i) + ", ");
                }
                sb.append(minutesToArrive.get(minutesToArrive.size() - 1));
                sb.append("\n");
            }
        } else {
            sb.append(getString(R.string.no_reports_for_connection) + "\n");
        }

        int nearestScheduledConnection = db.getNearestConnectionTimeArrival(queryRawData.getCurrentTime(), queryRawData.getLineId(), queryRawData.getBusStopId(), queryRawData.getDayLabel());
        sb.append(getString(R.string.nearest_scheduled_connection_in) + nearestScheduledConnection + " ");
        if (nearestScheduledConnection == 1) {
            sb.append(getString(R.string.one_minute) + "\n");
        } else {
            sb.append(getString(R.string.minutes) + "\n");
        }

        return sb.toString();
    }

    /**
     * Funkcja wyswietlajaza alert uzytkownikowi o najblizszych polaczeniach
     *
     * @param message wiadomosc ktora ma zostac wyswietlona
     */
    private void showMessage(String message) {
        AlertDialog.Builder builder = new AlertDialog.Builder(SearchActivity.this);
        builder.setTitle(getString(R.string.dialog_title));
        builder.setMessage(message);

        builder.setPositiveButton(getString(R.string.dialog_title), new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
                SearchActivity.this.finish();
            }
        });
        AlertDialog dialog = builder.create();
        dialog.show();
    }

    /**
     * Funkcja analizuje wynik zapytania zwrocony przez parsa i odsiewa niektore wyniki
     *
     * @param parseObjects obiekt zwrocony przez parsa
     * @param queryRawData obiekt zawierajacy dane z zapytania potrzebny do obliczen
     * @return lista raportow, ktore mozna dalej przetworzyc. Pusta lista jesli zapytanie nic nie zwrocilo lub zostalo odrzucone.
     */
    private List<ReportData> createReportDataList(List<ParseObject> parseObjects, QueryRawData queryRawData) {
        List<ReportData> reportDataList = new ArrayList<ReportData>();
        for (ParseObject parseObject : parseObjects) {
            // pobieramy id przystanku z ktorego przeslano raport
            int reportStopId = parseObject.getInt("bus_stop_id");

            // obliczamy czas (w minutach) pomiedzy aktualnym przystankiem a tym z raportu dla polaczenia najblizszego aktualnej godzinie
            int timeBetweenBusStops = db.getTimeBeetweenBusStops(queryRawData.getConnectionId(), reportStopId, queryRawData.getBusStopId());

            long reportTimeInMilliseconds = parseObject.getLong("report_time") * 1000;
            long estimatedTimeOfArrival = reportTimeInMilliseconds + timeBetweenBusStops * 60 * 1000; // w milisekundach
            Date estimatedDateOfArrival = new Date(estimatedTimeOfArrival);

            // obliczamy czas w minutach
            int expectedArrivalTime = timeTool.convertDateToMinutest(estimatedDateOfArrival) - timeTool.convertDateToMinutest(new Date(System.currentTimeMillis()));

            // dodajemy tylko te ktore jeszcze nie mogly przejechac przez przystnaek
            if (expectedArrivalTime > 0)
                reportDataList.add(new ReportData(reportTimeInMilliseconds, expectedArrivalTime));
        }

        return reportDataList;
    }

    /**
     * Funkcja przetwarza wynik zapytania z serwera i zwraca liste minut z najblizszymi polaczeniami
     *
     * @param reportDataList wstepnie przetworzone dane z serwera
     * @param queryRawData   obiekt zawierajacy dane z zapytania potrzebny do obliczen
     * @return lista minut z najblizszymi polaczeniami. Jezeli nie ma informacji o polaczeniu zwracana jest puta lista
     */
    private List<Integer> getMinutesToArrive(List<ReportData> reportDataList, QueryRawData queryRawData) {
        List<Integer> minutesToArrive = new ArrayList<Integer>();

        if (reportDataList.size() > 1) {
            minutesToArrive = manyReports(reportDataList, queryRawData);
        } else if (reportDataList.size() == 1) {
            minutesToArrive.add(reportDataList.get(0).minutestToArrive);
        }

        return minutesToArrive;
    }

    /**
     * Funckja majaca za zadanie przetworzyc wstepnie przetworzone dane z serwera w przypadku gdy zgloszen jest wiecej niz 1
     *
     * @param reportDataList wstepnie przetworzone dane z serwera
     * @param queryRawData   obiekt zawierajacy dane z zapytania potrzebny do obliczen
     * @return lista minut do przyjazdu autobusu
     */
    private List<Integer> manyReports(List<ReportData> reportDataList, QueryRawData queryRawData) {
        // sortujemy raporty po czasie ich zgloszenia
        Collections.sort(reportDataList, new ReportDataComparator());
        Collections.reverse(reportDataList);

        // obliczamy roznice czasu pomiedzy dwoma najblizszymi polaczeniami dla danego przystanku i linii w danym czasie
        int deltaT = db.getDeltaTimeBetweenNearestConnections(queryRawData.getCurrentTime(), queryRawData.getLineId(), queryRawData.getBusStopId(), queryRawData.getDayLabel());

        // lista przechowujaca liczbe minut do przyjazdu autobusow
        List<Integer> minutesToArrive = new ArrayList<Integer>();

        for (ReportData reportData : reportDataList) {
            // dodajemy najaktualniejszy wpis
            if (minutesToArrive.size() == 0) {
                minutesToArrive.add(reportData.minutestToArrive);
            } else {
                boolean add = true;
                // sprawdzamy czy aktualne polaczenie nie jest zbyt blisko polaczenia znajdujacego sie  juz na liscie
                for (Integer i : minutesToArrive) {
                    if (Math.abs(i - reportData.minutestToArrive) - deltaT < 0) {
                        add = false;
                    }
                }

                if (add) {
                    minutesToArrive.add(reportData.minutestToArrive);
                }
            }
        }
        Collections.sort(minutesToArrive);

        return minutesToArrive;
    }

    @Override
    protected void onResume() {
        super.onResume();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        return super.onOptionsItemSelected(item);
    }
}

/**
 * Klasa pomocnicza uzywana przy wysylaniu i przetwarzaniu zapytania
 */
class QueryRawData {
    private String busStopName;
    private int busStopId;
    private String lineName;
    private String lastStopName;
    private int lineDirectionId;
    private String lineId;
    private int currentTime;
    private String dayLabel;
    private String connectionId;
    private int timeDifferenceBetweenFirstAndCurrentBusStop;
    private int stopPlacement;

    public int getBusStopId() {
        return busStopId;
    }

    public void setBusStopId(int busStopId) {
        this.busStopId = busStopId;
    }

    public String getLastStopName() {
        return lastStopName;
    }

    public void setLastStopName(String lastStopName) {
        this.lastStopName = lastStopName;
    }

    public int getLineDirectionId() {
        return lineDirectionId;
    }

    public void setLineDirectionId(int lineDirectionId) {
        this.lineDirectionId = lineDirectionId;
    }

    public String getLineId() {
        return lineId;
    }

    public void setLineId(String lineId) {
        this.lineId = lineId;
    }

    public int getCurrentTime() {
        return currentTime;
    }

    public void setCurrentTime(int currentTime) {
        this.currentTime = currentTime;
    }

    public String getDayLabel() {
        return dayLabel;
    }

    public void setDayLabel(String dayLabel) {
        this.dayLabel = dayLabel;
    }

    public String getConnectionId() {
        return connectionId;
    }

    public void setConnectionId(String connectionId) {
        this.connectionId = connectionId;
    }

    public int getTimeDifferenceBetweenFirstAndCurrentBusStop() {
        return timeDifferenceBetweenFirstAndCurrentBusStop;
    }

    public void setTimeDifferenceBetweenFirstAndCurrentBusStop(int timeDifferenceBetweenFirstAndCurrentBusStop) {
        this.timeDifferenceBetweenFirstAndCurrentBusStop = timeDifferenceBetweenFirstAndCurrentBusStop;
    }

    public int getStopPlacement() {
        return stopPlacement;
    }

    public void setStopPlacement(int stopPlacement) {
        this.stopPlacement = stopPlacement;
    }

    public String getBusStopName() {
        return busStopName;
    }

    public void setBusStopName(String busStopName) {
        this.busStopName = busStopName;
    }

    public String getLineName() {
        return lineName;
    }

    public void setLineName(String lineName) {
        this.lineName = lineName;
    }
}

/**
 * klasa pomocnicza uzwana przy przetwarzaniu odpwiedzi z serwera
 */
class ReportData {
    long reportTime;
    int minutestToArrive;

    public ReportData( long reportTime, int minutestToArrive) {
        this.reportTime = reportTime;
        this.minutestToArrive = minutestToArrive;
    }
}

/**
 * klasa pomocnicza uzywana przy sortowaniu wynikow zapytania po daciie ich zgloszenia
 */
class ReportDataComparator implements Comparator<ReportData> {
    @Override
    public int compare(ReportData o1, ReportData o2) {
        if (o1.reportTime >= o2.reportTime) {
            return 1;
        } else {
            return -1;
        }
    }
}
