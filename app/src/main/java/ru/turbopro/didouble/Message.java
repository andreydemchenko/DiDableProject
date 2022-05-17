package ru.turbopro.didouble;

public class Message {

  private String message;
  private boolean isReceived;
  private String time;
  private String date;

  public Message(String message, boolean isReceived, String time, String date) {
    this.message = message;
    this.isReceived = isReceived;
    this.time = time;
    this.date = date;
  }

  public String getMessage() {
    return message;
  }

  public void setMessage(String message) {
    this.message = message;
  }

  public boolean getIsReceived() {
    return isReceived;
  }

  public void setIsReceived(boolean isReceived) {
    this.isReceived = isReceived;
  }

  public String getTime() {
    return time;
  }

  public void setTime(String time) {
    this.time = time;
  }

  public String getDate() {
    return date;
  }

  public void setDate(String date) {
    this.date = date;
  }
}