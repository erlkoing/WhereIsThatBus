package pl.edu.agh.sm.whereisthatbus.app;

import java.util.Calendar;
import java.util.Date;

/**
 * Klasa zawiera funkcje manipulujace czasem na potrzeby aplikacji.
 */
public class TimeTool {
    /**
     * Funkcja zwraca "godzine" w minutach.
     *
     * @return zwraca liczbe minut, ktore uplynely od polnocy.
     */
    public int getActualDayTimeInMinutes() {
        Calendar calendar = Calendar.getInstance();
        int currentDayTime = 0;
        currentDayTime += 60 * calendar.get(Calendar.HOUR_OF_DAY);
        currentDayTime += calendar.get(Calendar.MINUTE);
        return currentDayTime;
    }

    /**
     * Funkcja zwraca znacznik okreslajacy dzien tygodnia danego dnia.
     *
     * @return "N" jesli dany dzien jest niedziela, "S" jesli dany dzien jest sobota lub "T" jesli dany dzien jest zwyklym dniem tygodnia.
     */
    public String getCurrentDayLabel() {
        Calendar calendar = Calendar.getInstance();
        return getDayOfTheWeekLabel(calendar.get(Calendar.DAY_OF_WEEK));
    }

    /**
     * Funkcja zwraca znacznik okreslajacy dzien tygodnia dla podanego dnia.
     *
     * @param dayOfTheWeek argument z przedzialu od 1-7 okreslajacy polozenie dnia tygodnia. Zaczynamy liczyc od niedzieli (1).
     * @return "N" jesli dany dzien jest niedziela, "S" jesli dany dzien jest sobota lub "T" jesli dany dzien jest zwyklym dniem tygodnia.
     */
    public String getDayOfTheWeekLabel(int dayOfTheWeek) {
        if (dayOfTheWeek == 1) return "N";
        else if (dayOfTheWeek == 7) return "S";
        else return "T";
    }

    /**
     * Funkcja na podstawie daty zamienia godzine z daty na liczbe minut, ktore uplynely od polnocy.
     *
     * @param date data, ktora konwertujemy.
     * @return liczba minut, ktore uplynely od polnocy.
     */
    public int convertDateToMinutest(Date date) {
        Calendar calendar = Calendar.getInstance();
        calendar.setTime(date);
        System.out.println(date);
        int hours = calendar.get(Calendar.HOUR_OF_DAY);
        int minutes = calendar.get(Calendar.MINUTE);

        int dayTimeMinutes = 0;
        dayTimeMinutes += 60 * hours;
        dayTimeMinutes += minutes;

        return dayTimeMinutes;
    }
}
