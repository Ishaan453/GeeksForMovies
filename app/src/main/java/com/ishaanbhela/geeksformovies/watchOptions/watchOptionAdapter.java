package com.ishaanbhela.geeksformovies.watchOptions;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.ishaanbhela.geeksformovies.R;

import java.util.List;

public class watchOptionAdapter extends RecyclerView.Adapter<watchOptionAdapter.watchOptionViewHolder> {

    private List<watchOptionsModel> watchOptions;

    public watchOptionAdapter(List<watchOptionsModel> watchOptions) {
        this.watchOptions = watchOptions;
    }

    @NonNull
    @Override
    public watchOptionViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.watch_options_layout, parent, false);
        return new watchOptionViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull watchOptionViewHolder holder, int position) {
        if(watchOptions.isEmpty()){
            holder.providerLogo.setImageDrawable(ContextCompat.getDrawable(holder.itemView.getContext(), R.drawable.baseline_error_24));
            holder.providerName.setText("Data Unavailable :(");
            return;
        }
        watchOptionsModel watchOption = watchOptions.get(position);

        // Set provider name, logo, etc.
        holder.providerName.setText(watchOption.getProviderName());
        holder.optionType.setText(watchOption.getType());

        // Load the logo using a library like Glide or Picasso
        Glide.with(holder.itemView.getContext())
                .load("https://image.tmdb.org/t/p/w500" + watchOption.getLogoPath())
                .placeholder(R.drawable.placeholder)
                .error(R.drawable.placeholder)
                .into(holder.providerLogo);

        // Customize based on the type
        switch (watchOption.getType()) {
            case "rent":
                holder.optionType.setText("Rent");
                break;
            case "flatrate":
                holder.optionType.setText("Stream");
                break;
            case "buy":
                holder.optionType.setText("Buy");
                break;
        }
    }

    @Override
    public int getItemCount() {
        return watchOptions.isEmpty() ? 1: watchOptions.size();
    }

    public static class watchOptionViewHolder extends RecyclerView.ViewHolder {
        TextView providerName, optionType;
        ImageView providerLogo;

        public watchOptionViewHolder(@NonNull View itemView) {
            super(itemView);
            providerName = itemView.findViewById(R.id.provider_name);
            optionType = itemView.findViewById(R.id.option_type);
            providerLogo = itemView.findViewById(R.id.provider_logo);
        }
    }
}
