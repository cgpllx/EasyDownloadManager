package cc.easyandroid.downloadprovider.simple;

import android.app.Activity;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.ListView;

import java.util.ArrayList;
import java.util.List;

import cc.easyandroid.providers.core.EasyDownLoadManager;

public class MainActivity extends  Activity {
    ListView listView;
    MyAdapter adapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(cc.easyandroid.downloadprovider.R.layout.activity_main);
        listView = (ListView) findViewById(cc.easyandroid.downloadprovider.R.id.listview);
        adapter = new MyAdapter(this);
        EasyDownLoadManager.getInstance(this).addObserver(adapter);
        listView.setAdapter(adapter);
        List<String> urls = new ArrayList<>();
        urls.add("http://down.mumayi.com/41052/mbaidu");
        urls.add("http://down.mumayi.com/41052/mbaidu");
        urls.add("http://down.mumayi.com/41052/mbaidu");
        urls.add("http://down.mumayi.com/41052/mbaidu");
        urls.add("http://down.mumayi.com/41052/mbaidu");
        urls.add("http://down.mumayi.com/41052/mbaidu");
        urls.add("http://down.mumayi.com/41052/mbaidu");
        urls.add("http://down.mumayi.com/41052/mbaidu");
        urls.add("http://down.mumayi.com/41052/mbaidu");
        urls.add("http://down.mumayi.com/41052/mbaidu");
        urls.add("http://down.mumayi.com/41052/mbaidu");
        urls.add("http://down.mumayi.com/41052/mbaidu");
        urls.add("http://down.mumayi.com/41052/mbaidu");
        urls.add("http://down.mumayi.com/41052/mbaidu");
        urls.add("http://down.mumayi.com/41052/mbaidu");
        urls.add("http://down.mumayi.com/41052/mbaidu");
        urls.add("http://down.mumayi.com/41052/mbaidu");
        urls.add("http://down.mumayi.com/41052/mbaidu");
        urls.add("http://down.mumayi.com/41052/mbaidu");
        urls.add("http://down.mumayi.com/41052/mbaidu");
        urls.add("http://down.mumayi.com/41052/mbaidu");
        urls.add("http://down.mumayi.com/41052/mbaidu");
        urls.add("http://down.mumayi.com/41052/mbaidu");
        urls.add("http://down.mumayi.com/41052/mbaidu");
        urls.add("http://down.mumayi.com/41052/mbaidu");
        urls.add("http://down.mumayi.com/41052/mbaidu");
        urls.add("http://down.mumayi.com/41052/mbaidu");
        urls.add("http://down.mumayi.com/41052/mbaidu");
        urls.add("http://down.mumayi.com/41052/mbaidu");
        urls.add("http://down.mumayi.com/41052/mbaidu");
        urls.add("http://down.mumayi.com/41052/mbaidu");
        urls.add("http://down.mumayi.com/41052/mbaidu");
        urls.add("http://down.mumayi.com/41052/mbaidu");
        urls.add("http://down.mumayi.com/41052/mbaidu");
        urls.add("http://down.mumayi.com/41052/mbaidu");
        urls.add("http://down.mumayi.com/41052/mbaidu");
        urls.add("http://down.mumayi.com/41052/mbaidu");
        urls.add("http://down.mumayi.com/41052/mbaidu");
        urls.add("http://down.mumayi.com/41052/mbaidu");
        urls.add("http://down.mumayi.com/41052/mbaidu");
        urls.add("http://down.mumayi.com/41052/mbaidu");
        urls.add("http://down.mumayi.com/41052/mbaidu");
        urls.add("http://down.mumayi.com/41052/mbaidu");
        urls.add("http://down.mumayi.com/41052/mbaidu");
        urls.add("http://down.mumayi.com/41052/mbaidu");
        urls.add("http://down.mumayi.com/41052/mbaidu");
        urls.add("http://down.mumayi.com/41052/mbaidu");
        urls.add("http://down.mumayi.com/41052/mbaidu");
        adapter.setIVisiblePosition(new IVisiblePosition() {
            @Override
            public int getFirstVisiblePosition() {
                return listView.getFirstVisiblePosition();
            }

            @Override
            public int getLastVisiblePosition() {
//                listView.get
                return listView.getLastVisiblePosition();
            }

            @Override
            public View getVisibleView(int position) {
                ;
                return listView.getChildAt((position - getFirstVisiblePosition()) % listView.getChildCount());
            }
        });
        adapter.setUrls(urls);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        EasyDownLoadManager.getInstance(this).deleteObserver(adapter);
        EasyDownLoadManager.destroy();
    }
}
