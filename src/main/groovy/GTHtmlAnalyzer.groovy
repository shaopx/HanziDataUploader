import groovy.json.JsonBuilder
import groovy.json.JsonOutput
import groovyx.net.http.HTTPBuilder
import com.mongodb.BasicDBObject
import com.mongodb.client.MongoCollection
import com.mongodb.client.MongoDatabase
import groovyx.net.http.ContentType
import groovyx.net.http.HTTPBuilder
import groovyx.net.http.Method
import org.bson.Document
import groovyx.net.http.HTTPBuilder
import static groovyx.net.http.ContentType.URLENC
import groovyx.net.http.HTTPBuilder
import static groovyx.net.http.Method.GET
import static groovyx.net.http.Method.POST
import static groovyx.net.http.ContentType.TEXT
import groovyx.net.http.HttpURLClient
import static groovyx.net.http.ContentType.JSON

/**
 * Created by shaopengxiang on 2016/11/14.
 */
class GTHtmlAnalyzer {

    def type = "song"
    def cookie = 'JSESSIONID=33C675732BAAE0514B7133401337B10E'
    def http = new HTTPBuilder()

    def dir = "c:/dev/data/poem/"+type+"/";

    def builder = new JsonBuilder()

    def perform() {

        def file = new File(dir);
        if (!file.exists()) {
            file.mkdirs()
        }

        for (i in 1..254240) {
            requestPoem(i);
        }

    }


    def requestPoem(uid) {
        try {
            http.request('http://202.106.125.44:8082', GET, TEXT) { req ->
                uri.path = "/"+type+"/fullText.jsp"
                uri.query = [a: '1', e: '1', u: uid, b: '4']
                headers.'User-Agent' = "Mozilla/5.0 Firefox/3.0.4"
                headers.Accept = 'text/html,application/xhtml+xml,application/xml;q=0.9,*/*;q=0.8'
                headers.Cookie = cookie
                headers.Connection = 'keep-alive'

                response.success = { resp, reader ->
                    //println reader.text
                    handleHtml(uid, reader.text.toString())
                }

                response.'404' = {
                    println 'Not found'
                }
            }
        } catch (ex) {
            ex.printStackTrace()
            errorText(uid, "uid:"+uid+", error:" + ex.getMessage());
        }
    }


