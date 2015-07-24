import com.jaunt.Element;
import com.jaunt.Elements;
import com.jaunt.JauntException;
import com.jaunt.Node;
import com.jaunt.UserAgent;

import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Pattern;

/**
 * Created by Shazambom on 7/22/2015.
 */
public class ThreadRipper {
    public static void RipThread(String url, String filePath) {
        try {
            UserAgent userAgent = new UserAgent();
            userAgent.visit(url);
            System.out.println(userAgent.doc.getUrl());
            Elements fileNames = userAgent.doc.findEach("<a class=\"fileThumb\">");
            System.out.println(fileNames.size() + " images found");
            ArrayList<String> links = new ArrayList<String>();
            ArrayList<String> names = new ArrayList<String>();
            for (Element element: fileNames) {
                links.add(element.toString());
            }
            for (int i = 0; i < links.size(); i++) {
                links.set(i, parseLink(links.get(i)));
                names.add(parseFileName(links.get(i)));
            }

            System.out.print("[");
            int failures = downloadImages(links, names, userAgent, filePath);
            System.out.println("]");
            System.out.println(links.size() - failures + " images successfully downloaded");


        } catch(JauntException e) {
            e.printStackTrace();
        }
    }
    private static int downloadImages(List<String> links, List<String> names, UserAgent userAgent, String filePath) {
        int failures = 0;
        double percentageComplete;
        if (links.size() < 10) {
            percentageComplete = 1;
        } else {
            percentageComplete = links.size()/10;
        }
        for (int i = 0; i < links.size(); i++) {
            try {
                userAgent.download(links.get(i), new File(filePath + names.get(i)));
            } catch (JauntException e) {
                System.out.println("\nFile: "+ (i + 1) + " at the url: " + links.get(i) + " failed to download");
                failures += downloadImages(links.subList(i + 1, links.size()), names.subList(i + 1, names.size()), userAgent, filePath);
                failures++;
                i = links.size();
            }
            if (i != 0 && i % percentageComplete == 0) {
                System.out.print("∎");
            }
            try {
                Thread.sleep(1000);
            } catch (Exception e) {
                Thread.currentThread().interrupt();
            }
        }
        return failures;
    }


    private static String parseLink(String link) {
        String toReturn = "";
        for (int i = 0; i < link.length(); i++) {
            if (link.substring(i, i + 7).equals("http://")){
                while (link.charAt(i) != '\"') {
                    toReturn += link.charAt(i);
                    i++;
                }
                return (toReturn);
            }
        }
        return toReturn;
    }
    private static String parseFileName(String link) {
        for (int i = 0; i < link.length(); i++) {
            if (link.substring(i, i + 3).equals("/w/")) {
                return link.substring(i + 4);
            }
        }
        return "";
    }


    public static ArrayList<String> getThreads(String url) {
        ArrayList<String> threadUrls = new ArrayList<String>();
        try {
            UserAgent userAgent = new UserAgent();
            userAgent.visit(url);
            Elements clickHere = userAgent.doc.findEvery("<a class=\"replylink\">");
            for (Element element: clickHere) {
                threadUrls.add(parseLink(element.toString()));
            }
            try {
                userAgent.doc.submit("Next");
            } catch (JauntException e) {
                return threadUrls;
            }
            threadUrls.addAll(getThreads(userAgent.doc.getUrl()));

        } catch(JauntException e) {
            e.printStackTrace();
        }


        return threadUrls;
    }
}
