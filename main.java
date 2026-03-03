import android.app.Activity;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.graphics.PixelFormat;
import android.graphics.drawable.GradientDrawable;
import android.os.Build;
import android.os.Handler;
import android.os.Looper;
import android.os.SystemClock;
import android.text.TextUtils;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.view.ViewTreeObserver;
import android.view.inputmethod.InputMethodManager;
import android.widget.EditText;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

View[] islandRef = new View[]{null};
int[] globalAnimId = new int[]{0};

Object[] currentPmContact = new Object[]{null};
String[] currentPmName = new String[]{""};
String[] currentPmText = new String[]{""};
String[] currentPmUin = new String[]{""}; 
Runnable[] notifyPmRunnable = new Runnable[]{null};

addItem("岛！", "onShowIsland");
addItem("不岛了不岛了我错了😭", "onCloseIsland");
addItem("切开关莫奈取色", "onToggleMonet");
addItem("调整停靠位置", "onAdjustY");

void onAdjustY(int chatType, String peerUin, String name) {
    Activity act = getNowActivity();
    if (act == null) {
        qqToast(1, "请在聊天界面内调整！");
        return;
    }
    act.runOnUiThread(() -> {
        boolean useMonet = getBoolean("config", "use_monet", true);
        
        int bgColor = Color.parseColor("#1E1E1E"); 
        int targetTextColor = Color.parseColor("#FFFFFF");
        
        if (useMonet && Build.VERSION.SDK_INT >= 31) {
            try {
                bgColor = act.getResources().getColor(android.R.color.system_accent1_100, act.getTheme());
                targetTextColor = act.getResources().getColor(android.R.color.system_accent1_900, act.getTheme());
            } catch (Exception e) {}
        }
        
        int inputBgColor = Color.argb(30, Color.red(targetTextColor), Color.green(targetTextColor), Color.blue(targetTextColor));

        android.app.Dialog dialog = new android.app.Dialog(act);
        dialog.getWindow().setBackgroundDrawable(new android.graphics.drawable.ColorDrawable(Color.TRANSPARENT));
        
        float density = act.getResources().getDisplayMetrics().density;
        
        LinearLayout root = new LinearLayout(act);
        root.setOrientation(LinearLayout.VERTICAL);
        root.setPadding((int)(24*density), (int)(24*density), (int)(24*density), (int)(24*density));
        GradientDrawable rootBg = new GradientDrawable();
        rootBg.setShape(GradientDrawable.RECTANGLE);
        rootBg.setCornerRadius(28f * density);
        rootBg.setColor(bgColor);
        root.setBackground(rootBg);
        
        TextView title = new TextView(act);
        title.setText("停靠位置 (dp正为往下，负为往上)");
        title.setTextColor(targetTextColor);
        title.setTextSize(20f);
        title.setPadding(0, 0, 0, (int)(20*density));
        root.addView(title);
        
        EditText input = new EditText(act);
        input.setInputType(android.text.InputType.TYPE_CLASS_NUMBER | android.text.InputType.TYPE_NUMBER_FLAG_SIGNED);
        int currentY = getInt("config", "island_y", 5);
        input.setText(String.valueOf(currentY));
        input.setTextColor(targetTextColor);
        input.setHintTextColor(Color.argb(128, Color.red(targetTextColor), Color.green(targetTextColor), Color.blue(targetTextColor)));
        GradientDrawable inputBg = new GradientDrawable();
        inputBg.setShape(GradientDrawable.RECTANGLE);
        inputBg.setCornerRadius(12f * density);
        inputBg.setColor(inputBgColor);
        input.setBackground(inputBg);
        input.setPadding((int)(16*density), (int)(12*density), (int)(16*density), (int)(12*density));
        root.addView(input, new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT));
        
        LinearLayout btnContainer = new LinearLayout(act);
        btnContainer.setOrientation(LinearLayout.HORIZONTAL);
        btnContainer.setGravity(Gravity.END);
        btnContainer.setPadding(0, (int)(24*density), 0, 0);
        
        TextView btnCancel = new TextView(act);
        btnCancel.setText("取消");
        btnCancel.setTextColor(targetTextColor);
        btnCancel.setTextSize(14f);
        btnCancel.setPadding((int)(16*density), (int)(10*density), (int)(16*density), (int)(10*density));
        btnCancel.setOnClickListener(v -> dialog.dismiss());
        
        TextView btnConfirm = new TextView(act);
        btnConfirm.setText("确定");
        btnConfirm.setTextColor(bgColor);
        btnConfirm.setTextSize(14f);
        btnConfirm.setPadding((int)(20*density), (int)(10*density), (int)(20*density), (int)(10*density));
        GradientDrawable confirmBg = new GradientDrawable();
        confirmBg.setShape(GradientDrawable.RECTANGLE);
        confirmBg.setCornerRadius(20f * density);
        confirmBg.setColor(targetTextColor);
        btnConfirm.setBackground(confirmBg);
        btnConfirm.setOnClickListener(v -> {
            try {
                int y = Integer.parseInt(input.getText().toString());
                putInt("config", "island_y", y);
                qqToast(2, "设置成功，重岛！");
                dialog.dismiss();
            } catch (Exception e) {
                qqToast(1, "请输入有效数字！");
            }
        });
        
        btnContainer.addView(btnCancel);
        LinearLayout.LayoutParams confirmLp = new LinearLayout.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        confirmLp.leftMargin = (int)(8*density);
        btnContainer.addView(btnConfirm, confirmLp);
        
        root.addView(btnContainer, new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT));
        
        dialog.setContentView(root);
        dialog.getWindow().setLayout((int)(300 * density), ViewGroup.LayoutParams.WRAP_CONTENT);
        dialog.show();
    });
}

