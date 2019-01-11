package com.example.akawata.kakeibohelper;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;

import org.apache.commons.collections4.CollectionUtils;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

public class LogActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        final LogActivity self = this;
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_log);

        Intent intent = this.getIntent();
        String category = intent.getStringExtra("category");
        String categoryName = "";
        switch(category){
            case Const.CATEGORY_FOOD : categoryName = Const.CATEGORY_FOOD_NAME; break;
            case Const.CATEGORY_TRANSPORT : categoryName = Const.CATEGORY_TRANSPORT_NAME; break;
            case Const.CATEGORY_POCKET : categoryName = Const.CATEGORY_POCKET_NAME; break;
            case Const.CATEGORY_OTHER : categoryName = Const.CATEGORY_OTHER_NAME; break;
            default: categoryName = "FAULT!";
        }
        ((TextView)findViewById(R.id.category)).setText(categoryName);

        findViewById(R.id.back).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                self.finish();
            }
        });

        //log取得
        SharedPreferences foodPref = getSharedPreferences(category, Context.MODE_PRIVATE);
        List<Map<String, String>> list = new ArrayList<>();
        int summary = 0;
        try {
            Set<String> ids = foodPref.getAll().keySet();
            for(String id : ids){
                JSONArray jsonArray = new JSONArray("["+foodPref.getString(id, "")+"]");
                for (int i = 0; i < jsonArray.length(); i++) {
                    JSONObject jsonObject = jsonArray.getJSONObject(i);
                    Map<String, String> map = new HashMap<>();
                    map.put(Const.KEY_ID, id);
                    map.put(Const.KEY_DATE, jsonObject.getString(Const.KEY_DATE));
                    map.put(Const.KEY_MONEY, jsonObject.getString(Const.KEY_MONEY));
                    map.put(Const.KEY_MEMO, jsonObject.getString(Const.KEY_MEMO));
                    list.add(map);

                    summary += jsonObject.getInt(Const.KEY_MONEY);
                }
            }
        } catch (JSONException e) {
            //TODO
            e.printStackTrace();
        }

        Collections.sort(list, new Comparator<Map<String, String>>(){
            @Override
            public int compare(Map<String, String> rec1, Map<String, String> rec2) {
                String id1 = rec1.get(Const.KEY_ID);
                String id2 = rec2.get(Const.KEY_ID);
                if (id1 == null) return -1;
                if (id2 == null) return -1;
                return id2.compareTo(id1);
            }
        });

        //set to view
        ((TextView)findViewById(R.id.summary)).setText(summary + " THB");

        RecyclerView logRV = findViewById(R.id.log_list);
        logRV.setAdapter(new LogAdapter(list, category, self));
        logRV.setLayoutManager(new LinearLayoutManager(self));

    }

    class LogViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener{
        TextView dateTV;
        TextView moneyTV;
        Button deleteBtn;
        TextView idTV;
        TextView memoTV;
        private String category;
        private LogActivity logActivity;
        LogViewHolder(@NonNull View itemView) {
            super(itemView);
            idTV = itemView.findViewById(R.id.id);
            dateTV = itemView.findViewById(R.id.date);
            moneyTV = itemView.findViewById(R.id.money);
            deleteBtn = itemView.findViewById(R.id.delete);
            memoTV = itemView.findViewById(R.id.memo);

            deleteBtn.setOnClickListener(this);
        }

        void setCategory(String category){
            this.category = category;
        }
        void setLogActivity(LogActivity logActivity){
            this.logActivity = logActivity;
        }

        @Override
        public void onClick(View v) {
            SharedPreferences pref = getSharedPreferences(category, Context.MODE_PRIVATE);
            SharedPreferences.Editor editor = pref.edit();
            editor.remove((String) idTV.getText());
            editor.apply();

            //遷移
            Intent intent = new Intent(logActivity, MainActivity.class);
            intent.putExtra("message", "削除しました");
            startActivity(intent);
        }
    }

    class LogAdapter extends RecyclerView.Adapter<LogViewHolder> {
        private List<Map<String, String>> logList;
        private String category;
        private LogActivity logActivity;

        LogAdapter(List<Map<String, String>> logList, String category, LogActivity logActivity){
            this.logList = logList;
            this.category = category;
            this.logActivity = logActivity;
        }

        @NonNull
        @Override
        public LogViewHolder onCreateViewHolder(@NonNull ViewGroup viewGroup, int i) {
            LogViewHolder lvh = new LogViewHolder(
                    LayoutInflater.from(viewGroup.getContext()).inflate(R.layout.log_row, viewGroup, false)
            );
            lvh.setCategory(category);
            lvh.setLogActivity(logActivity);
            return lvh;
        }

        @Override
        public void onBindViewHolder(@NonNull LogViewHolder logViewHolder, int i) {
            if(!CollectionUtils.isEmpty(logList)) {
                Map<String, String> log = logList.get(i);
                logViewHolder.idTV.setText(log.get((Const.KEY_ID)));
                logViewHolder.dateTV.setText(log.get(Const.KEY_DATE));
                String money = log.get(Const.KEY_MONEY) + " THB";
                logViewHolder.moneyTV.setText(money);
                logViewHolder.memoTV.setText(log.get(Const.KEY_MEMO));
            }
        }

        @Override
        public int getItemCount() {
            if(CollectionUtils.isEmpty(logList)) {
               return 0;
            } else {
                return logList.size();
            }
        }
    }


}