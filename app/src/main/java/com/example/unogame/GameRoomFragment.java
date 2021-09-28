package com.example.unogame;

import androidx.lifecycle.ViewModelProvider;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
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
import com.google.android.material.dialog.MaterialAlertDialogBuilder;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.EventListener;
import com.google.firebase.firestore.FieldValue;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.FirebaseFirestoreException;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

public class GameRoomFragment extends Fragment {


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


        currentUser = mGameRoomScreen.getUser();
        getActivity().setTitle("Game Room");

        binding = GameRoomFragmentBinding.inflate(inflater, container, false);
        View view = binding.getRoot();

        navController = Navigation.findNavController(getActivity(), R.id.fragmentContainerView2);

        binding.player1TexViewId.setText(game.getPlayer1().getName());
        binding.player2TexViewId.setText(game.getPlayer2().getName());


        if (game.getPlayer1().getPhotoref() != null) {
            StorageReference storageReference = FirebaseStorage.getInstance().getReference().child(game.getPlayer1().getId()).child(game.getPlayer1().getPhotoref());
            GlideApp.with(view)
                    .load(storageReference)
                    .into(binding.player1Image);
        } else {
            GlideApp.with(view)
                    .load(R.drawable.profile_image)
                    .into(binding.player1Image);
        }

        if (game.getPlayer2().getPhotoref() != null) {
            StorageReference storageReference = FirebaseStorage.getInstance().getReference().child(game.getPlayer2().getId()).child(game.getPlayer2().getPhotoref());
            GlideApp.with(view)
                    .load(storageReference)
                    .into(binding.player2Image);
        } else {
            GlideApp.with(view)
                    .load(R.drawable.profile_image)
                    .into(binding.player2Image);
        }


