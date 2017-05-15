package cn.EGGMaster.ui;

import android.app.Activity;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.os.Bundle;
import android.view.View;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.ListView;
import android.widget.SimpleAdapter;
import android.widget.TextView;
import android.widget.Toast;

import com.google.gson.reflect.TypeToken;

import java.util.List;
import java.util.Map;

import cn.EGGMaster.R;
import cn.EGGMaster.util.StringCode;

import static cn.EGGMaster.util.DataUtils.admin;
import static cn.EGGMaster.util.DataUtils.gson;
import static cn.EGGMaster.util.Utils.sendPost;

public class ListActivity extends Activity implements OnItemClickListener {
    private ListView listView;

    private List<Map<String, String>> lines = null;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_list);

        listView = (ListView) findViewById(R.id.lineList);
        //模拟数据
//        lines = new ArrayList<Map<String, String>>();
//        for (int i = 0; i < 10; i++) {
//            Map<String, String> map = new HashMap<String, String>();
//            map.put("lineName", i + "");
//            map.put("lineId", i + "");
//            lines.add(map);
//        }

        String result = sendPost("getLineList", "id=" + admin.get("id"));
        lines = gson.fromJson(StringCode.getInstance().decrypt(result), new TypeToken<List<Map<String, String>>>() {
        }.getType());
        if (lines != null && lines.size() > 0) {
            SimpleAdapter adapter = new SimpleAdapter(this, lines, R.layout.item,
                    new String[]{"name", "id"}, new int[]{R.id.lineName, R.id.lineId});
            listView.setAdapter(adapter);
            listView.setOnItemClickListener(this);
        }
    }

    @Override
    public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
        TextView lineId = (TextView) view.findViewById(R.id.lineId);
        TextView lineName = (TextView) view.findViewById(R.id.lineName);
        SharedPreferences preferences = getSharedPreferences("EggInfo", MODE_PRIVATE);
        Editor editor = preferences.edit();
        editor.putString("lineId", lineId.getText().toString());
        editor.apply();
        Toast.makeText(this, "已选择：" + lineName.getText().toString(), Toast.LENGTH_SHORT).show();
    }
}
