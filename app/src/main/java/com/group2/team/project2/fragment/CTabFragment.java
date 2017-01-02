package com.group2.team.project2.fragment;

import android.app.NotificationManager;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.annotation.Nullable;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.app.Fragment;
import android.support.v7.app.AlertDialog;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import com.facebook.AccessToken;
import com.facebook.GraphRequest;
import com.facebook.GraphResponse;
import com.google.firebase.iid.FirebaseInstanceId;
import com.group2.team.project2.R;
import com.group2.team.project2.object.DutchPay;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;

public class CTabFragment extends Fragment {

    private FloatingActionButton fab;
    private AutoCompleteTextView autoComplete;
    private LinearLayout layout;

    private static ArrayList<DutchPay> newPays = new ArrayList<>();
    private ArrayList<Thread> threads = new ArrayList<>();
    private ArrayList<String> sendEmails = new ArrayList<>();
    private String mEmail;
    private IntentFilter filter;

    private BroadcastReceiver receiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            String email = intent.getStringExtra("email"), name = intent.getStringExtra("name"), time = intent.getStringExtra("time"),
                    amount = intent.getStringExtra("amount"), account = intent.getStringExtra("account");
            boolean isNew = intent.getBooleanExtra("isNew", true);
            solveNewPay(new DutchPay(email, name, account, amount, time, isNew));
            abortBroadcast();
        }
    };

    private Handler autoCompleteHandler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            try {
                Log.i("cs496test", (String) msg.obj);
                JSONArray array = new JSONArray((String) msg.obj);
                ArrayAdapter<String> adapter = new ArrayAdapter<>(getContext(), android.R.layout.simple_dropdown_item_1line);
                for (int i = 0; i < array.length(); i++)
                    adapter.add(array.getJSONObject(i).getString("email"));
                autoComplete.setAdapter(adapter);
            } catch (JSONException e) {
                e.printStackTrace();
            }
        }
    };

    private Handler checkHandler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            if (msg.arg1 == 't') {
                final String email = (String) msg.obj;
                sendEmails.add(email);

                final TextView textView = new TextView(getContext());
                textView.setText(email);
                LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.WRAP_CONTENT, LinearLayout.LayoutParams.WRAP_CONTENT);
                int margin = getResources().getDimensionPixelSize(R.dimen.c_add_textview_margin);
                params.setMargins(margin, margin, margin, margin);
                textView.setLayoutParams(params);
                textView.setBackgroundResource(R.drawable.c_add_textview_background);
                textView.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        layout.removeView(textView);
                        sendEmails.remove(email);
                    }
                });
                layout.addView(textView);
                autoComplete.setText("");
            } else {
                Toast.makeText(getContext(), R.string.c_add_toast_unavailable, Toast.LENGTH_SHORT).show();
            }
        }
    };

    public CTabFragment() {
    }

    public static CTabFragment newInstance() {
        return new CTabFragment();
    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);

        AccessToken token = AccessToken.getCurrentAccessToken();
        GraphRequest graphRequest = GraphRequest.newMeRequest(token, new GraphRequest.GraphJSONObjectCallback() {
            @Override
            public void onCompleted(JSONObject jsonObject, GraphResponse graphResponse) {
                try {
                    mEmail = jsonObject.getString("email");
                } catch (JSONException e) {
                    e.printStackTrace();
                }
                sendTokenToServer();
            }
        });
        Bundle param = new Bundle();
        param.putString("fields", "email");
        graphRequest.setParameters(param);
        graphRequest.executeAsync();

        filter = new IntentFilter();
        filter.addAction(getString(R.string.intent_action_broadcast_push));
        filter.setPriority(1);
    }

    @Override
    public void onStart() {
        super.onStart();

        NotificationManager notificationManager = (NotificationManager) getActivity().getSystemService(Context.NOTIFICATION_SERVICE);

        for (int i = 0; i < newPays.size(); i++)
            notificationManager.cancel(newPays.get(i).getNotification());
        while (!newPays.isEmpty())
            solveNewPay(newPays.get(newPays.size() - 1));
        getActivity().registerReceiver(receiver, filter);
    }

    @Override
    public View onCreateView(final LayoutInflater inflater, ViewGroup container, final Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.fragment_c, container, false);
        fab = (FloatingActionButton) rootView.findViewById(R.id.c_fab_add);

        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                sendEmails.clear();
                final View view = inflater.inflate(R.layout.dialog_c_add, null);
                layout = (LinearLayout) view.findViewById(R.id.c_add_linearLayout);
                autoComplete = (AutoCompleteTextView) view.findViewById(R.id.c_add_autoComplete);
                final Button button = (Button) view.findViewById(R.id.c_add_button);
                final EditText editTextTotal = (EditText) view.findViewById(R.id.c_add_editText_total),
                        editTextPerson = (EditText) view.findViewById(R.id.c_add_editText_person),
                        editTextAccount = (EditText) view.findViewById(R.id.c_add_editText_account);
                final Spinner spinner = (Spinner) view.findViewById(R.id.c_add_spinner);
                setAutoCompleteList();

                button.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        String newEmail = autoComplete.getText().toString();
                        if (newEmail.equals(mEmail)) {
                            Toast.makeText(getContext(), R.string.c_add_toast_your, Toast.LENGTH_SHORT).show();
                        } else if (sendEmails.contains(newEmail)) {
                            Toast.makeText(getContext(), R.string.c_add_toast_already, Toast.LENGTH_SHORT).show();
                        } else {
                            checkEmail(newEmail);
                        }
                    }
                });
                editTextPerson.addTextChangedListener(new TextWatcher() {
                    @Override
                    public void beforeTextChanged(CharSequence s, int start, int count, int after) {
                    }

                    @Override
                    public void onTextChanged(CharSequence s, int start, int before, int count) {
                    }

                    @Override
                    public void afterTextChanged(Editable s) {
                        String total = editTextTotal.getText().toString();
                        if (s.toString().length() == 0) {
                            if (total.length() != 0)
                                editTextTotal.setText("");
                            return;
                        }
                        int x = Integer.parseInt(s.toString()), y, n = sendEmails.size();
                        if (total.length() == 0)
                            y = 0;
                        else
                            y = Integer.parseInt(total);
                        int d = y - n * x;
                        Log.i("cs496test", d + "");
                        if (n > 0 && (d >= n || d < 0))
                            editTextTotal.setText(x * n + "");
                    }
                });
                editTextTotal.addTextChangedListener(new TextWatcher() {
                    @Override
                    public void beforeTextChanged(CharSequence s, int start, int count, int after) {
                    }

                    @Override
                    public void onTextChanged(CharSequence s, int start, int before, int count) {
                    }

                    @Override
                    public void afterTextChanged(Editable s) {
                        String person = editTextPerson.getText().toString();
                        if (s.toString().length() == 0) {
                            if (person.length() != 0)
                                editTextPerson.setText("");
                            return;
                        }
                        int x = Integer.parseInt(s.toString()), y, n = sendEmails.size();
                        if (person.length() == 0)
                            y = 0;
                        else
                            y = Integer.parseInt(person);
                        int d = x - n * y;
                        if (n > 0 && (d >= n || d < 0))
                            editTextPerson.setText(x / n + "");
                    }
                });
                new AlertDialog.Builder(getActivity())
                        .setView(view)
                        .setPositiveButton(R.string.c_add_positive, new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {

                            }
                        })
                        .setNegativeButton(R.string.c_add_negative, null)
                        .show();
            }
        });
        return rootView;
    }

    @Override
    public void onStop() {
        super.onStop();

        getActivity().unregisterReceiver(receiver);
    }

    @Override
    public void onDestroy() {
        super.onDestroy();

        for (Thread thread : threads)
            if (thread.isAlive())
                thread.interrupt();
    }

    private void sendTokenToServer() {
        final String token = FirebaseInstanceId.getInstance().getToken();
        new Thread() {
            @Override
            public void run() {
                threads.add(this);
                try {
                    //URL url = new URL("http://" + getString(R.string.server_ip) + ":" + getString(R.string.server_port));
                    URL url = new URL("http://143.248.49.156:3000");
                    HttpURLConnection connection = (HttpURLConnection) url.openConnection();

                    connection.setRequestMethod("POST");
                    connection.setDoOutput(true);
                    connection.setDoInput(true);
                    connection.setUseCaches(false);
                    connection.setDefaultUseCaches(false);
                    connection.setRequestProperty("tab", "C");
                    connection.setRequestProperty("type", "token");
                    connection.setRequestProperty("token", token);
                    connection.setRequestProperty("email", mEmail);

                    InputStream inputStream = connection.getInputStream();
                    byte[] arr = new byte[inputStream.available()];
                    inputStream.read(arr);
                    inputStream.close();
                } catch (MalformedURLException e) {
                    e.printStackTrace();
                } catch (IOException e) {
                    e.printStackTrace();
                }
                threads.remove(this);
            }
        }.start();
    }

    private void setAutoCompleteList() {
        new Thread() {
            @Override
            public void run() {
                threads.add(this);
                try {
                    //URL url = new URL("http://" + getString(R.string.server_ip) + ":" + getString(R.string.server_port));
                    URL url = new URL("http://143.248.49.156:3000");
                    HttpURLConnection connection = (HttpURLConnection) url.openConnection();

                    connection.setRequestMethod("GET");
                    connection.setDoOutput(false);
                    connection.setDoInput(true);
                    connection.setUseCaches(false);
                    connection.setDefaultUseCaches(false);
                    connection.setRequestProperty("tab", "C");
                    connection.setRequestProperty("type", "list");
                    connection.setRequestProperty("email", mEmail);

                    InputStream inputStream = connection.getInputStream();
                    ByteArrayOutputStream out = new ByteArrayOutputStream();

                    byte[] buf = new byte[1024 * 8];
                    int length;
                    while ((length = inputStream.read(buf)) != -1) {
                        out.write(buf, 0, length);
                    }
                    byte[] arr = out.toByteArray();
                    inputStream.close();

                    Message message = new Message();
                    message.obj = new String(arr);
                    autoCompleteHandler.sendMessage(message);
                } catch (MalformedURLException e) {
                    e.printStackTrace();
                } catch (IOException e) {
                    e.printStackTrace();
                }
                threads.remove(this);
            }
        }.start();
    }

    private void checkEmail(final String email) {
        new Thread() {
            @Override
            public void run() {
                threads.add(this);
                try {
                    //URL url = new URL("http://" + getString(R.string.server_ip) + ":" + getString(R.string.server_port));
                    URL url = new URL("http://143.248.49.156:3000");
                    HttpURLConnection connection = (HttpURLConnection) url.openConnection();

                    connection.setRequestMethod("GET");
                    connection.setDoOutput(false);
                    connection.setDoInput(true);
                    connection.setUseCaches(false);
                    connection.setDefaultUseCaches(false);
                    connection.setRequestProperty("tab", "C");
                    connection.setRequestProperty("type", "check");
                    connection.setRequestProperty("email", email);

                    InputStream inputStream = connection.getInputStream();
                    ByteArrayOutputStream out = new ByteArrayOutputStream();

                    byte[] buf = new byte[1024 * 8];
                    int length;
                    while ((length = inputStream.read(buf)) != -1) {
                        out.write(buf, 0, length);
                    }
                    byte[] arr = out.toByteArray();
                    inputStream.close();

                    Message message = new Message();
                    message.arg1 = new String(arr).charAt(0);
                    message.obj = email;
                    checkHandler.sendMessage(message);
                } catch (MalformedURLException e) {
                    e.printStackTrace();
                } catch (IOException e) {
                    e.printStackTrace();
                }
                threads.remove(this);
            }
        }.start();
    }

    public static void addPay(DutchPay pay) {
        newPays.add(pay);
    }

    private void solveNewPay(DutchPay pay) {
        newPays.remove(pay);
    }
}
