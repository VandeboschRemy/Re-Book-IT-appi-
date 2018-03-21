package be.rebookit.vandeboschremy.re_book_it;

import android.provider.BaseColumns;

/**
 * Created by Vandebosch Remy on 7/03/2018.
 */

public final class BookDataSheet {

    private BookDataSheet(){}

    //create contruct of data table
    public static class DataTable implements BaseColumns {
        public static final String TABLE_NAME = "bookData";
        public static final String COLUMN_NAME_TITLE ="title";
        public static final String COLUMN_NAME_SUBTITLE = "subtitle";
        public static final String COLUMN_NAME_COUNT = "count";
        public static final String COLUMN_NAME_AUTHORS = "authors";
        public static final String COLUMN_NAME_COURSES = "courses";
        public static final String COLUMN_NAME_COURSESFULLNAME = "coursesFullName";
        public static final String COLUMN_NAME_PRICE = "price";
        public static final String COLUMN_NAME_CREATEDAT = "createdAt";
        public static final String COLUMN_NAME_INSTITUTIONS = "institutions";
        public static final String COLUMN_NAME_IMAGEURL = "imageUrl";
        public static final String COLUMN_NAME_QUALITY = "quality";
        public static final String COLUMN_NAME_EDITION = "edition";
        public static final String COLUMN_NAME_ISBN = "isbn";
    }
}
