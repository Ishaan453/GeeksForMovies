package com.ishaanbhela.geeksformovies.searchedMovies;

import android.content.Context;
import android.content.Intent;
import android.util.TypedValue;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.ishaanbhela.geeksformovies.Database.SqLiteHelper;
import com.ishaanbhela.geeksformovies.R;
import com.ishaanbhela.geeksformovies.Movies.movieDetails;
import com.ishaanbhela.geeksformovies.Movies.savedMovieDetails;

import java.text.NumberFormat;
import java.util.List;

public class searchedMoviesAdapter extends RecyclerView.Adapter<searchedMoviesAdapter.searchedMoviesHolder> {

    private Context context;
    private List<searchedMoviesModel> movieList;
    private SqLiteHelper dbHelper;
    private int width;

    public searchedMoviesAdapter(List<searchedMoviesModel> movies, Context context, int width){
        movieList = movies;
        this.context = context;
        this.width = width;
    }

    @NonNull
    @Override
    public searchedMoviesHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.search_layout, parent, false);
        return new searchedMoviesHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull searchedMoviesHolder holder, int position) {
        if(width != 0){
            ViewGroup.LayoutParams params = holder.card.getLayoutParams();
            params.width = (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_PX, width, context.getResources().getDisplayMetrics());
            holder.card.setLayoutParams(params);
        }
        NumberFormat format = NumberFormat.getInstance();
        format.setMaximumFractionDigits(1);

        searchedMoviesModel movie = movieList.get(position);
        holder.title.setText(movie.getTitle());
        holder.release.setText("Release Date: " + movie.getReleaseDate());
        holder.rating.setText("Rating: " + format.format(movie.getVoteAverage()));
        holder.overview.setText(movie.getOverview());

        String posterUrl = "https://image.tmdb.org/t/p/w500" + movie.getPosterPath(); // TMDb URL for posters
        Glide.with(context)
                .load(posterUrl)
                .placeholder(R.drawable.placeholder) // Placeholder image while loading
                .error(R.drawable.placeholder)
                .into(holder.poster);


        holder.itemView.setOnClickListener(v -> {
            dbHelper = new SqLiteHelper(context);
            boolean isSaved = dbHelper.isMovieSaved(movie.getId());
            Intent intent;
            if (isSaved) {
                // If movie is saved, open the saved movie details
                intent = new Intent(context, savedMovieDetails.class);
                intent.putExtra("MOVIE_ID", movie.getId());
            } else {
                // If movie is not saved, open the regular movie details
                intent = new Intent(context, movieDetails.class);
                intent.putExtra("MOVIE_ID", movie.getId());
            }
            context.startActivity(intent);
        });

    }

    @Override
    public int getItemCount() {
        return movieList.size();
    }


    public class searchedMoviesHolder extends RecyclerView.ViewHolder {
        ImageView poster;
        TextView title, overview, rating, release;
        LinearLayout card;
        public searchedMoviesHolder(@NonNull View itemView) {
            super(itemView);
            poster = itemView.findViewById(R.id.movie_poster);
            title = itemView.findViewById(R.id.movie_title);
            overview = itemView.findViewById(R.id.movie_overview);
            rating = itemView.findViewById(R.id.movie_rating);
            release = itemView.findViewById(R.id.movie_release_date);
            card = itemView.findViewById(R.id.card);
        }
    }
}
