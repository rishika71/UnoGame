package com.example.unogame;

import android.app.AlertDialog;
import android.content.ContentResolver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.BaseAdapter;
import android.widget.GridView;
import android.widget.ImageView;
import android.widget.RadioButton;

import com.example.unogame.databinding.FragmentCreateNewAccountBinding;
import com.example.unogame.models.User;
import com.example.unogame.models.Utils;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.auth.UserProfileChangeRequest;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;

import java.io.ByteArrayOutputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.UUID;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.navigation.NavController;
import androidx.navigation.Navigation;

import static android.app.Activity.RESULT_OK;

public class CreateNewAccountFragment extends Fragment {

    private FirebaseAuth mAuth;

    final private String TAG = "demo";
    private static final int PICK_IMAGE_GALLERY = 100;
    private static final int PICK_IMAGE_CAMERA = 101;

    NavController navController;

    Uri imageUri;
    String fileName;
    String email, password, firstName, lastName, city, gender;

    FragmentCreateNewAccountBinding binding;

    IRegister am;

    Integer[] imageIDs = {R.drawable.apple_avatar,R.drawable.icecream_avatar,R.drawable.orange_avatar,R.drawable.pineapple_avatar,
            R.drawable.strawberry_avatar};


    @Override
    public void onAttach(@NonNull Context context) {
        super.onAttach(context);
        if (context instanceof IRegister) {
            am = (IRegister) context;
        } else {
            throw new RuntimeException(context.toString());
        }
    }

    private void storeUserInfoToFirestore(String firstName, String lastName, String city, String gender, String email, String fileName) {
        FirebaseFirestore db = FirebaseFirestore.getInstance();
        HashMap<String, Object> data = new HashMap<>();
        data.put("firstname", firstName);
        data.put("lastname", lastName);
        data.put("city", city);
        data.put("gender", gender);
        data.put("email", email);
        data.put("photoref", fileName);

        db.collection(Utils.DB_PROFILE)
                .document(mAuth.getUid())
                .set(data)
                .addOnCompleteListener(new OnCompleteListener<Void>() {
                    @Override
                    public void onComplete(@NonNull Task<Void> task) {
                        am.setUser(new User(firstName, lastName, fileName, city, email, gender, mAuth.getUid()));
                        navController.navigate(R.id.action_createNewAccountFragment_to_gameScreenFragment);
                    }
                });
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        getActivity().setTitle(R.string.createAccount);

        binding = FragmentCreateNewAccountBinding.inflate(inflater, container, false);

        View view = binding.getRoot();

        binding.cardView1.setVisibility(View.INVISIBLE);

        binding.userImage.setImageResource(R.drawable.profile_image);

        navController = Navigation.findNavController(getActivity(), R.id.fragmentContainerView2);

        //....Register Button......
        binding.registerButtonId.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                firstName = binding.createFragmentFirstNameId.getText().toString();
                lastName = binding.createFragmentLastNameId.getText().toString();
                city = binding.createFragmentCityNameId.getText().toString();
                email = binding.createFragmentEmailId.getText().toString();
                password = binding.createFragmentPasswordId.getText().toString();
                RadioButton radioButton = binding.getRoot().findViewById(binding.radioGroup.getCheckedRadioButtonId());
                gender = radioButton.getText().toString();

                if(firstName.isEmpty()){
                    getAlertDialogBox(getResources().getString(R.string.enterFirstName));
                }else if(lastName.isEmpty()){
                    getAlertDialogBox(getResources().getString(R.string.enterLastName));
                }else if(city.isEmpty()){
                    getAlertDialogBox(getResources().getString(R.string.enterCity));
                } else if(email.isEmpty()){
                    getAlertDialogBox(getResources().getString(R.string.enterEmail));
                }else if(password.isEmpty()){
                    getAlertDialogBox(getResources().getString(R.string.enterPassword));
                }else if(gender.isEmpty()){
                    getAlertDialogBox(getResources().getString(R.string.chooseGender));
                }else {

                    mAuth = FirebaseAuth.getInstance();
                    mAuth.createUserWithEmailAndPassword(email, password)
                            .addOnCompleteListener(getActivity(), new OnCompleteListener<AuthResult>() {
                                @Override
                                public void onComplete(@NonNull Task<AuthResult> task) {
                                    if(task.isSuccessful()) {

                                        UserProfileChangeRequest profileUpdates = new UserProfileChangeRequest.Builder()
                                                .setDisplayName(firstName + " " + lastName).build();
                                        FirebaseUser user = mAuth.getCurrentUser();
                                        user.updateProfile(profileUpdates);

                                        //...Store image in firebase storage
                                        if (imageUri != null) {
                                            fileName = imageUri.getLastPathSegment();

                                            storeUserInfoToFirestore(firstName, lastName, city, gender, email, fileName);
                                            StorageReference storageReference = FirebaseStorage.getInstance().getReference();
                                            storageReference.child(user.getUid()).child(fileName).putFile(imageUri);
                                        } else {
                                            storeUserInfoToFirestore(firstName, lastName, city, gender, email, null);
                                        }

                                    } else
                                        getAlertDialogBox(task.getException().getMessage());

                                }
                            });
                }

            }
        });

        //....Cancel Button......
        binding.cancelButtonId.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                navController.navigate(R.id.action_createNewAccountFragment_to_loginFragment);
            }
        });

        binding.userImage.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                binding.cardView1.setVisibility(View.VISIBLE);
                binding.gridview.setAdapter(new ImageAdapterGridView(getContext()));

                binding.closeImageView1.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        binding.cardView1.setVisibility(View.INVISIBLE);
                    }
                });

                binding.gridview.setOnItemClickListener(new AdapterView.OnItemClickListener() {
                    @Override
                    public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {

                        binding.userImage.setImageResource(imageIDs[i]);

                        //...Convert URI from drawable
                         imageUri = (new Uri.Builder())
                                .scheme(ContentResolver.SCHEME_ANDROID_RESOURCE)
                                .authority(getResources().getResourcePackageName(imageIDs[i]))
                                .appendPath(getResources().getResourceTypeName(imageIDs[i]))
                                .appendPath(getResources().getResourceEntryName(imageIDs[i]))
                                .build();


                        binding.cardView1.setVisibility(View.INVISIBLE);


                    }
                });

            }
        });

        return view;
    }

    public void getAlertDialogBox(String errorMessage){

        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        builder.setTitle(getResources().getString(R.string.errorMessage))
                .setMessage(errorMessage);

        builder.setPositiveButton(R.string.ok, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
                dialogInterface.dismiss();
            }
        });
        builder.create().show();

    }

    interface IRegister {

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