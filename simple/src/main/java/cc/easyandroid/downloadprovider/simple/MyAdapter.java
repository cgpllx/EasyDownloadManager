package cc.easyandroid.downloadprovider.simple;

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

import java.io.File;
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
            convertView = LayoutInflater.from(context).inflate(cc.easyandroid.downloadprovider.R.layout.item, parent, false);
            holder = new ViewHolder(convertView);
            convertView.setTag(holder);
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
//                request.setId()
                request.setDestinationInExternalPublicDir(
                        Environment.DIRECTORY_DOWNLOADS, "/");
                request.setDescription("Just for test");
                EasyDownLoadManager.getInstance(context).getDownloadManager().enqueue(request);
            }
        });
//        String key = getItem(position) + position;
        updataView(position, holder);
        return convertView;
    }

    public void updataView(final int position, final ViewHolder holder) {
        String key = getItem(position) + position;

        if (mDownloadingTask != null && mDownloadingTask.containsKey(key)) {
            EasyDownLoadInfo easyDownLoadInfo = mDownloadingTask.get(key);
            holder.progress_text.setText(easyDownLoadInfo.getStatus() + "下载的大小＝" + easyDownLoadInfo.getCurrentBytes());
            if (DownloadManager.isStatusRunning(easyDownLoadInfo.getStatus())) {//正在下载
            } else if (DownloadManager.isStatusPending(easyDownLoadInfo.getStatus())) {//等待
                // 下载等待中
                holder.button.setTag(Status.status_wait);
            } else if (easyDownLoadInfo.getStatus() == DownloadManager.STATUS_SUCCESSFUL) {// 下载完成
                holder.button.setTag(Status.status_successful);
                holder.button.setText("下载完成");
                // download success
                // 检查文件完整性，如果不存在，删除此条记录
                if (!new File(easyDownLoadInfo.getLocal_uri()).exists()) {
//                    mDownloadingList.remove(infoItem.getUri());
                }
            } else if (easyDownLoadInfo.getStatus() == DownloadManager.STATUS_PAUSED) {// 暂停 //
                holder.button.setTag(Status.status_pause);
            } else {
                System.out.println("下载出错");
                holder.button.setText("下载出错，点击重新下载");
//                holder.button.setTag("error");
                holder.button.setTag(Status.status_error);
                holder.button.setTag(R.id.xxx, easyDownLoadInfo.getId());
            }

        } else {
            holder.button.setTag(Status.status_default);
            holder.progress_text.setText("进度");
        }
        holder.button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                int status = Status.status_default;
                Object objects = holder.button.getTag();
                if (objects != null && objects instanceof Integer) {
                    status = (int) objects;
                }
                switch (status) {
                    case Status.status_default:
                        String url = getItem(position);
                        Uri srcUri = Uri.parse(url);
                        DownloadManager.Request request = new DownloadManager.Request(srcUri);
                        request.setTitle("" + position);
                        request.setDestinationInExternalPublicDir(
                                Environment.DIRECTORY_DOWNLOADS, "/");
                        request.setDescription("Just for test");
                        EasyDownLoadManager.getInstance(context).getDownloadManager().enqueue(request);
                        break;
                    case Status.status_error:
                        Long id = (Long) holder.button.getTag(R.id.xxx);
                        EasyDownLoadManager.getInstance(context).getDownloadManager().restartDownload(id);
                        break;
                    case Status.status_pause:
                        Long id1 = (Long) holder.button.getTag(R.id.xxx);
                        EasyDownLoadManager.getInstance(context).getDownloadManager().resumeDownload(id1);
                        break;
                    case Status.status_wait:
                        break;
                    case Status.status_successful:

                        break;
                }

            }
        });
    }

    public interface Status {
        int status_default = 0;
        int status_error = 1;
        int status_pause = 1 << 2;
        int status_wait = 1 << 3;
        int status_successful = 1 << 4;
    }

    @Override
    public void update(Observable observable, Object data) {
        if (data instanceof HashMap) {
            mDownloadingTask = (HashMap<String, EasyDownLoadInfo>) data;
            notifyItemData();
        }
    }

    IVisiblePosition visiblePosition;

    public void setIVisiblePosition(IVisiblePosition visiblePosition) {
        this.visiblePosition = visiblePosition;
    }

    public void notifyItemData() {
        if (visiblePosition == null) {
            return;
        }
        int firstVisiblePosition = visiblePosition.getFirstVisiblePosition();
        int lastVisiblePosition = visiblePosition.getLastVisiblePosition();
        for (int i = firstVisiblePosition; i <= lastVisiblePosition; i++) {
            View view = visiblePosition.getVisibleView(i);
            if (view != null) {
                Object object = view.getTag();
                if (object != null && object instanceof ViewHolder) {
                    ViewHolder holder = (ViewHolder) object;
                    updataView(i, holder);
                }
            }
        }
    }


    class ViewHolder {
        public ViewHolder(View view) {
            button = (Button) view.findViewById(cc.easyandroid.downloadprovider.R.id.button);
            progress_text = (TextView) view.findViewById(cc.easyandroid.downloadprovider.R.id.progress_text);
            progressBar = (ProgressBar) view.findViewById(cc.easyandroid.downloadprovider.R.id.progressBar);
        }

        Button button;
        TextView progress_text;
        ProgressBar progressBar;
    }
}
