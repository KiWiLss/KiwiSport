package com.winding.kiwisport;

import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.os.Bundle;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.WindowManager;
import android.widget.EditText;
import android.widget.Toast;

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
import com.amap.api.maps.model.LatLng;
import com.amap.api.maps.model.Marker;
import com.amap.api.maps.model.MarkerOptions;
import com.amap.api.maps.model.MyLocationStyle;
import com.winding.kiwisport.utils.Utils;

public class MainActivity extends AppCompatActivity {

    public static final String TAG = "MMM";
    private MapView mMapView;
    private EditText mEtInput;
    private AMap mMap;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_ALWAYS_HIDDEN);

        mEtInput = (EditText) findViewById(R.id.et_input);
        //获取地图控件引用
        mMapView = (MapView) findViewById(R.id.map);
        //在activity执行onCreate时执行mMapView.onCreate(savedInstanceState)，创建地图
        mMapView.onCreate(savedInstanceState);
        mMap = mMapView.getMap();

        //mMap.showIndoorMap(true);//开启室内地图


        //初始化定位
        initLocation();
        Log.e(TAG, "onCreate: sahn1=="+ Utils.sHA1(this));
        initBigSmall();
        //绘制点标记
        initMarker();
    }

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

        mMap.addMarker(markerOption);

        // 绑定 Marker 被点击事件
        mMap.setOnMarkerClickListener(new AMap.OnMarkerClickListener() {
            @Override
            public boolean onMarkerClick(Marker marker) {
                return false;
            }
        });
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
    //绘制 InfoWindow
//       mMap.setInfoWindowAdapter(new AMap.InfoWindowAdapter() {
//           @Override
//           public View getInfoWindow(Marker marker) {//当实现此方法并返回有效值时（返回值不为空，则视为有效）
//               return null;
//           }
//
//           @Override
//           public View getInfoContents(Marker marker) {//此方法和 getInfoWindow（Marker marker） 方法的实质是一样的
//               return null;
//           }
//       });

        //InfoWindow 点击事件
        mMap.setOnInfoWindowClickListener(new AMap.OnInfoWindowClickListener() {
            @Override
            public void onInfoWindowClick(Marker marker) {

                Toast.makeText(MainActivity.this, "点击了infowindow", Toast.LENGTH_SHORT).show();

            }
        });

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
    //声明定位回调监听器
    public AMapLocationListener mLocationListener = new AMapLocationListener() {
        @Override
        public void onLocationChanged(AMapLocation aMapLocation) {
            if (aMapLocation != null) {
                if (aMapLocation.getErrorCode() == 0) {
                //可在其中解析amapLocation获取相应内容。
                    double latitude = aMapLocation.getLatitude();
                    double longitude = aMapLocation.getLongitude();
                    String city = aMapLocation.getCity();
                    Log.e(TAG, "onLocationChanged: "+latitude+"longitude="+longitude+"city="+city+"||"+isFirst);
                    //mLocationClient.stopLocation();//停止定位后，本地定位服务并不会被销毁

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
                        myLocationStyle.myLocationType(MyLocationStyle.LOCATION_TYPE_SHOW);//只定位一次。
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
