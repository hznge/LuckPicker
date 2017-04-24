package com.hznge.luckpicker.services;

import android.accessibilityservice.AccessibilityService;
import android.app.KeyguardManager;
import android.app.Notification;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.os.PowerManager;
import android.text.TextUtils;
import android.util.Log;
import android.view.accessibility.AccessibilityEvent;
import android.view.accessibility.AccessibilityNodeInfo;
import android.widget.Toast;

import java.util.List;

public class RedPacketService extends AccessibilityService {

    private static final String TAG = "RedPacketService";
    // 微信聊天界面
    private final String LAUCHER = "LauncherUI";
    // 点击红包弹出界面
    private final String LUCKY_MONEY_DETAIL = "com.tencent.mm.plugin.luckymoney.ui.LuckyMoneyDetailUI";
    // 红包领取后详情界面
    private final String LUCKY_MONEY_RECEIVER = "com.tencent.mm.plugin.luckymoney.ui.En_fba4b94f";

    private final String WECHAT_CHATTING = "ChattingUI";
    // 是否已经打开
    private boolean isOpenedRedPacket;

    private boolean isOpenDetail = false;

    private PowerManager.WakeLock mWakeLock;

    private KeyguardManager.KeyguardLock mKeyguardLock;

    // private List<String> ids = Arrays.asList("bjj", "bi3");

    @Override
    public void onAccessibilityEvent(AccessibilityEvent event) {
        int eventType = event.getEventType();

        switch (eventType) {
            // 通知栏消息，判断是否为微信红包，是的话跳转
            case AccessibilityEvent.TYPE_NOTIFICATION_STATE_CHANGED:
                List<CharSequence> texts = event.getText();
                for (CharSequence text :
                        texts) {
                    String content = text.toString();
                    if (!TextUtils.isEmpty(content)) {
                        if (content.contains("[微信红包]") || content.contains("微信红包")) {
                            if (!isScreenOn()) {
                                wakeUpScreen();
                            }

                            openWechatPage(event);

                            isOpenedRedPacket = false;
                        }
                    }
                }

                break;
            // 页面跳转监听
            case AccessibilityEvent.TYPE_WINDOW_STATE_CHANGED:
            case AccessibilityEvent.TYPE_WINDOW_CONTENT_CHANGED:
                String className = event.getClassName().toString();
                // 是否为微信界面
                if (className.contains(LAUCHER) || className.contains(WECHAT_CHATTING)) {
                    // get current chat details' root view
                    AccessibilityNodeInfo rootNode = getRootInActiveWindow();

                    // Find the Red Packet
                    findRedPacket(rootNode);
                }

                if (LUCKY_MONEY_RECEIVER.equals(className)) {
                    AccessibilityNodeInfo rootNode = getRootInActiveWindow();
                    openRedPacket(rootNode);
                }

                if (isOpenDetail && LUCKY_MONEY_DETAIL.equals(className)) {
                    isOpenDetail = false;

                    backToHome();
                    performGlobalAction(GLOBAL_ACTION_BACK);

                    releaseWakeLock();
                }
                break;
        }
    }

    // 开始抢红包
    private void openRedPacket(AccessibilityNodeInfo rootNode) {
        for (int i = 0; i < rootNode.getChildCount(); i++) {
            AccessibilityNodeInfo node = rootNode.getChild(i);

            if ("android.widget.Button".equals(node.getClassName())) {
                node.performAction(AccessibilityNodeInfo.ACTION_CLICK);
                Log.d(TAG, "openRedPacket: 拿到了红包");
                isOpenDetail = true;
            }
            openRedPacket(node);
        }
    }

