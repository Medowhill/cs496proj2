package com.group2.team.project2.object;

import java.util.ArrayList;

/**
 * Created by q on 2017-01-02.
 */

public class ReceiveDebt {

    private String account, amount, time, name;
    private ArrayList<String> emails, names;
    private boolean[] payed;
    private boolean allpayed;

    public ReceiveDebt(String name, String account, String amount, String time, ArrayList<String> emails, ArrayList<String> names) {
        this.name = name;
        this.account = account;
        this.amount = amount;
        this.time = time;
        this.emails = emails;
        this.names = names;
    }

    public ReceiveDebt(String name, String account, String amount, String time, ArrayList<String> emails, ArrayList<String> names, boolean[] payed, boolean allpayed) {
        this.name = name;
        this.account = account;
        this.amount = amount;
        this.time = time;
        this.emails = emails;
        this.names = names;
        this.payed = payed;
        this.allpayed = allpayed;
    }

    public void setPayed(int position, boolean pay) {this.payed[position] = pay;}

    public void setAllpayed() {this.allpayed=true;}

    public String getAccount() {
        return account;
    }

    public String getName() {
        return name;
    }

    public String getAmount() {
        return amount;
    }

    public String getTime() {
        return time;
    }

    public ArrayList<String> getEmails() {
        return emails;
    }

    public ArrayList<String> getNames() {
        return names;
    }

    public boolean[] getPayed() {
        return payed;
    }

    public boolean getAllPayed() { return allpayed; }
}
