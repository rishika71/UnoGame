package com.example.unogame;

import android.content.Context;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.navigation.NavController;
import androidx.navigation.Navigation;

import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;

import com.example.unogame.databinding.FragmentUserProfileBinding;
import com.example.unogame.models.User;
import com.google.android.material.navigation.NavigationBarView;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;

public class UserProfileFragment extends Fragment {


    FragmentUserProfileBinding binding;

    NavController navController;

    IUserProfile am;

    @Override
    public void onAttach(@NonNull Context context) {
        super.onAttach(context);
        if (context instanceof IUserProfile) {
            am = (IUserProfile) context;
        } else {
            throw new RuntimeException(context.toString());
        }
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        getActivity().setTitle(R.string.userProfile);

        binding = FragmentUserProfileBinding.inflate(inflater, container, false);

        View view = binding.getRoot();

        User user = am.getUser();
        binding.nameTextViewId.setText(user.getFirstname().toUpperCase() + " " + user.getLastname().toUpperCase());
        binding.genderTextViewId.setText(user.getGender());
        binding.cityTextViewId.setText(user.getCity());
        binding.emailTextViewId.setText(user.getEmail());

        if (user.getPhotoref() != null) {
            StorageReference storageReference = FirebaseStorage.getInstance().getReference().child(user.getId()).child(user.getPhotoref());
            GlideApp.with(view)
                    .load(storageReference)
                    .into(binding.userProfileImageView);
        } else {
            GlideApp.with(view)
                    .load(R.drawable.profile_image)
                    .into(binding.userProfileImageView);
        }

        navController = Navigation.findNavController(getActivity(), R.id.fragmentContainerView2);

        //...Editing profile
        binding.editButtonId.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                navController.navigate(R.id.action_userProfileFragment_to_editProfileFragment);
            }
        });

        //...Back Button
        binding.backButtonId.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                navController.navigate(R.id.action_userProfileFragment_to_gameScreenFragment);
            }
        });

        return view;

    }

    interface IUserProfile {

        User getUser();

        void setUser(User user);
    }
}