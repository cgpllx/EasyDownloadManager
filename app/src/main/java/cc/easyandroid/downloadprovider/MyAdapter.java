package cc.easyandroid.downloadprovider;

import android.content.Context;
import android.net.Uri;
import android.os.Environment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.ProgressBar;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Observable;
import java.util.Observer;

import cc.easyandroid.providers.DownloadManager;
import cc.easyandroid.providers.core.EasyDownLoadInfo;
import cc.easyandroid.providers.core.EasyDownLoadManager;

/**
 * Created by chenguoping on 16/3/8.
 */
public class MyAdapter extends BaseAdapter implements Observer {
    private HashMap<String, EasyDownLoadInfo> mDownloadingTask;

    public MyAdapter(Context context) {
        this.context = context;
        EasyDownLoadManager.getInstance(context).addObserver(this);
//EasyDownLoadManager.open(context).r
    }

    Context context;
    List<String> urls = new ArrayList<String>();

    public void setUrls(List<String> urls) {
        this.urls = urls;
        notifyDataSetChanged();
    }

    @Override
    public int getCount() {
        return urls.size();
    }

    @Override
    public String getItem(int position) {
        return urls.get(position);
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    public View getView(final int position, View convertView, ViewGroup parent) {
        ViewHolder holder;
        if (convertView == null || convertView.getTag() == null) {
            convertView = LayoutInflater.from(context).inflate(R.layout.item, parent, false);
            holder = new ViewHolder(convertView);
        } else {
            holder = (ViewHolder) convertView.getTag();
        }
        holder.button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                String url = getItem(position);
                Uri srcUri = Uri.parse(url);
                DownloadManager.Request request = new DownloadManager.Request(srcUri);
                request.setTitle("" + position);
                request.setDestinationInExternalPublicDir(
                        Environment.DIRECTORY_DOWNLOADS, "/");
                request.setDescription("Just for test");
//                request.setId(2);
//                android.widget.CursorAdapter cursorAdapter;
                EasyDownLoadManager.getInstance(context).getDownloadManager().enqueue(request);
            }
        });
        if (mDownloadingTask != null && mDownloadingTask.containsKey(getItem(position) + position)) {
            EasyDownLoadInfo easyDownLoadInfo = mDownloadingTask.get(getItem(position) + position);
            System.out.println("下载的大小＝" + easyDownLoadInfo.getCurrentBytes());
            holder.progress_text.setText("下载的大小＝" + easyDownLoadInfo.getCurrentBytes());
        }
        return convertView;
    }

    @Override
    public void update(Observable observable, Object data) {
        if (data instanceof HashMap) {
            mDownloadingTask = (HashMap<String, EasyDownLoadInfo>) data;
            System.out.println("收到消息" + data);
            notifyDataSetChanged();
        }
    }

    class ViewHolder {
        public ViewHolder(View view) {
            button = (Button) view.findViewById(R.id.button);
            progress_text = (TextView) view.findViewById(R.id.progress_text);
            progressBar = (ProgressBar) view.findViewById(R.id.progressBar);
        }

        Button button;
        TextView progress_text;
        ProgressBar progressBar;
    }
}
