package be.rebookit.vandeboschremy.re_book_it;

import android.content.Intent;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v4.app.ShareCompat;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;

/**
 * Created by Vandebosch Remy on 21/02/2018.
 */

public class DetailActivity extends AppCompatActivity{

    private int pos;
    private ImageView bookImage;
    private Cursor cursor;
    private TextView title,subtitle,authors,edition,courses,isbn,institutions,price,count,quality;
    private Button buyButton;

    /**
     * The method that gets called when the activity is started.
     * @param savedInstanceState The instance state that is saved.
     */
    public void onCreate(Bundle savedInstanceState){
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_detail);

        Intent intent = getIntent();

        // get the position of the adapter from the intent if it is available.
        if(intent.hasExtra(this.getString(R.string.row_id_key))){
            pos = intent.getIntExtra(this.getString(R.string.row_id_key), 0);
        }

        // get the query that is searched by if it is available.
        if(intent.hasExtra(this.getString(R.string.query_key))){
            String query = intent.getStringExtra(this.getString(R.string.query_key));
            String searchBy = intent.getStringExtra(this.getString(R.string.searhedBy_key)); // get the state of the spinner.
            if(query != null){
                cursor = DatabaseUtils.getCursorFromDBySearch(query, searchBy);
            }
            else{
                cursor = DatabaseUtils.getCursorFromDB(this);
            }
        }

        bookImage = (ImageView) findViewById(R.id.bookImage);
        bookImage.setImageResource(R.drawable.default_image);

        title = (TextView) findViewById(R.id.tv_TitleDetail);
        subtitle = (TextView) findViewById(R.id.tv_subtitle);
        authors = (TextView) findViewById(R.id.tv_authors);
        edition = (TextView) findViewById(R.id.tv_edition);
        courses = (TextView) findViewById(R.id.tv_courses);
        isbn = (TextView) findViewById(R.id.tv_ISBN);
        institutions = (TextView) findViewById(R.id.tv_institutions);
        price = (TextView) findViewById(R.id.tv_price);
        count = (TextView) findViewById(R.id.tv_count);
        quality = (TextView) findViewById(R.id.tv_quality);
        buyButton = (Button) findViewById(R.id.buy_button);

