package com.example.unogame;

import android.os.Bundle;

import androidx.fragment.app.Fragment;
import androidx.navigation.NavController;
import androidx.navigation.Navigation;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.example.unogame.databinding.FragmentViewUserBinding;
import com.example.unogame.models.User;
import com.example.unogame.models.Utils;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;

public class ViewUserFragment extends Fragment {

    FragmentViewUserBinding binding;

    User user;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            user = (User) getArguments().getSerializable(Utils.DB_PROFILE);
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        getActivity().setTitle("Buddy Profile");
        binding = FragmentViewUserBinding.inflate(inflater, container, false);
        View view = binding.getRoot();

        if (user.getPhotoref() != null) {
            StorageReference storageReference = FirebaseStorage.getInstance().getReference().child(user.getId()).child(user.getPhotoref());
            GlideApp.with(view)
                    .load(storageReference)
                    .into(binding.imageView4);
        } else {
            GlideApp.with(view)
                    .load(R.drawable.profile_image)
                    .into(binding.imageView4);
        }

        NavController navController = Navigation.findNavController(getActivity(), R.id.fragmentContainerView2);

        binding.textView11.setText(user.getFirstname());
        binding.textView12.setText(user.getLastname());
        binding.textView13.setText(user.getGender());
        binding.textView14.setText(user.getCity());
        binding.textView15.setText(user.getEmail());

        binding.button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                navController.navigate(R.id.action_viewUserFragment_to_gameScreenFragment);
            }
        });

        return view;
    }
}