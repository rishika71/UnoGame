package com.example.unogame;

import androidx.lifecycle.ViewModelProvider;

import android.app.ProgressDialog;
import android.content.Context;
import android.graphics.Color;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.navigation.NavController;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.Toast;

import com.example.unogame.databinding.FragmentUsersBinding;
import com.example.unogame.databinding.GameRoomFragmentBinding;
import com.example.unogame.models.Card;
import com.example.unogame.models.DeckOfCards;
import com.example.unogame.models.Game;
import com.example.unogame.models.Player;
import com.example.unogame.models.User;
import com.example.unogame.models.Utils;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.EventListener;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.FirebaseFirestoreException;
import com.google.firebase.firestore.ListenerRegistration;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;
import com.google.firebase.firestore.SetOptions;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

public class GameRoomFragment extends Fragment {

    private GameRoomViewModel mViewModel;

    GameRoomFragmentBinding binding;

    IGameRoomScreen mGameRoomScreen;

    private FirebaseAuth mAuth;

    final private String TAG = "demo";

    NavController navController;
    FirebaseFirestore db;

    Map<String, String> playersIdMap;
    User currentUser;

    String gameDocumentId;
    Game game;

    public static GameRoomFragment newInstance() {
        return new GameRoomFragment();
    }

    @Override
    public void onAttach(@NonNull Context context) {
        super.onAttach(context);
        if (context instanceof IGameRoomScreen) {
            mGameRoomScreen = (IGameRoomScreen) context;
        } else {
            throw new RuntimeException(context.toString());
        }
        db = FirebaseFirestore.getInstance();
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            gameDocumentId = getArguments().getString("gameId");
            game = (Game) getArguments().getSerializable("game");
        }
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {

        Log.d(TAG, "onCreateView: gameDocumentId " + gameDocumentId);
        currentUser = mGameRoomScreen.getUser();
        getActivity().setTitle("Game Room");

        binding = GameRoomFragmentBinding.inflate(inflater, container, false);
        View view = binding.getRoot();

        binding.player1TexViewId.setText(game.getPlayer1().getName());
        binding.player2TexViewId.setText(game.getPlayer2().getName());

        getGameData();

        return view;
    }

    public void getGameData(){
        CollectionReference ddb = db.collection(Utils.DB_GAME);
        ddb.document(gameDocumentId).addSnapshotListener(new EventListener<DocumentSnapshot>() {
            @Override
            public void onEvent(@Nullable DocumentSnapshot value, @Nullable FirebaseFirestoreException error) {
                if (error != null) {
                    Toast.makeText(getActivity(), error.getMessage(), Toast.LENGTH_SHORT).show();
                    return;
                }
                if(value == null){
                    return;
                }
                Map<String, Object> documentMap = value.getData();

                String player;
                String playerType;
                if(currentUser.getId().equals(game.getPlayer1().getId())) {
                    player = "player1Deck";
                    playerType = "player1";
                } else {
                    player = "player2Deck";
                    playerType = "player2";
                }


                /* To disable/enable layout on the basis of turn */
                if( playerType.equals(documentMap.get("turn"))){
                    getActivity().getWindow().clearFlags(WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE);
                }else
                {
                    getActivity().getWindow().setFlags(WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE, WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE);
                }


                String[] arr = documentMap.get("topCard").toString().split(""); //arr first element is card color and second is card faceValue

                int color = Color.WHITE;
                if(arr[0].equals("B"))
                    color = Color.BLUE;
                else if(arr[0].equals("G"))
                    color = Color.GREEN;
                else if(arr[0].equals("Y"))
                    color = Color.YELLOW;
                else if(arr[0].equals("R"))
                    color = Color.RED;
                else if(arr[0].equals("W"))
                    arr[1] = arr[1]+arr[2];  //for wild cards

                binding.cardPlayed.setCardBackgroundColor(color);
                binding.topCard.setText(arr[1]);

                ArrayList<String> tableDeck = (ArrayList<String>) documentMap.get("tableDeck");

                ArrayList<String> playerDeck = (ArrayList<String>) documentMap.get(player);
                binding.recyclerView.setLayoutManager(new LinearLayoutManager(getContext(), LinearLayoutManager.HORIZONTAL, false));
                binding.recyclerView.setAdapter(new RecyclerViewAdapter(playerDeck, value.getId(), player, documentMap.get("topCard").toString(), playerType, tableDeck ));

            }
        });

    }


    public void getPlayerDetails(){

        CollectionReference ddb = db.collection(Utils.DB_GAME);
        ddb.get().addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
            @Override
            public void onComplete(@NonNull Task<QuerySnapshot> task) {
                if (task.isSuccessful()) {

                    playersIdMap = new HashMap<>();

                    for (QueryDocumentSnapshot snapshot : task.getResult()) {

                        Map<String, Object> snapshotData = snapshot.getData();
                        Game game = snapshot.toObject(Game.class);

                        binding.player1TexViewId.setText(game.getPlayer1().getName());
                        binding.player2TexViewId.setText(game.getPlayer2().getName());

                        playersIdMap.put("player1", game.getPlayer1().getId());
                        playersIdMap.put("player2", game.getPlayer2().getId());

                        gameDocumentId = snapshot.getId();

                    }
                } else {
                    task.getException().printStackTrace();
                }
            }
        });

    }


    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        mViewModel = new ViewModelProvider(this).get(GameRoomViewModel.class);
        // TODO: Use the ViewModel
    }

    public interface IGameRoomScreen {
        User getUser();

    }

}