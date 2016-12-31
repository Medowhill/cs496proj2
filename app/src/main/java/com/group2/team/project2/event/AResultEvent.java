package com.group2.team.project2.event;

import android.content.Intent;

public class AResultEvent {

    private int requestCode;
    private int resultCode;
    private Intent data;

    public static AResultEvent create(int requestCode, int resultCode, Intent intent) {
        return new AResultEvent(requestCode, resultCode, intent);
    }

    private AResultEvent(int requestCode, int resultCode, Intent data) {
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
