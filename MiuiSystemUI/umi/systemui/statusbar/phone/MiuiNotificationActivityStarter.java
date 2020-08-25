package com.android.systemui.statusbar.phone;

import android.app.ActivityManager;
import android.app.ActivityManagerCompat;
import android.app.ActivityOptions;
import android.app.ActivityTaskManagerCompat;
import android.app.KeyguardManager;
import android.app.Notification;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.os.RemoteException;
import android.os.UserHandle;
import android.service.notification.StatusBarNotification;
import android.util.Log;
import android.util.MiuiMultiWindowUtils;
import android.view.RemoteAnimationAdapter;
import com.android.internal.statusbar.IStatusBarService;
import com.android.internal.widget.LockPatternUtils;
import com.android.keyguard.KeyguardUpdateMonitor;
import com.android.keyguard.MiuiKeyguardUtils;
import com.android.systemui.Dependency;
import com.android.systemui.Logger;
import com.android.systemui.UiOffloadThread;
import com.android.systemui.assist.AssistManager;
import com.android.systemui.bubbles.BubbleController;
import com.android.systemui.miui.statusbar.ExpandedNotification;
import com.android.systemui.plugins.ActivityStarter;
import com.android.systemui.plugins.statusbar.StatusBarStateController;
import com.android.systemui.statusbar.ExpandableNotificationRow;
import com.android.systemui.statusbar.NotificationData;
import com.android.systemui.statusbar.NotificationPresenter;
import com.android.systemui.statusbar.notification.MiuiActivityLaunchAnimator;
import com.android.systemui.statusbar.notification.NotificationActivityStarter;
import com.android.systemui.statusbar.policy.HeadsUpManager;
import com.android.systemui.statusbar.policy.PreviewInflater;
import java.util.Objects;

public class MiuiNotificationActivityStarter implements NotificationActivityStarter {
    private final MiuiActivityLaunchAnimator mActivityLaunchAnimator;
    private final ActivityStarter mActivityStarter;
    private final BroadcastReceiver mAllUsersReceiver = new BroadcastReceiver() {
        public void onReceive(Context context, Intent intent) {
            String action = intent.getAction();
            if ("android.intent.action.USER_SWITCHED".equals(action)) {
                int unused = MiuiNotificationActivityStarter.this.mCurrentUserId = intent.getIntExtra("android.intent.extra.user_handle", -1);
            } else if ("android.intent.action.USER_UNLOCKED".equals(action) && MiuiNotificationActivityStarter.this.mRunnable != null) {
                ((UiOffloadThread) Dependency.get(UiOffloadThread.class)).submit(MiuiNotificationActivityStarter.this.mRunnable);
                Runnable unused2 = MiuiNotificationActivityStarter.this.mRunnable = null;
            }
        }
    };
    /* access modifiers changed from: private */
    public final AssistManager mAssistManager;
    /* access modifiers changed from: private */
    public final IStatusBarService mBarService;
    /* access modifiers changed from: private */
    public final BubbleController mBubbleController;
    /* access modifiers changed from: private */
    public final Context mContext;
    /* access modifiers changed from: private */
    public int mCurrentUserId = 0;
    /* access modifiers changed from: private */
    public final NotificationGroupManager mGroupManager;
    /* access modifiers changed from: private */
    public final HeadsUpManager mHeadsUpManager;
    /* access modifiers changed from: private */
    public final KeyguardManager mKeyguardManager;
    /* access modifiers changed from: private */
    public final LockPatternUtils mLockPatternUtils;
    /* access modifiers changed from: private */
    public final Handler mMainThreadHandler;
    /* access modifiers changed from: private */
    public final NotificationData mNotificationData;
    /* access modifiers changed from: private */
    public final NotificationPresenter mPresenter;
    /* access modifiers changed from: private */
    public Runnable mRunnable;
    /* access modifiers changed from: private */
    public final ShadeController mShadeController;

    public MiuiNotificationActivityStarter(Context context, AssistManager assistManager, NotificationPresenter notificationPresenter, NotificationData notificationData, LockPatternUtils lockPatternUtils, HeadsUpManager headsUpManager, KeyguardManager keyguardManager, NotificationGroupManager notificationGroupManager, ShadeController shadeController, Handler handler, Handler handler2, ActivityStarter activityStarter, IStatusBarService iStatusBarService, BubbleController bubbleController, MiuiActivityLaunchAnimator miuiActivityLaunchAnimator) {
        this.mContext = context;
        this.mAssistManager = assistManager;
        this.mPresenter = notificationPresenter;
        this.mNotificationData = notificationData;
        this.mLockPatternUtils = lockPatternUtils;
        this.mHeadsUpManager = headsUpManager;
        this.mKeyguardManager = keyguardManager;
        this.mGroupManager = notificationGroupManager;
        this.mShadeController = shadeController;
        this.mMainThreadHandler = handler;
        this.mActivityStarter = activityStarter;
        this.mBarService = iStatusBarService;
        this.mBubbleController = bubbleController;
        this.mActivityLaunchAnimator = miuiActivityLaunchAnimator;
        this.mCurrentUserId = ActivityManager.getCurrentUser();
        IntentFilter intentFilter = new IntentFilter("android.intent.action.USER_SWITCHED");
        intentFilter.addAction("android.intent.action.USER_UNLOCKED");
        this.mContext.registerReceiverAsUser(this.mAllUsersReceiver, UserHandle.ALL, intentFilter, (String) null, (Handler) null);
    }

