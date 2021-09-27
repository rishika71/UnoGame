package com.example.unogame;

import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.navigation.NavController;
import androidx.navigation.Navigation;

import com.example.unogame.databinding.FragmentGameScreenBinding;
import com.example.unogame.models.Game;
import com.example.unogame.models.Player;
import com.example.unogame.models.User;
import com.example.unogame.models.Utils;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.dialog.MaterialAlertDialogBuilder;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.EventListener;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.FirebaseFirestoreException;
import com.google.firebase.firestore.ListenerRegistration;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;

public class GameScreenFragment extends Fragment {

    private FragmentGameScreenBinding binding;

    IGameScreen mGameScreen;

    private FirebaseAuth mAuth;

    final private String TAG = "demo";

    NavController navController;
    ProgressDialog p;

    User currentUser;
    boolean progressBarFlag = false;

    FirebaseFirestore db;
    ListenerRegistration lr;


    @Override
    public void onAttach(@NonNull Context context) {
        super.onAttach(context);
        if (context instanceof IGameScreen) {
            mGameScreen = (IGameScreen) context;
        } else {
            throw new RuntimeException(context.toString());
        }
        db = FirebaseFirestore.getInstance();
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setHasOptionsMenu(true);
    }

    @Override
    public View onCreateView(
            LayoutInflater inflater, ViewGroup container,
            Bundle savedInstanceState
    ) {

        currentUser = mGameScreen.getUser();
        getActivity().setTitle(R.string.unogame);

        binding = FragmentGameScreenBinding.inflate(inflater, container, false);
        View view = binding.getRoot();

        navController = Navigation.findNavController(getActivity(), R.id.fragmentContainerView2);

        binding.startGameButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                createGame();
            }
        });
        listenGame();
        return view;

    }

    public void createGame(){

        HashMap<String, Object > data = new HashMap<>();
        data.put("status", "created");
        data.put("player1", new Player(currentUser.getId(), currentUser.getFirstname() + " " + currentUser.getLastname() ));
        data.put("createdAt", new Date());

        db.collection(Utils.DB_GAME)
                .document()
                .set(data)
                .addOnCompleteListener(new OnCompleteListener<Void>() {
                    @Override
                    public void onComplete(@NonNull Task<Void> task) {
                    }
                });
    }

    public void listenGame(){

             lr = db.collection(Utils.DB_GAME).addSnapshotListener(new EventListener<QuerySnapshot>() {
            @Override
            public void onEvent(@Nullable QuerySnapshot value, @Nullable FirebaseFirestoreException error) {
                if (error != null) {
                    Toast.makeText(getActivity(), error.getMessage(), Toast.LENGTH_SHORT).show();
                    return;
                }
                if(value == null){
                    return;
                }

                for (QueryDocumentSnapshot doc : value) {
                    Game game = doc.toObject(Game.class);
                   // Log.d(TAG, "onEvent: status : " + game.getStatus() );

                    if(game.getStatus().equals("created"))
                    {
                            progressBarFlag = true;
                            p = new ProgressDialog(getContext());

                            if(game.getPlayer1().getId().equals(currentUser.getId())){
                                //Log.d(TAG, "onEvent: game creator user");
                                p.setMessage("Waiting for players to join....");
                                p.setIndeterminate(false);
                                p.setCancelable(false);
                                p.show();

                                /*
                                 Runnable progressRunnable = new Runnable() {
                                    @Override
                                    public void run() {
                                        p.cancel();
                                    }
                                };
                                Handler pdCanceller = new Handler();
                                pdCanceller.postDelayed(progressRunnable, 10000); //for 10 secs
                                 */

                            }
                            else{

                                //Log.d(TAG, "onEvent:  other user " );
                                new MaterialAlertDialogBuilder(getContext())
                                        .setTitle("Game Request")
                                        .setMessage("Do you want to play UNO Game with your friends ?")
                                        .setPositiveButton("Accept", new DialogInterface.OnClickListener() {
                                            @Override
                                            public void onClick(DialogInterface dialogInterface, int i) {

                                                ArrayList<String> shuffledDeck = getShuffledDeck();

                                                String topCard = shuffledDeck.get(0);
                                                shuffledDeck.remove(0);

                                                ArrayList<String> player1Deck = new ArrayList<>();
                                                for(int j = 0; j<7 ;j++){
                                                    player1Deck.add(shuffledDeck.get(j));
                                                    shuffledDeck.remove(j);
                                                }

                                                ArrayList<String> player2Deck = new ArrayList<>();
                                                for(int k = 0; k<7 ;k++) {
                                                    player2Deck.add(shuffledDeck.get(k));
                                                    shuffledDeck.remove(k);
                                                }

                                                ArrayList<String> usedCards = new ArrayList<>();

                                                db.collection(Utils.DB_GAME)
                                                        .document(doc.getId())
                                                        .update("status", "started", "player2",new Player(currentUser.getId(), currentUser.getFirstname() + " " + currentUser.getLastname())
                                                                , "topCard", topCard
                                                                , "turn" , "player1"
                                                                , "tableDeck" , shuffledDeck
                                                                , "player1Deck" , player1Deck
                                                                , "player2Deck" , player2Deck
                                                                , "usedCards" , usedCards.add(topCard)
                                                                    );

                                                dialogInterface.dismiss();
                                            }
                                        })
                                        .setNegativeButton("Decline", new DialogInterface.OnClickListener() {
                                            @Override
                                            public void onClick(DialogInterface dialogInterface, int i) {
                                                dialogInterface.dismiss();
                                            }
                                        })
                                        .show();

                            }

                    }else if(game.getStatus().equals("started") ){

                        if(progressBarFlag)
                            p.hide();

                        Bundle bundle = new Bundle();
                        bundle.putString("gameId", doc.getId());
                        bundle.putSerializable("game", game);
                        navController.navigate(R.id.action_gameScreenFragment_to_gameRoomFragment, bundle);
                        lr.remove();

                    }else if(game.getStatus().equals("finished")){

                        // show game screen with start game button

                    }

                }


            }
        });

    }

    /*
    ON game start, set the table deck, player cards, top card
     */
    public ArrayList<String> getShuffledDeck(){
        ArrayList<String> cardList = new ArrayList<>();
        String cardColor[] = {"B", "G","R", "Y", "W" };
        String cardFaceValue[] = {"0", "1","2", "3", "4", "5", "6", "7","8","9","S","D4" };

        /* Add all the cards except draw four cards*/

        for(int i = 0; i<cardColor.length-1; i++){
            for(int j = 0; j<cardFaceValue.length-1;j++){
                cardList.add(cardColor[i]+cardFaceValue[j]);
            }
        }
        /*Add draw 4 cards to the deck */
        for(int i = 0; i<4; i++){
            cardList.add("Wd4");
        }

        ArrayList<String> shuffledDeck = shuffleDeck(cardList);
        return shuffledDeck;
       }


    /*Shuffle the deck */
    public ArrayList<String> shuffleDeck(ArrayList<String> cardList){
        Collections.shuffle(cardList);
        return  cardList;
    }


    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.action_profile:
                navController.navigate(R.id.action_gameScreenFragment_to_userProfileFragment);
                return true;
            case R.id.action_friends:
                navController.navigate(R.id.action_gameScreenFragment_to_usersFragment);
                return true;
            case R.id.action_logout:
                FirebaseAuth.getInstance().signOut();
                navController.navigate(R.id.action_gameScreenFragment_to_loginFragment);
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }

    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        inflater.inflate(R.menu.menu_main, menu);
        super.onCreateOptionsMenu(menu, inflater);
        }


    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }

    public interface IGameScreen {
        User getUser();

    }

}
