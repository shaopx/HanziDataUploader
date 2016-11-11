
import com.google.common.io.Files;
import edu.uci.ics.crawler4j.crawler.Page;
import edu.uci.ics.crawler4j.crawler.WebCrawler;
import edu.uci.ics.crawler4j.parser.HtmlParseData;
import edu.uci.ics.crawler4j.url.WebURL;

import java.io.*;
import java.nio.charset.Charset;
import java.util.Set;
import java.util.regex.Pattern;

/**
 * Created by shaopengxiang on 2016/11/11.
 */
public class MyCrawler extends WebCrawler {

    private final static Pattern FILTERS = Pattern.compile(".*(\\.(css|js|gif|jpg"
            + "|png|mp3|mp3|zip|gz))$");


    private static File storageFolder;
    private static File htmlStorageFolder;
    private static File textStorageFolder;


    private static String crawlDomains;

    public static void configure(String domain, String storageFolderName) {
        crawlDomains = domain;

        storageFolder = new File(storageFolderName);
        htmlStorageFolder = new File(storageFolderName + "/html/");
        if (!htmlStorageFolder.exists()) {
            htmlStorageFolder.mkdirs();
        }
        textStorageFolder = new File(storageFolderName + "/text/");
        if (!textStorageFolder.exists()) {
            textStorageFolder.mkdirs();
        }

        if (!storageFolder.exists()) {
            storageFolder.mkdirs();
        }
    }

    /**
     * This method receives two parameters. The first parameter is the page
     * in which we have discovered this new url and the second parameter is
     * the new url. You should implement this function to specify whether
     * the given url should be crawled or not (based on your crawling logic).
     * In this example, we are instructing the crawler to ignore urls that
     * have css, js, git, ... extensions and to only accept urls that start
     * with "http://www.ics.uci.edu/". In this case, we didn't need the
     * referringPage parameter to make the decision.
     */
    @Override
    public boolean shouldVisit(Page referringPage, WebURL url) {
        String href = url.getURL().toLowerCase();
        return !FILTERS.matcher(href).matches() && href.startsWith(crawlDomains);
    }

    /**
     * This function is called when a page is fetched and ready
     * to be processed by your program.
     */
    @Override
    public void visit(Page page) {
        String url = page.getWebURL().getURL();
        System.out.println("URL: " + url);


        if (page.getParseData() instanceof HtmlParseData) {
            HtmlParseData htmlParseData = (HtmlParseData) page.getParseData();
            String text = htmlParseData.getText();
            String html = htmlParseData.getHtml();
            Set<WebURL> links = htmlParseData.getOutgoingUrls();

            // get a unique name for storing this image
            String resourcename = url.substring(url.lastIndexOf('/'));
//            System.out.println("htmlfilename:" + htmlfilename);
            if (resourcename.equals("/")) {
                resourcename = "rootindex.html";
            }
            // store image
            String htmlFileName = htmlStorageFolder.getAbsolutePath() + "/" + resourcename;
            String textFileName = textStorageFolder.getAbsolutePath() + "/" + toTextSuffix(resourcename);

            try {
                Files.write(page.getContentData(), new File(htmlFileName));
                System.out.println("write to file " + htmlFileName);

                String cleanText = formatText(url, text);
                Files.write(cleanText, new File(textFileName), Charset.forName("utf-8"));
                System.out.println("write to file " + textFileName);
            } catch (Exception iox) {
                iox.printStackTrace();
                System.out.println(iox.getMessage());
            }

//            System.out.println("text: " + text);
//            System.out.println("Html length: " + html.length());
//            System.out.println("Number of outgoing links: " + links.size());
        }
    }


    public String toTextSuffix(String fileName) {
        if (fileName.endsWith(".html")) {
            fileName = fileName.substring(0, fileName.length() - 4) + "txt";
        } else if (fileName.endsWith(".htm")) {
            fileName = fileName.substring(0, fileName.length() - 3) + "txt";
        }
        return fileName;
    }


    String[] tailTags = new String[]{"分享到", "© 2014 guwen-online.com", "常见问题"};
//    String[] frontTags = new String[]{"分享到", "© 2014 guwen-online.com", "常见问题"};

    public String formatText(String url, String textContent) {
        if (textContent == null || textContent.trim().length() == 0) {
            return "";
        }

        if (textContent.contains("日 星期")) {
            int timeIndex = textContent.indexOf("日 星期");
            if (timeIndex < 12 && timeIndex > 8) {
                textContent = textContent.substring(timeIndex + 5);
            }
        }

        for (String tailTag : tailTags) {
            if (textContent.contains(tailTag)) {
                textContent = textContent.substring(0, textContent.indexOf(tailTag));
            }
        }
        textContent = textContent.trim();
        StringBuffer textBuffer = new StringBuffer();

        ByteArrayInputStream stream
                = new ByteArrayInputStream(textContent.getBytes());
        BufferedReader br = new BufferedReader(new InputStreamReader(stream));
        String line = "";
        try {
            while ((line = br.readLine()) != null) {
                line = line.trim();
                if (line.length() == 0) {
                    continue;
                }
                if (line.equals("小中大")) {
                    continue;
                }
                if (line.equals("普通阅读模式")) {
                    continue;
                }

                textBuffer.append(line.trim() + "\r\n");
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

        textBuffer.insert(0, url + "\r\n");
        return textBuffer.toString().trim();
    }


    public static void main(String[] args) throws Exception {


    }
}