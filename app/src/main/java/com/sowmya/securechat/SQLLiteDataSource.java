package com.sowmya.securechat;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import java.sql.Blob;
import java.util.ArrayList;
import java.util.Vector;

/**
 * Created by mohan on 29/5/17.
 */

public class SQLLiteDataSource extends SQLiteOpenHelper {

    private static final String DATABASE_NAME = "secure_chat";

    private static final String TABLE_MESSAGES = "table_messages";

    private static final String TABLE_CONVO = "table_convos";

    private static final int DATABASE_VERSION = 1;

    private static final String CONVERSATION_ID = "CONVERSATION_ID";
    private static final String MESSAGE = "MESSAGE";
    //private static final String RECIEVER = "RECIEVER";

    public SQLLiteDataSource(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {

        String CREATE_TABLE_CONVO = "CREATE TABLE " + TABLE_CONVO + " ( "+CONVERSATION_ID+" TEXT PRIMARY KEY"+")";
        String CREATE_TABLE_MESSAGES = "CREATE TABLE " + TABLE_MESSAGES + " (" + CONVERSATION_ID + " TEXT, " + MESSAGE + " BLOB ,  FOREIGN KEY( "+ CONVERSATION_ID +" ) REFERENCES "+TABLE_CONVO+"( "+CONVERSATION_ID+" ))";
        db.execSQL(CREATE_TABLE_CONVO);
        db.execSQL(CREATE_TABLE_MESSAGES);

    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {

        /*db.execSQL("DROP TABLE "+TABLE_MESSAGES+" IF EXSITS ");
        onCreate(db);*/


    }
    public void addConversationToSQLLite(String conversation_id){
        Log.e("database","in insertion "+conversation_id+"   ***********8");
        SQLiteDatabase sqLiteDatabase = this.getWritableDatabase();
        ContentValues val = new ContentValues();
        val.put(CONVERSATION_ID,conversation_id);
        if(!checkConversationID(conversation_id))
            sqLiteDatabase.insert(TABLE_CONVO,null,val);
    }

    //CURD Operations
    public void addMessageToSQLLite(String conversation_id, Message message) {
        SQLiteDatabase sqLiteDatabase = this.getWritableDatabase();

        //inserting into table 1
        ContentValues val=new ContentValues();
        val.put(CONVERSATION_ID,conversation_id);
        if(!checkConversationID(conversation_id))
            sqLiteDatabase.insert(TABLE_CONVO,null,val);

        //inserting into table 2
        ContentValues values = new ContentValues();

        //GSON Conversion
        Gson gson = new Gson();

        values.put(CONVERSATION_ID, conversation_id);
        values.put(MESSAGE, gson.toJson(message).getBytes());

        //insert row
        long result= sqLiteDatabase.insert(TABLE_MESSAGES, null, values);


        Log.e("SQLLite:- ","Is insterted = "+String.valueOf(result));
        sqLiteDatabase.close();
    }

    public boolean checkConversationID(String convo_id){

        int x;
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor cursor = db.rawQuery("SELECT "+CONVERSATION_ID+" FROM " + TABLE_CONVO + " WHERE " + CONVERSATION_ID + " = '" +convo_id +"'",null);
        if((x=cursor.getCount()) == 1) {
            Log.e("Database","*********"+x+"*******con*****"+convo_id);
            return true;
        }
        Log.e("Database","*********"+x+"*******con*****"+convo_id);
        return false;
    }

    public Vector<Message> getMessageFromConversationID(String conversation_id) {


        SQLiteDatabase db = this.getReadableDatabase();

        Vector<Message> vector=new Vector<>();

        Cursor cursor = db.rawQuery("SELECT MESSAGE FROM " + TABLE_MESSAGES + " WHERE " + CONVERSATION_ID + " = '" +conversation_id + "'", null);
        if(cursor.getCount() != 0) {
            while (cursor.moveToNext()) {
                byte blob[] = cursor.getBlob(0);

                String s = new String(blob);

                Message message = new Gson().fromJson(s, new TypeToken<Message>() {
                }.getType());

                vector.add(message);

                return vector;
            }
        }
        return null;
    }

    public Vector<String> getAllConversationIds(){
        Log.e("Database","=======in get all conversation ***********");
        Vector<String> list = new Vector<>();
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor cursor = db.rawQuery("SELECT " + CONVERSATION_ID +" FROM " +TABLE_CONVO,null);
        if(cursor.getCount() != 0) {
            while (cursor.moveToNext()) {
                String s = cursor.getString(0);
                Log.e("Database","++++++++++++"+s+"============");
                list.add(s);
            }
        }
        return list;
    }

    public Cursor getAllMessages() {
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor cursor = db.rawQuery("SELECT * FROM " + TABLE_MESSAGES, null);
        if (cursor!=null)
            return cursor;
        return null;
    }



}
