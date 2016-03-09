package cc.easyandroid.downloadprovider;

import android.app.Activity;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.support.v7.app.AppCompatActivity;
import android.widget.ListView;

import java.util.ArrayList;
import java.util.List;

import cc.easyandroid.providers.DownloadManager;
import cc.easyandroid.providers.core.EasyDownLoadManager;

public class MainActivity extends Activity {
    ListView listView;
    MyAdapter adapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        EasyDownLoadManager.open(this);
        listView = (ListView) findViewById(R.id.listview);
        adapter = new MyAdapter(this);
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
        adapter.setUrls(urls);

    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        EasyDownLoadManager.open(this).close();
    }
}
