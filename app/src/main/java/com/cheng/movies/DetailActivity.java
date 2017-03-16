package com.cheng.movies;

import android.content.Intent;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.design.widget.CoordinatorLayout;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.text.TextUtils;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import com.bumptech.glide.Glide;

import org.json.JSONException;
import org.json.JSONObject;

import static com.cheng.movies.MainActivity.movies;

public class DetailActivity extends AppCompatActivity {
    private Movie movie;
    private CoordinatorLayout coordinatorLayout;
    private Button btn_images;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_detail);

        initView();
        setListener();
    }

    private void initView() {
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        coordinatorLayout = (CoordinatorLayout) findViewById(R.id.coordinatorLayout);
        Intent intent = getIntent();
        if (intent != null) {
            int id = Integer.parseInt(intent.getStringExtra("id"));
            movie = movies.get(id);
            getSupportActionBar().setTitle(movie.getTitle());
            new GetData().execute(movie.getId());

            ImageView imageView = (ImageView) findViewById(R.id.iv_poster);
            Glide.with(this).load("http://image.tmdb.org/t/p/w185_and_h278_bestv2" + movie.getPosterPath()).into(imageView);
            ((TextView) findViewById(R.id.tv_release_date)).setText(movie.getReleaseDate());
            ((TextView) findViewById(R.id.tv_vote_average)).setText(movie.getVoteAverage() + "/10");
            ((TextView) findViewById(R.id.tv_overview)).setText(movie.getOverview());
            btn_images = (Button) findViewById(R.id.btn_images);
        }

        FloatingActionButton fab = (FloatingActionButton) findViewById(R.id.fab);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Snackbar.make(view, "收藏成功", Snackbar.LENGTH_LONG)
                        .setAction("撤销", new View.OnClickListener() {
                            @Override
                            public void onClick(View v) {

                            }
                        }).show();
            }
        });
    }

    private void setListener() {
        btn_images.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(DetailActivity.this, ImagesActivity.class).putExtra("id", movie.getId());
                startActivity(intent);
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

        private String getMovieUrl(String id) {
            final String DETAIL_BASE_URL = "http://api.themoviedb.org/3/movie/";
            final String API_KEY = "api_key";
            Uri builtUri = Uri.parse(DETAIL_BASE_URL + id).buildUpon()
                    .appendQueryParameter(API_KEY, BuildConfig.THE_MOVIE_DB_API_KEY)
                    .build();
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
            if (!TextUtils.isEmpty(result)) {
                final String DETAIL_VOTEAVERAGE = "vote_average";
                final String DETAIL_RUNTIME = "runtime";
                final String BACK_DROP = "backdrop_path";

                try {
                    JSONObject details = new JSONObject(result);
                    movie.setVoteAverage(details.getDouble(DETAIL_VOTEAVERAGE));
                    movie.setRuntime(details.getInt(DETAIL_RUNTIME));
                    movie.setBackDropPath(details.getString(BACK_DROP));
                    ((TextView) findViewById(R.id.tv_runtime)).setText(movie.getRuntime() + "min");
                    Glide.with(coordinatorLayout.getContext()).load("http://image.tmdb.org/t/p/w533_and_h300_bestv2" + movie.getBackDropPath())
                            .into((ImageView) findViewById(R.id.iv_backDrop));
                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }
        }
    }
}
