package com.example.akawata.kakeibohelper;

import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.TypedValue;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.FrameLayout;
import android.widget.PopupWindow;
import android.widget.Toast;

import org.apache.commons.lang3.StringUtils;
import org.json.JSONException;
import org.json.JSONObject;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Locale;

public class MainActivity extends AppCompatActivity {
    private EditText moneyEText;
    private PopupWindow otherPopupWindow;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        final Context self = this;
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        moneyEText = findViewById(R.id.money);
        Button registerFoodBtn = findViewById(R.id.register_food);
        Button registerTransportBtn = findViewById(R.id.register_transport);
        Button registerPocketBtn = findViewById(R.id.register_pocket);
        Button registerOtherBtn = findViewById(R.id.register_other);
        Button logFoodBtn = findViewById(R.id.log_food);
        Button logTransportBtn = findViewById(R.id.log_transport);
        Button logPocketBtn = findViewById(R.id.log_pocket);
        Button logOtherBtn = findViewById(R.id.log_other);
        Button deleteAllLogBtn = findViewById(R.id.delete_all_log);

        registerFoodBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                register(Const.CATEGORY_FOOD, null);
            }
        });
        registerTransportBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                register(Const.CATEGORY_TRANSPORT, null);
            }
        });
        registerPocketBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                register(Const.CATEGORY_POCKET, null);
            }
        });
        registerOtherBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showOtherPopup();
            }
        });

        logFoodBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                moveLogActivity(self, Const.CATEGORY_FOOD);
            }
        });
        logTransportBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                moveLogActivity(self, Const.CATEGORY_TRANSPORT);
            }
        });
        logPocketBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                moveLogActivity(self, Const.CATEGORY_POCKET);
            }
        });
        logOtherBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                moveLogActivity(self, Const.CATEGORY_OTHER);
            }
        });

        deleteAllLogBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                deleteAllLogConfirm();
            }
        });

        //通知
        Intent intent = this.getIntent();
        String message = intent.getStringExtra("message");
        if(!StringUtils.isEmpty(message)){
            showNotification(message);
        }
    }

    private void moveLogActivity(Context mainActivity, String category){
        Intent intent = new Intent(mainActivity, LogActivity.class);
        intent.putExtra("category", category);
        startActivity(intent);
    }

    private boolean validateRegister(){
        String moneyStr = moneyEText.getText().toString();
        if("".equals(moneyStr)){
            AlertDialog.Builder builder = new AlertDialog.Builder(this);
            builder.setMessage("金額を入力してください")
                    .setNegativeButton("OK", new DialogInterface.OnClickListener() {
                        public void onClick(DialogInterface dialog, int id) {
                        }
                    });
            builder.show();
            return false;
        }

        return true;
    }

    private void register(String category, String memo) {
        if(!validateRegister()){
            return;
        }

        //register
        SharedPreferences pref = getSharedPreferences(category, Context.MODE_PRIVATE);
        SharedPreferences.Editor foodEditor = pref.edit();

        Calendar now = Calendar.getInstance();
        String id = new SimpleDateFormat("yyyyMMddHHmmss", Locale.JAPAN).format(now.getTime());

        JSONObject json = new JSONObject();

        try {
            json.put(Const.KEY_DATE, new SimpleDateFormat("MM/dd", Locale.JAPAN).format(now.getTime()));
            json.put(Const.KEY_MONEY, Integer.parseInt(moneyEText.getText().toString()));
            json.put(Const.KEY_MEMO, StringUtils.defaultString(memo, ""));

        } catch (JSONException e) {
            //TODO
            e.printStackTrace();
        }

        foodEditor.putString(id, json.toString());
        foodEditor.apply();

        moneyEText.setText("");
        showNotification("登録しました");

    }

    private void deleteAllLogConfirm() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setMessage("履歴を全て削除します。よろしいですか？")
                .setNegativeButton("キャンセル", new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {
                    }
                })
                .setPositiveButton("削除", new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {
                        deleteAllLog();
                    }
                });
        builder.show();
    }

    private void deleteAllLog() {
        deleteCategoryLog(Const.CATEGORY_FOOD);
        deleteCategoryLog(Const.CATEGORY_TRANSPORT);
        deleteCategoryLog(Const.CATEGORY_POCKET);
        deleteCategoryLog(Const.CATEGORY_OTHER);
        showNotification("履歴を全て削除しました");
    }

    private void deleteCategoryLog(String category){
        SharedPreferences pref = getSharedPreferences(category, Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = pref.edit();
        editor.clear();
        editor.apply();
    }

    private void showOtherPopup(){
        if(!validateRegister()){
            return;
        }

        otherPopupWindow = new PopupWindow(this);
        final View popLayout = getLayoutInflater().inflate(R.layout.other_popup,new FrameLayout(this));
        popLayout.findViewById(R.id.close).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(otherPopupWindow.isShowing()){
                    otherPopupWindow.dismiss();
                }
            }
        });
        popLayout.findViewById(R.id.register).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                register(Const.CATEGORY_OTHER, ((EditText)popLayout.findViewById(R.id.memo)).getText().toString());
                otherPopupWindow.dismiss();
            }
        });

        otherPopupWindow.setContentView(popLayout);

        otherPopupWindow.setOutsideTouchable(true);
        otherPopupWindow.setFocusable(true);

        float width = TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 300, getResources().getDisplayMetrics());
        otherPopupWindow.setWidth((int) width);
        otherPopupWindow.setHeight(ViewGroup.LayoutParams.WRAP_CONTENT);

        otherPopupWindow.showAtLocation(findViewById(R.id.money), Gravity.CENTER, 0,0);
    }

    private void showNotification(final String message){
        Toast toast = Toast.makeText(this, message, Toast.LENGTH_SHORT);
        toast.setGravity(Gravity.TOP | Gravity.CENTER, 0, 0);
        toast.show();
    }

}