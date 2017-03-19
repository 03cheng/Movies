package com.cheng.movies;

import android.content.Intent;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.design.widget.Snackbar;
import android.support.v4.app.Fragment;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.text.TextUtils;
import android.util.TypedValue;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.GridLayout;

import com.cheng.movies.adapter.GridAdapter;
import com.cheng.movies.bean.Movie;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by asus on 2017-03-17.
 */

public class PageFragment extends Fragment {
    private String type;
    private int position;
    private GridAdapter mAdapter;
    public static List<Movie> movies;
    public static List<Movie> movies1;
    public static List<Movie> movies2;
    public static List<Movie> movies3;
    private GridLayoutManager mLayoutManager;
    private SwipeRefreshLayout swipeRefreshLayout;
    private RecyclerView recyclerView;
    private int loadPage = 1;
    private int lastVisibleItem;

    public static PageFragment newInstance(int position) {
        String type;
        switch (position) {
            case 0:
                type = "popular";
                break;
            case 1:
                type = "top_rated";
                break;
            case 2:
                type = "upcoming";
                break;
            default:
                type = "popular";
        }
        Bundle args = new Bundle();
        args.putString("type", type);
        args.putInt("position", position);
        PageFragment pageFragment = new PageFragment();
        pageFragment.setArguments(args);
        return pageFragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        type = getArguments().getString("type");
        position = getArguments().getInt("position");
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_page, container, false);

        mLayoutManager = new GridLayoutManager(getContext(), 2, GridLayout.VERTICAL, false);
        recyclerView = (RecyclerView) view.findViewById(R.id.recycleView);
        recyclerView.setLayoutManager(mLayoutManager);
        swipeRefreshLayout = (SwipeRefreshLayout) view.findViewById(R.id.swipeRefreshLayout);
        swipeRefreshLayout.setProgressViewOffset(false, 0,
                (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 24, getResources().getDisplayMetrics()));
        setListener();
        return view;
    }

    private void setListener() {
        swipeRefreshLayout.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                switch (position) {
                    case 0:
                        movies1 = null;
                        break;
                    case 1:
                        movies2 = null;
                        break;
                    case 2:
                        movies3 = null;
                        break;
                    default:
                        break;
                }
                mAdapter = null;
                loadPage = 1;
                new GetData().execute(type);
            }
        });

        recyclerView.addOnScrollListener(new RecyclerView.OnScrollListener() {
            @Override
            public void onScrollStateChanged(RecyclerView recyclerView, int newState) {
                super.onScrollStateChanged(recyclerView, newState);
                //0：当前屏幕停止滚动；1时：屏幕在滚动 且 用户仍在触碰或手指还在屏幕上；2时：随用户的操作，屏幕上产生的惯性滑动；
                // 滑动状态停止并且剩余少于两个item时，自动加载下一页
                if (newState == RecyclerView.SCROLL_STATE_IDLE && lastVisibleItem + 2 >= mLayoutManager.getItemCount()) {
                    new GetData().execute(type);
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

    @Override
    public void onStart() {
        super.onStart();
        switch (position) {
            case 0:
                movies1 = null;
                break;
            case 1:
                movies2 = null;
                break;
            case 2:
                movies3 = null;
                break;
            default:
                break;
        }
        mAdapter = null;
        loadPage = 1;
        new GetData().execute(type);
    }

    public class GetData extends AsyncTask<String, Void, String> {

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            //设置swipeRefreshLayout为刷新状态
            swipeRefreshLayout.setRefreshing(true);
        }

        private String getMovieUrl(String sortType) {
            final String MOVIE_BASE_URL = "http://api.themoviedb.org/3/movie/";
            final String API_KEY = "api_key";
            final String LANGUAGE = "language";
            final String PAGE = "page";
            Uri builtUri = Uri.parse(MOVIE_BASE_URL + sortType).buildUpon()
                    .appendQueryParameter(API_KEY, BuildConfig.THE_MOVIE_DB_API_KEY)
                    .appendQueryParameter(LANGUAGE, "zh-CN")
                    .appendQueryParameter(PAGE, loadPage++ + "")
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
            super.onPostExecute(result);
            if (!TextUtils.isEmpty(result)) {
                getMovieListFromJson(result);
                if (mAdapter == null) {
                    switch (position) {
                        case 0:
                            recyclerView.setAdapter(mAdapter = new GridAdapter(getActivity(), movies1));
                            break;
                        case 1:
                            recyclerView.setAdapter(mAdapter = new GridAdapter(getActivity(), movies2));
                            break;
                        case 2:
                            recyclerView.setAdapter(mAdapter = new GridAdapter(getActivity(), movies3));
                            break;
                        default:
                            break;
                    }
                    //recyclerView.setAdapter(mAdapter = new GridAdapter(getActivity(), movies));

                    mAdapter.setOnItemClickListener(new GridAdapter.OnRecyclerViewItemClickListener() {
                        @Override
                        public void onItemClick(View view) {
                            switch (position) {
                                case 0:
                                    if (movies == null || movies.size() == 0) {
                                        movies = new ArrayList<>();
                                        movies.addAll(movies1);
                                    } else {
                                        movies.clear();
                                        movies.addAll(movies1);
                                    }
                                    break;
                                case 1:
                                    if (movies == null || movies.size() == 0) {
                                        movies = new ArrayList<>();
                                        movies.addAll(movies2);
                                    } else {
                                        movies.clear();
                                        movies.addAll(movies2);
                                    }
                                    break;
                                case 2:
                                    if (movies == null || movies.size() == 0) {
                                        movies = new ArrayList<>();
                                        movies.addAll(movies3);
                                    } else {
                                        movies.clear();
                                        movies.addAll(movies3);
                                    }
                                    break;
                                default:
                                    break;
                            }
                            String position = String.valueOf(recyclerView.getChildAdapterPosition(view));
                            Intent intent = new Intent(getActivity(), DetailActivity.class).putExtra("id", position);
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
                switch (position) {
                    case 0:
                        if (movies1 == null || movies1.size() == 0)
                            movies1 = new ArrayList<>();
                        movies1.addAll(more);
                        if (movies == null || movies.size() == 0) {
                            movies = new ArrayList<>();
                            movies.addAll(movies1);
                        } else {
                            movies.clear();
                            movies.addAll(movies1);
                        }
                        break;
                    case 1:
                        if (movies2 == null || movies2.size() == 0)
                            movies2 = new ArrayList<>();
                        movies2.addAll(more);
                        if (movies == null || movies.size() == 0) {
                            movies = new ArrayList<>();
                            movies.addAll(movies2);
                        } else {
                            movies.clear();
                            movies.addAll(movies2);
                        }
                        break;
                    case 2:
                        if (movies3 == null || movies3.size() == 0)
                            movies3 = new ArrayList<>();
                        movies3.addAll(more);
                        if (movies == null || movies.size() == 0) {
                            movies = new ArrayList<>();
                            movies.addAll(movies3);
                        } else {
                            movies.clear();
                            movies.addAll(movies3);
                        }
                        break;
                    default:
                        break;
                }
                /*if (movies == null || movies.size() == 0)
                    movies = new ArrayList<>();
                movies.addAll(more);*/
            } catch (JSONException e) {
                e.printStackTrace();
            }
        }
    }
}
