package com.group2.team.project2.fragment;

import android.app.Activity;
import android.app.NotificationManager;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.annotation.NonNull;
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
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import com.google.firebase.iid.FirebaseInstanceId;
import com.group2.team.project2.EventBus;
import com.group2.team.project2.R;
import com.group2.team.project2.ReceiveDebtActivity;
import com.group2.team.project2.adapter.payViewAdapter;
import com.group2.team.project2.adapter.receiveViewAdapter;
import com.group2.team.project2.event.CResultEvent;
import com.group2.team.project2.object.PayDebt;
import com.group2.team.project2.object.ReceiveDebt;
import com.squareup.otto.Subscribe;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;

public class CTabFragment extends Fragment {

    private static final int REQUEST_DETAIL = 100;

    private FloatingActionButton fab;

    private EditText editTextPerson, editTextTotal;
    private AutoCompleteTextView autoComplete;
    private LinearLayout layout;

    private static ArrayList<Integer> notifications = new ArrayList<>();
    private static String mEmail, mName;
    private SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd");
    private ArrayList<Thread> threads = new ArrayList<>();
    private ArrayList<ReceiveDebt> receiveDebts = new ArrayList<>();
    private ArrayList<String> sendEmails = new ArrayList<>(), mEmails = new ArrayList<>(), mNames = new ArrayList<>();
    private IntentFilter filter;
    private ListView payView;
    private ListView receiveView;
    private payViewAdapter payAdapter;
    private receiveViewAdapter receiveAdapter;

