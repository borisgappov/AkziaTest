package com.example.gallery;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

public class ImgurDBHelper extends SQLiteOpenHelper {

    public ImgurDBHelper(Context context) {
      super(context, "ImgurGalleryDB", null, 1);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
      db.execSQL("create table images ( id integer primary key autoincrement, imgurId text);");
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {

    }
  }