package research.sg.edu.edapp;

import android.content.ContentProvider;
import android.content.ContentUris;
import android.content.ContentValues;
import android.content.Context;
import android.content.UriMatcher;
import android.database.Cursor;
import android.database.SQLException;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.database.sqlite.SQLiteQueryBuilder;
import android.net.Uri;

import java.util.HashMap;

import research.sg.edu.edapp.FinalClasses.FeaturesDetails;

public class TestFeaturesProvider extends ContentProvider {

    static final String PROVIDER_NAME = "research.sg.edu.edapp.TestFeaturesProvider";
    static final String URL = "content://" + PROVIDER_NAME + "/EmoTestFeatures";
    static final Uri CONTENT_URI = Uri.parse(URL);

    static final String id = "id";
    static final String name = "name";
    static final int uriCode = 1;
    static final UriMatcher uriMatcher;
    private static HashMap<String, String> values;
    static {
        uriMatcher = new UriMatcher(UriMatcher.NO_MATCH);
        uriMatcher.addURI(PROVIDER_NAME, "EmoTestFeatures", uriCode);
        uriMatcher.addURI(PROVIDER_NAME, "EmoTestFeatures/*", uriCode);
    }

    @Override
    public int delete(Uri uri, String selection, String[] selectionArgs) {
        int count = 0;
        switch (uriMatcher.match(uri)) {
            case uriCode:
                count = db.delete(FeaturesDetails.FeaturesEntry.TABLE_NAME, selection, selectionArgs);
                break;
            default:
                throw new IllegalArgumentException("Unknown URI " + uri);
        }
        getContext().getContentResolver().notifyChange(uri, null);
        return count;
    }

    @Override
    public String getType(Uri uri) {
        switch (uriMatcher.match(uri)) {
            case uriCode:
                return "vnd.android.cursor.dir/EmoTestFeatures";

            default:
                throw new IllegalArgumentException("Unsupported URI: " + uri);
        }
    }

    @Override
    public Uri insert(Uri uri, ContentValues values) {
        long rowID = db.insert(FeaturesDetails.FeaturesEntry.TABLE_NAME, "", values);
        if (rowID > 0) {
            Uri _uri = ContentUris.withAppendedId(CONTENT_URI, rowID);
            getContext().getContentResolver().notifyChange(_uri, null);
            return _uri;
        }
        throw new SQLException("Failed to add a record into " + uri);
    }

    @Override
    public boolean onCreate() {
        Context context = getContext();
        DBHelper dbHelper = new DBHelper(context);
        db = dbHelper.getWritableDatabase();
        return db != null;
    }

    @Override
    public Cursor query(Uri uri, String[] projection, String selection,
                        String[] selectionArgs, String sortOrder) {
        SQLiteQueryBuilder qb = new SQLiteQueryBuilder();
        qb.setTables(FeaturesDetails.FeaturesEntry.TABLE_NAME);

        switch (uriMatcher.match(uri)) {
            case uriCode:
                qb.setProjectionMap(values);
                break;
            default:
                throw new IllegalArgumentException("Unknown URI " + uri);
        }
        if (sortOrder == null || sortOrder == "") {
            sortOrder = FeaturesDetails.FeaturesEntry.TIMESTAMP;
        }
        Cursor c = qb.query(db, projection, selection, selectionArgs, null,
                null, sortOrder);
        c.setNotificationUri(getContext().getContentResolver(), uri);
        return c;
    }

    @Override
    public int update(Uri uri, ContentValues values, String selection,
                      String[] selectionArgs) {
        return 0;
    }

    private SQLiteDatabase db;

    public class DBHelper extends SQLiteOpenHelper {
        static final int DATABASE_VERSION = 1;

        public static final String DATABASE_NAME = "EmoTestFeatures.db";
        private static final String TEXT_TYPE = " TEXT";
        private static final String INT_TYPE = " INTEGER";
        private static final String REAL_TYPE = " REAL";
        //private static final String CHAR_TYPE = " CHAR";
        private static final String COMMA_SEP = ",";


        DBHelper(Context context) {
            super(context, DATABASE_NAME, null, DATABASE_VERSION);
        }

        @Override
        public void onCreate(SQLiteDatabase db) {

            // TODO MINE F
            String SQL_CREATE_ENTRIES =
                    "CREATE TABLE " + FeaturesDetails.FeaturesEntry.TABLE_NAME + " (" +
                            FeaturesDetails.FeaturesEntry._ID + " INTEGER PRIMARY KEY," +
                            FeaturesDetails.FeaturesEntry.SESSIONID + INT_TYPE + COMMA_SEP +
                            FeaturesDetails.FeaturesEntry.MSI + REAL_TYPE + COMMA_SEP +
                            FeaturesDetails.FeaturesEntry.RMSI + REAL_TYPE + COMMA_SEP +
                            FeaturesDetails.FeaturesEntry.SESSIONLEN + INT_TYPE + COMMA_SEP +
                            FeaturesDetails.FeaturesEntry.BACKSPACEPER + REAL_TYPE + COMMA_SEP +
                            FeaturesDetails.FeaturesEntry.SPLCHARPER + REAL_TYPE + COMMA_SEP +
                            FeaturesDetails.FeaturesEntry.SESSIONDUR + REAL_TYPE + COMMA_SEP +
                            FeaturesDetails.FeaturesEntry.APP_NAME + TEXT_TYPE + COMMA_SEP +
                            FeaturesDetails.FeaturesEntry.TIMESTAMP + TEXT_TYPE + COMMA_SEP +
                            FeaturesDetails.FeaturesEntry.EMOTION + INT_TYPE +
                            /*
                            FeaturesDetails.FeaturesEntry.PRESSURE + REAL_TYPE + COMMA_SEP +
                            FeaturesDetails.FeaturesEntry.VELOCITY + REAL_TYPE + COMMA_SEP +
                            FeaturesDetails.FeaturesEntry.SWIPEDURATION + REAL_TYPE + COMMA_SEP +
                            */
                            " )";

            db.execSQL(SQL_CREATE_ENTRIES);
            System.out.println("Test Features Table created successfully");
        }

        @Override
        public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
            String SQL_DELETE_TABLE = "DROP TABLE IF EXISTS " + FeaturesDetails.FeaturesEntry.TABLE_NAME;
            db.execSQL(SQL_DELETE_TABLE);
            onCreate(db);
        }
    }
}