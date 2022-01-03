package com.lucasnorgaard.tstudioservice.internal;

import com.lucasnorgaard.tstudioservice.Application;
import okhttp3.MediaType;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;

import java.io.IOException;

public class ThemeTask implements Runnable {


    @Override
    public void run() {


        try {
            String query = "{\"assetTypes\":[\"Microsoft.VisualStudio.Services.Icons.Default\",\"Microsoft.VisualStudio.Services.Icons.Branding\",\"Microsoft.VisualStudio.Services.Icons.Small\"],\"filters\":[{\"criteria\":[{\"filterType\":8,\"value\":\"Microsoft.VisualStudio.Code\"},{\"filterType\":10,\"value\":\"target:\\\"Microsoft.VisualStudio.Code\\\" \"},{\"filterType\":12,\"value\":\"37888\"},{\"filterType\":5,\"value\":\"Themes\"}],\"direction\":2,\"pageSize\":100,\"pageNumber\":1,\"sortBy\":4,\"sortOrder\":0,\"pagingToken\":null}],\"flags\":870}";
            RequestBody requestBody = RequestBody.create(query, MediaType.get("application/json"));
            Request request = new Request.Builder().url("https://marketplace.visualstudio.com/_apis/public/gallery/extensionquery").post(requestBody).build();

            try (Response response = Application.getHttpClient().newCall(request).execute()) {
                int code = response.code();

                if (code != 200) return;
                System.out.println(response.body().string());
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
