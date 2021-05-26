package com.example.demo.service;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.text.SimpleDateFormat;
import java.util.Date;

import org.springframework.stereotype.Repository;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

import enity.LichHen;
import enity.Role;

@Repository
public class LichHenService {
	
	static String POST_LICH_HEN="http://localhost:5001/lichhen/insert";
	static String GET_LICH_HEN_TRONG_NGAY="http://localhost:5001/lichhen/ktralichhenbn";
	
	
	/**
	 * @author Vien
	 * date : 12/4/2021
	 * @return Thêm lich hen vào cơ sở dữ liệu 
	 * @decripstion : Thêm lich hen bằng cái sử dụng RestFull API
	 * */
	//[START POST Lich Hen]
	public  int POSTLichHen(LichHen lh) throws IOException {

		Gson gson = new GsonBuilder()
    		    .setDateFormat("yyyy-MM-dd")
    		    .create();
		String POST_PARAMS = gson.toJson(lh);
	    System.out.println(POST_PARAMS);
	    URL obj = new URL(POST_LICH_HEN);
	    HttpURLConnection postConnection = (HttpURLConnection) obj.openConnection();
	    postConnection.setRequestMethod("POST");
	    postConnection.setRequestProperty("Content-Type","application/json;charset=utf8");


	    postConnection.setDoOutput(true);
	    OutputStream os = postConnection.getOutputStream();
	    os.write(POST_PARAMS.getBytes());
	    os.flush();
	    os.close();


	    int responseCode = postConnection.getResponseCode();
	    System.out.println("POST Response Code :  " + responseCode);
	    System.out.println("POST Response Message : " + postConnection.getResponseMessage());

	    if (responseCode == HttpURLConnection.HTTP_CREATED) { //success
	        BufferedReader in = new BufferedReader(new InputStreamReader(
	            postConnection.getInputStream()));
	        String inputLine;
	        StringBuffer response = new StringBuffer();

	        while ((inputLine = in .readLine()) != null) {
	            response.append(inputLine);
	        } in .close();

	        // print result
	        System.out.println(response.toString());
	    } else {
	        System.out.println("POST NOT WORKED");
	    }
	    return responseCode;
	}
	//[End POST Request]

	public String doichuoitungay(Date date) {
		SimpleDateFormat formatter = new SimpleDateFormat("yyyy-MM-dd");
		String dateFormat = formatter.format(date);
		return dateFormat;
	}
	
	public LichHen GetLichHenBenhNhan(String date,Long id) throws IOException {
		LichHen lh=new LichHen();
		URL urlForGetRequest = new URL(GET_LICH_HEN_TRONG_NGAY+"/"+date+"/"+id);
		String readLine = null;
		HttpURLConnection conection = (HttpURLConnection) urlForGetRequest.openConnection();
		conection.setRequestMethod("GET"); // set userId its a sample here
		conection.setRequestProperty("Content-Type", "application/json");
		int responseCode = conection.getResponseCode();


		if (responseCode == HttpURLConnection.HTTP_OK) {
			BufferedReader in = new BufferedReader(
					new InputStreamReader(conection.getInputStream()));
			String response = new String();
			while ((readLine = in .readLine()) != null) {
				response+=(readLine);
			} in .close();
			
			Gson gson = new Gson();
			lh = gson.fromJson(response, LichHen.class);

			
		} else {
			System.out.println("GET NOT WORKED");
		}

		return lh;
	}
}
