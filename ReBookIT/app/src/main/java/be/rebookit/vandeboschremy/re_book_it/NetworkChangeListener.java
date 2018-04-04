package be.rebookit.vandeboschremy.re_book_it;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.AsyncTask;
import android.widget.Toast;

/**
 * Created by Vandebosch Remy on 4/04/2018.
 */

public class NetworkChangeListener extends BroadcastReceiver {

    private static boolean startedFlag;

    @Override
    public void onReceive(Context context, Intent intent) {
        ConnectivityManager cm = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo info = cm.getActiveNetworkInfo();
        if(info != null && !MainActivity.getUpdatedFlag() && !MainActivity.getDownloaderStartedFlag()){
            new MainActivity.Downloader().executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR, "https://rebookit.be/search");
        }
        else if(info == null && !MainActivity.getUpdatedFlag() && !startedFlag){
            startedFlag = true;
            Toast toast = Toast.makeText(context, "Turn on internet to update content !!", Toast.LENGTH_LONG);
            toast.show();
        }
    }
}
