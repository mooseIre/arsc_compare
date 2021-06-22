package com.android.systemui.controlcenter.phone;

import android.content.Context;
import android.content.res.Configuration;
import android.util.AttributeSet;
import android.view.View;
import android.widget.FrameLayout;
import android.widget.ImageView;
import com.android.systemui.C0015R$id;
import com.android.systemui.Dependency;
import com.android.systemui.controlcenter.phone.controls.ControlsEditController;
import com.android.systemui.controlcenter.phone.controls.ControlsPluginManager;
import com.android.systemui.controlcenter.phone.customize.QSControlCustomizer;
import com.android.systemui.controlcenter.phone.detail.QSControlDetail;
import com.android.systemui.controlcenter.phone.widget.ControlCenterBrightnessView;
import com.android.systemui.controlcenter.phone.widget.MiuiQSPanel$MiuiRecord;
import com.android.systemui.qs.QSTileHost;
import com.miui.systemui.analytics.SystemUIStat;
import com.miui.systemui.events.ExpandPanelEvent;
import com.miui.systemui.events.QuickTilesEditEvent;

public class ControlPanelContentView extends FrameLayout {
    private Context mContext;
    private ControlCenterBrightnessView mControlCenterBrightnessView;
    private ControlCenterPanelView mControlCenterPanelView;
    private ControlPanelWindowManager mControlPanelWindowManager;
    private ControlsEditController mControlsEditController;
    private Runnable mControlsEditRunnable = new Runnable() {
        /* class com.android.systemui.controlcenter.phone.ControlPanelContentView.AnonymousClass2 */

        public void run() {
            if (ControlPanelContentView.this.mControlsEditController == null) {
                ControlPanelContentView controlPanelContentView = ControlPanelContentView.this;
                ControlPanelContentView controlPanelContentView2 = ControlPanelContentView.this;
                controlPanelContentView.mControlsEditController = new ControlsEditController((ControlsPluginManager) Dependency.get(ControlsPluginManager.class), controlPanelContentView2, controlPanelContentView2.mControlCenterPanelView);
            }
            ControlPanelContentView.this.mControlsEditController.addControlsEditView();
        }
    };
    private QSControlCustomizer.QSControlPanelCallback mCustomizerCallback = null;
    private QSControlDetail mDetail;
    private ExpandInfoController mExpandInfoController;
    private int mOrientation;
    private QSControlCustomizer mQSCustomizer;
    private ImageView mTilesEdit;

    public ControlPanelContentView(Context context) {
        super(context, null);
    }

    public ControlPanelContentView(Context context, AttributeSet attributeSet) {
        super(context, attributeSet);
        this.mContext = context;
    }

    /* access modifiers changed from: protected */
    public void onFinishInflate() {
        super.onFinishInflate();
        ExpandInfoController expandInfoController = (ExpandInfoController) Dependency.get(ExpandInfoController.class);
        this.mExpandInfoController = expandInfoController;
        expandInfoController.setContentView(this);
        this.mDetail = (QSControlDetail) findViewById(C0015R$id.qs_detail);
        ControlCenterPanelView controlCenterPanelView = (ControlCenterPanelView) findViewById(C0015R$id.control_center_panel);
        this.mControlCenterPanelView = controlCenterPanelView;
        controlCenterPanelView.setControlPanelContentView(this);
        QSControlCustomizer qSControlCustomizer = (QSControlCustomizer) findViewById(C0015R$id.qs_customize);
        this.mQSCustomizer = qSControlCustomizer;
        qSControlCustomizer.setQSControlCenterPanel(this);
        this.mControlCenterBrightnessView = (ControlCenterBrightnessView) findViewById(C0015R$id.brightness_container);
        ImageView imageView = (ImageView) findViewById(C0015R$id.tiles_edit);
        this.mTilesEdit = imageView;
        imageView.setOnClickListener(new View.OnClickListener() {
            /* class com.android.systemui.controlcenter.phone.ControlPanelContentView.AnonymousClass1 */

            public void onClick(View view) {
                ((SystemUIStat) Dependency.get(SystemUIStat.class)).handleControlCenterEvent(new QuickTilesEditEvent());
                ControlPanelContentView.this.showEdit();
            }
        });
        setVisibility(4);
        this.mOrientation = this.mContext.getResources().getConfiguration().orientation;
        updateLayout();
    }

    /* access modifiers changed from: protected */
    public void onConfigurationChanged(Configuration configuration) {
        super.onConfigurationChanged(configuration);
        int i = configuration.orientation;
        if (i != this.mOrientation) {
            this.mOrientation = i;
            updateLayout();
        }
    }

