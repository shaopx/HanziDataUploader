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
    def cookie = 'JSESSIONID=81B8C8124CAA1386A8F9B2FE45050DCD;tradsimp=true;'

    def type = "tang"

    def rootDir = "c:/dev/data/poem/" + type + "/";

    def errorUids = []


    void perform() {

        def file = new File(rootDir);
        if (!file.exists()) {
            file.mkdirs()
        }

        for (i in 1..57593) {
            requestPoem(i);
        }

        def newUids = errorUids.clone();
        errorUids.clear()
        for (e in newUids) {
            requestPoem(e);
        }

        newUids = errorUids.clone();
        errorUids.clear()
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
                uri.query = [a: '1', e: '0', u: uid+"", b: '4']
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

                detailPoem = getPContent(pNode);
//                def mnode = pNode.getAt(0);
//                def pchildrens = mnode.children()
//                for (leefnode in pchildrens) {
////                    println 'leefnode.class:' + leefnode.getClass().getName()
//                    if (leefnode instanceof String) {
//                        detailPoem += leefnode
//                    } else if (leefnode instanceof groovy.util.slurpersupport.Node) {
////                        println 'leefnode.name:' + leefnode.name()
//                        def attrs = leefnode.attributes()
//                        detailPoem += "<" + leefnode.name() + " class = '" + leefnode.attributes()['class'] + "'>" + leefnode.children()[0] + "</" + leefnode.name() + ">"
//                    }
//                }
            }

            println 'detailPoem:' + detailPoem

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
        if(pn.length()>32){
            poemFileName = uid + "_" + pa + "_" + pn.substring(0, 32) + ".json";
        }
        poemFileName = formatString(poemFileName)

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