void onToggleMonet(int chatType, String peerUin, String name) {
    boolean useMonet = getBoolean("config", "use_monet", true);
    putBoolean("config", "use_monet", !useMonet);
    qqToast(2, "莫奈取色已" + (!useMonet ? "开启" : "关闭") + "，重岛！");
}

void onMsg(Object data) {
    if (data.type == 1) { 
        String name = "";
        Object rawMsg = data.data; 
        String finalMsg = "";
        
        if (rawMsg != null) {
            name = rawMsg.sendNickName; 
            
            Object elements = rawMsg.elements; 
            if (elements != null) {
                StringBuilder sb = new StringBuilder();
                try {
                    java.util.List list = (java.util.List) elements;
                    for (Object el : list) {
                        if (el == null) continue;
                        int type = el.elementType; 
                        
                        if (type == 1 && el.textElement != null) {
                            if (el.textElement.content != null) {
                                sb.append(el.textElement.content);
                            }
                        } else if (type == 2) {
                            sb.append("[图片]");
                        } else if (type == 4) {
                            sb.append("[语音]");
                        } else if (type == 5) {
                            sb.append("[视频]");
                        } else if (type == 6) {
                            if (el.faceElement != null && el.faceElement.faceText != null) {
                                sb.append(el.faceElement.faceText);
                            } else {
                                sb.append("[表情]");
                            }
                        } else if (type == 7) {
                            sb.append("[回复]");
                        }
                    }
                    finalMsg = sb.toString();
                } catch (Exception e) {}
            }
        }
        
        if (name == null || name.isEmpty()) {
            name = data.userUin; 
        }
        
        if (finalMsg.isEmpty()) {
            finalMsg = data.msg;
        }
        
        String msg = finalMsg;
        Object contact = data.contact;
        String uin = data.userUin; 

        new Handler(Looper.getMainLooper()).post(() -> {
            if (notifyPmRunnable[0] != null && islandRef[0] != null) {
                currentPmContact[0] = contact;
                currentPmName[0] = name;
                currentPmText[0] = msg;
                currentPmUin[0] = uin;
                notifyPmRunnable[0].run();
            }
        });
    }
}

