package com.github.catvod.spider;

import android.content.Context;
import android.net.Uri;

import com.github.catvod.crawler.Spider;
import com.github.catvod.crawler.SpiderDebug;
import com.github.catvod.utils.okhttp.OkHttpUtil;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;

public class MGTV extends Spider {
    protected JSONObject a;
    protected JSONObject q = new JSONObject();

    private String q(String str, String str2) {
        String str3;
        try {
            if (str2.startsWith("//")) {
                Uri parse = Uri.parse(str);
                str3 = parse.getScheme() + ":" + str2;
            } else if (str2.contains("://")) {
                return str2;
            } else {
                Uri parse2 = Uri.parse(str);
                str3 = parse2.getScheme() + "://" + parse2.getHost() + str2;
            }
            return str3;
        } catch (Exception e) {
            SpiderDebug.log(e);
            return str2;
        }
    }

    public String categoryContent(String str, String str2, boolean z, HashMap<String, String> hashMap) {
        try {
            String str3 = "https://pianku.api.mgtv.com/rider/list/msite/v2?platform=msite&channelId=" + str + "&pn=" + str2 + "&chargeInfo=&sort=c2";
            if (hashMap != null) {
                for (String str4 : hashMap.keySet()) {
                    String trim = hashMap.get(str4).trim();
                    if (trim.length() != 0) {
                        str3 = str3 + "&" + str4 + "=" + URLEncoder.encode(trim);
                    }
                }
            }
            String content = OkHttpUtil.string(str3, getHeaders(str3));
            JSONObject jSONObject = new JSONObject();
            try {
                JSONArray optJSONArray = new JSONObject(content).optJSONObject("data").optJSONArray("hitDocs");
                JSONArray jSONArray = new JSONArray();
                for (int i = 0; i < optJSONArray.length(); i++) {
                    JSONObject optJSONObject = optJSONArray.optJSONObject(i);
                    String optString = optJSONObject.optString("title");
                    String q = q(str3, optJSONObject.optString("img"));
                    String optString2 = optJSONObject.optString("updateInfo");
                    if (optString2.equals("")) {
                        optString2 = optJSONObject.optString("subtitle");
                    }
                    String vodId = optJSONObject.optString("playPartId");
                    JSONObject jSONObject2 = new JSONObject();
                    jSONObject2.put("vod_id", vodId);
                    jSONObject2.put("vod_name", optString);
                    jSONObject2.put("vod_pic", q);
                    jSONObject2.put("vod_remarks", optString2);
                    jSONArray.put(jSONObject2);
                }
                jSONObject.put("list", jSONArray);
                jSONObject.put("page", str2);
                jSONObject.put("pagecount", Integer.MAX_VALUE);
                jSONObject.put("limit", 90);
                jSONObject.put("total", Integer.MAX_VALUE);
                jSONObject.put("list", jSONArray);
            } catch (Exception e) {
                SpiderDebug.log(e);
            }
            return jSONObject.toString(4);
        } catch (Exception e2) {
            SpiderDebug.log(e2);
            return "";
        }
    }

