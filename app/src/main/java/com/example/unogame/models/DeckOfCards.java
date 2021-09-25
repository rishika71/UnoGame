package com.example.unogame.models;

import android.util.Log;

import java.util.ArrayList;

public class DeckOfCards {

     private ArrayList<Card> deck;
     private int numOfCardsInDeck;

    public DeckOfCards() {
        this.deck = new ArrayList<Card>(48);
    }

    public ArrayList<Card> getDeck() {
        return deck;
    }

    public void setDeck(ArrayList<Card> deck) {
        this.deck = deck;
    }

    public int getNumOfCardsInDeck() {
        return numOfCardsInDeck;
    }

    public void setNumOfCardsInDeck(int numOfCardsInDeck) {
        this.numOfCardsInDeck = numOfCardsInDeck;
    }


    public  ArrayList<Card> createDeck(){

        Card.CardColor colors[] = Card.CardColor.values();
        Card.FaceValue faceValue[] =  Card.FaceValue.values();

        /* Add all the cards except draw four cards*/
        for(int i = 0; i<colors.length - 1; i++){
            for(int j = 0; j<faceValue.length - 1; j++){
                deck.add(new Card(colors[i].toString(), faceValue[j].toString()));
            }
        }

        /*Add draw 4 cards to the deck */
        for(int i = 0; i<4; i++){
            deck.add(new Card(Card.CardColor.wild.toString(), Card.FaceValue.drawFour.toString()));
        }

        return deck;
    }



    public ArrayList<Card> shuffleDeck(){
        return deck;
    }


    @Override
    public String toString() {
        return "DeckOfCards{" +
                "deck=" + deck +
                ", numOfCardsInDeck=" + numOfCardsInDeck +
                '}';
    }

}
