package com.group2.team.project2.service;

import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.media.RingtoneManager;
import android.net.Uri;
import android.support.v4.app.NotificationCompat;
import android.util.Log;

import com.google.firebase.messaging.FirebaseMessagingService;
import com.google.firebase.messaging.RemoteMessage;
import com.group2.team.project2.LoginActivity;
import com.group2.team.project2.MainActivity;
import com.group2.team.project2.R;

import java.util.Map;

public class MyFirebaseMessagingService extends FirebaseMessagingService {

    @Override
    public void onMessageReceived(RemoteMessage remoteMessage) {

        Log.i("cs496test", remoteMessage.getData().toString());
        if (remoteMessage.getData().size() > 0) {
            Map<String, String> data = remoteMessage.getData();
            String email = data.get("email"), name = data.get("name"), account = data.get("account"), amount = data.get("amount"), time = data.get("time");
            boolean isNew = data.get("isNew").equals("t"), please = data.get("please").equals("t");

            if (please) {
                Intent intent = new Intent(getApplicationContext(), LoginActivity.class);
                intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                PendingIntent pendingIntent = PendingIntent.getActivity(getApplicationContext(), 0, intent, PendingIntent.FLAG_ONE_SHOT);

                Uri defaultSoundUri = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION);
                NotificationCompat.Builder notificationBuilder = new NotificationCompat.Builder(getApplicationContext())
                        .setSmallIcon(R.mipmap.ic_launcher)
                        .setContentTitle(getString(R.string.c_notification_please))
                        .setContentText(name + " 님이 " + amount + " 원을 " + account + " 계좌로 " + getString(R.string.c_notification_text_new))
                        .setAutoCancel(true)
                        .setSound(defaultSoundUri)
                        .setContentIntent(pendingIntent);

                NotificationManager notificationManager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
                notificationManager.notify(Integer.MAX_VALUE, notificationBuilder.build());
            } else {
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
}
