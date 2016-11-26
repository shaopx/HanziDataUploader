import groovy.json.JsonBuilder
import groovy.json.JsonOutput
import groovyx.net.http.HTTPBuilder
import org.cyberneko.html.parsers.SAXParser

import static Utils.errorText
import static Utils.formatException
import static Utils.errorText
import static Utils.formatString
import static Utils.saveToFile
import static Utils.*
import static groovyx.net.http.ContentType.TEXT
import static groovyx.net.http.Method.GET


/**
 * Created by SHAOPENGXIANG on 2016/11/26.
 * 对习古网进行数据抓取
 * http://www.xigutang.com
 */
class XiGuLoader {

    def host = 'http://www.xigutang.com'
    def indexLinkPrefix = 'gushi/list_90_'

    def rootDir = "c:/dev/data/poem/xigutang/";
    def allUrlFileName = rootDir + "list/all_poemslink.txt";

    //def tags=['【作品介绍】':'intro', '【原文】':'yuanwen', '【注释】':'zhushi', '【作者介绍】':'zuozhejieshao','【繁体对照】':'']

    def pages = []
    def poemlinks = []
    def errorLinks = []

    def http = new HTTPBuilder()


    void perform() {
        //第一次需要运行, 获取所有url, 保存到文件中
        //getAndSaveAllLinks()

        //从文件中读取所有url, 并逐个解析
        readAllLinksAndParse()
    }

    void readAllLinksAndParse() {
        new File(allUrlFileName).eachLine {
            url ->
                println url
                def content = getUrlTextContent(rootDir, url);

                try {
                    parsePoemHtml(url, content)
                } catch (Throwable ex) {
                    println "exception when " + url
                    ex.printStackTrace()
                    def exception = formatException(ex);
                    //errorText(rootDir, url, "error:" + exception);
                    errorLinks.add(url)
                }
        }


        errorText(rootDir, "error_total", "" + errorLinks.toString())
    }

    def parsePoemHtml(String url, String content) {


        def author = getAuthor(content)
        println "作者:" + author

        def title = getTitle(content)
        println "题目:" + title

        if (!author || !title) {
            errorText(rootDir, "error", "" + url)
            errorLinks << url
            return;
        }

        def poemData = getYuanWen(content)
        //println "原文:" + poemData.get("【原文】")


        def builder = new JsonBuilder()
        // 构建json格式的poem
        builder.poem {
            n title + ""
            a author + ""
            l "" + url
            data poemData
        }


        def finalData = JsonOutput.prettyPrint(builder.toString())
        //println finalData

        def fname = author + "_" + title + ".json"
        fname = formatString(fname)
        saveToFile(rootDir, "gushi", fname, finalData)
    }

    def getYuanWen(String text) {
        def parser = new SAXParser()

        def page = new XmlSlurper(parser).parseText(text)

        def divs = page."**".findAll {
            it.name() == 'DIV' && it.@class == 'content'
        }

        def divNode = divs[0]
        def divStr = divNode.toString()
        println divStr

        //println '----------------------------------------'
        def matcher = divStr =~ ~/\s+【(.*?)】(\n)*/
        def tags = []
        if (matcher) {
            //println line

            matcher.each {
                //println it[1]
                def tag = it[0].toString().trim()
                if (!(tag in tags))
                    tags << tag
            }

        }

        println tags
        def poemData = [:]
        for (int i = 0; i < tags.size(); i++) {
            def fromTag = tags.get(i)
            def toTag = i < tags.size() - 1 ? tags.get(i + 1) : null
            def tagValue = getSubstrBetweenTags(divStr, fromTag, toTag);
            println fromTag + ":" + tagValue
            poemData[fromTag.toString()] = tagValue.toString()
        }


        return poemData
    }

    String getSubstrBetweenTags(String text, String tag1, String tag2) {
        int index1 = text.indexOf(tag1) + tag1.length();
        int index2 = text.length();
        if (tag2 != null && tag2.length() > 0) {
            index2 = text.indexOf(tag2);
        }
        String value = text.substring(index1, index2).trim();
        value = value.replaceAll("　", "");
        value = value.replaceAll(" ", "");
        value = value.replaceAll("\t", "");
        value = value.trim()
        return value;
    }

