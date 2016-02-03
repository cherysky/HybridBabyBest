package com.example.heruijun.hybrid;

import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Bitmap;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.webkit.WebSettings;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.Toast;

import com.github.lzyzsd.jsbridge.BridgeHandler;
import com.github.lzyzsd.jsbridge.BridgeWebView;
import com.github.lzyzsd.jsbridge.CallBackFunction;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import link.fls.SwipeStack;

public class MainActivity extends AppCompatActivity implements SwipeStack.SwipeStackListener {

    private BridgeWebView mWebView;
    private ArrayList<Integer> mData;
    private Integer[] mImageBoyData = {R.drawable.pic1, R.drawable.pic2, R.drawable.pic3};
    private Integer[] mImageGirlData = {R.drawable.pic4, R.drawable.pic5, R.drawable.pic6};
    private SwipeStack mSwipeStack;
    private SwipeStackAdapter mAdapter;
    private static final int CAPTURE_IMAGE_ACTIVITY_REQ = 0;
    private static final int CAMERA_ITEM = -1;
    private Bitmap photo;
    private int selectCount = 0;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        mWebView = (BridgeWebView) findViewById(R.id.main);
        WebSettings settings = mWebView.getSettings();
        settings.setJavaScriptEnabled(true);    // 启用js
        settings.setJavaScriptCanOpenWindowsAutomatically(true);    // js和android交互
        String cacheDirPath = getApplicationContext().getCacheDir().getAbsolutePath();
        settings.setAppCachePath(cacheDirPath);  // 设置缓存的指定路径
        settings.setAllowFileAccess(true);  // 允许访问文件
        settings.setAppCacheEnabled(true);  // 设置h5的缓存打开,默认关闭
        settings.setUseWideViewPort(true);  // 设置webview自适应屏幕大小
        settings.setLayoutAlgorithm(WebSettings.LayoutAlgorithm.NARROW_COLUMNS);    //设置，可能的话使所有列的宽度不超过屏幕宽度
        settings.setLoadWithOverviewMode(true);     // 设置webview自适应屏幕大小
        settings.setDomStorageEnabled(true);    // 设置可以使用localStorage
        settings.setSupportZoom(false);     // 关闭zoom按钮
        settings.setBuiltInZoomControls(false);     // 关闭zoom

        //        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB) {
        //            mWebView.setLayerType(View.LAYER_TYPE_SOFTWARE, null);
        //        }

        mWebView.loadUrl("file:///android_asset/bb/index.html");

        mSwipeStack = (SwipeStack) findViewById(R.id.swipeStack);
        mData = new ArrayList<>();
        mAdapter = new SwipeStackAdapter(mData);
        mSwipeStack.setAdapter(mAdapter);
        mSwipeStack.setListener(this);

        mWebView.registerHandler("sex", new BridgeHandler() {

            @Override
            public void handler(final String data, final CallBackFunction function) {
                // 此处判断选项比较粗暴,只为演示目的
                if (data.contains("-1")) {
                    mSwipeStack.resetStack();
                    fillData(mImageBoyData);
                    return;
                }
                new AlertDialog.Builder(MainActivity.this)
                        .setTitle("提示")
                        .setMessage("确认重新选择上传的照片?")
                        .setPositiveButton("确认", new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int which) {
                                mSwipeStack.resetStack();
                                if (data.contains("男")) {
                                    fillData(mImageBoyData);
                                } else if (data.contains("女")) {
                                    fillData(mImageGirlData);
                                }
                                photo = null;
                                function.onCallBack("选中结果: " + data);
                            }
                        })
                        .setNegativeButton("取消", new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int which) {
                                // do nothing
                            }
                        })
                        .show();
            }

        });

        mWebView.registerHandler("callCamera", new BridgeHandler() {

            @Override
            public void handler(String data, CallBackFunction function) {
                if (photo == null) {
                    Intent cameraIntent = new Intent(android.provider.MediaStore.ACTION_IMAGE_CAPTURE);
                    startActivityForResult(cameraIntent, CAPTURE_IMAGE_ACTIVITY_REQ);
                } else {
                    Toast.makeText(MainActivity.this, "你已经拍摄过照片", Toast.LENGTH_SHORT).show();
                }
            }

        });
    }

    private void fillData(Integer[] data) {
        if (mData.size() > 0) {
            mData.clear();
        }
        for (int i = 0; i < data.length; i++) {
            mData.add(data[i]);
        }
    }

    @Override
    public void onViewSwipedToLeft(int position) {
        // 左滑监听
    }

    @Override
    public void onViewSwipedToRight(int position) {
        // 右滑监听
    }

    private void changeOrder() {
        Collections.rotate(mData, -1);
    }

    @Override
    public void onStackEmpty() {
        // 没有card
        mSwipeStack.resetStack();
    }

    public class SwipeStackAdapter extends BaseAdapter {

        private List<Integer> mData;

        public SwipeStackAdapter(List<Integer> data) {
            this.mData = data;
        }

        @Override
        public int getCount() {
            return mData.size();
        }

        @Override
        public Integer getItem(int position) {
            return mData.get(position);
        }

        @Override
        public long getItemId(int position) {
            return position;
        }

        @Override
        public View getView(final int position, View convertView, ViewGroup parent) {
            final ViewHolder holder;
            if (convertView == null) {
                holder = new ViewHolder();
                convertView = getLayoutInflater().inflate(R.layout.card, parent, false);
                holder.imgView = (ImageView) convertView.findViewById(R.id.pic);
                convertView.setTag(holder);
            } else {
                holder = (ViewHolder) convertView.getTag();
            }

            if (mData.get(position) != CAMERA_ITEM) {
                holder.imgView.setImageResource(mData.get(position));
            } else {
                holder.imgView.setImageBitmap(photo);
            }

            return convertView;
        }

        class ViewHolder {
            ImageView imgView;
        }
    }

    class Bean implements Serializable {
        boolean isChecked;
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == CAPTURE_IMAGE_ACTIVITY_REQ && resultCode == RESULT_OK) {
            photo = (Bitmap) data.getExtras().get("data");
            mData.add(0, CAMERA_ITEM);
            mSwipeStack.resetStack();
        }
    }
}