    public void onNotificationClicked(StatusBarNotification statusBarNotification, ExpandableNotificationRow expandableNotificationRow) {
        Notification notification = statusBarNotification.getNotification();
        PendingIntent pendingIntent = notification.contentIntent;
        if (pendingIntent == null) {
            pendingIntent = notification.fullScreenIntent;
        }
        final PendingIntent pendingIntent2 = pendingIntent;
        boolean isBubble = expandableNotificationRow.getEntry().isBubble();
        if (pendingIntent2 != null || isBubble) {
            final String key = statusBarNotification.getKey();
            HeadsUpManager headsUpManager = this.mHeadsUpManager;
            final boolean z = headsUpManager != null && headsUpManager.isHeadsUp(key);
            boolean z2 = pendingIntent2.isActivity() && PreviewInflater.wouldLaunchResolverActivity(this.mContext, pendingIntent2.getIntent(), this.mCurrentUserId);
            Logger.fullI("NotificationClickHandler", "NotificationClicker onClick notification key=" + key + " afterKeyguardGone:" + z2);
            final ExpandableNotificationRow expandableNotificationRow2 = expandableNotificationRow;
            final StatusBarNotification statusBarNotification2 = statusBarNotification;
            this.mActivityStarter.dismissKeyguardThenExecute(new ActivityStarter.OnDismissAction() {
                public boolean onDismiss() {
                    if (z) {
                        if (MiuiNotificationActivityStarter.this.mPresenter.isPresenterFullyCollapsed()) {
                            HeadsUpManager.setIsClickedNotification(expandableNotificationRow2, true);
                        }
                        MiuiNotificationActivityStarter.this.mHeadsUpManager.releaseImmediately(key);
                    }
                    final ExpandedNotification expandedNotification = null;
                    if (MiuiNotificationActivityStarter.shouldAutoCancel(statusBarNotification2) && MiuiNotificationActivityStarter.this.mGroupManager.isOnlyChildInGroup(statusBarNotification2)) {
                        ExpandedNotification statusBarNotification = MiuiNotificationActivityStarter.this.mGroupManager.getLogicalGroupSummary(statusBarNotification2).getStatusBarNotification();
                        if (MiuiNotificationActivityStarter.shouldAutoCancel(statusBarNotification)) {
                            expandedNotification = statusBarNotification;
                        }
                    }
                    AnonymousClass1 r2 = new Runnable() {
                        public void run() {
                            try {
                                ActivityManagerCompat.getService().resumeAppSwitches();
                            } catch (RemoteException unused) {
                            }
                            if (pendingIntent2.isActivity()) {
                                int identifier = pendingIntent2.getCreatorUserHandle().getIdentifier();
                                if (MiuiNotificationActivityStarter.this.mLockPatternUtils.isSeparateProfileChallengeEnabled(identifier) && MiuiNotificationActivityStarter.this.mKeyguardManager.isDeviceLocked(identifier) && MiuiNotificationActivityStarter.this.mShadeController.startWorkChallengeIfNecessary(identifier, pendingIntent2.getIntentSender(), key)) {
                                    return;
                                }
                            }
                            boolean isBubble = expandableNotificationRow2.getEntry().isBubble();
                            PendingIntent pendingIntent = pendingIntent2;
                            boolean z = pendingIntent != null && pendingIntent.isActivity() && !isBubble;
                            if (!isBubble) {
                                AnonymousClass2 r3 = AnonymousClass2.this;
                                MiuiNotificationActivityStarter.this.startNotificationIntent(pendingIntent2, z, expandableNotificationRow2, z);
                            } else if (Looper.getMainLooper().isCurrentThread()) {
                                MiuiNotificationActivityStarter.this.mBubbleController.expandStackAndSelectBubble(key);
                            } else {
                                MiuiNotificationActivityStarter.this.mMainThreadHandler.post(
                                /*  JADX ERROR: Method code generation error
                                    jadx.core.utils.exceptions.CodegenException: Error generate insn: 0x009f: INVOKE  
                                      (wrap: android.os.Handler : 0x0092: INVOKE  (r1v21 android.os.Handler) = 
                                      (wrap: com.android.systemui.statusbar.phone.MiuiNotificationActivityStarter : 0x0090: IGET  (r1v20 com.android.systemui.statusbar.phone.MiuiNotificationActivityStarter) = 
                                      (wrap: com.android.systemui.statusbar.phone.MiuiNotificationActivityStarter$2 : 0x008e: IGET  (r1v19 com.android.systemui.statusbar.phone.MiuiNotificationActivityStarter$2) = 
                                      (r7v0 'this' com.android.systemui.statusbar.phone.MiuiNotificationActivityStarter$2$1 A[THIS])
                                     com.android.systemui.statusbar.phone.MiuiNotificationActivityStarter.2.1.this$1 com.android.systemui.statusbar.phone.MiuiNotificationActivityStarter$2)
                                     com.android.systemui.statusbar.phone.MiuiNotificationActivityStarter.2.this$0 com.android.systemui.statusbar.phone.MiuiNotificationActivityStarter)
                                     com.android.systemui.statusbar.phone.MiuiNotificationActivityStarter.access$1000(com.android.systemui.statusbar.phone.MiuiNotificationActivityStarter):android.os.Handler type: STATIC)
                                      (wrap: com.android.systemui.statusbar.phone.-$$Lambda$MiuiNotificationActivityStarter$2$1$Jp1UtP23aYdbIzyaXjMan9Pt5uA : 0x009c: CONSTRUCTOR  (r4v1 com.android.systemui.statusbar.phone.-$$Lambda$MiuiNotificationActivityStarter$2$1$Jp1UtP23aYdbIzyaXjMan9Pt5uA) = 
                                      (r7v0 'this' com.android.systemui.statusbar.phone.MiuiNotificationActivityStarter$2$1 A[THIS])
                                      (wrap: java.lang.String : 0x0098: IGET  (r3v5 java.lang.String) = 
                                      (wrap: com.android.systemui.statusbar.phone.MiuiNotificationActivityStarter$2 : 0x0096: IGET  (r3v4 com.android.systemui.statusbar.phone.MiuiNotificationActivityStarter$2) = 
                                      (r7v0 'this' com.android.systemui.statusbar.phone.MiuiNotificationActivityStarter$2$1 A[THIS])
                                     com.android.systemui.statusbar.phone.MiuiNotificationActivityStarter.2.1.this$1 com.android.systemui.statusbar.phone.MiuiNotificationActivityStarter$2)
                                     com.android.systemui.statusbar.phone.MiuiNotificationActivityStarter.2.val$notificationKey java.lang.String)
                                     call: com.android.systemui.statusbar.phone.-$$Lambda$MiuiNotificationActivityStarter$2$1$Jp1UtP23aYdbIzyaXjMan9Pt5uA.<init>(com.android.systemui.statusbar.phone.MiuiNotificationActivityStarter$2$1, java.lang.String):void type: CONSTRUCTOR)
                                     android.os.Handler.post(java.lang.Runnable):boolean type: VIRTUAL in method: com.android.systemui.statusbar.phone.MiuiNotificationActivityStarter.2.1.run():void, dex: classes.dex
                                    	at jadx.core.codegen.InsnGen.makeInsn(InsnGen.java:256)
                                    	at jadx.core.codegen.InsnGen.makeInsn(InsnGen.java:221)
                                    	at jadx.core.codegen.RegionGen.makeSimpleBlock(RegionGen.java:109)
                                    	at jadx.core.codegen.RegionGen.makeRegion(RegionGen.java:55)
                                    	at jadx.core.codegen.RegionGen.makeSimpleRegion(RegionGen.java:92)
                                    	at jadx.core.codegen.RegionGen.makeRegion(RegionGen.java:58)
                                    	at jadx.core.codegen.RegionGen.makeRegionIndent(RegionGen.java:98)
                                    	at jadx.core.codegen.RegionGen.makeIf(RegionGen.java:156)
                                    	at jadx.core.codegen.RegionGen.connectElseIf(RegionGen.java:175)
                                    	at jadx.core.codegen.RegionGen.makeIf(RegionGen.java:152)
                                    	at jadx.core.codegen.RegionGen.makeRegion(RegionGen.java:62)
                                    	at jadx.core.codegen.RegionGen.makeSimpleRegion(RegionGen.java:92)
                                    	at jadx.core.codegen.RegionGen.makeRegion(RegionGen.java:58)
                                    	at jadx.core.codegen.MethodGen.addRegionInsns(MethodGen.java:211)
                                    	at jadx.core.codegen.MethodGen.addInstructions(MethodGen.java:204)
                                    	at jadx.core.codegen.ClassGen.addMethodCode(ClassGen.java:318)
                                    	at jadx.core.codegen.ClassGen.addMethod(ClassGen.java:271)
                                    	at jadx.core.codegen.ClassGen.lambda$addInnerClsAndMethods$2(ClassGen.java:240)
                                    	at java.base/java.util.stream.ForEachOps$ForEachOp$OfRef.accept(ForEachOps.java:183)
                                    	at java.base/java.util.ArrayList.forEach(ArrayList.java:1540)
                                    	at java.base/java.util.stream.SortedOps$RefSortingSink.end(SortedOps.java:395)
                                    	at java.base/java.util.stream.Sink$ChainedReference.end(Sink.java:258)
                                    	at java.base/java.util.stream.AbstractPipeline.copyInto(AbstractPipeline.java:485)
                                    	at java.base/java.util.stream.AbstractPipeline.wrapAndCopyInto(AbstractPipeline.java:474)
                                    	at java.base/java.util.stream.ForEachOps$ForEachOp.evaluateSequential(ForEachOps.java:150)
                                    	at java.base/java.util.stream.ForEachOps$ForEachOp$OfRef.evaluateSequential(ForEachOps.java:173)
                                    	at java.base/java.util.stream.AbstractPipeline.evaluate(AbstractPipeline.java:234)
                                    	at java.base/java.util.stream.ReferencePipeline.forEach(ReferencePipeline.java:497)
                                    	at jadx.core.codegen.ClassGen.addInnerClsAndMethods(ClassGen.java:236)
                                    	at jadx.core.codegen.ClassGen.addClassBody(ClassGen.java:227)
                                    	at jadx.core.codegen.InsnGen.inlineAnonymousConstructor(InsnGen.java:676)
                                    	at jadx.core.codegen.InsnGen.makeConstructor(InsnGen.java:607)
                                    	at jadx.core.codegen.InsnGen.makeInsnBody(InsnGen.java:364)
                                    	at jadx.core.codegen.InsnGen.makeInsn(InsnGen.java:250)
                                    	at jadx.core.codegen.InsnGen.makeInsn(InsnGen.java:221)
                                    	at jadx.core.codegen.RegionGen.makeSimpleBlock(RegionGen.java:109)
                                    	at jadx.core.codegen.RegionGen.makeRegion(RegionGen.java:55)
                                    	at jadx.core.codegen.RegionGen.makeSimpleRegion(RegionGen.java:92)
                                    	at jadx.core.codegen.RegionGen.makeRegion(RegionGen.java:58)
                                    	at jadx.core.codegen.MethodGen.addRegionInsns(MethodGen.java:211)
                                    	at jadx.core.codegen.MethodGen.addInstructions(MethodGen.java:204)
                                    	at jadx.core.codegen.ClassGen.addMethodCode(ClassGen.java:318)
                                    	at jadx.core.codegen.ClassGen.addMethod(ClassGen.java:271)
                                    	at jadx.core.codegen.ClassGen.lambda$addInnerClsAndMethods$2(ClassGen.java:240)
                                    	at java.base/java.util.stream.ForEachOps$ForEachOp$OfRef.accept(ForEachOps.java:183)
                                    	at java.base/java.util.ArrayList.forEach(ArrayList.java:1540)
                                    	at java.base/java.util.stream.SortedOps$RefSortingSink.end(SortedOps.java:395)
                                    	at java.base/java.util.stream.Sink$ChainedReference.end(Sink.java:258)
                                    	at java.base/java.util.stream.AbstractPipeline.copyInto(AbstractPipeline.java:485)
                                    	at java.base/java.util.stream.AbstractPipeline.wrapAndCopyInto(AbstractPipeline.java:474)
                                    	at java.base/java.util.stream.ForEachOps$ForEachOp.evaluateSequential(ForEachOps.java:150)
                                    	at java.base/java.util.stream.ForEachOps$ForEachOp$OfRef.evaluateSequential(ForEachOps.java:173)
                                    	at java.base/java.util.stream.AbstractPipeline.evaluate(AbstractPipeline.java:234)
                                    	at java.base/java.util.stream.ReferencePipeline.forEach(ReferencePipeline.java:497)
                                    	at jadx.core.codegen.ClassGen.addInnerClsAndMethods(ClassGen.java:236)
                                    	at jadx.core.codegen.ClassGen.addClassBody(ClassGen.java:227)
                                    	at jadx.core.codegen.InsnGen.inlineAnonymousConstructor(InsnGen.java:676)
                                    	at jadx.core.codegen.InsnGen.makeConstructor(InsnGen.java:607)
                                    	at jadx.core.codegen.InsnGen.makeInsnBody(InsnGen.java:364)
                                    	at jadx.core.codegen.InsnGen.makeInsn(InsnGen.java:231)
                                    	at jadx.core.codegen.InsnGen.addWrappedArg(InsnGen.java:123)
                                    	at jadx.core.codegen.InsnGen.addArg(InsnGen.java:107)
                                    	at jadx.core.codegen.InsnGen.generateMethodArguments(InsnGen.java:787)
                                    	at jadx.core.codegen.InsnGen.makeInvoke(InsnGen.java:728)
                                    	at jadx.core.codegen.InsnGen.makeInsnBody(InsnGen.java:368)
                                    	at jadx.core.codegen.InsnGen.makeInsn(InsnGen.java:250)
                                    	at jadx.core.codegen.InsnGen.makeInsn(InsnGen.java:221)
                                    	at jadx.core.codegen.RegionGen.makeSimpleBlock(RegionGen.java:109)
                                    	at jadx.core.codegen.RegionGen.makeRegion(RegionGen.java:55)
                                    	at jadx.core.codegen.RegionGen.makeSimpleRegion(RegionGen.java:92)
                                    	at jadx.core.codegen.RegionGen.makeRegion(RegionGen.java:58)
                                    	at jadx.core.codegen.RegionGen.makeRegionIndent(RegionGen.java:98)
                                    	at jadx.core.codegen.RegionGen.makeIf(RegionGen.java:142)
                                    	at jadx.core.codegen.RegionGen.makeRegion(RegionGen.java:62)
                                    	at jadx.core.codegen.RegionGen.makeSimpleRegion(RegionGen.java:92)
                                    	at jadx.core.codegen.RegionGen.makeRegion(RegionGen.java:58)
                                    	at jadx.core.codegen.RegionGen.makeSimpleRegion(RegionGen.java:92)
                                    	at jadx.core.codegen.RegionGen.makeRegion(RegionGen.java:58)
                                    	at jadx.core.codegen.MethodGen.addRegionInsns(MethodGen.java:211)
                                    	at jadx.core.codegen.MethodGen.addInstructions(MethodGen.java:204)
                                    	at jadx.core.codegen.ClassGen.addMethodCode(ClassGen.java:318)
                                    	at jadx.core.codegen.ClassGen.addMethod(ClassGen.java:271)
                                    	at jadx.core.codegen.ClassGen.lambda$addInnerClsAndMethods$2(ClassGen.java:240)
                                    	at java.base/java.util.stream.ForEachOps$ForEachOp$OfRef.accept(ForEachOps.java:183)
                                    	at java.base/java.util.ArrayList.forEach(ArrayList.java:1540)
                                    	at java.base/java.util.stream.SortedOps$RefSortingSink.end(SortedOps.java:395)
                                    	at java.base/java.util.stream.Sink$ChainedReference.end(Sink.java:258)
                                    	at java.base/java.util.stream.AbstractPipeline.copyInto(AbstractPipeline.java:485)
                                    	at java.base/java.util.stream.AbstractPipeline.wrapAndCopyInto(AbstractPipeline.java:474)
                                    	at java.base/java.util.stream.ForEachOps$ForEachOp.evaluateSequential(ForEachOps.java:150)
                                    	at java.base/java.util.stream.ForEachOps$ForEachOp$OfRef.evaluateSequential(ForEachOps.java:173)
                                    	at java.base/java.util.stream.AbstractPipeline.evaluate(AbstractPipeline.java:234)
                                    	at java.base/java.util.stream.ReferencePipeline.forEach(ReferencePipeline.java:497)
                                    	at jadx.core.codegen.ClassGen.addInnerClsAndMethods(ClassGen.java:236)
                                    	at jadx.core.codegen.ClassGen.addClassBody(ClassGen.java:227)
                                    	at jadx.core.codegen.ClassGen.addClassCode(ClassGen.java:112)
                                    	at jadx.core.codegen.ClassGen.makeClass(ClassGen.java:78)
                                    	at jadx.core.codegen.CodeGen.wrapCodeGen(CodeGen.java:44)
                                    	at jadx.core.codegen.CodeGen.generateJavaCode(CodeGen.java:33)
                                    	at jadx.core.codegen.CodeGen.generate(CodeGen.java:21)
                                    	at jadx.core.ProcessClass.generateCode(ProcessClass.java:61)
                                    	at jadx.core.dex.nodes.ClassNode.decompile(ClassNode.java:273)
                                    Caused by: jadx.core.utils.exceptions.CodegenException: Error generate insn: 0x009c: CONSTRUCTOR  (r4v1 com.android.systemui.statusbar.phone.-$$Lambda$MiuiNotificationActivityStarter$2$1$Jp1UtP23aYdbIzyaXjMan9Pt5uA) = 
                                      (r7v0 'this' com.android.systemui.statusbar.phone.MiuiNotificationActivityStarter$2$1 A[THIS])
                                      (wrap: java.lang.String : 0x0098: IGET  (r3v5 java.lang.String) = 
                                      (wrap: com.android.systemui.statusbar.phone.MiuiNotificationActivityStarter$2 : 0x0096: IGET  (r3v4 com.android.systemui.statusbar.phone.MiuiNotificationActivityStarter$2) = 
                                      (r7v0 'this' com.android.systemui.statusbar.phone.MiuiNotificationActivityStarter$2$1 A[THIS])
                                     com.android.systemui.statusbar.phone.MiuiNotificationActivityStarter.2.1.this$1 com.android.systemui.statusbar.phone.MiuiNotificationActivityStarter$2)
                                     com.android.systemui.statusbar.phone.MiuiNotificationActivityStarter.2.val$notificationKey java.lang.String)
                                     call: com.android.systemui.statusbar.phone.-$$Lambda$MiuiNotificationActivityStarter$2$1$Jp1UtP23aYdbIzyaXjMan9Pt5uA.<init>(com.android.systemui.statusbar.phone.MiuiNotificationActivityStarter$2$1, java.lang.String):void type: CONSTRUCTOR in method: com.android.systemui.statusbar.phone.MiuiNotificationActivityStarter.2.1.run():void, dex: classes.dex
                                    	at jadx.core.codegen.InsnGen.makeInsn(InsnGen.java:256)
                                    	at jadx.core.codegen.InsnGen.addWrappedArg(InsnGen.java:123)
                                    	at jadx.core.codegen.InsnGen.addArg(InsnGen.java:107)
                                    	at jadx.core.codegen.InsnGen.generateMethodArguments(InsnGen.java:787)
                                    	at jadx.core.codegen.InsnGen.makeInvoke(InsnGen.java:728)
                                    	at jadx.core.codegen.InsnGen.makeInsnBody(InsnGen.java:368)
                                    	at jadx.core.codegen.InsnGen.makeInsn(InsnGen.java:250)
                                    	... 101 more
                                    Caused by: jadx.core.utils.exceptions.JadxRuntimeException: Expected class to be processed at this point, class: com.android.systemui.statusbar.phone.-$$Lambda$MiuiNotificationActivityStarter$2$1$Jp1UtP23aYdbIzyaXjMan9Pt5uA, state: NOT_LOADED
                                    	at jadx.core.dex.nodes.ClassNode.ensureProcessed(ClassNode.java:260)
                                    	at jadx.core.codegen.InsnGen.makeConstructor(InsnGen.java:606)
                                    	at jadx.core.codegen.InsnGen.makeInsnBody(InsnGen.java:364)
                                    	at jadx.core.codegen.InsnGen.makeInsn(InsnGen.java:231)
                                    	... 107 more
                                    */
                                /*
                                    this = this;
                                    android.app.IActivityManager r0 = android.app.ActivityManagerCompat.getService()     // Catch:{ RemoteException -> 0x0007 }
                                    r0.resumeAppSwitches()     // Catch:{ RemoteException -> 0x0007 }
                                L_0x0007:
                                    com.android.systemui.statusbar.phone.MiuiNotificationActivityStarter$2 r0 = com.android.systemui.statusbar.phone.MiuiNotificationActivityStarter.AnonymousClass2.this
                                    android.app.PendingIntent r0 = r8
                                    boolean r0 = r0.isActivity()
                                    if (r0 == 0) goto L_0x0054
                                    com.android.systemui.statusbar.phone.MiuiNotificationActivityStarter$2 r0 = com.android.systemui.statusbar.phone.MiuiNotificationActivityStarter.AnonymousClass2.this
                                    android.app.PendingIntent r0 = r8
                                    android.os.UserHandle r0 = r0.getCreatorUserHandle()
                                    int r0 = r0.getIdentifier()
                                    com.android.systemui.statusbar.phone.MiuiNotificationActivityStarter$2 r1 = com.android.systemui.statusbar.phone.MiuiNotificationActivityStarter.AnonymousClass2.this
                                    com.android.systemui.statusbar.phone.MiuiNotificationActivityStarter r1 = com.android.systemui.statusbar.phone.MiuiNotificationActivityStarter.this
                                    com.android.internal.widget.LockPatternUtils r1 = r1.mLockPatternUtils
                                    boolean r1 = r1.isSeparateProfileChallengeEnabled(r0)
                                    if (r1 == 0) goto L_0x0054
                                    com.android.systemui.statusbar.phone.MiuiNotificationActivityStarter$2 r1 = com.android.systemui.statusbar.phone.MiuiNotificationActivityStarter.AnonymousClass2.this
                                    com.android.systemui.statusbar.phone.MiuiNotificationActivityStarter r1 = com.android.systemui.statusbar.phone.MiuiNotificationActivityStarter.this
                                    android.app.KeyguardManager r1 = r1.mKeyguardManager
                                    boolean r1 = r1.isDeviceLocked(r0)
                                    if (r1 == 0) goto L_0x0054
                                    com.android.systemui.statusbar.phone.MiuiNotificationActivityStarter$2 r1 = com.android.systemui.statusbar.phone.MiuiNotificationActivityStarter.AnonymousClass2.this
                                    com.android.systemui.statusbar.phone.MiuiNotificationActivityStarter r1 = com.android.systemui.statusbar.phone.MiuiNotificationActivityStarter.this
                                    com.android.systemui.statusbar.phone.ShadeController r1 = r1.mShadeController
                                    com.android.systemui.statusbar.phone.MiuiNotificationActivityStarter$2 r2 = com.android.systemui.statusbar.phone.MiuiNotificationActivityStarter.AnonymousClass2.this
                                    android.app.PendingIntent r2 = r8
                                    android.content.IntentSender r2 = r2.getIntentSender()
                                    com.android.systemui.statusbar.phone.MiuiNotificationActivityStarter$2 r3 = com.android.systemui.statusbar.phone.MiuiNotificationActivityStarter.AnonymousClass2.this
                                    java.lang.String r3 = r6
                                    boolean r0 = r1.startWorkChallengeIfNecessary(r0, r2, r3)
                                    if (r0 == 0) goto L_0x0054
                                    return
                                L_0x0054:
                                    com.android.systemui.statusbar.phone.MiuiNotificationActivityStarter$2 r0 = com.android.systemui.statusbar.phone.MiuiNotificationActivityStarter.AnonymousClass2.this
                                    com.android.systemui.statusbar.ExpandableNotificationRow r0 = r5
                                    com.android.systemui.statusbar.NotificationData$Entry r0 = r0.getEntry()
                                    boolean r0 = r0.isBubble()
                                    com.android.systemui.statusbar.phone.MiuiNotificationActivityStarter$2 r1 = com.android.systemui.statusbar.phone.MiuiNotificationActivityStarter.AnonymousClass2.this
                                    android.app.PendingIntent r1 = r8
                                    r2 = 1
                                    if (r1 == 0) goto L_0x0071
                                    boolean r1 = r1.isActivity()
                                    if (r1 == 0) goto L_0x0071
                                    if (r0 != 0) goto L_0x0071
                                    r1 = r2
                                    goto L_0x0072
                                L_0x0071:
                                    r1 = 0
                                L_0x0072:
                                    if (r0 == 0) goto L_0x00a3
                                    android.os.Looper r1 = android.os.Looper.getMainLooper()
                                    boolean r1 = r1.isCurrentThread()
                                    if (r1 == 0) goto L_0x008e
                                    com.android.systemui.statusbar.phone.MiuiNotificationActivityStarter$2 r1 = com.android.systemui.statusbar.phone.MiuiNotificationActivityStarter.AnonymousClass2.this
                                    com.android.systemui.statusbar.phone.MiuiNotificationActivityStarter r1 = com.android.systemui.statusbar.phone.MiuiNotificationActivityStarter.this
                                    com.android.systemui.bubbles.BubbleController r1 = r1.mBubbleController
                                    com.android.systemui.statusbar.phone.MiuiNotificationActivityStarter$2 r3 = com.android.systemui.statusbar.phone.MiuiNotificationActivityStarter.AnonymousClass2.this
                                    java.lang.String r3 = r6
                                    r1.expandStackAndSelectBubble(r3)
                                    goto L_0x00b0
                                L_0x008e:
                                    com.android.systemui.statusbar.phone.MiuiNotificationActivityStarter$2 r1 = com.android.systemui.statusbar.phone.MiuiNotificationActivityStarter.AnonymousClass2.this
                                    com.android.systemui.statusbar.phone.MiuiNotificationActivityStarter r1 = com.android.systemui.statusbar.phone.MiuiNotificationActivityStarter.this
                                    android.os.Handler r1 = r1.mMainThreadHandler
                                    com.android.systemui.statusbar.phone.MiuiNotificationActivityStarter$2 r3 = com.android.systemui.statusbar.phone.MiuiNotificationActivityStarter.AnonymousClass2.this
                                    java.lang.String r3 = r6
                                    com.android.systemui.statusbar.phone.-$$Lambda$MiuiNotificationActivityStarter$2$1$Jp1UtP23aYdbIzyaXjMan9Pt5uA r4 = new com.android.systemui.statusbar.phone.-$$Lambda$MiuiNotificationActivityStarter$2$1$Jp1UtP23aYdbIzyaXjMan9Pt5uA
                                    r4.<init>(r7, r3)
                                    r1.post(r4)
                                    goto L_0x00b0
                                L_0x00a3:
                                    com.android.systemui.statusbar.phone.MiuiNotificationActivityStarter$2 r3 = com.android.systemui.statusbar.phone.MiuiNotificationActivityStarter.AnonymousClass2.this
                                    com.android.systemui.statusbar.phone.MiuiNotificationActivityStarter r4 = com.android.systemui.statusbar.phone.MiuiNotificationActivityStarter.this
                                    android.app.PendingIntent r5 = r8
                                    boolean r6 = r4
                                    com.android.systemui.statusbar.ExpandableNotificationRow r3 = r5
                                    r4.startNotificationIntent(r5, r6, r3, r1)
                                L_0x00b0:
                                    com.android.systemui.statusbar.phone.MiuiNotificationActivityStarter$2 r1 = com.android.systemui.statusbar.phone.MiuiNotificationActivityStarter.AnonymousClass2.this
                                    android.app.PendingIntent r1 = r8
                                    boolean r1 = r1.isActivity()
                                    if (r1 != 0) goto L_0x00bc
                                    if (r0 == 0) goto L_0x00c7
                                L_0x00bc:
                                    com.android.systemui.statusbar.phone.MiuiNotificationActivityStarter$2 r0 = com.android.systemui.statusbar.phone.MiuiNotificationActivityStarter.AnonymousClass2.this
                                    com.android.systemui.statusbar.phone.MiuiNotificationActivityStarter r0 = com.android.systemui.statusbar.phone.MiuiNotificationActivityStarter.this
                                    com.android.systemui.assist.AssistManager r0 = r0.mAssistManager
                                    r0.hideAssist()
                                L_0x00c7:
                                    com.android.systemui.statusbar.phone.MiuiNotificationActivityStarter$2 r0 = com.android.systemui.statusbar.phone.MiuiNotificationActivityStarter.AnonymousClass2.this     // Catch:{ Exception -> 0x00fe }
                                    com.android.systemui.statusbar.phone.MiuiNotificationActivityStarter r0 = com.android.systemui.statusbar.phone.MiuiNotificationActivityStarter.this     // Catch:{ Exception -> 0x00fe }
                                    com.android.systemui.statusbar.NotificationData r0 = r0.mNotificationData     // Catch:{ Exception -> 0x00fe }
                                    com.android.systemui.statusbar.phone.MiuiNotificationActivityStarter$2 r1 = com.android.systemui.statusbar.phone.MiuiNotificationActivityStarter.AnonymousClass2.this     // Catch:{ Exception -> 0x00fe }
                                    java.lang.String r1 = r6     // Catch:{ Exception -> 0x00fe }
                                    int r0 = r0.getRank(r1)     // Catch:{ Exception -> 0x00fe }
                                    com.android.systemui.statusbar.phone.MiuiNotificationActivityStarter$2 r1 = com.android.systemui.statusbar.phone.MiuiNotificationActivityStarter.AnonymousClass2.this     // Catch:{ Exception -> 0x00fe }
                                    com.android.systemui.statusbar.phone.MiuiNotificationActivityStarter r1 = com.android.systemui.statusbar.phone.MiuiNotificationActivityStarter.this     // Catch:{ Exception -> 0x00fe }
                                    com.android.systemui.statusbar.NotificationData r1 = r1.mNotificationData     // Catch:{ Exception -> 0x00fe }
                                    java.util.ArrayList r1 = r1.getActiveNotifications()     // Catch:{ Exception -> 0x00fe }
                                    int r1 = r1.size()     // Catch:{ Exception -> 0x00fe }
                                    com.android.systemui.statusbar.phone.MiuiNotificationActivityStarter$2 r3 = com.android.systemui.statusbar.phone.MiuiNotificationActivityStarter.AnonymousClass2.this     // Catch:{ Exception -> 0x00fe }
                                    java.lang.String r3 = r6     // Catch:{ Exception -> 0x00fe }
                                    com.android.internal.statusbar.NotificationVisibility r0 = com.android.internal.statusbar.NotificationVisibilityCompat.obtain(r3, r0, r1, r2)     // Catch:{ Exception -> 0x00fe }
                                    com.android.systemui.statusbar.phone.MiuiNotificationActivityStarter$2 r1 = com.android.systemui.statusbar.phone.MiuiNotificationActivityStarter.AnonymousClass2.this     // Catch:{ Exception -> 0x00fe }
                                    com.android.systemui.statusbar.phone.MiuiNotificationActivityStarter r1 = com.android.systemui.statusbar.phone.MiuiNotificationActivityStarter.this     // Catch:{ Exception -> 0x00fe }
                                    com.android.internal.statusbar.IStatusBarService r1 = r1.mBarService     // Catch:{ Exception -> 0x00fe }
                                    com.android.systemui.statusbar.phone.MiuiNotificationActivityStarter$2 r2 = com.android.systemui.statusbar.phone.MiuiNotificationActivityStarter.AnonymousClass2.this     // Catch:{ Exception -> 0x00fe }
                                    java.lang.String r2 = r6     // Catch:{ Exception -> 0x00fe }
                                    com.android.internal.statusbar.StatusBarServiceCompat.onNotificationClick(r1, r2, r0)     // Catch:{ Exception -> 0x00fe }
                                L_0x00fe:
                                    com.android.systemui.miui.statusbar.ExpandedNotification r0 = r0
                                    if (r0 == 0) goto L_0x0112
                                    com.android.systemui.statusbar.phone.MiuiNotificationActivityStarter$2 r0 = com.android.systemui.statusbar.phone.MiuiNotificationActivityStarter.AnonymousClass2.this
                                    com.android.systemui.statusbar.phone.MiuiNotificationActivityStarter r0 = com.android.systemui.statusbar.phone.MiuiNotificationActivityStarter.this
                                    android.os.Handler r0 = r0.mMainThreadHandler
                                    com.android.systemui.statusbar.phone.MiuiNotificationActivityStarter$2$1$1 r1 = new com.android.systemui.statusbar.phone.MiuiNotificationActivityStarter$2$1$1
                                    r1.<init>()
                                    r0.post(r1)
                                L_0x0112:
                                    return
                                */
                                throw new UnsupportedOperationException("Method not decompiled: com.android.systemui.statusbar.phone.MiuiNotificationActivityStarter.AnonymousClass2.AnonymousClass1.run():void");
                            }

                            /* access modifiers changed from: private */
                            /* renamed from: lambda$run$0 */
                            public /* synthetic */ void lambda$run$0$MiuiNotificationActivityStarter$2$1(String str) {
                                MiuiNotificationActivityStarter.this.mBubbleController.expandStackAndSelectBubble(str);
                            }
                        };
                        if (MiuiKeyguardUtils.isUserUnlocked(MiuiNotificationActivityStarter.this.mContext) || KeyguardUpdateMonitor.getInstance(MiuiNotificationActivityStarter.this.mContext).isUserUnlocked()) {
                            ((UiOffloadThread) Dependency.get(UiOffloadThread.class)).submit(r2);
                        } else {
                            Runnable unused = MiuiNotificationActivityStarter.this.mRunnable = r2;
                        }
                        if (MiuiNotificationActivityStarter.this.shouldCollapse()) {
                            MiuiNotificationActivityStarter.this.collapseOnMainThread();
                        }
                        if (!MiuiKeyguardUtils.isDefaultLockScreenTheme()) {
                            MiuiNotificationActivityStarter.this.mMainThreadHandler.postDelayed(new Runnable() {
                                public void run() {
                                    MiuiNotificationActivityStarter.this.mShadeController.readyForKeyguardDone();
                                }
                            }, 400);
                        }
                        MiuiNotificationActivityStarter.this.mShadeController.visibilityChanged(false);
                        return true;
                    }
                }, (Runnable) null, z2);
                return;
            }
            Logger.fullE("NotificationClickHandler", "onNotificationClicked called for non-clickable notification!");
        }

        /* access modifiers changed from: private */
        public void startNotificationIntent(PendingIntent pendingIntent, boolean z, ExpandableNotificationRow expandableNotificationRow, boolean z2) {
            Bundle bundle;
            ActivityOptions activityOptions = MiuiMultiWindowUtils.getActivityOptions(this.mContext, pendingIntent.getCreatorPackage());
            boolean z3 = activityOptions != null;
            if (!z || z3) {
                if (activityOptions != null) {
                    try {
                        bundle = activityOptions.toBundle();
                    } catch (PendingIntent.CanceledException e) {
                        Logger.fullW("NotificationClickHandler", "Sending contentIntent failed: " + e);
                        return;
                    }
                } else {
                    bundle = StatusBar.getActivityOptions();
                }
                pendingIntent.send((Context) null, 0, (Intent) null, (PendingIntent.OnFinished) null, (Handler) null, (String) null, bundle);
                Logger.fullI("NotificationClickHandler", "click notification, sending intent, key=" + expandableNotificationRow.getStatusBarNotification().getKey());
                return;
            }
            startHeadsUpNotificationIntent(pendingIntent, expandableNotificationRow, z2);
        }

        private void startHeadsUpNotificationIntent(PendingIntent pendingIntent, ExpandableNotificationRow expandableNotificationRow, boolean z) {
            RemoteAnimationAdapter launchAnimation = this.mActivityLaunchAnimator.getLaunchAnimation(expandableNotificationRow, false);
            if (launchAnimation != null) {
                try {
                    ActivityTaskManagerCompat.registerRemoteAnimationForNextActivityStart(pendingIntent.getCreatorPackage(), launchAnimation);
                } catch (PendingIntent.CanceledException | RemoteException e) {
                    Log.w("NotificationClickHandler", "Sending contentIntent failed: " + e);
                    return;
                }
            }
            this.mActivityLaunchAnimator.setLaunchResult(pendingIntent.sendAndReturnResult(this.mContext, 0, (Intent) null, (PendingIntent.OnFinished) null, (Handler) null, (String) null, StatusBar.getActivityOptions(launchAnimation)), z);
        }

        /* access modifiers changed from: private */
        public void collapseOnMainThread() {
            if (Looper.getMainLooper().isCurrentThread()) {
                this.mShadeController.animateCollapsePanels(2, true);
                return;
            }
            Handler handler = this.mMainThreadHandler;
            ShadeController shadeController = this.mShadeController;
            Objects.requireNonNull(shadeController);
            handler.post(new Runnable() {
                public final void run() {
                    ShadeController.this.collapsePanel();
                }
            });
        }

        /* access modifiers changed from: private */
        public boolean shouldCollapse() {
            return ((StatusBarStateController) Dependency.get(StatusBarStateController.class)).getState() != 0 || !this.mActivityLaunchAnimator.isAnimationPending();
        }

        /* access modifiers changed from: private */
        public static boolean shouldAutoCancel(StatusBarNotification statusBarNotification) {
            int i = statusBarNotification.getNotification().flags;
            return (i & 16) == 16 && (i & 64) == 0;
        }
    }
