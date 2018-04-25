package be.rebookit.vandeboschremy.re_book_it;

import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;

/**
 * The InfoActivity show the user what the smileys mean and how things work at Re-Book IT.
 * Created by Vandebosch Remy on 28/03/2018.
 */

public class InfoActivity extends AppCompatActivity {

    /**
     * This method gets called when the activity is created.
     * @param savedInstanceState The instance state that is saved.
     */
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_info);
    }
}
