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
addItem("开关莫奈取色", "onToggleMonet");

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
        
        final float pmNotifyWidth = 180f * density;
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

        wmParams.flags = FLAG_NO_FOCUS;
        wmParams.gravity = Gravity.TOP | Gravity.CENTER_HORIZONTAL;
        wmParams.width = (int)collapsedWidth;
        wmParams.height = (int)collapsedHeight;
        wmParams.y = (int)(14f * density); 

        float[] currentSize = new float[]{collapsedWidth, collapsedHeight};
        int[] autoHideToken = new int[]{0}; 

        Runnable triggerAnimation = new Runnable() {
            public void run() {
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
                    targetW = pmNotifyWidth; targetH = pmNotifyHeight;
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
            pmInfoText.setText(n + ": " + currentPmText[0]);
            
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
                
                pmInfoText.setText("✓ 已发送: " + txt);
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
