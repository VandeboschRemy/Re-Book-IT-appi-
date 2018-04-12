package be.rebookit.vandeboschremy.re_book_it;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.support.v7.app.AppCompatActivity;
import android.widget.TextView;

/**
 * Created by Vandebosch Remy on 12/04/2018.
 */

public class SplashScreen extends AppCompatActivity {

    TextView splashScreenTv;

    @Override
    /**
     * The method that is called when this activity is created
     */
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_splash_screen);

        splashScreenTv = (TextView) findViewById(R.id.splash_screen_tv);

        new Handler().postDelayed(new Runnable() {
            @Override
            public void run() {
                splashScreenTv.setText("Re");
            }
        }, 1000);

        new Handler().postDelayed(new Runnable() {
            @Override
            public void run() {
                splashScreenTv.setText("Re-Book");
            }
        }, 2000);

        new Handler().postDelayed(new Runnable() {
            @Override
            public void run() {
                splashScreenTv.setText("Re-Book IT");
            }
        }, 3000);

        new Handler().postDelayed(new Runnable() {
            @Override
            public void run() {
                Intent intent = new Intent(SplashScreen.this, MainActivity.class);
                SplashScreen.this.startActivity(intent);
                SplashScreen.this.finish();
            }
        }, 3000);
    }
}
