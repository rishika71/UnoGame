package com.example.unogame;

import android.content.Context;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.navigation.NavController;
import androidx.navigation.Navigation;
import androidx.recyclerview.widget.DividerItemDecoration;
import androidx.recyclerview.widget.LinearLayoutManager;

import android.util.Log;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;

import com.example.unogame.databinding.FragmentUsersBinding;
import com.example.unogame.models.User;
import com.example.unogame.models.Utils;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.navigation.NavigationBarView;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;

import java.util.ArrayList;


public class UsersFragment extends Fragment {

    FragmentUsersBinding binding;

    IUsers am;

    NavController navController;

    private final String TAG = "demo";

    @Override
    public void onAttach(@NonNull Context context) {
        super.onAttach(context);
        if (context instanceof IUsers) {
            am = (IUsers) context;
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

        getActivity().setTitle("UNO Buddies");

        binding = FragmentUsersBinding.inflate(inflater, container, false);

        View view = binding.getRoot();

        navController = Navigation.findNavController(getActivity(), R.id.fragmentContainerView2);

        binding.usersView.setHasFixedSize(true);
        LinearLayoutManager llm = new LinearLayoutManager(getContext());
        binding.usersView.setLayoutManager(llm);

        DividerItemDecoration dividerItemDecoration = new DividerItemDecoration(binding.usersView.getContext(),
                llm.getOrientation());
        binding.usersView.addItemDecoration(dividerItemDecoration);

        FirebaseFirestore db = FirebaseFirestore.getInstance();
        am.toggleDialog(true);
        CollectionReference ddb = db.collection(Utils.DB_PROFILE);
        ddb.get().addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
            @Override
            public void onComplete(@NonNull Task<QuerySnapshot> task) {
                am.toggleDialog(false);
                if (task.isSuccessful()) {
                    ArrayList<User> users = new ArrayList<>();
                    for (QueryDocumentSnapshot snapshot : task.getResult()) {
                        User user = snapshot.toObject(User.class);
                        user.setId(snapshot.getId());
                        users.add(user);
                    }
                    binding.usersView.setAdapter(new UsersAdapter(users));
                } else {
                    task.getException().printStackTrace();
                }
            }
        });

        binding.back.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                navController.navigate(R.id.action_usersFragment_to_gameScreenFragment);
            }
        });
        return view;
    }



    interface IUsers {

        void toggleDialog(boolean show);

    }

}
