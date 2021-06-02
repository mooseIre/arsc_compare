package com.android.systemui.pip.phone;

import android.content.Context;
import android.graphics.Rect;
import android.graphics.Region;
import android.os.Bundle;
import android.os.Handler;
import android.os.RemoteException;
import android.view.MagnificationSpec;
import android.view.accessibility.AccessibilityNodeInfo;
import android.view.accessibility.IAccessibilityInteractionConnection;
import android.view.accessibility.IAccessibilityInteractionConnectionCallback;
import com.android.systemui.C0015R$id;
import com.android.systemui.C0021R$string;
import com.android.systemui.pip.PipSnapAlgorithm;
import com.android.systemui.pip.PipTaskOrganizer;
import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;

public class PipAccessibilityInteractionConnection extends IAccessibilityInteractionConnection.Stub {
    private List<AccessibilityNodeInfo> mAccessibilityNodeInfoList;
    private AccessibilityCallbacks mCallbacks;
    private Context mContext;
    private final Rect mExpandedBounds = new Rect();
    private final Rect mExpandedMovementBounds = new Rect();
    private Handler mHandler;
    private PipMotionHelper mMotionHelper;
    private final Rect mNormalBounds = new Rect();
    private final Rect mNormalMovementBounds = new Rect();
    private PipSnapAlgorithm mSnapAlgorithm;
    private PipTaskOrganizer mTaskOrganizer;
    private Rect mTmpBounds = new Rect();
    private Runnable mUpdateMovementBoundCallback;

    public interface AccessibilityCallbacks {
        void onAccessibilityShowMenu();
    }

    public void clearAccessibilityFocus() {
    }

    public void notifyOutsideTouch() {
    }

    public PipAccessibilityInteractionConnection(Context context, PipMotionHelper pipMotionHelper, PipTaskOrganizer pipTaskOrganizer, PipSnapAlgorithm pipSnapAlgorithm, AccessibilityCallbacks accessibilityCallbacks, Runnable runnable, Handler handler) {
        this.mContext = context;
        this.mHandler = handler;
        this.mMotionHelper = pipMotionHelper;
        this.mTaskOrganizer = pipTaskOrganizer;
        this.mSnapAlgorithm = pipSnapAlgorithm;
        this.mUpdateMovementBoundCallback = runnable;
        this.mCallbacks = accessibilityCallbacks;
    }

    public void findAccessibilityNodeInfoByAccessibilityId(long j, Region region, int i, IAccessibilityInteractionConnectionCallback iAccessibilityInteractionConnectionCallback, int i2, int i3, long j2, MagnificationSpec magnificationSpec, Bundle bundle) {
        try {
            iAccessibilityInteractionConnectionCallback.setFindAccessibilityNodeInfosResult(j == AccessibilityNodeInfo.ROOT_NODE_ID ? getNodeList() : null, i);
        } catch (RemoteException unused) {
        }
    }

    public void performAccessibilityAction(long j, int i, Bundle bundle, int i2, IAccessibilityInteractionConnectionCallback iAccessibilityInteractionConnectionCallback, int i3, int i4, long j2) {
        boolean z = true;
        if (j == AccessibilityNodeInfo.ROOT_NODE_ID) {
            if (i == C0015R$id.action_pip_resize) {
                if (this.mMotionHelper.getBounds().width() == this.mNormalBounds.width() && this.mMotionHelper.getBounds().height() == this.mNormalBounds.height()) {
                    setToExpandedBounds();
                } else {
                    setToNormalBounds();
                }
            } else if (i == 16) {
                this.mHandler.post(new Runnable() {
                    /* class com.android.systemui.pip.phone.$$Lambda$PipAccessibilityInteractionConnection$yj5JMyeINsNwnRK777qXcVORJV0 */

                    public final void run() {
                        PipAccessibilityInteractionConnection.this.lambda$performAccessibilityAction$0$PipAccessibilityInteractionConnection();
                    }
                });
            } else if (i == 262144) {
                this.mMotionHelper.expandPipToFullscreen();
            } else if (i == 1048576) {
                this.mMotionHelper.dismissPip();
            } else if (i == 16908354) {
                int i5 = bundle.getInt("ACTION_ARGUMENT_MOVE_WINDOW_X");
                int i6 = bundle.getInt("ACTION_ARGUMENT_MOVE_WINDOW_Y");
                new Rect().set(this.mMotionHelper.getBounds());
                this.mTmpBounds.offsetTo(i5, i6);
                this.mMotionHelper.movePip(this.mTmpBounds);
            }
            iAccessibilityInteractionConnectionCallback.setPerformAccessibilityActionResult(z, i2);
        }
        z = false;
        try {
            iAccessibilityInteractionConnectionCallback.setPerformAccessibilityActionResult(z, i2);
        } catch (RemoteException unused) {
        }
    }

    /* access modifiers changed from: private */
    /* renamed from: lambda$performAccessibilityAction$0 */
    public /* synthetic */ void lambda$performAccessibilityAction$0$PipAccessibilityInteractionConnection() {
        this.mCallbacks.onAccessibilityShowMenu();
    }

    private void setToExpandedBounds() {
        this.mSnapAlgorithm.applySnapFraction(this.mExpandedBounds, this.mExpandedMovementBounds, this.mSnapAlgorithm.getSnapFraction(new Rect(this.mTaskOrganizer.getLastReportedBounds()), this.mNormalMovementBounds));
        this.mTaskOrganizer.scheduleFinishResizePip(this.mExpandedBounds, new Consumer() {
            /* class com.android.systemui.pip.phone.$$Lambda$PipAccessibilityInteractionConnection$ooTCZxn3Zqg4Jawuz5C0YCDXc2E */

            @Override // java.util.function.Consumer
            public final void accept(Object obj) {
                PipAccessibilityInteractionConnection.this.lambda$setToExpandedBounds$1$PipAccessibilityInteractionConnection((Rect) obj);
            }
        });
    }

