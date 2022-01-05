package com.lucasnorgaard.tstudioservice.internal;

import com.lucasnorgaard.tstudioservice.Application;
import com.lucasnorgaard.tstudioservice.models.Preset;
import okhttp3.Request;
import okhttp3.Response;
import org.apache.commons.io.FileUtils;

import java.io.*;

public class DownloadPresetVSIX implements Runnable {

    public Preset preset;

    public DownloadPresetVSIX(Preset preset) {
        this.preset = preset;
    }

    @Override
    public void run() {
        System.out.println(preset.download_url);
        Request request = new Request.Builder().url(preset.download_url).build();

        try (Response response = Application.getHttpClient().newCall(request).execute()) {
            System.out.println(response.headers().get("Retry-After"));
            System.out.println(response.headers().get("X-RateLimit-Remaining"));
            System.out.println(response.headers().get("X-RateLimit-Limit"));


            System.out.println(response.body().contentType());
            System.out.println(response.code());
            System.out.println(response.body().byteStream());
            InputStream g = response.body().byteStream();


            File tempFile = File.createTempFile( "myfile", ".vsix" , new File("./"));
            FileUtils.copyToFile( g, tempFile );
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
