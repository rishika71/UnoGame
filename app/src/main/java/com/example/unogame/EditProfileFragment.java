package com.example.unogame;

import android.content.ContentResolver;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.navigation.NavController;
import androidx.navigation.Navigation;

import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.BaseAdapter;
import android.widget.GridView;
import android.widget.ImageView;
import android.widget.RadioButton;

import com.example.unogame.databinding.FragmentEditProfileBinding;
import com.example.unogame.models.User;
import com.example.unogame.models.Utils;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.auth.UserProfileChangeRequest;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;

import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.UUID;

import static android.app.Activity.RESULT_OK;

public class EditProfileFragment extends Fragment {

    private static final int PICK_IMAGE = 100;
    private final String TAG = "demo";

    FragmentEditProfileBinding binding;
    IEditUser am;
    String fileName;

    Integer[] imageIDs = {R.drawable.apple_avatar,R.drawable.icecream_avatar,R.drawable.orange_avatar,R.drawable.pineapple_avatar,
            R.drawable.strawberry_avatar};


    @Override
    public void onAttach(@NonNull Context context) {
        super.onAttach(context);
        if (context instanceof IEditUser) {
            am = (IEditUser) context;
        } else {
            throw new RuntimeException(context.toString());
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        getActivity().setTitle("Edit Profile");

        binding = FragmentEditProfileBinding.inflate(inflater, container, false);
        View view = binding.getRoot();

        binding.cardView.setVisibility(View.INVISIBLE);
        binding.imageView5.setImageResource(R.drawable.profile_image);

        User user = am.getUser();
        binding.editTextTextPersonName2.setText(user.getFirstname());
        binding.editTextTextPersonName3.setText(user.getLastname());
        binding.editTextTextPersonName4.setText(user.getEmail());
        binding.editTextTextPersonName5.setText(user.getCity());
        if (user.getGender().equals("Male")) binding.radioButton2.setChecked(true);
        else binding.radioButton3.setChecked(true);

        FirebaseFirestore db = FirebaseFirestore.getInstance();

        NavController navController = Navigation.findNavController(getActivity(), R.id.fragmentContainerView2);

        binding.cancelButtonId.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                navController.popBackStack();
            }
        });

        if (user.getPhotoref() != null) {
            StorageReference storageReference = FirebaseStorage.getInstance().getReference().child(user.getId()).child(user.getPhotoref());
            GlideApp.with(view)
                    .load(storageReference)
                    .into(binding.imageView5);
        } else {
            GlideApp.with(view)
                    .load(R.drawable.profile_image)
                    .into(binding.imageView5);
        }

        binding.imageView5.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                binding.cardView.setVisibility(View.VISIBLE);
                binding.gridview.setAdapter(new ImageAdapterGridView(getContext()));

                binding.closeImageView.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        binding.cardView.setVisibility(View.INVISIBLE);
                    }
                });

                binding.gridview.setOnItemClickListener(new AdapterView.OnItemClickListener() {
                    @Override
                    public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {

                        binding.imageView5.setImageResource(imageIDs[i]);

                        //...Convert URI from drawable
                        Uri imageUri = (new Uri.Builder())
                                .scheme(ContentResolver.SCHEME_ANDROID_RESOURCE)
                                .authority(getResources().getResourcePackageName(imageIDs[i]))
                                .appendPath(getResources().getResourceTypeName(imageIDs[i]))
                                .appendPath(getResources().getResourceEntryName(imageIDs[i]))
                                .build();

                        StorageReference storageReference = FirebaseStorage.getInstance().getReference();

                        fileName = imageUri.getLastPathSegment();

                        storageReference.child(am.getUser().getId()).child(fileName).putFile(imageUri);

                        binding.cardView.setVisibility(View.INVISIBLE);


                    }
                });

            }
        });

        binding.updateButtonId.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                String firstName = binding.editTextTextPersonName2.getText().toString();
                String lastName = binding.editTextTextPersonName3.getText().toString();
                String email = binding.editTextTextPersonName4.getText().toString();
                String city = binding.editTextTextPersonName5.getText().toString();
                RadioButton radioButton = binding.getRoot().findViewById(binding.radioGroup2.getCheckedRadioButtonId());
                String gender = radioButton.getText().toString();

                if (firstName.isEmpty()) {
                    am.alert(getResources().getString(R.string.enterFirstName));
                } else if (lastName.isEmpty()) {
                    am.alert(getResources().getString(R.string.enterLastName));
                } else if (city.isEmpty()) {
                    am.alert(getResources().getString(R.string.enterCity));
                } else if (email.isEmpty()) {
                    am.alert(getResources().getString(R.string.enterEmail));
                } else if (gender.isEmpty()) {
                    am.alert(getResources().getString(R.string.chooseGender));
                } else {
                    FirebaseAuth mAuth = FirebaseAuth.getInstance();
                    FirebaseUser cur_user = mAuth.getCurrentUser();
                    cur_user.updateEmail(email).addOnCompleteListener(new OnCompleteListener<Void>() {
                        @Override
                        public void onComplete(@NonNull Task<Void> task) {
                            if (task.isSuccessful()) {
                                UserProfileChangeRequest profileUpdates = new UserProfileChangeRequest.Builder()
                                        .setDisplayName(firstName + " " + lastName).build();
                                cur_user.updateProfile(profileUpdates);
                                HashMap<String, Object> data = new HashMap<>();
                                data.put("firstname", firstName);
                                data.put("lastname", lastName);
                                data.put("city", city);
                                data.put("gender", gender);
                                data.put("email", email);
                                data.put("photoref", fileName);
                                db.collection(Utils.DB_PROFILE).document(cur_user.getUid()).update(data).addOnCompleteListener(new OnCompleteListener<Void>() {
                                    @Override
                                    public void onComplete(@NonNull Task<Void> task) {
                                        am.setUser(new User(firstName, lastName, fileName, city, email, gender, cur_user.getUid()));
                                        am.alert("Profile updated");
                                        navController.popBackStack();
                                    }
                                });
                            } else {
                                am.alert(task.getException().getMessage());
                            }
                        }
                    });
                }
            }
        });

        return view;
    }

    interface IEditUser {
        void alert(String msg);

        User getUser();

        void setUser(User user);
    }


    public class ImageAdapterGridView extends BaseAdapter {

        private Context mContext;


        public ImageAdapterGridView(Context c) {
            mContext = c;
        }

        @Override
        public int getCount() {
            return imageIDs.length;
        }

        @Override
        public Object getItem(int i) {
            return null;
        }

        @Override
        public long getItemId(int i) {
            return 0;
        }

        @Override
        public View getView(int i, View view, ViewGroup viewGroup) {
            ImageView mImageView;

            if (view == null) {
                mImageView = new ImageView(mContext);
                mImageView.setLayoutParams(new GridView.LayoutParams(200, 200));
                mImageView.setScaleType(ImageView.ScaleType.CENTER_CROP);
                mImageView.setPadding(6, 6, 6, 6);
            } else {
                mImageView = (ImageView) view;
            }
            mImageView.setImageResource(imageIDs[i]);
            return mImageView;

        }
    }
}

