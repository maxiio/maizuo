package com.maizuo.fiveone.maizuo.cinemas;


import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ListView;
import android.widget.TextView;

import com.maizuo.fiveone.maizuo.R;
import com.maizuo.fiveone.maizuo.RN.DevActivity;
import com.maizuo.fiveone.maizuo.RN.MyReactActivity;
import com.maizuo.fiveone.maizuo.filmDetail.ActorAdaper;
import com.maizuo.fiveone.maizuo.filmDetail.FilmDetail;
import com.maizuo.fiveone.maizuo.filmDetail.RequestInfo;
import com.maizuo.fiveone.maizuo.main.Fragment.cinema.Cinema;
import com.maizuo.fiveone.maizuo.main.Fragment.cinema.CityAdaper;
import com.maizuo.fiveone.maizuo.main.Fragment.cinema.ListAdaper;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Created by MyPC on 2019/7/20.
 */

public class CinemasActivity extends AppCompatActivity {
    private ListView listView;
    ViewGroup rangeTabs;

    private List allList = new ArrayList<Map<String, Object>>();
    private List dateList = new ArrayList<Map<String, Object>>();
    private List<Cinema.District> cityList = new ArrayList ();
    private ListAdaper adaper;
    private CityAdaper cityAdaper;
    private DateAdaper dateAdaper;

    private RequestCinemas requestCinemas = new RequestCinemas();
    private RequestCinemaList requestCinemaList = new RequestCinemaList();
    private RequestInfo requestInfo = new RequestInfo();