    private void updateLayout() {
        if (this.mQSCustomizer.isShown() && this.mOrientation == 2) {
            hideEdit();
        }
        ControlsEditController controlsEditController = this.mControlsEditController;
        if (controlsEditController != null && controlsEditController.isShown() && this.mOrientation == 2) {
            hideControlEdit();
        }
    }

    public void updateResources() {
        this.mDetail.updateResources();
        this.mQSCustomizer.updateResources();
    }

    public void setControlPanelWindowManager(ControlPanelWindowManager controlPanelWindowManager) {
        this.mControlPanelWindowManager = controlPanelWindowManager;
    }

    public void setControlPanelWindowBlurRatio(float f) {
        this.mControlPanelWindowManager.setBlurRatio(f);
    }

    public void showExpandDetail(boolean z, MiuiQSPanel$MiuiRecord miuiQSPanel$MiuiRecord) {
        this.mControlCenterPanelView.showDetail(z, miuiQSPanel$MiuiRecord);
    }

    public boolean isDetailShowing() {
        return this.mDetail.isShowingDetail();
    }

    public boolean isEditShowing() {
        return this.mQSCustomizer.isShown();
    }

    public boolean isControlEditShowing() {
        ControlsEditController controlsEditController = this.mControlsEditController;
        return controlsEditController != null && controlsEditController.isShown();
    }

    public boolean isMirrorShowing() {
        return this.mControlCenterBrightnessView.isMirrorShowing();
    }

    public QSControlDetail getDetailView() {
        return this.mDetail;
    }

    public void setHost(QSTileHost qSTileHost) {
        this.mQSCustomizer.setHost(qSTileHost);
        this.mControlCenterPanelView.setHost(qSTileHost);
    }

    public void showContent() {
        ((SystemUIStat) Dependency.get(SystemUIStat.class)).handleControlCenterEvent(new ExpandPanelEvent());
        setVisibility(0);
        this.mExpandInfoController.requestData();
        ControlCenterPanelView controlCenterPanelView = this.mControlCenterPanelView;
        if (controlCenterPanelView != null) {
            controlCenterPanelView.showPanel(true, true);
        }
        post(new Runnable() {
            /* class com.android.systemui.controlcenter.phone.$$Lambda$ControlPanelContentView$Hr1R5n0YYh0JFrdOBbPUQfRThM4 */

            public final void run() {
                ControlPanelContentView.this.lambda$showContent$0$ControlPanelContentView();
            }
        });
        removeCallbacks(this.mControlsEditRunnable);
        postDelayed(this.mControlsEditRunnable, 1000);
    }

    /* access modifiers changed from: private */
    /* renamed from: lambda$showContent$0 */
    public /* synthetic */ void lambda$showContent$0$ControlPanelContentView() {
        ControlCenterPanelView controlCenterPanelView = this.mControlCenterPanelView;
        if (controlCenterPanelView != null) {
            controlCenterPanelView.addControlsPlugin();
        }
    }

    public void hideContent() {
        removeCallbacks(this.mControlsEditRunnable);
        ControlCenterPanelView controlCenterPanelView = this.mControlCenterPanelView;
        if (controlCenterPanelView != null) {
            controlCenterPanelView.showPanel(false, true);
        }
        ControlsEditController controlsEditController = this.mControlsEditController;
        if (controlsEditController != null) {
            controlsEditController.removeControlsEditView();
        }
    }

    public void showEdit() {
        QSControlCustomizer.QSControlPanelCallback qSControlPanelCallback = this.mCustomizerCallback;
        if (qSControlPanelCallback != null) {
            qSControlPanelCallback.show();
        }
    }

    public void updateTransHeight(float f) {
        this.mControlCenterPanelView.updateTransHeight(f);
    }

    public void hideEdit() {
        QSControlCustomizer.QSControlPanelCallback qSControlPanelCallback = this.mCustomizerCallback;
        if (qSControlPanelCallback != null) {
            qSControlPanelCallback.hide();
        }
    }

    public void hideControlEdit() {
        ControlsEditController controlsEditController = this.mControlsEditController;
        if (controlsEditController != null) {
            controlsEditController.hideEditPanel();
        }
    }

    public View getControlCenterPanel() {
        return this.mControlCenterPanelView;
    }

    public void setQSCustomizerCallback(QSControlCustomizer.QSControlPanelCallback qSControlPanelCallback) {
        this.mCustomizerCallback = qSControlPanelCallback;
    }
}
