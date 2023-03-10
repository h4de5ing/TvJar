package com.github.catvod.spider;

import android.content.Context;
import android.net.Uri;

import com.github.catvod.crawler.Spider;
import com.github.catvod.crawler.SpiderDebug;
import com.github.catvod.utils.okhttp.OkHttpUtil;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;

public class QQ extends Spider {
    protected JSONObject dn;
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
        String str3;
        try {
            String str4 = "https://v.qq.com/x/bu/pagesheet/list?_all=1&append=1&channel=" + str + "&listpage=1&offset=" + ((Integer.parseInt(str2) - 1) * 21) + "&pagesize=21&sort=18";
            if (hashMap != null) {
                for (String str5 : hashMap.keySet()) {
                    String trim = hashMap.get(str5).trim();
                    if (trim.length() != 0) {
                        str4 = str4 + "&" + str5 + "=" + URLEncoder.encode(trim);
                    }
                }
            }
            String content = OkHttpUtil.string(str4, getHeaders(str4));
            JSONObject jSONObject = new JSONObject();
            try {
                Elements listItems = Jsoup.parse(content).select(".list_item");
                JSONArray jSONArray = new JSONArray();
                for (int i = 0; i < listItems.size(); i++) {
                    Element item = listItems.get(i);
                    String Pd = item.select("a").attr("title");
                    String q = q(str4, item.select("img").attr("src"));
                    if (item.select(".figure_caption") == null) {
                        str3 = "";
                    } else {
                        str3 = item.select(".figure_caption").text();
                    }
                    String Pd2 = item.select("a").attr("data-float");
                    JSONObject jSONObject2 = new JSONObject();
                    jSONObject2.put("vod_id", Pd2);
                    jSONObject2.put("vod_name", Pd);
                    jSONObject2.put("vod_pic", q);
                    jSONObject2.put("vod_remarks", str3);
                    jSONArray.put(jSONObject2);
                }
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
        JSONArray jSONArray;
        CharSequence charSequence;
        JSONArray jSONArray2;
        CharSequence charSequence2;
        CharSequence charSequence3 = ",";
        try {
            String str = "https://node.video.qq.com/x/api/float_vinfo2?cid=" + list.get(0);
            JSONObject jSONObject = new JSONObject(OkHttpUtil.string(str, getHeaders(str)));
            JSONObject optJSONObject = jSONObject.optJSONObject("c");
            if (optJSONObject == null) {
                return "";
            }
            JSONObject jSONObject2 = new JSONObject();
            jSONObject2.put("vod_id", list.get(0));
            jSONObject2.put("vod_name", optJSONObject.optString("title"));
            jSONObject2.put("vod_pic", q(str, optJSONObject.optString("pic")));
            jSONObject2.put("type_name", jSONObject.optJSONArray("typ").opt(0));
            jSONObject2.put("vod_year", optJSONObject.optString("year"));
            jSONObject.optJSONObject("people");
            JSONArray optJSONArray = jSONObject.optJSONArray("nam").optJSONArray(0);
            ArrayList arrayList = new ArrayList();
            if (optJSONArray != null) {
                for (int i = 0; i < optJSONArray.length(); i++) {
                    arrayList.add(optJSONArray.opt(i));
                }
            }
            jSONObject2.put("vod_actor", join(charSequence3, arrayList));
            jSONObject2.put("vod_content", optJSONObject.optString("description"));
            JSONArray jSONArray3 = optJSONObject.getJSONArray("video_ids");
            ArrayList arrayList2 = new ArrayList();
            ArrayList arrayList3 = new ArrayList();
            int i2 = 1;
            while (i2 <= jSONArray3.length()) {
                arrayList2.add(jSONArray3.optString(i2 - 1));
                if (!(i2 % 30 == 0 || i2 == jSONArray3.length())) {
                    charSequence = charSequence3;
                    jSONArray = jSONArray3;
                    i2++;
                    charSequence3 = charSequence;
                    jSONArray3 = jSONArray;
                }
                String str2 = "https://union.video.qq.com/fcgi-bin/data?otype=json&tid=682&appid=20001238&appkey=6c03bbe9658448a4&union_platform=1&idlist=" + join(charSequence3, arrayList2);
                String h = OkHttpUtil.string(str2, getHeaders(str2));
                JSONArray jSONArray4 = new JSONObject(h.substring(13, h.length() - 1)).getJSONArray("results");
                int i3 = 0;
                while (i3 < jSONArray4.length()) {
                    JSONObject jSONObject3 = jSONArray4.getJSONObject(i3).getJSONObject("fields");
                    if (!jSONObject3.optString("title").contains("??????")) {
                        StringBuilder sb = new StringBuilder();
                        String optString = jSONObject3.optString("title");
                        charSequence2 = charSequence3;
                        StringBuilder sb2 = new StringBuilder();
                        jSONArray2 = jSONArray3;
                        sb2.append(optJSONObject.optString("title"));
                        sb2.append("_");
                        sb.append(optString.replace(sb2.toString(), ""));
                        sb.append("$https://v.qq.com/x/cover/");
                        sb.append(list.get(0));
                        sb.append("/");
                        sb.append(jSONObject3.optString("vid"));
                        sb.append(".html");
                        arrayList3.add(sb.toString());
                    } else {
                        charSequence2 = charSequence3;
                        jSONArray2 = jSONArray3;
                    }
                    i3++;
                    charSequence3 = charSequence2;
                    jSONArray3 = jSONArray2;
                }
                charSequence = charSequence3;
                jSONArray = jSONArray3;
                arrayList2.clear();
                i2++;
                charSequence3 = charSequence;
                jSONArray3 = jSONArray;
            }
            jSONObject2.put("vod_play_from", "qq");
            jSONObject2.put("vod_play_url", join("#", arrayList3));
            JSONObject jSONObject4 = new JSONObject();
            JSONArray jSONArray5 = new JSONArray();
            jSONArray5.put(jSONObject2);
            jSONObject4.put("list", jSONArray5);
            return jSONObject4.toString(4);
        } catch (Exception e) {
            SpiderDebug.log(e);
            return "";
        }
    }

    protected HashMap<String, String> getHeaders(String str) {
        HashMap<String, String> hashMap = new HashMap<>();
        hashMap.put("User-Agent", "Mozilla/5.0 (Macintosh; Intel Mac OS X 10_15_7) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/91.0.4472.114 Safari/537.36");
        return hashMap;
    }

    public String homeContent(boolean z) {
        try {
            Document doc = Jsoup.parse(OkHttpUtil.string("https://v.qq.com/channel/tv?listpage=1&channel=tv&sort=18&_all=1", getHeaders("https://v.qq.com/channel/tv?listpage=1&channel=tv&sort=18&_all=1")));
            JSONArray jSONArray = new JSONArray();
            Iterator<Element> it = doc.select(".nav_cell").iterator();
            while (it.hasNext()) {
                Element next = it.next();
                JSONObject jSONObject = new JSONObject();
                String Pd = next.select("a").attr("href");
                if (!Pd.contains("/art") && !Pd.contains("feeds_hotspot") && !Pd.contains("wwe") && !Pd.contains("choice") && !Pd.contains("sports_new") && !Pd.contains("games") && !Pd.contains("lols11") && !Pd.contains("ent") && !Pd.contains("news") && !Pd.contains("fashion") && !Pd.contains("tech") && !Pd.contains("auto") && !Pd.contains("house") && !Pd.contains("finance") && !Pd.contains("astro") && !Pd.contains("nba") && !Pd.contains("fun") && !Pd.contains("baby") && !Pd.contains("music") && !Pd.contains("life") && !Pd.contains("travel") && Pd.contains("/channel/")) {
                    jSONObject.put("type_name", next.select("a").text());
                    jSONObject.put("type_id", Pd.split("/channel/")[1]);
                    jSONArray.put(jSONObject);
                }
            }
            String h = OkHttpUtil.string("https://v.qq.com/x/bu/pagesheet/list?_all=1&append=1&channel=choice", getHeaders("https://v.qq.com/x/bu/pagesheet/list?_all=1&append=1&channel=choice"));
            JSONObject jSONObject2 = new JSONObject();
            if (z) {
                jSONObject2.put("filters", this.dn);
            }
            jSONObject2.put("class", jSONArray);
            try {
                Elements listItem = Jsoup.parse(h).select(".list_item");
                JSONArray jSONArray2 = new JSONArray();
                for (int i = 0; i < listItem.size(); i++) {
                    Element item = listItem.get(i);
                    String title = item.select("a").attr("title");
                    String pic = q("https://v.qq.com/x/bu/pagesheet/list?_all=1&append=1&channel=choice", item.select("img").attr("src"));
                    String remark = item.select(".figure_caption").text();
                    String id = item.select("a").attr("data-float");
                    JSONObject jSONObject3 = new JSONObject();
                    jSONObject3.put("vod_id", id);
                    jSONObject3.put("vod_name", title);
                    jSONObject3.put("vod_pic", pic);
                    jSONObject3.put("vod_remarks", remark);
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
            JSONArray jSONArray = new JSONObject(OkHttpUtil.string("https://v.qq.com/api.php/app/index_video?token=", getHeaders("https://v.qq.com/api.php/app/index_video?token="))).getJSONArray("list");
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
        QQ.super.init(context);
        try {
            this.dn = new JSONObject("{\"tv\":[{\"name\":\"??????\",\"value\":[{\"v\":\"19\",\"n\":\"??????\"},{\"v\":\"18\",\"n\":\"??????\"},{\"v\":\"16\",\"n\":\"??????\"},{\"v\":\"21\",\"n\":\"????????????\"},{\"v\":\"54\",\"n\":\"????????????\"},{\"v\":\"22\",\"n\":\"????????????\"}],\"key\":\"sort\"},{\"name\":\"??????\",\"value\":[{\"v\":\"-1\",\"n\":\"??????\"},{\"v\":\"1\",\"n\":\"????????????\"},{\"v\":\"2\",\"n\":\"????????????\"},{\"v\":\"3\",\"n\":\"????????????\"},{\"v\":\"4\",\"n\":\"????????????\"},{\"v\":\"14\",\"n\":\"???????????????\"},{\"v\":\"5\",\"n\":\"????????????\"},{\"v\":\"6\",\"n\":\"????????????\"},{\"v\":\"7\",\"n\":\"????????????\"},{\"v\":\"8\",\"n\":\"??????\"},{\"v\":\"9\",\"n\":\"????????????\"},{\"v\":\"10\",\"n\":\"????????????\"},{\"v\":\"11\",\"n\":\"????????????\"},{\"v\":\"12\",\"n\":\"????????????\"},{\"v\":\"13\",\"n\":\"????????????\"},{\"v\":\"15\",\"n\":\"??????\"},{\"v\":\"44\",\"n\":\"??????\"}],\"key\":\"feature\"},{\"name\":\"??????\",\"value\":[{\"v\":\"-1\",\"n\":\"??????\"},{\"v\":\"814\",\"n\":\"??????\"},{\"v\":\"815\",\"n\":\"??????\"},{\"v\":\"816\",\"n\":\"??????\"},{\"v\":\"818\",\"n\":\"??????\"},{\"v\":\"9\",\"n\":\"??????\"},{\"v\":\"10\",\"n\":\"??????\"},{\"v\":\"14\",\"n\":\"????????????\"},{\"v\":\"817\",\"n\":\"????????????\"},{\"v\":\"819\",\"n\":\"??????\"}],\"key\":\"iarea\"},{\"name\":\"??????\",\"value\":[{\"v\":\"-1\",\"n\":\"??????\"},{\"v\":\"2022\",\"n\":\"2022\"},{\"v\":\"2021\",\"n\":\"2021\"},{\"v\":\"2020\",\"n\":\"2020\"},{\"v\":\"4061\",\"n\":\"2019\"},{\"v\":\"4060\",\"n\":\"2018\"},{\"v\":\"2017\",\"n\":\"2017\"},{\"v\":\"859\",\"n\":\"2016\"},{\"v\":\"860\",\"n\":\"2015\"},{\"v\":\"861\",\"n\":\"2014\"},{\"v\":\"862\",\"n\":\"2013\"},{\"v\":\"863\",\"n\":\"2012\"},{\"v\":\"864\",\"n\":\"2011\"},{\"v\":\"865\",\"n\":\"2010\"},{\"v\":\"866\",\"n\":\"??????\"}],\"key\":\"year\"},{\"name\":\"??????\",\"value\":[{\"v\":\"-1\",\"n\":\"??????\"},{\"v\":\"867\",\"n\":\"??????\"},{\"v\":\"6\",\"n\":\"??????\"}],\"key\":\"pay\"}],\"education\":[{\"name\":\"??????\",\"value\":[{\"v\":\"19\",\"n\":\"?????????\"},{\"v\":\"40\",\"n\":\"??????\"}],\"key\":\"sort\"},{\"name\":\"??????\",\"value\":[{\"v\":\"-1\",\"n\":\"??????\"},{\"v\":\"2\",\"n\":\"??????\"},{\"v\":\"3\",\"n\":\"??????\"},{\"v\":\"4\",\"n\":\"??????\"},{\"v\":\"5\",\"n\":\"??????\"},{\"v\":\"6\",\"n\":\"?????????\"},{\"v\":\"7\",\"n\":\"??????\"}],\"key\":\"section\"},{\"name\":\"??????\",\"value\":[{\"v\":\"1\",\"n\":\"??????\"},{\"v\":\"5\",\"n\":\"?????????\"},{\"v\":\"6\",\"n\":\"?????????\"},{\"v\":\"7\",\"n\":\"?????????\"},{\"v\":\"8\",\"n\":\"?????????\"},{\"v\":\"9\",\"n\":\"?????????\"},{\"v\":\"10\",\"n\":\"?????????\"},{\"v\":\"11\",\"n\":\"??????\"},{\"v\":\"12\",\"n\":\"??????\"},{\"v\":\"13\",\"n\":\"??????\"},{\"v\":\"14\",\"n\":\"??????\"},{\"v\":\"15\",\"n\":\"??????\"},{\"v\":\"16\",\"n\":\"??????\"},{\"v\":\"20\",\"n\":\"??????\"}],\"key\":\"grade\"},{\"name\":\"??????\",\"value\":[{\"v\":\"-1\",\"n\":\"??????\"},{\"v\":\"41\",\"n\":\"??????\"},{\"v\":\"42\",\"n\":\"??????\"},{\"v\":\"43\",\"n\":\"??????\"},{\"v\":\"44\",\"n\":\"??????\"},{\"v\":\"45\",\"n\":\"??????\"},{\"v\":\"46\",\"n\":\"??????\"},{\"v\":\"47\",\"n\":\"??????\"},{\"v\":\"50\",\"n\":\"??????\"}],\"key\":\"subject\"}],\"movie\":[{\"name\":\"??????\",\"value\":[{\"v\":\"18\",\"n\":\"????????????\"},{\"v\":\"19\",\"n\":\"????????????\"},{\"v\":\"21\",\"n\":\"????????????\"},{\"v\":\"22\",\"n\":\"????????????\"}],\"key\":\"sort\"},{\"name\":\"??????\",\"value\":[{\"v\":\"-1\",\"n\":\"??????\"},{\"v\":\"100018\",\"n\":\"??????\"},{\"v\":\"100004\",\"n\":\"??????\"},{\"v\":\"100061\",\"n\":\"??????\"},{\"v\":\"100005\",\"n\":\"??????\"},{\"v\":\"100010\",\"n\":\"??????\"},{\"v\":\"4\",\"n\":\"??????\"},{\"v\":\"100009\",\"n\":\"??????\"},{\"v\":\"100006\",\"n\":\"??????\"},{\"v\":\"100012\",\"n\":\"??????\"},{\"v\":\"100015\",\"n\":\"??????\"},{\"v\":\"100007\",\"n\":\"??????\"},{\"v\":\"100017\",\"n\":\"??????\"},{\"v\":\"100022\",\"n\":\"??????\"},{\"v\":\"100003\",\"n\":\"??????\"},{\"v\":\"100016\",\"n\":\"??????\"},{\"v\":\"100011\",\"n\":\"??????\"},{\"v\":\"100021\",\"n\":\"??????\"},{\"v\":\"2\",\"n\":\"??????\"},{\"v\":\"100014\",\"n\":\"??????\"},{\"v\":\"100013\",\"n\":\"??????\"},{\"v\":\"100020\",\"n\":\"??????\"},{\"v\":\"100019\",\"n\":\"??????\"},{\"v\":\"3\",\"n\":\"??????\"}],\"key\":\"itype\"},{\"name\":\"??????\",\"value\":[{\"v\":\"-1\",\"n\":\"??????\"},{\"v\":\"100024\",\"n\":\"??????\"},{\"v\":\"100025\",\"n\":\"????????????\"},{\"v\":\"100029\",\"n\":\"??????\"},{\"v\":\"100032\",\"n\":\"??????\"},{\"v\":\"100026\",\"n\":\"????????????\"},{\"v\":\"100027\",\"n\":\"??????\"},{\"v\":\"100028\",\"n\":\"??????\"},{\"v\":\"100030\",\"n\":\"??????\"},{\"v\":\"100031\",\"n\":\"??????\"},{\"v\":\"15\",\"n\":\"??????\"},{\"v\":\"16\",\"n\":\"??????\"},{\"v\":\"17\",\"n\":\"??????\"},{\"v\":\"18\",\"n\":\"?????????\"},{\"v\":\"19\",\"n\":\"?????????\"},{\"v\":\"20\",\"n\":\"?????????\"},{\"v\":\"21\",\"n\":\"????????????\"},{\"v\":\"22\",\"n\":\"??????\"},{\"v\":\"23\",\"n\":\"????????????\"},{\"v\":\"100033\",\"n\":\"??????\"}],\"key\":\"iarea\"},{\"name\":\"??????\",\"value\":[{\"v\":\"-1\",\"n\":\"??????\"},{\"v\":\"1\",\"n\":\"??????\"},{\"v\":\"2\",\"n\":\"????????????\"},{\"v\":\"5\",\"n\":\"??????\"},{\"v\":\"8\",\"n\":\"??????\"},{\"v\":\"9\",\"n\":\"??????\"},{\"v\":\"3\",\"n\":\"??????\"},{\"v\":\"6\",\"n\":\"?????????\"}],\"key\":\"characteristic\"},{\"name\":\"??????\",\"value\":[{\"v\":\"-1\",\"n\":\"??????\"},{\"v\":\"2022\",\"n\":\"2022\"},{\"v\":\"2022\",\"n\":\"2022\"},{\"v\":\"2021\",\"n\":\"2021\"},{\"v\":\"2020\",\"n\":\"2020\"},{\"v\":\"20\",\"n\":\"2019\"},{\"v\":\"2018\",\"n\":\"2018\"},{\"v\":\"2017\",\"n\":\"2017\"},{\"v\":\"2016\",\"n\":\"2016\"},{\"v\":\"100063\",\"n\":\"2015\"},{\"v\":\"100034\",\"n\":\"2014\"},{\"v\":\"100035\",\"n\":\"2013-2011\"},{\"v\":\"100036\",\"n\":\"2010-2006\"},{\"v\":\"100037\",\"n\":\"2005-2000\"},{\"v\":\"100038\",\"n\":\"90??????\"},{\"v\":\"100039\",\"n\":\"80??????\"},{\"v\":\"100040\",\"n\":\"??????\"}],\"key\":\"year\"},{\"name\":\"??????\",\"value\":[{\"v\":\"-1\",\"n\":\"??????\"},{\"v\":\"1\",\"n\":\"??????\"},{\"v\":\"2\",\"n\":\"??????\"},{\"v\":\"3\",\"n\":\"??????\"},{\"v\":\"4\",\"n\":\"??????\"}],\"key\":\"charge\"}],\"variety\":[{\"name\":\"??????\",\"value\":[{\"v\":\"4\",\"n\":\"??????\"},{\"v\":\"5\",\"n\":\"??????\"}],\"key\":\"sort\"},{\"name\":\"??????\",\"value\":[{\"v\":\"-1\",\"n\":\"??????\"},{\"v\":\"1\",\"n\":\"????????????\"},{\"v\":\"2\",\"n\":\"??????\"}],\"key\":\"exclusive\"},{\"name\":\"??????\",\"value\":[{\"v\":\"-1\",\"n\":\"??????\"},{\"v\":\"1\",\"n\":\"??????\"},{\"v\":\"2\",\"n\":\"??????\"}],\"key\":\"iarea\"},{\"name\":\"??????\",\"value\":[{\"v\":\"-1\",\"n\":\"??????\"},{\"v\":\"10\",\"n\":\"??????\"},{\"v\":\"11\",\"n\":\"??????\"},{\"v\":\"24\",\"n\":\"??????\"},{\"v\":\"12\",\"n\":\"??????\"},{\"v\":\"14\",\"n\":\"??????\"},{\"v\":\"2\",\"n\":\"?????????\"},{\"v\":\"16\",\"n\":\"??????\"},{\"v\":\"25\",\"n\":\"??????\"},{\"v\":\"17\",\"n\":\"??????\"},{\"v\":\"26\",\"n\":\"??????\"},{\"v\":\"19\",\"n\":\"??????\"},{\"v\":\"20\",\"n\":\"??????\"},{\"v\":\"21\",\"n\":\"??????\"},{\"v\":\"15\",\"n\":\"????????????\"},{\"v\":\"3\",\"n\":\"??????\"},{\"v\":\"22\",\"n\":\"????????????\"},{\"v\":\"23\",\"n\":\"??????\"},{\"v\":\"7\",\"n\":\"??????\"},{\"v\":\"6\",\"n\":\"??????\"}],\"key\":\"itype\"},{\"name\":\"??????\",\"value\":[{\"v\":\"-1\",\"n\":\"??????\"},{\"v\":\"2022\",\"n\":\"2022\"},{\"v\":\"2021\",\"n\":\"2021\"},{\"v\":\"50\",\"n\":\"2020\"},{\"v\":\"7\",\"n\":\"2019\"},{\"v\":\"1\",\"n\":\"2018\"},{\"v\":\"2\",\"n\":\"2017\"},{\"v\":\"3\",\"n\":\"2016\"},{\"v\":\"4\",\"n\":\"2015\"},{\"v\":\"5\",\"n\":\"2014\"},{\"v\":\"6\",\"n\":\"2013\"},{\"v\":\"2012\",\"n\":\"2012\"},{\"v\":\"2011\",\"n\":\"2011\"},{\"v\":\"2010\",\"n\":\"2010\"},{\"v\":\"99\",\"n\":\"??????\"}],\"key\":\"iyear\"},{\"name\":\"??????\",\"value\":[{\"v\":\"-1\",\"n\":\"??????\"},{\"v\":\"1\",\"n\":\"??????\"}],\"key\":\"ipay\"}],\"cartoon\":[{\"name\":\"??????\",\"value\":[{\"v\":\"40\",\"n\":\"??????\"},{\"v\":\"23\",\"n\":\"??????\"},{\"v\":\"20\",\"n\":\"??????\"},{\"v\":\"22\",\"n\":\"????????????\"}],\"key\":\"sort\"},{\"name\":\"??????\",\"value\":[{\"v\":\"-1\",\"n\":\"??????\"},{\"v\":\"2\",\"n\":\"??????\"},{\"v\":\"5\",\"n\":\"??????\"},{\"v\":\"1\",\"n\":\"??????\"},{\"v\":\"3\",\"n\":\"??????\"},{\"v\":\"4\",\"n\":\"??????\"},{\"v\":\"9\",\"n\":\"??????\"},{\"v\":\"6\",\"n\":\"??????\"},{\"v\":\"13\",\"n\":\"??????\"},{\"v\":\"7\",\"n\":\"??????\"},{\"v\":\"14\",\"n\":\"??????\"},{\"v\":\"11\",\"n\":\"????????????\"},{\"v\":\"15\",\"n\":\"??????\"},{\"v\":\"16\",\"n\":\"??????\"},{\"v\":\"17\",\"n\":\"??????\"},{\"v\":\"18\",\"n\":\"??????\"},{\"v\":\"19\",\"n\":\"??????\"},{\"v\":\"20\",\"n\":\"??????\"},{\"v\":\"12\",\"n\":\"??????\"}],\"key\":\"itype\"},{\"name\":\"??????\",\"value\":[{\"v\":\"-1\",\"n\":\"??????\"},{\"v\":\"1\",\"n\":\"??????\"},{\"v\":\"2\",\"n\":\"??????\"},{\"v\":\"3\",\"n\":\"??????\"},{\"v\":\"4\",\"n\":\"??????\"}],\"key\":\"iarea\"},{\"name\":\"??????\",\"value\":[{\"v\":\"-1\",\"n\":\"??????\"},{\"v\":\"2022\",\"n\":\"2022\"},{\"v\":\"2021\",\"n\":\"2021\"},{\"v\":\"50\",\"n\":\"2020\"},{\"v\":\"11\",\"n\":\"2019\"},{\"v\":\"2018\",\"n\":\"2018\"},{\"v\":\"2017\",\"n\":\"2017\"},{\"v\":\"1\",\"n\":\"2016\"},{\"v\":\"2\",\"n\":\"2015\"},{\"v\":\"3\",\"n\":\"2014\"},{\"v\":\"4\",\"n\":\"2013\"},{\"v\":\"5\",\"n\":\"2012\"},{\"v\":\"6\",\"n\":\"2011\"},{\"v\":\"7\",\"n\":\"00??????\"},{\"v\":\"8\",\"n\":\"90??????\"},{\"v\":\"9\",\"n\":\"80??????\"},{\"v\":\"10\",\"n\":\"??????\"}],\"key\":\"iyear\"},{\"name\":\"??????\",\"value\":[{\"v\":\"-1\",\"n\":\"??????\"},{\"v\":\"1\",\"n\":\"??????\"},{\"v\":\"2\",\"n\":\"??????\"}],\"key\":\"ipay\"},{\"name\":\"??????\",\"value\":[{\"v\":\"-1\",\"n\":\"??????\"},{\"v\":\"46\",\"n\":\"?????????\"},{\"v\":\"44\",\"n\":\"??????\"},{\"v\":\"45\",\"n\":\"??????\"}],\"key\":\"anime_status\"},{\"name\":\"??????\",\"value\":[{\"v\":\"1\",\"n\":\"??????\"},{\"v\":\"2\",\"n\":\"3D??????\"},{\"v\":\"3\",\"n\":\"2D??????\"},{\"v\":\"4\",\"n\":\"??????\"},{\"v\":\"5\",\"n\":\"??????\"}],\"key\":\"item\"}],\"doco\":[{\"name\":\"??????\",\"value\":[{\"v\":\"19\",\"n\":\"??????\"},{\"v\":\"18\",\"n\":\"??????\"},{\"v\":\"20\",\"n\":\"??????\"},{\"v\":\"22\",\"n\":\"????????????\"}],\"key\":\"sort\"},{\"name\":\"????????????\",\"value\":[{\"v\":\"-1\",\"n\":\"??????\"},{\"v\":\"1\",\"n\":\"BBC\"},{\"v\":\"4\",\"n\":\"????????????\"},{\"v\":\"3175\",\"n\":\"HBO\"},{\"v\":\"2\",\"n\":\"NHK\"},{\"v\":\"7\",\"n\":\"????????????\"},{\"v\":\"3530\",\"n\":\"ITV\"},{\"v\":\"3174\",\"n\":\"????????????\"},{\"v\":\"3176\",\"n\":\"ZDF\"},{\"v\":\"3172\",\"n\":\"ARTE\"},{\"v\":\"15\",\"n\":\"????????????\"},{\"v\":\"6\",\"n\":\"????????????\"},{\"v\":\"5\",\"n\":\"??????\"}],\"key\":\"itrailer\"},{\"name\":\"??????\",\"value\":[{\"v\":\"-1\",\"n\":\"??????\"},{\"v\":\"4\",\"n\":\"??????\"},{\"v\":\"9\",\"n\":\"??????\"},{\"v\":\"3\",\"n\":\"??????\"},{\"v\":\"5\",\"n\":\"??????\"},{\"v\":\"1\",\"n\":\"??????\"},{\"v\":\"2\",\"n\":\"??????\"},{\"v\":\"7\",\"n\":\"??????\"},{\"v\":\"13\",\"n\":\"??????\"},{\"v\":\"15\",\"n\":\"??????\"},{\"v\":\"6\",\"n\":\"??????\"},{\"v\":\"11\",\"n\":\"??????\"},{\"v\":\"10\",\"n\":\"??????\"}],\"key\":\"itype\"},{\"name\":\"??????\",\"value\":[{\"v\":\"-1\",\"n\":\"??????\"},{\"v\":\"1\",\"n\":\"??????\"},{\"v\":\"2\",\"n\":\"??????\"}],\"key\":\"pay\"}],\"child\":[{\"name\":\"??????\",\"value\":[{\"v\":\"19\",\"n\":\"??????\"},{\"v\":\"18\",\"n\":\"??????\"},{\"v\":\"20\",\"n\":\"??????\"}],\"key\":\"sort\"},{\"name\":\"??????\",\"value\":[{\"v\":\"-1\",\"n\":\"??????\"},{\"v\":\"1\",\"n\":\"??????\"},{\"v\":\"2\",\"n\":\"??????\"},{\"v\":\"3\",\"n\":\"??????\"}],\"key\":\"iarea\"},{\"name\":\"??????\",\"value\":[{\"v\":\"-1\",\"n\":\"??????\"},{\"v\":\"1\",\"n\":\"0-3???\"},{\"v\":\"2\",\"n\":\"4-6???\"},{\"v\":\"3\",\"n\":\"7-9???\"},{\"v\":\"4\",\"n\":\"10?????????\"}],\"key\":\"iyear\"},{\"name\":\"??????\",\"value\":[{\"v\":\"-1\",\"n\":\"??????\"},{\"v\":\"1\",\"n\":\"??????\"},{\"v\":\"2\",\"n\":\"??????\"}],\"key\":\"gender\"},{\"name\":\"??????\",\"value\":[{\"v\":\"-1\",\"n\":\"??????\"},{\"v\":\"1\",\"n\":\"??????\"},{\"v\":\"2\",\"n\":\"??????\"},{\"v\":\"3\",\"n\":\"??????????????\"},{\"v\":\"4\",\"n\":\"??????\"},{\"v\":\"5\",\"n\":\"??????\"},{\"v\":\"7\",\"n\":\"??????\"},{\"v\":\"6\",\"n\":\"??????\"},{\"v\":\"8\",\"n\":\"??????\"},{\"v\":\"9\",\"n\":\"?????????\"},{\"v\":\"10\",\"n\":\"??????\"},{\"v\":\"11\",\"n\":\"????????????\"},{\"v\":\"12\",\"n\":\"??????????????\"},{\"v\":\"13\",\"n\":\"??????\"},{\"v\":\"14\",\"n\":\"????????????\"},{\"v\":\"15\",\"n\":\"??????\"},{\"v\":\"16\",\"n\":\"??????\"}],\"key\":\"itype\"},{\"name\":\"??????\",\"value\":[{\"v\":\"-1\",\"n\":\"??????\"},{\"v\":\"1\",\"n\":\"??????\"},{\"v\":\"2\",\"n\":\"??????\"}],\"key\":\"ipay\"}],\"knowledge\":[{\"name\":\"??????\",\"value\":[{\"v\":\"41\",\"n\":\"??????\"},{\"v\":\"10\",\"n\":\"??????\"}],\"key\":\"sort\"},{\"name\":\"??????\",\"value\":[{\"v\":\"-1\",\"n\":\"??????\"},{\"v\":\"2\",\"n\":\"????????????\"},{\"v\":\"3\",\"n\":\"????????????\"},{\"v\":\"4\",\"n\":\"??????\"},{\"v\":\"5\",\"n\":\"????????????\"},{\"v\":\"7\",\"n\":\"??????\"},{\"v\":\"9\",\"n\":\"????????????\"},{\"v\":\"8\",\"n\":\"????????????\"},{\"v\":\"6\",\"n\":\"????????????\"},{\"v\":\"12\",\"n\":\"??????\"},{\"v\":\"14\",\"n\":\"????????????\"},{\"v\":\"15\",\"n\":\"??????\"},{\"v\":\"16\",\"n\":\"IT/?????????\"}],\"key\":\"pay_level_one\"}]}");
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
                jSONObject.put("url", str2);
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
            String str2 = "http://node.video.qq.com/x/api/msearch?keyWord=" + str;
            JSONArray jSONArray = new JSONObject(OkHttpUtil.string(str2, getHeaders(str2))).getJSONArray("uiData");
            JSONArray jSONArray2 = new JSONArray();
            for (int i = 0; i < jSONArray.length(); i++) {
                JSONObject jSONObject = jSONArray.getJSONObject(i).getJSONArray("data").getJSONObject(0);
                JSONObject jSONObject2 = new JSONObject();
                jSONObject2.put("vod_id", jSONObject.optString("id"));
                jSONObject2.put("vod_name", jSONObject.optString("title"));
                jSONObject2.put("vod_pic", jSONObject.optString("posterPic"));
                jSONObject2.put("vod_remarks", jSONObject.optString("publishDate"));
                jSONArray2.put(jSONObject2);
            }
            JSONObject jSONObject3 = new JSONObject();
            jSONObject3.put("list", jSONArray2);
            return jSONObject3.toString();
        } catch (Exception e) {
            SpiderDebug.log(e);
            return "";
        }
    }
}
