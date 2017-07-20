package com.userinfosetting;

import android.Manifest;
import android.app.Dialog;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Build;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.RequiresApi;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.yanzhenjie.permission.AndPermission;
import com.yanzhenjie.permission.PermissionListener;

import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.concurrent.TimeUnit;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;
import de.hdodenhof.circleimageview.CircleImageView;
import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.MultipartBody;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;

public class MainActivity extends AppCompatActivity implements BottomDialog.OnBottomMenuItemClickListener {
    @BindView(R.id.nick_name)
    TextView mNickName; //昵称
    @BindView(R.id.head_imag_setting)
    CircleImageView head_imag_setting; //头像

    private MainActivity mContext;
    private BottomDialog bottomDialog;

    //存储
    public static final int REQUEST_CODE_PERMISSION_STORAGE = 100;
    //相机
    public static final int REQUEST_CODE_PERMISSION_CAMERA = 101;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.my_info_fragment);
        ButterKnife.bind(this);
        mContext = this;

        bottomDialog = new BottomDialog(this, R.layout.dialog_bottom_layout, new int[]{R.id.pick_photo_album, R.id.pick_photo_camera, R.id.pick_photo_cancel});
        bottomDialog.setOnBottomMenuItemClickListener(this);

    }

    @Override
    public void onResume() {
        super.onResume();

        showIcon();
    }

    private void showIcon() {
        //显示用户头像
        String icon_path = SpUtil.getString(mContext, GlobalVariables.USER_ICON_FILE_PATH);
        if (icon_path != null && isFileExist(icon_path)) {
            Bitmap bitmap = BitmapFactory.decodeFile(icon_path);
            head_imag_setting.setImageBitmap(bitmap);
        }
    }

    @OnClick({R.id.info_head_imag, R.id.info_username, R.id.info_address, R.id.info_change_pwd, R.id.log_off})
    public void onClick(View view) {
        switch (view.getId()) {
            case R.id.info_head_imag: //头像
                bottomDialog.show();
                break;
            case R.id.info_username: //用户名
                showInputDialog();
                break;
            case R.id.info_address: //收货地址
                break;
            case R.id.info_change_pwd: //修改密码
                break;
            case R.id.log_off: //退出登录
                break;
        }
    }

    public void showInputDialog() {
        final Dialog dialog = new Dialog(mContext, R.style.MyDialog);
        dialog.setCanceledOnTouchOutside(false);
        View view = View.inflate(mContext, R.layout.playing_dialog, null);
        if (dialog.isShowing()) {
            return;
        }
        dialog.setContentView(view);
        dialog.show();

        final EditText et_dialog = (EditText) view.findViewById(R.id.et_dialog);
        Button submit = (Button) view.findViewById(R.id.submit);
        TextView close = (TextView) view.findViewById(R.id.close);

        //确认
        submit.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v) {
                String result = et_dialog.getText().toString().trim();
                SpUtil.putString(mContext, GlobalVariables.USER_NICK_NAME, result);
                mNickName.setText(result);
                dialog.dismiss();
            }
        });
        //关闭
        close.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                dialog.dismiss();
            }
        });
    }

    @Override
    public void onBottomMenuItemClick(BottomDialog dialog, View view) {
        switch (view.getId()) {
            case R.id.pick_photo_album: //从相册选
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
                    checkPermission(REQUEST_CODE_PERMISSION_STORAGE);
                } else {
                    CropImageUtils.getInstance().openAlbum(this);
                }
                break;
            case R.id.pick_photo_camera: //拍照
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
                    checkPermission(REQUEST_CODE_PERMISSION_CAMERA);
                } else {
                    CropImageUtils.getInstance().takePhoto(this);
                }
                break;
        }
    }

    /**
     * 检测权限
     */
    public void checkPermission(int permissionType) {
        if (Build.VERSION.SDK_INT >= 23) {
            switch (permissionType) {
                //调用单个权限
                case REQUEST_CODE_PERMISSION_STORAGE:
                    if (!AndPermission.hasPermission(this, Manifest.permission.READ_EXTERNAL_STORAGE)) {
                        AndPermission.with(this)
                                .requestCode(REQUEST_CODE_PERMISSION_STORAGE)
                                .permission(Manifest.permission.READ_EXTERNAL_STORAGE)
                                .callback(permissionListener)
                                .start();
                    } else {
                        CropImageUtils.getInstance().openAlbum(mContext);
                    }
                    break;
                //调用多个权限，相机和存储(拍照)
                case REQUEST_CODE_PERMISSION_CAMERA:
                    //如果没有申请权限
                    if (!AndPermission.hasPermission(mContext, Manifest.permission.CAMERA, Manifest.permission.WRITE_EXTERNAL_STORAGE)) {
                        AndPermission.with(this)
                                .requestCode(REQUEST_CODE_PERMISSION_CAMERA)
                                .permission(Manifest.permission.CAMERA, Manifest.permission.READ_EXTERNAL_STORAGE)
                                .callback(permissionListener)
                                .start();
                    } else {
                        CropImageUtils.getInstance().takePhoto(mContext);
                    }
                    break;
            }
        }
    }

    /**
     * 回调监听。
     */
    private PermissionListener permissionListener = new PermissionListener() {
        @Override
        public void onSucceed(int requestCode, @NonNull List<String> grantPermissions) {
            switch (requestCode) {
                case REQUEST_CODE_PERMISSION_STORAGE: {
                    CropImageUtils.getInstance().openAlbum(mContext);
                    break;
                }
                case REQUEST_CODE_PERMISSION_CAMERA: {
                    CropImageUtils.getInstance().takePhoto(mContext);
                    break;
                }
            }
        }

        @Override
        public void onFailed(int requestCode, @NonNull List<String> deniedPermissions) {
            switch (requestCode) {
                case REQUEST_CODE_PERMISSION_STORAGE: {
                    Toast.makeText(MainActivity.this, "获取读取sd卡权限失败", Toast.LENGTH_SHORT).show();
                    break;
                }
                case REQUEST_CODE_PERMISSION_CAMERA: {
                    Toast.makeText(MainActivity.this, "获取拍照权限失败", Toast.LENGTH_SHORT).show();
                    break;
                }
            }
        }
    };

    @RequiresApi(api = Build.VERSION_CODES.KITKAT)
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        CropImageUtils.getInstance().onActivityResult(mContext, requestCode, resultCode, data, new CropImageUtils.OnResultListener() {
            @Override
            public void takePhotoFinish(String path) {
                //拍照回调，去裁剪
                CropImageUtils.getInstance().cropPicture(mContext, path);
            }

            @Override
            public void selectPictureFinish(String path) {
                //相册回调，去裁剪
                CropImageUtils.getInstance().cropPicture(mContext, path);
            }

            @Override
            public void cropPictureFinish(String path) {
                LogUtils.d("path", path + " .....");
                //TODO 上传图片
                SpUtil.putString(mContext, GlobalVariables.USER_ICON_FILE_PATH, path);

                //                upload(path);
            }

        });
    }

    /**
     * 上传图片到服务器
     *
     * @param path
     */
    private void upload(String path) {
        HashMap<String, Object> paramsMap = getMapParams(path);
        //TODO 1. 请求路径url
        String request_url = "";
        MultipartBody.Builder builder = new MultipartBody.Builder();
        //设置类型(表单上传)
        builder.setType(MultipartBody.FORM);
        //追加参数
        for (String key : paramsMap.keySet()) {
            Object object = paramsMap.get(key);
            if (!(object instanceof File)) {
                builder.addFormDataPart(key, object.toString());
            } else {
                File file = (File) object;
                builder.addFormDataPart(key, file.getName(), RequestBody.create(null, file));
            }
        }
        //创建RequestBody
        RequestBody body = builder.build();
        //创建Request
        final Request request = new Request.Builder().url(request_url).post(body).build();
        //单独设置参数 比如读取超时时间
        Call call = new OkHttpClient().newBuilder().writeTimeout(50, TimeUnit.SECONDS).build().newCall(request);
        call.enqueue(new Callback() {
            @Override
            public void onFailure(Call call, IOException e) {
                LogUtils.d("json", "上传失败");
            }

            @Override
            public void onResponse(Call call, Response response) throws IOException {
                if (response.isSuccessful()) {
                    String string = response.body().string();
                    LogUtils.d("json", "上传图片成功: " + string);
                } else {
                    LogUtils.d("json", "上传失败");
                }
            }
        });
    }

    //TODO 2. 添加参数
    private HashMap<String, Object> getMapParams(String path) {
        HashMap<String, Object> params = new HashMap<>();
        //todo 这里根据接口自己改变参数
        //        params.put("uid", uid);
        //        params.put("pwd", Encrypt.getMD5Str(pwd));
        //        params.put("bid", Constant.PRODUCT);
        //        params.put("cp", Constant.OS);
        //        params.put("cv", AndroidUtil.getVersionName(this));
        //        params.put("pkgname", this.getPackageName());
        //        params.put("imei", SystemUtil.getImei(this));
        //        params.put("imsi", SystemUtil.getImsi(this));
        //        params.put("netmode", SystemUtil.getNetworkName(this));
        //        params.put("ts", String.valueOf(System.currentTimeMillis() / 1000));
        File file = new File(path);
        if (file != null) {
            params.put("pic", file);
        }
        return params;
    }

    public static boolean isFileExist(String icon_path) {
        File file = new File(icon_path);
        if (file.exists()) {
            return true;
        }
        return false;
    }
}