    public String detailContent(List<String> list) {
        try {
            String vodId = list.get(0);
            String infoUrl = "https://pcweb.api.mgtv.com/player/vinfo?video_id=" + vodId;
            String infoJson = OkHttpUtil.string(infoUrl, getHeaders(infoUrl));
            JSONObject info = new JSONObject(infoJson).getJSONObject("data");
            String pic = info.getString("clip_imgurl2");

            int pageSize = 30;
            int page = 1;
            String url = "https://pcweb.api.mgtv.com/episode/list?_support=10000000&version=5.5.35&video_id=" + vodId + "&page=" + page + "&size=" + pageSize + "&allowedRC=1&_support=10000000";
            String json = OkHttpUtil.string(url, getHeaders(url));
            JSONObject jSONObject = new JSONObject(json).getJSONObject("data");
            JSONObject jSONObject2 = new JSONObject();
            JSONObject optJSONObject = jSONObject.optJSONObject("info");
            jSONObject2.put("vod_id", vodId);
            jSONObject2.put("vod_name", optJSONObject.optString("title"));
            jSONObject2.put("vod_pic", pic);
            jSONObject2.put("vod_content", optJSONObject.optString("desc"));
            JSONArray optJSONArray = jSONObject.optJSONArray("list");
            ArrayList arrayList = new ArrayList();

            for (int i = 0; i < optJSONArray.length(); i++) {
                JSONObject optJSONObject2 = optJSONArray.optJSONObject(i);
                String viewName = optJSONObject2.optString("t1") + " " + optJSONObject2.optString("t2");
                arrayList.add(viewName + "$" + optJSONObject2.optString("url"));
            }

            int totalPage = jSONObject.getInt("total_page");
            int count = jSONObject.getInt("count");
            int total = jSONObject.getInt("total");
            ArrayList<String> blocks = new ArrayList<String>();
            ArrayList blockList = new ArrayList();
            if (totalPage == 1) {
                blocks.add("mgtv");
            } else if (totalPage > 1) {
                blocks.add("1-" + pageSize);
            }
            blockList.add(join("#", arrayList));
            if (totalPage > page) {
                for (int curPage = 2; curPage <= totalPage; curPage++) {
                    String listUrl = "https://pcweb.api.mgtv.com/episode/list?_support=10000000&version=5.5.35&video_id=" + vodId + "&page=" + curPage + "&size=" + pageSize + "&allowedRC=1&_support=10000000";
                    String resultJson = OkHttpUtil.string(listUrl, getHeaders(listUrl));
                    JSONObject dataObj = new JSONObject(resultJson).getJSONObject("data");
                    JSONArray dataList = dataObj.getJSONArray("list");
                    int first = (curPage - 1) * pageSize + 1;
                    int last = 0;
                    if (totalPage == curPage) {
                        last = total;
                    } else {
                        last = curPage * pageSize;
                    }
                    blocks.add(first + "-" + last);
                    ArrayList curArrayList = new ArrayList();
                    for (int i = 0; i < dataList.length(); i++) {
                        JSONObject item = dataList.optJSONObject(i);
                        String viewThisName = item.optString("t1") + " " + item.optString("t2");
                        curArrayList.add(viewThisName + "$" + item.optString("url"));
                    }
                    blockList.add(join("#", curArrayList));
                }
            }

            jSONObject2.put("vod_play_from", join("$$$", blocks));
            jSONObject2.put("vod_play_url", join("$$$", blockList));
            JSONObject result = new JSONObject();
            JSONArray jSONArray = new JSONArray();
            jSONArray.put(jSONObject2);
            result.put("list", jSONArray);
            return result.toString();
        } catch (Exception e) {
            SpiderDebug.log(e);
            return "";
        }
    }

    protected HashMap<String, String> getHeaders(String str) {
        HashMap<String, String> hashMap = new HashMap<>();
        hashMap.put("referer", "https://so.mgtv.com");
        hashMap.put("User-Agent", "Mozilla/5.0 (Macintosh; Intel Mac OS X 10_15_7) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/91.0.4472.114 Safari/537.36");
        return hashMap;
    }

