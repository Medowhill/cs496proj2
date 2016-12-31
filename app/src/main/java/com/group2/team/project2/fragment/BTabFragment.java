package com.group2.team.project2.fragment;

import android.Manifest;
import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.os.Message;
import android.provider.MediaStore;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.Fragment;
import android.support.v4.content.ContextCompat;
import android.support.v4.content.FileProvider;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Base64;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.LinearLayout;

import com.group2.team.project2.EventBus;
import com.group2.team.project2.R;
import com.group2.team.project2.adapter.PreviewAdapter;
import com.group2.team.project2.event.BResultEvent;
import com.group2.team.project2.object.PhotoPreview;
import com.squareup.otto.Subscribe;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;

public class BTabFragment extends Fragment {

    public final static int REQUEST_GALLERY = 0, REQUEST_CAMERA = 1;

    private final int PERMISSION_REQUEST_STORAGE = 0;
    private final String IP = "143.248.49.156";
    private final int PORT = 8083;

    private LinearLayout layout;
    private FloatingActionButton fabAdd, fabCamera, fabGallery, fabCancel;
    private RecyclerView recyclerView;
    private ProgressDialog progressDialog;

    private Animation animationFabAddDisappear, animationFabCameraAppear, animationFabGalleryAppear, animationFabCancelAppear,
            animationFabCameraDisappear, animationFabGalleryDisappear, animationFabAddAppear, animationFabCancelDisappear;

