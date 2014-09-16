package pl.edu.agh.sm.whereisthatbus.app;

import android.app.Activity;
import android.content.Context;
import android.content.SharedPreferences;
import android.net.ConnectivityManager;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.Spinner;
import android.widget.Toast;

import com.parse.Parse;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;


/**
 * Klasa zawiera wspolna funkcjonalnosc dla aktywnosci SearchActivity oraz ReportActivity
 * tj. zachowanie przy wznowieniu, obsluge menu, ustawienie zachowania adapterow, listenerow.
 */
public abstract class BaseActivityFunctions extends Activity {

    // Obiekty UI
    protected AutoCompleteTextView busStopsName;
    protected Spinner lineNameSpinner;
    protected Spinner directionSpinner;
    protected Button actionButton;
    protected ImageButton refreshButton;

    // Pomocnicze obiekty
    protected DataBaseRepository db;
    protected List<BusStopCoords> busStopCoordsList;
    protected SharedPreferences prefs;

    // finkcje abstrakcyjne
    protected abstract void setUIActivityElements();

    protected abstract void setActionButtonListeners();

    /**
     * Funkcja wywolywana przy tworzeniu aktywnosci inicjalizuje obiekty bazy danych,
     * parsa, sharedpreferences oraz pobiera koordynaty dla przystankow.
     *
     * @param savedInstanceState stan instancji.
     */
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        db = new DataBaseRepository(this);
        busStopCoordsList = db.getBusStopsCoords();
        prefs = this.getSharedPreferences(getString(R.string.shared_preferences_path), Context.MODE_PRIVATE);
        Parse.initialize(this, getString(R.string.parse_application_id), getString(R.string.parse_client_key)); // inicjalizacja parsa do przesylania odbierania danych z serwera
    }

    /**
     * Funkcja wywolywana przy kazdym wznowieniu aktywnosci. Sprawdza czy automatyczna lokalizacja
     * jest wlaczona i jezeli jest ustawia najblizszy przystanek jako aktualny.
     */
    @Override
    protected void onResume() {
        super.onResume();

        // sprawdzanie czy jest wlaczone automatyczne wykrywanie najblizszego przystanku
        boolean autoLocation = prefs.getBoolean(getString(R.string.sp_auto_location), true);
        if (!prefs.contains(getString(R.string.sp_auto_location)))
            prefs.edit().putBoolean(getString(R.string.sp_auto_location), autoLocation).commit();

        if (autoLocation)
            setNearestBusStopAsCurrentBusStop();
    }

    /**
     * Funkcja oblicza i ustawia najblizyszy przystanek jako biezacy przystanek. Jezeli nie jest
     * mozliwe okreslenie lokalizacji lub jest wylaczona wyswietlana jest odpowiednia wiadomosc.
     */
    protected void setNearestBusStopAsCurrentBusStop() {
        GPSTracker gpsTracker = new GPSTracker(this);
        if (gpsTracker.canGetLocation()) {
            // pobranie nazwy najblizszego przystanku
            String nearestBusStop = getNearestBusStopName();

            // ustawienie nazwy przystanku w EditTexcie
            busStopsName.setText(nearestBusStop);
            busStopsName.performCompletion();

            // ustawia linie, ktore kursuja przez podany przystanek
            setLineNameSpinnerAdapter(db.getBusStopId(nearestBusStop));

            // ustawia kierunek dla biezacej linii
            setDirectionSpinnerAdapter();
        } else {
            Toast.makeText(getApplicationContext(), getString(R.string.could_not_get_location), Toast.LENGTH_LONG).show();
        }

        gpsTracker.stopUsingGPS();
    }

    /**
     * Funkcja zwraca nazwe najblizszego przystanku na podstawie koordynatow GPS.
     *
     * @return Zwraca nazwe najblizszego przystanku. Jezeli nie mozna ustalic polozenia funkcja zwraca pusty string.
     */
    protected String getNearestBusStopName() {
        GPSTracker gpsTracker = new GPSTracker(this);
        if (gpsTracker.canGetLocation()) {

            BusStopCoords nearestBusStop = null;

            double currentLat = gpsTracker.getLatitude();
            double currentLon = gpsTracker.getLongitude();
            for (BusStopCoords busStopCoords : busStopCoordsList) {
                if (nearestBusStop == null)
                    nearestBusStop = busStopCoords;
                else {
                    if (distanceBetweenPoints(busStopCoords.getLat(), busStopCoords.getLon(), currentLat, currentLon) < distanceBetweenPoints(nearestBusStop.getLat(), nearestBusStop.getLon(), currentLat, currentLon))
                        nearestBusStop = busStopCoords;
                }
            }
            gpsTracker.stopUsingGPS();
            return nearestBusStop.getBusStopName();
        } else
            return "";

    }

    /**
     * Funkcja ustawia dostepne nazwy linii w zaleznosci od wybranego przystanku.
     *
     * @param busStopId jesli -1 wstawione zostana nazwy wszystkich linii. W przeciwnym razie zostana wstawione tylko te linie, ktore kursuja przez podany przystanek.
     */
    protected void setLineNameSpinnerAdapter(int busStopId) {
        ArrayAdapter<String> adapter;
        if (busStopId == -1)
            adapter = new ArrayAdapter<String>(this, android.R.layout.simple_list_item_1, db.getAllLineNames());
        else
            adapter = new ArrayAdapter<String>(this, android.R.layout.simple_list_item_1, db.getAllLinesForBusStop(busStopId));

        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        lineNameSpinner.setAdapter(adapter);
    }

    /**
     * Funkcja ustawia nazwy przystankow koncowych dla aktualnie wybranej linii.
     */
    protected void setDirectionSpinnerAdapter() {
        String line = lineNameSpinner.getSelectedItem().toString();
        ArrayAdapter<String> adapter = new ArrayAdapter<String>(this, android.R.layout.simple_list_item_1, db.getEndStopIdsForLine(line));
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        directionSpinner.setAdapter(adapter);
    }

    /**
     * Funkcja obliczajaca odleglosc pomiedzy dwoma punktami w 2D (x1, y1) (x2, y2) wedlug wzoru sqrt((x1-x2)^2 + (y1-y2)^2).
     *
     * @param x1 wspolrzedna x pierwszego punktu.
     * @param y1 wspolrzedna y pierwszego punktu.
     * @param x2 wspolrzedna x drugiego punktu.
     * @param y2 wspolrzedna y drugiego punktu.
     * @return odleglosc pomiedzy punktami.
     */
    protected double distanceBetweenPoints(double x1, double y1, double x2, double y2) {
        return Math.sqrt(Math.pow((x1 - x2), 2) + Math.pow((y1 - y2), 2));
    }

    /**
     * Funkcja ma za zadanie pobrac nazwy przystankow z bazy danych i dodac je jako podpowiedzi do obiektu AutoCompleteTextView busStopsName.
     */
    protected void setBusStopsNameAdapter() {
        ArrayAdapter<String> adapter;

        List<String> busStopsNames = new ArrayList<String>(prefs.getStringSet(getString(R.string.sp_bus_stops_names), null));
        if (busStopsNames.isEmpty())
            adapter = new ArrayAdapter<String>(this, android.R.layout.simple_list_item_1, db.getAllBusStopsNames());
        else {
            Collections.sort(busStopsNames);
            adapter = new ArrayAdapter<String>(this, android.R.layout.simple_list_item_1, busStopsNames);
        }
        busStopsName.setAdapter(adapter);
        busStopsName.setThreshold(1);
    }

    /**
     * Funkcja ustawia akcje, ktora ma zostac wykonana po zmianie nazwy przystanku.
     * Po zmianie nazwy przystanku zmienia sie lista linii oraz ustawione zostaja przystanki koncowe.
     */
    protected void setBusStopsNameListeners() {
        busStopsName.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
                String busStopName = busStopsName.getText().toString();
                int busStopId = db.getBusStopId(busStopName);
                setLineNameSpinnerAdapter(busStopId);
                setDirectionSpinnerAdapter();
            }
        });
    }

    /**
     * Funkcja ustawia akcje, ktora ma zostac wykonana po zmianie nazwy linii.
     * Po zmianie nazwy linii zmieniane sa dostepne przystanki koncowe.
     */
    protected void setLineNameSpinnerListeners() {
        lineNameSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> adapterView, View view, int i, long l) {
                setDirectionSpinnerAdapter();
            }

            @Override
            public void onNothingSelected(AdapterView<?> adapterView) {

            }
        });
    }

    /**
     * Funkcja ustawia akcje, ktora jest wykonywana po nacisnieciu przycisku refresh.
     * Ustawiany jest najblizszy przystanek jako aktualny przystanek.
     */
    protected void setRefreshButtonListeners() {
        refreshButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                setNearestBusStopAsCurrentBusStop();
            }
        });
    }

    /**
     * Funkcja sprawdza czy jest mozliwosc dostepu do internetu.
     *
     * @param context kontekst aplikacji.
     * @return true jesli jest polaczenie z internetem. W przeciwnym razie false.
     */
    protected boolean isInternetConnection(Context context) {
        ConnectivityManager connectivityManager = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
        if (connectivityManager.getActiveNetworkInfo() != null && connectivityManager.getActiveNetworkInfo().isAvailable() && connectivityManager.getActiveNetworkInfo().isConnected())
            return true;
        else
            return false;
    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.main, menu);

        boolean autoLocation = prefs.getBoolean(getString(R.string.sp_auto_location), true);
        if (!prefs.contains(getString(R.string.sp_auto_location)))
            prefs.edit().putBoolean(getString(R.string.sp_auto_location), autoLocation).commit();

        menu.findItem(R.id.auto_location).setChecked(autoLocation);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();
        if (id == R.id.auto_location) {
            prefs.edit().putBoolean(getString(R.string.sp_auto_location), !item.isChecked()).commit();
            item.setChecked(!item.isChecked());

            if (item.isChecked())
                setNearestBusStopAsCurrentBusStop();

            return true;
        }

        return super.onOptionsItemSelected(item);
    }
}
