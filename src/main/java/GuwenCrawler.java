import com.google.common.io.Files;
import edu.uci.ics.crawler4j.crawler.Page;
import edu.uci.ics.crawler4j.crawler.WebCrawler;
import edu.uci.ics.crawler4j.parser.HtmlParseData;
import edu.uci.ics.crawler4j.url.WebURL;

import java.io.*;
import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.regex.Pattern;


/**
 * Created by Administrator on 2016/11/12.
 * http://www.gushiwen.org/wenyan/
 */
public class GuwenCrawler extends WebCrawler {

    private final static Pattern FILTERS = Pattern.compile(".*(\\.(css|js|gif|jpg"
            + "|png|mp3|mp3|zip|gz))$");


    //    public static final String targetDomain = "http://www.gushiwen.org/";
    public static final String seed = "http://www.gushiwen.org/wenyan/";
    public static final String localPath = "data/local/guwen";

    private static File storageFolder;
    private static File htmlStorageFolder;
    private static File textStorageFolder;


    private static String crawlDomains = "gushiwen.org";

    static {
//        crawlDomains = crawlDomains;

        storageFolder = new File(localPath);
        htmlStorageFolder = new File(localPath + "/html/");
        if (!htmlStorageFolder.exists()) {
            htmlStorageFolder.mkdirs();
        }
        textStorageFolder = new File(localPath + "/text/");
        if (!textStorageFolder.exists()) {
            textStorageFolder.mkdirs();
        }

        if (!storageFolder.exists()) {
            storageFolder.mkdirs();
        }
    }

//    public static void init(String domain, String storageFolderName) {
//
//    }

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
        System.out.println("GuwenCrawler shouldVisit URL: " + url);
        String href = url.getURL().toLowerCase();
        return !FILTERS.matcher(href).matches() && href.contains(crawlDomains);
    }

    /**
     * This function is called when a page is fetched and ready
     * to be processed by your program.
     */
    @Override
    public void visit(Page page) {
        String url = page.getWebURL().getURL();
        System.out.println("GuwenCrawler visit URL: " + url);


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
            resourcename = resourcename.replace('?', '_');
            resourcename = resourcename.replace('/', '_');
            resourcename = resourcename.replace('&', '_');
            resourcename = resourcename.replace('=', '_');
            resourcename = resourcename.replace('|', '_');
            // store image
            String htmlFileName = htmlStorageFolder.getAbsolutePath() + "/" + resourcename;


            try {
                Files.write(page.getContentData(), new File(htmlFileName));
                System.out.println("write to file " + htmlFileName);

                Article article = formatText(url, text);
                String cleanText = text;
                String errTag ="";
                String textFileName = "";
                if (article != null) {
                    cleanText = article.data;
                    textFileName = textStorageFolder.getAbsolutePath() + "/" + errTag+"_" + article.author + "_" + article.title + "_" + resourcename + ".txt";
                } else {
                    errTag ="error";
                    textFileName = textStorageFolder.getAbsolutePath() + "/" + errTag+"_" + resourcename + ".txt";
                }




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


    String[] tailTags = new String[]{"© 2016 古诗文网", "古诗大全", "写赏析", "评语"};
    String[] prefixTags = new String[]{"android · iphone", "首页诗文名句典籍随便看看我的收藏app下载", "先秦\n" +
            "两汉\n" +
            "魏晋\n" +
            "南北朝\n" +
            "隋代\n" +
            "唐代\n" +
            "五代\n" +
            "宋代\n" +
            "金朝\n" +
            "元代\n" +
            "明代\n" +
            "清代",
            "首页\n" +
            "诗文\n" +
            "名句\n" +
            "典籍\n" +
            "作者\n" +
            "随便看看\n" +
            "我的收藏\n" +
            "app下载"};
//    String[] frontTags = new String[]{"分享到", "© 2014 guwen-online.com", "常见问题"};

    public Article formatText(String url, String textContent) {
        if (textContent == null || textContent.trim().length() == 0) {
            return null;
        }


        for (String tag : prefixTags) {
            if (textContent.contains(tag)) {
                int timeIndex = textContent.indexOf(tag);
                textContent = textContent.substring(timeIndex + tag.length());
            }
        }

        for (String tailTag : tailTags) {
            if (textContent.contains(tailTag)) {
                textContent = textContent.substring(0, textContent.indexOf(tailTag));
            }
        }
        textContent = textContent.trim();
        StringBuffer textBuffer = new StringBuffer();

        List<String> data = new ArrayList<>();

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
                data.add(line.trim());

            }
        } catch (Exception e) {
            e.printStackTrace();
        }

        if (data.size() > 0)
            data.remove(data.size() - 1);

        if (data.size() == 0) {
            return null;
        }

        data.add(0, url);
        for (String s : data) {
            textBuffer.append(s + "\r\n");
        }

        if (data.size() < 4) {
            return null;
        }
        Article article = new Article();
        article.url = data.get(0);
        article.author = data.get(1);
        article.title = data.get(2);
        article.content = data.get(3);
        article.data = textBuffer.toString();

        return article;
    }


    public static void main(String[] args) throws Exception {


    }
}