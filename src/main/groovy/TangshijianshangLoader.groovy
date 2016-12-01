import groovy.json.JsonBuilder
import groovy.json.JsonOutput
import groovyx.net.http.HTTPBuilder
import org.cyberneko.html.parsers.SAXParser

import static util.Utils.errorText
import static util.Utils.formatString
import static util.Utils.saveToFile
import static util.Utils.*
import static groovyx.net.http.ContentType.TEXT
import static groovyx.net.http.Method.GET

/**
 * Created by Administrator on 2016/11/20.
 */
class TangshijianshangLoader {

    def rootDir = "c:/dev/data/poem/tagnshijianshang/";

    def links = []
    def errorLinks = []
    def authorList = ['张继','孟云卿','岑参','李华','卢纶','严维','胡令能','景云','民谣','杜甫','王维',
                      '刘方平','裴迪','元结','钱起','贾至','郎士元','韩翃','司空曙','皎然',
                      '李端','胡令能','顾况','韩氏','窦叔向','严武','张潮','于良史','柳中庸','韦应物','李益','李白']

    void perform() {
        try {
            requestIndexs()

            for (def link in links) {
                requestLinkJava(link)
                sleep(500)
            }


            def retryErrorLinks = errorLinks.clone()
            for (def link in retryErrorLinks)
                requestLinkJava(link)
        } catch (ex) {
            ex.printStackTrace()
            def exception = formatString(ex)
            errorText(rootDir, "root", "error:" + exception);
        }
    }

    def requestIndexs() {
        try {
            def http = new HTTPBuilder()
            http.request('http://www.eywedu.net/', GET, TEXT) { req ->
                uri.path = "/tangshi/"
//                uri.query = [a: '1', e: '0', u: uid+"", b: '4']
                headers.'User-Agent' = "Mozilla/5.0 Firefox/3.0.4"
                headers.Accept = 'text/html,application/xhtml+xml,application/xml;q=0.9,*/*;q=0.8'
                headers.'Accept-Language' = 'en-US,en;q=0.5'
                headers.Connection = 'keep-alive'

                response.success = { resp, reader ->

//                    println reader.text.getClass().getName();
                    def htmlStr = reader.text

//                    println htmlStr
                    handleIndexHtml(htmlStr)
                }

                response.'404' = {
                    println 'Not found'
                }
            }
        } catch (ex) {
            ex.printStackTrace()
            StringWriter sw = new StringWriter();
            PrintWriter pw = new PrintWriter(sw);
            ex.printStackTrace(pw);
            def exception = sw.toString();
            errorText(rootDir, "index", "error:" + exception);
        }
    }

    def requestLinkJava(link) {
        try {

            URL getUrl = new URL("http://www.eywedu.net/tangshi/" + link);
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

            handleHtml(link, sb.toString())
        } catch (Throwable ex) {
            println "exception when " + link
            ex.printStackTrace()
            def exception = formatException(ex);
            errorText(rootDir, link, "error:" + exception);
            errorLinks.add(link)
        }
    }

//    def requestLink(link) {
//        try {
//            def http = new HTTPBuilder()
//            http.request('http://www.eywedu.net/', GET, TEXT) { req ->
//                uri.path = "/tangshi/" + link
////                uri.query = [a: '1', e: '0', u: uid+"", b: '4']
//                headers.'User-Agent' = "Mozilla/5.0 Firefox/3.0.4"
//                headers.Accept = 'text/html,application/xhtml+xml,application/xml;q=0.9,*/*;q=0.8'
//                headers.Connection = 'keep-alive'
////                headers.'content-type' = "text/html; charset=gb2312"
//
//                response.success = { resp, reader ->
//                    //def htmlStr = new String(reader.text.toString().getBytes("UTF-8"));
////                    def htmlStr = reader.text;
//                    InputStreamReader isr = reader;
//                    println "requestLink isr:" + isr.getClass().getName();
//                    println "requestLink getEncoding:" + isr.getEncoding();
//                    def htmlStr = reader.text
//                    //def htmlStr = new String(reader.text.getBytes("UTF8"), "gb2312");
//
////                    def newStr = new String(htmlStr.getBytes("gb2312"), "UTF-8");
//
//                    println htmlStr
//                    //handleHtml(htmlStr)
//
//                    new File("temp_utf8.txt").withWriter {
//                        it.println(htmlStr)
//                    }
//                }
//
//                response.'404' = {
//                    println 'Not found'
//                }
//            }
//        } catch (ex) {
//            ex.printStackTrace()
//            StringWriter sw = new StringWriter();
//            PrintWriter pw = new PrintWriter(sw);
//            ex.printStackTrace(pw);
//            def exception = sw.toString();
//            errorText(rootDir, link, "error:" + exception);
//        }
//    }

