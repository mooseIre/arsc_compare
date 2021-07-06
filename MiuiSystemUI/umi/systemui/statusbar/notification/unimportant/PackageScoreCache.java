package com.android.systemui.statusbar.notification.unimportant;

import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.util.Log;
import android.util.LruCache;
import com.android.systemui.statusbar.notification.DatabaseHelper;
import com.android.systemui.statusbar.notification.unimportant.PackageScoreCache;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

public class PackageScoreCache {
    private static final long DAYS_TO_MILLIS = TimeUnit.DAYS.toMillis(1);
    private int mCurrentDays;
    private final ExecutorService mExecutor = Executors.newCachedThreadPool();
    private final DatabaseHelper mOpenHelper;
    private final RankLruCache<String, PackageEntity> mPkgEntities = new RankLruCache<>(64);
    private int mTotalClickCount;
    private int mTotalShowCount;

    public PackageScoreCache(Context context) {
        this.mOpenHelper = DatabaseHelper.getInstance(context);
    }

    public PackageEntity getPkgEntity(String str) {
        return this.mPkgEntities.get(str);
    }

    public int getTotalShowCount() {
        return this.mTotalShowCount;
    }

    public int getTotalClickCount() {
        return this.mTotalClickCount;
    }

    public void asyncUpdate() {
        this.mExecutor.submit(new Runnable() {
            /* class com.android.systemui.statusbar.notification.unimportant.$$Lambda$PackageScoreCache$H3ol39vfIqoLj8kQxiHm7tbftE4 */

            public final void run() {
                PackageScoreCache.lambda$H3ol39vfIqoLj8kQxiHm7tbftE4(PackageScoreCache.this);
            }
        });
    }

    private void updateEntity(PackageEntity packageEntity) {
        SQLiteDatabase openDB;
        if (packageEntity.isDataChanged() && (openDB = openDB()) != null) {
            insertOrUpdate(openDB, packageEntity);
            closeDB(openDB);
        }
    }

    /* access modifiers changed from: public */
    private void updateAll() {
        SQLiteDatabase openDB = openDB();
        if (openDB != null) {
            Map<String, PackageEntity> snapshot = this.mPkgEntities.snapshot();
            try {
                writeToDatabase(openDB, snapshot);
            } catch (Exception e) {
                Log.d("packageScoreCache", "updateAll Exception " + e);
            }
            if (isDateChanged()) {
                removeExpiredData(openDB);
                updateEntryData(openDB, snapshot);
                updateLocalData(openDB);
            }
            closeDB(openDB);
        }
    }

    private void writeToDatabase(SQLiteDatabase sQLiteDatabase, Map<String, PackageEntity> map) {
        sQLiteDatabase.beginTransaction();
        try {
            for (Map.Entry<String, PackageEntity> entry : map.entrySet()) {
                PackageEntity value = entry.getValue();
                if (value.isDataChanged()) {
                    insertOrUpdate(sQLiteDatabase, value);
                    value.setDataChanged(false);
                }
            }
            sQLiteDatabase.setTransactionSuccessful();
        } catch (Exception e) {
            Log.e("packageScoreCache", "writeToDatabase Exception " + e);
        } catch (Throwable th) {
            sQLiteDatabase.endTransaction();
            throw th;
        }
        sQLiteDatabase.endTransaction();
    }

    private void insertOrUpdate(SQLiteDatabase sQLiteDatabase, PackageEntity packageEntity) {
        int digitalFormatDateToday = DateUtils.getDigitalFormatDateToday();
        String packageName = packageEntity.getPackageName();
        try {
            sQLiteDatabase.execSQL(" INSERT OR REPLACE INTO notification_sort " + " (_id, package_name, date, click_count, show_count) " + " VALUES((SELECT _id FROM notification_sort " + " WHERE package_name = '" + packageName + "' " + " AND date = " + digitalFormatDateToday + ") " + " , '" + packageName + "' " + " , " + digitalFormatDateToday + " , " + packageEntity.getDailyClick() + " , " + packageEntity.getDailyShow() + ") ");
        } catch (Exception e) {
            Log.d("packageScoreCache", "insertOrUpdate Exception " + e);
        }
    }

