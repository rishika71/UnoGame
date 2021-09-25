package com.example.unogame.models;

public class Card {

    enum FaceValue{

        zero, one, two, three, four, five, six, seven, eight, nine, skip, drawFour;

        private static final FaceValue faceValueList[] = FaceValue.values();

        public static FaceValue getFaceValueList(int i) {
            return FaceValue.getFaceValueList(i);
        }

    }

    public enum CardColor{

        red, green, blue, yellow, wild;

        private static final CardColor colors[] = CardColor.values();

        public static CardColor getColors(int i) {
            return CardColor.getColors(i);
        }
    }

    private String cardColor;
    private String cardFaceValue;

    public Card(String cardColor, String cardFaceValue) {
        this.cardColor = cardColor;
        this.cardFaceValue = cardFaceValue;
    }


    public String getCardColor() {
        return cardColor;
    }

    public void setCardColor(String cardColor) {
        this.cardColor = cardColor;
    }

    public String getCardFaceValue() {
        return cardFaceValue;
    }

    public void setCardFaceValue(String cardFaceValue) {
        this.cardFaceValue = cardFaceValue;
    }

    @Override
    public String toString() {
        return "Card{" +
                "cardColor='" + cardColor + '\'' +
                ", cardFaceValue='" + cardFaceValue + '\'' +
                '}';
    }
}
