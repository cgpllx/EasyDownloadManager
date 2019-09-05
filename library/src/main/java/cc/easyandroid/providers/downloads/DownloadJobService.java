
package cc.easyandroid.providers.downloads;


import android.app.job.JobParameters;
import android.app.job.JobService;
import android.content.ContentUris;
import android.database.ContentObserver;
import android.database.Cursor;
import android.net.Uri;
import android.os.Environment;
import android.os.Process;
import android.util.Log;
import android.util.SparseArray;

import java.io.File;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;

/**
 * Service that hosts download jobs. Each active download job is handled as a
 * unique {@link DownloadThread} instance.
 * <p>
 * The majority of downloads should have ETag values to enable resuming, so if a
 * given download isn't able to finish in the normal job timeout (10 minutes),
 * we just reschedule the job and resume again in the future.
 */
public class DownloadJobService extends JobService {
    // @GuardedBy("mActiveThreads")
    private SparseArray<DownloadThread> mActiveThreads = new SparseArray<>();
    private Map<Long, DownloadInfo> mDownloads = new HashMap<>();

    @Override
    public void onCreate() {
        super.onCreate();

        // While someone is bound to us, watch for database changes that should
        // trigger notification updates.
        getContentResolver().registerContentObserver(Downloads.ALL_DOWNLOADS_CONTENT_URI, true, mObserver);
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        getContentResolver().unregisterContentObserver(mObserver);
    }

    @Override
    public boolean onStartJob(JobParameters params) {
        final int id = params.getJobId();
        System.out.println("cgp onStartJob");

        // Spin up thread to handle this download
        final DownloadInfo info = DownloadInfo.queryDownloadInfo(this, id);
        if (info == null) {
            Log.w(Constants.TAG, "Odd, no details found for download " + id);
            return false;
        }

        final DownloadThread thread;
        synchronized (mActiveThreads) {
            if (mActiveThreads.indexOfKey(id) >= 0) {
                Log.w(Constants.TAG, "Odd, already running download " + id);
                return false;
            }
            thread = new DownloadThread(this, params, info);
            info.startIfReady(System.currentTimeMillis());
            mActiveThreads.put(id, thread);
            mDownloads.put(info.mId, info);
        }
        thread.start();


        return true;
    }

    @Override
    public boolean onStopJob(JobParameters params) {
        final int id = params.getJobId();

        final DownloadThread thread;
        synchronized (mActiveThreads) {
            thread = mActiveThreads.get(id);
            mActiveThreads.remove(id);
            mDownloads.remove(id);
            // thread = mActiveThreads.removeReturnOld(id);
        }
        if (thread != null) {
            // If the thread is still running, ask it to gracefully shutdown,
            // and reschedule ourselves to resume in the future.
            thread.requestShutdown();

            Helpers.scheduleJob(this, DownloadInfo.queryDownloadInfo(this, id));
        }
        return false;
    }

    public void jobFinishedInternal(JobParameters params, boolean needsReschedule) {
        final int id = params.getJobId();

        synchronized (mActiveThreads) {
            mActiveThreads.remove(params.getJobId());
            mDownloads.remove(id);
        }
        if (needsReschedule) {
            Helpers.scheduleJob(this, DownloadInfo.queryDownloadInfo(this, id));
        }

        // Update notifications one last time while job is protecting us
        mObserver.onChange(false);

        // We do our own rescheduling above
        jobFinished(params, false);
    }

    private ContentObserver mObserver = new ContentObserver(Helpers.getAsyncHandler()) {
        @Override
        public void onChange(boolean selfChange) {
            // updateFromProvider();
            //  Helpers.getDownloadNotifier(DownloadJobService.this).update();
        }

        @Override
        public void onChange(boolean selfChange, Uri uri) {
            super.onChange(selfChange, uri);
            String id=uri.getLastPathSegment();
            if(!id.equals("all_downloads")){
                updateDownloadInfo(uri);
            }
//               updateFromProvider(id);
        }
    };
    private boolean mPendingUpdate;
    /**
     * Parses data from the content provider into private array
     */
    UpdateThread mUpdateThread;