    /* access modifiers changed from: private */
    /* renamed from: lambda$setToExpandedBounds$1 */
    public /* synthetic */ void lambda$setToExpandedBounds$1$PipAccessibilityInteractionConnection(Rect rect) {
        this.mMotionHelper.synchronizePinnedStackBounds();
        this.mUpdateMovementBoundCallback.run();
    }

    private void setToNormalBounds() {
        this.mSnapAlgorithm.applySnapFraction(this.mNormalBounds, this.mNormalMovementBounds, this.mSnapAlgorithm.getSnapFraction(new Rect(this.mTaskOrganizer.getLastReportedBounds()), this.mExpandedMovementBounds));
        this.mTaskOrganizer.scheduleFinishResizePip(this.mNormalBounds, new Consumer() {
            /* class com.android.systemui.pip.phone.$$Lambda$PipAccessibilityInteractionConnection$79jkZILLclQeuRzkIEwlk9IySM */

            @Override // java.util.function.Consumer
            public final void accept(Object obj) {
                PipAccessibilityInteractionConnection.this.lambda$setToNormalBounds$2$PipAccessibilityInteractionConnection((Rect) obj);
            }
        });
    }

    /* access modifiers changed from: private */
    /* renamed from: lambda$setToNormalBounds$2 */
    public /* synthetic */ void lambda$setToNormalBounds$2$PipAccessibilityInteractionConnection(Rect rect) {
        this.mMotionHelper.synchronizePinnedStackBounds();
        this.mUpdateMovementBoundCallback.run();
    }

    public void findAccessibilityNodeInfosByViewId(long j, String str, Region region, int i, IAccessibilityInteractionConnectionCallback iAccessibilityInteractionConnectionCallback, int i2, int i3, long j2, MagnificationSpec magnificationSpec) {
        try {
            iAccessibilityInteractionConnectionCallback.setFindAccessibilityNodeInfoResult((AccessibilityNodeInfo) null, i);
        } catch (RemoteException unused) {
        }
    }

    public void findAccessibilityNodeInfosByText(long j, String str, Region region, int i, IAccessibilityInteractionConnectionCallback iAccessibilityInteractionConnectionCallback, int i2, int i3, long j2, MagnificationSpec magnificationSpec) {
        try {
            iAccessibilityInteractionConnectionCallback.setFindAccessibilityNodeInfoResult((AccessibilityNodeInfo) null, i);
        } catch (RemoteException unused) {
        }
    }

    public void findFocus(long j, int i, Region region, int i2, IAccessibilityInteractionConnectionCallback iAccessibilityInteractionConnectionCallback, int i3, int i4, long j2, MagnificationSpec magnificationSpec) {
        try {
            iAccessibilityInteractionConnectionCallback.setFindAccessibilityNodeInfoResult((AccessibilityNodeInfo) null, i2);
        } catch (RemoteException unused) {
        }
    }

    public void focusSearch(long j, int i, Region region, int i2, IAccessibilityInteractionConnectionCallback iAccessibilityInteractionConnectionCallback, int i3, int i4, long j2, MagnificationSpec magnificationSpec) {
        try {
            iAccessibilityInteractionConnectionCallback.setFindAccessibilityNodeInfoResult((AccessibilityNodeInfo) null, i2);
        } catch (RemoteException unused) {
        }
    }

    /* access modifiers changed from: package-private */
    public void onMovementBoundsChanged(Rect rect, Rect rect2, Rect rect3, Rect rect4) {
        this.mNormalBounds.set(rect);
        this.mExpandedBounds.set(rect2);
        this.mNormalMovementBounds.set(rect3);
        this.mExpandedMovementBounds.set(rect4);
    }

    public static AccessibilityNodeInfo obtainRootAccessibilityNodeInfo(Context context) {
        AccessibilityNodeInfo obtain = AccessibilityNodeInfo.obtain();
        obtain.setSourceNodeId(AccessibilityNodeInfo.ROOT_NODE_ID, -3);
        obtain.addAction(AccessibilityNodeInfo.AccessibilityAction.ACTION_CLICK);
        obtain.addAction(AccessibilityNodeInfo.AccessibilityAction.ACTION_DISMISS);
        obtain.addAction(AccessibilityNodeInfo.AccessibilityAction.ACTION_MOVE_WINDOW);
        obtain.addAction(AccessibilityNodeInfo.AccessibilityAction.ACTION_EXPAND);
        obtain.addAction(new AccessibilityNodeInfo.AccessibilityAction(C0015R$id.action_pip_resize, context.getString(C0021R$string.accessibility_action_pip_resize)));
        obtain.setImportantForAccessibility(true);
        obtain.setClickable(true);
        obtain.setVisibleToUser(true);
        return obtain;
    }

    private List<AccessibilityNodeInfo> getNodeList() {
        if (this.mAccessibilityNodeInfoList == null) {
            this.mAccessibilityNodeInfoList = new ArrayList(1);
        }
        AccessibilityNodeInfo obtainRootAccessibilityNodeInfo = obtainRootAccessibilityNodeInfo(this.mContext);
        this.mAccessibilityNodeInfoList.clear();
        this.mAccessibilityNodeInfoList.add(obtainRootAccessibilityNodeInfo);
        return this.mAccessibilityNodeInfoList;
    }
}
