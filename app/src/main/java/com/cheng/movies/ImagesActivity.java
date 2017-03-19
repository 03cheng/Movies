package com.cheng.movies;

import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.text.TextUtils;
import android.util.TypedValue;
import android.view.MenuItem;
import android.view.View;

import com.cheng.movies.adapter.LinearAdapter;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

public class ImagesActivity extends AppCompatActivity {
    private RecyclerView recyclerView;
    private Toolbar toolbar;
    private SwipeRefreshLayout swipeRefreshLayout;
    private LinearLayoutManager mLayoutManager;
    private LinearAdapter mAdapter;
    private String id;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_images);

        initView();
        setListener();
        new GetData().execute(id);
    }

    private void initView() {
        toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setTitle(getResources().getString(R.string.still));

        Intent intent = getIntent();
        if (intent != null) {
            recyclerView = (RecyclerView) findViewById(R.id.recycleView);
            swipeRefreshLayout = (SwipeRefreshLayout) findViewById(R.id.swipeRefreshLayout);
            swipeRefreshLayout.setProgressViewOffset(false, 0,
                    (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 24, getResources().getDisplayMetrics()));

            mLayoutManager = new LinearLayoutManager(this, LinearLayoutManager.VERTICAL, false);
            recyclerView.setLayoutManager(mLayoutManager);
            id = intent.getStringExtra("id");
        }
    }

    private void setListener() {
        swipeRefreshLayout.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                new GetData().execute(id);
            }
        });
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == android.R.id.home) {
            onBackPressed();
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    public class GetData extends AsyncTask<String, Void, String> {
        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            swipeRefreshLayout.setRefreshing(true);
        }

        @Override
        protected String doInBackground(String... params) {
            if (params != null) {
                return MyOkhttp.get("https://api.themoviedb.org/3/movie/" + params[0] + "/images?api_key=" + BuildConfig.THE_MOVIE_DB_API_KEY);
            }
            return null;
        }

        @Override
        protected void onPostExecute(String result) {
            super.onPostExecute(result);
            if (!TextUtils.isEmpty(result)) {
                if (mAdapter == null) {
                    try {
                        JSONObject details = new JSONObject(result);
                        JSONArray backdropArray = details.getJSONArray("backdrops");
                        final List<String> images = new ArrayList<>();
                        for (int i = 0; i < backdropArray.length(); i++) {
                            JSONObject backdropItem = backdropArray.getJSONObject(i);
                            images.add(backdropItem.getString("file_path"));
                        }
                        recyclerView.setAdapter(mAdapter = new LinearAdapter(ImagesActivity.this, images));
                        mAdapter.setOnItemClickListener(new LinearAdapter.OnRecyclerViewItemClickListener(){
                            @Override
                            public void onItemClick(View view) {
                                int position = recyclerView.getChildAdapterPosition(view);
                                Intent intent = new Intent(ImagesActivity.this, Photoview.class).putExtra("ImagePath", images.get(position));
                                startActivity(intent);
                            }
                        });
                    } catch (JSONException e) {
                        e.printStackTrace();
                    }
                } else {
                    mAdapter.notifyDataSetChanged();
                }
            }
            swipeRefreshLayout.setRefreshing(false);
        }
    }
}
