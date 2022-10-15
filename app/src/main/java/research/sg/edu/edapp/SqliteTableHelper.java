package research.sg.edu.edapp;

import android.content.ContentValues;
import android.content.Context;
import android.content.res.Resources;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

public class SqliteTableHelper extends SQLiteOpenHelper {
    // single database contain multiple table (table name contain all table names)
    private static final String[] table_name=new String[] {"Happy","Sad","Stressed","Relaxed"};
    private static final String DB_NAME = "EmoTextdb";
    private static Context fContext;
    // below int is our database version
    private static final int DB_VERSION =4;
    private String TABLE_NAME="";
    private static final String EMO_TEXT="EmoText";
    // below variable is for our id column.
    //private static final String ID_COL = "id";
    // creating a constructor for our database handler.
    public SqliteTableHelper(Context context) {
        super(context, DB_NAME, null, DB_VERSION);
        fContext=context;
    }
    @Override
    public void onCreate(SQLiteDatabase db) {
        String[] emoArray;
        String query;

       for(int i=0;i<table_name.length;i++)
       {
         TABLE_NAME=table_name[i];
         //query="CREATE TABLE "+ TABLE_NAME +" (" +EMO_TEXT+" TEXT)";
         db.execSQL("CREATE TABLE "+ table_name[i] +" (" +EMO_TEXT+" TEXT)");
         System.out.println("Table is created:"+TABLE_NAME);
         ContentValues values = new ContentValues();
         Resources res = fContext.getResources();
         if(TABLE_NAME=="Happy") {
             emoArray = res.getStringArray(R.array.happy_text);
             System.out.println("Happy Table is populated");
         }
          else
              if(TABLE_NAME=="Sad") {
                  emoArray = res.getStringArray(R.array.sad_text);
                  System.out.println("Sad Table is populated");
              }
              else
                if(TABLE_NAME=="Stressed") {
                    emoArray = res.getStringArray(R.array.stress_text);
                    System.out.println("Stressed Table is populated");
                }
                else {
                    emoArray = res.getStringArray(R.array.relax_text);
                    System.out.println("Relaxed Table is populated");
                }

           for (String item : emoArray) {
               values.put(EMO_TEXT,item);
               db.insert(TABLE_NAME,null,values);
           }
       }
    }
    public String readText(String TABLE_NAME, int pos){
        SQLiteDatabase db=this.getReadableDatabase();
        Cursor cursorText=db.rawQuery("SELECT * FROM "+ TABLE_NAME, null);
        String readSen="";
        if (cursorText.moveToPosition(pos)){
            readSen=cursorText.getString(0);
        }
        return readSen;
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {

    }
}
