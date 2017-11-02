package com.winding.kiwisport;

import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.os.Bundle;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.text.TextUtils;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.WindowManager;
import android.widget.AdapterView;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.alibaba.fastjson.JSON;
import com.amap.api.location.AMapLocation;
import com.amap.api.location.AMapLocationClient;
import com.amap.api.location.AMapLocationClientOption;
import com.amap.api.location.AMapLocationListener;
import com.amap.api.maps.AMap;
import com.amap.api.maps.AMapOptions;
import com.amap.api.maps.CameraUpdateFactory;
import com.amap.api.maps.MapView;
import com.amap.api.maps.UiSettings;
import com.amap.api.maps.model.BitmapDescriptorFactory;
import com.amap.api.maps.model.CameraPosition;
import com.amap.api.maps.model.LatLng;
import com.amap.api.maps.model.Marker;
import com.amap.api.maps.model.MarkerOptions;
import com.amap.api.maps.model.MyLocationStyle;
import com.amap.api.maps.model.Polyline;
import com.amap.api.maps.model.PolylineOptions;
import com.amap.api.services.core.AMapException;
import com.amap.api.services.core.LatLonPoint;
import com.amap.api.services.core.PoiItem;
import com.amap.api.services.core.SuggestionCity;
import com.amap.api.services.help.Inputtips;
import com.amap.api.services.help.InputtipsQuery;
import com.amap.api.services.help.Tip;
import com.amap.api.services.poisearch.PoiResult;
import com.amap.api.services.poisearch.PoiSearch;
import com.amap.api.services.route.BusRouteResult;
import com.amap.api.services.route.DrivePath;
import com.amap.api.services.route.DriveRouteResult;
import com.amap.api.services.route.RideRouteResult;
import com.amap.api.services.route.RouteSearch;
import com.amap.api.services.route.WalkRouteResult;
import com.winding.kiwisport.utils.Utils;

import java.util.ArrayList;
import java.util.List;

public class MainActivity extends AppCompatActivity {