    def handleIndexHtml(htmlStr) {
        def parser = new SAXParser()

        def page = new XmlSlurper(parser).parseText(htmlStr)

        links = page."**".findAll {
            it.name() == 'A' && it.@href != 'index.html' && it.@href.toString().endsWith("htm")
        }.collect() {
            it.@href
        }

        println links
    }

    void handleHtml(link, htmlStr) {

        println "link: " + link
        println "htmlStr: " + htmlStr
        def parser = new SAXParser()

        def page = new XmlSlurper(parser).parseText(htmlStr)

        def titleItem = page.depthFirst().find { it.name() == 'TITLE' }?.text()
        if (titleItem == null) {
            errorText(rootDir, link.toString(), "error: title is null");
            errorLinks.add(link)
            sleep(1000)
            return
        }
        String title = titleItem.toString()
        println "title:" + title
        def tag1 = "："
        def tag2 = "-原文、赏析"
        def index = title?.indexOf(tag1);
        def index2 = title?.indexOf(tag2);

        def author_title = ""
        def name_title = title
        if (index > 0) {
            author_title = title?.substring(0, index)
        }

        name_title = title?.substring(index + 1, index2)
        title = title?.substring(0, title.indexOf(tag2))
//        println 'title:'+title
        def index3 = title?.indexOf("◎");
//        println 'index3:'+index3
//        println 'author_title:'+author_title
        if(index3==0){
            title = title?.substring(1)
            if(author_title.length()==0){
                if (title.length() >=3) {
                    for (int i = 2; i < title.length(); i++) {
                        String tempName = title.substring(0, i);
                        println 'tempName:'+tempName
                        if (authorList.contains(tempName)) {
                            author_title = tempName
                            name_title = title.substring(i);
                            break;
                        }
                    }
                }
            }

        }
        println 'title:'+title
        println 'author_title:'+author_title
        println 'name_title:'+name_title

        def table = page."**".findAll { it.name() == 'TABLE' && it.@id == 'table4' }.toString().trim()
        println table
        if (table.startsWith("[")) {
            table = table.substring(1).trim()
        }
        if (table.endsWith("]")) {
            table = table.substring(0, table.length() - 1).trim()
        }
//        table = table.replace(name, "").trim();
        while (table.startsWith("　")) {
            table = table.substring(1)
        }
        while (table.endsWith("　")) {
            table = table.substring(0, table.length() - 1)
        }

        def tokens = table.tokenize("　");

        def size = tokens.size()

        def poemName = tokens[0]
        def author = tokens[1]
        def contentStartIndex = 0;


        for (int i=0;i<tokens.size()&&i<=4;i++) {
            def token = tokens.get(i)
//            println "["+i+"]:"+token
            if(authorList.contains(token)){
//                println "["+i+"] is author"
                author = token
                contentStartIndex = i+1;
                break
            }
        }

        if(contentStartIndex==0){
            def colonIndex = poemName.indexOf("：")
            if (colonIndex > 0) {
                contentStartIndex = 1;
                author = poemName.substring(0, colonIndex).trim()
                poemName = poemName.substring(colonIndex + 1).trim()
            } else if (author.contains("，")||author.contains("。")||author.contains("？")) {
                contentStartIndex = 1;
                author = author_title
                if (poemName.contains(name_title) && poemName.contains(author)) {
                    poemName = name_title
                }
            }
        } else {
            poemName = ""
            for (int i=0;i<tokens.size()&&i<contentStartIndex-1;i++) {
                poemName +=tokens.get(i)+" "
            }
        }




        def maxLength = tokens[contentStartIndex].length()

        def shangxiStartIndex = contentStartIndex + 1;
        for (int i = shangxiStartIndex; i < tokens.size(); i++) {
            if (isShangxi(tokens[i], poemName, author, maxLength)) {
                shangxiStartIndex = i
                break;
            }

            if (tokens[i].length() > maxLength) {
                maxLength = tokens[i].length()
            }
        }

        def poemcontentArray = tokens[contentStartIndex..shangxiStartIndex - 1];
        def poemcontent = ""
        for (def str : poemcontentArray) {
            poemcontent += str + "\r\n";
        }
        poemcontent = poemcontent.trim()



        def shangxiAuthor = ""
        def remark = "";
        def shangxiArray = tokens[shangxiStartIndex..size - 1]
        def shangxi = ""
        for (def str : shangxiArray) {
            shangxi += str + "\r\n";
            if (isShangXiAuthor(str)) {
                shangxiAuthor = str.substring(1, str.length() - 1)
            } else if (str.startsWith("［注］")) {
                remark = str.substring("［注］".length())
            }

        }

//        shangxi = shangxi.replace("（" + shangxiAuthor + "）", "").trim();

//        table = table.replace("　", "").trim();
        println "title:" + poemName
        println "author:" + author
        println "poemcontent:" + poemcontent
        println "shangxi:" + shangxi
        println "shangxiAuthor:" + shangxiAuthor
        println "remark:" + remark
//        if (!author) {
//            def authorIndex = table.indexOf("　");
//            if (authorIndex > 0) {
//                author = table.substring(0, authorIndex)
//                table = table.replace(author, "").trim();
//            }
//
//        }
//        while (table.startsWith("　")) {
//            table = table.substring(1)
//        }
//        println "table3:" + table

//        println "name:" + name
//        println "author:" + author

        def builder = new JsonBuilder()

        poemName = poemName.replace("◎", "").replace(" ","").trim()
        author = author.replace(" ","").trim()

        if(author.length()==0){
            //试着从诗名中获取作者名
            if (poemName.length() >=3) {
                for (int i = 2; i < poemName.length(); i++) {
                    String tempName = poemName.substring(0, i);
                    if (authorList.contains(tempName)) {
                        author = tempName
                        poemName = poemName.substring(i);
                        break;
                    }
                }
            }
        }

        // 构建json格式的poem
        builder.poem {
            n poemName + ""
            a author + ""
            c poemcontent + ""
            sx shangxi + ""
            sxa shangxiAuthor + ""
            r remark
            l "http://www.eywedu.net/tangshi/" + link
        }
        def finalData = JsonOutput.prettyPrint(builder.toString())
        def linkNum = "";
        if (link.toString().contains(".htm")) {
            linkNum = link.toString().substring(0, link.toString().indexOf(".htm"))
        }
        def fname = "a" + linkNum + "_" + author + "_" + poemName + ".json"
        fname = formatString(fname)
        saveToFile(rootDir, "root", fname, finalData)


        if(author.length()!=0){
            authorList.add(author);
        } else {
            errorLinks.add(link)
        }
    }


    boolean isShangXiAuthor(String text) {
        String str = text.trim();
        if (str.length() < 8 && str.startsWith("（") && str.endsWith("）")) {
            return true;
        }
        return false;
    }

    boolean isShangxi(String text, String poemName, String author, int lastMaxLength) {
        int score = 0;
        if (text.length() > 100) {
            score++;
        }
        if (text.length() > lastMaxLength * 4) {
            score++;
        }
        if (text.length() < 17) {
            score--;
        }

        def keyWords = ['这', '首', '诗', '立意', '新颖', '为题', '描写', '思想', '诗篇', '途经',
                        '诗人', '第一句', '这首', '之作', '作品', '先写','此为','手法','表达了','之情','写于','诗的']
        for (def word in keyWords) {
            if (text.contains(word)) {
                score++;
            }
        }

        if (poemName != null && poemName.length() > 0 && text.contains(poemName)) {
            score += 3;
        }


        def kkeyWords = ['本诗', '这首诗', author]
        for (def word in kkeyWords) {
            if (word != null && word.length() > 0 && text.contains(word)) {
                score += 4;
            }
        }


        return score >= 4;

    }

    static void main(args) {
        new TangshijianshangLoader().perform()
    }

}
