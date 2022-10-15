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

import research.sg.edu.edapp.FinalClasses.StatsDetails;

public class StatsProvider extends ContentProvider {

    static final String PROVIDER_NAME = "research.sg.edu.edapp.StatsProvider";
    static final String URL = "content://" + PROVIDER_NAME + "/EmoStats";
    static final Uri CONTENT_URI = Uri.parse(URL);

    static final String id = "id";
    static final String name = "name";
    static final int uriCode = 1;
    static final UriMatcher uriMatcher;
    private static HashMap<String, String> values;
    static {
        uriMatcher = new UriMatcher(UriMatcher.NO_MATCH);
        uriMatcher.addURI(PROVIDER_NAME, "EmoStats", uriCode);
        uriMatcher.addURI(PROVIDER_NAME, "EmoStats/*", uriCode);
    }

    @Override
    public int delete(Uri uri, String selection, String[] selectionArgs) {
        int count = 0;
        switch (uriMatcher.match(uri)) {
            case uriCode:
                count = db.delete(StatsDetails.StatsEntry.TABLE_NAME, selection, selectionArgs);
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
                return "vnd.android.cursor.dir/EmoStats";

            default:
                throw new IllegalArgumentException("Unsupported URI: " + uri);
        }
    }

    @Override
    public Uri insert(Uri uri, ContentValues values) {
        long rowID = db.insert(StatsDetails.StatsEntry.TABLE_NAME, "", values);
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
        qb.setTables(StatsDetails.StatsEntry.TABLE_NAME);

        switch (uriMatcher.match(uri)) {
            case uriCode:
                qb.setProjectionMap(values);
                break;
            default:
                throw new IllegalArgumentException("Unknown URI " + uri);
        }
        if (sortOrder == null || sortOrder == "") {
            sortOrder = StatsDetails.StatsEntry.TIMESTAMP;
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

        public static final String DATABASE_NAME = "EmoStats.db";
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

            String SQL_CREATE_ENTRIES =
                    "CREATE TABLE " + StatsDetails.StatsEntry.TABLE_NAME + " (" +
                            StatsDetails.StatsEntry._ID + " INTEGER PRIMARY KEY," +
                            StatsDetails.StatsEntry.APP_NAME + TEXT_TYPE + COMMA_SEP +
                            StatsDetails.StatsEntry.TIMESTAMP + TEXT_TYPE + COMMA_SEP +
                            StatsDetails.StatsEntry.APP_CAT + INT_TYPE + COMMA_SEP +
                            StatsDetails.StatsEntry.WEEKEND + INT_TYPE + COMMA_SEP +
                            StatsDetails.StatsEntry.DAYSESSION + INT_TYPE + COMMA_SEP +
                            StatsDetails.StatsEntry.EMOTION + INT_TYPE +
                            " )";

            db.execSQL(SQL_CREATE_ENTRIES);
            System.out.println("Stats Table created successfully");
        }

        @Override
        public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
            String SQL_DELETE_TABLE = "DROP TABLE IF EXISTS " + StatsDetails.StatsEntry.TABLE_NAME;
            db.execSQL(SQL_DELETE_TABLE);
            onCreate(db);
        }
    }
}