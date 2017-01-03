package com.group2.team.project2.fragment;

import android.Manifest;
import android.app.ProgressDialog;
import android.content.ContentResolver;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.provider.ContactsContract;
import android.support.v4.app.Fragment;
import android.support.v7.app.AlertDialog;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.Toast;

import com.facebook.AccessToken;
import com.facebook.GraphRequest;
import com.facebook.GraphResponse;
import com.facebook.HttpMethod;
import com.group2.team.project2.R;
import com.group2.team.project2.adapter.ContactviewAdapter;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;

public class ATabFragment extends Fragment {

    //52.78.240.193
    private String ownID;
    private static final int PERMISSIONS_REQUEST_READ_CONTACTS = 100;
    private String ownEmail;
    private ProgressDialog dialog;
    ListView listView;
    ContactviewAdapter m_adapter;
    JSONArray contactArray = new JSONArray();

    private Handler dismissDialogHandler = new Handler() {

        @Override
        public void handleMessage(Message msg) {
            dialog.dismiss();
        }
    };

    public ATabFragment() {
    }

    public static ATabFragment newInstance() {
        return new ATabFragment();
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.fragment_a, container, false);
        listView = (ListView) rootView.findViewById(R.id.listView);

        AccessToken token = AccessToken.getCurrentAccessToken();
        GraphRequest graphRequest = GraphRequest.newMeRequest(token, new GraphRequest.GraphJSONObjectCallback() {
            @Override
            public void onCompleted(JSONObject jsonObject, GraphResponse graphResponse) {
                try {
                    ownID = jsonObject.getString("id");
                    Log.d("LOGGED IN", ownID);
                    ownEmail = jsonObject.getString("email");
                    Log.d("my own ID", ownID);
                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }
        });
        Bundle param = new Bundle();
        param.putString("fields", "id, name, email");
        graphRequest.setParameters(param);
        graphRequest.executeAsync();

        //Get buttons
        Button postButton = (Button) rootView.findViewById(R.id.message_post);
        Button getButton = (Button) rootView.findViewById(R.id.message_get);
        Button clearButton = (Button) rootView.findViewById(R.id.clear);

        clearButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                m_adapter.setJsonArray(contactArray = new JSONArray());
                m_adapter.notifyDataSetChanged();
            }
        });

        postButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                dialog = ProgressDialog.show(getContext(), "", "Please wait", true);
                try {
                    CrawlPostView();
                } catch (JSONException e) {
                    e.printStackTrace();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }

        });

        getButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                try {
                    GetView();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        });
        listView.setOnItemClickListener(new ListViewItemClickListener());

        m_adapter = new ContactviewAdapter(getActivity(), contactArray);
        listView.setAdapter(m_adapter);
        return rootView;
    }

    private class ListViewItemClickListener implements AdapterView.OnItemClickListener {
        @Override
        public void onItemClick(AdapterView<?> parent, View view, final int position, long id) {
            AlertDialog.Builder alertDlg = new AlertDialog.Builder(view.getContext());


            alertDlg.setNegativeButton("문자", new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                    Intent intent = new Intent(Intent.ACTION_VIEW);
                    try {
                        intent.setData(Uri.parse("sms:"+contactArray.getJSONObject(position).getString("mobile_number")));
                    } catch (JSONException e) {
                        e.printStackTrace();
                    }
                    startActivity(intent);
                }
            });

            alertDlg.setNeutralButton("전화", new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                    Intent intent = new Intent(Intent.ACTION_DIAL);
                    try {
                        intent.setData(Uri.parse("tel:"+contactArray.getJSONObject(position).getString("mobile_number")));
                    } catch (JSONException e) {
                        e.printStackTrace();
                    }
                    startActivity(intent);
                }
            });

            alertDlg.setPositiveButton("확인", new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                    dialog.dismiss();  // AlertDialog를 닫는다.
                }
            });

            try {
                alertDlg.setTitle(contactArray.getJSONObject(position).getString("name"));
                String message = "";
                if (contactArray.getJSONObject(position).has("mobile_number")) {
                    message = "Mobile Number: " + contactArray.getJSONObject(position).getString("mobile_number");
                    alertDlg.setMessage(message);
                }
                if (contactArray.getJSONObject(position).has("home_number")) {
                    message += "\nHome Number: " + contactArray.getJSONObject(position).getString("home_number");
                    alertDlg.setMessage(message);
                }
                if (contactArray.getJSONObject(position).has("work_number")) {
                    message += "\nWork Number: " + contactArray.getJSONObject(position).getString("work_number");
                    alertDlg.setMessage(message);
                }
                alertDlg.show();
            } catch (JSONException e) {
                e.printStackTrace();
            }
        }
    }

    // Contact 가져올 때 첫번째로 부르는 함수
    private JSONArray queryContact() throws JSONException {
        JSONArray phoneArray = new JSONArray();
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M && getActivity().checkSelfPermission(Manifest.permission.READ_CONTACTS) != PackageManager.PERMISSION_GRANTED) {
            requestPermissions(new String[]{Manifest.permission.READ_CONTACTS}, PERMISSIONS_REQUEST_READ_CONTACTS);
        } else {
            phoneArray = readContact();
        }
        return phoneArray;
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
        if (requestCode == PERMISSIONS_REQUEST_READ_CONTACTS) {
            if (grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                try {
                    JSONArray phoneArray = readContact();
                    for (int i = 0; i < phoneArray.length(); i++)
                        contactArray.put(phoneArray.get(i));
                    m_adapter.notifyDataSetChanged();

                    SendToHttp(phoneArray);
                } catch (JSONException e) {
                    e.printStackTrace();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            } else {
                Toast.makeText(getActivity(), "Permission Denied", Toast.LENGTH_SHORT).show();
            }
        }
    }

    private JSONArray readContact() throws JSONException {
        JSONArray phoneArray = new JSONArray();
        try {
            // Android version is lesser than 6.0 or the permission is already granted.
            ContentResolver cr = getActivity().getContentResolver();
            Uri uri = ContactsContract.Contacts.CONTENT_URI;

            Cursor cur = cr.query(uri, null, null, null, null);
            if (cur.getCount() > 0) {
                while (cur.moveToNext()) {
                    JSONObject j = new JSONObject();
                    String id = cur.getString(cur.getColumnIndex(ContactsContract.Contacts._ID));
                    String name = cur.getString(cur.getColumnIndex(ContactsContract.Contacts.DISPLAY_NAME));
                    j.put("name", name);
                    String phone_num = "";
                    if (cur.getInt(cur.getColumnIndex(ContactsContract.Contacts.HAS_PHONE_NUMBER)) > 0) {
                        Cursor pCur = getActivity().getContentResolver().query(
                                ContactsContract.CommonDataKinds.Phone.CONTENT_URI,
                                null,
                                ContactsContract.CommonDataKinds.Phone.CONTACT_ID + " = ?",
                                new String[]{id}, null);
                        while (pCur.moveToNext()) {
                            int phoneType = pCur.getInt(pCur.getColumnIndex(
                                    ContactsContract.CommonDataKinds.Phone.TYPE));
                            String phoneNumber = pCur.getString(pCur.getColumnIndex(
                                    ContactsContract.CommonDataKinds.Phone.NUMBER));
                            switch (phoneType) {
                                case ContactsContract.CommonDataKinds.Phone.TYPE_MOBILE:
                                    j.put("mobile_number", phoneNumber);
                                    break;
                                case ContactsContract.CommonDataKinds.Phone.TYPE_HOME:
                                    j.put("home_number", phoneNumber);
                                    break;
                                case ContactsContract.CommonDataKinds.Phone.TYPE_WORK:
                                    j.put("work_number", phoneNumber);
                                    break;
                                default:
                                    break;
                            }
                        }
                        pCur.close();
                    }
                    phoneArray.put(j);
                }
            }
        } catch (JSONException e) {
            throw new JSONException(e.getMessage());
        }
        return phoneArray;
    }
    // 요기까지 Contact 가져오는거 함수들

    public void GetView() throws IOException {
        final Handler handler = new Handler();
        new Thread() {
            public void run() {
                try {
                    URL u = new URL("http://" + getString(R.string.server_ip) + ":" + getString(R.string.server_port));
                    HttpURLConnection urlConnection = (HttpURLConnection) u.openConnection();
                    //urlConnection.setConnectTimeout(3 * 1000);
                    //urlConnection.setReadTimeout(3 * 1000);

                    urlConnection.setDoOutput(false);
                    urlConnection.setDoInput(true);
                    urlConnection.setRequestMethod("GET");
                    urlConnection.setRequestProperty("tab", "A");
                    urlConnection.setRequestProperty("email", ownEmail);

                    InputStream is = urlConnection.getInputStream();
                    ByteArrayOutputStream out = new ByteArrayOutputStream();

                    byte[] buf = new byte[1024 * 8];
                    int length;
                    while ((length = is.read(buf)) != -1) {
                        out.write(buf, 0, length);
                    }
                    byte[] arr = out.toByteArray();
                    is.close();

                    JSONObject array = new JSONObject(new String(arr));
                    contactArray = array.getJSONArray("contact");

                    handler.post(new Runnable() {
                        public void run() {
                            m_adapter = new ContactviewAdapter(getActivity(), contactArray);
                            listView.setAdapter(m_adapter);
                        }
                    });
                } catch (IOException e) {
                    e.printStackTrace();
                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }
        }.start();
    }

    public void CrawlPostView() throws JSONException, IOException {

        Log.d("OwnID", ownID);
        JSONArray array = queryContact(); //이 친구가 폰 Contact 불러오고
        for (int i = 0; i < array.length(); i++)
            contactArray.put(array.get(i));
        m_adapter.notifyDataSetChanged();

        JSONArray mkArray = contactArray;
        SendToHttp(mkArray);

        getFacebookFriends(); // 이 친구가 Facebook Friends 불러오면 되고
        //Log.d("ContactArray", contactArray.toString());
    }

    public void getFacebookFriends() {
        final JSONArray facebookArray = new JSONArray();
        Bundle param = new Bundle();
        param.putString("fields", "name");
        new GraphRequest(
                AccessToken.getCurrentAccessToken(),
                "/" + ownID + "/taggable_friends",
                param,
                HttpMethod.GET,
                new GraphRequest.Callback() {
                    public void onCompleted(GraphResponse response) {
                        JSONObject obj = response.getJSONObject();
                        Log.d("RESPONSE", response + "");
                        Log.d("RESPONSE", obj + "");

                        try {
                            JSONArray arr = obj.getJSONArray("data");
                            for (int i = 0; i < arr.length(); i++) {
                                arr.getJSONObject(i).remove("id");
                                contactArray.put(arr.getJSONObject(i));
                                facebookArray.put(arr.getJSONObject(i));
                            }
                            //Log.d("facebookArray", arr.toString());
                            m_adapter.notifyDataSetChanged();
                        } catch (JSONException e) {
                            e.printStackTrace();
                        }
                        GraphRequest nextRequest = response.getRequestForPagedResults(GraphResponse.PagingDirection.NEXT);
                        if (nextRequest != null) {
                            Bundle parameters = new Bundle();
                            nextRequest.setParameters(parameters);
                            nextRequest.setCallback(this);
                            nextRequest.executeAsync();
                        } else {
                            //Log.d("facebookArrayLength", String.valueOf(facebookArray.length()));
                            //Log.d("facebookArray", facebookArray.toString());
                            try {
                                SendToHttp(facebookArray);
                            } catch (IOException e) {
                                e.printStackTrace();
                            } catch (JSONException e) {
                                e.printStackTrace();
                            }
                            dismissDialogHandler.sendEmptyMessage(0);
                        }
                    }
                }
        ).executeAsync();
    }


    private void SendToHttp(final JSONArray JsonArr) throws IOException, JSONException {
        new Thread() {
            public void run() {
                try {
                    // URL클래스의 생성자로 주소를 넘겨준다.
                    URL u = new URL("http://" + getString(R.string.server_ip) + ":" + getString(R.string.server_port));
                    // 해당 주소의 페이지로 접속을 하고, 단일 HTTP 접속을 하기위해 캐스트한다.
                    HttpURLConnection urlConnection = (HttpURLConnection) u.openConnection();

                    // POST방식으로 요청한다.( 기본값은 GET )
                    urlConnection.setRequestMethod("POST");
                    // InputStream으로 서버로 부터 응답 헤더와 메시지를 읽어들이겠다는 옵션을 정의한다
                    urlConnection.setDoInput(true);
                    // OutputStream으로 POST 데이터를 넘겨주겠다는 옵션을 정의한다.
                    urlConnection.setDoOutput(true);

                    // 요청 헤더를 정의한다.( 원래 Content-Length값을 넘겨주어야하는데 넘겨주지 않아도 되는것이 이상하다. )
                    //urlConnection.setRequestProperty("Content-Type", "application/json; charset=UTF-8");
                    urlConnection.setRequestProperty("tab", "A");
                    urlConnection.setRequestProperty("email", ownEmail);

                    // 새로운 OutputStream에 요청할 OutputStream을 넣는다
                    OutputStream os = urlConnection.getOutputStream();

                    // 그리고 write메소드로 메시지로 작성된 파라미터정보를 바이트단위로 "EUC-KR"로 인코딩해서 요청한다.
                    // 여기서 중요한 점은 "UTF-8"로 해도 되는데 한글일 경우는 "EUC-KR"로 인코딩해야만 한글이 제대로 전달된다.
                    os.write(JsonArr.toString().getBytes("UTF-8"));
                    // 그리고 스트림의 버퍼를 비워준다.
                    os.flush();
                    os.close();

                    // 밑에 있는 코드는, setDoInput 지우고, 지워보고 없어도 상관없으면 지우자.
                    InputStream is = urlConnection.getInputStream();
                    //byte[] arr = new byte[is.available()];
                    //is.read(arr);
                    is.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }

            }
        }.start();
    }
}
