package pl.edu.agh.sm.whereisthatbus.app;

import android.app.Activity;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.Toast;

import java.text.SimpleDateFormat;
import java.util.Date;


public class DisplayActivity extends Activity {
    String busStop, lineName, lineDirection;
    String lastSeen;
    DataBaseRepository db;
    SimpleDateFormat sdf = new SimpleDateFormat("HH:mm");
    String currentTime = sdf.format(new Date());

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_display);

        Bundle extras = getIntent().getExtras();
        if (extras != null){
            busStop = extras.getString("busStop");
            lineName = extras.getString("lineNumber");
            lineDirection = extras.getString("lineDirection");
            lastSeen = extras.getString("lastSeen");
            if (lastSeen == null){
                Toast.makeText(getApplicationContext(),"Niestety brak informacji od uzytkownikow", Toast.LENGTH_LONG).show();
            }
        }

        db = new DataBaseRepository(this);

        Log.i("Zmienne:", busStop + lineName + lineDirection + lastSeen);
        //Sczytanie danych wyszukanie w bazie, wyświetlenie
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.display, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();
        if (id == R.id.action_settings) {
            return true;
        }
        return super.onOptionsItemSelected(item);
    }
}
