package com.winding.kiwisport.activity;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.EditText;

import com.amap.api.navi.AMapNavi;
import com.amap.api.navi.AMapNaviListener;
import com.amap.api.navi.AMapNaviView;
import com.amap.api.navi.AMapNaviViewListener;
import com.amap.api.navi.AMapNaviViewOptions;
import com.amap.api.navi.enums.NaviType;
import com.amap.api.navi.model.AMapLaneInfo;
import com.amap.api.navi.model.AMapNaviCameraInfo;
import com.amap.api.navi.model.AMapNaviCross;
import com.amap.api.navi.model.AMapNaviInfo;
import com.amap.api.navi.model.AMapNaviLocation;
import com.amap.api.navi.model.AMapNaviTrafficFacilityInfo;
import com.amap.api.navi.model.AMapServiceAreaInfo;
import com.amap.api.navi.model.AimLessModeCongestionInfo;
import com.amap.api.navi.model.AimLessModeStat;
import com.amap.api.navi.model.NaviInfo;
import com.amap.api.navi.model.NaviLatLng;
import com.autonavi.tbt.TrafficFacilityInfo;
import com.winding.kiwisport.R;
import com.winding.kiwisport.TTSController;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by 刘少帅 on 2017/11/6
 */

public class NavigationActivity extends AppCompatActivity {

    public static final String TAG = "MMM";
    private AMapNaviView mMnv;
    private EditText mEtS;
    private EditText mEtE;
    private AMapNavi mAMapNavi;


    //算路终点坐标
    protected NaviLatLng mEndLatlng;
    //算路起点坐标
    protected NaviLatLng mStartLatlng;
    //存储算路起点的列表
    protected final List<NaviLatLng> sList = new ArrayList<>();
    //存储算路终点的列表
    protected final List<NaviLatLng> eList = new ArrayList<>();
    protected List<NaviLatLng> mWayPointList;
    private TTSController mTtsManager;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_navigation);

        //初始化控件
        mMnv = (AMapNaviView) findViewById(R.id.amv_navigation_amv);
        mEtS = (EditText) findViewById(R.id.et_navigation_start);
        mEtE = (EditText) findViewById(R.id.et_navigation_end);

        mMnv.onCreate(savedInstanceState);

        mAMapNavi = AMapNavi.getInstance(getApplicationContext());
        //集合加入起点,终点
