import android.app.Activity;
import android.graphics.Color;
import android.graphics.drawable.GradientDrawable;
import android.os.Build;
import android.os.SystemClock;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.widget.TextView;

View dynamicIsland = null;
int[] globalAnimId = new int[]{0};

addItem("岛！", "onShowIsland");
addItem("不岛了不岛了我错了😭", "onCloseIsland");

void onShowIsland(int chatType, String peerUin, String name) {
    Activity act = getNowActivity();
    if (act == null) return;

    act.runOnUiThread(() -> {
        ViewGroup rootDecorView = (ViewGroup) act.getWindow().getDecorView();
        View oldIsland = rootDecorView.findViewWithTag("QFun_Island");
        if (oldIsland != null) {
            rootDecorView.removeView(oldIsland);
        }

        if (dynamicIsland != null && dynamicIsland.getParent() != null) {
            ((ViewGroup) dynamicIsland.getParent()).removeView(dynamicIsland);
        }

        float density = act.getResources().getDisplayMetrics().density;
        
        final float collapsedWidth = 140f * density;  
        final float collapsedHeight = 35f * density;  
// 上是调整默认长短 粗细，下面是展开        
        final float expandedWidth = 220f * density;   
        final float expandedHeight = 48f * density;   
        final float squareSize = 140f * density;      

        FrameLayout island = new FrameLayout(act);
        island.setTag("QFun_Island");
        
        TextView textView = new TextView(act);
        textView.setGravity(Gravity.CENTER);
        textView.setText("Hello World");
        textView.setTextSize(14f);
        textView.setSingleLine(true);
        textView.setAlpha(0f); 

        FrameLayout spinnerContainer = new FrameLayout(act);
        spinnerContainer.setAlpha(0f);
        
        int bgColor = Color.parseColor("#EADDFF");
        int targetTextColor = Color.parseColor("#21005D");
        if (Build.VERSION.SDK_INT >= 31) {
            try {
                bgColor = act.getResources().getColor(android.R.color.system_accent1_100, act.getTheme());
                targetTextColor = act.getResources().getColor(android.R.color.system_accent1_900, act.getTheme());
            } catch (Exception e) {}
        }
        textView.setTextColor(targetTextColor);

        View[] dotWrappers = new View[5];
        for (int i = 0; i < 5; i++) {
            FrameLayout wrapper = new FrameLayout(act);
            View dot = new View(act);
            GradientDrawable dotShape = new GradientDrawable();
            dotShape.setShape(GradientDrawable.OVAL);
            dotShape.setColor(targetTextColor);
            dot.setBackground(dotShape);

            FrameLayout.LayoutParams dotLp = new FrameLayout.LayoutParams((int)(6f * density), (int)(6f * density));
            dotLp.gravity = Gravity.TOP | Gravity.CENTER_HORIZONTAL;
            wrapper.addView(dot, dotLp);

            FrameLayout.LayoutParams wrapperLp = new FrameLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT);
            spinnerContainer.addView(wrapper, wrapperLp);
            dotWrappers[i] = wrapper;
        }

        GradientDrawable shape = new GradientDrawable();
        shape.setShape(GradientDrawable.RECTANGLE);
        shape.setCornerRadius(20f * density); 
        shape.setColor(bgColor);
        island.setBackground(shape);

        FrameLayout.LayoutParams textParams = new FrameLayout.LayoutParams(
            ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT);
        island.addView(textView, textParams);

        FrameLayout.LayoutParams spinnerParams = new FrameLayout.LayoutParams(
            (int)(48 * density), (int)(48 * density));
        spinnerParams.gravity = Gravity.CENTER;
        island.addView(spinnerContainer, spinnerParams);

        FrameLayout.LayoutParams params = new FrameLayout.LayoutParams((int)collapsedWidth, (int)collapsedHeight);
        params.gravity = Gravity.TOP | Gravity.CENTER_HORIZONTAL;
        params.topMargin = (int)(14f * density); //默认停靠位置
        island.setLayoutParams(params);

        int[] islandState = new int[]{0}; 
        float[] currentSize = new float[]{collapsedWidth, collapsedHeight};

        Runnable triggerAnimation = new Runnable() {
            public void run() {
                float targetW;
                float targetH;

                if (islandState[0] == 0) {
                    targetW = collapsedWidth; targetH = collapsedHeight;
                    textView.animate().alpha(0f).setDuration(200).start();
                    spinnerContainer.animate().alpha(0f).scaleX(0.5f).scaleY(0.5f).setDuration(200).start();
                } else if (islandState[0] == 1) {
                    targetW = expandedWidth; targetH = expandedHeight;
                    textView.animate().alpha(1f).setDuration(200).start();
                    spinnerContainer.animate().alpha(0f).scaleX(0.5f).scaleY(0.5f).setDuration(200).start();
                } else {
                    targetW = squareSize; targetH = squareSize;
                    textView.animate().alpha(0f).setDuration(200).start();
                    spinnerContainer.animate().alpha(1f).scaleX(1f).scaleY(1f).setDuration(250).start();
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
                            ViewGroup.LayoutParams lp = island.getLayoutParams();
                            lp.width = (int) currentSize[0];
                            lp.height = (int) currentSize[1];
                            island.setLayoutParams(lp);
                        } else if (currentSize[0] != targetW) {
                            currentSize[0] = targetW; currentSize[1] = targetH;
                            ViewGroup.LayoutParams lp = island.getLayoutParams();
                            lp.width = (int) currentSize[0];
                            lp.height = (int) currentSize[1];
                            island.setLayoutParams(lp);
                        }

                        if (islandState[0] == 2) {
                            long elapsed = System.currentTimeMillis() - startTime;
                            float duration = 2800f; 
                            float t = (elapsed % (long)duration) / duration;

                            for (int i = 0; i < 5; i++) {
                                float dotT = t - (i * 0.04f);
                                if (dotT < 0) dotT += 1f;

                                float eased;
                                if (dotT < 0.5f) {
                                    eased = 4f * dotT * dotT * dotT;
                                } else {
                                    float p = 2f * dotT - 2f;
                                    eased = 0.5f * p * p * p + 1f;
                                }
                                
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

        island.setOnClickListener(v -> {
            if (islandState[0] == 2) {
                islandState[0] = 0;
            } else {
                islandState[0] = (islandState[0] == 0) ? 1 : 0;
            }
            triggerAnimation.run();
        });

        island.setOnLongClickListener(v -> {
            if (islandState[0] != 2) {
                islandState[0] = 2;
                triggerAnimation.run();
            }
            return true; 
        });

        rootDecorView.addView(island);
        dynamicIsland = island;
    });
}

void onCloseIsland(int chatType, String peerUin, String name) {
    globalAnimId[0]++;
    if (dynamicIsland != null) {
        dynamicIsland.post(() -> {
            if (dynamicIsland != null && dynamicIsland.getParent() != null) {
                ((ViewGroup) dynamicIsland.getParent()).removeView(dynamicIsland);
            }
            dynamicIsland = null;
        });
    }
    Activity act = getNowActivity();
    if (act != null) {
        act.runOnUiThread(() -> {
            ViewGroup root = (ViewGroup) act.getWindow().getDecorView();
            View old = root.findViewWithTag("QFun_Island");
            if (old != null) root.removeView(old);
        });
    }
}

void unLoadPlugin() {
    globalAnimId[0]++;
    if (dynamicIsland != null) {
        dynamicIsland.post(() -> {
            if (dynamicIsland != null && dynamicIsland.getParent() != null) {
                ((ViewGroup) dynamicIsland.getParent()).removeView(dynamicIsland);
            }
            dynamicIsland = null;
        });
    }
    Activity act = getNowActivity();
    if (act != null) {
        act.runOnUiThread(() -> {
            ViewGroup root = (ViewGroup) act.getWindow().getDecorView();
            View old = root.findViewWithTag("QFun_Island");
            if (old != null) root.removeView(old);
        });
    }
}
