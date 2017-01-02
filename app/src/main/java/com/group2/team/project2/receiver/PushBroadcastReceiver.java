package com.group2.team.project2.receiver;

import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.media.RingtoneManager;
import android.net.Uri;
import android.support.v4.app.NotificationCompat;

import com.group2.team.project2.MainActivity;
import com.group2.team.project2.R;
import com.group2.team.project2.fragment.CTabFragment;
import com.group2.team.project2.object.PayDebt;

public class PushBroadcastReceiver extends BroadcastReceiver {

    private static final int REQUEST_ACTIVITY = 0;
    private static int notification = 0;

    @Override
    public void onReceive(Context context, Intent intent) {
        String email = intent.getStringExtra("email"), name = intent.getStringExtra("name"), time = intent.getStringExtra("time"),
                amount = intent.getStringExtra("amount"), account = intent.getStringExtra("account");
        boolean isNew = intent.getBooleanExtra("isNew", true);

        Intent in = new Intent(context, MainActivity.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
        PendingIntent pendingIntent = PendingIntent.getActivity(context, REQUEST_ACTIVITY, in, PendingIntent.FLAG_ONE_SHOT);

        Uri defaultSoundUri = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION);
        NotificationCompat.Builder notificationBuilder = new NotificationCompat.Builder(context)
                .setSmallIcon(R.mipmap.ic_launcher)
                .setContentTitle(isNew ? context.getString(R.string.c_notification_new) : context.getString(R.string.c_notification_old))
                .setContentText(name + " (" + email + ") 님이 " + amount + " 원을 " + account + " 계좌로 " +
                        (isNew ? context.getString(R.string.c_notification_text_new) : context.getString(R.string.c_notification_text_old)))
                .setAutoCancel(true)
                .setSound(defaultSoundUri)
                .setContentIntent(pendingIntent);

        NotificationManager notificationManager = (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);
        notificationManager.notify(notification, notificationBuilder.build());

        PayDebt pay = new PayDebt(email, name, account, amount, time, isNew);
        pay.setNotification(notification++);
        CTabFragment.addPay(pay);

        abortBroadcast();
    }
}
