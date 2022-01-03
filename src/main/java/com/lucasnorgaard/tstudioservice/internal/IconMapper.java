package com.lucasnorgaard.tstudioservice.internal;


import lombok.AllArgsConstructor;

public class IconMapper implements Runnable {


    @Override
    public void run() {

    }

    @AllArgsConstructor
    private static class IconMapperTask implements Runnable {

        private String url;


        @Override
        public void run() {

        }
    }
}
