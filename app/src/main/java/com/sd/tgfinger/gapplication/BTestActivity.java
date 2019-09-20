package com.sd.tgfinger.gapplication;

import android.annotation.SuppressLint;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.TextView;

import com.sd.tgfinger.api.TGAPI;
import com.sd.tgfinger.utils.AlertDialogUtil;
import com.sd.tgfinger.utils.LogUtils;
import com.sd.tgfinger.utils.ToastUtil;

import java.io.File;
import java.util.Arrays;


/**
 * Created By pq
 * on 2019/6/23
 */
public class BTestActivity extends AppCompatActivity implements View.OnClickListener {

    private TGAPI tgapi;
    private TextView tipTv, devStatus;
    @SuppressLint("InlinedApi")
    private String[] perms = new String[]{
            android.Manifest.permission.WRITE_EXTERNAL_STORAGE
    };
    private AlertDialog alertDialog;

    private int readDataType = -1;
    private int templType = -1;
    private TextView volumeTt, SDKVersion;
    private Button closeDevBtn;
    private Button openDevBtn;
    private Button voiceDecreaceBtn;
    private Button voiceIncreaceBtn;
    private Button cancelRegisterBtnBehind;
    private Button registerBtnBehind;
    private Button ver1_nBtn;
    private Button ver1_1Btn;
    private Button getTemplFW;
    private Button getTemplSN;
    private Button templTimeBtn;
    private Button getTemplAlgorVersionBtn;
    private CheckBox autoUpdateTempl;
    private RadioButton templ3Rb;
    private RadioButton templ6Rb;
    private RadioGroup templSumModel;


    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_behind);

        tgapi = TGAPI.getTGAPI();
        initView();
        pers();//权限申请
        String sdkVersion = tgapi.getSDKVersion();
        SDKVersion.setText(sdkVersion);
    }

    private void pers() {
        int i = ContextCompat.checkSelfPermission(this, perms[0]);
        if (i == PackageManager.PERMISSION_DENIED) {
            ActivityCompat.requestPermissions(this, perms, 0x11);
        } else {
            //后比算法初始化
            TGAPI.getTGAPI().init(this, null);
            openDev();
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions,
                                           @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (grantResults[0] == PackageManager.PERMISSION_DENIED) {
            ActivityCompat.requestPermissions(this, permissions, 0x11);
        } else {
//            //后比算法初始化
            TGAPI.getTGAPI().init(this, null);
            openDev();
        }
    }

    private void initView() {
        closeDevBtn = findViewById(R.id.closeDevBtn);
        openDevBtn = findViewById(R.id.openDevBtn);
        voiceDecreaceBtn = findViewById(R.id.voiceDecreaceBtn);
        voiceIncreaceBtn = findViewById(R.id.voiceIncreaceBtn);
        cancelRegisterBtnBehind = findViewById(R.id.cancelRegisterBtnBehind);
        registerBtnBehind = findViewById(R.id.registerBtnBehind);
        ver1_nBtn = findViewById(R.id.ver1_NBtn);
        ver1_1Btn = findViewById(R.id.ver1_1Btn);
        getTemplFW = findViewById(R.id.getTemplFW);
        getTemplSN = findViewById(R.id.getTemplSN);
        templTimeBtn = findViewById(R.id.templTimeBtn);
        getTemplAlgorVersionBtn = findViewById(R.id.getTemplAlgorVersionBtn);
        autoUpdateTempl = findViewById(R.id.autoUpdateTempl);
        volumeTt = findViewById(R.id.volumeTt);
        SDKVersion = findViewById(R.id.SDKVersion);
        devStatus = findViewById(R.id.devStatus);
        templSumModel = findViewById(R.id.templSumModel);
        templ3Rb = findViewById(R.id.templ3Rb);
        templ6Rb = findViewById(R.id.templ6Rb);
        tipTv = findViewById(R.id.tipTv);

        tgapi.setVolume(this, 1);
        getCurrentVoice();
        //默认特征模式为6模板
        templType = TGAPI.TEMPL_MODEL_6;
        tgapi.setTemplModelType(templType);

        openDevBtn.setOnClickListener(this);
        closeDevBtn.setOnClickListener(this);
        voiceDecreaceBtn.setOnClickListener(this);
        voiceIncreaceBtn.setOnClickListener(this);
        cancelRegisterBtnBehind.setOnClickListener(this);
        registerBtnBehind.setOnClickListener(this);
        ver1_nBtn.setOnClickListener(this);
        ver1_1Btn.setOnClickListener(this);
        getTemplFW.setOnClickListener(this);
        getTemplSN.setOnClickListener(this);
        templTimeBtn.setOnClickListener(this);
        getTemplAlgorVersionBtn.setOnClickListener(this);
        //验证成功后，自动更新模板
        autoUpdateTempl.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton compoundButton, boolean b) {

            }
        });
        //切换设置3/6特征模板的模式
        templSumModel.setOnCheckedChangeListener(new RadioGroup.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(RadioGroup radioGroup, int i) {
                if (templ3Rb.getId() == i) {
                    templType = TGAPI.TEMPL_MODEL_3;
                    tgapi.setTemplModelType(TGAPI.TEMPL_MODEL_3);
                    //初始化数据，开启连续验证
                    //readFingerData(3);
                } else if (templ6Rb.getId() == i) {
                    templType = TGAPI.TEMPL_MODEL_6;
                    tgapi.setTemplModelType(TGAPI.TEMPL_MODEL_6);
                    //初始化数据，开启连续验证
                    //readFingerData(3);
                }
            }
        });
    }

    private void openDev() {
        if (!tgapi.isDevOpen()) {
            alertDialog = AlertDialogUtil.Instance()
                    .showWaitDialog(this, "设备正在打开...");
            tgapi.openDev(handler, TGAPI.WORK_BEHIND, TGAPI.TEMPL_MODEL_6, true);
        } else {
            toast("设备已经打开");
        }
    }

    //取消抓图的标记 0:正常取消抓图 1:注册取消抓图 2:验证取消抓图
    private int cancelGetImgType = 0;

    @Override
    public void onClick(View view) {
        switch (view.getId()) {
            case R.id.closeDevBtn:
                if (tgapi.isDevOpen())
                    tgapi.closeDev(handler);
                else
                    toast("设备已经关闭");
                break;
            case R.id.openDevBtn:
                openDev();
                break;
            case R.id.voiceDecreaceBtn:
                boolean descreaseVolume = tgapi.decreaseVolume();
                if (descreaseVolume) getCurrentVoice();
                break;
            case R.id.voiceIncreaceBtn:
                boolean increaseVolume = tgapi.increaseVolume();
                if (increaseVolume) getCurrentVoice();
                break;
            case R.id.cancelRegisterBtnBehind:
                cancelRegisterBtnBehind.setClickable(false);
                cancelGetImgType = 2;
                tgapi.cancelRegisterGetImg(handler);
                break;
            case R.id.registerBtnBehind:
                //注册前读取数据，查重
                templSumModel.setEnabled(false);
                templ3Rb.setEnabled(false);
                templ6Rb.setEnabled(false);
                registerBtnBehind.setClickable(false);
                if (tgapi.isContinueVerify()) {
                    //处于连续验证状态,先调用取消抓图接口，获取已注册的数据查重，，再调用注册接口
                    cancelGetImgType = 1;
                    Log.d("===KKK", " cancelGetImgType: " + cancelGetImgType);
                    tgapi.cancelRegisterGetImg(handler);
                } else {
                    //没有处于验证中，获取已注册的数据查重，调用注册接口
                    readFingerData(1);
                }
                break;
            case R.id.ver1_NBtn:
                readFingerData(2);
                break;
            case R.id.ver1_1Btn:
                //1:1注册
                String dataPath = tgapi.moniExter6Path + File.separator + "1566370752907.dat";
                LogUtils.d("1:1的数据路径:" + dataPath);
                this.readDataType = 4;
                tgapi.readDataFromHost(handler, dataPath);
                break;
            case R.id.getTemplFW:
//                tgapi.getTemplFW(handler,);
                break;
            case R.id.getTemplSN:
//                tgapi.getTemplSN(handler, );
                break;
            case R.id.templTimeBtn:
//                 tgapi.getTemplTime(handler,);
                break;
            case R.id.getTemplAlgorVersionBtn:
//                tgapi.getTemplVersion(handler,);
                break;
        }
    }

    @SuppressLint("HandlerLeak")
    private Handler handler = new Handler() {

        @Override
        public void handleMessage(Message msg) {
            super.handleMessage(msg);
            switch (msg.what) {
                case TGAPI.DEV_STATUS:
                    /*
                     * 设备状态：
                     *  1：设备状态：已连接
                     *  -1：已断开,连接中...
                     */
                    int devStatusArg = msg.arg1;
                    if (devStatusArg >= 0) {
                        if (tipTv.getText().toString().contains("断开")) {
                            showTip("设备状态：已连接");
                            //初始化数据，开启连续验证
                            //readFingerData(3);
                            templSumModel.setEnabled(true);
                            templ3Rb.setEnabled(true);
                            templ6Rb.setEnabled(true);
                            registerBtnBehind.setClickable(true);
                        }
                        devStatus.setText("设备状态:已连接");
                    } else if (devStatusArg == -1) {
                        showTip("设备状态：未连接");
                        devStatus.setText("设备状态:未连接");
                    } else if (devStatusArg == -2) {
                        showTip("设备状态：未连接/已断开");
                        devStatus.setText("设备状态:已断开,重新连接中...");
                    }
                    break;
                case TGAPI.OPEN_DEV:
                    /**
                     * 1:设备打开成功，后比设置成功
                     * -1:设备打开失败
                     */
                    int openArg = msg.arg1;
                    if (openArg == 1) {
                        showTip("设备打开成功");
                        //设置设备的工作模式
                        tgapi.setDevWorkModel(handler, TGAPI.WORK_BEHIND);
                        AlertDialogUtil.Instance().disDialog();
                        //初始化数据，开启连续验证
                        //readFingerData(3);
                    } else if (openArg == -1) {
                        showTip("设备打开失败");
                    }
                    break;
                case TGAPI.READ_FILE:
                    //读取数据
                    Bundle bundle = msg.getData();
                    if (bundle != null) {
                        byte[] fingerData = bundle.getByteArray(TGAPI.DATA);
                        int fingerSize = bundle.getInt(TGAPI.FINGER_SIZE);
                        //以下注销时测试1:1的验证代码，简单测试
//                        if (fingerSize == 1 && readDataType == 4) {
//                            verify1(fingerData);
//                        } else
                        if (fingerData != null && fingerData.length > 0) {
                            if (readDataType == 1) {
                                Log.d("===KKK", "  执行注册111");
                                tgapi.extractFeatureRegister(handler, fingerData, fingerSize, false);
                            } else if (readDataType == 2) {
                                Log.d("===KKK", "  执行单次验证");

                                tgapi.featureCompare1_N(handler, fingerData, fingerSize, false);
                            } else if (readDataType == 3) {
                                Log.d("===KKK", "  执行连续验证");
                                toast("数据准备完毕");
                                //开启连续验证,true表示大特征
                                tgapi.continueVerifyN(handler, fingerData, fingerSize, 1000, false);
                            }
                        } else {
                            if (readDataType == 1) {
                                Log.d("===KKK", "  执行注册222");
                                tgapi.extractFeatureRegister(handler, null, 0, false);
                            } else if (readDataType == 2) {
                                toast("暂无模板数据，请先注册模板");
                            }
                        }
                    }
                    break;
                case TGAPI.WRITE_FILE:
                    //存储数据
                    int saveArg = msg.arg1;
                    if (saveArg == 1) {
                        showTip("数据存储成功");
                        //初始化数据，开启连续验证
                        //readFingerData(3);
                    } else if (saveArg == -1) {
                        showTip("数据存储失败");
                    }
                    break;
                case TGAPI.CLOSE_DEV:
                    /**
                     * 1:指静脉设备关闭成功
                     * -1:指静脉设备关闭失败
                     */
                    int closeArg = msg.arg1;
                    if (closeArg == 1) {
                        showTip("指静脉设备关闭成功");
                    } else if (closeArg == -1) {
                        showTip("指静脉设备关闭失败");
                    }
                    break;
                case TGAPI.DEV_WORK_MODEL:
                    /**
                     * 1:前比3特征模板
                     * 2:后比
                     * 3:前比6特征模板
                     * -1:超时
                     */
                    int devModelArg = msg.arg1;
                    if (devModelArg == 1) {
                        showTip("前比3特征模板");
                    } else if (devModelArg == 2) {
                        showTip("后比");
                    } else if (devModelArg == 3) {
                        showTip("前比6特征模板");
                    } else if (devModelArg == -1) {
                        showTip("超时");
                    }
                    break;
                case TGAPI.CANCEL_REGISTER://取消注册接口也可取消验证
                    /**
                     * 1:取消注册成功
                     * -1:取消注册失败
                     */
                    int cancelRegisterArg = msg.arg1;
                    if (cancelRegisterArg == 1) {
                        if (cancelGetImgType == 1) {
                            showTip("取消连续验证成功");
                            //初始化数据，注册
                            readFingerData(1);
                        } else if (cancelGetImgType == 2) {
                            showTip("取消注册成功");
                            //初始化数据，开启连续验证
                            //readFingerData(3);
                            templ3Rb.setEnabled(true);
                            templ6Rb.setEnabled(true);
                            templSumModel.setEnabled(true);
                            cancelRegisterBtnBehind.setClickable(true);
                        }
                    } else if (cancelRegisterArg == -1) {
                        showTip("取消注册失败");
                        templ3Rb.setEnabled(true);
                        templ6Rb.setEnabled(true);
                        templSumModel.setEnabled(true);
                        cancelRegisterBtnBehind.setClickable(true);
                    }
                    break;
                case TGAPI.EXTRACT_FEATURE_REGISTER:
                    /**
                     * 注册
                     * 1:特征提取成功,Output数据有效
                     * 2:特征提取失败,因证书路径错误,Output数据无效
                     * 3:特征提取失败,因证书内容无效,Output数据无效
                     * 4:特征提取失败,因证书内容过期,Output数据无效
                     * 5:特征提取失败,因"图像"数据无效,Output数据无效
                     * 6:特征提取失败,因"图像"质量较差,Output数据无效
                     * 7:模板登记重复
                     * 8:登记成功
                     * 9:特征融合失败，因"特征"数据一致性差，Output数据无效
                     * -1:特征提取失败,因参数不合法,Output数据无效
                     * -2:注册特征提取失败
                     * -3:抓图超时
                     * -4:设备断开
                     * -5:操作取消
                     * -6:入参错误
                     * -7:抓图未知错误
                     * -8:特征比对（1：N）失败，因参数不合法，Output数据无效
                     * -9:验证提取特征失败
                     * -10:登记失败
                     */
                    int registerArg = msg.arg1;
                    if (registerArg != 1) {
                        templ3Rb.setEnabled(true);
                        templ6Rb.setEnabled(true);
                        templSumModel.setEnabled(true);
                        //readFingerData(3);
                    }
                    if (registerArg == 1) {
                        showTip("特征提取成功,Output数据有效");
                    } else if (registerArg == 2) {
                        showTip("特征提取失败,因证书路径错误,Output数据无效");
                    } else if (registerArg == 3) {
                        showTip("特征提取失败,因证书路径错误,Output数据无效");
                    } else if (registerArg == 4) {
                        showTip("特征提取失败,因证书内容过期,Output数据无效");
                    } else if (registerArg == 5) {
                        showTip("特征提取失败,因\"图像\"数据无效,Output数据无效");
                    } else if (registerArg == 6) {
                        showTip("特征提取失败,因\"图像\"质量较差,Output数据无效");
                    } else if (registerArg == 7) {
                        showTip("模板登记重复");
                    } else if (registerArg == 8) {
                        showTip("登记成功");
                        Bundle data = msg.getData();
                        if (data != null) {
                            byte[] fingerData = data.getByteArray(TGAPI.FINGER_DATA);
                            Log.d("===KKK", "   fingerData:" + Arrays.toString(fingerData));
                            if (fingerData != null && fingerData.length > 0) {
                                if (fingerData[0] != 0) {
                                    //模拟数据库存储数据
                                    saveData(fingerData);
                                }
                                showTip("接收到注册的模板数据");
                            }
                        }
                    } else if (registerArg == 9) {
                        showTip("特征融合失败，因\"特征\"数据一致性差，Output数据无效");
                    } else if (registerArg == -1) {
                        showTip("特征提取失败,因参数不合法,Output数据无效");
                    } else if (registerArg == -2) {
                        showTip("注册特征提取失败");
                    } else if (registerArg == -3) {
                        showTip("抓图超时");
                    } else if (registerArg == -4) {
                        showTip("设备断开");
                    } else if (registerArg == -5) {
                        Log.d("===KKK", "操作取消  111");
                        showTip("操作取消");
                    } else if (registerArg == -6) {
                        showTip("入参错误");
                    } else if (registerArg == -7) {
                        showTip("抓图未知错误");
                    } else if (registerArg == -8) {
                        showTip("特征比对（1：N）失败，因参数不合法，Output数据无效");
                    } else if (registerArg == -9) {
                        showTip("验证提取特征失败");
                    } else if (registerArg == -10) {
                        showTip("登记失败");
                    }
                    registerBtnBehind.setClickable(true);
                    break;
                case TGAPI.FEATURE_COMPARE1_N:
                case TGAPI.CONTINUE_VERIFY:
                    /**
                     * 1:验证成功,Output数据有效
                     * -1:特征比对（1：N）失败，因参数不合法，Output数据无效
                     * -2:特征比对（1：N）失败，仅Output的matchScore数据有效
                     * -3:抓图超时
                     * -4:设备断开
                     * -5:操作取消
                     * -6:入参错误
                     * -7:抓图未知错误
                     * -8:验证提取特征失败
                     */
                    int compareNArg = msg.arg1;
                    if (compareNArg == 1) {
                        Bundle data = msg.getData();
                        if (data != null) {
                            //获取比对的分数，比对模板的位置，比对的后可更新的模板数据
                            int score = data.getInt(TGAPI.COMPARE_SCORE);
                            LogUtils.d("分数:" + score);
                            int index = data.getInt(TGAPI.INDEX);
                            byte[] updateFinger = data.getByteArray(TGAPI.UPDATE_FINGER);
                            showTip("特征比对（1：N）成功，Output数据有效:" + score);
                        }
                    } else if (compareNArg == -1) {
                        showTip("特征比对（1：N）失败，因参数不合法，Output数据无效");
                    } else if (compareNArg == -2) {
                        Bundle data = msg.getData();
                        if (data != null) {
                            int score = data.getInt(TGAPI.COMPARE_SCORE);
                            showTip("特征比对（1：N）失败，仅Output的matchScore数据有效:" + score);
                        }
                    } else if (compareNArg == -3) {
                        showTip("抓图超时");
                    } else if (compareNArg == -4) {
                        showTip("设备断开");
                    } else if (compareNArg == -5) {
                        Log.d("===KKK", "操作取消  222");
                        showTip("操作取消");
                    } else if (compareNArg == -6) {
                        showTip("入参错误");
                    } else if (compareNArg == -7) {
                        showTip("抓图未知错误");
                    } else if (compareNArg == -8) {
                        showTip("验证提取特征失败");
                    }
                    break;
                case TGAPI.FEATURE_COMPARE1_1:
                    int compare1Arg = msg.arg1;
                    if (compare1Arg == 1) {
                        Bundle data = msg.getData();
                        if (data != null) {
                            //获取比对的分数，比对模板的位置，比对的后可更新的模板数据
                            int score = data.getInt(TGAPI.COMPARE_SCORE);
                            byte[] updateFinger = data.getByteArray(TGAPI.UPDATE_FINGER);
                            showTip("特征比对（1：1）成功，Output数据有效:" + score);
                        }
                    } else if (compare1Arg == -1) {
                        showTip("特征比对（1：1）失败，因参数不合法，Output数据无效");
                    } else if (compare1Arg == -2) {
                        Bundle data = msg.getData();
                        if (data != null) {
                            int score = data.getInt(TGAPI.COMPARE_SCORE);
                            showTip("特征比对（1：1）失败，仅Output的matchScore数据有效:" + score);
                        }
                    } else if (compare1Arg == -3) {
                        showTip("抓图超时");
                    } else if (compare1Arg == -4) {
                        showTip("设备断开");
                    } else if (compare1Arg == -5) {
                        Log.d("===KKK", "操作取消  222");
                        showTip("操作取消");
                    } else if (compare1Arg == -6) {
                        showTip("入参错误");
                    } else if (compare1Arg == -7) {
                        showTip("抓图未知错误");
                    } else if (compare1Arg == -8) {
                        showTip("验证提取特征失败");
                    }
                    break;
                case TGAPI.TEMPL_FV_VERSION:
                    /**
                     * 1：获取成功， Output数据有效
                     * -1：获取失败，参数错误，Output数据无效
                     */
                    getTNstr(msg, 1);
                    break;
                case TGAPI.TEMPL_SN:
                    getTNstr(msg, 2);
                    break;
                case TGAPI.TEMPL_FW:
                    getTNstr(msg, 3);
                    break;
                case TGAPI.TEMPL_TIME:
                    getTNstr(msg, 4);
                    break;
            }
        }
    };

    @Override
    protected void onDestroy() {
        super.onDestroy();
        tgapi.closeDev(handler);
        if (alertDialog != null && alertDialog.isShowing()) {
            alertDialog.dismiss();
            alertDialog = null;
        }
    }

    private void verify1(byte[] singleFinger) {
        tgapi.featureCompare1_1(handler, singleFinger, false);
    }

    private void getTNstr(Message msg, int type) {
        int arg1 = msg.arg1;
        if (arg1 == 1) {
            Bundle data = msg.getData();
            if (data != null) {
                String str = data.getString(TGAPI.TN_STR);
                if (type == 1) {
                    showTip("模板算法版本号:" + str);
                } else if (type == 2) {
                    showTip("模板设备序列号:" + str);
                } else if (type == 3) {
                    showTip("模板设备固件号:" + str);
                } else if (type == 4) {
                    showTip("模板生成时间:" + str);
                }
            }
        } else if (arg1 == -1) {
            showTip("获取失败，参数错误，Output数据无效");
        }
    }

    private void readFingerData(int type) {
        this.readDataType = type;
        readData();
    }

    private void getCurrentVoice() {
        String currentVolume = tgapi.getCurrentVolume(this);
        volumeTt.setText(currentVolume);
    }

    private void readData() {
        if (templType == TGAPI.TEMPL_MODEL_3) {
            tgapi.readDataFromHost(handler, tgapi.moniExter3Path);
        } else if (templType == TGAPI.TEMPL_MODEL_6) {
            tgapi.readDataFromHost(handler, tgapi.moniExter6Path);
        }
    }

    private void saveData(byte[] data) {
        if (data != null && data.length > 0) {
            long l = System.currentTimeMillis();
            String fileName = String.valueOf(l) + ".dat";
            if (templType == TGAPI.TEMPL_MODEL_3) {
                String path = tgapi.moniExter3Path + File.separator + fileName;
                tgapi.saveDataToHost(handler, data, path);
            } else if (templType == TGAPI.TEMPL_MODEL_6) {
                String path = tgapi.moniExter6Path + File.separator + fileName;
                tgapi.saveDataToHost(handler, data, path);
            }
        }
    }

    private void toast(String tip) {
        ToastUtil.toast(this, tip);
    }

    private void showTip(String tip) {
        tipTv.setText(tip);
    }


}