    private ArrayList<Thread> threads = new ArrayList<>();
    private SimpleDateFormat format = new SimpleDateFormat("yyyyMMddHHmmssSSS");
    private PreviewAdapter adapter;
    private String currentPath;

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
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        EventBus.getInstance().register(this);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.fragment_b, container, false);

        layout = (LinearLayout) rootView.findViewById(R.id.b_linearLayout);
        fabCamera = (FloatingActionButton) rootView.findViewById(R.id.b_fab_camera);
        fabGallery = (FloatingActionButton) rootView.findViewById(R.id.b_fab_gallery);
        fabCancel = (FloatingActionButton) rootView.findViewById(R.id.b_fab_cancel);
        fabAdd = (FloatingActionButton) rootView.findViewById(R.id.b_fab_add);
        recyclerView = (RecyclerView) rootView.findViewById(R.id.b_recyclerView);

        animationFabAddDisappear = AnimationUtils.loadAnimation(getContext(), R.anim.b_fabadd_disappear);
        animationFabCameraAppear = AnimationUtils.loadAnimation(getContext(), R.anim.b_fabcamera_appear);
        animationFabGalleryAppear = AnimationUtils.loadAnimation(getContext(), R.anim.b_fabgallery_appear);
        animationFabCancelAppear = AnimationUtils.loadAnimation(getContext(), R.anim.b_fabcancel_appear);
        animationFabCameraDisappear = AnimationUtils.loadAnimation(getContext(), R.anim.b_fabcamera_disappear);
        animationFabGalleryDisappear = AnimationUtils.loadAnimation(getContext(), R.anim.b_fabgallery_disappear);
        animationFabAddAppear = AnimationUtils.loadAnimation(getContext(), R.anim.b_fabcancel_appear);
        animationFabCancelDisappear = AnimationUtils.loadAnimation(getContext(), R.anim.b_fabadd_disappear);
        animationFabAddDisappear.setAnimationListener(new Animation.AnimationListener() {
            @Override
            public void onAnimationStart(Animation animation) {
                layout.setVisibility(View.VISIBLE);
                fabCamera.startAnimation(animationFabCameraAppear);
                fabGallery.startAnimation(animationFabGalleryAppear);
                fabCancel.setVisibility(View.INVISIBLE);
            }

            @Override
            public void onAnimationEnd(Animation animation) {
                fabAdd.setVisibility(View.INVISIBLE);
                fabCancel.setVisibility(View.VISIBLE);
                fabCancel.startAnimation(animationFabCancelAppear);
            }

            @Override
            public void onAnimationRepeat(Animation animation) {
            }
        });
        animationFabCancelDisappear.setAnimationListener(new Animation.AnimationListener() {
            @Override
            public void onAnimationStart(Animation animation) {
                fabCamera.startAnimation(animationFabCameraDisappear);
                fabGallery.startAnimation(animationFabGalleryDisappear);
            }

            @Override
            public void onAnimationEnd(Animation animation) {
                fabCancel.setVisibility(View.INVISIBLE);
                fabAdd.setVisibility(View.VISIBLE);
                fabAdd.startAnimation(animationFabAddAppear);
            }

            @Override
            public void onAnimationRepeat(Animation animation) {
            }
        });
        animationFabAddAppear.setAnimationListener(new Animation.AnimationListener() {
            @Override
            public void onAnimationStart(Animation animation) {
            }

            @Override
            public void onAnimationEnd(Animation animation) {
                layout.setVisibility(View.INVISIBLE);
            }

            @Override
            public void onAnimationRepeat(Animation animation) {
            }
        });

        recyclerView.setHasFixedSize(true);
        recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));
        adapter = new PreviewAdapter();
        recyclerView.setAdapter(adapter);

        fabAdd.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                fabAdd.startAnimation(animationFabAddDisappear);
            }
        });
        fabCancel.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                fabCancel.startAnimation(animationFabCancelDisappear);
            }
        });
        fabGallery.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                fabCancel.startAnimation(animationFabCancelDisappear);
                if (Build.VERSION.SDK_INT >= 23 && ContextCompat.checkSelfPermission(getContext(), Manifest.permission.READ_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED)
                    requestPermissions(new String[]{Manifest.permission.READ_EXTERNAL_STORAGE}, PERMISSION_REQUEST_STORAGE);
                else
                    intentToGallery();
            }
        });
        fabCamera.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                fabCancel.startAnimation(animationFabCancelDisappear);
                intentToCamera();
            }
        });

        getPhotoList();
        return rootView;
    }

    @Override
    public void onDestroy() {
        super.onDestroy();

        for (Thread thread : threads)
            if (thread.isAlive())
                thread.interrupt();
    }

    @SuppressWarnings("unused")
    @Subscribe
    public void onActivityResultEvent(@NonNull BResultEvent event) {
        onActivityResult(event.getRequestCode(), event.getResultCode(), event.getData());
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        Log.i("cs496", requestCode + ", " + resultCode);
        switch (requestCode) {
            case REQUEST_CAMERA:
                if (resultCode == Activity.RESULT_OK) {
                    try {
                        sendImage(new FileInputStream(currentPath));
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }
                break;
            case REQUEST_GALLERY:
                if (resultCode == Activity.RESULT_OK) {
                    try {
                        sendImage(getActivity().getContentResolver().openInputStream(data.getData()));
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }
                break;
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        switch (requestCode) {
            case PERMISSION_REQUEST_STORAGE:
                if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED)
                    intentToGallery();
                break;
        }
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        EventBus.getInstance().unregister(this);
    }

    private void getPhotoList() {
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
                    connection.setRequestProperty("tab", "B");
                    connection.setRequestProperty("type", "list");

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

    private File createImageFile() throws IOException {
        String timeStamp = format.format(new Date());
        String imageFileName = "JPEG_" + timeStamp + "_";
        File storageDir = getActivity().getExternalFilesDir(Environment.DIRECTORY_PICTURES);
        File image = File.createTempFile(imageFileName, ".jpg", storageDir);

        currentPath = image.getAbsolutePath();
        return image;
    }

    private void intentToCamera() {
        Intent intent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        File file = null;
        try {
            file = createImageFile();
        } catch (IOException e) {
            e.printStackTrace();
        }
        if (file != null) {
            Uri photoURI = FileProvider.getUriForFile(getActivity(), "com.group2.team.project2", file);
            intent.putExtra(MediaStore.EXTRA_OUTPUT, photoURI);
            if (Build.VERSION.SDK_INT <= Build.VERSION_CODES.KITKAT) {
                List<ResolveInfo> resInfoList = getContext().getPackageManager().queryIntentActivities(intent, PackageManager.MATCH_DEFAULT_ONLY);
                for (ResolveInfo resolveInfo : resInfoList) {
                    String packageName = resolveInfo.activityInfo.packageName;
                    getContext().grantUriPermission(packageName, photoURI, Intent.FLAG_GRANT_WRITE_URI_PERMISSION | Intent.FLAG_GRANT_READ_URI_PERMISSION);
                }
            }
            if (intent.resolveActivity(getActivity().getPackageManager()) != null)
                ActivityCompat.startActivityForResult(getActivity(), intent, REQUEST_CAMERA, null);
        }
    }

    private void intentToGallery() {
        Intent intent = new Intent(Intent.ACTION_PICK);
        intent.setType("image/*");
        ActivityCompat.startActivityForResult(getActivity(), intent, REQUEST_GALLERY, null);
    }

    private void sendImage(InputStream inputStream) {
        showDialogHandler.sendEmptyMessage(0);
        byte[] tmp;
        try {
            tmp = new byte[inputStream.available()];
            inputStream.read(tmp);
        } catch (IOException e) {
            e.printStackTrace();
            return;
        }
        final byte[] arr = tmp;

        Bitmap temp = BitmapFactory.decodeByteArray(arr, 0, arr.length);
        Bitmap bitmap = Bitmap.createScaledBitmap(temp, 50, 50, false);
        temp.recycle();
        ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
        bitmap.compress(Bitmap.CompressFormat.JPEG, 100, outputStream);
        bitmap.recycle();
        final byte[] arrThumb = outputStream.toByteArray();

        new Thread() {
            @Override
            public void run() {
                threads.add(this);
                try {
                    URL url = new URL("http://" + IP + ":" + PORT);
                    HttpURLConnection connection = (HttpURLConnection) url.openConnection();

                    connection.setRequestMethod("POST");
                    connection.setDoOutput(true);
                    connection.setDoInput(true);
                    connection.setUseCaches(false);
                    connection.setDefaultUseCaches(false);
                    connection.setRequestProperty("tab", "B");
                    connection.setRequestProperty("time", format.format(Calendar.getInstance().getTime()));
                    connection.setRequestProperty("len", arrThumb.length + "");

                    OutputStream outputStream = connection.getOutputStream();
                    outputStream.write(arrThumb);
                    outputStream.write(arr);
                    outputStream.flush();
                    outputStream.close();

                    InputStream inputStream1 = connection.getInputStream();
                    byte[] arr = new byte[inputStream1.available()];
                    inputStream1.read(arr);
                    inputStream1.close();
                } catch (MalformedURLException e) {
                    e.printStackTrace();
                } catch (IOException e) {
                    e.printStackTrace();
                }
                dismissDialogHandler.sendEmptyMessage(0);
                threads.remove(this);
            }
        }.start();
    }
}