    private void removeExpiredData(SQLiteDatabase sQLiteDatabase) {
        int digitalPreviousMonthDate = DateUtils.getDigitalPreviousMonthDate();
        try {
            sQLiteDatabase.execSQL(" DELETE FROM notification_sort " + " WHERE date < " + digitalPreviousMonthDate);
        } catch (Exception e) {
            Log.d("packageScoreCache", "removeExpiredData Exception " + e);
        }
    }

    private void updateEntryData(SQLiteDatabase sQLiteDatabase, Map<String, PackageEntity> map) {
        int digitalFormatDateToday = DateUtils.getDigitalFormatDateToday();
        Cursor cursor = null;
        try {
            cursor = sQLiteDatabase.rawQuery(" SELECT package_name, SUM(click_count), SUM(show_count) FROM notification_sort " + " WHERE date < " + digitalFormatDateToday + " GROUP BY package_name ", null);
            if (cursor != null) {
                while (cursor.moveToNext()) {
                    String string = cursor.getString(0);
                    int i = cursor.getInt(1);
                    int i2 = cursor.getInt(2);
                    PackageEntity packageEntity = map.get(string);
                    if (packageEntity != null) {
                        packageEntity.onDateChanged(i, i2);
                    }
                }
            }
        } catch (Exception e) {
            Log.d("packageScoreCache", "updateEntryData exception " + e);
        } catch (Throwable th) {
            closeCursor(null);
            throw th;
        }
        closeCursor(cursor);
    }

    private void updateLocalData(SQLiteDatabase sQLiteDatabase) {
        int digitalFormatDateToday = DateUtils.getDigitalFormatDateToday();
        Cursor cursor = null;
        try {
            cursor = sQLiteDatabase.rawQuery(" SELECT SUM(click_count), SUM(show_count) FROM notification_sort " + " WHERE date < " + digitalFormatDateToday, null);
            if (cursor != null && cursor.moveToFirst()) {
                this.mTotalClickCount = cursor.getInt(0);
                this.mTotalShowCount = cursor.getInt(1);
            }
        } catch (Exception e) {
            Log.d("packageScoreCache", "updateLocalData exception " + e);
        } catch (Throwable th) {
            closeCursor(null);
            throw th;
        }
        closeCursor(cursor);
    }

    public PackageEntity addShow(String str) {
        if (this.mPkgEntities.get(str) == null) {
            retrievePackage(str);
        }
        PackageEntity packageEntity = this.mPkgEntities.get(str);
        packageEntity.addShowCount();
        return packageEntity;
    }

    public PackageEntity addClick(String str) {
        if (this.mPkgEntities.get(str) == null) {
            retrievePackage(str);
        }
        PackageEntity packageEntity = this.mPkgEntities.get(str);
        packageEntity.addClickCount();
        return packageEntity;
    }

    private void retrievePackage(String str) {
        final PackageEntity packageEntity = new PackageEntity(str);
        this.mPkgEntities.put(str, packageEntity);
        this.mExecutor.submit(new Runnable() {
            /* class com.android.systemui.statusbar.notification.unimportant.PackageScoreCache.AnonymousClass1 */

            public void run() {
                PackageScoreCache.this.updateEntityData(packageEntity);
            }
        });
    }

    private void updateEntityData(PackageEntity packageEntity) {
        SQLiteDatabase openDB = openDB();
        if (openDB != null) {
            updateDailyData(openDB, packageEntity);
            updateHistoryData(openDB, packageEntity);
            closeDB(openDB);
        }
    }

    private void updateDailyData(SQLiteDatabase sQLiteDatabase, PackageEntity packageEntity) {
        int digitalFormatDateToday = DateUtils.getDigitalFormatDateToday();
        String packageName = packageEntity.getPackageName();
        Cursor cursor = null;
        try {
            cursor = sQLiteDatabase.rawQuery(" SELECT click_count, show_count FROM notification_sort " + " WHERE package_name = '" + packageName + "' " + " AND date = " + digitalFormatDateToday, null);
            if (cursor != null && cursor.moveToFirst()) {
                packageEntity.setDailyData(cursor.getInt(0), cursor.getInt(1));
            }
        } catch (Exception e) {
            Log.d("packageScoreCache", "updateDailyData exception " + e);
        } catch (Throwable th) {
            closeCursor(null);
            throw th;
        }
        closeCursor(cursor);
    }

