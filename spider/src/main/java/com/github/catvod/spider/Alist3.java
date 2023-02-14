package com.github.catvod.spider;

import android.content.Context;

import com.github.catvod.crawler.Spider;
import com.github.catvod.utils.okhttp.OKCallBack;
import com.github.catvod.utils.okhttp.OkHttpUtil;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.HashMap;
import java.util.Iterator;
import java.util.List;

import okhttp3.Call;

public class Alist3 extends Spider {
    private JSONObject ext;

    public String categoryContent(String tid, String pg, boolean filter, HashMap<String, String> hashMap) {
        try {
            int index = tid.indexOf('$');
            String name = tid.substring(0, index);
            String path = tid.substring(index+1);

            String url = this.ext.getString(name) + "/api/fs/list";
            JSONObject params = new JSONObject();
            params.put("path", path);

            JSONArray jSONArray2 = new JSONArray();
            OkHttpUtil.postJson(OkHttpUtil.defaultClient(), url, params.toString(), new OKCallBack.OKCallBackString() {
                @Override
                protected void onFailure(Call call, Exception e) {

                }

                @Override
                protected void onResponse(String response) {
                    try {
                        JSONObject retval = new JSONObject(response);
                        JSONArray list = retval.getJSONObject("data").getJSONArray("content");
                        for (int i =0; i < list.length(); ++i){
                            JSONObject o = list.getJSONObject(i);
                            String pic = o.getString("thumb");
                            if(pic.isEmpty() && (o.getInt("type")==1) ){
                                pic = "http://img1.3png.com/281e284a670865a71d91515866552b5f172b.png";
                            }
                            JSONObject jSONObject2 = new JSONObject();
                            jSONObject2.put("vod_id", tid + (tid.charAt(tid.length()-1) == '/' ? "" : "/") + o.getString("name"));
                            jSONObject2.put("vod_name", o.getString("name"));
                            jSONObject2.put("vod_pic", pic);
                            jSONObject2.put("vod_tag",o.getInt("type")==1 ? "folder" : "file" );

                            double sz = o.getLong("size");
                            String filesize = "";
                            if(sz > 1024*1024*1024*1024.0){
                                sz /= (1024*1024*1024*1024.0);
                                filesize = "TB";
                            }
                            else if(sz > 1024*1024*1024.0){
                                sz /= (1024*1024*1024.0);
                                filesize = "GB";
                            }else if(sz > 1024*1024.0){
                                sz /= (1024*1024.0);
                                filesize = "MB";
                            }else{
                                sz /= 1024.0;
                                filesize = "KB";
                            }
                            String remark ="";
                            if(o.getLong("size") !=0){
                                remark = String.format("%.2f%s", sz, filesize);
                            }

                            jSONObject2.put("vod_remarks", o.getInt("type")==1 ? remark + " 文件夹" : remark);
                            jSONArray2.put(jSONObject2);

                        }
                    }catch (Exception e){
                        e.printStackTrace();
                    }
                }
            });

            JSONObject jSONObject3 = new JSONObject();
            jSONObject3.put("page", 1);
            jSONObject3.put("pagecount", 1);
            jSONObject3.put("limit", jSONArray2.length());
            jSONObject3.put("total", jSONArray2.length());
            jSONObject3.put("list", jSONArray2);
            return jSONObject3.toString();
        } catch (Exception e) {
            e.printStackTrace();
            return "";
        }
    }

    public String detailContent(List<String> ids) {
        try
        {

            String tid = ids.get(0);
            int index = tid.indexOf('$');
            String name = tid.substring(0, index);
            String path = tid.substring(index+1);


            String url = this.ext.getString(name) + "/api/fs/get";
            JSONObject params = new JSONObject();
            params.put("path", path);

            JSONObject result = new JSONObject();
            JSONArray list = new JSONArray();

            OkHttpUtil.postJson(OkHttpUtil.defaultClient(), url, params.toString(), new OKCallBack.OKCallBackString() {
                @Override
                protected void onFailure(Call call, Exception e) {

                }

                @Override
                protected void onResponse(String response) {
                    try {
                        JSONObject retval = new JSONObject(response);
                        JSONObject files = retval.getJSONObject("data");
                            JSONObject o = files;
                            String url = o.getString("raw_url");
                            if(url.indexOf("//") == 0){
                                url = "http:"+url;
                            }

                            JSONObject jSONObject2 = new JSONObject();
                            jSONObject2.put("vod_id", tid + "/" + o.getString("name"));
                            jSONObject2.put("vod_name", o.getString("name"));
                            jSONObject2.put("vod_pic", "");
                            jSONObject2.put("vod_tag",o.getInt("type") == 1 ? "folder" : "file" );
                            jSONObject2.put("vod_play_from", "播放");
                            jSONObject2.put("vod_play_url", o.getString("name")+"$"+url);
                            list.put(jSONObject2);

                    }catch (Exception e){
                        e.printStackTrace();
                    }
                }
            });

            result.put("list", list);
            return result.toString();
        }
        catch (Exception e){
            e.printStackTrace();
        }
        return  "";

    }


    public String homeContent(boolean z) {
        try {
            JSONArray classes = new JSONArray();
            Iterator<String> keys = this.ext.keys();
            while (keys.hasNext()) {
                String k = keys.next();
                String url  = this.ext.getString(k);
                JSONObject newCls = new JSONObject();
                newCls.put("type_id", k+"$/"); // 使用$来分割站点名称+path
                newCls.put("type_name", k);
                newCls.put("type_flag", "1"); // 1 列表形式的文件夹 2 缩略图 0 或者不存在 表示正常
                classes.put(newCls);
            }

            JSONArray jSONArray3 = new JSONArray();
            JSONObject jSONObject4 = new JSONObject();
            jSONObject4.put("class", classes);
            if(z) {
                jSONObject4.put("filters", new JSONObject("{}"));
            }
            return jSONObject4.toString();
        } catch (Exception e) {
            e.printStackTrace();
            return "";
        }
    }

    public void init(Context context, String ext){
        Alist3.super.init(context);
        try {
            if(ext.startsWith("http")){
                this.ext = new JSONObject(OkHttpUtil.string(ext,null));
            }else {
                this.ext = new JSONObject();
                String vec[] = ext.split("#");
                for (int i =0; i < vec.length; ++i){
                    String arr[] = vec[i].split("\\$");
                    if(arr.length ==1){
                        this.ext.put("alist", arr[0]);
                    } else if(arr.length == 2) {
                        this.ext.put(arr[0], arr[1]);
                    }
                }
            }

        } catch (JSONException jsonException) {
            jsonException.printStackTrace();
        }
    }

    public String playerContent(String str, String str2, List<String> list) {
        try{
            JSONObject result = new JSONObject();
            result.put("parse", 0);
            result.put("playUrl", "");
            result.put("url", str2);
            return result.toString();
        }catch (Exception e){
            e.printStackTrace();
        }
        return "";
    }

    public String searchContent(String str, boolean z) {
        try {
            return "";
        } catch (Exception e) {
            e.printStackTrace();
            return "";
        }
    }

}