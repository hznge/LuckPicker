package com.hznge.luckpicker.services;

import android.accessibilityservice.AccessibilityService;
import android.util.Log;
import android.view.accessibility.AccessibilityEvent;
import android.view.accessibility.AccessibilityNodeInfo;

import java.util.ArrayList;
import java.util.List;

public class QQWallerService extends AccessibilityService {
    private static final String TAG = "QQWallerService";

    private static final String QQWALLER = "QWalletPluginProxyActivity";

    private static final String QQCALL = "SplashActivity";

    private static final String QQ_OPEN_EN = "Open";

    private static final String QQ_OPENED_EN = "You've opened";

    private static final String QQ_DEFAULT_CLICK_OPEN = "点击拆开";

    private static final String QQ_HONG_BAO_PASSWORD = "口令红包";

    private static final String QQ_CLICK_TO_PASTE_PASSWORD = "点击输入口令";
    private static final int MAX_CACHE_TOLERANCE = 5000;
    private boolean mLuckyMoneyReceived;
    private String lastFetchedHongbaoId = null;
    private long lastFetchedTime = 0;
    private AccessibilityNodeInfo rootNodeInfo;

    private List<AccessibilityNodeInfo> mReceiveNode;


    public void recycle(AccessibilityNodeInfo info) {
        if (info.getChildCount() == 0) {
            if (info.getText() != null && info.getText().toString().equals(QQ_CLICK_TO_PASTE_PASSWORD)) {
                info.getParent().performAction(AccessibilityNodeInfo.ACTION_CLICK);
            }

            if (info.getClassName().toString().equals("android.widget.Button") && info.getText()
                    .toString().equals("发送")) {
                info.performAction(AccessibilityNodeInfo.ACTION_CLICK);
            }
        } else {
            for (int i = 0; i < info.getChildCount(); i++) {
                if (info.getChild(i) != null) {
                    recycle(info.getChild(i));
                }
            }
        }
    }

    @Override
    public void onAccessibilityEvent(AccessibilityEvent event) {
        this.rootNodeInfo = event.getSource();
        if (rootNodeInfo == null) {
            return;
        }
        mReceiveNode = null;
        checkNodeInfo();

        if (mLuckyMoneyReceived && (mReceiveNode != null)) {
            int size = mReceiveNode.size();
            if (size > 0) {
                String id = getHongbaoText(mReceiveNode.get(size - 1));
                long now = System.currentTimeMillis();
                if (this.shouldReturn(id, now - lastFetchedTime)) {
                    return;
                }
                lastFetchedHongbaoId = id;
                lastFetchedTime = now;
                AccessibilityNodeInfo cellNode = mReceiveNode.get(size - 1);

                if (cellNode.getText().toString().equals("口令红包已拆开")) {
                    return;
                }

                cellNode.getParent().performAction(AccessibilityNodeInfo.ACTION_CLICK);

                if (cellNode.getText().toString().equals(QQ_HONG_BAO_PASSWORD)) {
                    AccessibilityNodeInfo rowNode = getRootInActiveWindow();
                    if (rowNode == null) {
                        Log.e(TAG, "onAccessibilityEvent: rootInfo is null");
                        return;
                    } else {
                        recycle(rowNode);
                    }
                }
                mLuckyMoneyReceived = true;
            }
        }
    }

    private void checkNodeInfo() {
        if (rootNodeInfo == null) {
            return;
        }

        List<AccessibilityNodeInfo> node1 = this.findAccessibilityNodeInfosByText(this.rootNodeInfo,
                new String[]{
                        QQ_DEFAULT_CLICK_OPEN, QQ_HONG_BAO_PASSWORD, QQ_CLICK_TO_PASTE_PASSWORD, "发送"
                });

        if (!node1.isEmpty()) {
            String nodeId = Integer.toHexString(System.identityHashCode(this.rootNodeInfo));
            if (!nodeId.equals(lastFetchedHongbaoId)) {
                mLuckyMoneyReceived = true;
                mReceiveNode = node1;
            }

            return;
        }
    }

    private String getHongbaoText(AccessibilityNodeInfo node) {
        String content;
        try {
            AccessibilityNodeInfo i = node.getParent().getChild(0);
            content = i.getText().toString();
        } catch (NullPointerException e) {
            return null;
        }

        return content;
    }

    private boolean shouldReturn(String id, long duration) {
        if (id == null) {
            return true;
        }
        if (duration < MAX_CACHE_TOLERANCE && id.equals(lastFetchedHongbaoId)) {
            return true;
        }

        return false;
    }

    private List<AccessibilityNodeInfo> findAccessibilityNodeInfosByText(AccessibilityNodeInfo nodeInfo, String[] texts) {
        for (String text : texts) {
            if (text == null) {
                continue;
            }
            List<AccessibilityNodeInfo> nodes = nodeInfo.findAccessibilityNodeInfosByText(text);
            if (!nodes.isEmpty()) {
                if (text.equals(QQ_OPEN_EN) && !nodeInfo.findAccessibilityNodeInfosByText(QQ_OPENED_EN).isEmpty()) {
                    continue;
                }

                return nodes;
            }
        }
        return new ArrayList<>();
    }

    @Override
    public void onInterrupt() {

    }
}