    public String homeContent(boolean z) {
        try {
            JSONArray optJSONArray = new JSONObject(OkHttpUtil.string("https://pianku.api.mgtv.com/rider/config/platformChannels/v1?platform=msite&abroad=0&_support=10000000", getHeaders("https://pianku.api.mgtv.com/rider/config/platformChannels/v1?platform=msite&abroad=0&_support=10000000"))).optJSONArray("data");
            JSONArray jSONArray = new JSONArray();
            for (int i = 0; i < optJSONArray.length(); i++) {
                JSONObject optJSONObject = optJSONArray.optJSONObject(i);
                JSONObject jSONObject = new JSONObject();
                jSONObject.put("type_name", optJSONObject.optString("channelName"));
                jSONObject.put("type_id", optJSONObject.optString("channelId"));
                jSONArray.put(jSONObject);
            }
            JSONObject jSONObject2 = new JSONObject();
            if (z) {
                jSONObject2.put("filters", this.a);
            }
            jSONObject2.put("class", jSONArray);
            try {
                JSONArray optJSONArray2 = new JSONObject(OkHttpUtil.string("https://pianku.api.mgtv.com/rider/list/pcweb/v3?platform=pcweb&channelId=2&pn=1&chargeInfo=&sort=c2", getHeaders("https://pianku.api.mgtv.com/rider/list/pcweb/v3?platform=pcweb&channelId=2&pn=1&chargeInfo=&sort=c2"))).optJSONObject("data").optJSONArray("hitDocs");
                JSONArray jSONArray2 = new JSONArray();
                for (int i2 = 0; i2 < optJSONArray2.length(); i2++) {
                    JSONObject optJSONObject2 = optJSONArray2.optJSONObject(i2);
                    String optString = optJSONObject2.optString("title");
                    String q = optJSONObject2.optString("imgUrlH");
                    String optString2 = optJSONObject2.optString("updateInfo");
                    JSONObject jSONObject3 = new JSONObject();
                    jSONObject3.put("vod_id", optJSONObject2.optString("playPartId"));
                    jSONObject3.put("vod_name", optString);
                    jSONObject3.put("vod_pic", q);
                    jSONObject3.put("vod_remarks", optString2);
                    jSONArray2.put(jSONObject3);
                }
                jSONObject2.put("list", jSONArray2);
            } catch (Exception e) {
                SpiderDebug.log(e);
            }
            return jSONObject2.toString(4);
        } catch (Exception e2) {
            SpiderDebug.log(e2);
            return "";
        }
    }

    public String homeVideoContent() {
        try {
            JSONArray jSONArray = new JSONObject(OkHttpUtil.string("https://www.mgtv.com/api.php/app/index_video?token=", getHeaders("https://www.mgtv.com/api.php/app/index_video?token="))).getJSONArray("list");
            JSONArray jSONArray2 = new JSONArray();
            for (int i = 0; i < jSONArray.length(); i++) {
                JSONArray jSONArray3 = jSONArray.getJSONObject(i).getJSONArray("vlist");
                int i2 = 0;
                while (i2 < jSONArray3.length() && i2 < 6) {
                    JSONObject jSONObject = jSONArray3.getJSONObject(i2);
                    JSONObject jSONObject2 = new JSONObject();
                    jSONObject2.put("vod_id", jSONObject.optString("vod_id"));
                    jSONObject2.put("vod_name", jSONObject.optString("vod_name"));
                    jSONObject2.put("vod_pic", jSONObject.optString("vod_pic"));
                    jSONObject2.put("vod_remarks", jSONObject.optString("vod_remarks"));
                    jSONArray2.put(jSONObject2);
                    i2++;
                }
            }
            JSONObject jSONObject3 = new JSONObject();
            jSONObject3.put("list", jSONArray2);
            return jSONObject3.toString();
        } catch (Exception e) {
            SpiderDebug.log(e);
            return "";
        }
    }