        binding.deck.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                pickCardFromDeck();
            }
        });

        binding.skipTurn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                onSkipTurn();
            }
        });

        binding.exit.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                new MaterialAlertDialogBuilder(getContext())
                        .setTitle("EXIT")
                        .setMessage("Do you want to quit?")
                        .setPositiveButton("YES", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialogInterface, int i) {
                                String winner;
                                if(currentUser.getId().equals(game.getPlayer1().getId()))
                                    winner = game.getPlayer2().getName();
                                else
                                    winner = game.getPlayer1().getName();


                                Toast.makeText(getContext(),"You Lost the match!!!", Toast.LENGTH_LONG).show();
                                db.collection(Utils.DB_GAME)
                                        .document(gameDocumentId)
                                        .update("status", "finished"
                                                ,"winner", winner
                                        );

                                dialogInterface.dismiss();

                            }
                        })
                        .setNegativeButton("NO", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialogInterface, int i) {
                                dialogInterface.dismiss();
                            }
                        }).show();
            }
        });

        getGameData();

        return view;
    }

    public void onSkipTurn(){
        CollectionReference ddb = db.collection(Utils.DB_GAME);
        ddb.document(gameDocumentId).get().addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
            @Override
            public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                Map<String, Object> documentMap = task.getResult().getData();
                String documentId = task.getResult().getId();

                if(documentMap.get("turn").toString().equals("player1")){
                    db.collection(Utils.DB_GAME)
                            .document(documentId)
                            .update("turn", "player2"
                            );
                }else{
                    db.collection(Utils.DB_GAME)
                            .document(documentId)
                            .update("turn", "player1"
                            );
                }

            }
        });
    }

    public void pickCardFromDeck(){
        Toast.makeText(getContext(),"A new card has been picked from the deck", Toast.LENGTH_SHORT).show();
        CollectionReference ddb = db.collection(Utils.DB_GAME);
        ddb.document(gameDocumentId).get().addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
            @Override
            public void onComplete(@NonNull Task<DocumentSnapshot> task) {

                Map<String, Object> documentMap = task.getResult().getData();
                String documentId = task.getResult().getId();

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


                  /* table deck is empty then usedCards should become table deck
                   not empty then check it so that it should match the top card
                   */
                    if(tableDeck.isEmpty()){
                       // Log.d(TAG, "onEvent: Empty DECK");

                        Collections.shuffle(usedCards);

                        for(int j = 0; j<usedCards.size(); j++){
                            if(usedCards.get(j).equals("R+4") || usedCards.get(j).equals("B+4") || usedCards.get(j).equals("G+4") || usedCards.get(j).equals("Y+4"))
                                newTableDeck.add("W+4");
                            else
                                newTableDeck.add(usedCards.get(j));
                        }

                        db.collection(Utils.DB_GAME)
                                .document(documentId)
                                .update("tableDeck", newTableDeck
                                        ,"usedCards", new ArrayList<String>()
                                );
                    }else{
                       // Log.d(TAG, "onEvent: Not Empty DECK");
                        String pickedCard = tableDeck.get(0);
                        String[] array = pickedCard.split(""); //arr first element is card color and second is card faceValue

                        if(array[0].equals(topcard_color) || array[1].equals(topcard_facevalue) ){

                            if(array[1].equals("S")) {
                                nextPlayerTurn = currentPlayer;
                            }


                            db.collection(Utils.DB_GAME)
                                    .document(documentId)
                                    .update("topCard", pickedCard
                                            ,"turn", nextPlayerTurn
                                            , "tableDeck", FieldValue.arrayRemove(pickedCard)
                                            ,"usedCards", FieldValue.arrayUnion(pickedCard)
                                    );

                        }else{

                            // add picked card in player's deck
                            db.collection(Utils.DB_GAME)
                                    .document(documentId)
                                    .update("turn", nextPlayerTurn
                                            , "tableDeck", FieldValue.arrayRemove(pickedCard)
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
                    // Game status is finished/End Game
                    navController.navigate(R.id.action_gameRoomFragment_to_gameScreenFragment);
                }

               ArrayList<String> player1Deck = (ArrayList<String>)documentMap.get("player1Deck");
                ArrayList<String> player2Deck = (ArrayList<String>)documentMap.get("player2Deck");


               if(player1Deck.isEmpty()){

                   if(currentUser.getId().equals(game.getPlayer1().getId()))
                       popupGameWinMessage();
                   else
                       popupGameLostMessage();

                   db.collection(Utils.DB_GAME)
                           .document(value.getId())
                           .update("winner", game.getPlayer1().getName()
                                   ,"status", "finished"
                           );

               }else if(player2Deck.isEmpty()){

                   if(currentUser.getId().equals(game.getPlayer2().getId()))
                       popupGameWinMessage();
                   else
                       popupGameLostMessage();


                   db.collection(Utils.DB_GAME)
                           .document(value.getId())
                           .update("winner", game.getPlayer2().getName()
                                   ,"status", "finished"
                           );
               } else{
                    /*
                        if non of the players deck is empty
                     */

                   String currentPlayerDeck;
                   String currentPlayer;
                   if(currentUser.getId().equals(game.getPlayer1().getId())) {
                       currentPlayerDeck = "player1Deck";
                       currentPlayer = "player1";
                   } else {
                       currentPlayerDeck = "player2Deck";
                       currentPlayer = "player2";
                   }

                   if(documentMap.get("turn").toString().equals("player1")){
                       binding.player1TexViewId.setTextColor(Color.RED);
                   }else{
                       binding.player1TexViewId.setTextColor(getResources().getColor(R.color.blue));
                   }

                   if(documentMap.get("turn").toString().equals("player2")){
                       binding.player2TexViewId.setTextColor(Color.RED);
                   }else{
                       binding.player2TexViewId.setTextColor(getResources().getColor(R.color.blue));
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

                   ArrayList<String> playerDeck = (ArrayList<String>) documentMap.get(currentPlayerDeck);


                   String topCard = documentMap.get("topCard").toString();
                   String[] arr = topCard.split(""); //arr first element is card color and second is card faceValue

                   int color = Color.WHITE;
                   if(arr[0].equals("B"))
                       color = getResources().getColor(R.color.blue);
                   else if(arr[0].equals("G"))
                       color = getResources().getColor(R.color.green);
                   else if(arr[0].equals("Y"))
                       color = getResources().getColor(R.color.yellow);
                   else if(arr[0].equals("R"))
                       color = getResources().getColor(R.color.red);
                   else if(arr[0].equals("W"))
                   {
                       color = Color.BLACK;
                       arr[1] = arr[1]+arr[2];  //for wild cards
                   }

                   //binding.cardPlayed.setCardBackgroundColor(color);
                   binding.cardPlayed.setStrokeColor(color);
                   binding.topCard.setText(arr[1]);
                   binding.topCard.setTextColor(color);

                   binding.recyclerView.setLayoutManager(new LinearLayoutManager(getContext(), LinearLayoutManager.HORIZONTAL, false));
                   binding.recyclerView.setAdapter(new RecyclerViewAdapter(playerDeck, value.getId(), currentPlayerDeck, topCard, currentPlayer, tableDeck ));


               }


            }
        });

    }

    public void popupGameWinMessage(){
        AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(getContext());
        alertDialogBuilder.setTitle("WINNER")
                          .setMessage("You won the game !!!!")
                            .setIcon(R.drawable.ic_baseline_auto_awesome_24);

        AlertDialog alertDialog = alertDialogBuilder.create();
        alertDialog.setCanceledOnTouchOutside(true);
        alertDialog.show();
    }

    public void popupGameLostMessage(){
        AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(getContext());
        alertDialogBuilder.setTitle("SORRY")
                .setMessage("You Lost the game !!!!")
                .setIcon(R.drawable.sad);

        AlertDialog alertDialog = alertDialogBuilder.create();
        alertDialog.setCanceledOnTouchOutside(true);
        alertDialog.show();
    }

    public interface IGameRoomScreen {
        User getUser();

    }

}