void onShowIsland(int chatType, String peerUin, String name) {
    new Handler(Looper.getMainLooper()).post(() -> {
        WindowManager wm = (WindowManager) context.getSystemService(Context.WINDOW_SERVICE);
        if (wm == null) return;

        if (islandRef[0] != null) {
            try { wm.removeView(islandRef[0]); } catch (Exception e) {}
            islandRef[0] = null;
        }

        float density = context.getResources().getDisplayMetrics().density;
        
        final float collapsedWidth = 140f * density;  
        final float collapsedHeight = 35f * density;  
        float[] dynamicExpandedWidth = new float[]{220f * density};   
        final float expandedHeight = 48f * density;   
        final float squareSize = 220f * density;
        
        float[] dynamicPmNotifyWidth = new float[]{180f * density};
        final float pmNotifyHeight = 40f * density;

        FrameLayout island = new FrameLayout(context);
        
        boolean useMonet = getBoolean("config", "use_monet", true);
        int bgColor = Color.parseColor("#000000");
        int targetTextColor = Color.parseColor("#FFFFFF");
        
        if (useMonet && Build.VERSION.SDK_INT >= 31) {
            try {
                bgColor = context.getResources().getColor(android.R.color.system_accent1_100, context.getTheme());
                targetTextColor = context.getResources().getColor(android.R.color.system_accent1_900, context.getTheme());
            } catch (Exception e) {}
        }

        GradientDrawable shape = new GradientDrawable();
        shape.setShape(GradientDrawable.RECTANGLE);
        shape.setCornerRadius(20f * density); 
        shape.setColor(bgColor);
        island.setBackground(shape);

        TextView textView = new TextView(context);
        textView.setGravity(Gravity.CENTER_VERTICAL | Gravity.LEFT);
        textView.setPadding((int)(20f * density), 0, (int)(20f * density), 0);
        textView.setText("正在获取一言..."); 
        textView.setTextSize(14f);
        textView.setTextColor(targetTextColor);
        textView.setSingleLine(true);
        textView.setEllipsize(TextUtils.TruncateAt.END);
        textView.setAlpha(0f); 

        int[] islandState = new int[]{0}; 
        Runnable[] triggerAnimRef = new Runnable[]{null};

        Runnable fetchYiyan = () -> {
            textView.setText("正在获取一言...");
            dynamicExpandedWidth[0] = 220f * density;
            
            new Thread(() -> {
                try {
                    java.net.URL url = new java.net.URL("https://api.kxzjoker.cn/api/yiyan?m=json");
                    java.net.HttpURLConnection conn = (java.net.HttpURLConnection) url.openConnection();
                    conn.setConnectTimeout(3000); 
                    
                    java.io.BufferedReader reader = new java.io.BufferedReader(new java.io.InputStreamReader(conn.getInputStream()));
                    StringBuilder jsonSb = new StringBuilder();
                    String line;
                    while ((line = reader.readLine()) != null) {
                        jsonSb.append(line);
                    }
                    reader.close();
                    
                    org.json.JSONObject json = new org.json.JSONObject(jsonSb.toString());
                    String contentStr = json.optString("content", "Hello World");
                    
                    new Handler(Looper.getMainLooper()).post(() -> {
                        textView.setText(contentStr);
                        float textWidth = textView.getPaint().measureText(contentStr);
                        float targetWidth = textWidth + (40f * density);
                        float screenWidth = context.getResources().getDisplayMetrics().widthPixels;
                        
                        dynamicExpandedWidth[0] = Math.max(220f * density, Math.min(targetWidth, screenWidth - 32f * density));
                        
                        if (islandState[0] == 1 && triggerAnimRef[0] != null) {
                            triggerAnimRef[0].run();
                        }
                    });
                } catch (Exception e) {
                    new Handler(Looper.getMainLooper()).post(() -> {
                        textView.setText("获取失败，请重试");
                        dynamicExpandedWidth[0] = 220f * density;
                        if (islandState[0] == 1 && triggerAnimRef[0] != null) {
                            triggerAnimRef[0].run();
                        }
                    });
                }
            }).start();
        };

        fetchYiyan.run();

        FrameLayout spinnerContainer = new FrameLayout(context);
        spinnerContainer.setAlpha(0f);
        View[] dotWrappers = new View[5];
        for (int i = 0; i < 5; i++) {
            FrameLayout wrapper = new FrameLayout(context);
            View dot = new View(context);
            GradientDrawable dotShape = new GradientDrawable();
            dotShape.setShape(GradientDrawable.OVAL);
            dotShape.setColor(targetTextColor);
            dot.setBackground(dotShape);

            FrameLayout.LayoutParams dotLp = new FrameLayout.LayoutParams((int)(10f * density), (int)(10f * density));
            dotLp.gravity = Gravity.TOP | Gravity.CENTER_HORIZONTAL;
            wrapper.addView(dot, dotLp);
            spinnerContainer.addView(wrapper, new FrameLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT));
            dotWrappers[i] = wrapper;
        }

        LinearLayout pmContainer = new LinearLayout(context);
        pmContainer.setOrientation(LinearLayout.HORIZONTAL);
        pmContainer.setGravity(Gravity.CENTER_VERTICAL);
        pmContainer.setAlpha(0f);
        pmContainer.setVisibility(View.GONE);
        pmContainer.setPadding((int)(4f*density), 0, (int)(8f*density), 0);

        ImageView avatarView = new ImageView(context);
        avatarView.setScaleType(ImageView.ScaleType.CENTER_CROP);
        avatarView.setOutlineProvider(new android.view.ViewOutlineProvider() {
            public void getOutline(View view, android.graphics.Outline outline) {
                outline.setOval(0, 0, view.getWidth(), view.getHeight());
            }
        });
        avatarView.setClipToOutline(true);
        GradientDrawable avatarBg = new GradientDrawable();
        avatarBg.setShape(GradientDrawable.OVAL);
        avatarBg.setColor(targetTextColor); 
        avatarView.setBackground(avatarBg);
        
        LinearLayout.LayoutParams avatarLp = new LinearLayout.LayoutParams((int)(30f*density), (int)(30f*density));
        avatarLp.setMargins(0, 0, (int)(8f*density), 0);
        pmContainer.addView(avatarView, avatarLp);

        TextView pmInfoText = new TextView(context);
        pmInfoText.setTextColor(targetTextColor);
        pmInfoText.setTextSize(13f);
        pmInfoText.setSingleLine(true);
        pmInfoText.setEllipsize(TextUtils.TruncateAt.END);
        LinearLayout.LayoutParams infoLp = new LinearLayout.LayoutParams(0, ViewGroup.LayoutParams.WRAP_CONTENT, 1f);
        pmContainer.addView(pmInfoText, infoLp);

        LinearLayout pmInputContainer = new LinearLayout(context);
        pmInputContainer.setOrientation(LinearLayout.HORIZONTAL);
        pmInputContainer.setGravity(Gravity.CENTER_VERTICAL);
        pmInputContainer.setVisibility(View.GONE);
        LinearLayout.LayoutParams inputContainerLp = new LinearLayout.LayoutParams(0, ViewGroup.LayoutParams.MATCH_PARENT, 1f);
        pmContainer.addView(pmInputContainer, inputContainerLp);

        EditText replyInput = new EditText(context);
        replyInput.setTextColor(targetTextColor);
        replyInput.setHintTextColor(Color.argb(128, Color.red(targetTextColor), Color.green(targetTextColor), Color.blue(targetTextColor)));
        replyInput.setHint("回复...");
        replyInput.setTextSize(13f);
        replyInput.setSingleLine(true);
        replyInput.setBackground(null); 
        replyInput.setPadding(0, 0, 0, 0);
        pmInputContainer.addView(replyInput, new LinearLayout.LayoutParams(0, ViewGroup.LayoutParams.WRAP_CONTENT, 1f));

        TextView sendBtn = new TextView(context);
        sendBtn.setText("发送");
        sendBtn.setTextColor(bgColor);
        sendBtn.setTextSize(12f);
        sendBtn.setGravity(Gravity.CENTER);
        sendBtn.setPadding((int)(8f*density), (int)(4f*density), (int)(8f*density), (int)(4f*density));
        GradientDrawable btnBg = new GradientDrawable();
        btnBg.setShape(GradientDrawable.RECTANGLE);
        btnBg.setCornerRadius(10f*density);
        btnBg.setColor(targetTextColor);
        sendBtn.setBackground(btnBg);
        pmInputContainer.addView(sendBtn, new LinearLayout.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT));

        island.addView(textView, new FrameLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT));
        FrameLayout.LayoutParams spinnerParams = new FrameLayout.LayoutParams((int)(80f * density), (int)(80f * density));
        spinnerParams.gravity = Gravity.CENTER;
        island.addView(spinnerContainer, spinnerParams);
        island.addView(pmContainer, new FrameLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT));

        WindowManager.LayoutParams wmParams = new WindowManager.LayoutParams();
        if (Build.VERSION.SDK_INT >= 26) { 
            wmParams.type = WindowManager.LayoutParams.TYPE_APPLICATION_OVERLAY;
        } else {
            wmParams.type = WindowManager.LayoutParams.TYPE_PHONE;
        }
        wmParams.format = PixelFormat.RGBA_8888; 
        
        int FLAG_NO_FOCUS = WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE 
                          | WindowManager.LayoutParams.FLAG_NOT_TOUCH_MODAL 
                          | WindowManager.LayoutParams.FLAG_LAYOUT_IN_SCREEN
                          | WindowManager.LayoutParams.FLAG_LAYOUT_INSET_DECOR;
                          
        int FLAG_WITH_FOCUS = WindowManager.LayoutParams.FLAG_NOT_TOUCH_MODAL 
                            | WindowManager.LayoutParams.FLAG_LAYOUT_IN_SCREEN
                            | WindowManager.LayoutParams.FLAG_LAYOUT_INSET_DECOR;

        int FLAG_GHOST = FLAG_NO_FOCUS | WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE;

        wmParams.flags = FLAG_NO_FOCUS;
        wmParams.gravity = Gravity.TOP | Gravity.CENTER_HORIZONTAL;
        wmParams.width = (int)collapsedWidth;
        wmParams.height = (int)collapsedHeight;
        
        int islandY = getInt("config", "island_y", 5); 
        wmParams.y = (int)(islandY * density); 

        float[] currentSize = new float[]{collapsedWidth, collapsedHeight};
        int[] autoHideToken = new int[]{0}; 

        Runnable triggerAnimation = new Runnable() {
            public void run() {
                if (context.getResources().getConfiguration().orientation == android.content.res.Configuration.ORIENTATION_LANDSCAPE) {
                    return;
                }

                float targetW, targetH;

                if (islandState[0] == 4) {
                    wmParams.flags = FLAG_WITH_FOCUS;
                } else {
                    wmParams.flags = FLAG_NO_FOCUS;
                    InputMethodManager imm = (InputMethodManager) context.getSystemService(Context.INPUT_METHOD_SERVICE);
                    imm.hideSoftInputFromWindow(island.getWindowToken(), 0);
                }

                if (islandState[0] == 0) {
                    targetW = collapsedWidth; targetH = collapsedHeight;
                    textView.animate().alpha(0f).setDuration(200).start();
                    spinnerContainer.animate().alpha(0f).scaleX(0.5f).scaleY(0.5f).setDuration(200).start();
                    pmContainer.animate().alpha(0f).setDuration(200).withEndAction(() -> pmContainer.setVisibility(View.GONE)).start();
                } else if (islandState[0] == 1) {
                    targetW = dynamicExpandedWidth[0]; 
                    targetH = expandedHeight;
                    textView.animate().alpha(1f).setDuration(200).start();
                    spinnerContainer.animate().alpha(0f).scaleX(0.5f).scaleY(0.5f).setDuration(200).start();
                    pmContainer.animate().alpha(0f).setDuration(200).withEndAction(() -> pmContainer.setVisibility(View.GONE)).start();
                } else if (islandState[0] == 2) {
                    targetW = squareSize; targetH = squareSize;
                    textView.animate().alpha(0f).setDuration(200).start();
                    pmContainer.animate().alpha(0f).setDuration(200).withEndAction(() -> pmContainer.setVisibility(View.GONE)).start();
                    spinnerContainer.animate().alpha(1f).scaleX(1f).scaleY(1f).setDuration(250).start();
                } else if (islandState[0] == 3) {
                    targetW = dynamicPmNotifyWidth[0]; 
                    targetH = pmNotifyHeight;
                    textView.animate().alpha(0f).setDuration(150).start();
                    spinnerContainer.animate().alpha(0f).setDuration(150).start();
                    
                    pmContainer.setVisibility(View.VISIBLE);
                    pmInfoText.setVisibility(View.VISIBLE);
                    pmInputContainer.setVisibility(View.GONE);
                    pmContainer.animate().alpha(1f).setDuration(250).start();
                } else if (islandState[0] == 4) {
                    targetW = Math.max(280f * density, context.getResources().getDisplayMetrics().widthPixels - 120f * density); 
                    targetH = 42f * density;
                    textView.animate().alpha(0f).setDuration(150).start();
                    spinnerContainer.animate().alpha(0f).setDuration(150).start();
                    
                    pmContainer.setVisibility(View.VISIBLE);
                    pmInfoText.setVisibility(View.GONE);
                    pmInputContainer.setVisibility(View.VISIBLE);
                    pmContainer.animate().alpha(1f).setDuration(250).start();
                } else {
                    targetW = collapsedWidth; targetH = collapsedHeight;
                }

                globalAnimId[0]++; 
                final int myAnimId = globalAnimId[0];
                final float animationSpeed = 0.15f; 
                final long startTime = System.currentTimeMillis();

                Runnable tick = new Runnable() {
                    public void run() {
                        if (myAnimId != globalAnimId[0]) return; 

                        float diffW = targetW - currentSize[0];
                        float diffH = targetH - currentSize[1];
                        boolean resizing = Math.abs(diffW) > 0.5f || Math.abs(diffH) > 0.5f;

                        if (resizing) {
                            currentSize[0] += diffW * animationSpeed;
                            currentSize[1] += diffH * animationSpeed;
                            wmParams.width = (int) currentSize[0];
                            wmParams.height = (int) currentSize[1];
                            wm.updateViewLayout(island, wmParams);
                        } else if (currentSize[0] != targetW) {
                            currentSize[0] = targetW; currentSize[1] = targetH;
                            wmParams.width = (int) currentSize[0];
                            wmParams.height = (int) currentSize[1];
                            wm.updateViewLayout(island, wmParams);
                        }

                        if (islandState[0] == 2) {
                            long elapsed = System.currentTimeMillis() - startTime;
                            float duration = 2800f; 
                            float t = (elapsed % (long)duration) / duration;
                            for (int i = 0; i < 5; i++) {
                                float dotT = t - (i * 0.04f);
                                if (dotT < 0) dotT += 1f;
                                float eased = dotT < 0.5f ? 4f * dotT * dotT * dotT : 0.5f * (2f * dotT - 2f) * (2f * dotT - 2f) * (2f * dotT - 2f) + 1f;
                                dotWrappers[i].setRotation(eased * 720f);
                            }
                        }

                        if (resizing || islandState[0] == 2) {
                            island.postOnAnimation(this);
                        }
                    }
                };
                island.postOnAnimation(tick);
            }
        };
        triggerAnimRef[0] = triggerAnimation;

        island.getViewTreeObserver().addOnGlobalLayoutListener(new ViewTreeObserver.OnGlobalLayoutListener() {
            int lastOrientation = -1;
            public void onGlobalLayout() {
                int orientation = context.getResources().getConfiguration().orientation;
                if (orientation != lastOrientation) {
                    lastOrientation = orientation;
                    if (orientation == android.content.res.Configuration.ORIENTATION_LANDSCAPE) {
                        island.setAlpha(0f);
                        wmParams.flags = FLAG_GHOST;
                        wm.updateViewLayout(island, wmParams);
                    } else {
                        island.setAlpha(1f);
                        wmParams.flags = FLAG_NO_FOCUS;
                        wm.updateViewLayout(island, wmParams);
                        triggerAnimation.run();
                    }
                }
            }
        });

        Runnable scheduleAutoHide = () -> {
            autoHideToken[0]++;
            final int currentToken = autoHideToken[0];
            new Handler(Looper.getMainLooper()).postDelayed(() -> {
                if (autoHideToken[0] == currentToken && (islandState[0] == 3 || islandState[0] == 4)) {
                    islandState[0] = 0;
                    currentPmContact[0] = null;
                    replyInput.setText("");
                    triggerAnimation.run();
                }
            }, 5000); 
        };

        notifyPmRunnable[0] = () -> {
            String n = currentPmName[0];
            String uin = currentPmUin[0];
            String fullStr = n + ": " + currentPmText[0];
            pmInfoText.setText(fullStr);
            
            float textWidth = pmInfoText.getPaint().measureText(fullStr);
            float targetWidth = textWidth + (50f * density);
            float screenWidth = context.getResources().getDisplayMetrics().widthPixels;
            dynamicPmNotifyWidth[0] = Math.max(180f * density, Math.min(targetWidth, screenWidth - 32f * density));

            avatarView.setImageDrawable(null);
            
            new Thread(() -> {
                try {
                    java.net.URL url = new java.net.URL("http://q.qlogo.cn/headimg_dl?dst_uin=" + uin + "&spec=640");
                    java.net.HttpURLConnection conn = (java.net.HttpURLConnection) url.openConnection();
                    conn.setConnectTimeout(5000);
                    conn.connect();
                    Bitmap bmp = BitmapFactory.decodeStream(conn.getInputStream());
                    
                    new Handler(Looper.getMainLooper()).post(() -> {
                        if (bmp != null && currentPmUin[0].equals(uin)) {
                            avatarView.setImageBitmap(bmp);
                        }
                    });
                } catch (Exception e) {}
            }).start();
            
            islandState[0] = 3; 
            triggerAnimation.run();
            scheduleAutoHide.run();
        };

        island.setOnClickListener(v -> {
            if (islandState[0] == 3) {
                autoHideToken[0]++; 
                islandState[0] = 4;
                replyInput.requestFocus();
            } else if (islandState[0] == 4) {
            } else if (islandState[0] == 2) {
                islandState[0] = 0;
            } else {
                if (islandState[0] == 0) {
                    islandState[0] = 1;
                    fetchYiyan.run(); 
                } else {
                    islandState[0] = 0;
                }
            }
            triggerAnimation.run();
        });

        island.setOnLongClickListener(v -> {
            if (islandState[0] == 3 || islandState[0] == 4) {
                autoHideToken[0]++; 
                islandState[0] = 0;
                currentPmContact[0] = null;
                replyInput.setText("");
            } else if (islandState[0] != 2) {
                islandState[0] = 2;
            }
            triggerAnimation.run();
            return true; 
        });

        sendBtn.setOnClickListener(v -> {
            String txt = replyInput.getText().toString().trim();
            if (!txt.isEmpty() && currentPmContact[0] != null) {
                sendMsg(currentPmContact[0], txt);
                
                String sentStr = "✓ 已发送: " + txt;
                pmInfoText.setText(sentStr);
                
                float sentWidth = pmInfoText.getPaint().measureText(sentStr);
                float targetWidth = sentWidth + (50f * density);
                float screenWidth = context.getResources().getDisplayMetrics().widthPixels;
                dynamicPmNotifyWidth[0] = Math.max(180f * density, Math.min(targetWidth, screenWidth - 32f * density));

                islandState[0] = 3; 
                triggerAnimation.run();
                
                scheduleAutoHide.run();
            } else {
                islandState[0] = 0;
                triggerAnimation.run();
            }
            replyInput.setText("");
        });

        try {
            wm.addView(island, wmParams);
            islandRef[0] = island;
        } catch (Exception e) {
            qqToast(1, "岛岛失败！报错: " + e.getMessage());
        }
    });
}

void onCloseIsland(int chatType, String peerUin, String name) {
    globalAnimId[0]++;
    new Handler(Looper.getMainLooper()).post(() -> {
        if (islandRef[0] != null) {
            WindowManager wm = (WindowManager) context.getSystemService(Context.WINDOW_SERVICE);
            if (wm != null) {
                try { wm.removeView(islandRef[0]); } catch (Exception e) {}
            }
            islandRef[0] = null;
        }
    });
}

void unLoadPlugin() {
    globalAnimId[0]++;
    new Handler(Looper.getMainLooper()).post(() -> {
        if (islandRef[0] != null) {
            WindowManager wm = (WindowManager) context.getSystemService(Context.WINDOW_SERVICE);
            if (wm != null) {
                try { wm.removeView(islandRef[0]); } catch (Exception e) {}
            }
            islandRef[0] = null;
        }
    });
}
