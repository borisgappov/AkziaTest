package com.example.gallery;

import java.util.ArrayList;
import com.example.gallery.Model.GalleryItem;
import com.example.gallery.Model.GalleryResult;
import com.google.gson.Gson;
import com.innahema.collections.query.queriables.Queryable;

import android.app.Activity;
import android.os.Bundle;
import android.os.Handler;
import android.view.View;
import android.widget.AbsListView;
import android.widget.ListView;
import android.widget.AbsListView.OnScrollListener;

public class MainActivity extends Activity implements TaskListener {

	private Handler handler = new Handler();
	private ArrayList<GalleryItem> listViewItems = new ArrayList<GalleryItem>();
	CustomListAdapter adapter;
	private MainActivity me = this;
	private Gson gson = new Gson();
	private int currentPage = 0;
	private int savedPosition = 0;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);    
		ListView lvMain = (ListView) findViewById(R.id.lvMain);
		adapter = new CustomListAdapter(this, listViewItems);
	    lvMain.setAdapter(adapter);
	    lvMain.setOnScrollListener(new OnScrollListener() {
	        
	    	public void onScrollStateChanged(AbsListView view, int scrollState) {
	        
	    	}
	        
	        public void onScroll(AbsListView view, int firstVisibleItem, int visibleItemCount, int totalItemCount) {
	        	if(firstVisibleItem > 0 && visibleItemCount == (totalItemCount - firstVisibleItem)){
		        	  findViewById(R.id.loadingIndicator).setVisibility(View.VISIBLE);
		        	  DownloadNextPage();	        		
	        	}       	
	        }
	      });
	    DownloadNextPage();
	}
	
	static final String STATE_SCROLL_POSITION = "scrollPosition";

	@Override
	public void onSaveInstanceState(Bundle savedInstanceState) {
	    int currentPosition = ((ListView) findViewById(R.id.lvMain)).getFirstVisiblePosition();
	    savedInstanceState.putInt(STATE_SCROLL_POSITION, currentPosition);
	    super.onSaveInstanceState(savedInstanceState);
	}

	public void onRestoreInstanceState(Bundle savedInstanceState) {
	    super.onRestoreInstanceState(savedInstanceState);
	    savedPosition = savedInstanceState.getInt(STATE_SCROLL_POSITION);
	}	
	
	@Override
	public void onTaskCompleted(TaskType taskType, Object result, Object data) {
		
		if(taskType == TaskType.GALLERY) {
			
			findViewById(R.id.loadingIndicator).setVisibility(View.GONE);		

			listViewItems.addAll(Queryable
					.from(gson.fromJson(result.toString(), GalleryResult.class).data)
	                .filter(p->p.is_album == false)
	                .toList());
			
			adapter.notifyDataSetChanged();
			
			if(savedPosition > 0){
				((ListView) findViewById(R.id.lvMain)).setSelection(savedPosition);
				savedPosition = 0;
			}
		}
		
	}
	
	void DownloadNextPage(){
		handler.post(new ImgurRunnable( me, TaskType.GALLERY,
				String.format("https://api.imgur.com/3/gallery/hot/top/all/%d?showViral=true&perPage=%d", 
						currentPage++,
						Common.GALLERY_PER_PAGE_COUNT), 
				null));
	}
	
}