    public static final String TAG = "MMM";
    private MapView mMapView;
    private EditText mEtInput;
    private AMap mMap;
    private ArrayList<Marker> mMarkerList;
    private ListView mLv;
    private double mLatitude;
    private double mLongitude;
    private int mTap;//区分点击的是哪种类型的图标
    private boolean isFirstPlan=true;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_ALWAYS_HIDDEN);

        mEtInput = (EditText) findViewById(R.id.et_input);

        mLv = (ListView) findViewById(R.id.lv_list);

        //获取地图控件引用
        mMapView = (MapView) findViewById(R.id.map);
        //在activity执行onCreate时执行mMapView.onCreate(savedInstanceState)，创建地图
        mMapView.onCreate(savedInstanceState);
        mMap = mMapView.getMap();

        //mMap.showIndoorMap(true);//开启室内地图

        //收集所有的marker
        mMarkerList = new ArrayList<>();
        //初始化定位
        initLocation();
        Log.e(TAG, "onCreate: sahn1=="+ Utils.sHA1(this));
        initBigSmall();
        //绘制点标记
        initMarker();
        //对地图点击监听,移动监听
        initMapClickListener();
        //在地图上绘制一条线
        drawOneLine();
        //输入内容搜索
        initSearchListener();
        //输入提示列表单一点击监听
        itemClickListener();
    }

    Marker mSingeMarke;//单独搜索的Marker
    private void itemClickListener() {
        mLv.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
                Log.e(TAG, "onItemClick: "+i+"|||"+l );
                String poiID = mTipList.get(i).getPoiID();
                //用id进行搜索
                PoiSearch poiSearch = new PoiSearch(MainActivity.this, null);
                poiSearch.setOnPoiSearchListener(new PoiSearch.OnPoiSearchListener() {
                    @Override
                    public void onPoiSearched(PoiResult poiResult, int i) {

                    }

                    @Override
                    public void onPoiItemSearched(PoiItem poiItem, int i) {
                        //获取PoiItem获取POI的详细信息
                        if (i == AMapException.CODE_AMAP_SUCCESS&&poiItem!=null) {//正确返回
                            Log.e(TAG, "onPoiItemSearched: *****"+JSON.toJSONString(poiItem) );
                            mLv.setVisibility(View.GONE);
                            mTap=3;
                            //mMap.clear();// 清理之前的图标
                            LatLng latLng = new LatLng(poiItem.getEnter().getLatitude(), poiItem.getEnter().getLongitude());
                            //mSingeMarke.remove();
                            mSingeMarke = mMap.addMarker(new MarkerOptions().position(latLng)
                                    .icon(BitmapDescriptorFactory.fromBitmap
                                            (BitmapFactory.decodeResource(getResources(), R.mipmap.big_blue_landmark))));
                            mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(latLng, 18f));

                        }
                    }
                });
                poiSearch.searchPOIIdAsyn(poiID);// 异步搜索

            }
        });
    }


    private void initSearchListener() {

        mEtInput.setOnEditorActionListener(new TextView.OnEditorActionListener() {
            @Override
            public boolean onEditorAction(TextView textView, int i, KeyEvent keyEvent) {

                String content = mEtInput.getText().toString();
                Log.e(TAG, "onEditorAction: "+content+"|||"+keyEvent.getAction() +"actionId"+i);

                if (!TextUtils.isEmpty(content)) {
                    //开始poi搜索,市搜索
                    startSearchCountry(content);
                    autoSearch2(content);
                }
                return false;
            }
        });


    }
    ArrayList<Tip> mTipList = new ArrayList<>();
    private void autoSearch2(String content) {
        //第二个参数传入null或者“”代表在全国进行检索，否则按照传入的city进行检索
        InputtipsQuery inputquery = new InputtipsQuery(content, mCurrentCity);
        inputquery.setCityLimit(true);//限制在当前城市
        Inputtips inputTips = new Inputtips(MainActivity.this, inputquery);
        inputTips.setInputtipsListener(new Inputtips.InputtipsListener() {
            @Override
            public void onGetInputtips(List<Tip> list, int i) {//搜索内容回调结果
                Log.e(TAG, "onGetInputtips: "+list.size()+"||"+JSON.toJSONString(list));
                if (i == 1000 && list!= null) {//正确的返回
                    mLv.setVisibility(View.VISIBLE);
                    mTipList.clear();
                    mTipList.addAll(list);
                    SearchAdapter adapter = new SearchAdapter(MainActivity.this, list);
                    mLv.setAdapter(adapter);
                }





            }
        });
        inputTips.requestInputtipsAsyn();

    }

    /**杭州市poi搜索
     * @param
     */
    PoiOverlay mPoiOverlay;
    private void startSearchCountry(String content) {
        PoiSearch.Query query = new PoiSearch.Query(content, "", mCurrentCity);
//keyWord表示搜索字符串，
//第二个参数表示POI搜索类型，二者选填其一，选用POI搜索类型时建议填写类型代码，码表可以参考下方（而非文字）
//cityCode表示POI搜索区域，可以是城市编码也可以是城市名称，也可以传空字符串，空字符串代表全国在全国范围内进行搜索
        query.setPageSize(20);// 设置每页最多返回多少条poiitem
        query.setPageNum(0);//设置查询页码
        //设置搜索结果监听
        PoiSearch poiSearch = new PoiSearch(this, query);
        poiSearch.setOnPoiSearchListener(new PoiSearch.OnPoiSearchListener() {
            @Override
            public void onPoiSearched(PoiResult poiResult, int i) {//i=1000,代表成功,其他为失败
                //搜索的全部结果
                ArrayList<PoiItem> pois = poiResult.getPois();
                Log.e(TAG, "onPoiSearched: pois--->" +pois.size()+"|||"+ JSON.toJSONString(pois));
                List<SuggestionCity> cityList = poiResult.getSearchSuggestionCitys();
                Log.e(TAG, "onPoiSearched: city--->"+JSON.toJSONString(cityList) );


                /*mMap.clear();// 清理之前的图标
                //展示搜索到的点
                 mPoiOverlay = new PoiOverlay(getContext(), mMap, pois, getResources());
                mPoiOverlay.removeFromMap();
                mPoiOverlay.addToMap();

                mPoiOverlay.zoomToSpan();*/


            }

            @Override
            public void onPoiItemSearched(PoiItem poiItem, int i) {
                Log.e(TAG, "onPoiItemSearched: "+JSON.toJSONString(poiItem) );


            }
        });
        LatLonPoint lp = new LatLonPoint(mLatitude, mLongitude);
        poiSearch.setBound(new PoiSearch.SearchBound(lp, 3000));//设置周边搜索的中心点以及半径
        poiSearch.searchPOIAsyn();
    }

    private void drawOneLine() {
        List<LatLng> latLngs = new ArrayList<LatLng>();
        latLngs.add(new LatLng(30.220356,120.270379));
        latLngs.add(new LatLng(35.220356,110.270379));
        latLngs.add(new LatLng(39.898323,116.057694));

        Polyline polyline = mMap.addPolyline(new PolylineOptions().
                addAll(latLngs).width(10).color(ContextCompat.getColor(this,R.color.green)));
        //可通过polyline设置属性
        //polyline.setVisible(true);
    }

    Marker mCustomMarke;

    private void initMapClickListener() {
        //对地图点击监听
        mMap.setOnMapClickListener(new AMap.OnMapClickListener() {
            @Override
            public void onMapClick(LatLng latLng) {
                Toast.makeText(MainActivity.this, "点击了地图", Toast.LENGTH_SHORT).show();
                //infowindow消失
                //mInfoWindowView.setVisibility(View.GONE);//无效
                //mCustomMarke.hideInfoWindow();对应的marker才有效
                for (Marker m :
                        mMarkerList) {
                    Log.e(TAG, "onMapClick: "+m.isInfoWindowShown() );
                    if (m.isInfoWindowShown()) {
                        m.hideInfoWindow();
                    }
                }
                //规划路线也要隐藏
                if (mDrivingRouteOverlay!=null) {
                    mDrivingRouteOverlay.removeFromMap();
                }
                if (mPoiOverlay!=null){//poi搜索
                    mPoiOverlay.removeFromMap();
                }

            }
        });

        //设置地图移动监听
        mMap.setOnCameraChangeListener(new AMap.OnCameraChangeListener() {
            @Override
            public void onCameraChange(CameraPosition cameraPosition) {
                Toast.makeText(MainActivity.this, "地图正在移动", Toast.LENGTH_SHORT).show();
                //infowindow消失
                //mInfoWindowView.setVisibility(View.GONE);
                for (Marker m :
                        mMarkerList) {
                    if (m.isInfoWindowShown()) {
                        m.hideInfoWindow();
                    }
                }

            }

            @Override
            public void onCameraChangeFinish(CameraPosition cameraPosition) {
                Toast.makeText(MainActivity.this, "地图移动完成", Toast.LENGTH_SHORT).show();
            }
        });


    }
    View mInfoWindowView;
    private void initMarker() {

        //添加默认的标记点
        LatLng latLng = new LatLng(30.220342,120.270377);
//         Marker marker = mMap.addMarker(new MarkerOptions().position(latLng).title("杭州市").snippet("DefaultMarker"));
        //添加自定义标记点
        MarkerOptions markerOption = new MarkerOptions();
        markerOption.position(latLng);
        markerOption.title("杭州市").snippet("杭州市：34.341568, 108.940174");

        markerOption.draggable(true);//设置Marker可拖动
        markerOption.icon(BitmapDescriptorFactory.fromBitmap(BitmapFactory
                .decodeResource(getResources(),R.mipmap.dinggan)));
        // 将Marker设置为贴地显示，可以双指下拉地图查看效果
        markerOption.setFlat(true);//设置marker平贴地图效果

        Marker marker1 = mMap.addMarker(markerOption);


        DisplayMetrics dm = new DisplayMetrics();
        getWindowManager().getDefaultDisplay().getMetrics(dm);
        int widthPixels = dm.widthPixels;
        int heightPixels = dm.heightPixels;
        Marker marker = mMap.addMarker(new MarkerOptions()
                .setFlat(false)
                .title("标记点").snippet("测试标记点")
                .icon(BitmapDescriptorFactory.fromBitmap
                        (BitmapFactory.decodeResource(getResources(), R.mipmap.dinggan)))
                .draggable(false));
        marker.setPositionByPixels(widthPixels / 2, heightPixels / 2);//标记点添加到屏幕中间

        mCustomMarke=marker;
        mMarkerList.add(marker1);
        mMarkerList.add(marker);
        //绘制 InfoWindow
       mMap.setInfoWindowAdapter(new AMap.InfoWindowAdapter() {
           @Override
           public View getInfoWindow(Marker marker) {//当实现此方法并返回有效值时（返回值不为空，则视为有效）
               //写个自定义的infoWindow
               mInfoWindowView = LayoutInflater.from(MainActivity.this).inflate(R.layout.info_window, null);
               TextView tvTitle = mInfoWindowView.findViewById(R.id.tv_info_title);
               TextView tvContent = mInfoWindowView.findViewById(R.id.tv_info_content);
                if (TextUtils.isEmpty(marker.getTitle())){
                    return null;
                }
               tvTitle.setText(marker.getTitle());
               tvContent.setText(marker.getSnippet());

               //marker.hideInfoWindow();//隐藏infowindow
               return mInfoWindowView;
           }

           @Override
           public View getInfoContents(Marker marker) {//此方法和 getInfoWindow（Marker marker） 方法的实质是一样的
               return null;
           }
       });

        //InfoWindow 点击事件
        mMap.setOnInfoWindowClickListener(new AMap.OnInfoWindowClickListener() {
            @Override
            public void onInfoWindowClick(Marker marker) {

                Toast.makeText(MainActivity.this, "点击了infowindow", Toast.LENGTH_SHORT).show();

            }
        });
        // 绑定 Marker 被点击事件
        mMap.setOnMarkerClickListener(new AMap.OnMarkerClickListener() {
            @Override
            public boolean onMarkerClick(Marker marker) {
                Toast.makeText(MainActivity.this, "marker被点击", Toast.LENGTH_SHORT).show();
                Log.e(TAG, "onMarkerClick: "+JSON.toJSONString(marker) );
                if (mTap==3){
                    Log.e(TAG, "onMarkerClick: singemarker3333" );
                    //开始为单独地点搜索规划路线
                    startGuHuaRoad(marker);



                }




                return false;
            }
        });
    }
    DrivingRouteOverlay mDrivingRouteOverlay;
    private void startGuHuaRoad(Marker marker) {
        Log.e(TAG, "startGuHuaRoad: msinglemarker-->"+JSON.toJSONString(mSingeMarke) );
        RouteSearch routeSearch = new RouteSearch(this);

        routeSearch.setRouteSearchListener(new RouteSearch.OnRouteSearchListener() {
            @Override
            public void onBusRouteSearched(BusRouteResult busRouteResult, int i) {

            }

            @Override
            public void onDriveRouteSearched(DriveRouteResult driveRouteResult, int i) {
                //解析result获取算路结果，可参考官方demo
                mMap.clear();// 清理地图上的所有覆盖物
                if (i == AMapException.CODE_AMAP_SUCCESS) {
                    if (driveRouteResult != null && driveRouteResult.getPaths() != null) {
                        if (driveRouteResult.getPaths().size() > 0) {
                            Log.e(TAG, "onDriveRouteSearched: "+JSON.toJSONString(driveRouteResult) );
                            final DrivePath drivePath = driveRouteResult.getPaths()
                                    .get(0);
                             mDrivingRouteOverlay = new DrivingRouteOverlay(
                                    MainActivity.this, mMap, drivePath,
                                    driveRouteResult.getStartPos(),
                                    driveRouteResult.getTargetPos(), null);

                            mDrivingRouteOverlay.setNodeIconVisibility(false);//设置节点marker是否显示
                            mDrivingRouteOverlay.setIsColorfulline(true);//是否用颜色展示交通拥堵情况，默认true
                            mDrivingRouteOverlay.removeFromMap();
                            mDrivingRouteOverlay.addToMap();

                            mDrivingRouteOverlay.zoomToSpan();



                        }
                    }
                }

            }

            @Override
            public void onWalkRouteSearched(WalkRouteResult walkRouteResult, int i) {

            }

            @Override
            public void onRideRouteSearched(RideRouteResult rideRouteResult, int i) {

            }
        });//回调监听
        //起点,当前定位点
        LatLonPoint startPoint = new LatLonPoint(mLatitude, mLongitude);
        //终点,点击的点
        LatLonPoint endPoint = new LatLonPoint(mSingeMarke.getPosition().latitude, mSingeMarke.getPosition().longitude);
        RouteSearch.FromAndTo fromAndTo = new RouteSearch.FromAndTo(startPoint, endPoint);

        // fromAndTo包含路径规划的起点和终点，drivingMode表示驾车模式
// 第三个参数表示途经点（最多支持16个），第四个参数表示避让区域（最多支持32个），第五个参数表示避让道路
        RouteSearch.DriveRouteQuery query = new RouteSearch.DriveRouteQuery(fromAndTo, RouteSearch.DrivingDefault, null, null, "");

        routeSearch.calculateDriveRouteAsyn(query);
    }


    /**
     * 一些简单的设置
     */
    private void initBigSmall() {
        UiSettings uiSettings = mMap.getUiSettings();
        //隐藏右下角放大,缩小
        uiSettings.setZoomControlsEnabled(false);
        //设置指南针是否显示
        uiSettings.setCompassEnabled(false);


        //定位按钮影响当前位置图标
        /*uiSettings.setMyLocationButtonEnabled(true); //显示默认的定位按钮

       // mMap.setMyLocationEnabled(true);// 可触发定位并显示当前位置

        mMap.setLocationSource(new LocationSource() {
            @Override
            public void activate(OnLocationChangedListener onLocationChangedListener) {//激活
                Log.e(TAG, "activate: " );
            }

            @Override
            public void deactivate() {//停用
                Log.e(TAG, "deactivate: " );
            }
        });//通过aMap对象设置定位数据源的监听*/



        uiSettings.setLogoPosition(AMapOptions.LOGO_POSITION_BOTTOM_LEFT);//设置logo位置
    }



    /**
     * 初始化定位
     */
    private static final int STROKE_COLOR = Color.argb(180, 3, 145, 255);
    private static final int FILL_COLOR = Color.argb(48, 156, 216, 255);
    boolean isFirst=true;//表示是否是首次进入
    //声明AMapLocationClient类对象
    public AMapLocationClient mLocationClient = null;
    private String mCurrentCity;
    //声明定位回调监听器
    public AMapLocationListener mLocationListener = new AMapLocationListener() {
        @Override
        public void onLocationChanged(AMapLocation aMapLocation) {
            if (aMapLocation != null) {
                if (aMapLocation.getErrorCode() == 0) {
                //可在其中解析amapLocation获取相应内容。
                    double latitude = aMapLocation.getLatitude();
                    double longitude = aMapLocation.getLongitude();

                    mLatitude=latitude;
                    mLongitude=longitude;


                    String city = aMapLocation.getCity();
                    mCurrentCity=city;
                    Log.e(TAG, "onLocationChanged: "+latitude+"longitude="+longitude+"city="+city+"||"+isFirst);
                    mLocationClient.stopLocation();//停止定位后，本地定位服务并不会被销毁

                    if (isFirst){//首次进入移动地图到地位点
                        //设置缩放级别
                        mMap.moveCamera(CameraUpdateFactory.zoomTo(16));
                        //将地图移动到定位点
                        mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(new LatLng(latitude, longitude), 16));
                        isFirst=false;
                        //设置地位蓝点
                        MyLocationStyle myLocationStyle;
                        myLocationStyle = new MyLocationStyle();//初始化定位蓝点样式类myLocationStyle.myLocationType(MyLocationStyle.LOCATION_TYPE_LOCATION_ROTATE);//连续定位、且将视角移动到地图中心点，定位点依照设备方向旋转，并且会跟随设备移动。（1秒1次定位）如果不设置myLocationType，默认也会执行此种模式。
                        myLocationStyle.interval(2000); //设置连续定位模式下的定位间隔，只在连续定位模式下生效，单次定位模式下不会生效。单位为毫秒。
                        //设置是否显示小蓝点
                        //myLocationStyle.showMyLocation();
                        //设置定位蓝点精度圆圈的边框颜色的方法。
                        myLocationStyle.strokeColor(ContextCompat.getColor(MainActivity.this,R.color.green));
                        //设置定位蓝点精度圆圈的填充颜色的方法。
                        myLocationStyle.radiusFillColor(FILL_COLOR);
                        //设置定位蓝点精度圈的边框宽度的方法。
                        myLocationStyle.strokeWidth(10);
                        myLocationStyle.myLocationType(MyLocationStyle.LOCATION_TYPE_LOCATION_ROTATE_NO_CENTER);////连续定位、蓝点不会移动到地图中心点，定位点依照设备方向旋转，并且蓝点会跟随设备移动。
                        //设置自定义图标
//                        BitmapDescriptor bitmapDescriptor = BitmapDescriptorFactory.fromBitmap
//                                (BitmapFactory.decodeResource(getResources(), R.mipmap.ic_launcher));
//                        myLocationStyle.myLocationIcon(bitmapDescriptor);
                        mMap.setMyLocationStyle(myLocationStyle);//设置定位蓝点的Style
//                        //aMap.getUiSettings().setMyLocationButtonEnabled(true);设置默认定位按钮是否显示，非必需设置。

                        mMap.setMyLocationEnabled(true);// 设置为true表示启动显示定位蓝点，false表示隐藏定位蓝点并不进行定位，默认是false。

                    }

                }else {
                    //定位失败时，可通过ErrCode（错误码）信息来确定失败的原因，errInfo是错误信息，详见错误码表。
                    Log.e("MMM","location Error, ErrCode:"
                            + aMapLocation.getErrorCode() + ", errInfo:"
                            + aMapLocation.getErrorInfo());
                }
            }

        }
    };
    //声明AMapLocationClientOption对象
    public AMapLocationClientOption mLocationOption = null;
    private void initLocation() {
        //初始化AMapLocationClientOption对象
        mLocationOption = new AMapLocationClientOption();
        //设置定位模式为AMapLocationMode.Hight_Accuracy，高精度模式。
        mLocationOption.setLocationMode(AMapLocationClientOption.AMapLocationMode.Hight_Accuracy);
        //获取一次定位结果：
        //该方法默认为false。
        mLocationOption.setOnceLocation(false);
        //获取最近3s内精度最高的一次定位结果：
        //设置setOnceLocationLatest(boolean b)接口为true，启动定位时SDK会返回最近3s内精度最高的一次定位结果。
        // 如果设置其为true，setOnceLocation(boolean b)接口也会被设置为true，反之不会，默认为false。
        mLocationOption.setOnceLocationLatest(false);
        //设置是否返回地址信息（默认返回地址信息）
        mLocationOption.setNeedAddress(true);
        //初始化定位
        mLocationClient = new AMapLocationClient(getApplicationContext());
        //设置定位回调监听
        mLocationClient.setLocationListener(mLocationListener);

        //给定位客户端对象设置定位参数
        mLocationClient.setLocationOption(mLocationOption);
        //启动定位
        mLocationClient.startLocation();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        //在activity执行onDestroy时执行mMapView.onDestroy()，销毁地图
        mMapView.onDestroy();
        //销毁定位客户端，同时销毁本地定位服务。
        mLocationClient.onDestroy();
    }
    @Override
    protected void onResume() {
        super.onResume();
        //在activity执行onResume时执行mMapView.onResume ()，重新绘制加载地图
        mMapView.onResume();
    }
    @Override
    protected void onPause() {
        super.onPause();
        //在activity执行onPause时执行mMapView.onPause ()，暂停地图的绘制
        mMapView.onPause();
    }
    @Override
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        //在activity执行onSaveInstanceState时执行mMapView.onSaveInstanceState (outState)，保存地图当前的状态
        mMapView.onSaveInstanceState(outState);
    }
}
