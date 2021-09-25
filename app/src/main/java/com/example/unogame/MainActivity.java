package com.example.unogame;

import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.os.Bundle;

import com.example.unogame.databinding.FragmentCreateNewAccountBinding;
import com.example.unogame.models.User;
import com.google.android.material.snackbar.Snackbar;

import androidx.appcompat.app.AppCompatActivity;

import android.view.View;

import androidx.navigation.NavController;
import androidx.navigation.Navigation;
import androidx.navigation.ui.AppBarConfiguration;
import androidx.navigation.ui.NavigationUI;

import com.example.unogame.databinding.ActivityMainBinding;

import android.view.Menu;
import android.view.MenuItem;

import static com.example.unogame.R.layout.*;

public class MainActivity extends AppCompatActivity implements CreateNewAccountFragment.IRegister, LoginFragment.ILogin, GameScreenFragment.IGameScreen, UserProfileFragment.IUserProfile, EditProfileFragment.IEditUser, UsersFragment.IUsers, GameRoomFragment.IGameRoomScreen, RecyclerViewAdapter.IRecyclerViewAdapter {

    ProgressDialog dialog;
    User user = null;

    //private ActivityMainBinding binding;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
//        binding = ActivityMainBinding.inflate(getLayoutInflater());
//        setContentView(binding.getRoot());
    }

    public User getUser() {
        return user;
    }

    public void setUser(User user) {
        this.user = user;
    }

    public void alert(String alert) {
        runOnUiThread(() -> new AlertDialog.Builder(this)
                .setTitle(R.string.info)
                .setMessage(alert)
                .setPositiveButton(R.string.okay, null)
                .show());
    }

    public void toggleDialog(boolean show) {
        toggleDialog(show, null);
    }

    public void toggleDialog(boolean show, String msg) {
        if (show) {
            dialog = new ProgressDialog(this);
            if (msg == null)
                dialog.setMessage(getString(R.string.loading));
            else
                dialog.setMessage(msg);
            dialog.setCancelable(false);
            dialog.show();
        } else {
            dialog.dismiss();
        }
    }


}