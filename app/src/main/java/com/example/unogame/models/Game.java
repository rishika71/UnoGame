package com.example.unogame.models;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

public class Game implements Serializable {

    String status;
    Date createdAt;
    Player player1;
    Player player2;
    String topCard;
    String turn;
    ArrayList<String> tableDeck;
    ArrayList<String> player1Deck;
    ArrayList<String> player2Deck;
    ArrayList<String> usedCards;

    public Game(){

    }

    public Game(String status, Date createdAt, Player player1) {
        this.status = status;
        this.createdAt = createdAt;
        this.player1 = player1;
    }

    public String getTopCard() {
        return topCard;
    }

    public void setTopCard(String topCard) {
        this.topCard = topCard;
    }

    public String getTurn() {
        return turn;
    }

    public void setTurn(String turn) {
        this.turn = turn;
    }

    public ArrayList<String> getTableDeck() {
        return tableDeck;
    }

    public void setTableDeck(ArrayList<String> tableDeck) {
        this.tableDeck = tableDeck;
    }

    public ArrayList<String> getPlayer1Deck() {
        return player1Deck;
    }

    public void setPlayer1Deck(ArrayList<String> player1Deck) {
        this.player1Deck = player1Deck;
    }

    public ArrayList<String> getPlayer2Deck() {
        return player2Deck;
    }

    public void setPlayer2Deck(ArrayList<String> player2Deck) {
        this.player2Deck = player2Deck;
    }

    public ArrayList<String> getUsedCards() {
        return usedCards;
    }

    public void setUsedCards(ArrayList<String> usedCards) {
        this.usedCards = usedCards;
    }

    public Player getPlayer2() {
        return player2;
    }

    public void setPlayer2(Player player2) {
        this.player2 = player2;
    }


    public Player getPlayer1() {
        return player1;
    }

    public void setPlayer1(Player player1) {
        this.player1 = player1;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public Date getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(Date createdAt) {
        this.createdAt = createdAt;
    }

    @Override
    public String toString() {
        return "Game{" +
                "status='" + status + '\'' +
                ", createdAt=" + createdAt +
                ", player1=" + player1 +
                ", player2=" + player2 +
                ", topCard='" + topCard + '\'' +
                ", turn='" + turn + '\'' +
                ", tableDeck=" + tableDeck +
                ", player1Deck=" + player1Deck +
                ", player2Deck=" + player2Deck +
                ", usedCards=" + usedCards +
                '}';
    }
}
