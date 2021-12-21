package com.lucasnorgaard.tstudioservice.internal;

import com.lucasnorgaard.tstudioservice.Application;
import com.lucasnorgaard.tstudioservice.Utils;

public class LanguageTask implements Runnable {


    @Override
    public void run() {
        Application.setLanguages(Utils.getLanguages());
    }
}
