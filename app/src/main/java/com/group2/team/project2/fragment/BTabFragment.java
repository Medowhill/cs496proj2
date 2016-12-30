package com.group2.team.project2.fragment;

import android.app.ProgressDialog;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.v4.app.Fragment;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Base64;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;

import com.group2.team.project2.R;
import com.group2.team.project2.adapter.PreviewAdapter;
import com.group2.team.project2.object.PhotoPreview;

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

public class BTabFragment extends Fragment {

    private final String IP = "143.248.49.156";
    private final int PORT = 8083;

    private Button button;
    private RecyclerView recyclerView;
    private ProgressDialog progressDialog;

    private ArrayList<Thread> threads = new ArrayList<>();
    private PreviewAdapter adapter;

    private Handler previewDataHandler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            PhotoPreview preview = (PhotoPreview) msg.obj;
            adapter.add(preview);
        }
    };

    private Handler showDialogHandler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            progressDialog = ProgressDialog.show(getActivity(), "", "Please wait", true);
        }
    };

    private Handler dismissDialogHandler = new Handler() {

        @Override
        public void handleMessage(Message msg) {
            progressDialog.dismiss();
        }
    };

    public BTabFragment() {
    }

    public static BTabFragment newInstance() {
        return new BTabFragment();
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.fragment_b, container, false);

        button = (Button) rootView.findViewById(R.id.b_button);
        recyclerView = (RecyclerView) rootView.findViewById(R.id.b_recyclerView);

        recyclerView.setHasFixedSize(true);
        recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));
        adapter = new PreviewAdapter();
        recyclerView.setAdapter(adapter);

        button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showDialogHandler.sendEmptyMessage(0);
                new Thread() {
                    @Override
                    public void run() {
                        threads.add(this);
                        try {
                            URL url = new URL("http://" + IP + ":" + PORT);
                            HttpURLConnection connection = (HttpURLConnection) url.openConnection();

                            connection.setRequestMethod("GET");
                            connection.setDoOutput(false);
                            connection.setDoInput(true);
                            connection.setUseCaches(false);
                            connection.setDefaultUseCaches(false);

                            InputStream inputStream = connection.getInputStream();
                            ByteArrayOutputStream out = new ByteArrayOutputStream();

                            byte[] buf = new byte[1024 * 8];
                            int length;
                            while ((length = inputStream.read(buf)) != -1) {
                                out.write(buf, 0, length);
                            }
                            byte[] arr = out.toByteArray();
                            inputStream.close();

                            JSONArray array = new JSONArray(new String(arr));
                            for (int i = 0; i < array.length(); i++) {
                                JSONObject object = array.getJSONObject(i);
                                byte[] thumbnail = Base64.decode(object.getString("thumbnail"), Base64.DEFAULT);
                                PhotoPreview preview = new PhotoPreview(BitmapFactory.decodeByteArray(thumbnail, 0, thumbnail.length), object.getString("time"));
                                Message message = new Message();
                                message.obj = preview;
                                previewDataHandler.sendMessage(message);
                            }
                        } catch (MalformedURLException e) {
                            e.printStackTrace();
                        } catch (IOException e) {
                            e.printStackTrace();
                        } catch (JSONException e) {
                            e.printStackTrace();
                        }
                        dismissDialogHandler.sendEmptyMessage(0);
                        threads.remove(this);
                    }
                }.start();
            }
        });
        return rootView;
    }

    @Override
    public void onDestroy() {
        super.onDestroy();

        for (Thread thread : threads)
            if (thread.isAlive())
                thread.interrupt();
    }
}