    private void updateFromProvider(long id) {
        synchronized (this) {
            mPendingUpdate = true;
            if (mUpdateThread == null) {
                mUpdateThread = new UpdateThread(id);
                mUpdateThread.start();
            }
        }
    }

    public void updateDownloadInfo(Uri uri) {
        Cursor cursor = getContentResolver().query(uri, null, null, null, null);
        if (cursor == null) {
            return;
        }
        try {
            DownloadInfo.Reader reader = new DownloadInfo.Reader(
                    getContentResolver(), cursor);
            for (cursor.moveToFirst(); !cursor.isAfterLast(); cursor
                    .moveToNext()) {
                int idColumn = cursor.getColumnIndexOrThrow(Downloads._ID);
                long id = cursor.getLong(idColumn);
                DownloadInfo info = mDownloads.get(id);
                if (info != null) {
                    updateDownload(reader, info, System.currentTimeMillis());

                }
            }
        } finally {
            cursor.close();
        }

    }


    private class UpdateThread extends Thread {
        long id;

        public UpdateThread(long id) {
            super("Download Service");
            this.id = id;
        }

        public void run() {
            Process.setThreadPriority(Process.THREAD_PRIORITY_BACKGROUND);

            trimDatabase();
            removeSpuriousFiles();

            boolean keepService = false;
            // for each update from the database, remember which download is
            // supposed to get restarted soonest in the future
            long wakeUp = Long.MAX_VALUE;
            for (; ; ) {
                synchronized (DownloadJobService.this) {
                    if (mUpdateThread != this) {
                        throw new IllegalStateException(
                                "multiple UpdateThreads in DownloadService");
                    }
                    if (!mPendingUpdate) {
                        mUpdateThread = null;
                        if (!keepService) {
                            stopSelf();
                        }
                        return;
                    }
                    mPendingUpdate = false;
                }

                long now = System.currentTimeMillis();
                keepService = false;
                wakeUp = Long.MAX_VALUE;
                Set<Long> idsNoLongerInDatabase = new HashSet<Long>(
                        mDownloads.keySet());

                Cursor cursor = getContentResolver().query(
                        Downloads.ALL_DOWNLOADS_CONTENT_URI, null, Downloads._ID, new String[]{id + ""},
                        null);
                if (cursor == null) {
                    continue;
                }
                try {
                    DownloadInfo.Reader reader = new DownloadInfo.Reader(
                            getContentResolver(), cursor);
                    int idColumn = cursor.getColumnIndexOrThrow(Downloads._ID);

                    for (cursor.moveToFirst(); !cursor.isAfterLast(); cursor
                            .moveToNext()) {
                        long id = cursor.getLong(idColumn);
                        idsNoLongerInDatabase.remove(id);
                        DownloadInfo info = mDownloads.get(id);
                        if (info != null) {
                            updateDownload(reader, info, now);
                        } else {
                            //info = insertDownload(reader, now);
                        }
                        if (info.hasCompletionNotification()) {
                            keepService = true;
                        }
                        long next = info.nextAction(now);
                        if (next == 0) {
                            keepService = true;
                        } else if (next > 0 && next < wakeUp) {
                            wakeUp = next;
                        }
                    }
                } finally {
                    cursor.close();
                }

                for (Long id : idsNoLongerInDatabase) {
                    deleteDownload(id);
                }

                // is there a need to start the DownloadService? yes, if there
                // are rows to be deleted.

                for (DownloadInfo info : mDownloads.values()) {
                    if (info.mDeleted) {
                        keepService = true;
                        break;
                    }
                }

                //  mNotifier.updateNotification(mDownloads.values());

                // look for all rows with deleted flag set and delete the rows
                // from the database
                // permanently
                for (DownloadInfo info : mDownloads.values()) {
                    if (info.mDeleted) {
                        Helpers.deleteFile(getContentResolver(), info.mId,
                                info.mFileName, info.mMimeType);
                    }
                }
            }
        }
    }

    /**
     * Keeps a local copy of the info about a download, and initiates the
     * download if appropriate.
     */
//    private DownloadInfo insertDownload(DownloadInfo.Reader reader, long now) {
//        DownloadInfo info = reader.newDownloadInfo(this, Helpers.getSystemFacade(this));
//        //mDownloads.put(info.mId, info);
//
////        if (Constants.LOGVV) {
////            info.logVerboseInfo();
////        }
//        Helpers.scheduleJob(this, info);
//        //info.startIfReady(now);
//        return info;
//    }

