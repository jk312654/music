package com.example.music_chenyujie;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.text.SpannableString;
import android.text.Spanned;
import android.text.TextPaint;
import android.text.method.LinkMovementMethod;
import android.text.style.ClickableSpan;
import android.view.LayoutInflater;
import android.view.View;
import android.view.WindowManager;
import android.widget.TextView;

import com.example.music_chenyujie.ui.MainActivity;
import com.example.music_chenyujie.utils.PrivacyUtils;


/**
 * 启动页，应用入口
 */
public class SplashActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // 设置窗口为全屏（隐藏状态栏）
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,
                WindowManager.LayoutParams.FLAG_FULLSCREEN);

        // 加载启动页布局
        setContentView(R.layout.activity_splash);

        // 延迟展示 Dialog，让 Logo 有时间显示
        new Handler(Looper.getMainLooper()).postDelayed(() -> {
            // 如果用户未同意隐私协议，则弹出隐私弹窗
            if (!PrivacyUtils.isPrivacyAccepted()) {
                showPrivacyDialog();
            } else {
                // 已同意，直接进入主界面
                goToMainPageWithAnim();
            }
        }, 1500);
    }


    /**
     * 展示隐私政策弹窗（用户首次进入必须同意）
     */
    private void showPrivacyDialog() {
        // 加载弹窗布局
        View dialogView = LayoutInflater.from(this).inflate(R.layout.dialog_privacy, null);

        // 创建 AlertDialog，并设置不可取消（必须选择，要么同意，要么不同意）
        AlertDialog dialog = new AlertDialog.Builder(this)
                .setView(dialogView)
                .setCancelable(false)
                .create();

        // 绑定“同意”按钮点击事件
        dialogView.findViewById(R.id.btn_accept).setOnClickListener(v -> {
            // 保存用户已同意的标志（使用的是MMKV）
            PrivacyUtils.setPrivacyAccepted(true);
            dialog.dismiss(); // 关闭弹窗
            goToMainPageWithAnim(); // 进入主页面
        });

        // 点击“不同意”
        dialogView.findViewById(R.id.btn_decline).setOnClickListener(v -> {
            dialog.dismiss(); // 关闭弹窗
            finish(); // 关闭应用
        });

        // 获取弹窗中的文本组件
        TextView tvContent = dialogView.findViewById(R.id.tv_content);

        String fullText = "欢迎使用声音社区，我们将严格遵守相关法律和隐私政策保障您的个人隐私，请您阅读并同意《用户协议》和《隐私政策》。";
        // 构建 SpannableString 以支持点击跳转
        SpannableString spannableString = new SpannableString(fullText);

        // 设置“用户协议”点击行为
        ClickableSpan userAgreementSpan = new ClickableSpan() {
            @Override
            public void onClick(@NonNull View widget) {
                openWeb("https://www.mi.com");
            }

            @Override
            public void updateDrawState(@NonNull TextPaint ds) {
                super.updateDrawState(ds);
                ds.setColor(Color.parseColor("#3489FD")); // 蓝色
                ds.setUnderlineText(false); // 无下划线
            }
        };

        // 设置“隐私政策”点击行为
        ClickableSpan privacyPolicySpan = new ClickableSpan() {
            @Override
            public void onClick(@NonNull View widget) {
                openWeb("https://www.xiaomiev.com");
            }

            @Override
            public void updateDrawState(@NonNull TextPaint ds) {
                super.updateDrawState(ds);
                ds.setColor(Color.parseColor("#3489FD"));
                ds.setUnderlineText(false);
            }
        };

        // 设置文字范围
        int startUser = fullText.indexOf("《用户协议》");
        int endUser = startUser + "《用户协议》".length();

        int startPrivacy = fullText.indexOf("《隐私政策》");
        int endPrivacy = startPrivacy + "《隐私政策》".length();

        // 应用点击样式到指定文本区域
        spannableString.setSpan(userAgreementSpan, startUser, endUser, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
        spannableString.setSpan(privacyPolicySpan, startPrivacy, endPrivacy, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);

        // 设置文本和点击行为
        tvContent.setText(spannableString);
        tvContent.setMovementMethod(LinkMovementMethod.getInstance()); // 启用点击
        tvContent.setHighlightColor(Color.TRANSPARENT);

        // 显示弹窗
        dialog.show();

        // 设置弹窗背景透明（可以看到图标）
        dialog.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
    }

    /**
     * 跳转到首页MainActivity，并应用淡入淡出动画
     */
    private void goToMainPageWithAnim() {
        Intent intent = new Intent(SplashActivity.this, MainActivity.class);
        startActivity(intent);
        overridePendingTransition(android.R.anim.fade_in, android.R.anim.fade_out);
        finish(); // 结束启动页
    }

    /**
     * 打开网页链接（用于跳转隐私政策或用户协议）
     */
    private void openWeb(String url) {
        Intent browserIntent = new Intent(Intent.ACTION_VIEW, Uri.parse(url));
        startActivity(browserIntent);// 使用系统浏览器打开网页
    }


}

