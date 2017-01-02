package com.group2.team.project2.service;

import android.content.Intent;

import com.google.firebase.messaging.FirebaseMessagingService;
import com.google.firebase.messaging.RemoteMessage;
import com.group2.team.project2.R;

import java.util.Map;

public class MyFirebaseMessagingService extends FirebaseMessagingService {

    @Override
    public void onMessageReceived(RemoteMessage remoteMessage) {

        if (remoteMessage.getData().size() > 0) {
            Map<String, String> data = remoteMessage.getData();
            String email = data.get("email"), name = data.get("name"), account = data.get("account"), amount = data.get("amount"), time = data.get("time");
            boolean isNew = data.get("isNew").equals("t");

            Intent intent = new Intent();
            intent.putExtra("email", email);
            intent.putExtra("name", name);
            intent.putExtra("account", account);
            intent.putExtra("amount", amount);
            intent.putExtra("time", time);
            intent.putExtra("isNew", isNew);
            intent.setAction(getString(R.string.intent_action_broadcast_push));
            sendOrderedBroadcast(intent, null);
        }
    }
}
