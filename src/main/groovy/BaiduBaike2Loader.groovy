import com.mongodb.BasicDBObject
import groovy.json.JsonBuilder
import groovy.json.JsonOutput
import groovy.sql.Sql
import org.cyberneko.html.parsers.SAXParser

import static Utils.errorText
import static Utils.formatString
import static Utils.getUrlTextContent
import static Utils.saveToFile

/**
 * Created by SHAOPENGXIANG on 2016/11/30.
 */
class BaiduBaike2Loader {
    def rootDir = "c:/dev/data/poem/baidu/";

    def notExistWords = []

    def dbLoader = new GroovyDataLoader()

    void perform() {
        dbLoader.copyDbs()

        def sql_poem = Sql.newInstance("jdbc:sqlite:poem.db", "", "", "org.sqlite.JDBC")
        BasicDBObject query = new BasicDBObject();
        def sql_id_list = "select * from poem where _id ==2 "

        sql_poem.eachRow(sql_id_list) { row ->
            def pname = row["mingcheng"]
            def pauthor = row["zuozhe"]
            def pid = row["_id"]
            println 'pid:'+pid+', pname:' + pname

            requestWord(pname)
        }
        //requestWord('五弦弹－恶郑之夺雅也')

        writeErrors()
    }

    void writeErrors() {
        def wordstr = ''
        notExistWords.each {
            wordstr += it + "\n"
        }
        errorText(rootDir, 'error', wordstr)
    }

    def requestWord(word) {
        def url = "http://baike.baidu.com/item/" + word
        def text = getUrlTextContent(rootDir, url);
        //println text
        if (text.contains('百度百科错误页') && text.contains('您所访问的页面不存在')) {
            println "[" + word + ']   不存在!'
            notExistWords << word
            return;
        }

        def parser = new SAXParser()
        def page = new XmlSlurper(parser).parseText(text)

//        def infoDiv = page.depthFirst().find { it.@class == 'basic-info cmn-clearfix'}
//        println infoDiv

        def dtList = page."**".findAll {
            it.name() == 'DT' && it.@class == 'basicInfo-item name'
        }

        def ddList = page."**".findAll {
            it.name() == 'DD' && it.@class == 'basicInfo-item value'
        }



        def tags = page."**".findAll {
            it.name() == 'DIV' && it.@class == 'para-title level-2'
        }

        def engdTags = page."**".findAll {
            it.name() == 'DL' && it.@class == 'lemma-reference collapse nslog-area log-set-param'
        }
        println 'engdTags:' + engdTags.toString()

        def divs = page."**".findAll {
            it.name() == 'DIV' || it.name() == 'DL'
        }

        def divMap = [:]
        def divChilds = []
        def lastDivTag = null
        divs.each { div ->

            if (div in tags || div in engdTags) {
                if (lastDivTag) {
                    def content = ""
                    divChilds.each { line ->
                        content += line + "\n"
                    }
                    println div.toString() + ":    key:" + lastDivTag + "--->" + content

                    def key = lastDivTag.toString().trim()
                    if (key.contains(word) && key.contains('原文') && key.contains('编辑')) {
                        key = 'yuanwen'
                    } else if (key.contains(word) && key.contains('作者简介') && key.contains('编辑')) {
                        key = 'zzjj'
                    } else if (key.contains(word) && key.contains('作品鉴赏') && key.contains('编辑')) {
                        key = 'jianshang'
                    } else if (key.contains(word) && key.contains('赏析') && key.contains('编辑')) {
                        key = 'shangxi'
                    } else if (key.contains(word) && key.contains('注释') && key.contains('编辑')) {
                        key = 'zhushi'
                    } else if (key.contains(word) && key.contains('译文') && key.contains('编辑')) {
                        key = 'yiwen'
                    } else if (key.contains(word) && key.contains('背景') && key.contains('编辑')) {
                        key = 'beijing'
                    } else if (key.contains(word) && key.contains('出处') && key.contains('编辑')) {
                        key = 'chuchu'
                    }

                    divMap[key] = content.toString().trim()
                    divChilds.clear()
                }


                lastDivTag = div
                if (div in engdTags) {
                    lastDivTag = null
                }
            } else {
                if (lastDivTag)
                    divChilds << div
            }

        }


        for (int i = 0; i < dtList.size(); i++) {
            def name = dtList.get(i).toString()
            def value = ddList.get(i)

            name = name.toString().replace("\n", "").replace(" ", "").trim()
            value = value.toString().replace("\n", "").replace(" ", "").trim()
            if (name.contains('作品名称') || name.contains('中文名')) {
                name = 'title'
            } else if (name.contains('作者')) {
                name = 'zuozhe'
            } else if (name.contains('年代')) {
                name = 'niandai'
            } else if (name.contains('体裁')) {
                name = 'ticai'
            }

            println "" + name + ":" + value
            divMap[name] = value
        }

        divMap['link'] = url

        println '=============================='
//        divMap.each { key,value ->
//            println key+">>>"+value.toString()
//        }

        divMap.each { entry ->
            println entry.key.toString() + "==>" + entry.value.toString()
        }
        //println divMap.toString()

        def author = divMap['zuozhe']
        def title = divMap['title']

        def builder = new JsonBuilder()
        // 构建json格式的poem
        builder {
            poem divMap
        }

        def finalData = JsonOutput.prettyPrint(builder.toString())
        println finalData

        def fname = author + "_" + title + ".json"
        fname = formatString(fname)
        fname = fname.replace("<", "")
        fname = fname.replace(">", "")
        fname = fname.replace("\"", "")
        fname = fname.replace(":", "")
        fname = fname.replace("=", "")
        saveToFile(rootDir, "baike", fname, finalData)
    }

    static void main(args) {
        new BaiduBaike2Loader().perform()
    }

}