//        Poi start = new Poi("三元桥", new LatLng(39.96087,116.45798), "");
///**终点传入的是北京站坐标,但是POI的ID "B000A83M61"对应的是北京西站，所以实际算路以北京西站作为终点**/
//        Poi end = new Poi("北京站", new LatLng(39.904556, 116.427231), "B000A83M61");
        sList.add(new NaviLatLng(39.96087,116.45798));
        eList.add(new NaviLatLng(39.904556, 116.427231));
        //添加监听回调，用于处理算路成功
        mAMapNavi.addAMapNaviListener(new AMapNaviListener() {
            @Override
            public void onInitNaviFailure() {

            }

            @Override
            public void onInitNaviSuccess() {
                /**
                 * 方法:
                 *   int strategy=mAMapNavi.strategyConvert(congestion, avoidhightspeed, cost, hightspeed, multipleroute);
                 * 参数:
                 * @congestion 躲避拥堵
                 * @avoidhightspeed 不走高速
                 * @cost 避免收费
                 * @hightspeed 高速优先
                 * @multipleroute 多路径
                 *
                 * 说明:
                 *      以上参数都是boolean类型，其中multipleroute参数表示是否多条路线，如果为true则此策略会算出多条路线。
                 * 注意:
                 *      不走高速与高速优先不能同时为true
                 *      高速优先与避免收费不能同时为true
                 */
                int strategy=0;
                try {
                    strategy = mAMapNavi.strategyConvert(true, false, false, false, false);
                } catch (Exception e) {
                    e.printStackTrace();
                }
                mAMapNavi.calculateDriveRoute(sList, eList, mWayPointList, strategy);
            }

            @Override
            public void onStartNavi(int i) {

            }

            @Override
            public void onTrafficStatusUpdate() {

            }

            @Override
            public void onLocationChange(AMapNaviLocation aMapNaviLocation) {

            }

            @Override
            public void onGetNavigationText(int i, String s) {

            }

            @Override
            public void onGetNavigationText(String s) {
                //获取移动时的数据
                Log.e(TAG, "onGetNavigationText: "+s );

            }

            @Override
            public void onEndEmulatorNavi() {

            }

            @Override
            public void onArriveDestination() {

            }

            @Override
            public void onCalculateRouteFailure(int i) {

            }

            @Override
            public void onReCalculateRouteForYaw() {

            }

            @Override
            public void onReCalculateRouteForTrafficJam() {

            }

            @Override
            public void onArrivedWayPoint(int i) {

            }

            @Override
            public void onGpsOpenStatus(boolean b) {

            }

            @Override
            public void onNaviInfoUpdate(NaviInfo naviInfo) {//获取导航时的数据
                //获取距离和时间
                int pathRetainDistance = naviInfo.getPathRetainDistance();
                int pathRetainTime = naviInfo.getPathRetainTime();
                Log.e(TAG, "onNaviInfoUpdate: "+pathRetainDistance+"time"+pathRetainTime );
                //获取指示图标类型
                int iconType = naviInfo.getIconType();
                switch (iconType) {
                    case 0:
                    case 1:
                        //mIvDirection.setBackgroundResource(R.mipmap.wangshang);
                        break;
                    case 9:
                        //mIvDirection.setBackgroundResource(R.mipmap.zhizou);
                        break;
                    case 2:
                        //mIvDirection.setBackgroundResource(R.mipmap.zuozhuan);
                        break;
                    case 3:
                        //mIvDirection.setBackgroundResource(R.mipmap.youzhuan);
                        break;
                    case 51:
                        //mIvDirection.setBackgroundResource(R.mipmap.zuoxia);
                        break;
                    case 52:
                        //mIvDirection.setBackgroundResource(R.mipmap.youxia);
                        break;
                }
            }

            @Override
            public void onNaviInfoUpdated(AMapNaviInfo aMapNaviInfo) {

            }

            @Override
            public void updateCameraInfo(AMapNaviCameraInfo[] aMapNaviCameraInfos) {

            }

            @Override
            public void onServiceAreaUpdate(AMapServiceAreaInfo[] aMapServiceAreaInfos) {

            }

            @Override
            public void showCross(AMapNaviCross aMapNaviCross) {

            }

            @Override
            public void hideCross() {

            }

            @Override
            public void showLaneInfo(AMapLaneInfo[] aMapLaneInfos, byte[] bytes, byte[] bytes1) {

            }

            @Override
            public void hideLaneInfo() {

            }

            @Override
            public void onCalculateRouteSuccess(int[] ints) {
                //显示路径或开启导航

                //onCalculateRouteSuccess(ints);
                //mAMapNavi.startNavi(NaviType.EMULATOR);//模拟导航
                mAMapNavi.startNavi(NaviType.GPS);//实际导航
            }

            @Override
            public void notifyParallelRoad(int i) {

            }

            @Override
            public void OnUpdateTrafficFacility(AMapNaviTrafficFacilityInfo aMapNaviTrafficFacilityInfo) {

            }

            @Override
            public void OnUpdateTrafficFacility(AMapNaviTrafficFacilityInfo[] aMapNaviTrafficFacilityInfos) {

            }

            @Override
            public void OnUpdateTrafficFacility(TrafficFacilityInfo trafficFacilityInfo) {

            }

            @Override
            public void updateAimlessModeStatistics(AimLessModeStat aimLessModeStat) {

            }

            @Override
            public void updateAimlessModeCongestionInfo(AimLessModeCongestionInfo aimLessModeCongestionInfo) {

            }

            @Override
            public void onPlayRing(int i) {

            }
        });


        mMnv.setAMapNaviViewListener(new AMapNaviViewListener() {
            @Override
            public void onNaviSetting() {

            }

            @Override
            public void onNaviCancel() {

            }

            @Override
            public boolean onNaviBackClick() {
                return false;
            }

            @Override
            public void onNaviMapMode(int i) {

            }

            @Override
            public void onNaviTurnClick() {

            }

            @Override
            public void onNextRoadClick() {

            }

            @Override
            public void onScanViewButtonClick() {

            }

            @Override
            public void onLockMap(boolean b) {

            }

            @Override
            public void onNaviViewLoaded() {

            }
        });
        //实例化语音引擎
        mTtsManager = TTSController.getInstance(this);
        mTtsManager.init();
        //      启动语音引擎    监听
        mAMapNavi.addAMapNaviListener(mTtsManager);