    private BroadcastReceiver receiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            String email = intent.getStringExtra("email"), name = intent.getStringExtra("name"), time = intent.getStringExtra("time"),
                    amount = intent.getStringExtra("amount"), account = intent.getStringExtra("account");
            boolean isNew = intent.getBooleanExtra("isNew", true);
            solveNewPay(new PayDebt(email, name, account, amount, time, isNew));
            abortBroadcast();
        }
    };

    private Handler autoCompleteHandler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            Log.i("cs496test", (String) msg.obj);
            ArrayAdapter<String> adapter = new ArrayAdapter<>(getContext(), android.R.layout.simple_dropdown_item_1line);
            try {
                JSONArray array = new JSONArray((String) msg.obj);
                for (int i = 0; i < array.length(); i++) {
                    String email = array.getJSONObject(i).getString("email"), name = array.getJSONObject(i).getString("name");
                    adapter.add(email);
                    adapter.add(name);
                    mEmails.add(email);
                    mNames.add(name);
                }
            } catch (JSONException e) {
                e.printStackTrace();
            }
            autoComplete.setAdapter(adapter);
        }
    };

    private Handler checkHandler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            if (msg.arg1 == 't') {
                final String email = (String) msg.obj, total = editTextTotal.getText().toString(), person = editTextPerson.getText().toString();
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

                        if (!sendEmails.isEmpty()) {
                            if (total.length() == 0) {
                                if (person.length() != 0)
                                    editTextTotal.setText(Integer.parseInt(person) * sendEmails.size() + "");
                            } else
                                editTextPerson.setText(Integer.parseInt(total) / sendEmails.size() + "");
                        }
                    }
                });
                layout.addView(textView);
                autoComplete.setText("");

                if (total.length() == 0) {
                    if (person.length() != 0)
                        editTextTotal.setText(Integer.parseInt(person) * sendEmails.size() + "");
                } else
                    editTextPerson.setText(Integer.parseInt(total) / sendEmails.size() + "");
            } else {
                Toast.makeText(getContext(), R.string.c_add_toast_unavailable, Toast.LENGTH_SHORT).show();
            }
        }
    };

    private Handler dataHandler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            try {
                JSONObject object = new JSONObject((String) msg.obj);
                viewPay(object.getJSONArray("pay"));
                viewReceive(object.getJSONArray("receive"));
                Log.i("cs496test", "login fin");
            } catch (JSONException e) {
                e.printStackTrace();
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

        EventBus.getInstance().register(this);
        filter = new IntentFilter();
        filter.addAction(getString(R.string.intent_action_broadcast_push));
        filter.setPriority(1);
    }

    @Override
    public void onStart() {
        super.onStart();

        getDataFromServer();

        NotificationManager notificationManager = (NotificationManager) getActivity().getSystemService(Context.NOTIFICATION_SERVICE);
        for (int i = 0; i < notifications.size(); i++)
            notificationManager.cancel(notifications.get(i));
        notifications.clear();

        getActivity().registerReceiver(receiver, filter);
    }

    @Override
    public View onCreateView(final LayoutInflater inflater, ViewGroup container, final Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.fragment_c, container, false);
        payView = (ListView) rootView.findViewById(R.id.payView);
        receiveView = (ListView) rootView.findViewById(R.id.receiveView);
        fab = (FloatingActionButton) rootView.findViewById(R.id.c_fab_add);
        receiveView.setOnItemClickListener(new receiveViewItemClickListener());

        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                sendEmails.clear();
                final View view = inflater.inflate(R.layout.dialog_c_add, null);
                layout = (LinearLayout) view.findViewById(R.id.c_add_linearLayout);
                autoComplete = (AutoCompleteTextView) view.findViewById(R.id.c_add_autoComplete);
                editTextTotal = (EditText) view.findViewById(R.id.c_add_editText_total);
                editTextPerson = (EditText) view.findViewById(R.id.c_add_editText_person);
                final Button button = (Button) view.findViewById(R.id.c_add_button);
                final EditText editTextAccount = (EditText) view.findViewById(R.id.c_add_editText_account);
                final Spinner spinner = (Spinner) view.findViewById(R.id.c_add_spinner);
                setAutoCompleteList();

                button.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        String newEmail = autoComplete.getText().toString();
                        int index = mNames.indexOf(newEmail);
                        if (index != -1)
                            newEmail = mEmails.get(index);
                        if (newEmail.equals(mEmail)) {
                            Toast.makeText(getContext(), R.string.c_add_toast_your, Toast.LENGTH_SHORT).show();
                            autoComplete.setText("");
                        } else if (sendEmails.contains(newEmail)) {
                            Toast.makeText(getContext(), R.string.c_add_toast_already, Toast.LENGTH_SHORT).show();
                            autoComplete.setText("");
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
                final AlertDialog alertDialog = new AlertDialog.Builder(getActivity())
                        .setView(view)
                        .setPositiveButton(R.string.c_add_positive, null)
                        .setNegativeButton(R.string.c_add_negative, null)
                        .create();
                alertDialog.setOnShowListener(new DialogInterface.OnShowListener() {
                    @Override
                    public void onShow(final DialogInterface dialog) {
                        Button buttonPositive = alertDialog.getButton(AlertDialog.BUTTON_POSITIVE);
                        buttonPositive.setOnClickListener(new View.OnClickListener() {
                            @Override
                            public void onClick(View v) {
                                if (sendEmails.isEmpty())
                                    Toast.makeText(getContext(), R.string.c_add_toast_noperson, Toast.LENGTH_SHORT).show();
                                else if (editTextPerson.getText().length() == 0)
                                    Toast.makeText(getContext(), R.string.c_add_toast_noamount, Toast.LENGTH_SHORT).show();
                                else if (editTextAccount.getText().length() == 0)
                                    Toast.makeText(getContext(), R.string.c_add_toast_noaccount, Toast.LENGTH_SHORT).show();
                                else {
                                    ArrayList<String> emails = new ArrayList<>(), names = new ArrayList<>();
                                    emails.addAll(sendEmails);
                                    for (String email : emails)
                                        names.add(mNames.get(mEmails.indexOf(email)));

                                    addDebt(new ReceiveDebt(mName, getResources().getStringArray(R.array.c_add_banks)[spinner.getSelectedItemPosition()]
                                            + " " + editTextAccount.getText().toString(), editTextPerson.getText().toString(),
                                            format.format(Calendar.getInstance().getTime()), emails, names));
                                    alertDialog.dismiss();
                                }
                            }
                        });
                    }
                });
                alertDialog.show();
            }
        });
        return rootView;
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        EventBus.getInstance().unregister(this);
    }

    @SuppressWarnings("unused")
    @Subscribe
    public void onActivityResultEvent(@NonNull CResultEvent event) {
        onActivityResult(event.getRequestCode(), event.getResultCode(), event.getData());
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        Log.i("cs496", requestCode + ", " + resultCode);
        switch (requestCode) {
            case REQUEST_DETAIL:
                if (resultCode == Activity.RESULT_OK) {
                    int position = data.getIntExtra("position", -1);
                    boolean[] payed = data.getBooleanArrayExtra("payed");
                    receiveAdapter.update(position, payed);
                }
                break;
        }
    }

    public void viewPay(JSONArray payArray) throws JSONException {
        //JSONArray 서버에서 받아옴 (name, account, amount, time)
        payAdapter = new payViewAdapter(getActivity(), payArray, mEmail);
        payView.setAdapter(payAdapter);
    }

    public void viewReceive(JSONArray receiveArray) throws JSONException {
        receiveAdapter = new receiveViewAdapter(getActivity(), receiveArray, mEmail);
        receiveView.setAdapter(receiveAdapter);
    }

    private class receiveViewItemClickListener implements AdapterView.OnItemClickListener {
        @Override
        public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
            Log.d("Clicked", "Clicked");
            ReceiveDebt receiveDebt = receiveAdapter.getItem(position);
            Intent intent = new Intent(getContext(), ReceiveDebtActivity.class);
            intent.putExtra("amount", receiveDebt.getAmount());
            intent.putExtra("names", receiveDebt.getNames());
            intent.putExtra("payed", receiveDebt.getPayed());
            intent.putExtra("allPayed", receiveDebt.getAllPayed());
            intent.putExtra("position", position);
            startActivityForResult(intent, REQUEST_DETAIL);
            /*
            새로운 receive_detail_item 이랑 adapter 만들고,
            Payed click 하면 1) push message 보내고, 가장 밑으로 보내기(List 중에). 추가해야됨
             */
//            OK 눌러서 dialog 끄면:
//                receiveAdapter.update(receive);
//            payAdapter.notifyDataSetChanged(); <-- 이거 adapter쪽으로 옮겨야함
        }
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

                    OutputStream outputStream = connection.getOutputStream();
                    outputStream.write(mName.getBytes("UTF-8"));
                    outputStream.flush();
                    outputStream.close();

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

    private void receiveDebt(ReceiveDebt debt) {
        final JSONObject object = new JSONObject();
        try {
            object.put("account", debt.getAccount());
            object.put("amount", debt.getAmount());
            object.put("time", debt.getTime());
            object.put("name", debt.getName());
            JSONArray array = new JSONArray();

        } catch (JSONException e) {
            e.printStackTrace();
        }
    }

    private void addDebt(ReceiveDebt debt) {
        receiveDebts.add(debt);
        final JSONObject object = new JSONObject();
        try {
            object.put("account", debt.getAccount());
            object.put("amount", debt.getAmount());
            object.put("time", debt.getTime());
            object.put("name", debt.getName());

            JSONArray array = new JSONArray();
            for (int i = 0; i < debt.getEmails().size(); i++) {
                JSONObject o = new JSONObject();
                o.put("email", debt.getEmails().get(i));
                o.put("name", debt.getNames().get(i));
                o.put("payed", false);
                array.put(o);
            }
            object.put("people", array);
        } catch (JSONException e) {
            e.printStackTrace();
        }

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
                    connection.setRequestProperty("type", "debt");
                    connection.setRequestProperty("email", mEmail);

                    OutputStream outputStream = connection.getOutputStream();
                    outputStream.write(object.toString().getBytes("UTF-8"));
                    outputStream.flush();
                    outputStream.close();

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

    public static void addNotification(int i) {
        notifications.add(i);
    }

    private void solveNewPay(PayDebt pay) {
        if (pay.isNew()) {
            pay.setNew(false);
            payAdapter.add(0, pay);
        } else {
            payAdapter.update(pay);
        }
    }

    private void getDataFromServer() {
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
                    connection.setRequestProperty("type", "login");
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
                    dataHandler.sendMessage(message);
                } catch (MalformedURLException e) {
                    e.printStackTrace();
                } catch (IOException e) {
                    e.printStackTrace();
                }
                threads.remove(this);
            }
        }.start();
    }

    public static void setUser(String name, String email) {
        mName = name;
        mEmail = email;
    }
}