    private JSONArray showCinemas;
    private int activeDateIndex = 0;
    private int activeRangeIndex = -1;
    private int cityId = 0;
    private String cityName = "全部";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.cinemas_activity);
        // 注册请求响应
        initFilmInfoCall();
        initCinemasCall();
        intCinmaListCall();

        initDateAdaper();
        initCityAdaper();
        initCinemasAdaper();
        initOnclickEvent();
        // 获取请求参数
        String filmId = getIntent().getStringExtra("filmId");
        requestInfo.setFilmId(filmId);
        requestInfo.getFilmInfo();
        requestCinemas.setFilmId(filmId);
        requestCinemas.getCinemas();
    }
    public void initDateAdaper(){
        LinearLayoutManager manager = new LinearLayoutManager(getBaseContext());
        manager.setOrientation(GridLayoutManager.HORIZONTAL);
        RecyclerView recyclerView = findViewById(R.id.date_recycler_view);
        recyclerView.setLayoutManager(manager);
        dateAdaper = new DateAdaper(dateList, getBaseContext());
        recyclerView.setAdapter(dateAdaper);
    }
    public void initDateItemClick(){
        dateAdaper.setOnItemClickListener(new DateAdaper.OnItemClickListener(){
            public void onItemClick(View v){

            }
        });
    }
    public void initCityAdaper(){
        GridLayoutManager manager = new GridLayoutManager(getBaseContext(), 4);
        RecyclerView recyclerView = findViewById(R.id.city_recycler_view);
        recyclerView.setLayoutManager(manager);
        cityAdaper = new CityAdaper(cityList, getBaseContext());
        recyclerView.setAdapter(cityAdaper);
    }
    public void initCinemasAdaper(){
        adaper = new ListAdaper(allList, getBaseContext());
        listView = (ListView)findViewById(R.id.list);
        listView.setAdapter(adaper);
    }
    public void initOnclickEvent(){
        initRangeSelectEvent();
        initCityItemClick();
        initDateItemClick();
        initCinemasItemClick();
        initBackClick();

        findViewById(R.id.mask).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                toggleMask(false);
            }
        });
    }
    public void initBackClick(){
        findViewById(R.id.goback).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                finish();
            }
        });
    }
    public void initRangeSelectEvent(){
        rangeTabs = (ViewGroup)findViewById(R.id.range_select);
        for (int i = 0; i < rangeTabs.getChildCount(); i++) {
            View tabPane = rangeTabs.getChildAt(i);
            tabPane.setTag(i);
            tabPane.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    setRangeSelectTab((Integer) view.getTag());
                }
            });
        }
    }
    public void initCityItemClick(){
        cityAdaper.setOnItemClickListener(new CityAdaper.OnItemClickListener(){
            public void onItemClick(View v){
                cityId = (Integer) v.getTag();
                for (int i = 0; i < cityList.size(); i++) {
                    Cinema.District d = cityList.get(i);
                    if (d.cityId == cityId) {
                        d.viewType = 2;
                        cityName = d.cityName;
                    } else {
                        d.viewType =1;
                    }
                }
                ViewGroup tabs = (ViewGroup)findViewById(R.id.range_select);
                TextView text = (TextView)tabs.getChildAt(0);
                text.setText(cityName);
                setRangeSelectTab(activeRangeIndex);
                cityAdaper.notifyDataSetChanged();
            }
        });
    }
    public void initCinemasItemClick(){
        adaper.setOnItemClickListener(new ListAdaper.OnItemClickListener(){
            @Override
            public void onItemClick(View view, String cinemaId) {

               Intent intent = new Intent(CinemasActivity.this, MyReactActivity.class);
                intent.putExtra("data", cinemaId);
                intent.putExtra("module", "cinemaDetail");
                startActivity(intent);
            }
        });
    }
    public void setRangeSelectTab(int index) {
        TextView activeTextView =  (TextView)rangeTabs.getChildAt(index);
        if (index != activeRangeIndex) {
            activeRangeIndex = index;
            toggleMask(true);
            activeTextView.setTextColor(Color.parseColor("#ff5f16"));
        } else {
            activeRangeIndex = -1;
            toggleMask(false);
        }
    }
    // 切换弹窗
    public void toggleMask(Boolean isshow){
        View mask = findViewById(R.id.mask);
        ViewGroup groupOptions =  (ViewGroup)findViewById(R.id.options);
        // tab样式初始化
        for (int i = 0; i < rangeTabs.getChildCount(); i++) {
            TextView tabPane = (TextView)rangeTabs.getChildAt(i);
            tabPane.setTextColor(Color.parseColor("#191a1b"));
        }
        for (int j =0; j < groupOptions.getChildCount(); j++) {
            View opt = groupOptions.getChildAt(j);
            opt.setVisibility(View.GONE);
        }
        if (isshow == false) {
            mask.setVisibility(View.GONE);
            listView.setVisibility(View.VISIBLE);
        } else {
            mask.setVisibility(View.VISIBLE);
            listView.setVisibility(View.GONE);
            groupOptions.getChildAt(activeRangeIndex).setVisibility(View.VISIBLE);
        }

    }
    public void initFilmInfoCall() {
        requestInfo.setOnCall(new RequestInfo.OnCall() {
            @Override
            public void OnCallBack(JSONObject responseData) throws JSONException {
                JSONObject data = (JSONObject) responseData.get("data");
                JSONObject film = (JSONObject) data.get("film");
                TextView title = (TextView)findViewById(R.id.title);
                title.setText(film.getString("name"));

            }
        });
    }
    public void initCinemasCall() {
        requestCinemas.setOnCall(new RequestCinemas.OnCall() {
            @Override
            public void OnCallBack(JSONObject responseData) throws JSONException {
                List list = new ArrayList<Map<String, Object>>();
                JSONObject data = (JSONObject) responseData.get("data");
                showCinemas = (JSONArray) data.get("showCinemas");
                // 日期列表


                Map<String, String> dateMap = new HashMap<>();
                final String dayNames[] = { "周日", "周一", "周二", "周三", "周四", "周五","周六" };
                Calendar calendar = Calendar.getInstance();


                for (int i = 0; i < showCinemas.length();i++) {
                    Map<String, Object> map = new HashMap<>();
                    JSONObject cinema = (JSONObject)showCinemas.get(i);

                    Date date = new Date(cinema.getLong("showDate")*1000);
                    calendar.setTime(date);
                    int dayOfWeek = calendar.get(Calendar.DAY_OF_WEEK)-1;


                    map.put("name", dayNames[dayOfWeek]+new SimpleDateFormat( "MM月dd日" ).format( cinema.getLong("showDate")*1000));
                    map.put("showDate", cinema.getString("showDate"));
                    dateList.add(map);
                }
                Collections.sort(dateList, new Comparator<Map<String, String>>(){
                    public int compare(Map<String, String> o1, Map<String, String> o2) {
                        int a = Integer.parseInt(o1.get("showDate"));
                        int b = Integer.parseInt(o2.get("showDate"));
                        if (a > b) {
                            return 1;
                        } else if(a == b) {
                            return 0;
                        } else
                            return -1;

                    }
                });

                dateAdaper.notifyDataSetChanged();
                getCinemaList();
            }
        });
    }
    public void getCinemaList() throws JSONException{
        JSONObject cinema = (JSONObject)showCinemas.get(activeDateIndex);
        JSONArray cinemaList = (JSONArray)cinema.get("cinemaList");
        Map<String, String> params = new HashMap();
        JSONObject params1 = new JSONObject();
        StringBuilder s = new StringBuilder();
        for (int i = 0; i < cinemaList.length(); i++ ) {
            s.append(cinemaList.get(i).toString());
            if (i != cinemaList.length()-1) {
                s.append(",");
            }
        }
        params.put("cityId", "440300");
        params.put("cinemaIds", s.toString());
        params1.put("cityId", "440300");
        params1.put("cinemaIds", s.toString());
        requestCinemaList.setJsonBody(params1);
        requestCinemaList.getCinemaList();
    }
    public void intCinmaListCall() {
        requestCinemaList.setOnCall(new RequestCinemaList.OnCall() {
            @Override
            public void OnCallBack(JSONObject responseData) throws JSONException {
                JSONObject data = (JSONObject) responseData.get("data");
                JSONArray cinemas = (JSONArray)data.get("cinemas");

                Map<Integer, Cinema.District> cityMap = new HashMap();
                cityList.add(new Cinema.District(0, "全部", 2));
                for (int i = 0; i < cinemas.length(); i++) {
                    JSONObject object = (JSONObject) cinemas.get(i);
                    // 城市数据
                    int districtId = object.getInt("districtId")  ;
                    if (cityMap.get(districtId) == null) {
                        Cinema.District district = new Cinema.District(districtId, object.getString("districtName"), 1);
                        cityMap.put(districtId, district);
                        cityList.add(district);
                    }
                    // 电影院数据
                    Map map = new HashMap<String, Object>();
                    String address = "", name = ""; String lowPrice = "", cinemaId = "";
                    if (object.has("cinemaId")) map.put("cinemaId", object.getString("cinemaId"));
                    if (object.has("name")) map.put("name", object.getString("name"));
                    if (object.has("address")) map.put("address", object.getString("address"));
                    if (object.has("lowPrice")){

                        map.put("lowPrice", Integer.toString(object.getInt("lowPrice")/100));
                    }
                    allList.add(map);
                }
                adaper.notifyDataSetChanged();
                cityAdaper.notifyDataSetChanged();
            }
        });
    }

    /**
     * Created by MyPC on 2019/7/31.
     */


}