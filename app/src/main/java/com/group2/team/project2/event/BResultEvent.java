package com.group2.team.project2.event;

import android.content.Intent;

public class BResultEvent {

    private int requestCode;
    private int resultCode;
    private Intent data;

    public static BResultEvent create(int requestCode, int resultCode, Intent intent) {
        return new BResultEvent(requestCode, resultCode, intent);
    }

    private BResultEvent(int requestCode, int resultCode, Intent data) {
        this.requestCode = requestCode;
        this.resultCode = resultCode;
        this.data = data;
    }

    public int getRequestCode() {
        return requestCode;
    }

    public int getResultCode() {
        return resultCode;
    }

    public Intent getData() {
        return data;
    }
}
