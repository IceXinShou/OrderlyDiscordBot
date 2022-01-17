package com.ice.main.util;

public class QuestionStep {
    public String minecraftID;
    public String chineseNick;
    public String englishNick;
    public String infoMessageID;
    public boolean playMinecraft;
    private int step;
    private String messageID;

    public QuestionStep(int step, String messageID) {
        this.step = step;
        this.messageID = messageID;
    }

    public void setInfoMessageID(String infoMessageID) {
        this.infoMessageID = infoMessageID;
    }

    public int getStep() {
        return step;
    }

    public void setStep(int step) {
        this.step = step;
    }

    public String getMessageID() {
        return messageID;
    }

    public void setMessageID(String messageID) {
        this.messageID = messageID;
    }
}
