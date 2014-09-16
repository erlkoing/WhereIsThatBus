package pl.edu.agh.sm.whereisthatbus.app;

import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AutoCompleteTextView;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.Spinner;
import android.widget.Toast;

import com.parse.ParseException;
import com.parse.ParseObject;
import com.parse.SaveCallback;

/**
 * Activity odpowiedzialne za tworzenie i przesylanie raportow o polaczeniach do serwera.
 */
public class ReportActivity extends BaseActivityFunctions {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_report);
        setUIActivityElements();
    }

    /**
     * Funkcja przypisuje do obiektow odpowiednie elementy interfejsu oraz ustawia dla nich adaptery, litenery.
     */
    @Override
    protected void setUIActivityElements() {
        busStopsName = (AutoCompleteTextView) findViewById(R.id.busStopsNameReport);
        lineNameSpinner = (Spinner) findViewById(R.id.lineNameSpinnerReport);
        directionSpinner = (Spinner) findViewById(R.id.directionSpinnerReport);
        actionButton = (Button) findViewById(R.id.reportButton);
        refreshButton = (ImageButton) findViewById(R.id.refreshButtonReport);

        setBusStopsNameAdapter();
        setLineNameSpinnerAdapter(-1);
        setDirectionSpinnerAdapter();

        setBusStopsNameListeners();
        setLineNameSpinnerListeners();
        setActionButtonListeners();
        setRefreshButtonListeners();
    }

    /**
     * Funkcja ustawia odpowiednia akcje po nacisnieciu przycisku wyslij.
     */
    protected void setActionButtonListeners() {
        actionButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                sendReport();
            }
        });
    }

    /**
     * Funkcja odpowiedzialna za wyslanie raportu o polaczeniu na serwer lub wyswietlenie odpowiedniej informacji jezeli nie ma mozliwosci przeslania (np. brak polaczenia z internetem).
     */
    private void sendReport() {
        if (isInternetConnection(getApplicationContext())) {
            ParseObject reportData = createReport();

            if (reportData == null)
                Toast.makeText(getApplicationContext(), getString(R.string.invalid_input_data), Toast.LENGTH_LONG).show();
            else
                reportData.saveInBackground(new SaveCallback() {
                    @Override
                    public void done(ParseException e) {
                        afterReport();
                    }
                });

        } else
            Toast.makeText(getApplicationContext(), getString(R.string.no_internet_connection), Toast.LENGTH_LONG).show();
    }

    /**
     * Funkcja odpowiedzialna za pobranie informacji z interfejsu, sprawdzenie ich poprawnosci oraz utworzenie odpowiedniego obiektu jezeli dane sa poprawne.
     *
     * @return obiekt ReportObject jesli wprowadzone dane sa poprawne. W przeciwnym razie null.
     */
    private ParseObject createReport() {
        // pobranie danych potrzebnych do stworzenia obiektu Report
        String busStopName = busStopsName.getText().toString();
        int stopId = db.getBusStopId(busStopName);
        String lineNumber = lineNameSpinner.getSelectedItem().toString();
        String lastStopName = directionSpinner.getSelectedItem().toString();
        int lastStopId = db.getBusStopId(lastStopName);
        String lineId = db.getLineId(lineNumber, lastStopId, stopId);
        int stopPlacement = db.getStopPlacement(lineId, stopId);

        // sprawdzenie poprawnosci pobranych danych
        if (isReportDataValid(lineId, stopId, stopPlacement))
            return createReportObject(stopId, lastStopId, stopPlacement, lineId, lineNumber);
        else
            return null;
    }

    /**
     * Funkcja wywolywana po udanym wyslaniu danych na serwer. Funkcja wyswietla stosowna informacje oraz zamyka aktywnosc powracajac do MainActivity.
     */
    private void afterReport() {
        Toast.makeText(getApplicationContext(), getString(R.string.report_send), Toast.LENGTH_SHORT).show();
        this.finish();
    }

    /**
     * Funkcja ma za zadanie sprawdzic czy wprowadzone dane sa poprawne.
     *
     * @param lineId        id linii.
     * @param stopId        id przystanku.
     * @param stopPlacement umiejscowienie przystanku w kontekscie danego polaczenia.
     * @return true jesli dane sa poprawne, false w przeciwnym razie.
     */
    private boolean isReportDataValid(String lineId, int stopId, int stopPlacement) {
        return db.validate(lineId, stopId, stopPlacement);
    }

    /**
     * Funkcja tworzy obiekt parsa - ReportObject zawirajacy dane, ktore maja zostac umieszone na serwerze.
     *
     * @param stopId        id przystanku, z ktorego jest wysylany raport.
     * @param lastStopId    id przystanku koncowego dla zglaszanej linii.
     * @param stopPlacement umiejscowienie przystanku w kontekscie danego polaczenia.
     * @param lineId        id zglaszanej linii.
     * @param lineNumber    numer linii.
     * @return obiekt parsa zawirajacy informacje o polaczeniu.
     */
    private ParseObject createReportObject(int stopId, int lastStopId, int stopPlacement, String lineId, String lineNumber) {
        // umieszczenie danych w obiekcje Report
        ParseObject reportData = new ParseObject(getString(R.string.parse_object_name));
        reportData.put("bus_stop_id", stopId);
        reportData.put("stop_placement", stopPlacement);
        reportData.put("line_id", lineId);
        reportData.put("line_number", lineNumber);
        reportData.put("line_direction_id", lastStopId);
        reportData.put("report_time", System.currentTimeMillis() / 1000);

        return reportData;
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
