package guotu

import groovy.json.JsonBuilder
import groovy.json.JsonOutput
import groovyx.net.http.HTTPBuilder

import static util.Utils.formatString
import static groovyx.net.http.ContentType.TEXT
import static groovyx.net.http.Method.GET
import org.cyberneko.html.parsers.SAXParser

import static util.Utils.*

/**
 * Created by Administrator on 2016/11/15.
 */
class GT25Loader {

    def type = "h25"
    def cookie = 'JSESSIONID=D30444D1895733328C18B77FB492BB23;comment=false;tradsimp=true;'
    def http = new HTTPBuilder()

    def rootPath = '/h25/'
    def pathsFileName = "paths.txt"
    def prefix = 'READ/書籍庫/二十五史'

    def targetPaths = []

    def dir = "c:/dev/data/poem/" + type + "/";

    def builder = new JsonBuilder()

    void initEnv() {
        def file = new File(dir);
        if (!file.exists()) {
            file.mkdirs()
        }

        new File(dir, pathsFileName).eachLine { line ->
            targetPaths << line
        }
    }

    def perform() {
        initEnv()
        for (path in targetPaths) {
            requestPoem(path);
            sleep(2000)
        }
    }


    def requestPoem(String targetPath) {
        println 'request ' + targetPath
        if (targetPath == prefix) {
            println prefix + ' can no load'
            return
        }

        String bookName = ''
        if (targetPath.startsWith(prefix)) {
            String endStr = targetPath.substring(prefix.length() + 1);
            def index = endStr.indexOf("/")
            bookName = endStr.substring(0, index);
        }

        try {
            //http://202.106.125.44:8082/h25/READ/書籍庫/二十五史/史記/史記卷一 五帝本紀第一
            http.request('http://202.106.125.44:8082', GET, TEXT) { req ->
                uri.path = rootPath + targetPath
//                uri.query = [a: '1', e: '1', u: uid, b: '4']
                headers.'User-Agent' = "Mozilla/5.0 Firefox/3.0.4"
                headers.Accept = 'text/html,application/xhtml+xml,application/xml;q=0.9,*/*;q=0.8'
                headers.Cookie = cookie
                headers.Connection = 'keep-alive'

                response.success = { resp, reader ->
                    def htmlText = reader.text
                    //println htmlText
                    handleHtml(bookName, targetPath, htmlText)
                }

                response.'404' = {
                    println 'Not found'
                }
            }
        } catch (ex) {
            ex.printStackTrace()

            errorText(dir, targetPath, "target:" + targetPath + ", error:" + ex.getMessage());
        }
    }


    def handleHtml(String bookName, String targetPath, String text) {

        def parser = new SAXParser()
        def page = new XmlSlurper(parser).parseText(text)

//        page."**".findAll { it.name() == 'A' && it.@class == '' }.each {
//            // doSomething with each entry
//            //println it.text() + "==>" + it.@href.text()
//           // targetPaths.add(it.@href.text())
//        }

        def docDivNode = page.depthFirst().find { it.name() == 'DIV' && it.@class == 'doc' };
        if (docDivNode) {
            String title = docDivNode.H1?.text().toString()
            println "title:" + title

//            def divList = page."**".findAll {
//                it.name() == 'DIV' && (it.@class == '' || it.@class == 'comment') && it.@style == ''
//            }

            String content = "";
            String contentWithoutComment = ""
            def divNodeChildren = docDivNode.getAt(0)?.children();
            if (divNodeChildren != null && divNodeChildren.size() > 1) {
                def divContentNode = divNodeChildren[1]
                def divContentNodeChildren = divContentNode?.children()
                if (divContentNodeChildren != null && divContentNodeChildren.size() > 0) {
                    def pNode = divContentNodeChildren[0];
                    if (pNode != null) {
                        def pNodeChildren = pNode.children()
                        for (def childNode in pNodeChildren) {
                            content += getPContent(childNode)
                            contentWithoutComment += getPContent(childNode, false)
                        }
                    }


                    println "content:" + content
                    println "contentWithoutComment:" + contentWithoutComment
                }
            }

//            def content = docDivNode.DIV?.DIV?.P?.text()
            if (title && content && contentWithoutComment) {
                println "content:" + content.substring(0, 64)



                def t1 = title;
                def t2 = ""
                if (title.indexOf(" ") != -1) {
                    t1 = title.substring(0, title.indexOf(" ")).trim();
                    t2 = title.substring(title.indexOf(" ")).trim();
                }



                def builder = new JsonBuilder()
                // 构建json格式的poem
                builder.article {
                    book bookName
                    tile t1
                    subtitle t2
                    c content
                    cc contentWithoutComment
                }
                def finalData = JsonOutput.prettyPrint(builder.toString())

                def fname = title + ".json"
                fname = formatString(fname)
                saveToFile(dir, bookName, fname, finalData)
            } else {
                errorText(dir, bookName, "content is null")
            }
        }


    }


    public static void main(args) {
        new GT25Loader().perform()
    }
}