    /*
    private void openRedPacket(AccessibilityNodeInfo rootNode) {
        if (rootNode == null) {
            Log.d(TAG, "openRedPacket: Null RootNode");
            return;
        }

        List<AccessibilityNodeInfo> list = null;
        for (String id : ids) {
            list = rootNode.findAccessibilityNodeInfosByViewId(id);

            if (list != null && list.size() > 0) {
                list.get(0).performAction(AccessibilityNodeInfo.ACTION_CLICK);
                return;
            }
        }

        for (int i = 0; i < rootNode.getChildCount(); i++) {
            rootNode.getChild(i).performAction(AccessibilityNodeInfo.ACTION_CLICK);
        }
    }
    */

    // 查找红包
    private void findRedPacket(AccessibilityNodeInfo rootNode) {
        if (rootNode != null) {
            // 从后开始往前找
            int childNodes = rootNode.getChildCount();
            for (int i = childNodes - 1; i >= 0; i--) {
                AccessibilityNodeInfo node = rootNode.getChild(i);
                // 跳过空节点
                if (node == null) {
                    continue;
                }
                CharSequence text = node.getText();

                if (text != null && (text.toString().equals("领取红包") ||
                        text.toString().equals("查看红包"))) {

                    Log.d(TAG, "findRedPacket: 找到了红包");
                    AccessibilityNodeInfo parent = node.getParent();

                    while (parent != null) {
                        if (parent.isClickable()) {
                            parent.performAction(AccessibilityNodeInfo.ACTION_CLICK);
                            Log.d(TAG, "findRedPacket: 可点击:" + parent.isClickable());
                            isOpenedRedPacket = true;

                            // openRedPacket(parent);
                            break;
                        }
                        parent = parent.getParent();
                    }
                }

                if (isOpenedRedPacket) {
                    break;
                } else {
                    findRedPacket(node);
                }
            }
        }
    }

    private void openWechatPage(AccessibilityEvent event) {
        if (event.getParcelableData() != null && event.getParcelableData() instanceof Notification) {
            Notification notification = (Notification) event.getParcelableData();

            PendingIntent pendingIntent = notification.contentIntent;
            try {
                pendingIntent.send();
            } catch (PendingIntent.CanceledException e) {
                e.printStackTrace();
            }
        }
    }

    @Override
    protected void onServiceConnected() {
        Toast.makeText(this, "抢红包服务已开始", Toast.LENGTH_SHORT).show();
        super.onServiceConnected();
    }

    @Override
    public void onInterrupt() {
        Toast.makeText(this, "即将终止", Toast.LENGTH_SHORT).show();
    }

    @Override
    public boolean onUnbind(Intent intent) {
        Toast.makeText(this, "抢红包服务已被关闭", Toast.LENGTH_SHORT).show();
        return super.onUnbind(intent);
    }

    private void backToHome() {
        Intent backHome = new Intent(Intent.ACTION_MAIN);
        backHome.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        backHome.addCategory(Intent.CATEGORY_HOME);

        performGlobalAction(GLOBAL_ACTION_BACK);
        startActivity(backHome);
    }

    private boolean isScreenOn() {
        PowerManager powerManager = (PowerManager) getSystemService(Context.POWER_SERVICE);

        boolean isScreenOn = powerManager.isScreenOn();

        Log.e(TAG, "isScreenOn: " + isScreenOn);

        return isScreenOn;
    }

    private void wakeUpScreen() {
        PowerManager powerManager = (PowerManager) getSystemService(Context.POWER_SERVICE);

        mWakeLock = powerManager.newWakeLock(PowerManager.ACQUIRE_CAUSES_WAKEUP |
                PowerManager.FULL_WAKE_LOCK, "bright");

        mWakeLock.acquire();

        KeyguardManager keyguardManager = (KeyguardManager) getSystemService(Context.KEYGUARD_SERVICE);
        mKeyguardLock = keyguardManager.newKeyguardLock("unlock");

        mKeyguardLock.disableKeyguard();
    }

    private void releaseWakeLock() {
        if (mKeyguardLock != null) {
            mKeyguardLock.reenableKeyguard();
            mKeyguardLock = null;
        }
        if (mWakeLock != null) {
            mWakeLock.release();
            mWakeLock = null;
        }
    }
}
