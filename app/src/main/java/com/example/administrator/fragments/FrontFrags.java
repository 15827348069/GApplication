package com.example.administrator.fragments;


import android.annotation.SuppressLint;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.v4.app.Fragment;
import android.support.v7.app.AlertDialog;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.OrientationHelper;
import android.support.v7.widget.RecyclerView;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.TextView;

import com.TG.library.CallBack.ActionClickListener;
import com.TG.library.CallBack.CommitCallBack;
import com.TG.library.api.TG661JAPI;
import com.TG.library.utils.AlertDialogUtil;
import com.TG.library.utils.FileUtil;
import com.TG.library.utils.LogUtils;
import com.TG.library.utils.RegularUtil;
import com.TG.library.utils.ToastUtil;
import com.example.administrator.adapters.ConsoleTipAdapter;
import com.example.administrator.adapters.HostTemplAdapter;
import com.example.administrator.adapters.TemplAdapter;
import com.example.administrator.gapplication.R;

import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.List;

/**
 * A simple {@link Fragment} subclass.
 */
public class FrontFrags extends Fragment implements View.OnClickListener,
        TemplAdapter.ItemClick, HostTemplAdapter.ItemClick, ActionClickListener
        , CheckBox.OnCheckedChangeListener, CommitCallBack {


    private boolean devStatus;
    private AlertDialog waitDialog;
    //标记区分设备中最大的模板数或者已注册的模板数
    private int devTempls = 0;
    private TG661JAPI tg661JAPI = TG661JAPI.getTG661JAPI();

    public FrontFrags() {

    }

    public static FrontFrags instance() {
        FrontFrags frontFrags = new FrontFrags();
        Bundle bundle = new Bundle();
        frontFrags.setArguments(bundle);
        return frontFrags;
    }

    public void setDevFW(String fw) {
        devFW.setText(String.format("设备固件号:%s", fw));
    }

    public void setDevSN(String sn) {
        devSN.setText(String.format("设备序列号:%s", sn));
    }

    public void setDevStatus(String status) {
        devStatusTv.setText(status);
    }

    public void setKeRegTempNum(String num) {
        keRegTempNum.setText(MessageFormat.format("设备中可注册模板的数量:{0}",
                num));
    }

    public void setYiRegTempNum(String num) {
        yiRegTempNum.setText(MessageFormat.format("设备中已注册模板的数量:{0}",
                num));
    }

    public void setDevWorkModel(String devWorkModelStr) {
        devWorkModel.setText(MessageFormat.format("工作模式：{0}", devWorkModelStr));
    }

    public void setInitDevTemplAdapter(List<String> templList) {
        if (templList != null && templList.size() > 0) {
            templAdapter.clearData();
            templAdapter.addData(templList);
        }
        get6CanDevTemplNum();
    }

    public void devWrokModel() {
        tg661JAPI.getDevWorkModel(handler);
    }

    @SuppressLint("HandlerLeak")
    private Handler handler = new Handler() {

        @Override
        public void handleMessage(Message msg) {
            super.handleMessage(msg);
            switch (msg.what) {
                case TG661JAPI.WAIT_DIALOG:
                    int type = msg.arg1;
                    if (type == 0) {
                        String tipStr = (String) msg.obj;
                        waitDialog = AlertDialogUtil.Instance()
                                .showWaitDialog(getActivity(), tipStr);
                    } else if (type == 1) {
                        if (waitDialog != null && waitDialog.isShowing()) {
                            waitDialog.dismiss();
                        }
                    }
                    break;
                case TG661JAPI.SET_DEV_MODEL:
                    /**
                     * 设置设备工作模式
                     * 返回值：setDevModelArg
                     * 0:设备工作模式设置成功
                     * 1:设置失败，该设备不支持6特征模板注册
                     * 2:请先删除设备中的3模板
                     * 3:请先删除设备中的6模板
                     * -1:设备工作模式设置失败
                     * -2:入参错误
                     */
                    int setDevModelArg = msg.arg1;
                    if (setDevModelArg == 0) {
                        tg661JAPI.getDevWorkModel(handler);
                        tipTv.setText("设备工作模式设置成功");
                        consoleTipAdapter.addData("设备工作模式设置成功");
                    } else if (setDevModelArg == 2) {
                        tipTv.setText("设置失败，该设备不支持6特征模板注册");
                        consoleTipAdapter.addData("设置失败，该设备不支持6特征模板注册");
                    } else if (setDevModelArg == 3) {
                        tipTv.setText("请先删除设备中的3模板");
                        consoleTipAdapter.addData("请先删除设备中的3模板");
                    } else if (setDevModelArg == 4) {
                        tipTv.setText("请先删除设备中的6模板");
                        consoleTipAdapter.addData("请先删除设备中的6模板");
                    } else if (setDevModelArg == -1) {
                        tipTv.setText("设置失败");
                        consoleTipAdapter.addData("设置失败");
                    } else if (setDevModelArg == -2) {
                        tipTv.setText("入参错误");
                        consoleTipAdapter.addData("入参错误");
                    }
                    break;
                case TG661JAPI.DEV_WORK_MODEL:
                    /**
                     * 获取工作模式：
                     * 返回值：devWorkModelArg
                     * -1:获取工作模式失败
                     * 0:前比3模板
                     * 1:后比
                     * 2:前比6模板
                     */
                    int devWorkModelArg = msg.arg1;
                    if (devWorkModelArg == 0) {
                        devWorkModel.setText(String.format("设备的工作模式:%s", "前比3模板"));
                    } else if (devWorkModelArg == 1) {
                        devWorkModel.setText(String.format("设备的工作模式:%s", "后比"));
                    } else if (devWorkModelArg == 2) {
                        devWorkModel.setText(String.format("设备的工作模式:%s", "前比6模板"));
                    } else if (devWorkModelArg == -1) {
                        devWorkModel.setText("获取工作模式失败");
                    }
                    break;
                case TG661JAPI.DEV_TEMPL_CLEAR:
                    /**
                     * 清空设备中的模板
                     * 返回值：devClearTemplArg
                     * 0:设备中模板清空成功
                     * 1:设备中模板不存在
                     * -1:设备中模板清空失败
                     */
                    int devClearTemplArg = msg.arg1;
                    if (devClearTemplArg == 0) {
                        //获取设备中得模板列表，更新界面
                        templType = 0;
                        tg661JAPI.getDevTemplNum(handler, templType);
                        tipTv.setText("设备中模板清空成功");
                        consoleTipAdapter.addData("设备中模板清空成功");
                        templAdapter.clearData();
                    } else if (devClearTemplArg == 1) {
                        tipTv.setText("设备中模板不存在");
                        consoleTipAdapter.addData("设备中模板不存在");
                    } else if (devClearTemplArg == -1) {
                        tipTv.setText("设备中模板清空失败");
                        consoleTipAdapter.addData("设备中模板清空失败");
                    }
                    break;
                case TG661JAPI.DEV_VOICE:
                    /**
                     * 调节设备音量
                     * 返回值:
                     * 0: 设备中模板清空失败
                     * -1: 设备音量调节失败
                     */
                    int devVoiceArg = msg.arg1;
                    if (devVoiceArg == 0) {
                        String voiceValue = (String) msg.obj;
                        voiceTv.setText(voiceValue);
                        tipTv.setText("设备音量调节成功");
                    } else if (devVoiceArg == -1) {
                        tipTv.setText("设备音量调节失败");
                    }
                    break;
                case TG661JAPI.DEV_REGISTER:
                    /**
                     * 设备注册：
                     * 返回值：devRegisterArg
                     * 0:登记成功
                     * -1:登记失败
                     */
                    int devRegisterArg = msg.arg1;
                    if (devRegisterArg == -1) {
                        tipTv.setText("登记失败");
                        consoleTipAdapter.addData("登记失败");
                    } else if (devRegisterArg == 0) {
                        //获取设备中得模板列表，更新界面
                        templType = 0;
                        tg661JAPI.getDevTemplNum(handler, templType);
                        tipTv.setText("登记成功");
                        consoleTipAdapter.addData("登记成功");
                    }
                    break;
                case TG661JAPI.CONTINUE_VERIFY:
                    /**
                     * 连续验证:
                     * 返回值:continueVerifyArg
                     * 0:连续验证调用成功
                     * -1:连续验证调用失败
                     */
                    int continueVerifyArg = msg.arg1;
                    if (continueVerifyArg == 0) {
                        consoleTipAdapter.addData("连续验证调用成功");
                    } else if (continueVerifyArg == -1) {
                        consoleTipAdapter.addData("连续验证调用失败");
                    }
                    break;
                case TG661JAPI.DEV_VERIFY_TEMPL:
                    /**
                     * 设备1:N验证模板
                     * 返回值：devVerify1_NArg
                     * -1:调用设备1:N验证接口失败
                     * 0:1:N验证成功
                     * 1:1:N验证失败
                     */
                    //前比1:N验证
                    int devVerify1_NArg = msg.arg1;
                    if (devVerify1_NArg == -1) {
                        tipTv.setText("调用设备1:N验证接口失败");
                        consoleTipAdapter.addData("调用设备1:N验证接口失败");
                    } else if (devVerify1_NArg == 0) {
                        tipTv.setText("1:N验证成功");
                        consoleTipAdapter.addData("1:N验证成功");
                    } else if (devVerify1_NArg == 1) {
                        tipTv.setText("1:N验证失败");
                        consoleTipAdapter.addData("1:N验证失败");
                    }
                    break;
                case TG661JAPI.DEV_VERIFY1_1:
                    /**
                     * 设备1:1验证失败：
                     * 返回值：devVerify1_1Arg
                     * -1:调用设备1:1验证接口失败
                     * 0:1:1验证成功
                     * 1:1:1验证失败
                     * 2:比对的模板不存在
                     */
                    int devVerify1_1Arg = msg.arg1;
                    if (devVerify1_1Arg == -1) {
                        consoleTipAdapter.addData("调用设备1:1验证接口失败");
                    } else if (devVerify1_1Arg == 0) {
                        consoleTipAdapter.addData("1:1验证成功");
                    } else if (devVerify1_1Arg == 1) {
                        consoleTipAdapter.addData("1:1验证失败");
                    } else if (devVerify1_1Arg == 2) {
                        consoleTipAdapter.addData("比对的模板不存在");
                    }
                    break;
                case TG661JAPI.CANCEL_VERIFY:
                    /**
                     * 取消验证或注册：
                     * 返回值：cancelRegisterArg
                     * 0:取消成功
                     * -1:取消失败
                     */
                    int cancelRegisterArg = msg.arg1;
                    if (cancelRegisterArg == 0) {
                        tipTv.setText("取消成功");
                    } else if (cancelRegisterArg == -1) {
                        tipTv.setText("取消失败");
                    }
                    break;
                case TG661JAPI.WRITE_DEV_INFO:
                    /**
                     * 往设备中写入信息
                     * 返回值:writeDevInfoArg
                     * 0:设备写入信息成功
                     * -1:设备写入数据超时
                     * -2:入参错误
                     */
                    int writeDevInfoArg = msg.arg1;
                    if (writeDevInfoArg == 0) {
                        tipTv.setText("设备写入信息成功");
                        consoleTipAdapter.addData("设备写入信息成功");
                    } else if (writeDevInfoArg == -1) {
                        tipTv.setText("设备写入数据超时");
                        consoleTipAdapter.addData("设备写入数据超时");
                    } else if (writeDevInfoArg == -2) {
                        tipTv.setText("入参错误");
                        consoleTipAdapter.addData("入参错误");
                    }
                    break;
                case TG661JAPI.READ_DEV_INFO:
                    /**
                     * 读取设备信息
                     * 返回值:readDevInfoArg
                     * 0:设备读取信息成功
                     * -1:设备写入数据超时
                     * -2:入参错误
                     */
                    int readDevInfoArg = msg.arg1;
                    if (readDevInfoArg == 0) {
                        String devInfo = (String) msg.obj;//设备信息
                        if (TextUtils.isEmpty(devInfo)) {
                            ToastUtil.toast(getActivity(), "暂无设备信息");
                        } else {
                            AlertDialogUtil.Instance().showResultDialog(getActivity(),
                                    devInfo, true);
                        }
                        tipTv.setText(MessageFormat.format("设备信息:{0}", devInfo));
                        consoleTipAdapter.addData("设备读取信息成功");
                    } else if (readDevInfoArg == -1) {
                        tipTv.setText("设备写入数据超时");
                        consoleTipAdapter.addData("设备读取数据超时");
                    } else if (readDevInfoArg == -2) {
                        tipTv.setText("设备写入数据超时");
                        consoleTipAdapter.addData("入参错误");
                    }
                    break;
                case TG661JAPI.UP_TEMPL_HOST:
                    /**
                     * 上传的那个模板到主机
                     * 返回值：upTemplHostArg
                     * -1:上传超时
                     * 0:上传主机成功，写入主机成功
                     * 1:设备中不存在待上传的模板
                     * 2:上传主机失败
                     */
                    int upTemplHostArg = msg.arg1;
                    if (upTemplHostArg == -1) {
                        tipTv.setText("上传超时");
                        consoleTipAdapter.addData("上传超时");
                    } else if (upTemplHostArg == 0) {
                        tipTv.setText("上传主机成功，写入主机成功");
                        consoleTipAdapter.addData("上传主机成功，写入主机成功");
                        boolean writeFile = (boolean) msg.obj;
                        if (writeFile) {
                            ToastUtil.toast(getActivity(), "模板上传到主机成功");
                            //这里明确了是前比
                            getAimDirListToAdapter();
                        }
                    } else if (upTemplHostArg == 1) {
                        tipTv.setText("设备中不存在待上传的模板");
                        consoleTipAdapter.addData("设备中不存在待上传的模板");
                    } else if (upTemplHostArg == 2) {
                        tipTv.setText("上传主机失败");
                        consoleTipAdapter.addData("上传主机失败");
                    }
                    break;
                case TG661JAPI.DOWN_TEMPL_DEV:
                    /**
                     * 下载单个模板到设备:
                     * 返回值:downTemplArg
                     * 0:设备下载模板成功
                     * -1:设备下载模板超时
                     * -2:模板错误
                     * -3:设备可容纳模板数已满
                     */
                    int downTemplArg = msg.arg1;
                    if (downTemplArg == 0) {
                        //获取设备中得模板列表，更新界面
                        templType = 0;
                        tg661JAPI.getDevTemplNum(handler, templType);
                        tipTv.setText("设备下载模板成功");
                        consoleTipAdapter.addData("设备下载模板成功");
                    } else if (downTemplArg == -1) {
                        tipTv.setText("设备下载模板超时");
                        consoleTipAdapter.addData("设备下载模板超时");
                    } else if (downTemplArg == -2) {
                        tipTv.setText("模板错误");
                        consoleTipAdapter.addData("模板错误");
                    } else if (downTemplArg == -3) {
                        tipTv.setText("设备可容纳模板数已满");
                        consoleTipAdapter.addData("设备可容纳模板数已满");
                    }
                    break;
                case TG661JAPI.UP_TEMPL_PAC_HOST:
                    /**
                     * 上传模板包到主机:
                     * 返回值:upTemplPacArg
                     * -1:获取模板数量超时
                     * 0:上传模板包到主机成功
                     * 1:设备中不存在模板
                     * 2:设备上传模板包超时
                     */
                    int upTemplPacArg = msg.arg1;
                    if (upTemplPacArg == -1) {
                        tipTv.setText("获取模板数量超时");
                        consoleTipAdapter.addData("获取模板数量超时");
                    } else if (upTemplPacArg == 0) {
                        tipTv.setText("上传模板包到主机成功");
                        consoleTipAdapter.addData("上传模板包到主机成功");
                        if (templType == TG661JAPI.TEMPL_MODEL_3) {
                            String frontHost3TemplPath = tg661JAPI.getFrontHost3TemplPath();
                            tg661JAPI.scanAimDirFileName(frontHost3TemplPath);
                        } else if (templType == TG661JAPI.TEMPL_MODEL_6) {
                            String frontHost6TemplPath = tg661JAPI.getFrontHost6TemplPath();
                            tg661JAPI.scanAimDirFileName(frontHost6TemplPath);
                        }
                    } else if (upTemplPacArg == 1) {
                        tipTv.setText("设备中不存在模板");
                        consoleTipAdapter.addData("设备中不存在模板");
                    } else if (upTemplPacArg == 2) {
                        tipTv.setText("设备上传模板包超时");
                        consoleTipAdapter.addData("设备上传模板包超时");
                    }
                    break;
                case TG661JAPI.DOWN_TEMPL_PAC_DEV:
                    /**
                     * 下载模板包到设备:
                     * 返回值:downTemplPagArg
                     * 0:设备下载模板包成功
                     * -1:设备下载模板包超时
                     * -2:带下载的模板包错误
                     * -3:设备中可存储的指静脉模板已满
                     */
                    int downTemplPagArg = msg.arg1;
                    if (downTemplPagArg == 0) {
                        templType = 0;
                        tg661JAPI.getDevTemplNum(handler, templType);
                        tipTv.setText("设备下载模板包成功");
                        consoleTipAdapter.addData("设备下载模板包成功");
                    } else if (downTemplPagArg == -1) {
                        tipTv.setText("设备下载模板包超时");
                        consoleTipAdapter.addData("设备下载模板包超时");
                    } else if (downTemplPagArg == -2) {
                        tipTv.setText("带下载的模板包错误");
                        consoleTipAdapter.addData("带下载的模板包错误");
                    } else if (downTemplPagArg == -3) {
                        tipTv.setText("设备中可存储的指静脉模板已满");
                        consoleTipAdapter.addData("设备中可存储的指静脉模板已满");
                    }
                    break;
                case TG661JAPI.DEV_TEMPL_NUM:
                    /**
                     * 获取设备中模板的数量：
                     * 返回值:devTemplNumArg
                     * -1:获取设备模板数接口超时
                     * >=0:设备已注册的模板/或设备中可注册的最大模板数量
                     */
                    int devTemplNumArg = msg.arg1;
                    if (devTemplNumArg >= 0) {
                        devTempls = ((int) msg.obj);
                        if (devTempls == 0) {
                            if (TextUtils.isEmpty(keRegTempNum.getText().toString())) {
                                get6CanDevTemplNum();
                            }
                            //设备中模板的数量
                            yiRegTempNum.setText(MessageFormat.format("设备中已注册模板的数量:{0}",
                                    devTemplNumArg));
                            consoleTipAdapter.addData(MessageFormat.format("设备中已注册模板数量:{0}",
                                    devTemplNumArg));
                        } else if (devTempls == 1) {
                            if (TextUtils.isEmpty(yiRegTempNum.getText().toString())) {
                                devTempls = 0;
                                tg661JAPI.getDevTemplNum(handler, devTempls);
                            }
                            //设备中模板的数量
                            keRegTempNum.setText(MessageFormat.format("设备中可注册最大模板数量:{0}",
                                    devTemplNumArg));
                            consoleTipAdapter.addData(MessageFormat.format("设备中可注册最大模板数量:{0}",
                                    devTemplNumArg));
                        }
                    } else {
                        tipTv.setText("获取设备模板数接口超时");
                        consoleTipAdapter.addData("获取设备模板数接口超时");
                    }
                    break;
                case TG661JAPI.DEV_TEMPL_LIST:
                    /**
                     * 获取设备中的模板名称列表：
                     * 返回值:devTemplListArg
                     * -1:调用获取设备端模板信息列表接口超时
                     * 0:获取成功
                     * 1:获取设备端模板信息列表数量为:0
                     */
                    int devTemplListArg = msg.arg1;
                    if (devTemplListArg == 1) {
                        ArrayList<String> templList = (ArrayList<String>) msg.obj;
                        if (templList != null && templList.size() > 0) {
                            templAdapter.clearData();
                            templAdapter.addData(templList);
                        }
                    } else if (devTemplListArg == 2) {
                        tipTv.setText("获取设备端模板信息列表数量为:0");
                        consoleTipAdapter.addData("获取设备端模板信息列表数量为:0");
                    } else if (devTemplListArg == -1) {
                        tipTv.setText("调用获取设备端模板信息列表接口超时");
                        consoleTipAdapter.addData("调用获取设备端模板信息列表接口超时");
                    }
                    break;
                case TG661JAPI.DEV_DEL_ID_TEMPL:
                    /**
                     * 从设备中删除指定ID的模板:
                     * 返回值:delDevIdTemplArg
                     * -1:删除模板超时
                     * 0:模板从设备中删除成功
                     * 1:设备中不存在该模板
                     */
                    int delDevIdTemplArg = msg.arg1;
                    if (delDevIdTemplArg == -1) {
                        tipTv.setText("删除模板超时");
                        consoleTipAdapter.addData("删除模板超时");
                    } else if (delDevIdTemplArg == 0) {
                        //获取设备中得模板列表，更新界面
                        templType = 0;
                        tg661JAPI.getDevTemplNum(handler, templType);
                        tipTv.setText("模板从设备中删除成功");
                        consoleTipAdapter.addData("模板从设备中删除成功");
                    } else if (delDevIdTemplArg == 1) {
                        tipTv.setText("设备中不存在该模板");
                        consoleTipAdapter.addData("设备中不存在该模板");
                    }
                    break;
                case TG661JAPI.DELETE_HOST_ALL_TEMPL:
                    /**
                     * 删除主机中的所有模板:
                     * 返回值:deleteHostTemplArg
                     * -1:删除失败
                     * 0:删除成功
                     */
                    int deleteHostTemplArg = msg.arg1;
                    if (deleteHostTemplArg == 0) {
                        ArrayList<String> hostFileList = tg661JAPI.getAimFileList();
                        hostTemplAdapter.clearData();
                        hostTemplAdapter.addData(hostFileList);
                        tipTv.setText("删除成功");
                        LogUtils.d("删除成功");
                    } else if (deleteHostTemplArg == -1) {
                        tipTv.setText("删除失败");
                        LogUtils.d("删除失败");
                    }
            }
        }
    };

    private TextView keRegTempNum;
    private TextView yiRegTempNum;
    private TextView devFW;
    private TextView devSN;
    private TextView devStatusTv;
    private TextView devWorkModel;
    private TextView tipTv;
    private boolean devOpen;
    private EditText userNameEt;
    private ConsoleTipAdapter consoleTipAdapter;
    private TemplAdapter templAdapter;
    private HostTemplAdapter hostTemplAdapter;
    private CheckBox openContinueVerifyCb;
    private TextView voiceTv;

    //3模板还是6模板
    private int templType = TG661JAPI.TEMPL_MODEL_3;//默认3模板

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_front_frags, container, false);

        Button clearAllDevBtn = view.findViewById(R.id.clearAllDevBtn);
        Button registerBtn = view.findViewById(R.id.registerBtn);
        Button verifyBtn = view.findViewById(R.id.verifyBtn);
        Button upTemplPacBtn = view.findViewById(R.id.upTemplPacBtn);
        Button downTemplPacBtn = view.findViewById(R.id.downTemplPacBtn);
        Button getDevInfo = view.findViewById(R.id.getDevInfo);
        Button setDevInfo = view.findViewById(R.id.setDevInfo);
        Button VerifyBtn1_1 = view.findViewById(R.id.VerifyBtn1_1);
        RadioGroup RG = view.findViewById(R.id.RG);
        final RadioButton rb3 = view.findViewById(R.id.rb3);
        final RadioButton rb6 = view.findViewById(R.id.rb6);
        Button addVoice = view.findViewById(R.id.addVoice);
        Button loseVoice = view.findViewById(R.id.loseVoice);
        Button clearAllHostBtn = view.findViewById(R.id.clearAllHostBtn);
        openContinueVerifyCb = view.findViewById(R.id.openContinueVerifyCb);
        Button cancelVerifyBtn = view.findViewById(R.id.cancelVerifyBtn);
        ImageView consoleClearIcon = view.findViewById(R.id.consoleClearIcon);
        userNameEt = view.findViewById(R.id.UserNameEt);
        voiceTv = view.findViewById(R.id.voiceValue);
        keRegTempNum = view.findViewById(R.id.keRegTempNum);
        yiRegTempNum = view.findViewById(R.id.yiRegTempNum);
        devFW = view.findViewById(R.id.devFW);
        devStatusTv = view.findViewById(R.id.devStatusTv);
        devSN = view.findViewById(R.id.devSN);
        devWorkModel = view.findViewById(R.id.devWorkModel);
        tipTv = view.findViewById(R.id.tipTv);
        RecyclerView datFileRv = view.findViewById(R.id.datFileRv);
        RecyclerView consoleRv = view.findViewById(R.id.consoleRv);
        RecyclerView hostDatFileRv = view.findViewById(R.id.hostDatFileRv);

        //默认的初始化音量是4
        voiceTv.setText("4");

        //日志
        consoleRv.setLayoutManager(new LinearLayoutManager(getActivity(),
                OrientationHelper.VERTICAL, false));
        consoleTipAdapter = new ConsoleTipAdapter(getActivity());
        consoleRv.setAdapter(consoleTipAdapter);
        //设备模板
        datFileRv.setLayoutManager(new LinearLayoutManager(getActivity(),
                OrientationHelper.VERTICAL, false));
        templAdapter = new TemplAdapter(getActivity());
        templAdapter.setItemClick(this);
        datFileRv.setAdapter(templAdapter);
        //主机设备模板
        hostDatFileRv.setLayoutManager(new LinearLayoutManager(getActivity(),
                OrientationHelper.VERTICAL, false));
        hostTemplAdapter = new HostTemplAdapter(getActivity());
        hostTemplAdapter.setItemClick(this);
        hostDatFileRv.setAdapter(hostTemplAdapter);

        openContinueVerifyCb.setOnCheckedChangeListener(this);
        clearAllDevBtn.setOnClickListener(this);
        registerBtn.setOnClickListener(this);
        verifyBtn.setOnClickListener(this);
        consoleClearIcon.setOnClickListener(this);
        upTemplPacBtn.setOnClickListener(this);
        downTemplPacBtn.setOnClickListener(this);
        addVoice.setOnClickListener(this);
        loseVoice.setOnClickListener(this);
        cancelVerifyBtn.setOnClickListener(this);
        getDevInfo.setOnClickListener(this);
        setDevInfo.setOnClickListener(this);
        VerifyBtn1_1.setOnClickListener(this);
        clearAllHostBtn.setOnClickListener(this);

        rb3.setChecked(true);
        RG.setOnCheckedChangeListener(new RadioGroup.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(RadioGroup radioGroup, int i) {
                if (i == rb3.getId()) {
                    templType = TG661JAPI.TEMPL_MODEL_3;
                    tg661JAPI.setDevWorkModel(handler, workType, templType);
                    getAimDirListToAdapter();
                } else if (i == rb6.getId()) {
                    templType = TG661JAPI.TEMPL_MODEL_6;
                    tg661JAPI.setDevWorkModel(handler, workType, templType);
                    getAimDirListToAdapter();
                }
                get6CanDevTemplNum();
            }
        });
        return view;
    }

    //获取6特征模板状态下得设备可注册得模板数量
    private void get6CanDevTemplNum() {
        try {
            Thread.sleep(1000);
            tg661JAPI.getDevTemplNum(handler, 1);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }

    private int workType = TG661JAPI.WORK_FRONT;//设备工作模式--》前比

    @Override
    public void onClick(View view) {
        switch (view.getId()) {
            case R.id.clearAllDevBtn:
                devStatus = checkDevStatus();
                if (devStatus)
                    tg661JAPI.clearDevTempl(handler);
                break;
            case R.id.registerBtn:
                devStatus = checkDevStatus();
                if (!devStatus) {
                    ToastUtil.toast(getActivity(), "请先开启设备");
                } else {
                    String userN = userNameEt.getText().toString().trim();
                    if (TextUtils.isEmpty(userN)) {
                        consoleTipAdapter.addData("请填写模板注册ID");
                        tipTv.setText(R.string.user_id_tip);
                    } else {
                        //检测注册名只包含字母或数字或中文
                        boolean b = RegularUtil.strContainsNumOrAlpOrChin(userN);
                        if (b) {
                            byte[] userNBytes = userN.getBytes();
                            if (userNameEt.length() > 49) {
                                ToastUtil.toast(getActivity(),
                                        "注册模板的名字太长，请重新填写待注册模板的名字");
                                userNameEt.getText().clear();
                            } else {
                                tg661JAPI.registerDev(handler, userNBytes);
                            }
                        } else {
                            ToastUtil.toast(getActivity(), "注册的模板名称不可包含数字/字母/中文以外的字符");
                        }
                    }
                }
                break;
            case R.id.verifyBtn:
                devStatus = checkDevStatus();
                if (devStatus)
                    tg661JAPI.devModelVerify(handler);
                break;
            case R.id.consoleClearIcon:
                consoleTipAdapter.clearData();
                break;
            case R.id.upTemplPacBtn:
                devStatus = checkDevStatus();
                if (devStatus)
                    AlertDialogUtil.Instance().showActDialog(getActivity(),
                            "确定要从设备上传模板包到主机吗?", this, 3);
                break;
            case R.id.downTemplPacBtn:
                devStatus = checkDevStatus();
                if (devStatus)
                    AlertDialogUtil.Instance().showActDialog(getActivity(),
                            "确定要从主机下载模板包到设备吗?", this, 4);
                break;
            case R.id.cancelVerifyBtn:
                devStatus = checkDevStatus();
                if (devStatus)
                    tg661JAPI.cancelVerify(handler);
                break;
            case R.id.addVoice:
                devStatus = checkDevStatus();
                if (devStatus)
                    tg661JAPI.setDevVoice(handler, 1);
                break;
            case R.id.loseVoice:
                devStatus = checkDevStatus();
                if (devStatus)
                    tg661JAPI.setDevVoice(handler, 2);
                break;
            case R.id.getDevInfo:
                checkDevStatus();
                tg661JAPI.readDevInfo(handler);
                break;
            case R.id.setDevInfo:
                devStatus = checkDevStatus();
                if (devStatus)
                    AlertDialogUtil.Instance().showGetTipDialog(getActivity(),
                            this, 1);
                break;
            case R.id.VerifyBtn1_1:
                devStatus = checkDevStatus();
                if (devStatus) {
                    String userN = userNameEt.getText().toString().trim();
                    if (TextUtils.isEmpty(userN)) {
                        consoleTipAdapter.addData("请填写模板注册ID");
                        tipTv.setText(R.string.user_id_tip);
                    } else {
                        byte[] userNBytes = userN.getBytes();
                        if (userNameEt.length() > 49) {
                            ToastUtil.toast(getActivity(),
                                    "注册模板的名字太长，请重新填写待注册模板的名字");
                            userNameEt.getText().clear();
                        } else {
                            tg661JAPI.verifyDev1_1(handler, userNBytes);
                        }
                    }
                }
                break;
            case R.id.clearAllHostBtn:
                tg661JAPI.deleteHostAllTempl(handler);
                break;

        }
    }

    private boolean checkDevStatus() {
        devOpen = tg661JAPI.isDevOpen();
        if (!devOpen) {
            ToastUtil.toast(getActivity(), "请先开启设备");
            return false;
        } else {
            return true;
        }
    }

    public void consoleAddData(String tip) {
        tipTv.setText(tip);
        consoleTipAdapter.addData(tip);
    }

    //扫描主机目标文件夹更新adapter得数据
    public void getAimDirListToAdapter() {
        ArrayList<String> hostDatFileNameList = tg661JAPI.scanHostAimDir();
        hostTemplAdapter.clearData();
        hostTemplAdapter.addData(hostDatFileNameList);
    }

    private String datFileName;

    @Override
    public void itemSelectFile(String datFileName) {
        this.datFileName = datFileName;
        AlertDialogUtil.Instance().showActDialog(getActivity(),
                "确定要上传该模板到主机吗?", this, 1);
    }

    @Override
    public void delTempl(String datFileName) {
        if (!TextUtils.isEmpty(datFileName)) {
            String templId = datFileName.substring(0, datFileName.indexOf(".dat"));
            tg661JAPI.delIDTemplDev(handler, templId.getBytes(), templType);
            templAdapter.removeData(datFileName);
        }
    }

    @Override
    public void hostItemSelectFile(String datFileName) {
        this.datFileName = datFileName;
        AlertDialogUtil.Instance().showActDialog(getActivity(),
                "确定要下载该模板到设备吗?", this, 2);
    }

    @Override
    public void hostDelTempl(String datFileName) {
        String datDirPath = "";
        if (templType == TG661JAPI.TEMPL_MODEL_3) {
            datDirPath = tg661JAPI.getFrontHost3TemplPath();
        } else if (templType == TG661JAPI.TEMPL_MODEL_6) {
            datDirPath = tg661JAPI.getFrontHost6TemplPath();
        }
        boolean removeFile = FileUtil.insance().removeFile(datDirPath, datFileName);
        if (removeFile)
            hostTemplAdapter.removeData(datFileName);
    }

    @Override
    public void actListener(int flag) {
        switch (flag) {
            case 1:
                if (TextUtils.isEmpty(datFileName)) {
                    ToastUtil.toast(getActivity(), "所选待上传模板的ID为空，请检查");
                } else {
                    tg661JAPI.upTemplHost(handler, datFileName, templType);
                }
                break;
            case 2:
                if (TextUtils.isEmpty(datFileName)) {
                    ToastUtil.toast(getActivity(), "所选待上传模板的ID为空，请检查");
                } else {
                    tg661JAPI.downTemplDev(handler, datFileName, templType);
                }
                break;
            case 3:
                tg661JAPI.upTemplPacHost(handler, templType);
                break;
            case 4:
                tg661JAPI.downTemplPacDev(handler, templType);
                break;
        }
    }

    @Override
    public void onCheckedChanged(CompoundButton compoundButton, boolean b) {
        devOpen = tg661JAPI.isDevOpen();
        if (!devOpen) {
            openContinueVerifyCb.setChecked(false);
            ToastUtil.toast(getActivity(), "请先开启设备");
            return;
        }
        if (b) {
            LogUtils.d("选中");
            tg661JAPI.continueVerify(handler, 0);
        } else {
            LogUtils.d("取消选中");
            tg661JAPI.cancelVerify(handler);
        }
    }

    @Override
    public void commiteInfo(String info, int flag) {
        switch (flag) {
            case 1:
                byte[] infoBytes = info.getBytes();
                if (infoBytes.length > 1024) {
                    ToastUtil.toast(getActivity(), "输入的设备信息超出长度");
                    tipTv.setText("输入的设备信息超出长度");
                } else {
                    tg661JAPI.writeDevInfo(handler, infoBytes);
                }
                break;
        }
    }

    @Override
    public void showTip(String tip) {
        ToastUtil.toast(getActivity(), tip);
    }
}
