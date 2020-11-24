package com.android.systemui.miui.statusbar.analytics;

import android.content.Context;
import android.text.TextUtils;
import com.android.systemui.Constants;
import com.android.systemui.Logger;
import com.android.systemui.miui.analytics.AnalyticsWrapper;
import com.android.systemui.miui.analytics.OneTrackWrapper$Generic;
import com.android.systemui.miui.analytics.OneTrackWrapper$Notification;
import com.android.systemui.miui.statusbar.analytics.StatManager;
import com.xiaomi.stat.MiStatParams;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.BiConsumer;
import java.util.function.Consumer;
import miui.os.Build;
import org.json.JSONObject;

public class StatManager {
    private static boolean DEBUG = Constants.DEBUG;
    private static OneTrackImpl sOneTrack = new OneTrackImpl();
    private static List<Stat> sStats = new ArrayList(2);

    interface Stat {
        void init(Context context);

        void track(String str, Map<String, Object> map, List<Object> list);
    }

    public static void init(Context context) {
        if (!Build.IS_STABLE_VERSION) {
            sStats.add(sOneTrack);
        }
        sStats.add(new MiStatImpl());
        sStats.forEach(new Consumer(context) {
            private final /* synthetic */ Context f$0;

            {
                this.f$0 = r1;
            }

            public final void accept(Object obj) {
                ((StatManager.Stat) obj).init(this.f$0);
            }
        });
    }

    public static void track(String str, Map<String, Object> map, List<Object> list) {
        sStats.forEach(new Consumer(str, map, list) {
            private final /* synthetic */ String f$0;
            private final /* synthetic */ Map f$1;
            private final /* synthetic */ List f$2;

            {
                this.f$0 = r1;
                this.f$1 = r2;
                this.f$2 = r3;
            }

            public final void accept(Object obj) {
                ((StatManager.Stat) obj).track(this.f$0, StatManager.verify((Map<String, Object>) this.f$1), StatManager.verify((List<Object>) this.f$2));
            }
        });
    }

    public static void trackGenericEvent(String str, Map<String, Object> map) {
        sOneTrack.trackGenericEvent(str, verify(map));
    }

    private static Map<String, Object> verify(Map<String, Object> map) {
        if (map == null || map.size() == 0) {
            return new HashMap(sOneTrack.mDefaultParams);
        }
        HashMap hashMap = new HashMap(map.size() + 1);
        map.forEach(new BiConsumer(hashMap) {
            private final /* synthetic */ Map f$0;

            {
                this.f$0 = r1;
            }

            public final void accept(Object obj, Object obj2) {
                StatManager.optPut(this.f$0, (String) obj, obj2);
            }
        });
        return hashMap;
    }

    /* access modifiers changed from: private */
    public static Map<String, Object> optPut(Map<String, Object> map, String str, Object obj) {
        if (obj instanceof Integer) {
            map.put(str, (Integer) obj);
        } else if (obj instanceof Long) {
            map.put(str, (Long) obj);
        } else if (obj instanceof Boolean) {
            map.put(str, (Boolean) obj);
        } else if (obj instanceof Double) {
            map.put(str, (Double) obj);
        } else if (obj instanceof String) {
            String str2 = (String) obj;
            if (!TextUtils.isEmpty(str2)) {
                map.put(str, str2);
            }
        } else if (obj != null) {
            Logger.e("不支持的value类型 k=" + str + ",v=" + obj);
        }
        return map;
    }

    private static List<Object> verify(List<Object> list) {
        if (list == null || list.isEmpty()) {
            return null;
        }
        ArrayList arrayList = new ArrayList(list.size());
        list.forEach(new Consumer(arrayList) {
            private final /* synthetic */ List f$0;

            {
                this.f$0 = r1;
            }

            public final void accept(Object obj) {
                StatManager.lambda$verify$3(this.f$0, obj);
            }
        });
        return arrayList;
    }

    static /* synthetic */ void lambda$verify$3(List list, Object obj) {
        if (obj instanceof Map) {
            obj = verify((Map<String, Object>) (Map) obj);
        }
        list.add(obj);
    }

    private static class MiStatImpl implements Stat {
        private MiStatImpl() {
        }

        public void init(Context context) {
            AnalyticsWrapper.init(context);
        }

        public void track(String str, Map<String, Object> map, List<Object> list) {
            if (!"status_bar_settings_status".equals(str)) {
                map.put("ts", Long.valueOf(System.currentTimeMillis()));
                if (list == null || list.isEmpty()) {
                    MiStatParams miStatParams = new MiStatParams();
                    map.forEach(new BiConsumer(miStatParams) {
                        private final /* synthetic */ MiStatParams f$1;

                        {
                            this.f$1 = r2;
                        }

                        public final void accept(Object obj, Object obj2) {
                            StatManager.MiStatImpl.this.lambda$track$0$StatManager$MiStatImpl(this.f$1, (String) obj, obj2);
                        }
                    });
                    SystemUIStat.log("trackEvent eventName=%s params=%s", str, miStatParams.toJsonString());
                    AnalyticsWrapper.trackEvent(str, miStatParams);
                    return;
                }
                map.put("items", list);
                String jSONObject = new JSONObject(map).toString();
                SystemUIStat.log("trackEvent eventName=%s params=%s", str, jSONObject);
                AnalyticsWrapper.trackPlainTextEvent(str, jSONObject);
            }
        }

        /* access modifiers changed from: private */
        /* renamed from: optPut */
        public MiStatParams lambda$track$0$StatManager$MiStatImpl(MiStatParams miStatParams, String str, Object obj) {
            if (obj instanceof Integer) {
                miStatParams.putInt(str, ((Integer) obj).intValue());
            } else if (obj instanceof Long) {
                miStatParams.putLong(str, ((Long) obj).longValue());
            } else if (obj instanceof Boolean) {
                miStatParams.putBoolean(str, ((Boolean) obj).booleanValue());
            } else if (obj instanceof Double) {
                miStatParams.putDouble(str, ((Double) obj).doubleValue());
            } else if (obj instanceof String) {
                miStatParams.putString(str, (String) obj);
            }
            return miStatParams;
        }
    }

    private static class OneTrackImpl implements Stat {
        public Map<String, Object> mDefaultParams = new HashMap();

        public OneTrackImpl() {
            this.mDefaultParams.put("a", "a");
        }

        public void init(Context context) {
            OneTrackWrapper$Notification.init(context);
            OneTrackWrapper$Generic.init(context);
        }

        public void track(String str, Map<String, Object> map, List<Object> list) {
            if (map == null || map.size() == 0) {
                map = new HashMap<>(this.mDefaultParams);
            }
            if (list != null && !list.isEmpty()) {
                map.put("items", list);
            }
            SystemUIStat.log("trackEvent eventName=%s params=%s one", str, new JSONObject(map).toString());
            OneTrackWrapper$Notification.track(str, map);
        }

        public void trackGenericEvent(String str, Map<String, Object> map) {
            if (map == null || map.size() == 0) {
                map = new HashMap<>(this.mDefaultParams);
            }
            SystemUIStat.log("trackEvent eventName=%s params=%s one", str, new JSONObject(map).toString());
            OneTrackWrapper$Generic.track(str, map);
        }
    }
}