        // set on onlick listener for the button to buy a book.
        buyButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Uri webpage = Uri.parse("https://rebookit.be/login");
                Intent intent = new Intent(Intent.ACTION_VIEW, webpage);
                if (intent.resolveActivity(getPackageManager()) != null) {
                    startActivity(intent);
                }
            }
        });

        showData(pos);
    }

    /**
     * Display the data in the activity.
     * @param pos The position of the adapter.
     */
    public void showData(int pos){
        cursor.moveToPosition(pos); // move the cursor to the position.

        // set the title of the activity to the title of the book
        setTitle(cursor.getString(cursor.getColumnIndex(BookDataSheet.DataTable.COLUMN_NAME_TITLE)));

        // Set the correct text to the correct fields.
        title.setText(cursor.getString(cursor.getColumnIndex(BookDataSheet.DataTable.COLUMN_NAME_TITLE)));
        if(!cursor.getString(cursor.getColumnIndex(BookDataSheet.DataTable.COLUMN_NAME_SUBTITLE)).equals("")){
            subtitle.setText(cursor.getString(cursor.getColumnIndex(BookDataSheet.DataTable.COLUMN_NAME_SUBTITLE)));
        }
        else{
            subtitle.setText(this.getString(R.string.nothing_found));
        }

        String authorString = cursor.getString(cursor.getColumnIndex(BookDataSheet.DataTable.COLUMN_NAME_AUTHORS));
        authorString = authorString.replace("[","").replace("]","");
        authors.setText(this.getString(R.string.author_detail) + authorString);

        edition.setText(cursor.getString(cursor.getColumnIndex(BookDataSheet.DataTable.COLUMN_NAME_EDITION)));
        if(!cursor.getString(cursor.getColumnIndex(BookDataSheet.DataTable.COLUMN_NAME_COURSES)).equals("[]")){
            String coursesString = cursor.getString(cursor.getColumnIndex(BookDataSheet.DataTable.COLUMN_NAME_COURSES));
            coursesString = coursesString.replace("]", "").replace("[", "");
            courses.setText(this.getString(R.string.courses_detail) + coursesString);

            String institutionsString = cursor.getString(cursor.getColumnIndex(BookDataSheet.DataTable.COLUMN_NAME_INSTITUTIONS));
            institutionsString = institutionsString.replace("]","").replace("[","");
            institutions.setText(this.getString(R.string.university_detail) + institutionsString);
        }
        else{
            courses.setText(this.getString(R.string.no_courses_found));
            institutions.setText(this.getString(R.string.no_univerities_found));
        }
        isbn.setText(this.getString(R.string.isbn_detail) + cursor.getString(cursor.getColumnIndex(BookDataSheet.DataTable.COLUMN_NAME_ISBN)));
        price.setText(cursor.getString(cursor.getColumnIndex(BookDataSheet.DataTable.COLUMN_NAME_PRICE))+"€");
        count.setText(this.getString(R.string.count_detail) + cursor.getString(cursor.getColumnIndex(BookDataSheet.DataTable.COLUMN_NAME_COUNT)));
        quality.setText(this.getString(R.string.quality_detail) + cursor.getString(cursor.getColumnIndex(BookDataSheet.DataTable.COLUMN_NAME_QUALITY)) +"%");

        String url = cursor.getString(cursor.getColumnIndex(BookDataSheet.DataTable.COLUMN_NAME_IMAGEURL));
        Log.i("DetailAcitivy", url);

        new GetImage().execute("https://rebookit.be/" + url);
    }

    /**
     * Create the actionbar buttons.
     * @param menu The menu.
     * @return A boolean if the creation is successful.
     */
    @Override
    public boolean onCreateOptionsMenu(Menu menu){
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.detail, menu);
        return true;
    }

    /**
     * Gets triggered when the share button in the actionbar is clicked.
     * @param item The item that was clicked.
     */
    public void share(MenuItem item){
        String mimeType = "text/plain";
        String text = this.getString(R.string.share_begin)
                        +this.title.getText() + "\n"
                        +this.authors.getText() + "\n"
                        +this.isbn.getText() + "\n"
                        +this.quality.getText() + "\n"
                        +this.getString(R.string.share_end);
        Intent intent = ShareCompat.IntentBuilder.from(this)
                                                    .setChooserTitle(this.getString(R.string.share_title))
                                                    .setType(mimeType)
                                                    .setText(text)
                                                    .getIntent();
        if(intent.resolveActivity(getPackageManager()) != null){
            startActivity(intent);
        }
    }

    /**
     * this asynctask downloads the image of the book.
     */
    public class GetImage extends AsyncTask<String, Void, Bitmap>{

        /**
         * Download the image of the book.
         * @param urls The url of the image.
         * @return A bitmap that is the downloaded image.
         */
        @Override
        protected Bitmap doInBackground(String... urls) {
            try {
                Uri uri = Uri.parse(urls[0]);
                URL url = new URL(uri.toString());
                HttpURLConnection conn = (HttpURLConnection) url.openConnection();
                InputStream in = conn.getInputStream();
                return BitmapFactory.decodeStream(in);
            } catch (MalformedURLException e) {
                e.printStackTrace();
            } catch (IOException e) {
                e.printStackTrace();
            }
            return null;
        }

        /**
         * Set the bitmap to the imageview to display the image.
         * @param bitmap
         */
        @Override
        protected void onPostExecute(Bitmap bitmap){
            if(bitmap == null) return;
            else{
                Log.i("DetailActivity", "image loaded");
                bookImage.setImageBitmap(bitmap);
            }

        }
    }
}