    public void init(Context context) {
        MGTV.super.init(context);
        try {
            this.a = new JSONObject("{\"1\":[{\"name\":\"??????\",\"value\":[{\"v\":\"a1\",\"n\":\"??????\"},{\"v\":\"4\",\"n\":\"????????????\"},{\"v\":\"5\",\"n\":\"????????????\"},{\"v\":\"6\",\"n\":\"??????\"},{\"v\":\"7\",\"n\":\"??????\"},{\"v\":\"8\",\"n\":\"??????\"},{\"v\":\"9\",\"n\":\"??????\"},{\"v\":\"179\",\"n\":\"??????\"},{\"v\":\"170\",\"n\":\"??????\"},{\"v\":\"171\",\"n\":\"??????\"},{\"v\":\"173\",\"n\":\"?????????\"},{\"v\":\"174\",\"n\":\"??????\"},{\"v\":\"172\",\"n\":\"??????\"},{\"v\":\"180\",\"n\":\"?????????\"}],\"key\":\"kind\"},{\"name\":\"??????\",\"value\":[{\"v\":\"a1\",\"n\":\"??????\"},{\"v\":\"1\",\"n\":\"??????\"},{\"v\":\"2\",\"n\":\"??????\"},{\"v\":\"3\",\"n\":\"??????\"}],\"key\":\"area\"},{\"name\":\"??????\",\"value\":[{\"v\":\"c1\",\"n\":\"??????\"},{\"v\":\"c2\",\"n\":\"??????\"},{\"v\":\"c4\",\"n\":\"????????????\"}],\"key\":\"sort\"}],\"2\":[{\"name\":\"??????\",\"value\":[{\"v\":\"a1\",\"n\":\"??????\"},{\"v\":\"14\",\"n\":\"????????????\"},{\"v\":\"15\",\"n\":\"????????????\"},{\"v\":\"16\",\"n\":\"????????????\"},{\"v\":\"17\",\"n\":\"????????????\"},{\"v\":\"19\",\"n\":\"????????????\"},{\"v\":\"148\",\"n\":\"??????\"},{\"v\":\"20\",\"n\":\"????????????\"},{\"v\":\"147\",\"n\":\"??????\"},{\"v\":\"21\",\"n\":\"????????????\"},{\"v\":\"22\",\"n\":\"????????????\"},{\"v\":\"23\",\"n\":\"????????????\"},{\"v\":\"24\",\"n\":\"????????????\"},{\"v\":\"25\",\"n\":\"????????????\"},{\"v\":\"26\",\"n\":\"??????\"}],\"key\":\"kind\"},{\"name\":\"??????\",\"value\":[{\"v\":\"a1\",\"n\":\"??????\"},{\"v\":\"10\",\"n\":\"??????\"},{\"v\":\"11\",\"n\":\"??????\"},{\"v\":\"12\",\"n\":\"??????\"}],\"key\":\"area\"},{\"name\":\"??????\",\"value\":[{\"v\":\"c2\",\"n\":\"??????\"},{\"v\":\"c1\",\"n\":\"??????\"},{\"v\":\"c4\",\"n\":\"????????????\"}],\"key\":\"sort\"},{\"name\":\"??????\",\"value\":[{\"v\":\"all\",\"n\":\"??????\"},{\"v\":\"2037\",\"n\":\"TV???\"},{\"v\":\"2038\",\"n\":\"?????????\"},{\"v\":\"2040\",\"n\":\"?????????\"},{\"v\":\"2036\",\"n\":\"????????????\"}],\"key\":\"edition\"}],\"3\":[{\"name\":\"??????\",\"value\":[{\"v\":\"a1\",\"n\":\"??????\"},{\"v\":\"34\",\"n\":\"????????????\"},{\"v\":\"175\",\"n\":\"??????\"},{\"v\":\"176\",\"n\":\"??????\"},{\"v\":\"177\",\"n\":\"??????\"},{\"v\":\"178\",\"n\":\"??????\"},{\"v\":\"39\",\"n\":\"??????\"},{\"v\":\"43\",\"n\":\"????????????\"},{\"v\":\"44\",\"n\":\"??????\"},{\"v\":\"45\",\"n\":\"??????\"},{\"v\":\"46\",\"n\":\"??????\"},{\"v\":\"47\",\"n\":\"??????\"},{\"v\":\"48\",\"n\":\"??????\"},{\"v\":\"50\",\"n\":\"??????\"}],\"key\":\"kind\"},{\"name\":\"??????\",\"value\":[{\"v\":\"a1\",\"n\":\"??????\"},{\"v\":\"b1\",\"n\":\"??????\"},{\"v\":\"b2\",\"n\":\"VIP\"},{\"v\":\"b3\",\"n\":\"VIP??????\"},{\"v\":\"b4\",\"n\":\"????????????\"}],\"key\":\"chargeInfo\"},{\"name\":\"??????\",\"value\":[{\"v\":\"c2\",\"n\":\"??????\"},{\"v\":\"c1\",\"n\":\"??????\"},{\"v\":\"c4\",\"n\":\"????????????\"}],\"key\":\"sort\"}],\"106\":[{\"name\":\"??????\",\"value\":[{\"v\":\"a1\",\"n\":\"??????\"},{\"v\":\"106\",\"n\":\"??????\"},{\"v\":\"107\",\"n\":\"??????\"},{\"v\":\"108\",\"n\":\"??????\"},{\"v\":\"109\",\"n\":\"??????\"}],\"key\":\"kind\"},{\"name\":\"??????\",\"value\":[{\"v\":\"c1\",\"n\":\"??????\"},{\"v\":\"c2\",\"n\":\"??????\"}],\"key\":\"sort\"}],\"91\":[{\"name\":\"??????\",\"value\":[{\"v\":\"a1\",\"n\":\"??????\"},{\"v\":\"119\",\"n\":\"??????\"},{\"v\":\"120\",\"n\":\"??????\"},{\"v\":\"121\",\"n\":\"??????\"},{\"v\":\"122\",\"n\":\"??????\"},{\"v\":\"123\",\"n\":\"??????\"},{\"v\":\"124\",\"n\":\"??????\"}],\"key\":\"kind\"},{\"name\":\"??????\",\"value\":[{\"v\":\"c2\",\"n\":\"??????\"},{\"v\":\"c1\",\"n\":\"??????\"}],\"key\":\"sort\"}],\"50\":[{\"name\":\"??????\",\"value\":[{\"v\":\"a1\",\"n\":\"??????\"},{\"v\":\"60\",\"n\":\"????????????\"},{\"v\":\"86\",\"n\":\"??????\"},{\"v\":\"62\",\"n\":\"??????\"},{\"v\":\"63\",\"n\":\"??????\"},{\"v\":\"64\",\"n\":\"????????????\"},{\"v\":\"65\",\"n\":\"????????????\"},{\"v\":\"66\",\"n\":\"??????\"},{\"v\":\"67\",\"n\":\"??????\"},{\"v\":\"68\",\"n\":\"????????????\"},{\"v\":\"69\",\"n\":\"????????????\"},{\"v\":\"70\",\"n\":\"????????????\"},{\"v\":\"71\",\"n\":\"??????\"},{\"v\":\"72\",\"n\":\"??????\"}],\"key\":\"kind\"},{\"name\":\"??????\",\"value\":[{\"v\":\"a1\",\"n\":\"??????\"},{\"v\":\"52\",\"n\":\"??????\"},{\"v\":\"53\",\"n\":\"??????\"},{\"v\":\"54\",\"n\":\"??????\"},{\"v\":\"55\",\"n\":\"??????\"}],\"key\":\"area\"},{\"name\":\"??????\",\"value\":[{\"v\":\"a1\",\"n\":\"??????\"},{\"v\":\"165\",\"n\":\"?????????\"},{\"v\":\"57\",\"n\":\"TV???\"},{\"v\":\"166\",\"n\":\"OVA???\"},{\"v\":\"167\",\"n\":\"?????????\"}],\"key\":\"edition\"},{\"name\":\"??????\",\"value\":[{\"v\":\"c2\",\"n\":\"??????\"},{\"v\":\"c1\",\"n\":\"??????\"}],\"key\":\"sort\"}],\"51\":[{\"name\":\"??????\",\"value\":[{\"v\":\"a1\",\"n\":\"??????\"},{\"v\":\"97\",\"n\":\"??????\"},{\"v\":\"98\",\"n\":\"??????\"},{\"v\":\"99\",\"n\":\"??????\"},{\"v\":\"100\",\"n\":\"??????\"},{\"v\":\"101\",\"n\":\"??????\"},{\"v\":\"102\",\"n\":\"??????\"},{\"v\":\"103\",\"n\":\"??????\"},{\"v\":\"104\",\"n\":\"??????\"},{\"v\":\"105\",\"n\":\"??????\"}],\"key\":\"kind\"},{\"name\":\"??????\",\"value\":[{\"v\":\"c2\",\"n\":\"??????\"},{\"v\":\"c1\",\"n\":\"??????\"}],\"key\":\"sort\"}],\"20\":[{\"name\":\"??????\",\"value\":[{\"v\":\"a1\",\"n\":\"??????\"},{\"v\":\"150\",\"n\":\"MV\"},{\"v\":\"151\",\"n\":\"????????????\"},{\"v\":\"152\",\"n\":\"????????????\"},{\"v\":\"153\",\"n\":\"?????????\"},{\"v\":\"154\",\"n\":\"?????????\"},{\"v\":\"155\",\"n\":\"????????????\"},{\"v\":\"156\",\"n\":\"??????\"}],\"key\":\"kind\"},{\"name\":\"??????\",\"value\":[{\"v\":\"a1\",\"n\":\"??????\"},{\"v\":\"157\",\"n\":\"??????\"},{\"v\":\"158\",\"n\":\"Hip-hop\"},{\"v\":\"159\",\"n\":\"R&B\"},{\"v\":\"160\",\"n\":\"??????\"},{\"v\":\"161\",\"n\":\"??????\"},{\"v\":\"162\",\"n\":\"??????\"},{\"v\":\"163\",\"n\":\"??????\"},{\"v\":\"164\",\"n\":\"??????\"}],\"key\":\"musicStyle\"},{\"name\":\"??????\",\"value\":[{\"v\":\"c1\",\"n\":\"??????\"},{\"v\":\"c2\",\"n\":\"??????\"}],\"key\":\"sort\"}]}");
        } catch (JSONException e) {
            SpiderDebug.log(e);
        }
    }

