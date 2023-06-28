package com.doublesymmetry.trackplayer.offline;


/*
 * Copyright (C) 2016 The Android Open Source Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */


import android.app.Application;
import android.content.Context;

import com.facebook.react.bridge.ReactContext;
import com.google.android.exoplayer2.DefaultRenderersFactory;
import com.google.android.exoplayer2.RenderersFactory;
import com.google.android.exoplayer2.database.DatabaseProvider;
// import com.google.android.exoplayer2.database.ExoDatabaseProvider;
import com.google.android.exoplayer2.database.StandaloneDatabaseProvider;
import com.google.android.exoplayer2.offline.DefaultDownloadIndex;
import com.google.android.exoplayer2.offline.DefaultDownloaderFactory;
import com.google.android.exoplayer2.offline.DownloadManager;
import com.google.android.exoplayer2.offline.DownloadRequest;
// import com.google.android.exoplayer2.offline.DownloaderConstructorHelper;
import com.google.android.exoplayer2.ui.DownloadNotificationHelper;
import com.google.android.exoplayer2.upstream.DataSource;
// import com.google.android.exoplayer2.upstream.DefaultDataSourceFactory;
import com.google.android.exoplayer2.upstream.DefaultDataSource;
import com.google.android.exoplayer2.upstream.DefaultHttpDataSource;
import com.google.android.exoplayer2.util.Log;
//import com.google.android.exoplayer2.upstream.DefaultHttpDataSourceFactory;
import com.google.android.exoplayer2.upstream.FileDataSource;
import com.google.android.exoplayer2.upstream.HttpDataSource;
import com.google.android.exoplayer2.upstream.cache.Cache;
import com.google.android.exoplayer2.upstream.cache.CacheDataSource;
// import com.google.android.exoplayer2.offline.DownloadHelper;
//import com.google.android.exoplayer2.upstream.cache.CacheDataSourceFactory;
import com.google.android.exoplayer2.upstream.cache.NoOpCacheEvictor;
import com.google.android.exoplayer2.upstream.cache.SimpleCache;
import com.google.android.exoplayer2.upstream.cache.CacheSpan;
// import com.google.android.exoplayer2.source.MediaSource;
import com.google.android.exoplayer2.util.Log;
import com.google.android.exoplayer2.util.Util;
// import com.google.android.exoplayer2.MediaItem;
// import com.doublesymmetry.trackplayer.kotlinaudio.event.EventHolder;
import java.util.concurrent.Executors;

import java.io.File;
import java.io.IOException;
import java.net.CookieHandler;
import java.net.CookieManager;
import java.net.CookiePolicy;
import java.util.List;
import java.util.Set;

/**
 * Placeholder application to facilitate overriding Application methods for debugging and testing.
 */
public final class DownloadUtil {

    public static final String DOWNLOAD_NOTIFICATION_CHANNEL_ID = "download_channel";

    private static final String TAG = "DemoApplication";
    // private static final String DOWNLOAD_ACTION_FILE = "actions";
    // private static final String DOWNLOAD_TRACKER_ACTION_FILE = "tracked_actions";
    private static final String DOWNLOAD_CONTENT_DIRECTORY = "downloads";

    protected static String userAgent;

    private static DatabaseProvider databaseProvider;
    private static File downloadDirectory;
    private static Cache downloadCache;
    private static DownloadManager downloadManager;
    private static DownloadTracker downloadTracker;
    private static DownloadNotificationHelper downloadNotificationHelper;

    private static DataSource.Factory dataSourceFactory;
    private static HttpDataSource.Factory httpDataSourceFactory;


    /**
     * Returns a {@link DataSource.Factory}.
     */

    private static CacheDataSource.Factory buildReadOnlyCacheDataSource(
      DataSource.Factory upstreamFactory, Cache cache) {
      return new CacheDataSource.Factory()
          .setCache(cache)
          .setUpstreamDataSourceFactory(upstreamFactory)
          .setCacheWriteDataSinkFactory(null)
          .setFlags(CacheDataSource.FLAG_IGNORE_CACHE_ON_ERROR);
    }


  /** Returns a {@link DataSource.Factory}. */
  public static synchronized DataSource.Factory getDataSourceFactory(Context context) {
    if (dataSourceFactory == null) {
      context = context.getApplicationContext();
      DefaultDataSource.Factory upstreamFactory =
          new DefaultDataSource.Factory(context, getHttpDataSourceFactory());
      dataSourceFactory = buildReadOnlyCacheDataSource(upstreamFactory, getDownloadCache(context));
    }
    return dataSourceFactory;
  }


    /**
     * Returns a {@link HttpDataSource.Factory}.
     */
    public static HttpDataSource.Factory getHttpDataSourceFactory() {
      if (httpDataSourceFactory == null) {
        // We don't want to use Cronet, or we failed to instantiate a CronetEngine.
        CookieManager cookieManager = new CookieManager();
        cookieManager.setCookiePolicy(CookiePolicy.ACCEPT_ORIGINAL_SERVER);
        CookieHandler.setDefault(cookieManager);
        httpDataSourceFactory = new DefaultHttpDataSource.Factory();
      }
      return httpDataSourceFactory;
    }


    public static RenderersFactory buildRenderersFactory(Context context) {
        return new DefaultRenderersFactory(context.getApplicationContext());
    }

    // public static MediaSource createMediaSource(Context context, MediaItem mediaItem, DataSource.Factory factory, DownloadRequest downloadRequest) {
    //   DownloadHelper downloadHelper = new DownloadHelper(
    //     mediaItem,
    //     DownloadHelper.createMediaSource(downloadRequest, factory),
    //     null,
    //     null
    //   );
    //   return downloadHelper.createMediaSource(downloadRequest, factory);
    // }


    public static DownloadNotificationHelper getDownloadNotificationHelper(Context context) {
        if (downloadNotificationHelper == null) {
            downloadNotificationHelper =
                    new DownloadNotificationHelper(context, DOWNLOAD_NOTIFICATION_CHANNEL_ID);
        }
        return downloadNotificationHelper;
    }

    public static DownloadManager getDownloadManager(Context context) {
        ensureDownloadManagerInitialized(context);
        return downloadManager;
    }

    public static DownloadTracker getDownloadTracker(Context context) {
        ensureDownloadManagerInitialized(context);
        return downloadTracker;
    }

    public static synchronized Cache getDownloadCache(Context context) {
        if (downloadCache == null) {
            File downloadContentDirectory = new File(getDownloadDirectory(context), DOWNLOAD_CONTENT_DIRECTORY);
            downloadCache =
                    new SimpleCache(downloadContentDirectory, new NoOpCacheEvictor(), getDatabaseProvider(context));
        }
        return downloadCache;
    }


    private static synchronized void ensureDownloadManagerInitialized(Context context) {
      if (downloadManager == null) {
        downloadManager =
            new DownloadManager(
                context,
                getDatabaseProvider(context),
                getDownloadCache(context),
                getHttpDataSourceFactory(),
                Executors.newFixedThreadPool(/* nThreads= */ 6));
        downloadTracker =
            new DownloadTracker(context, getHttpDataSourceFactory(), downloadManager);
      }
    }

    private static DatabaseProvider getDatabaseProvider(Context context) {
        if (databaseProvider == null) {
          databaseProvider = new StandaloneDatabaseProvider(context);
        }
        return databaseProvider;
    }


    public static File getDownloadDirectory(Context context) {
        if (downloadDirectory == null) {
            downloadDirectory = context.getExternalFilesDir(null);
            if (downloadDirectory == null) {
                downloadDirectory = context.getFilesDir();
            }
        }
        return downloadDirectory;
    }
}
