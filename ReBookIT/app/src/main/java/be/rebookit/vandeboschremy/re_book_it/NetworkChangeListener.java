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

    /**
     * This method is triggered if a networkchange occurred.
     * @param context The context.
     * @param intent The intent that is send when a networkchange occurs.
     */
    @Override
    public void onReceive(Context context, Intent intent) {
        ConnectivityManager cm = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo info = cm.getActiveNetworkInfo();
        // If the network is connected and the content has not been updated or the downloader from the mainactivity has not started
        // start a download.
        if(info != null && !MainActivity.getUpdatedFlag() && !MainActivity.getDownloaderStartedFlag()){
            new MainActivity.Downloader().executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR, "https://rebookit.be/search");
        }
        // If the network is not connected tell the user that he needs an internet connection to update.
        else if(info == null && !MainActivity.getUpdatedFlag() && !startedFlag){
            startedFlag = true;
            Toast toast = Toast.makeText(context, context.getString(R.string.no_network), Toast.LENGTH_LONG);
            toast.show();
        }
    }
}