    public String join(CharSequence charSequence, Iterable iterable) {
        Iterator it = iterable.iterator();
        if (!it.hasNext()) {
            return "";
        }
        StringBuilder sb = new StringBuilder();
        sb.append(it.next());
        while (it.hasNext()) {
            sb.append(charSequence);
            sb.append(it.next());
        }
        return sb.toString();
    }

    public String playerContent(String str, String str2, List<String> list) {
        try {
            JSONObject jSONObject = new JSONObject();
            try {
                jSONObject.put("parse", 1);
                jSONObject.put("jx", "1");
                jSONObject.put("url", "https://www.mgtv.com" + str2);
                return jSONObject.toString();
            } catch (Exception e) {
                SpiderDebug.log(e);
                return jSONObject.toString();
            }
        } catch (Exception e2) {
            SpiderDebug.log(e2);
            return "";
        }
    }

    public String searchContent(String str, boolean quick) {
        try {
            String url = "https://mobileso.bz.mgtv.com/pc/search/v1?allowedRC=1&q=" + str + "&pn=1&pc=10&uid=&corr=1&_support=10000000";
            String json = OkHttpUtil.string(url, getHeaders(url));
            JSONObject data = new JSONObject(json).getJSONObject("data");
            JSONArray contents = data.getJSONArray("contents");
            JSONArray items = new JSONArray();
            for (int i = 0; i < contents.length(); i++) {
                JSONObject jSONObject = contents.getJSONObject(i).getJSONObject("data");
                if (!jSONObject.has("sourceList")) {
                    continue;
                }
                JSONArray sourceList = jSONObject.getJSONArray("sourceList");
                String vodUrl = sourceList.getJSONObject(0).getString("url");
                String vodId = sourceList.getJSONObject(0).getString("vid");
                String pic = jSONObject.optString("pic");
                JSONObject item = new JSONObject();
                item.put("vod_id", vodId);
                item.put("vod_name", jSONObject.optString("title"));
                item.put("vod_pic", pic);
                item.put("vod_remarks", jSONObject.optString("playTime"));
                items.put(item);
            }
            JSONObject result = new JSONObject();
            result.put("list", items);
            return result.toString();
        } catch (Exception e) {
            SpiderDebug.log(e);
            return "";
        }
    }
}