    private void updateHistoryData(SQLiteDatabase sQLiteDatabase, PackageEntity packageEntity) {
        int digitalFormatDateToday = DateUtils.getDigitalFormatDateToday();
        String packageName = packageEntity.getPackageName();
        Cursor cursor = null;
        try {
            cursor = sQLiteDatabase.rawQuery(" SELECT SUM(click_count), SUM(show_count) FROM notification_sort " + " WHERE package_name = '" + packageName + "' " + " AND date < " + digitalFormatDateToday, null);
            if (cursor != null && cursor.moveToFirst()) {
                packageEntity.setHistoryData(cursor.getInt(0), cursor.getInt(1));
            }
        } catch (Exception e) {
            Log.d("packageScoreCache", "updateHistoryData exception " + e);
        } catch (Throwable th) {
            closeCursor(null);
            throw th;
        }
        closeCursor(cursor);
    }

    private SQLiteDatabase openDB() {
        try {
            return this.mOpenHelper.getWritableDatabase();
        } catch (Exception e) {
            Log.d("packageScoreCache", "openDB failed " + e);
            return null;
        }
    }

    private void closeDB(SQLiteDatabase sQLiteDatabase) {
        if (sQLiteDatabase != null && sQLiteDatabase.isOpen()) {
            sQLiteDatabase.close();
        }
    }

    private void closeCursor(Cursor cursor) {
        if (cursor != null && !cursor.isClosed()) {
            cursor.close();
        }
    }

    private boolean isDateChanged() {
        int currentTimeMillis = (int) (System.currentTimeMillis() / DAYS_TO_MILLIS);
        if (currentTimeMillis == this.mCurrentDays) {
            return false;
        }
        this.mCurrentDays = currentTimeMillis;
        return true;
    }

    public int getTotalClickCount(String str) {
        PackageEntity pkgEntity = getPkgEntity(str);
        if (pkgEntity != null) {
            return pkgEntity.getTotalClick();
        }
        return 0;
    }

    public int getTotalShowCount(String str) {
        PackageEntity pkgEntity = getPkgEntity(str);
        if (pkgEntity != null) {
            return pkgEntity.getTotalShow();
        }
        return 0;
    }

    public Map<String, Integer> getAllCount(String str) {
        HashMap hashMap = new HashMap();
        hashMap.put("total_click_count", Integer.valueOf(getTotalClickCount()));
        hashMap.put("total_show_count", Integer.valueOf(getTotalShowCount()));
        hashMap.put("pkg_click_count", Integer.valueOf(getTotalClickCount(str)));
        hashMap.put("pkg_show_count", Integer.valueOf(getTotalShowCount(str)));
        return hashMap;
    }

    public class RankLruCache<K, V> extends LruCache<K, V> {
        /* JADX INFO: super call moved to the top of the method (can break code semantics) */
        public RankLruCache(int i) {
            super(i);
            PackageScoreCache.this = r1;
        }

        @Override // android.util.LruCache
        public void entryRemoved(boolean z, K k, V v, V v2) {
            if (z && (v instanceof PackageEntity)) {
                PackageScoreCache.this.mExecutor.submit(new Runnable(v) {
                    /* class com.android.systemui.statusbar.notification.unimportant.$$Lambda$PackageScoreCache$RankLruCache$WQnjbZIoT7ZZWn9W9dw8lkU5l0 */
                    public final /* synthetic */ PackageEntity f$1;

                    {
                        this.f$1 = r2;
                    }

                    public final void run() {
                        PackageScoreCache.RankLruCache.this.lambda$entryRemoved$0$PackageScoreCache$RankLruCache(this.f$1);
                    }
                });
            }
        }

        /* access modifiers changed from: public */
        /* access modifiers changed from: private */
        /* renamed from: lambda$entryRemoved$0 */
        public /* synthetic */ void lambda$entryRemoved$0$PackageScoreCache$RankLruCache(PackageEntity packageEntity) {
            PackageScoreCache.this.updateEntity(packageEntity);
        }
    }
}
