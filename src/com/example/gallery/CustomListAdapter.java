package com.example.gallery;

import java.util.List;

import com.example.gallery.Model.GalleryItem;

import android.app.Activity;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.graphics.BitmapFactory;
import android.os.Handler;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;

import java.io.ByteArrayOutputStream;
import java.io.FileOutputStream;
import java.io.InputStream;


public class CustomListAdapter extends ArrayAdapter<GalleryItem> implements TaskListener {

	private final Activity context;
	private final List<GalleryItem> items;
	private Handler handler = new Handler();
	private ImgurDBHelper idbHelper;
	private SQLiteDatabase db;
	private int tasksCount = 0;
	
	public CustomListAdapter(Activity context, List<GalleryItem> items) {
		super(context, R.layout.list_item, items);
		this.context=context;
		this.items=items;
		idbHelper = new ImgurDBHelper(context);
		db = idbHelper.getWritableDatabase();
	}
	
	public View getView(int position, View view, ViewGroup parent) {
		LayoutInflater inflater=context.getLayoutInflater();
		View rowView = inflater.inflate(R.layout.list_item, null, true);
		try{
			TextView txtTitle = (TextView) rowView.findViewById(R.id.item);
			ImageView imageView = (ImageView) rowView.findViewById(R.id.icon);
			ProgressBar progressBar = (ProgressBar)rowView.findViewById(R.id.progressBar); 
			GalleryItem currentItem = items.get(position);
			txtTitle.setText(currentItem.title);
			byte[] imageBytes = ExistsInDatabase(currentItem.id) ? readFile(currentItem.id) : null;
			if(imageBytes == null){
				if(tasksCount < Common.MAX_ASYNC_TASKS_COUNT){
					handler.post(new ImgurRunnable( this, TaskType.IMAGE, 
							new StringBuilder(currentItem.link).insert(currentItem.link.lastIndexOf("."), "m").toString(), 
							new Object[]{ imageView, progressBar, currentItem.id } ));
					tasksCount++;
				}
			} else {
				progressBar.setVisibility(View.GONE);
				imageView.setImageBitmap(BitmapFactory.decodeByteArray(imageBytes, 0, imageBytes.length));
			}
		} catch(Exception ex) {
			ex.printStackTrace();
		}
		return rowView;
	}

	@Override
	public void onTaskCompleted(TaskType taskType, Object result, Object data) {
		try {
			tasksCount--;
			if(result == null) return;
			byte[] byteArray = (byte[])result;
			((ImageView)((Object[])data)[0]).setImageBitmap(BitmapFactory.decodeByteArray(byteArray, 0, byteArray.length));
			String imageId = ((Object[])data)[2].toString();
			((ProgressBar)((Object[])data)[1]).setVisibility(View.GONE);
			writeFile(imageId, byteArray);
			if(!ExistsInDatabase(imageId)){
				db.execSQL(String.format("insert into images (imgurId) values(\"%s\");", imageId));
			}
		} catch(Exception ex) {
			ex.printStackTrace();
		}
	};
	
	
	boolean ExistsInDatabase(String id){
		try {
			Cursor c = db.rawQuery(String.format("select count(*) from images where imgurId=\"%s\"", id), null);
			int count = c.moveToNext() ? c.getInt(0) : 0;
			c.close();
			return count > 0;
		} catch(Exception ex) {
			ex.printStackTrace();
		}
		return false;
	}
	
	void writeFile(String fileName, byte[] bytes) {
		try {
			FileOutputStream os = context.openFileOutput(fileName, context.MODE_WORLD_READABLE);
		    os.write(bytes);
		    os.close();
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	byte[] readFile(String fileName) {
		try {
			InputStream is = context.openFileInput(fileName);
			ByteArrayOutputStream bos = new ByteArrayOutputStream();
			byte[] b = new byte[1024];
			int bytesRead = 0;
			while ((bytesRead = is.read(b)) != -1) {
				bos.write(b, 0, bytesRead);
			}
			return bos.toByteArray();
		} catch (Exception e) {
			e.printStackTrace();
		}
		return null;
	}	
	
}
