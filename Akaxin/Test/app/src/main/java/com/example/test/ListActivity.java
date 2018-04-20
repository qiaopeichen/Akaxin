package com.example.test;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.media.Image;
import android.os.Debug;
import android.os.Environment;
import android.os.Handler;
import android.os.Message;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutCompat;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.SimpleAdapter;
import android.widget.TextView;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.List;
import java.util.Map;

public class ListActivity extends AppCompatActivity {

    private ListView lv_test;
    private final int DOWNLOAD_BAIDU = 1;
    private final int LIST_COUNT = 32;
    private final String path = Environment.getExternalStorageDirectory().getPath();
    private volatile boolean conversion = false;
    private ListViewAdapter listViewAdapter;

    private Handler handler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            switch (msg.what) {
                case DOWNLOAD_BAIDU:
                    try {
                        String BAIDU_LOGO_ADDRESS = "https://www.baidu.com/img/bd_logo1.png";
                        downloadPic("baidu.png", BAIDU_LOGO_ADDRESS);
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                    conversion = true;
                    for (int i = 0; i < 32; i = i+2) {
                        updateItem(i);
                        listViewAdapter.notifyDataSetChanged();
                        try {
                            Thread.sleep(1000);
                        } catch (InterruptedException e) {
                            e.printStackTrace();
                        }
                    }
                    break;
                default:
                    break;
            }
        }
    };

    // 单行刷新
    private void updateItem(int position) {
        /**第一个可见的位置**/
        int firstVisiblePosition = lv_test.getFirstVisiblePosition();
        /**最后一个可见的位置**/
        int lastVisiblePosition = lv_test.getLastVisiblePosition();

        /**在看见范围内才更新，不可见的滑动后自动会调用getView方法更新**/
        if (position >= firstVisiblePosition && position <= lastVisiblePosition) {
            /**获取指定位置view对象**/
            View view = lv_test.getChildAt(position - firstVisiblePosition);
            ImageView item_iv = (ImageView) view.findViewById(R.id.item_iv);
            TextView item_tv = (TextView) view.findViewById(R.id.item_tv);
            item_tv.setVisibility(View.GONE);
            FrameLayout frameLayout = (FrameLayout) view.findViewById(R.id.item_layout);
            Bitmap bit = BitmapFactory.decodeFile(path + "/baidu.png");
            item_iv.setImageBitmap(bit);
            ListView.LayoutParams layoutParams = new ListView.LayoutParams(ListView.LayoutParams.MATCH_PARENT, position + 16);
            frameLayout.setLayoutParams(layoutParams);
            item_iv.setVisibility(View.VISIBLE);
        }
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_list);
        initView();
        initData();
    }

    // 初始化视图
    private void initView() {
        lv_test = (ListView) findViewById(R.id.lv_test);
    }

    // 初始化数据
    private void initData() {
        listViewAdapter = new ListViewAdapter(getApplicationContext(), null, R.layout.item, null, null);
        lv_test.setAdapter(listViewAdapter);
    }

    class ListViewAdapter extends SimpleAdapter {

        public ListViewAdapter(Context context, List<? extends Map<String, ?>> data, int resource, String[] from, int[] to) {
            super(context, data, resource, from, to);
            handler.postDelayed(new Runnable() {
                @Override
                public void run() {
                    Message message = Message.obtain();
                    message.what = DOWNLOAD_BAIDU;
                    handler.sendMessage(message);
                }
            }, 3000);
        }

        @Override
        public int getCount() {
            return LIST_COUNT;
        }

        @Override
        public long getItemId(int position) {
            return position;
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            ViewHolder viewHolder = null;
            if (convertView == null) {
                convertView = getLayoutInflater().inflate(R.layout.item, null);
                viewHolder = new ViewHolder();

                viewHolder.imageView = (ImageView) convertView.findViewById(R.id.item_iv);
                viewHolder.textView = (TextView) convertView.findViewById(R.id.item_tv);
                viewHolder.frameLayout = (FrameLayout) convertView.findViewById(R.id.item_layout);
                convertView.setTag(viewHolder);
            } else {
                viewHolder = (ViewHolder) convertView.getTag();
            }
            viewHolder.textView.setText(position + "");
            if (position % 2 == 0) {
                viewHolder.textView.setBackgroundColor(Color.YELLOW);
            } else {
//                if (conversion) {
//                    viewHolder.textView.setVisibility(View.INVISIBLE);
//                    Bitmap bit = BitmapFactory.decodeFile(path + "/baidu.png");
//                    viewHolder.imageView.setImageBitmap(bit);
//                    ViewGroup.LayoutParams layoutParams = new FrameLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, position + 16);
//                    viewHolder.frameLayout.setLayoutParams(layoutParams);
//                    viewHolder.imageView.setVisibility(View.VISIBLE);
//                } else {
                    viewHolder.textView.setBackgroundColor(Color.BLUE);
//                }
            }

            return convertView;
        }
    }

    // ViewHolder
    class ViewHolder {
        private TextView textView;
        private ImageView imageView;
        private FrameLayout frameLayout;
    }

    // 下载图片
    private void downloadPic(final String name, final String picurl) {
        new Thread(new Runnable() {
            @Override
            public void run() {
                FileOutputStream fos = null;
                InputStream in = null;
                // 创建文件
                File file = new File(path, name);
                try {
                    fos = new FileOutputStream(file);
                    URL url = new URL(picurl);
                    in = url.openStream();
                    int len = -1;
                    byte[] b = new byte[1024];
                    while ((len = in.read(b)) != -1) {
                        fos.write(b, 0, len);
                    }
                } catch (MalformedURLException e) {
                    e.printStackTrace();
                } catch (IOException e) {
                    e.printStackTrace();
                } finally {
                    try {
                        if (fos != null) {
                            fos.close();
                        }
                        if (in != null) {
                            in.close();
                        }
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }
            }
        }).start();
    }
}