//设置模拟导航的行车速度
        mAMapNavi.setEmulatorNaviSpeed(75);

        AMapNaviViewOptions aMapNaviViewOptions = new AMapNaviViewOptions();
        aMapNaviViewOptions.setCrossDisplayShow(false);
        aMapNaviViewOptions.setRealCrossDisplayShow(false);


        aMapNaviViewOptions.setAutoChangeZoom(true);
        // aMapNaviViewOptions.setLayoutVisible(true);
        // aMapNaviViewOptions.setAutoDrawRoute(true);
        mMnv.setViewOptions(aMapNaviViewOptions);
    }

    @Override
    protected void onResume() {
        super.onResume();

        //mTtsManager.init();


        mMnv.onResume();
    }

    @Override
    protected void onPause() {
        super.onPause();


        mMnv.onPause();
        mTtsManager.stopSpeaking();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        mMnv.onDestroy();
        mAMapNavi.stopNavi();
        mAMapNavi.destroy();
        mTtsManager.destroy();
    }

//    /**
//     * 导航初始化失败的回调
//     */
//    @Override
//    public void onInitNaviFailure() {
//
//    }
//    /**
//     * 导航播报信息回调函数。
//     * @param // 语音播报文字
//     **/
//    @Override
//    public void onGetNavigationText(String s) {
//        Log.e(TAG, "onGetNavigationText: "+s);
//
//
//    }
//    /**
//     * 当GPS位置有更新时的回调函数。
//     *@param //当前自车坐标位置
//     **/
//    @Override
//    public void onLocationChange(AMapNaviLocation aMapNaviLocation) {
//
//    }
//
//    /**到达目的地后回调函数。
//     * @param b
//     */
//    @Override
//    public void onArriveDestination(boolean b) {
//
//    }
//    /**
//     * 启动导航后的回调函数
//     **/
//    @Override
//    public void onStartNavi(int i) {
//
//    }
//    /**
//     * 算路成功回调
//     * @param // 路线id数组
//     */
//    @Override
//    public void onCalculateRouteSuccess(int[] ints) {
//
//        //mAMapNavi.startNavi(NaviType.EMULATOR);
//    }
//    /**
//     * 步行或者驾车路径规划失败后的回调函数
//     **/
//    @Override
//    public void onCalculateRouteFailure(int i) {
//
//    }
//    /**
//     * 停止语音回调，收到此回调后用户可以停止播放语音
//     **/
//    @Override
//    public void onStopSpeaking() {
//
//    }

    /**开始导航
     * @param view
     */
    public void startListenet(View view) {
//        //传入任意的起点和终点
//        Poi start = new Poi("三元桥", new LatLng(39.96087,116.45798), "");
///**终点传入的是北京站坐标,但是POI的ID "B000A83M61"对应的是北京西站，所以实际算路以北京西站作为终点**/
//        Poi end = new Poi("北京站", new LatLng(39.904556, 116.427231), "B000A83M61");
//        AmapNaviPage.getInstance().showRouteActivity(this, new AmapNaviParams(start, null, end, AmapNaviType.DRIVER), this);
    }
}
