package com.example.unogame;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.example.unogame.databinding.UsersLayoutBinding;
import com.example.unogame.models.User;
import com.example.unogame.models.Utils;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;

import java.util.ArrayList;

import androidx.annotation.NonNull;
import androidx.navigation.Navigation;
import androidx.recyclerview.widget.RecyclerView;

public class UsersAdapter extends RecyclerView.Adapter<UsersAdapter.UViewHolder> {

    ArrayList<User> users;

    UsersLayoutBinding binding;

    public UsersAdapter(ArrayList<User> users) {
        this.users = users;
    }

    @NonNull
    @Override
    public UViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        binding = UsersLayoutBinding.inflate(LayoutInflater.from(parent.getContext()), parent, false);
        return new UViewHolder(binding);
    }

    @Override
    public void onBindViewHolder(@NonNull UViewHolder holder, int position) {
        User user = users.get(position);

        holder.binding.textView16.setText(new StringBuilder().append(user.getFirstname()).append(" ").append(user.getLastname()).toString());

        if (user.getPhotoref() != null) {
            StorageReference storageReference = FirebaseStorage.getInstance().getReference().child(user.getId()).child(user.getPhotoref());
            GlideApp.with(holder.binding.getRoot())
                    .load(storageReference)
                    .into(holder.binding.imageView6);
        } else {
            GlideApp.with(holder.binding.getRoot())
                    .load(R.drawable.profile_image)
                    .into(holder.binding.imageView6);
        }

        holder.binding.getRoot().setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Bundle bundle = new Bundle();
                bundle.putSerializable(Utils.DB_PROFILE, user);
                Navigation.findNavController(holder.itemView).navigate(R.id.action_usersFragment_to_viewUserFragment, bundle);
            }
        });

    }

    @Override
    public int getItemCount() {
        return this.users.size();
    }

    public static class UViewHolder extends RecyclerView.ViewHolder {

        UsersLayoutBinding binding;

        public UViewHolder(@NonNull UsersLayoutBinding binding) {
            super(binding.getRoot());
            this.binding = binding;
        }

    }

}

