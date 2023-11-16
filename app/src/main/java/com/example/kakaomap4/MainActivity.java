package com.example.kakaomap4;

import androidx.appcompat.app.AppCompatActivity;
import android.os.AsyncTask;
import android.os.Bundle;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.Spinner;
import android.widget.Toast;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;

public class MainActivity extends AppCompatActivity {

    private Spinner categorySpinner;
    private ListView storeListView;
    private List<String> categories;
    private ArrayAdapter<String> categoryAdapter;
    private ArrayAdapter<String> storeAdapter;

    // TODO: 카카오 REST API 키를 설정하세요.
    private static final String REST_API_KEY = "8a4f1750a33a01a17d50933a8336b64a";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        categorySpinner = findViewById(R.id.categorySpinner);
        storeListView = findViewById(R.id.storeListView);

        categories = new ArrayList<>();
        categories.add("PM9");
        categories.add("CS2");// 예시 카테고리 코드
        // 다른 카테고리 추가 가능

        categoryAdapter = new ArrayAdapter<>(this, android.R.layout.simple_spinner_item, categories);
        categoryAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        categorySpinner.setAdapter(categoryAdapter);

        storeAdapter = new ArrayAdapter<>(this, android.R.layout.simple_list_item_1);
        storeListView.setAdapter(storeAdapter);

        categorySpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parentView, View selectedItemView, int position, long id) {
                String selectedCategory = categories.get(position);
                new FetchStoresTask().execute(selectedCategory);
            }

            @Override
            public void onNothingSelected(AdapterView<?> parentView) {
                // Do nothing here
            }
        });
    }

    private class FetchStoresTask extends AsyncTask<String, Void, String> {

        @Override
        protected String doInBackground(String... params) {
            // 백그라운드에서 네트워크 요청 수행
            String categoryCode = params[0];
            String apiUrl = "https://dapi.kakao.com/v2/local/search/category.json?category_group_code=" + categoryCode + "&radius=20000";

            try {
                URL url = new URL(apiUrl);
                HttpURLConnection connection = (HttpURLConnection) url.openConnection();
                connection.setRequestMethod("GET");
                connection.setRequestProperty("Authorization", "KakaoAK " + REST_API_KEY);

                BufferedReader reader = new BufferedReader(new InputStreamReader(connection.getInputStream()));
                StringBuilder response = new StringBuilder();
                String line;

                while ((line = reader.readLine()) != null) {
                    response.append(line);
                }

                reader.close();
                connection.disconnect();

                return response.toString();
            } catch (IOException e) {
                e.printStackTrace();
                return null;
            }
        }

        @Override
        protected void onPostExecute(String response) {
            // 네트워크 요청 완료 후 매장 목록 표시
            if (response != null) {
                parseStoreResponse(response);
            } else {
                Toast.makeText(MainActivity.this, "Error fetching stores", Toast.LENGTH_SHORT).show();
            }
        }
    }

    private void parseStoreResponse(String response) {
        // 응답에서 매장 정보를 추출하여 리스트뷰에 표시
        storeAdapter.clear();

        try {
            // JSON 파싱 작업
            // 예시로 간단하게 처리하였으며, 실제 데이터 구조에 맞게 수정 필요
            // response를 JSONObject 또는 JSONArray로 파싱하여 사용
            // 여기서는 단순히 response를 리스트뷰에 표시하는 것으로 대체함
            storeAdapter.add(response);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
