package com.example.dossa_crawler;

import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Intent;
import android.net.Uri;
import android.os.IBinder;
import android.util.Log;
import android.widget.EditText;
import android.widget.Toast;


import androidx.core.app.NotificationCompat;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.select.Elements;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.StringTokenizer;

public class crawler_service extends Service {

    private boolean running;

    class Param {
        public ArrayList<CharSequence> chrs;
        public String period;

        public Param(String period, ArrayList<CharSequence> chrs){
            this.chrs = chrs; this.period = period;
        }
    }

    /**
     *    dossa는 ajax를 이용하기 때문에 가격에 대한 정보를 받아오기 위해서는 http request를 2번 적용해야 한다.
     *    1. 검색으로 최신 게시물 확인
     *    2. 게시물에 해당하는 id로 다시 http request하여 가격 확인
     *
     *    귀찮기 때문에 걍 최신 매물 알림만 적용하자
      */

    class crawler_thread implements Runnable {
        int period;
        ArrayList<String> queries;

        public crawler_thread(Param param){
            try {
                period = Integer.parseInt(param.period);
            }catch (NumberFormatException e){
                e.printStackTrace();
                period = 10;
            }

            queries = new ArrayList<String>();
            for(CharSequence chr : param.chrs) queries.add(chr.toString());
        }

        public void run() {
            RequestQueue queue = Volley.newRequestQueue(getApplicationContext());
            String url_base = "http://www.corearoadbike.com/board/board.php?t_id=Menu30Top6&sort=wr_num%2C+wr_reply&sch_W=title&sch_O=AND&sch_T=";

            running = true;
            while (running) {
                for (String query : queries) {
                    Log.d("dossa_log", "request for " + query);

                    String url = url_base + query;
                    // Request a string response from the provided URL.
                    StringRequest stringRequest = new StringRequest(Request.Method.GET, url,
                            new Response.Listener<String>() {
                                @Override
                                public void onResponse(String response) {
                                    Document doc = Jsoup.parse(response);
                                    Elements elements = doc.getElementsByClass("hand");

                                    String str = elements.get(18).text();
                                    Log.d("dossa_log", str);
                                    StringTokenizer stl = new StringTokenizer(str, "|");

                                    ArrayList<String> pstr = new ArrayList<>();
                                    while (stl.hasMoreTokens()) pstr.add(stl.nextToken());

                                    if(pstr.size() != 4) {
                                    }else if (pstr.get(2).contains(":")) {

                                        SimpleDateFormat fmt = new SimpleDateFormat(" HH:mm:ss");

                                        Date d_board = null;
                                        Date d_curr = null;
                                        try {
                                            d_board = fmt.parse(pstr.get(2));
                                            d_curr = fmt.parse(fmt.format(new Date()));
                                        } catch (Exception e) {
                                            e.printStackTrace();
                                        }

                                        long sec_diff = (d_curr.getTime() - d_board.getTime()) / 1000;
                                        Log.d("dossa_log", String.valueOf(sec_diff));

                                        if (sec_diff < period) {//period){
                                            Intent notificationIntent = new Intent(Intent.ACTION_VIEW, Uri.parse(url));
                                            PendingIntent pendingIntent =
                                                    PendingIntent.getActivity(getApplicationContext(), 0, notificationIntent, 0);

                                            NotificationCompat.Builder builder =
                                                    new NotificationCompat.Builder(getApplicationContext(), "push")
                                                            .setContentTitle("Dossa Crawler")
                                                            .setContentText("found!")
                                                            .setSmallIcon(R.drawable.ic_launcher_foreground)
                                                            .setContentIntent(pendingIntent);


                                            NotificationChannel channel = new NotificationChannel("push", "name", NotificationManager.IMPORTANCE_DEFAULT);
                                            NotificationManager notificationManager = getSystemService(NotificationManager.class);
                                            notificationManager.createNotificationChannel(channel);


                                            // notificationId is a unique int for each notification that you must define
                                            notificationManager.notify(2, builder.build());

                                            running = false;
                                        }
                                    } else {
                                        Log.d("dossa_log", "date does not contain :");
                                    }
                                }
                            }, new Response.ErrorListener() {
                        @Override
                        public void onErrorResponse(VolleyError error) {
                            Log.d("dossa_log", error.toString());
                        }
                    });

                    // Add the request to the RequestQueue.
                    queue.add(stringRequest);
                    try {
                        Thread.sleep(period * 1000);
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }
            }
        }
    }

    @Override
    public void onCreate() {
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        Toast.makeText(this, "service starting", Toast.LENGTH_SHORT).show();

        if(intent == null) Log.d("first_frag", "null intent");
        else {

            String period = intent.getExtras().getString("period");
            ArrayList<CharSequence> chrs = intent.getExtras().getCharSequenceArrayList("query");
            Param param = new Param(period, chrs);

            Intent notificationIntent = new Intent(this, MainActivity.class);
            PendingIntent pendingIntent =
                    PendingIntent.getActivity(this, 0, notificationIntent, 0);

            Notification notification =
                    new Notification.Builder(this, "channel")
                            .setContentTitle("Dossa Crawler")
                            .setContentText("running")
                            .setSmallIcon(R.drawable.ic_launcher_foreground)
                            .setContentIntent(pendingIntent)
                            .build();

            // Notification ID cannot be 0.
            startForeground(1, notification);
            Runnable r = new crawler_thread(param);
            new Thread(r).start();

        }

        // If we get killed, after returning from here, restart
        return START_STICKY;
    }

    @Override
    public IBinder onBind(Intent intent) {
        // We don't provide binding, so return null
        return null;
    }

    @Override
    public void onDestroy() {
        Toast.makeText(this, "service done", Toast.LENGTH_SHORT).show();
        running = false;
    }
}
