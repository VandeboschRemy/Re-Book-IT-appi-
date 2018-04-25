package be.rebookit.vandeboschremy.re_book_it;

import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;

/**
 * The SettingsActivity lets the user filter the list of books in the MainActivity by the maximum price
 * and the way that the list is sorted.
 * Created by Vandebosch Remy on 21/03/2018.
 */

public class SettingsActivity extends AppCompatActivity {

    /**
     * This method gets called when the activity is created.
     * @param savedInstanceState The instance state that is saved.
     */
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_settings);
    }
}