    def getTitle(String text) {
        def matcher = text =~ ~/\<h1\>《(.*?)》/
        def title = ''
        if (matcher) {
            //println line

            matcher.each {
                //println it
                title = it[1]
            }

        }

        if (!title) {
            matcher = text =~ ~/\<h1\>(.*?)\<\/h1\>/
            if (matcher) {
                //println line

                matcher.each {
                    //println it
                    title = it[1]
                }

            }
        }

        return title
    }

    def getAuthor(String text) {

        def authorlist = []
        def author = ''
//        text.eachLine { line ->

        def matcher = text =~ ~/作者[:：]?(.*?)\)?唐诗赏析/
        //println 'matcher:'+matcher.getClass().getName()
        if (matcher) {
            //println line

            matcher.each {
                //println it
                author = it[1]
                //println author
                if (author && !(author in authorlist)) {
                    authorlist << author
                }
            }

        }
        //println matcher.getClass().getName()

//        }
        if (authorlist.size() < 1) {
            matcher = text =~ ~/》(.*?)唐诗鉴赏/
            //println 'matcher:'+matcher.getClass().getName()
            if (matcher) {
                //println line

                matcher.each {
                    //println it
                    author = it[1]
                    //println author
                    if (author && !(author in authorlist)) {
                        authorlist << author
                    }
                }

            }
        }

        if (authorlist.size() > 0) {
            return authorlist[0]
        }
    }

    void getAndSaveAllLinks() {
        getAllLinks()

        pages.each { pageLink ->
            requestPage(pageLink)
        }

        def allpoemLinksSb = '';
        poemlinks.each {
            println it
            allpoemLinksSb += it + "\r\n"
        }
        saveToFile(rootDir, "list", "all_poemslink.txt", allpoemLinksSb)
        println "共有" + poemlinks.size() + "首诗"
    }

    def getAllLinks() {
        def allPagesSb = '';
        (1..359).each {
            def url = host + "/" + indexLinkPrefix + it + ".html"
            println url
            pages << url
            allPagesSb += url + "\r\n"
        }
        saveToFile(rootDir, "list", "page_list.txt", allPagesSb)
        println "共有" + pages.size() + "页"
    }


    def requestPage(link) {
        println 'request ' + link + ", poemlinks.size:" + poemlinks.size()
        try {

            URL getUrl = new URL(link);
            HttpURLConnection connection = (HttpURLConnection) getUrl.openConnection();
            // 进行连接，但是实际上get request要在下一句的connection.getInputStream()函数中才会真正发到
            // 服务器
            connection.connect();
            // 取得输入流，并使用Reader读取

            BufferedReader reader = new BufferedReader(new InputStreamReader(connection.getInputStream(), "GBK"));

//            System.out.println("=============================");
//            System.out.println("Contents of get request");
//            System.out.println("=============================");
            String lines;
            StringBuilder sb = new StringBuilder()
            while ((lines = reader.readLine()) != null) {
                //lines = new String(lines.getBytes(), "utf-8");
                //System.out.println(lines);
                sb.append(lines)
            }
            reader.close();
            // 断开连接
            connection.disconnect();

            handlePageHtml(link, sb.toString())
        } catch (Throwable ex) {
            println "exception when " + link
            ex.printStackTrace()
            def exception = formatException(ex);
            errorText(rootDir, link, "error:" + exception);
            //errorLinks.add(link)
        }
    }


    def handlePageHtml(link, text) {
        //println text
        def parser = new SAXParser()

        def page = new XmlSlurper(parser).parseText(text)

        def links = page."**".findAll {
            it.name() == 'A' && it.@class == 'title' && it.@target == '_blank'
        }.collect() {
            host + it.@href
        }

        poemlinks.addAll(links)

    }

    static void main(args) {
        new XiGuLoader().perform()
    }
}
