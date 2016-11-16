import groovy.json.JsonBuilder
import groovy.json.JsonOutput
import groovy.util.slurpersupport.NodeChild
import groovyx.net.http.HTTPBuilder
import org.cyberneko.html.parsers.SAXParser

import java.util.concurrent.Future

import static groovyx.net.http.Method.GET
import static groovyx.net.http.ContentType.TEXT
import static Utils.*
import java.util.concurrent.ExecutorService
import java.util.concurrent.Executors
import java.util.concurrent.Callable
import java.util.concurrent.Executors
import java.util.concurrent.TimeUnit
import java.util.concurrent.Future


/**
 * Created by shaopengxiang on 2016/11/14.
 */
class GTHtmlAnalyzer {
    def cookie = 'JSESSIONID=21A9CF604D1F325ECA4DA6CBB54A8978'

    def type = "song"

    def rootDir = "c:/dev/data/poem/" + type + "/";

    def errorUids = []


    void perform() {

        def file = new File(rootDir);
        if (!file.exists()) {
            file.mkdirs()
        }

        for (i in 100000..254240) {
            requestPoem(i);
        }

        def newUids = errorUids.clone();
        for (e in newUids) {
            requestPoem(e);
        }
//        def myClosure = { num -> requestPoem(num) }
//        def threadPool = Executors.newFixedThreadPool(1)
//        try {
//            for (int i = 99000; i < 99900; i += 100) {
//
//                List<Future> futures = (i..i + 100).collect { num ->
//                    threadPool.submit({ ->
//                        myClosure num
//                    } as Callable);
//                }
//                // recommended to use following statement to ensure the execution of all tasks.
//                futures.each { it.get() }
//
//            }
//
//        } finally {
//            threadPool.shutdown()
//        }
    }