    def handleHtml(int uid, String text) {

        def startTag = "<div style=\"clear: both; margin-bottom: 25px;\">"
        int startIndex = text.indexOf(startTag)
        if (startIndex == -1 || startIndex + startTag.length() > text.length()) {
            errorText(uid, text);
            return;
        }
        text = text.substring(startIndex + startTag.length())

        int firstDivEndIndex = text.indexOf("</div>")
        def poemDiv = text.substring(0, firstDivEndIndex)


        def tagBegin = "<h3 class=\"TEXT_DARK\">"
        def tagEnd = "</h3>"
        def beginIndex = poemDiv.indexOf(tagBegin)
        def endIndex = poemDiv.indexOf(tagEnd)
        if (beginIndex == -1 || endIndex == -1) {
            errorText(uid, text);
            return;
        }
        def poemTitle = poemDiv.substring(beginIndex + tagBegin.length(), endIndex).trim()
        poemTitle = formatString(poemTitle)
        println '诗名:' + poemTitle

        poemDiv = poemDiv.substring(endIndex+tagEnd.length());

//        println 'poemDiv:' + poemDiv

        //诗:  xxxxxx xxxxxxx xxxxxxx xxxxxxx
        String poemContent = new String();
        int sIndex = -1;
        while ((sIndex = poemDiv.indexOf("<p>")) != -1) {

            int eIndex = poemDiv.indexOf("</p>", sIndex)
            if (eIndex <= sIndex) {
                errorText(uid, text);
                return;
            }

            def str = poemDiv.substring(sIndex+3, eIndex)
            //println 'str:' + str

            poemContent += str
            poemDiv = poemDiv.substring(eIndex+4)
            //println 'poemDiv:' + poemDiv
        }
        poemContent = formatString(poemContent)
        println '诗内容:' + poemContent


        text = text.substring(firstDivEndIndex + 5)

        def poemRemark = ''
        def poemContentExplain = ''
        if(text.contains("帶註釋文本")){
            //诗备注
            tagBegin = "<h3 class=\"TEXT_DARK\">"
            tagEnd = "</h3>"
            beginIndex = text.indexOf(tagBegin)
            endIndex = text.indexOf(tagEnd)
            if (beginIndex == -1 || endIndex == -1) {
                errorText(uid, text);
                return;
            }
            poemRemark = text.substring(beginIndex + tagBegin.length(), endIndex).trim()
            poemRemark = formatString(poemRemark)
            println '诗名备注:' + poemRemark

            text = text.substring(endIndex + tagEnd.length())

            //诗内容备注
            tagBegin = "<p>"
            tagEnd = "</p>"
            beginIndex = text.indexOf(tagBegin)
            endIndex = text.indexOf(tagEnd)
            if (beginIndex == -1 || endIndex == -1) {
                errorText(uid, text);
                return;
            }
            poemContentExplain= text.substring(beginIndex + tagBegin.length(), endIndex).trim()
            poemContentExplain = formatString(poemContentExplain)
            println '诗内容备注:' + poemContentExplain

            text = text.substring(endIndex + tagEnd.length())
        }



        //诗作者
        tagBegin = "作者:"
        tagEnd = "</h2>"
        beginIndex = text.indexOf(tagBegin)
        endIndex = text.indexOf(tagEnd)
        if (beginIndex == -1 || endIndex == -1) {
            errorText(uid, text);
            return;
        }
        def poemAuthor = text.substring(beginIndex + tagBegin.length(), endIndex).trim()
        poemAuthor = poemAuthor.replace("作者:", "")
        poemAuthor = formatString(poemAuthor)
        println '诗作者:' + poemAuthor
        text = text.substring(endIndex + tagEnd.length())

        //作者简介
        tagBegin = "<p>"
        tagEnd = "</p>"
        beginIndex = text.indexOf(tagBegin)
        endIndex = text.lastIndexOf(tagEnd)
        if (beginIndex == -1 || endIndex == -1) {
            errorText(uid, text);
            return;
        }
        def poemAuthorDesc = text.substring(beginIndex + tagBegin.length(), endIndex).trim()
//        poemAuthorDesc = poemAuthor.replace("作者:", "")
        println '作者简介:' + poemAuthorDesc
        //println text

        def poemFileName = uid + "_" + poemAuthor + "_" + poemTitle + ".json";


        builder.poem{
            n poemTitle
            a poemAuthor
            r poemRemark
            c poemContent
            x poemContentExplain
            b poemAuthorDesc
        }

//        StringBuilder sb = new StringBuilder("{")
//        sb.append('"name":"').append(poemTitle).
//                append("<remark>").append(poemRemark).append("</remark>").
//                append("<author>").append(poemAuthor).append("</author>").
//                append("<content>").append(poemContent).append("</content>").
//                append("<xiangxi>").append(poemContentExplain).append("</xiangxi>").
//                append("<aboutauthor>").append(poemAuthorDesc).append("</aboutauthor>").append("}")

//        String fileData = sb.replaceAll("，", ",");
//        fileData = fileData.replaceAll("。", ".");
        saveToFile(uid, poemFileName, JsonOutput.prettyPrint(builder.toString()))


    }

    def formatString(String str) {
        return str.replaceAll("，", ",").replaceAll("。", ".").
                replace("<span class=\"comment\">", "(").
                replace("</span>", ")").replace("\t", "").replace("{", "(").replace("}", ")").replace("\\", "_").replace("/", "");
    }

    def errorText(int uid, String text) {
        System.err.println("error")
        saveToFile(uid, "error_" + uid + ".txt", text);
    }

    def getFileSubDir(int uid) {
        for (int i = 1000; i < 100000; i += 1000) {
            if (uid <i) {
                return dir + "/" + i + "/";
            }
        }
        return dir + "/big/";
    }

    def saveToFile(uid, fn, content) {
        def subDir = getFileSubDir(uid);
        def file = new File(subDir);
        if (!file.exists()) {
            file.mkdirs()
        }

        new File(subDir, fn).withPrintWriter { printWriter ->
            printWriter.println(content)
        }

        new File(subDir, "error_" + uid + ".txt").delete()
    }

    public static void main(args) {
        new GTHtmlAnalyzer().perform()
    }
}
