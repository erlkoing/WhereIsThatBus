package pl.edu.agh.sm.whereisthatbus.app;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;

import java.util.HashSet;
import java.util.Set;

/**
 * Poczatkowe activity, w ktorym wybieramy co bedziemy robic: czy zglaszac czy szukac
 */
public class MainActivity extends Activity {

    private Button search;
    private Button report;
    private SharedPreferences prefs;
    private DataBaseRepository db;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        db = new DataBaseRepository(this);

        // inicjalizacja SharedPreferences
        prefs = this.getSharedPreferences("pl.edu.agh.sm.whereisthatbus", Context.MODE_PRIVATE);

        // przypisanie elementow do obiektow
        search = (Button) findViewById(R.id.search);
        report = (Button) findViewById(R.id.report);

        // ustawienie listenerow dla przycikow
        setListeners();

        // zaladowanie nazw przystankow i linii do SharedPreferences
        loadBusStopsNames();
        loadLinesNames();
    }

    /**
     * Funkcja ustawia listenery dla przyciskow
     */
    private void setListeners() {
        // przycisniecie przycisku przypisanego do "search" spowoduje wywołanie  funkcji startSearchActivity()
        search.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                startSearchActivity();
            }
        });

        // przycisniecie przycisku przypisanego do "report" spowoduje wywołanie  funkcji startReportActivity()
        report.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                startReportActivity();
            }
        });
    }

    /**
     * funkcja laduje nazwy przystankow do sharedpreferences
     */
    private void loadBusStopsNames() {
        Set<String> busStopsNames = new HashSet<String>(db.getAllBusStopsNames());
        prefs.edit().putStringSet("bus stops names", busStopsNames).commit();
    }

    /**
     * funkcja laduje nazwy przystankow do sharedpreferences
     */
    private void loadLinesNames() {
        Set<String> lineNames = new HashSet<String>(db.getAllLineNames());
        prefs.edit().putStringSet("lines names", lineNames).commit();
    }

    /**
     * Funkcja uruchamia SearchArctivity
     */
    private void startSearchActivity() {
        Intent i = new Intent(this, SearchActivity.class);
        startActivity(i);
    }

    /**
     * Funkcja uruchamia ReportActivity
     */
    private void startReportActivity() {
        Intent i = new Intent(this, ReportActivity.class);
        startActivity(i);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.main, menu);

        boolean autoLocation = prefs.getBoolean("auto location", true);
        if (!prefs.contains("auto location"))
            prefs.edit().putBoolean("auto location", autoLocation).commit();

        menu.findItem(R.id.auto_location).setChecked(autoLocation);

        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();
        if (id == R.id.auto_location) {
            prefs.edit().putBoolean("auto location", !item.isChecked()).commit();
            item.setChecked(!item.isChecked());

            return true;
        }
        return super.onOptionsItemSelected(item);
    }
}
