package com.cheng.movies;

import android.content.Intent;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.design.widget.Snackbar;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.text.TextUtils;
import android.util.TypedValue;
import android.view.View;
import android.widget.GridLayout;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

public class MainActivity extends AppCompatActivity {
    private Toolbar toolbar;
    private GridAdapter mAdapter;
    public static List<Movie> movies;
    private GridLayoutManager mLayoutManager;
    private SwipeRefreshLayout swipeRefreshLayout;
    private RecyclerView recyclerView;
    private int page = 1;
    private int lastVisibleItem;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        initView();
        setListener();
        new GetData().execute("popular");
    }

    private void initView() {
        toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        mLayoutManager = new GridLayoutManager(MainActivity.this, 2, GridLayout.VERTICAL, false);
        recyclerView = (RecyclerView) findViewById(R.id.recycleView);
        recyclerView.setLayoutManager(mLayoutManager);
        swipeRefreshLayout = (SwipeRefreshLayout) findViewById(R.id.swipeRefreshLayout);
        swipeRefreshLayout.setProgressViewOffset(false, 0,
                (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 24, getResources().getDisplayMetrics()));
    }

    private void setListener() {
        swipeRefreshLayout.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                movies = null;
                mAdapter = null;
                page = 1;
                new GetData().execute("popular");
            }
        });

        recyclerView.addOnScrollListener(new RecyclerView.OnScrollListener() {
            @Override
            public void onScrollStateChanged(RecyclerView recyclerView, int newState) {
                super.onScrollStateChanged(recyclerView, newState);
                //0：当前屏幕停止滚动；1时：屏幕在滚动 且 用户仍在触碰或手指还在屏幕上；2时：随用户的操作，屏幕上产生的惯性滑动；
                // 滑动状态停止并且剩余少于两个item时，自动加载下一页
                if (newState == RecyclerView.SCROLL_STATE_IDLE && lastVisibleItem + 2 >= mLayoutManager.getItemCount()) {
                    new GetData().execute("popular");
                }
            }

            @Override
            public void onScrolled(RecyclerView recyclerView, int dx, int dy) {
                super.onScrolled(recyclerView, dx, dy);
                //获取加载的最后一个可见视图在适配器的位置。
                lastVisibleItem = mLayoutManager.findLastVisibleItemPosition();
            }
        });
    }

    public class GetData extends AsyncTask<String, Void, String> {

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            //设置swipeRefreshLayout为刷新状态
            swipeRefreshLayout.setRefreshing(true);
        }

        private String getMovieUrl(String sortType) {
            final String MOVIE_BASE_URL = "http://api.themoviedb.org/3";
            final String POP_URL = "/movie/popular";
            final String RATED_URL = "/movie/top_rated";
            final String API_KEY = "api_key";
            final String LANGUAGE = "language";
            final String PAGE = "page";
            Uri builtUri = null;
            if (sortType.equals("popular")) {
                builtUri = Uri.parse(MOVIE_BASE_URL + POP_URL).buildUpon()
                        .appendQueryParameter(API_KEY, BuildConfig.THE_MOVIE_DB_API_KEY)
                        .appendQueryParameter(LANGUAGE, "zh-CN")
                        .appendQueryParameter(PAGE, page++ + "")
                        .build();
            } else if (sortType.equals("rated")) {
                builtUri = Uri.parse(MOVIE_BASE_URL + RATED_URL).buildUpon()
                        .appendQueryParameter(API_KEY, BuildConfig.THE_MOVIE_DB_API_KEY)
                        .appendQueryParameter(LANGUAGE, "zh-CN")
                        .appendQueryParameter(PAGE, ++page + "")
                        .build();
            }
            return builtUri.toString();
        }

        @Override
        protected String doInBackground(String... params) {
            if (params != null) {
                return MyOkhttp.get(getMovieUrl(params[0]));
            }
            return null;
        }

        @Override
        protected void onPostExecute(String result) {
            super.onPostExecute(result);
            if (!TextUtils.isEmpty(result)) {
                getMovieListFromJson(result);
                if (mAdapter == null) {
                    recyclerView.setAdapter(mAdapter = new GridAdapter(MainActivity.this, movies));
                    mAdapter.setOnItemClickListener(new GridAdapter.OnRecyclerViewItemClickListener() {
                        @Override
                        public void onItemClick(View view) {
                            String position = String.valueOf(recyclerView.getChildAdapterPosition(view));
                            Intent intent = new Intent(MainActivity.this, DetailActivity.class).putExtra("id", position);
                            startActivity(intent);
                        }

                        @Override
                        public void onItemLongClick(View view) {
                            int position = recyclerView.getChildAdapterPosition(view);
                            Snackbar.make(view, "Its' ID is " + movies.get(position).getId(), Snackbar.LENGTH_SHORT)
                                    .setAction("Action", null).show();
                        }
                    });
                } else {
                    mAdapter.notifyDataSetChanged();
                }
            }
            swipeRefreshLayout.setRefreshing(false);
        }

        private void getMovieListFromJson(String moviesJson) {
            final String MOVIE_LIST = "results";
            final String MOVIE_ID = "id";
            final String MOVIE_POSTER = "poster_path";
            final String MOVIE_TITLE = "title";
            final String MOVIE_OVERVIEW = "overview";
            final String MOVIE_RELEASEDATE = "release_date";
            final String MOVIE_VOTEAVERAGE = "vote_average";

            try {
                List<Movie> more = new ArrayList<>();
                JSONObject jsonObject = new JSONObject(moviesJson);
                JSONArray movieArray = jsonObject.getJSONArray(MOVIE_LIST);
                for (int i = 0; i < movieArray.length(); i++) {
                    JSONObject movieItem = movieArray.getJSONObject(i);
                    Movie movie = new Movie();
                    movie.setId(movieItem.getString(MOVIE_ID));
                    movie.setPosterPath(movieItem.getString(MOVIE_POSTER));
                    movie.setTitle(movieItem.getString(MOVIE_TITLE));
                    movie.setOverview(movieItem.getString(MOVIE_OVERVIEW));
                    movie.setReleaseDate(movieItem.getString(MOVIE_RELEASEDATE));
                    movie.setVoteAverage(movieItem.getDouble(MOVIE_VOTEAVERAGE));
                    more.add(movie);
                }
                if (movies == null || movies.size() == 0)
                    movies = new ArrayList<>();
                movies.addAll(more);
            } catch (JSONException e) {
                e.printStackTrace();
            }
        }
    }
}
