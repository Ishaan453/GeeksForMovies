package com.ishaanbhela.geeksformovies.cast;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.ishaanbhela.geeksformovies.R;

import java.util.List;

public class castAdapter extends RecyclerView.Adapter<castAdapter.castHolder> {

    List<castModel> casts;
    Context context;

    public castAdapter(List<castModel> casts, Context context){
        this.casts = casts;
        this.context = context;
    }

    @NonNull
    @Override
    public castHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.cast_layout, parent, false);
        return new castHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull castHolder holder, int position) {
        castModel cast = casts.get(position);
        String url = "https://image.tmdb.org/t/p/w500";

        try{
            Glide.with(context)
                    .load(url + cast.getImgPath())
                    .placeholder(R.drawable.placeholder)
                    .error(R.drawable.placeholder)
                    .into(holder.castImg);
        }
        catch (Exception e){
            System.out.println(e);
        }

        holder.castName.setText(cast.getName());
        holder.castCharacter.setText(cast.getCharacter());
    }

    @Override
    public int getItemCount() {
        return casts.size();
    }

    public class castHolder extends RecyclerView.ViewHolder{
        ImageView castImg;
        TextView castName, castCharacter;
        public castHolder(@NonNull View itemView) {
            super(itemView);
            castImg = itemView.findViewById(R.id.castImg);
            castName = itemView.findViewById(R.id.castName);
            castCharacter = itemView.findViewById(R.id.castCharacter);
        }
    }
}
