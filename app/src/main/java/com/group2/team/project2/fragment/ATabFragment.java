package com.group2.team.project2.fragment;

import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

import com.facebook.AccessToken;
import com.facebook.CallbackManager;
import com.facebook.FacebookCallback;
import com.facebook.FacebookException;
import com.facebook.FacebookSdk;
import com.facebook.GraphRequest;
import com.facebook.GraphResponse;
import com.facebook.HttpMethod;
import com.facebook.appevents.AppEventsLogger;
import com.facebook.internal.Utility;
import com.facebook.login.LoginResult;
import com.facebook.login.widget.LoginButton;
import com.group2.team.project2.R;

import org.json.JSONArray;
import org.json.JSONObject;
import org.json.JSONException;


import java.io.BufferedInputStream;
import java.io.BufferedReader;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLConnection;
import java.util.Arrays;
import android.os.Handler;

import static com.facebook.FacebookSdk.getApplicationContext;
import static junit.framework.Assert.assertNotNull;

public class ATabFragment extends Fragment {

    private CallbackManager callbackManager;
    private EditText etMessage;
    private TextView tvRecvData;
    private String url ="http://143.248.49.125:8000";
    private String ownID="";

    public ATabFragment() {
    }

    public static ATabFragment newInstance() {
        return new ATabFragment();
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {

        // 여기부터 facebook login button (Gradle, manifests, fragment_a.xml 참조
        callbackManager = CallbackManager.Factory.create();

        View rootView = inflater.inflate(R.layout.fragment_a, container, false);

        LoginButton loginButton = (LoginButton) rootView.findViewById(R.id.login_button);
        loginButton.setReadPermissions(Arrays.asList("public_profile", "email", "user_friends", "read_custom_friendlists"));
        // If using in a fragment
        loginButton.setFragment(this);
        // Other app specific specialization

        // Callback registration
        loginButton.registerCallback(callbackManager, new FacebookCallback<LoginResult>() {
            @Override
            public void onSuccess(LoginResult loginResult) {
                GraphRequest graphRequest = GraphRequest.newMeRequest(loginResult.getAccessToken(), new GraphRequest.GraphJSONObjectCallback() {
                    @Override
                    public void onCompleted(JSONObject object, GraphResponse response) {
                        Log.v("result",object.toString());
                        try {
                            ownID = object.getJSONObject("id").toString();
                        } catch (JSONException e) {
                            e.printStackTrace();
                        }
                    }
                });

                Bundle parameters = new Bundle();
                parameters.putString("fields", "id,name,email,gender,birthday");
                graphRequest.setParameters(parameters);
                graphRequest.executeAsync();
            }

            @Override
            public void onCancel() {
                // App code
            }

            @Override
            public void onError(FacebookException exception) {
                Log.e("LoginErr", exception.toString());
            }
        });
        //여기까지 facebook login


        //Testing Get button with Server
        Button postButton = (Button) rootView.findViewById(R.id.message_post);
        Button getButton = (Button) rootView.findViewById(R.id.message_get);
        etMessage = (EditText) rootView.findViewById(R.id.et_message);
        tvRecvData = (TextView) rootView.findViewById(R.id.tv_recvData);
        final Handler handler = new Handler();

        postButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                new Thread(){
                    public void run() {
                        String sMessage = etMessage.getText().toString(); // 보내는 메시지를 받아옴
                        try {
                            SendToHttp(sMessage); // 메시지를 서버로 보냄
                        } catch (IOException e) {
                            e.printStackTrace();
                        } catch (JSONException e) {
                            e.printStackTrace();
                        }
                    }
                }.start();
            }
        });

        getButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                new Thread(){
                    public void run(){
                        try {
                            String result = GetByHttp(); // 메시지를 받아옴
                            Log.d("result", result);
                            final JSONObject obj = new JSONObject(result);
                            Log.d("Test", obj.toString());
                            Log.d("JSON", obj.get("message").toString());
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
                        //String[][] parsedData = jsonParserList(); // 받은 메시지를 json 파싱
                    }
                }.start();

                /*
                AccessToken token = AccessToken.getCurrentAccessToken();
                GraphRequest graphRequest = GraphRequest.newMeRequest(token, new GraphRequest.GraphJSONObjectCallback() {
                    @Override
                    public void onCompleted(JSONObject jsonObject, GraphResponse graphResponse) {
                        try {
                            Log.d("JSONOBJECT", jsonObject.toString());
                            JSONObject jsonArrayFriend = jsonObject.getJSONObject("friendlists");
                            JSONArray jsonArrayFriends = jsonObject.getJSONObject("friendlists").getJSONArray("data");
                            Log.d("JSONFriend", jsonArrayFriend.toString());
                            Log.d("JSONFriends", jsonArrayFriends.toString());
                            JSONObject friendlistObject = jsonArrayFriends.getJSONObject(0);
                            String friendListID = friendlistObject.getString("id");
                            //myNewGraphReq(friendListID);
                            Log.d("JSONID", friendListID.toString());

                        } catch (JSONException e) {
                            e.printStackTrace();
                        }
                    }
                });
                Bundle param = new Bundle();
                param.putString("fields", "id, name, friends, friendlists");
                graphRequest.setParameters(param);
                graphRequest.executeAsync();
                */

                AccessToken token = AccessToken.getCurrentAccessToken();
                GraphRequest graphRequest = GraphRequest.newMeRequest(token, new GraphRequest.GraphJSONObjectCallback() {
                    @Override
                    public void onCompleted(JSONObject jsonObject, GraphResponse graphResponse) {
                        try {
                            ownID = jsonObject.getString("id");
                        } catch (JSONException e) {
                            e.printStackTrace();
                        }
                    }
                });
                Bundle param = new Bundle();
                param.putString("fields", "id, name");
                graphRequest.setParameters(param);
                graphRequest.executeAsync();


                /* make the API call */
                param = new Bundle();
                param.putString("fields", "name,id");
                GraphRequest friend = new GraphRequest(
                        AccessToken.getCurrentAccessToken(),
                        "/10210835687305905/taggable_friends",
                        param,
                        HttpMethod.GET,
                        new GraphRequest.Callback() {
                            public void onCompleted(GraphResponse response) {
                                Log.d("Members", response.toString());
                            }
                        }
                );
                friend.executeAsync();

                /* make the API call */
                param = new Bundle();
                param.putString("fields", "name,id");
                new GraphRequest(
                        AccessToken.getCurrentAccessToken(),
                        "/10210835687305905/taggable_friends",
                        param,
                        HttpMethod.GET,
                        new GraphRequest.Callback() {
                            public void onCompleted(GraphResponse response) {
                                Log.d("Members4", response.toString());
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


                new Thread(){
                    public void run(){
                        Bundle param = new Bundle();
                        param.putString("fields", "name,id");
                        GraphRequest friend = new GraphRequest(
                                AccessToken.getCurrentAccessToken(),
                                "/10210835687305905/taggable_friends",
                                param,
                                HttpMethod.GET,
                                new GraphRequest.Callback() {
                                    public void onCompleted(GraphResponse response) {
                                        Log.d("Members2", response.toString());

                                    }
                                }
                        );

                        GraphResponse response = friend.executeAndWait();
                        GraphRequest nextRequest = response.getRequestForPagedResults(GraphResponse.PagingDirection.NEXT);
                        if (nextRequest!= null){
                            nextRequest.setCallback(friend.getCallback());
                            nextRequest.executeAndWait();
                        }

                        /*
                        GraphResponse response = friend.executeAndWait();
                        GraphRequest nextRequest = response.getRequestForPagedResults(GraphResponse.PagingDirection.NEXT);
                        assertNotNull(response.getJSONObject());
                        Log.d("helloMonkey", response.toString());
                        nextRequest.setCallback(friend.getCallback());
                        response = nextRequest.executeAndWait();
                        */
                    }
                }.start();




            }
        });

        return rootView;
    }

    /*
    private void myNewGraphReq(String friendlistId) {
        final String graphPath = "/"+friendlistId+"/members/";
        AccessToken token = AccessToken.getCurrentAccessToken();
        GraphRequest request = new GraphRequest(token, graphPath, null, HttpMethod.GET, new GraphRequest.Callback() {
            @Override
            public void onCompleted(GraphResponse graphResponse) {
                JSONObject object = graphResponse.getJSONObject();
                Log.d("FriendJSONOBJECT", object.toString());
                try {
                    JSONArray arrayOfUsersInFriendList= object.getJSONArray("data");
                // Do something with the user list
                // ex: get first user in list, "name"
                    JSONObject user = arrayOfUsersInFriendList.getJSONObject(0);
                    String usersName = user.getString("name");
                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }
        });
        Bundle param = new Bundle();
        param.putString("fields", "name");
        request.setParameters(param);
        request.executeAsync();
    }
    */


    private String GetByHttp() throws IOException {
        URL u = new URL(url);
        HttpURLConnection urlConnection = (HttpURLConnection) u.openConnection();
        urlConnection.setConnectTimeout(3*1000);
        urlConnection.setReadTimeout(3*1000);

        urlConnection.setRequestMethod("GET");
        // InputStream으로 서버로 부터 응답 헤더와 메시지를 읽어들이겠다는 옵션을 정의한다
        urlConnection.setDoInput(true);
        //urlConnection.setRequestProperty("Content-Type", "application/json; charset=UTF-8");

        String response = "";
        String line;
        BufferedReader br = new BufferedReader(new InputStreamReader(urlConnection.getInputStream()));
        while ((line = br.readLine()) != null) {
            response += line;
        }
        return response;
    }

    private void SendToHttp(String msg) throws IOException, JSONException {
        if (msg == null)
            msg = "";

        JSONObject job = new JSONObject();
        try{
            job.put("phoneNum", "01000000000");
            job.put("name", "test name");
            job.put("address", "test address");
            job.put("message", msg);
        } catch (JSONException e1) {
            e1.printStackTrace();
        }

        // URL클래스의 생성자로 주소를 넘겨준다.
        URL u = new URL(url);
        // 해당 주소의 페이지로 접속을 하고, 단일 HTTP 접속을 하기위해 캐스트한다.
        HttpURLConnection urlConnection = (HttpURLConnection) u.openConnection();
        urlConnection.setConnectTimeout(3*1000);
        urlConnection.setReadTimeout(3*1000);

        // POST방식으로 요청한다.( 기본값은 GET )
        urlConnection.setRequestMethod("POST");
        // InputStream으로 서버로 부터 응답 헤더와 메시지를 읽어들이겠다는 옵션을 정의한다
        urlConnection.setDoInput(true);
        // OutputStream으로 POST 데이터를 넘겨주겠다는 옵션을 정의한다.
        urlConnection.setDoOutput(true);

        // 요청 헤더를 정의한다.( 원래 Content-Length값을 넘겨주어야하는데 넘겨주지 않아도 되는것이 이상하다. )
        urlConnection.setRequestProperty("Content-Type", "application/json; charset=UTF-8");
        //urlConnection.setRequestProperty("Cache-Control", "no-cache");
        //urlConnection.setRequestProperty("Accept", "application/json");

        // 새로운 OutputStream에 요청할 OutputStream을 넣는다
        OutputStream os = urlConnection.getOutputStream();

        // 그리고 write메소드로 메시지로 작성된 파라미터정보를 바이트단위로 "EUC-KR"로 인코딩해서 요청한다.
        // 여기서 중요한 점은 "UTF-8"로 해도 되는데 한글일 경우는 "EUC-KR"로 인코딩해야만 한글이 제대로 전달된다.
        os.write(job.toString().getBytes("UTF-8"));
        // 그리고 스트림의 버퍼를 비워준다.
        os.flush();
        os.close();

        InputStream is = urlConnection.getInputStream();
        byte[] arr = new byte[is.available()];
        is.read(arr);
        is.close();

        /*
        // 스트림을 닫는다.

        // 응답받은 메시지의 길이만큼 버퍼를 생성하여 읽어들이고, "EUC-KR"로 디코딩해서 읽어들인다.
        BufferedReader br = new BufferedReader( new OutputStreamReader( urlConnection.getInputStream(), "EUC-KR" ), urlConnection.getContentLength() );

        String buf;
        // 표준출력으로 한 라인씩 출력
        while( ( buf = br.readLine() ) != null ) {
            System.out.println( buf );
        }
        // 스트림을 닫는다.
        br.close();


        try {
            InputStream in = new BufferedInputStream(urlConnection.getInputStream());
            readStream(in);
        } finally {
            urlConnection.disconnect();
        }
        */

    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        callbackManager.onActivityResult(requestCode, resultCode, data);
    }
}
