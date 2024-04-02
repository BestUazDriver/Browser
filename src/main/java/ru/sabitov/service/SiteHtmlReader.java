package ru.sabitov.service;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.ProtocolException;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;

public class SiteHtmlReader {

    public List<String> generateSitemap(String targetUrl) {
        List<String> contentUrls = new ArrayList<>();

        try {
            URL url = new URL(targetUrl);
            HttpURLConnection connection = (HttpURLConnection) url.openConnection();
            connection.setRequestMethod("GET");

            BufferedReader reader = new BufferedReader(new InputStreamReader(connection.getInputStream()));
            String line;
            while ((line = reader.readLine()) != null) {
                if (line.contains("app-list-logo")) {
                    contentUrls.add(extractHref(line));
                }
            }
            reader.close();
        } catch (IOException e) {
            throw  new IllegalArgumentException("Could't read HTML", e);
        }

        generateTxtSitemap(contentUrls);
        writeHtmlByUrlList(contentUrls);

        return contentUrls;
    }

    private void generateTxtSitemap(List<String> contentUrls) {
        try (BufferedWriter bufferedWriter = new BufferedWriter(new FileWriter("src/main/resources/index.txt"))){
            for (String url : contentUrls) {
                bufferedWriter.write("https://startpack.ru/" +  url);
                bufferedWriter.newLine();
            }
        } catch (IOException e) {
            throw new IllegalArgumentException("Couldn't write url's to file", e);
        }
    }

    private void writeHtmlByUrlList(List<String> contentUrls) {
        try {
            for (String url : contentUrls) {
                URL source = new URL("https://startpack.ru/" + url);
                HttpURLConnection connection = (HttpURLConnection) source.openConnection();
                connection.setRequestMethod("GET");

                BufferedWriter bufferedWriter = new BufferedWriter(new FileWriter("src/main/resources/html/" + url.replaceAll("application/", "") + ".html"));
                BufferedReader reader = new BufferedReader(new InputStreamReader(connection.getInputStream()));
                String line;
                boolean ignoreTag = false;
                while ((line = reader.readLine()) != null) {

                    if (line.contains("<script")){
                        ignoreTag = true;
                        continue;
                    }

                    if (line.contains("</script>")){
                        ignoreTag = false;
                        continue;
                    }

                    if (!ignoreTag) {
                    bufferedWriter.write(line);
                    bufferedWriter.newLine();
                    }
                }
                reader.close();

            }
        } catch (ProtocolException e) {
            throw new IllegalArgumentException("Could't read source HTML", e);
        } catch (IOException e) {
            throw new IllegalArgumentException("Could't connect by source URL", e);
        }

    }

    public static String extractHref(String html) {
        String hrefStr = "href='/";
        int start = html.indexOf(hrefStr) + hrefStr.length();
        int end = html.indexOf("'", start);
        return html.substring(start, end);
    }

}
