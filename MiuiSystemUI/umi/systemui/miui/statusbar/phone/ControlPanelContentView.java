package com.android.systemui.miui.statusbar.phone;

import android.content.Context;
import android.content.res.Configuration;
import android.util.AttributeSet;
import android.view.View;
import android.widget.FrameLayout;
import android.widget.ImageView;
import com.android.systemui.Dependency;
import com.android.systemui.miui.controlcenter.ExpandInfoController;
import com.android.systemui.miui.controlcenter.QSControlCenterPanel;
import com.android.systemui.miui.controlcenter.QSControlDetail;
import com.android.systemui.miui.controlcenter.QSControlTileHost;
import com.android.systemui.miui.controlcenter.customize.QSControlCustomizer;
import com.android.systemui.miui.statusbar.analytics.SystemUIStat;
import com.android.systemui.plugins.R;
import com.android.systemui.qs.QSPanel;

public class ControlPanelContentView extends FrameLayout {
    private Context mContext;
    private ControlPanelWindowManager mControlPanelWindowManager;
    private QSControlCustomizer.QSControlPanelCallback mCustomizerCallback = null;
    private QSControlDetail mDetail;
    private ExpandInfoController mExpandInfoController;
    private int mOrientation;
    private QSControlCustomizer mQSCustomizer;
    private QSControlCenterPanel mQsControlCenterPanel;
    private ImageView mTilesEdit;

    public ControlPanelContentView(Context context) {
        super(context, (AttributeSet) null);
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
        this.mDetail = (QSControlDetail) findViewById(R.id.qs_detail);
        QSControlCenterPanel qSControlCenterPanel = (QSControlCenterPanel) findViewById(R.id.qs_control_center_panel);
        this.mQsControlCenterPanel = qSControlCenterPanel;
        qSControlCenterPanel.setControlPanelContentView(this);
        QSControlCustomizer qSControlCustomizer = (QSControlCustomizer) findViewById(R.id.qs_customize);
        this.mQSCustomizer = qSControlCustomizer;
        qSControlCustomizer.setQSControlCenterPanel(this);
        ImageView imageView = (ImageView) findViewById(R.id.tiles_edit);
        this.mTilesEdit = imageView;
        imageView.setOnClickListener(new View.OnClickListener() {
            public void onClick(View view) {
                ((SystemUIStat) Dependency.get(SystemUIStat.class)).handleControlCenterEvent("event_quick_tiles_edit");
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
        this.mQsControlCenterPanel.onOrientationChanged(this.mOrientation, false);
    }

    public void updateResources() {
        this.mQsControlCenterPanel.updateResources();
        this.mDetail.updateResources();
        this.mQSCustomizer.updateResources();
    }

    public void setControlPanelWindowManager(ControlPanelWindowManager controlPanelWindowManager) {
        this.mControlPanelWindowManager = controlPanelWindowManager;
    }

    public void setControlPanelWindowBlurRatio(float f) {
        this.mControlPanelWindowManager.setBlurRatio(f);
    }

    public void showExpandDetail(boolean z, QSPanel.Record record) {
        this.mQsControlCenterPanel.showDetail(z, record);
    }

    public boolean isDetailShowing() {
        return this.mDetail.isShowingDetail();
    }

    public boolean isEditShowing() {
        return this.mQSCustomizer.isShown();
    }

    public QSControlDetail getDetailView() {
        return this.mDetail;
    }

    public void setHost(QSControlTileHost qSControlTileHost) {
        this.mQSCustomizer.setHost(qSControlTileHost);
        this.mQsControlCenterPanel.setHost(qSControlTileHost);
    }

    public void showContent() {
        ((SystemUIStat) Dependency.get(SystemUIStat.class)).handleControlCenterEvent("expand_panel");
        setVisibility(0);
        this.mExpandInfoController.requestData();
        QSControlCenterPanel qSControlCenterPanel = this.mQsControlCenterPanel;
        if (qSControlCenterPanel != null) {
            qSControlCenterPanel.setExpand(true, true);
            this.mQsControlCenterPanel.addControlsPlugin();
        }
    }

    public void hideContent() {
        QSControlCenterPanel qSControlCenterPanel = this.mQsControlCenterPanel;
        if (qSControlCenterPanel != null) {
            qSControlCenterPanel.setExpand(false, true);
            this.mQsControlCenterPanel.removeControlsPlugin();
        }
    }

    public void showEdit() {
        QSControlCustomizer.QSControlPanelCallback qSControlPanelCallback = this.mCustomizerCallback;
        if (qSControlPanelCallback != null) {
            qSControlPanelCallback.show();
        }
    }

    public void updateTransHeight(float f) {
        this.mQsControlCenterPanel.updateTransHeight(f);
    }

    public void hideEdit() {
        QSControlCustomizer.QSControlPanelCallback qSControlPanelCallback = this.mCustomizerCallback;
        if (qSControlPanelCallback != null) {
            qSControlPanelCallback.hide();
        }
    }

    public View getControlCenterPanel() {
        return this.mQsControlCenterPanel;
    }

    public void setQSCustomizerCallback(QSControlCustomizer.QSControlPanelCallback qSControlPanelCallback) {
        this.mCustomizerCallback = qSControlPanelCallback;
    }
}
