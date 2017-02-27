package com.example.gallery;

import java.io.BufferedReader;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;

import android.os.AsyncTask;
import android.util.Log;


public class ImgurRunnable implements Runnable {
	
	TaskListener taskListener;
	TaskType taskType;
	String url;
	Object data;

	AsyncTask task = new AsyncTask(){
		@Override
		protected Object doInBackground(Object... parameters) {
			Object result = null;
			HttpURLConnection connection = null;
			try {
				connection =(HttpURLConnection)new URL(url).openConnection();				
				connection.setRequestMethod("GET");
				connection.setConnectTimeout(Common.REQUEST_TIMEOUT);
				connection.setDoInput(true);
				connection.setRequestProperty("Authorization", "Client-ID " + Common.CLIENT_ID);
				connection.connect();
				InputStream is = connection.getInputStream();
				switch(taskType){
					case GALLERY:					    
					    BufferedReader aReader=new java.io.BufferedReader(new java.io.InputStreamReader(is));
					    StringBuffer aResponse=new StringBuffer();
					    String aLine = aReader.readLine();
					    while (aLine != null) {
					    	aResponse.append(aLine);
					    	aLine=aReader.readLine();
					    }
					    result = aResponse;
					    break;
					case IMAGE:					
						int imageLength = connection.getContentLength();
						byte[] bytes = new byte[imageLength];
						int bytesRead = 0;
						while (bytesRead < imageLength) {
						    int n = is.read(bytes, bytesRead, imageLength - bytesRead);
						    bytesRead += n;
						}
						result = bytes;
						break;
				}
				is.close();
			} catch(Exception e){
				Log.d(Common.LOG_TAG, e.getMessage());
			} finally {
				if(connection != null) connection.disconnect();
			}
			return result;
		}
		
	    protected void onPostExecute(Object result){
	    	taskListener.onTaskCompleted(taskType, result, data);
	    } 		
	};
	
	public ImgurRunnable(TaskListener taskListener, TaskType taskType, String url, Object data) {
		super();
		this.taskType = taskType;
		this.url = url;
		this.data = data;
		this.taskListener = taskListener;
	}

	@Override
	public void run() {
		task.execute();
	}

}
