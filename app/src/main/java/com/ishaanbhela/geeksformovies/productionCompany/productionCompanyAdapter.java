package com.ishaanbhela.geeksformovies.productionCompany;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.ishaanbhela.geeksformovies.R;

import java.util.List;

public class productionCompanyAdapter extends RecyclerView.Adapter<productionCompanyAdapter.productionCompanyHolder> {
    List<productionCompanyModel> companies;
    Context context;

    public productionCompanyAdapter(List<productionCompanyModel> companies, Context context){
        this.companies = companies;
        this.context = context;
    }

    @NonNull
    @Override
    public productionCompanyHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.production_company_layout, parent, false);
        return new productionCompanyHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull productionCompanyHolder holder, int position) {
        if(companies.isEmpty()){
            holder.logo.setImageDrawable(context.getDrawable(R.drawable.baseline_error_24));
            holder.name = "No Data Available";
        }
        else{
            String url = "https://image.tmdb.org/t/p/w500" + companies.get(position).logo_path;

            try{
                Glide.with(context)
                        .load(url)
                        .placeholder(R.drawable.placeholder) // Placeholder image while loading
                        .error(R.drawable.placeholder)
                        .into(holder.logo);
            }
            catch (Exception e){
                System.out.println(e);
            }

            holder.name = companies.get(position).name;
        }

        holder.itemView.setOnClickListener(v -> {
            Toast.makeText(context, holder.name, Toast.LENGTH_SHORT).show();
        });

    }

    @Override
    public int getItemCount() {
        return companies.isEmpty()? 1: companies.size();
    }

    public class productionCompanyHolder extends RecyclerView.ViewHolder{
        ImageView logo;
        String name;
        public productionCompanyHolder(@NonNull View itemView) {
            super(itemView);
            logo = itemView.findViewById(R.id.productionLogo);
        }
    }
}
