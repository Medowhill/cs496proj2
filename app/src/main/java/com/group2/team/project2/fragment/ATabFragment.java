package com.group2.team.project2.fragment;

import android.Manifest;
import android.content.ContentResolver;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.provider.ContactsContract;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.facebook.AccessToken;
import com.facebook.GraphRequest;
import com.facebook.GraphResponse;
import com.facebook.HttpMethod;
import com.group2.team.project2.EventBus;
import com.group2.team.project2.R;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;

public class ATabFragment extends Fragment {

    private EditText etMessage;
    private TextView tvRecvData;
    private String url = "http://143.248.49.125:8000", ownID, ownEmail;
    private static final int PERMISSIONS_REQUEST_READ_CONTACTS = 100;

    public ATabFragment() {
    }

    public static ATabFragment newInstance() {
        return new ATabFragment();
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.fragment_a, container, false);

        //Get buttons
        Button postButton = (Button) rootView.findViewById(R.id.message_post);
        Button getButton = (Button) rootView.findViewById(R.id.message_get);
        final Handler handler = new Handler();

        postButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                try {
                    CrawlPostView();
                } catch (JSONException e) {
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

        return rootView;
    }

    // Contact 가져올 때 첫번째로 부르는 함수
    private JSONArray queryContact() throws JSONException {
        JSONArray contactArray = new JSONArray();
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M && getActivity().checkSelfPermission(Manifest.permission.READ_CONTACTS) != PackageManager.PERMISSION_GRANTED) {
            requestPermissions(new String[]{Manifest.permission.READ_CONTACTS}, PERMISSIONS_REQUEST_READ_CONTACTS);
            //After this point you wait for callback in onRequestPermissionsResult(int, String[], int[]) overriden method
        } else {
            contactArray = readContact();
        }
        return contactArray;
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
        JSONArray contactArray = new JSONArray();
        if (requestCode == PERMISSIONS_REQUEST_READ_CONTACTS) {
            if (grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                try {
                    contactArray = readContact();
                } catch (JSONException e) {
                    e.printStackTrace();
                }
            } else {
                Toast.makeText(getActivity(), "Permission Denied", Toast.LENGTH_SHORT).show();
            }
        }
    }

    public class ContactInfo {
        public String mobileNumber = "";
        public String name = "";
        public String id;
        public String homeNumber = "";
        public String workNumber = "";

        public ContactInfo(String name, String number, String id) {
            this.name = name;
            mobileNumber = number;
            this.id = id;
        }

        public ContactInfo(String name, String id) {
            this.name = name;
            this.id = id;
        }

        @Override
        public String toString() {
            String tostr = "";
            if (mobileNumber.length() > 1)
                tostr = tostr + "\nmobile:" + mobileNumber;
            if (homeNumber.length() > 1)
                tostr = tostr + "\nhome:" + homeNumber;
            if (workNumber.length() > 1)
                tostr = tostr + "\nwork:" + workNumber;
            return tostr;
        }
    }

    ArrayList<ContactInfo> contact;

    private JSONArray readContact() throws JSONException {
        JSONArray contactArray = new JSONArray();
        try {
            // Android version is lesser than 6.0 or the permission is already granted.
            contact = new ArrayList<ContactInfo>();
            ContentResolver cr = getActivity().getContentResolver();
            Uri uri = ContactsContract.Contacts.CONTENT_URI;

            Cursor cur = cr.query(uri, null, null, null, null);
            if (cur.getCount() > 0) {
                while (cur.moveToNext()) {
                    JSONObject j = new JSONObject();
                    String id = cur.getString(cur.getColumnIndex(ContactsContract.Contacts._ID));
                    String name = cur.getString(cur.getColumnIndex(ContactsContract.Contacts.DISPLAY_NAME));
                    j.put("Name", name);
                    ContactInfo c = new ContactInfo(name, id);
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
                                    j.put("Mobile number", phoneNumber);
                                    c.mobileNumber = phoneNumber;
                                    break;
                                case ContactsContract.CommonDataKinds.Phone.TYPE_HOME:
                                    j.put("Home number", phoneNumber);
                                    c.homeNumber = phoneNumber;
                                    break;
                                case ContactsContract.CommonDataKinds.Phone.TYPE_WORK:
                                    j.put("Work number", phoneNumber);
                                    c.workNumber = phoneNumber;
                                    break;
                                default:
                                    break;
                            }
                        }
                        pCur.close();
                    }
                    contact.add(c);
                    contactArray.put(j);
                }
            }
        } catch (JSONException e) {
            throw new JSONException(e.getMessage());
        }
        return contactArray;
    }
    // 요까지 Contact 가져오는거 함수들

    public void GetView() throws IOException {
        URL u = new URL(url);
        HttpURLConnection urlConnection = (HttpURLConnection) u.openConnection();
        urlConnection.setConnectTimeout(3 * 1000);
        urlConnection.setReadTimeout(3 * 1000);

        urlConnection.setRequestMethod("GET");
        urlConnection.setDoInput(true);

        String response = "";
        String line;
        BufferedReader br = new BufferedReader(new InputStreamReader(urlConnection.getInputStream()));
        while ((line = br.readLine()) != null) {
            response += line;
        }

        /* 쓰일 것 같음
        new Thread(){
                    public void run(){
                        try {
                            String result = GetByHttp(); // 메시지를 받아옴
                            handler.post(new Runnable() {
                                public void run() {
                                    try {
                                        tvRecvData.setText(obj.get("message").toString());
                                    } catch (JSONException e) {
                                        e.printStackTrace();
                                    }
                                }
                            });
                        } catch (IOException e) {
                            e.printStackTrace();
                        } catch (JSONException e) {
                            e.printStackTrace();
                        }
                    }
                }.start();
         */

        // String으로 받아서, JSON Array로 바꾸고, View 해줘야함
    }


    public void CrawlPostView() throws JSONException {
        Log.d("OwnID", ownID);
        JSONArray contactArray = queryContact(); //이 친구가 폰 Contact 불러오고
        JSONArray facebookArray = getFacebookFriends(); // 이 친구가 Facebook Friends 불러오면 되고
        Log.d("ContactArray", contactArray.toString());
        Log.d("facebookArray", facebookArray.toString());
        //jsonArray 합쳐줘야겠네
        //SendToHttp(jsonArray);
        //view();
    }

    public JSONArray getFacebookFriends() {
        final JSONArray facebookJSON = new JSONArray();
        Bundle param = new Bundle();
        param.putString("fields", "name");
        new GraphRequest(
                AccessToken.getCurrentAccessToken(),
                "/10210835687305905/taggable_friends",
                param,
                HttpMethod.GET,
                new GraphRequest.Callback() {
                    public void onCompleted(GraphResponse response) {
                        facebookJSON.put(response.getJSONObject());
                        JSONObject a = response.getJSONObject();
                        Log.d("FriendsJSON:", a.toString());
                        Log.d("Friends:", response.toString());
                        GraphRequest nextRequest = response.getRequestForPagedResults(GraphResponse.PagingDirection.NEXT);
                        if (nextRequest != null) {
                            Bundle parameters = new Bundle();
                            nextRequest.setParameters(parameters);
                            nextRequest.setCallback(this);
                            nextRequest.executeAsync();
                        }
                    }
                }
        ).executeAsync();
        return facebookJSON;
    }

    private void SendToHttp(JSONArray contactArray) throws IOException, JSONException {

        JSONObject job = new JSONObject();

        // URL클래스의 생성자로 주소를 넘겨준다.
        URL u = new URL(url);
        // 해당 주소의 페이지로 접속을 하고, 단일 HTTP 접속을 하기위해 캐스트한다.
        HttpURLConnection urlConnection = (HttpURLConnection) u.openConnection();

        // POST방식으로 요청한다.( 기본값은 GET )
        urlConnection.setRequestMethod("POST");
        // InputStream으로 서버로 부터 응답 헤더와 메시지를 읽어들이겠다는 옵션을 정의한다
        urlConnection.setDoInput(true);
        // OutputStream으로 POST 데이터를 넘겨주겠다는 옵션을 정의한다.
        urlConnection.setDoOutput(true);

        // 요청 헤더를 정의한다.( 원래 Content-Length값을 넘겨주어야하는데 넘겨주지 않아도 되는것이 이상하다. )
        urlConnection.setRequestProperty("Content-Type", "application/json; charset=UTF-8");
        urlConnection.setRequestProperty("Tab", "A");
        urlConnection.setRequestProperty("Email", ownEmail);

        // 새로운 OutputStream에 요청할 OutputStream을 넣는다
        OutputStream os = urlConnection.getOutputStream();

        // 그리고 write메소드로 메시지로 작성된 파라미터정보를 바이트단위로 "EUC-KR"로 인코딩해서 요청한다.
        // 여기서 중요한 점은 "UTF-8"로 해도 되는데 한글일 경우는 "EUC-KR"로 인코딩해야만 한글이 제대로 전달된다.
        os.write(job.toString().getBytes("UTF-8"));
        // 그리고 스트림의 버퍼를 비워준다.
        os.flush();
        os.close();

        // 밑에 있는 코드는, setDoInput 지우고, 지워보고 없어도 상관없으면 지우자.
        InputStream is = urlConnection.getInputStream();
        byte[] arr = new byte[is.available()];
        is.read(arr);
        is.close();
    }
}
