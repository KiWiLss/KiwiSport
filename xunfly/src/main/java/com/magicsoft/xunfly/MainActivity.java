package com.magicsoft.xunfly;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.Environment;
import android.support.v7.app.AppCompatActivity;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;

import com.iflytek.cloud.ErrorCode;
import com.iflytek.cloud.GrammarListener;
import com.iflytek.cloud.InitListener;
import com.iflytek.cloud.RecognizerListener;
import com.iflytek.cloud.RecognizerResult;
import com.iflytek.cloud.SpeechConstant;
import com.iflytek.cloud.SpeechError;
import com.iflytek.cloud.SpeechRecognizer;
import com.iflytek.cloud.SpeechUtility;
import com.iflytek.cloud.ui.RecognizerDialog;
import com.iflytek.cloud.ui.RecognizerDialogListener;
import com.iflytek.sunflower.FlowerCollector;
import com.magicsoft.xunfly.activity.SynthesisActivity;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.HashMap;
import java.util.LinkedHashMap;

public class MainActivity extends AppCompatActivity {


    public static final String TAG = "MMM";
    private SharedPreferences mSharedPreferences;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        // 将“12345678”替换成您申请的APPID，申请地址：http://www.xfyun.cn
// 请勿在“=”与appid之间添加任何空字符或者转义符
        SpeechUtility.createUtility(this, SpeechConstant.APPID +"=5a05025f");

    }
    // 语音识别对象
    private SpeechRecognizer mAsr;
    // 云端语法文件
    private String mCloudGrammar = null;
    //语音引擎类型
    String mEngineType = SpeechConstant.TYPE_CLOUD;//在线识别
    String mEngineType2 = SpeechConstant.TYPE_LOCAL;//离线识别

    // 语法、词典临时变量
    String mContent;
    // 函数调用返回值
    int ret = 0;
    private static final String KEY_GRAMMAR_ABNF_ID = "grammar_abnf_id";
    private static final String GRAMMAR_TYPE_ABNF = "abnf";
    private static final String GRAMMAR_TYPE_BNF = "bnf";

    /**在线语音识别
     * @param view
     */
    public void soundJudge(View view) {
        //初始化识别对象
        mAsr = SpeechRecognizer.createRecognizer(this, mInitListener);
        mCloudGrammar = FucUtil.readFile(this,"grammar_sample.abnf","utf-8");

        mSharedPreferences = getSharedPreferences(getPackageName(),	MODE_PRIVATE);
        //构建语法
        if( null == mAsr ){
            // 创建单例失败，与 21001 错误为同样原因，参考 http://bbs.xfyun.cn/forum.php?mod=viewthread&tid=9688
            this.showTip( "创建对象失败，请确认 libmsc.so 放置正确，且有调用 createUtility 进行初始化" );
            return;
        }

        if(null == mEngineType) {
            showTip("请先选择识别引擎类型");
            return;
        }
        RecognizerDialog mDialog = new RecognizerDialog(this, mInitListener);

        //若要将 RecognizerDialog 用于语义理解，必须添加以下参数设置，设置之后 onResult 回调返回将是语义理解的结果
        // mDialog.setParameter("asr_sch", "1");
        // mDialog.setParameter("nlp_version", "3.0");
        //3.设置回调接口
        mDialog.setListener(mRecognizerDialogListener);
        //4.显示 dialog，接收语音输入

        mDialog.show();

        // 在线-构建语法文件，生成语法id
        mContent = new String(mCloudGrammar);
        //指定引擎类型
        mAsr.setParameter(SpeechConstant.ENGINE_TYPE, mEngineType);
        mAsr.setParameter(SpeechConstant.TEXT_ENCODING,"utf-8");
        ret = mAsr.buildGrammar(GRAMMAR_TYPE_ABNF, mContent, mCloudGrammarListener);
        if(ret != ErrorCode.SUCCESS){
            showTip("语法构建失败,错误码：" + ret);
        }

        //开始识别
        // 设置参数
        if (!setParam()) {
            showTip("请先构建语法。");
            return;
        };

        ret = mAsr.startListening(mRecognizerListener);
        if (ret != ErrorCode.SUCCESS) {
            showTip("识别失败,错误码: " + ret);
        }

    }

    /**
     * 听写UI监听器,语音识别的结果
     */
    private RecognizerDialogListener mRecognizerDialogListener = new RecognizerDialogListener() {
        public void onResult(RecognizerResult results, boolean isLast) {

            //处理结果
            printResult(results);
            Log.e(TAG, "onResult: ------->"+results.toString() );




        }

        /**
         * 识别回调错误.
         */
        public void onError(SpeechError error) {
            //            if(mTranslateEnable && error.getErrorCode() == 14002) {
            //                 ToastUtils.showShortToast( error.getPlainDescription(true)+"\n请确认是否已开通翻译功能" );
            //            } else {
            //                 ToastUtils.showShortToast(error.getPlainDescription(true));
            //            }
        }

    };

    /**处理识别的结果
     * @param results
     */

    // 用HashMap存储听写结果
    private HashMap<String, String> mIatResults = new LinkedHashMap<>();

    private void printResult(RecognizerResult results) {
        String text = JsonParser.parseIatResult(results.getResultString());

        String sn = null;
        // 读取json结果中的sn字段
        try {
            JSONObject resultJson = new JSONObject(results.getResultString());
            sn = resultJson.optString("sn");
        } catch (JSONException e) {
            e.printStackTrace();
        }

        mIatResults.put(sn, text);

        StringBuffer resultBuffer = new StringBuffer();
        for (String key : mIatResults.keySet()) {
            resultBuffer.append(mIatResults.get(key));
        }
        //结果
        Log.e("MMM", "printResult: "+resultBuffer.toString().replaceAll("。",""));





    }


    /**
     * 识别监听器。
     */
    private RecognizerListener mRecognizerListener = new RecognizerListener() {

        @Override
        public void onVolumeChanged(int volume, byte[] data) {
            showTip("当前正在说话，音量大小：" + volume);
            Log.d(TAG, "返回音频数据："+data.length);
        }

        @Override
        public void onResult(final RecognizerResult result, boolean isLast) {
            if (null != result) {
                Log.d(TAG, "recognizer result：" + result.getResultString());
                String text ;
                if("cloud".equalsIgnoreCase(mEngineType)){
                    text = JsonParser.parseGrammarResult(result.getResultString());
                }else {
                    text = JsonParser.parseLocalGrammarResult(result.getResultString());
                }
                // 显示
                Log.e(TAG, "onResult: ***********"+text );
            } else {
                Log.d(TAG, "recognizer result : null");
            }
        }

        @Override
        public void onEndOfSpeech() {
            // 此回调表示：检测到了语音的尾端点，已经进入识别过程，不再接受语音输入
            showTip("结束说话");
        }

        @Override
        public void onBeginOfSpeech() {
            // 此回调表示：sdk内部录音机已经准备好了，用户可以开始语音输入
            showTip("开始说话");
        }

        @Override
        public void onError(SpeechError error) {
            showTip("onError Code："	+ error.getErrorCode());
        }

        @Override
        public void onEvent(int eventType, int arg1, int arg2, Bundle obj) {
            // 以下代码用于获取与云端的会话id，当业务出错时将会话id提供给技术支持人员，可用于查询会话日志，定位出错原因
            // 若使用本地能力，会话id为null
            //	if (SpeechEvent.EVENT_SESSION_ID == eventType) {
            //		String sid = obj.getString(SpeechEvent.KEY_EVENT_SESSION_ID);
            //		Log.d(TAG, "session id =" + sid);
            //	}
        }

    };

    /**
     * 参数设置
     * @return
     */
    public boolean setParam(){
        boolean result = false;
        //设置识别引擎
        mAsr.setParameter(SpeechConstant.ENGINE_TYPE, mEngineType);
        //设置返回结果为json格式
        mAsr.setParameter(SpeechConstant.RESULT_TYPE, "json");

        if("cloud".equalsIgnoreCase(mEngineType))
        {
            String grammarId = mSharedPreferences.getString(KEY_GRAMMAR_ABNF_ID, null);
            if(TextUtils.isEmpty(grammarId))
            {
                result =  false;
            }else {
                //设置云端识别使用的语法id
                mAsr.setParameter(SpeechConstant.CLOUD_GRAMMAR, grammarId);
                result =  true;
            }
        }

        // 设置音频保存路径，保存音频格式支持pcm、wav，设置路径为sd卡请注意WRITE_EXTERNAL_STORAGE权限
        // 注：AUDIO_FORMAT参数语记需要更新版本才能生效
        mAsr.setParameter(SpeechConstant.AUDIO_FORMAT,"wav");
        mAsr.setParameter(SpeechConstant.ASR_AUDIO_PATH, Environment.getExternalStorageDirectory()+"/msc/asr.wav");
        return result;
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();

        if( null != mAsr ){
            // 退出时释放连接
            mAsr.cancel();
            mAsr.destroy();
        }
    }

    @Override
    protected void onResume() {
        //移动数据统计分析
        FlowerCollector.onResume(this);
        FlowerCollector.onPageStart(TAG);
        super.onResume();
    }

    @Override
    protected void onPause() {
        //移动数据统计分析
        FlowerCollector.onPageEnd(TAG);
        FlowerCollector.onPause(this);
        super.onPause();
    }

    /**
     * 云端构建语法监听器。
     */
    private GrammarListener mCloudGrammarListener = new GrammarListener() {
        @Override
        public void onBuildFinish(String grammarId, SpeechError error) {
            if(error == null){
                String grammarID = new String(grammarId);
                SharedPreferences.Editor editor = mSharedPreferences.edit();
                if(!TextUtils.isEmpty(grammarId))
                    editor.putString(KEY_GRAMMAR_ABNF_ID, grammarID);
                editor.commit();
                showTip("语法构建成功：" + grammarId);
            }else{
                showTip("语法构建失败,错误码：" + error.getErrorCode());
            }
        }
    };
    /**
     * 初始化监听器。
     */
    private InitListener mInitListener = new InitListener() {

        @Override
        public void onInit(int code) {
            Log.e(TAG, "SpeechRecognizer init() code = " + code);
            if (code != ErrorCode.SUCCESS) {
                Log.e(TAG, "初始化失败,错误码："+code);
            }
        }
    };

    private void showTip(final String str) {
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                Log.e(TAG, "run: "+str );
            }
        });
    }

    public void cancel(View view) {
        mAsr.cancel();
        showTip("取消识别");
    }

    public void stop(View view) {
        mAsr.stopListening();
        showTip("停止识别");
    }

    /**离线语音合成
     * @param view
     */
    public void offline(View view) {


    }


    public void synthesisListener(View view) {
        startActivity(new Intent(this, SynthesisActivity.class));
    }
}
