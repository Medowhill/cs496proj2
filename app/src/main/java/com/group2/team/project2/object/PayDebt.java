package com.group2.team.project2.object;

public class PayDebt {

    private String email, name, account, amount, time;
    private boolean isNew;
    private int notification;

    public PayDebt(String email, String name, String account, String amount, String time, boolean isNew) {
        this.email = email;
        this.name = name;
        this.account = account;
        this.amount = amount;
        this.time = time;
        this.isNew = isNew;
    }

    public String getEmail() {
        return email;
    }

    public String getName() {
        return name;
    }

    public String getAccount() {
        return account;
    }

    public String getAmount() {
        return amount;
    }

    public boolean isNew() {
        return isNew;
    }

    public String getTime() {
        return time;
    }

    public int getNotification() {
        return notification;
    }

    public void setNotification(int notification) {
        this.notification = notification;
    }
}
