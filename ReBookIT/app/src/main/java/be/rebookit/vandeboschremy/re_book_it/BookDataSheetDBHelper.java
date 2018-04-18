package be.rebookit.vandeboschremy.re_book_it;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

/**
 * Created by Vandebosch Remy on 7/03/2018.
 * The helper class that builds and updates database.
 */

public class BookDataSheetDBHelper extends SQLiteOpenHelper {
    private static final String SQL_CREATE_ENTRIES =
            "CREATE TABLE " + BookDataSheet.DataTable.TABLE_NAME + " (" +
                    BookDataSheet.DataTable._ID + " INTEGER PRIMARY KEY," +
                    BookDataSheet.DataTable.COLUMN_NAME_TITLE + " TEXT, " +
                    BookDataSheet.DataTable.COLUMN_NAME_SUBTITLE + " TEXT," +
                    BookDataSheet.DataTable.COLUMN_NAME_COUNT + " TEXT, " +
                    BookDataSheet.DataTable.COLUMN_NAME_AUTHORS + " TEXT, " +
                    BookDataSheet.DataTable.COLUMN_NAME_COURSES + " TEXT, " +
                    BookDataSheet.DataTable.COLUMN_NAME_COURSESFULLNAME + " TEXT, " +
                    BookDataSheet.DataTable.COLUMN_NAME_PRICE + " FLOAT, " +
                    BookDataSheet.DataTable.COLUMN_NAME_CREATEDAT + " TEXT, " +
                    BookDataSheet.DataTable.COLUMN_NAME_INSTITUTIONS + " TEXT, " +
                    BookDataSheet.DataTable.COLUMN_NAME_IMAGEURL + " TEXT, " +
                    BookDataSheet.DataTable.COLUMN_NAME_QUALITY + " TEXT, " +
                    BookDataSheet.DataTable.COLUMN_NAME_EDITION + " TEXT, " +
                    BookDataSheet.DataTable.COLUMN_NAME_ISBN + " TEXT) ";

    private static final String SQL_DELETE_ENTRIES =
            "DROP TABLE IF EXISTS " + BookDataSheet.DataTable.TABLE_NAME;


    public BookDataSheetDBHelper(Context context){
        super(context, "BookDataSheetDB", null, 1);
    }

    /**
     * Create a new datatable in the database.
     * @param db The database.
     */
    @Override
    public void onCreate(SQLiteDatabase db) {
        db.execSQL(SQL_CREATE_ENTRIES);
    }

    /**
     * upgrade the database by deleting the existing datatable and creating a new datatable.
     * @param db The database.
     * @param oldVersion The number of the old version of the database.
     * @param newVersion The number of the new version of the database.
     */
    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        db.execSQL(SQL_DELETE_ENTRIES);
        onCreate(db);
    }
}
