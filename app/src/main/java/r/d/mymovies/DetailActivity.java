package r.d.mymovies;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.lifecycle.ViewModelProviders;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.ImageView;
import android.widget.ScrollView;
import android.widget.TextView;
import android.widget.Toast;

import com.squareup.picasso.Picasso;

import org.json.JSONObject;

import java.util.ArrayList;
import java.util.Locale;

import r.d.mymovies.adapters.ReviewAdapter;
import r.d.mymovies.adapters.TrailerAdapter;
import r.d.mymovies.data.FavoriteMovie;
import r.d.mymovies.data.MainViewModel;
import r.d.mymovies.data.Movie;
import r.d.mymovies.data.Review;
import r.d.mymovies.data.Trailer;
import r.d.mymovies.utils.JSONUtils;
import r.d.mymovies.utils.NetworkUtils;

public class DetailActivity extends AppCompatActivity {

    private ImageView imageViewAddToFavorite;
    private ImageView imageViewBigPoster;
    private TextView textViewTitle;
    private TextView textViewOriginalTitle;
    private TextView textViewRating;
    private TextView textViewReleaseDate;
    private TextView textViewOverView;
    private ScrollView scrollViewInfo;

    private RecyclerView recyclerViewTrailers;
    private RecyclerView recyclerViewReviews;
    private ReviewAdapter reviewAdapter;
    private TrailerAdapter trailerAdapter;

    private int id;
    private Movie movie;
    private FavoriteMovie favoriteMovie;

    private MainViewModel viewModel;

    private static String lang;

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.main_menu, menu);
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        int id = item.getItemId();
//        switch (id) {
//            case R.id.itemMain:
//                Intent intent = new Intent(this, MainActivity.class);
//                startActivity(intent);
//                break;
//            case R.id.itemFavorite:
//                Intent intentToFavorite = new Intent(this, FavoriteActivity.class);
//                startActivity(intentToFavorite);
//                break;
//        }
        if (id == R.id.itemMain) {
            Intent intent = new Intent(this, MainActivity.class);
            startActivity(intent);
        } else if (id == R.id.itemFavorite) {
            Intent intentToFavorite = new Intent(this, FavoriteActivity.class);
            startActivity(intentToFavorite);
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_detail);
        lang = Locale.getDefault().getLanguage();
        imageViewBigPoster = findViewById(R.id.imageViewBigPoster);
        textViewTitle = findViewById(R.id.textViewTitle);
        textViewOriginalTitle = findViewById(R.id.textViewOriginalTitle);
        textViewRating = findViewById(R.id.textViewRating);
        textViewReleaseDate = findViewById(R.id.textViewReleaseDate);
        textViewOverView = findViewById(R.id.textViewOverview);
        imageViewAddToFavorite = findViewById(R.id.imageViewAddToFavorite);

        scrollViewInfo = findViewById(R.id.scrollViewInfo);

        Intent intent = getIntent();
        if (intent != null && intent.hasExtra("id")) {
            id = intent.getIntExtra("id", -1);
        } else {
            finish();
        }
        viewModel = ViewModelProviders.of(this).get(MainViewModel.class);
        movie = viewModel.getMovieById(id);
        Picasso.get().load(movie.getBigPosterPath()).into(imageViewBigPoster);
        textViewTitle.setText(movie.getTitle());
        textViewOriginalTitle.setText(movie.getOriginalTitle());
        textViewOverView.setText(movie.getOverview());
        textViewReleaseDate.setText(movie.getReleaseDate());
        textViewRating.setText(Double.toString(movie.getVoteAverage()));
        setFavorite();
        recyclerViewTrailers = findViewById(R.id.recyclerViewTrailers);
        recyclerViewReviews = findViewById(R.id.recyclerViewReviews);
        reviewAdapter = new ReviewAdapter();
        trailerAdapter = new TrailerAdapter();
        trailerAdapter.setOnTrailerClickListener(new TrailerAdapter.OnTrailerClickListener() {
            @Override
            public void onTrailerClick(String url) {
//                Toast.makeText(DetailActivity.this, url, Toast.LENGTH_SHORT).show();
                Intent intentToTrailer = new Intent(Intent.ACTION_VIEW, Uri.parse(url));
                startActivity(intentToTrailer);
            }
        });
        recyclerViewReviews.setLayoutManager(new LinearLayoutManager(this));
        recyclerViewTrailers.setLayoutManager(new LinearLayoutManager(this));
        recyclerViewReviews.setAdapter(reviewAdapter);
        recyclerViewTrailers.setAdapter(trailerAdapter);
        JSONObject jsonObjectTrailers = NetworkUtils.getJSONForVideos(movie.getId(), lang);
        JSONObject jsonObjectReviews = NetworkUtils.getJSONForReviews(movie.getId(), lang);
        ArrayList<Trailer> trailers = JSONUtils.getTrailersFromJSON(jsonObjectTrailers);
        ArrayList<Review> reviews = JSONUtils.getReviewsFromJSON(jsonObjectReviews);
        reviewAdapter.setReviews(reviews);
        trailerAdapter.setTrailers(trailers);
        scrollViewInfo.smoothScrollTo(0, 0);
    }

    public void onClickChangeFavorite(View view) {
        if (favoriteMovie == null) {
            viewModel.insertFavoriteMovie(new FavoriteMovie(movie));
            Toast.makeText(this, "Добавлено в избранное", Toast.LENGTH_SHORT).show();
        } else {
            viewModel.deleteFavoriteMovie(favoriteMovie);
            Toast.makeText(this, "Удалено из избранного", Toast.LENGTH_SHORT).show();
        }
        setFavorite();
    }

    private void setFavorite() {
        favoriteMovie = viewModel.getFavoriteMovieById(id);
        if (favoriteMovie == null) {
            imageViewAddToFavorite.setImageResource(R.drawable.favourite_add_to);
        } else {
            imageViewAddToFavorite.setImageResource(R.drawable.favourite_remove);
        }
    }
}