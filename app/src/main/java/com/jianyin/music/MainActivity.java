package com.jianyin.music;

import android.Manifest;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.provider.MediaStore;
import android.util.Log;
import android.webkit.JavascriptInterface;
import android.webkit.WebSettings;
import android.webkit.WebView;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import org.json.JSONArray;
import org.json.JSONObject;
import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.List;

public class MainActivity extends AppCompatActivity {
    private WebView webView;
    private JsBridge jsBridge;
    private static final int PERMISSION_REQ = 1001;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        webView = findViewById(R.id.webview);

        WebSettings ws = webView.getSettings();
        ws.setJavaScriptEnabled(true);
        ws.setDomStorageEnabled(true);
        ws.setAllowFileAccess(true);
        ws.setMediaPlaybackRequiresUserGesture(false);

        webView.setWebViewClient(new android.webkit.WebViewClient());
        jsBridge = new JsBridge(this);
        webView.addJavascriptInterface(jsBridge, "Android");
        webView.loadUrl("file:///android_asset/index.html");

        checkPermission();
    }

    public void checkPermission(){
        List<String> needPerm = new ArrayList<>();
        if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU){
            if(ContextCompat.checkSelfPermission(this, Manifest.permission.READ_MEDIA_AUDIO) != PackageManager.PERMISSION_GRANTED){
                needPerm.add(Manifest.permission.READ_MEDIA_AUDIO);
            }
        }else{
            if(ContextCompat.checkSelfPermission(this, Manifest.permission.READ_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED){
                needPerm.add(Manifest.permission.READ_EXTERNAL_STORAGE);
            }
        }
        if(!needPerm.isEmpty()){
            ActivityCompat.requestPermissions(this, needPerm.toArray(new String[0]), PERMISSION_REQ);
        }
    }

    public String scanAllAudio(){
        JSONArray arr = new JSONArray();
        String[] proj = {
                MediaStore.Audio.Media._ID,
                MediaStore.Audio.Media.DISPLAY_NAME,
                MediaStore.Audio.Media.DATA,
                MediaStore.Audio.Media.DURATION
        };
        Uri uri = MediaStore.Audio.Media.EXTERNAL_CONTENT_URI;
        Cursor cur = getContentResolver().query(uri, proj, null, null, MediaStore.Audio.Media.DISPLAY_NAME);
        if(cur == null) return arr.toString();
        while(cur.moveToNext()){
            try{
                String name = cur.getString(cur.getColumnIndexOrThrow(MediaStore.Audio.Media.DISPLAY_NAME));
                String path = cur.getString(cur.getColumnIndexOrThrow(MediaStore.Audio.Media.DATA));
                String id = cur.getString(cur.getColumnIndexOrThrow(MediaStore.Audio.Media._ID));
                Uri audioUri = Uri.withAppendedPath(MediaStore.Audio.Media.EXTERNAL_CONTENT_URI, id);

                JSONObject obj = new JSONObject();
                obj.put("name", name);
                obj.put("uri", audioUri.toString());
                obj.put("path", path);
                arr.put(obj);
            }catch (Exception e){
                Log.e("scanAudio", e.getMessage());
            }
        }
        cur.close();
        return arr.toString();
    }

    public void runJs(String js){
        runOnUiThread(()-> webView.evaluateJavascript(js, null));
    }
}
