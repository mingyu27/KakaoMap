package com.example.kakaomap4;

import android.Manifest;
import android.content.pm.PackageManager;
import android.location.Location;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.Spinner;
import android.widget.Toast;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationServices;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

public class MainActivity2 extends AppCompatActivity {


    private Spinner categorySpinner;
    private ListView storeListView;
    private List<String> categories;
    private ArrayAdapter<String> categoryAdapter;
    private ArrayAdapter<String> storeAdapter;
    private FusedLocationProviderClient fusedLocationClient;


    private static final String REST_API_KEY = "8a4f1750a33a01a17d50933a8336b64a";
    private static final int LOCATION_PERMISSION_REQUEST_CODE = 1;






    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);


        if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.ACCESS_FINE_LOCATION}, LOCATION_PERMISSION_REQUEST_CODE);
        }

        categorySpinner = findViewById(R.id.categorySpinner);
        storeListView = findViewById(R.id.storeListView);

        categories = new ArrayList<>();
        categories.add("PM9");
        categories.add("CE7");
        categories.add("PM9");// 예시 카테고리 코드
        // 다른 카테고리 추가 가능

        categoryAdapter = new ArrayAdapter<>(this, android.R.layout.simple_spinner_item, categories);
        categoryAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        categorySpinner.setAdapter(categoryAdapter);

        storeAdapter = new ArrayAdapter<>(this, android.R.layout.simple_list_item_1);
        storeListView.setAdapter(storeAdapter);

        fusedLocationClient = LocationServices.getFusedLocationProviderClient(this);

        // 위치 권한 요청
        requestLocationPermission();

        categorySpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parentView, View selectedItemView, int position, long id) {
                // 위치 권한이 허용되어 있을 때만 위치 기반 매장 검색 수행
                if (ContextCompat.checkSelfPermission(MainActivity2.this,
                        Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
                    fetchStores(categories.get(position));
                    Log.d("SMG", "GOOD");
                } else Log.d("SMG", "BAD");
            }

            @Override
            public void onNothingSelected(AdapterView<?> parentView) {
                // Do nothing here
            }
        });
    }

    private void requestLocationPermission() {
        // 위치 권한 요청
        if (ContextCompat.checkSelfPermission(this,
                Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this,
                    new String[]{Manifest.permission.ACCESS_FINE_LOCATION},
                    LOCATION_PERMISSION_REQUEST_CODE);
        }
    }




    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);

        // 권한 요청 결과 처리
        if (requestCode == LOCATION_PERMISSION_REQUEST_CODE) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                // 위치 권한이 허용되었을 때의 동작
                fetchStores(categories.get(categorySpinner.getSelectedItemPosition()));
            } else {
                Toast.makeText(this, "Location permission denied. Unable to fetch nearby stores.", Toast.LENGTH_SHORT).show();
            }
        }
    }

    private void fetchStores(String category) {
        // 위치 권한이 허용되어 있을 때만 위치 기반 매장 검색 수행
        if (ContextCompat.checkSelfPermission(MainActivity2.this, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
            fusedLocationClient.getLastLocation()
                    .addOnSuccessListener(this, location -> {
                        if (location != null) {
                            double latitude = location.getLatitude();
                            double longitude = location.getLongitude();
                            new FetchStoresTask().execute(category, String.valueOf(latitude), String.valueOf(longitude));
                        }
                    });
        } else {
            // 위치 권한이 허용되어 있지 않은 경우
            Log.d("SMG", "Location permission not granted.");
        }
    }


    private class FetchStoresTask extends AsyncTask<String, Void, String> {

        @Override
        protected String doInBackground(String... params) {
            // 백그라운드에서 네트워크 요청 수행
            String categoryCode = params[0];
//            String latitude = params[1];
//            String longitude = params[2];
            String latitude = "37.496264";
            String longitude = "126.957411";
            String apiUrl = "https://dapi.kakao.com/v2/local/search/category.json?category_group_code=" + categoryCode
                    + "&radius=20000&x=" + longitude + "&y=" + latitude;
            Log.d("SMG", latitude);
            Log.d("SMG", longitude);








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
                Toast.makeText(MainActivity2.this, "Error fetching stores", Toast.LENGTH_SHORT).show();
            }
        }
    }

// 코드 중간 생략...

    private void parseStoreResponse(String response) {
        // 응답에서 매장 정보를 추출하여 리스트뷰에 표시
        storeAdapter.clear();

        try {
            // JSON 파싱 작업
            JSONObject jsonResponse = new JSONObject(response);
            JSONArray storesArray = jsonResponse.getJSONArray("documents");

            // 매장 정보를 담을 리스트
            List<Store> storeList = new ArrayList<>();

            for (int i = 0; i < storesArray.length(); i++) {
                JSONObject storeObject = storesArray.getJSONObject(i);
                String storeName = storeObject.getString("place_name");
                String storeAddress = storeObject.getString("address_name");
                String storeDistance = storeObject.getString("distance");

                // Store 객체 생성 및 리스트에 추가
                Store store = new Store(storeName, storeAddress, Double.parseDouble(storeDistance));
                storeList.add(store);
            }

            // 거리순으로 정렬
            Collections.sort(storeList, new Comparator<Store>() {
                @Override
                public int compare(Store s1, Store s2) {
                    return Double.compare(s1.getDistance(), s2.getDistance());
                }
            });

            // 리스트뷰에 추가
            for (Store store : storeList) {
                String storeInfo = "Name: " + store.getName() + "\nAddress: " + store.getAddress() + "\nDistance: " + store.getDistance() + "m";
                storeAdapter.add(storeInfo);
            }

            // 리스트뷰 갱신
            storeAdapter.notifyDataSetChanged();
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }

    // Store 클래스 정의
    private static class Store {
        private String name;
        private String address;
        private double distance;

        public Store(String name, String address, double distance) {
            this.name = name;
            this.address = address;
            this.distance = distance;
        }

        public String getName() {
            return name;
        }

        public String getAddress() {
            return address;
        }

        public double getDistance() {
            return distance;
        }
    }



}
