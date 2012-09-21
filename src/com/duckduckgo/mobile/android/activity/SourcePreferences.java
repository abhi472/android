package com.duckduckgo.mobile.android.activity;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

import android.app.Activity;
import android.os.Bundle;
import android.widget.ListView;

import com.duckduckgo.mobile.android.DDGApplication;
import com.duckduckgo.mobile.android.R;
import com.duckduckgo.mobile.android.adapters.SourcesAdapter;
import com.duckduckgo.mobile.android.container.SourcePreferencesContainer;
import com.duckduckgo.mobile.android.objects.SourcesObject;
import com.duckduckgo.mobile.android.tasks.SourcesTask;
import com.duckduckgo.mobile.android.tasks.SourcesTask.SourcesListener;
import com.duckduckgo.mobile.android.util.DDGControlVar;
import com.duckduckgo.mobile.android.util.DDGUtils;

public class SourcePreferences extends Activity implements SourcesListener {

	SourcePreferencesContainer sourcePrefContainer;
	
	private ListView sourcesView = null;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		
		setContentView(R.layout.sources);
		
		sourcePrefContainer = (SourcePreferencesContainer) getLastNonConfigurationInstance();
		if(sourcePrefContainer == null){
			sourcePrefContainer = new SourcePreferencesContainer();
			sourcePrefContainer.sourcesAdapter = new SourcesAdapter(this);
		}
		
		sourcesView = (ListView) findViewById(R.id.sourceItems);
		sourcesView.setAdapter(sourcePrefContainer.sourcesAdapter);
	}
	
	@Override
	protected void onResume() {
		super.onResume();
		
		sourcePrefContainer.sourcesTask = new SourcesTask(this);
		sourcePrefContainer.sourcesTask.execute();
	}

	public void onSourcesRetrieved(List<SourcesObject> feed) {
		
		// if using defaults, should repopulate source set with default list
		if(DDGControlVar.useDefaultSources){
			Set<String> sourceSet = new HashSet<String>();
			
			for(SourcesObject sobj : feed){
				if(sobj.getDefault() == 1)
					sourceSet.add(sobj.getId());
			}
			
			DDGUtils.saveSet(DDGApplication.getSharedPreferences(), sourceSet, "sourceset");
			
			// reset source set of underlying list adapter
			sourcePrefContainer.sourcesAdapter.sourceSet = sourceSet;
			sourcePrefContainer.sourcesAdapter.notifyDataSetChanged();
		}
		
		sourcePrefContainer.sourcesAdapter.setList(feed);
		sourcePrefContainer.sourcesAdapter.notifyDataSetChanged();
		
	}

	public void onSourcesRetrievalFailed() {
		//If the sourcesTask is null, we are currently paused
		//Otherwise, we can try again
		if (sourcePrefContainer.sourcesTask != null) {
			sourcePrefContainer.sourcesTask = new SourcesTask(this);
			sourcePrefContainer.sourcesTask.execute();
		}
		
	}
	
	@Override
    public Object onRetainNonConfigurationInstance() {
    	// return page container, holding all non-view data
    	return sourcePrefContainer;
    }
	
}