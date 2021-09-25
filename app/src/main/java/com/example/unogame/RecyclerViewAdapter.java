package com.example.unogame;

import android.graphics.Color;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import com.example.unogame.databinding.LayoutCardslistBinding;
import com.example.unogame.databinding.UsersLayoutBinding;
import com.example.unogame.models.Card;
import com.example.unogame.models.Player;
import com.example.unogame.models.User;
import com.example.unogame.models.Utils;
import com.google.firebase.firestore.EventListener;
import com.google.firebase.firestore.FieldValue;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.FirebaseFirestoreException;
import com.google.firebase.firestore.ListenerRegistration;
import com.google.firebase.firestore.QuerySnapshot;

import java.util.ArrayList;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.cardview.widget.CardView;
import androidx.recyclerview.widget.RecyclerView;

public class RecyclerViewAdapter extends RecyclerView.Adapter<RecyclerViewAdapter.CardViewHolder> {

    FirebaseFirestore db = FirebaseFirestore.getInstance();
    IRecyclerViewAdapter mRecyclerViewAdapter;
    LayoutCardslistBinding binding;

    ArrayList<String> cardsList;
    User currentUser;
    String documentId;
    String player;
    String topCard;

    final private String TAG = "demo";

    ListenerRegistration lr;

    public RecyclerViewAdapter(ArrayList<String> cardsList, String documentId, String player, String topCard) {
        this.cardsList = cardsList;
        this.documentId = documentId;
        this.player = player;
        this.topCard = topCard;
    }

    @NonNull
    @Override
    public CardViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {

        binding = LayoutCardslistBinding.inflate(LayoutInflater.from(parent.getContext()), parent, false);
        mRecyclerViewAdapter = (IRecyclerViewAdapter) parent.getContext();
        currentUser = mRecyclerViewAdapter.getUser();
        return new CardViewHolder(binding);

    }

    @Override
    public void onBindViewHolder(@NonNull CardViewHolder holder, int position) {

        String card;
        String color;
        String facevalue;

        card = cardsList.get(position); // Card
        String[] arr = card.split(""); //arr first element is card color and second is card faceValue

        color = arr[0];
        facevalue = arr[1];

        int cardColor = Color.WHITE;
        if(color.equals("B"))
            cardColor = Color.BLUE;
        else if(color.equals("G"))
            cardColor = Color.GREEN;
        else if(color.equals("Y"))
            cardColor = Color.YELLOW;
        else if(color.equals("R"))
            cardColor = Color.RED;
//        else if(color.equals("W"))
//        {
//            facevalue = arr[1] + arr[2];//for wild cards
//        }

        holder.binding.playerCard.setCardBackgroundColor(cardColor);
        holder.binding.textviewId.setText(facevalue);

      holder.binding.playerCard.setOnClickListener(new View.OnClickListener() {
          @Override
          public void onClick(View view) {
             // Log.d(TAG, "onClick: Card chosen " + card);

              /* Game Rules */

              String[] topCardArr = topCard.split(""); //arr first element is card color and second is card faceValue
              String topCardColor = topCardArr[0];
              String topCardFaceValue = topCardArr[1];

              if(color.equals(topCardColor) || facevalue.equals(topCardFaceValue)){
                  topCard = card;
                  db.collection(Utils.DB_GAME)
                          .document(documentId)
                          .update(  "topCard", topCard
                                  ,"usedCards", FieldValue.arrayUnion(card)
                                  ,player, FieldValue.arrayRemove(card)
                          );
              }else{
                  Toast.makeText(view.getContext(), "Choose another card!!!", Toast.LENGTH_SHORT).show();
              }



          }
      });
    }

    @Override
    public int getItemCount() {
        return this.cardsList.size();
    }

    public interface IRecyclerViewAdapter {

        User getUser();

    }

    public static class CardViewHolder extends RecyclerView.ViewHolder {

        LayoutCardslistBinding binding;

        public CardViewHolder(@NonNull LayoutCardslistBinding binding) {
            super(binding.getRoot());
            this.binding = binding;
        }


    }


    public void getGameData(){

        lr = db.collection(Utils.DB_GAME).addSnapshotListener(new EventListener<QuerySnapshot>() {
            @Override
            public void onEvent(@Nullable QuerySnapshot value, @Nullable FirebaseFirestoreException error) {
                if (error != null) {
                   // Toast.makeText(getActivity(), error.getMessage(), Toast.LENGTH_SHORT).show();
                    return;
                }
                if(value == null){
                    return;
                }


            }
        });
    }
}
