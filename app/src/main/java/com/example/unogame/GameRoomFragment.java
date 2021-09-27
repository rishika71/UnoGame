package com.example.unogame;

import androidx.lifecycle.ViewModelProvider;

import android.app.AlertDialog;
import android.content.Context;
import android.graphics.Color;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.navigation.NavController;
import androidx.navigation.Navigation;
import androidx.recyclerview.widget.LinearLayoutManager;

import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.Toast;

import com.example.unogame.databinding.GameRoomFragmentBinding;
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
import com.google.firebase.firestore.FieldValue;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.FirebaseFirestoreException;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

public class GameRoomFragment extends Fragment {

    private GameRoomViewModel mViewModel;

    GameRoomFragmentBinding binding;

    IGameRoomScreen mGameRoomScreen;

    final private String TAG = "demo";

    NavController navController;
    FirebaseFirestore db;

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

        navController = Navigation.findNavController(getActivity(), R.id.fragmentContainerView2);

        binding.player1TexViewId.setText(game.getPlayer1().getName());
        binding.player2TexViewId.setText(game.getPlayer2().getName());

        binding.deck.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                pickCardFromDeck();
            }
        });

        getGameData();

        return view;
    }

    public void pickCardFromDeck(){
        CollectionReference ddb = db.collection(Utils.DB_GAME);
        ddb.document(gameDocumentId).get().addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
            @Override
            public void onComplete(@NonNull Task<DocumentSnapshot> task) {

                Map<String, Object> documentMap = task.getResult().getData();
                String documnetId = task.getResult().getId();

                String currentPlayer;
                String playerDeck;
                String nextPlayerTurn;

                if(currentUser.getId().equals(game.getPlayer1().getId())) {
                    currentPlayer = "player1";
                    playerDeck = "player1Deck";
                    nextPlayerTurn = "player2";
                }
               else
                {
                    currentPlayer = "player2";
                    playerDeck = "player2Deck";
                    nextPlayerTurn = "player1";
                }

                ArrayList<String> tableDeck = (ArrayList<String>) documentMap.get("tableDeck");

                ArrayList<String> usedCards = (ArrayList<String>) documentMap.get("usedCards");

                ArrayList<String> newTableDeck = new ArrayList<>();

                String topCard = documentMap.get("topCard").toString();
                String[] arr = topCard.split(""); //arr first element is card color and second is card faceValue

                String topcard_color = arr[0];
                String topcard_facevalue = arr[1];

              // Log.d(TAG, "onEvent: Picking Card From the Deck ");

                  /* table deck is empty then usedCards should become table deck
                   not empty then check it so that it should match the top card
                   */
                    if(tableDeck.isEmpty()){
                       // Log.d(TAG, "onEvent: Empty DECK");

                        Collections.shuffle(usedCards);

                        for(int j = 0; j<usedCards.size(); j++){
                            if(usedCards.get(j).equals("Rd4") || usedCards.get(j).equals("Bd4") || usedCards.get(j).equals("Gd4") || usedCards.get(j).equals("Yd4"))
                                newTableDeck.add("Wd4");
                            else
                                newTableDeck.add(usedCards.get(j));
                        }

                        db.collection(Utils.DB_GAME)
                                .document(documnetId)
                                .update("tableDeck", newTableDeck
                                        ,"usedCards", new ArrayList<String>()
                                );
                    }else{
                       // Log.d(TAG, "onEvent: Not Empty DECK");
                        String pickedCard = tableDeck.get(0);
                        String[] array = pickedCard.split(""); //arr first element is card color and second is card faceValue

                        String pickedCard_color = array[0];
                        String pickedCard_facevalue = array[1];

                        if(pickedCard_color.equals(topcard_color) || pickedCard_facevalue.equals(topcard_facevalue) ){

                            if(pickedCard_facevalue.equals("S")) {
                                nextPlayerTurn = currentPlayer;
                            }


                            db.collection(Utils.DB_GAME)
                                    .document(documnetId)
                                    .update("topCard", topCard
                                            ,"turn", nextPlayerTurn
                                            , "tableDeck", FieldValue.arrayRemove(topCard)
                                            ,"usedCards", FieldValue.arrayUnion(topCard)
                                    );

                        }else{

                            // add picked card in player's deck
                            db.collection(Utils.DB_GAME)
                                    .document(documnetId)
                                    .update("turn", nextPlayerTurn
                                            , "tableDeck", FieldValue.arrayRemove(topCard)
                                            ,playerDeck, FieldValue.arrayUnion(pickedCard)
                                    );

                        }

                    }

            }
        });

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

               if (documentMap.get("status").toString().equals("finished")){
                    // Game status is finished
                    navController.navigate(R.id.action_gameRoomFragment_to_gameScreenFragment);
                }

               ArrayList<String> player1Deck = (ArrayList<String>) documentMap.get("player1Deck");
                ArrayList<String> player2Deck = (ArrayList<String>) documentMap.get("player2Deck");

               if(player1Deck.isEmpty()){

                   db.collection(Utils.DB_GAME)
                           .document(value.getId())
                           .update("winner", "player1"
                                   ,"status", "finished"
                           );

               }else if(player2Deck.isEmpty()){

                   db.collection(Utils.DB_GAME)
                           .document(value.getId())
                           .update("winner", "player2"
                                   ,"status", "finished"
                           );
               } else{
                    /*
                        if non of the players deck is empty
                     */

                   String player;
                   String playerType;
                   if(currentUser.getId().equals(game.getPlayer1().getId())) {
                       player = "player1Deck";
                       playerType = "player1";
                   } else {
                       player = "player2Deck";
                       playerType = "player2";
                   }

//
//                   /* To disable/enable layout on the basis of turn */
//                   if( playerType.equals(documentMap.get("turn"))){
//                       getActivity().getWindow().clearFlags(WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE);
//                   }else
//                   {
//                       getActivity().getWindow().setFlags(WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE, WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE);
//                   }

                   ArrayList<String> tableDeck = (ArrayList<String>) documentMap.get("tableDeck");

                   ArrayList<String> playerDeck = (ArrayList<String>) documentMap.get(player);



                   String topCard = documentMap.get("topCard").toString();
                   String[] arr = topCard.split(""); //arr first element is card color and second is card faceValue

                   String topcard_color = arr[0];

                   int color = Color.WHITE;
                   if(topcard_color.equals("B"))
                       color = Color.BLUE;
                   else if(topcard_color.equals("G"))
                       color = Color.GREEN;
                   else if(topcard_color.equals("Y"))
                       color = Color.YELLOW;
                   else if(topcard_color.equals("R"))
                       color = Color.RED;
                   else if(topcard_color.equals("W"))
                       arr[1] = arr[1]+arr[2];  //for wild cards

                   binding.cardPlayed.setCardBackgroundColor(color);
                   binding.topCard.setText(arr[1]);

                   binding.recyclerView.setLayoutManager(new LinearLayoutManager(getContext(), LinearLayoutManager.HORIZONTAL, false));
                   binding.recyclerView.setAdapter(new RecyclerViewAdapter(playerDeck, value.getId(), player, topCard, playerType, tableDeck ));


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