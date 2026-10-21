package com.jianyin.music;

public class JsBridge {
    private final MainActivity act;
    public JsBridge(MainActivity activity){
        this.act = activity;
    }

    @JavascriptInterface
    public String scanMusic(){
        return act.scanAllAudio();
    }
}