    def requestPoem(uid) {
        try {
            def http = new HTTPBuilder()
            http.request('http://202.106.125.44:8082', GET, TEXT) { req ->
                uri.path = "/" + type + "/fullText.jsp"
                uri.query = [a: '1', e: '1', u: uid, b: '4']
                headers.'User-Agent' = "Mozilla/5.0 Firefox/3.0.4"
                headers.Accept = 'text/html,application/xhtml+xml,application/xml;q=0.9,*/*;q=0.8'
                headers.Cookie = cookie
                headers.Connection = 'keep-alive'

                response.success = { resp, reader ->
                    def htmlStr = reader.text
                    //println htmlStr
                    handleHtml(uid, htmlStr)
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
            errorText(rootDir, uid, "uid:" + uid + ", error:" + exception);
            errorUids.add(uid)
        }
    }


    def handleHtml(int uid, String rawHtmlText) {

        def startTag = "<div style=\"clear: both; margin-bottom: 25px;\">"
        int startIndex = rawHtmlText.indexOf(startTag)
        if (startIndex == -1 || startIndex + startTag.length() > rawHtmlText.length()) {
            errorText(rootDir, uid, rawHtmlText);
            return;
        }
        def text = rawHtmlText.substring(startIndex + startTag.length())
//
//        int firstDivEndIndex = text.indexOf("</div>")
//        def poemDiv = text.substring(0, firstDivEndIndex)
//
//
//        def tagBegin = "<h3 class=\"TEXT_DARK\">"
//        def tagEnd = "</h3>"
//        def beginIndex = poemDiv.indexOf(tagBegin)
//        def endIndex = poemDiv.indexOf(tagEnd)
//        if (beginIndex == -1 || endIndex == -1) {
//            errorText(rootDir, uid, text);
//            return;
//        }
//        def poemTitle = poemDiv.substring(beginIndex + tagBegin.length(), endIndex).trim()
//        poemTitle = formatString(poemTitle)
//        println '诗名:' + poemTitle
//
//        poemDiv = poemDiv.substring(endIndex + tagEnd.length());
//
////        println 'poemDiv:' + poemDiv
//
//        //诗:  xxxxxx xxxxxxx xxxxxxx xxxxxxx
//        String poemContent = new String();
//        int sIndex = -1;
//        while ((sIndex = poemDiv.indexOf("<p>")) != -1) {
//
//            int eIndex = poemDiv.indexOf("</p>", sIndex)
//            if (eIndex <= sIndex) {
//                errorText(rootDir, uid, text);
//                return;
//            }
//
//            def str = poemDiv.substring(sIndex + 3, eIndex)
//            //println 'str:' + str
//
//            poemContent += str
//            poemDiv = poemDiv.substring(eIndex + 4)
//            //println 'poemDiv:' + poemDiv
//        }
//        poemContent = formatString(poemContent)
//        println '诗内容:' + poemContent
//
//
//        text = text.substring(firstDivEndIndex + 5)
//
//        def poemRemark = ''
//        def poemContentExplain = ''
//        if (text.contains("帶註釋文本")) {
//            //诗备注
//            tagBegin = "<h3 class=\"TEXT_DARK\">"
//            tagEnd = "</h3>"
//            beginIndex = text.indexOf(tagBegin)
//            endIndex = text.indexOf(tagEnd)
//            if (beginIndex == -1 || endIndex == -1) {
//                errorText(rootDir, uid, text);
//                return;
//            }
//            poemRemark = text.substring(beginIndex + tagBegin.length(), endIndex).trim()
//            poemRemark = formatString(poemRemark)
//            println '诗名备注:' + poemRemark
//
//            text = text.substring(endIndex + tagEnd.length())
//
//            //诗内容备注
//            tagBegin = "<p>"
//            tagEnd = "</p>"
//            beginIndex = text.indexOf(tagBegin)
//            endIndex = text.indexOf(tagEnd)
//            if (beginIndex == -1 || endIndex == -1) {
//                errorText(rootDir, uid, text);
//                return;
//            }
//            poemContentExplain = text.substring(beginIndex + tagBegin.length(), endIndex).trim()
//            poemContentExplain = formatString(poemContentExplain)
//            println '诗内容备注:' + poemContentExplain
//
//            text = text.substring(endIndex + tagEnd.length())
//        }
//
//        //诗作者
//        tagBegin = "作者:"
//        tagEnd = "</h2>"
//        beginIndex = text.indexOf(tagBegin)
//        endIndex = text.indexOf(tagEnd)
//        if (beginIndex == -1 || endIndex == -1) {
//            errorText(rootDir, uid, text);
//            return;
//        }
//        def poemAuthor = text.substring(beginIndex + tagBegin.length(), endIndex).trim()
//        poemAuthor = poemAuthor.replace("作者:", "")
//        poemAuthor = formatString(poemAuthor)
//        println '诗作者:' + poemAuthor
//        text = text.substring(endIndex + tagEnd.length())
//
//        //作者简介
//        tagBegin = "<p>"
//        tagEnd = "</p>"
//        beginIndex = text.indexOf(tagBegin)
//        endIndex = text.lastIndexOf(tagEnd)
//        if (beginIndex == -1 || endIndex == -1) {
//            errorText(rootDir, uid, text);
//            return;
//        }
//        def poemAuthorDesc = text.substring(beginIndex + tagBegin.length(), endIndex).trim()
////        poemAuthorDesc = poemAuthor.replace("作者:", "")
//        println '作者简介:' + poemAuthorDesc
        //println text

//        def poemFileName = uid + "_" + poemAuthor + "_" + poemTitle + ".json";


        println "-----"
        def parser = new SAXParser()

        def page = new XmlSlurper(parser).parseText(rawHtmlText)

//        println 'poem name:'+ page.h3[0].text()
//        println 'poem author:'+ page.h4[0].text()

//        def ptitle = page.depthFirst().find { it.@class.equals("TEXT_DARK") }.text()
//        println 'poem name:' + ptitle
//
//        def pauthor = page.depthFirst().find { it.@class.equals("TEXT_LIGHT") }.text()
//        println 'poem author:' + pauthor

//        def ptitle_remark = page.'**'.find { it.name() == 'DIV' && it.@style == 'clear: both; margin-bottom: 25px;' }.H3
//        println 'poem nnn:' + nnn

        def names = page."**".findAll { it.@class == 'TEXT_DARK' }
//        println 'poem names:' + names
        String pn, pa, pr;
        if (names.size() == 2) {
            pn = names[0].toString().trim()
            pa = names[1].toString().trim()
            pr = pn.toString()
        } else if (names.size() == 3) {
            pn = names[0].toString().trim()
            pr = names[1].toString().trim()
            pa = names[2].toString().trim()
        } else {
            // error!!!
            errorText(rootDir, uid, text);
            return;
        }

        def contentDivPList = page.'**'.find {
            it.name() == 'DIV' && it.@style == 'clear: both; margin-bottom: 25px;'
        }."**".findAll { it.name() == 'P' && it.@class == '' }

        String ppp = "";
        for (oneP in contentDivPList) {
            ppp += oneP.text()
        }
        println ppp


        def divList = page."**".findAll {
            it.name() == 'DIV' && (it.@class == '' || it.@class == 'comment') && it.@style == ''
        }

        def detailPoem = "";
        def pabauthor = "";
//        println 'poem divList.size:' + divList.size()
        if (divList.size() == 2) {
            def pxiangxi = divList[0]
            def pxiangxi_children = pxiangxi.children();
            def pNode = pxiangxi_children[1];


            if (pNode instanceof NodeChild) {
                def mnode = pNode.getAt(0);
                def pchildrens = mnode.children()
                for (leefnode in pchildrens) {
//                    println 'leefnode.class:' + leefnode.getClass().getName()
                    if (leefnode instanceof String) {
                        detailPoem += leefnode
                    } else if (leefnode instanceof groovy.util.slurpersupport.Node) {
//                        println 'leefnode.name:' + leefnode.name()
                        def attrs = leefnode.attributes()
                        detailPoem += "<" + leefnode.name() + " class = '" + leefnode.attributes()['class'] + "'>" + leefnode.children()[0] + "</" + leefnode.name() + ">"
                    }
                }
            }

            //println 'detailPoem:' + detailPoem

            pabauthor = divList[1].P.text();
            //println 'pabauthor:' + pabauthor
        } else if (divList.size() == 1) {
            pabauthor = divList[0].P.text();
            //println 'pabauthor:' + pabauthor
        } else {
            //error!!!
            errorText(rootDir, uid, text);
            return;
        }

        //println 'build json....'
        def builder = new JsonBuilder()
        // 构建json格式的poem
        builder.poem {
            n pn
            a pa
            r pr
            c ppp
            x detailPoem
            b pabauthor
        }
//        println 'pn:' + pn
//        println 'pa:' + pa
//        println 'pr:' + pr
//        println 'ppp:' + ppp

        def finalData = JsonOutput.prettyPrint(builder.toString())
        //println 'save json:' + finalData

        def poemFileName = uid + "_" + pa + "_" + pn + ".json";
        println 'save ' + poemFileName
        saveToFile(rootDir, uid, poemFileName, finalData)

//        def spans = page."**".findAll {
//            it.name() == 'SPAN'
//        }.each {
//            println '<span class="' + it.@class + '">' + it.text() + '</span>'
//        }
//        println 'poem spans:' + spans
//        def teams =
//                page.'**'.find{it.name() == 'DIV' && it.@style=='clear: both; margin-bottom: 25px;'}.
//                /* (2) Folow the path */
//                        DIV[0].H2.LI.collect{li->
//                    /* (3) For each element in list get the title of the first anchor */
//                    li.'**'.find{it.name() == 'A'}*.@title
//                }.flatten()
    }

    public static void main(args) {
        new GTHtmlAnalyzer().perform()
    }
}
