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

    def dbLoader = GroovyDataLoader.instance

    void perform() {
        dbLoader.copyDbs()

        def sql_poem = Sql.newInstance("jdbc:sqlite:poem.db", "", "", "org.sqlite.JDBC")
        BasicDBObject query = new BasicDBObject();
        def sql_id_list = "select * from poem where _id >0 and _id<1000 "

        sql_poem.eachRow(sql_id_list) { row ->
            def pname = row["mingcheng"]
            def pauthor = row["zuozhe"]
            def pid = row["_id"]
            println 'pid:'+pid+', zuozhe:'+pauthor+', pname:' + pname

            boolean succeed = requestWord(pid, pauthor, pname)
            if(!succeed ){
                def newName = pname.toString().replace('。','·')
                succeed = requestWord(pid, pauthor, newName)
                def names = newName.split('·')
                names.each { name ->
                    succeed = requestWord(pid, pauthor, name)
                }
            }
        }
        //requestWord('五弦弹－恶郑之夺雅也')

        writeErrors()
    }

    void writeErrors() {
        def wordstr = ''
        notExistWords.each {
            wordstr += (it + '\r\n')
        }
        errorText(rootDir, 'error', wordstr)
    }

    boolean requestWord(pid, zuozhe, word) {
        def url = "http://baike.baidu.com/item/" + word
        def text = getUrlTextContent(rootDir, url);
        //println text
        if (!text || text.contains('百度百科错误页') && text.contains('您所访问的页面不存在')) {
            println '[' +pid+', '+ word + ']   不存在!'
            notExistWords << word
            return false;
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


        def summaryDivs = page."**".findAll {
            it.name() == 'DD' && it.@class == 'lemma-summary'
        }

        def summary = ''
        if (summaryDivs) {
            summaryDivs.each {
                summary += it.toString()
            }
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
                    StringBuilder sb = new StringBuilder()
                    divChilds.each { line ->
                        //println line
                        sb.append(line).append("\n")
                    }
                    String content = sb.toString()
                    //println divChilds.size() + ":    key:" + lastDivTag + "--->" + content

                    def key = lastDivTag.toString().trim()
                    if (key.contains(word) && key.contains('原文') && key.contains('编辑')) {
                        key = 'yuanwen'
                    } else if ( key.contains('作者简介') && key.contains('编辑')) {
                        key = 'zzjj'
                    } else if (key.contains('作品鉴赏') && key.contains('编辑')) {
                        key = 'jianshang'
                    } else if (key.contains('赏析') && key.contains('编辑')) {
                        key = 'shangxi'
                    } else if (key.contains('注释') && key.contains('编辑')) {
                        key = 'zhushi'
                    }else if (key.contains('注解') && key.contains('编辑')) {
                        key = 'zhujie'
                    } else if ( key.contains('译文') && key.contains('编辑')) {
                        key = 'yiwen'
                    } else if ( key.contains('背景') && key.contains('编辑')) {
                        key = 'beijing'
                    } else if (key.contains('出处') ) {
                        key = 'chuchu'
                    } else if (key.contains('别名') ) {
                        key = 'bieming'
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
            } else if (name.contains('出处') ) {
                name = 'chuchu'
            } else if (name.contains('别名') ) {
                name = 'bieming'
            }

            //println "" + name + ":" + value
            divMap[name] = value
        }

        divMap['link'] = url
        divMap['sum'] = summary
        divMap['pid'] = pid

        println '=============================='
//        divMap.each { key,value ->
//            println key+">>>"+value.toString()
//        }

        divMap.each { entry ->
            //println entry.key.toString() + "==>" + entry.value.toString()
        }
        //println divMap.toString()

        def author = divMap['zuozhe']
        def title = divMap['title']
        if(author==null || author.equals("null") || title==null || title.equals('null')){
            println "[" + word + ']   failed!'
            notExistWords << word
            return false;
        }


        if (title == '《' + word + "》") {
            title = word
        }

        if (title != word && !(title.toString().contains(word))) {
            println '[' +pid+', '+ word + ']   失败了!!'
            notExistWords << word
            return false;
        }

        if (author != zuozhe && author != '无名氏') {
            println '[' +pid+', '+ word + '] 作者不匹配  失败了!!'
            notExistWords << word
            return false;
        }



        def builder = new JsonBuilder()
        // 构建json格式的poem
        builder {
            poem divMap
        }

        def finalData = JsonOutput.prettyPrint(builder.toString())
        //println finalData

        def fname = pid+'_'+author + "_" + title + ".json"
        fname = formatString(fname)
        fname = fname.replace("<", "")
        fname = fname.replace(">", "")
        fname = fname.replace("\"", "")
        fname = fname.replace(":", "")
        fname = fname.replace("=", "")
        saveToFile(rootDir, "baike", fname, finalData)

        return true;
    }

    static void main(args) {
        new BaiduBaike2Loader().perform()
    }

}