    /**
     * Updates the local copy of the info about a download.
     */
    private void updateDownload(DownloadInfo.Reader reader, DownloadInfo info,
                                long now) {
        int oldVisibility = info.mVisibility;
        int oldStatus = info.mStatus;

        reader.updateFromDatabase(info);

        boolean lostVisibility = oldVisibility == Downloads.VISIBILITY_VISIBLE_NOTIFY_COMPLETED
                && info.mVisibility != Downloads.VISIBILITY_VISIBLE_NOTIFY_COMPLETED
                && Downloads.isStatusCompleted(info.mStatus);
        boolean justCompleted = !Downloads.isStatusCompleted(oldStatus)
                && Downloads.isStatusCompleted(info.mStatus);


        info.startIfReady(now);
    }

    /**
     * Drops old rows from the database to prevent it from growing too large
     */
    private void trimDatabase() {
        Cursor cursor = getContentResolver().query(
                Downloads.ALL_DOWNLOADS_CONTENT_URI,
                new String[]{Downloads._ID},
                Downloads.COLUMN_STATUS + " >= '200'", null,
                Downloads.COLUMN_LAST_MODIFICATION);
        if (cursor == null) {
            // This isn't good - if we can't do basic queries in our database,
            // nothing's gonna work
            Log.e(Constants.TAG, "null cursor in trimDatabase");
            return;
        }
        if (cursor.moveToFirst()) {
            int numDelete = cursor.getCount() - Constants.MAX_DOWNLOADS;
            int columnId = cursor.getColumnIndexOrThrow(Downloads._ID);
            while (numDelete > 0) {
                Uri downloadUri = ContentUris.withAppendedId(
                        Downloads.ALL_DOWNLOADS_CONTENT_URI,
                        cursor.getLong(columnId));
                getContentResolver().delete(downloadUri, null, null);
                if (!cursor.moveToNext()) {
                    break;
                }
                numDelete--;
            }
        }
        cursor.close();
    }

    /**
     * Removes files that may have been left behind in the cache directory
     */
    private void removeSpuriousFiles() {
        File[] files = Environment.getDownloadCacheDirectory().listFiles();
        if (files == null) {
            // The cache folder doesn't appear to exist (this is likely the case
            // when running the simulator).
            return;
        }
        HashSet<String> fileSet = new HashSet<String>();
        for (int i = 0; i < files.length; i++) {
            if (files[i].getName().equals(Constants.KNOWN_SPURIOUS_FILENAME)) {
                continue;
            }
            if (files[i].getName().equalsIgnoreCase(
                    Constants.RECOVERY_DIRECTORY)) {
                continue;
            }
            fileSet.add(files[i].getPath());
        }

        Cursor cursor = getContentResolver().query(
                Downloads.ALL_DOWNLOADS_CONTENT_URI,
                new String[]{Downloads._DATA}, null, null, null);
        if (cursor != null) {
            if (cursor.moveToFirst()) {
                do {
                    fileSet.remove(cursor.getString(0));
                } while (cursor.moveToNext());
            }
            cursor.close();
        }
        Iterator<String> iterator = fileSet.iterator();
        while (iterator.hasNext()) {
            String filename = iterator.next();
            if (Constants.LOGV) {
                Log.v(Constants.TAG, "deleting spurious file " + filename);
            }
            new File(filename).delete();
        }
    }

    /**
     * Removes the local copy of the info about a download.
     */
    private void deleteDownload(long id) {
        DownloadInfo info = mDownloads.get(id);
        if (info.mStatus == Downloads.STATUS_RUNNING) {
            info.mStatus = Downloads.STATUS_CANCELED;
        }
        if (info.mDestination != Downloads.DESTINATION_EXTERNAL
                && info.mFileName != null) {
            new File(info.mFileName).delete();
        }
        // mSystemFacade.cancelNotification(info.mId);
        mDownloads.remove(info.mId);
    }
}
