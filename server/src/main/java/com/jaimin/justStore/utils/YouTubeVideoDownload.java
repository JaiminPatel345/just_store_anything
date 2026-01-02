package com.jaimin.justStore.utils;


import jakarta.annotation.PostConstruct;
import org.apache.commons.exec.CommandLine;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.io.InputStream;


@Component
public class YouTubeVideoDownload {

    private static String YT_DLP_PATH;

    @Value("${yt_dlp.path:/home/linuxbrew/.linuxbrew/bin/yt-dlp}")
    private String ytDlpPathTemp;

    @PostConstruct
    public void init() {
        YT_DLP_PATH = ytDlpPathTemp;
    }

    public String getYtDlpPathTemp() {
        return ytDlpPathTemp;
    }

    public void setYtDlpPathTemp(String ytDlpPathTemp) {
        this.ytDlpPathTemp = ytDlpPathTemp;
    }

    public static InputStream downloadVideo(String videoUrl) throws IOException {
        System.out.println("In downlod" + YT_DLP_PATH);
        CommandLine cmdLine = new CommandLine(YT_DLP_PATH);

        // Best video only, no audio
        cmdLine.addArgument("-f");
        cmdLine.addArgument("bestvideo");

        // Output to stdout
        cmdLine.addArgument("-o");
        cmdLine.addArgument("-");

        cmdLine.addArgument(videoUrl);

        ProcessBuilder processBuilder = new ProcessBuilder(cmdLine.toStrings());
        Process process = processBuilder.start();

        return process.getInputStream();
    }
}