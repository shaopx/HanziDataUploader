import com.mongodb.BasicDBObject
import db.GroovyDataLoader
import groovy.json.JsonBuilder
import groovy.json.JsonOutput
import groovy.sql.Sql
import groovy.util.slurpersupport.NodeChild
import org.cyberneko.html.parsers.SAXParser

import static Utils.errorText
import static Utils.formatString
import static Utils.getUrlTextContent
import static Utils.*
import static Utils.saveToFile

/**
 * Created by SHAOPENGXIANG on 2016/11/30.
 */
class BaiduBaike2Loader {
    def rootDir = "c:/dev/data/poem/baidu/";

    def notExistWords = []

    def dbLoader = GroovyDataLoader.instance

    def blockDivClasses = ['promotion_title', 'side-content', 'lemmaWgt-promotion-vbaike', 'promotion_viewport',
                           'clear', 'side-box lemma-statistics', 'description', 'credit-title',
                           'bd_weixin_popup_header', 'top-collect', 'collect-tip', 'bdsharebuttonbox', 'promotion-declaration', 'open-tag']

    void perform() {
        dbLoader.copyDbs()

        def sql_poem = Sql.newInstance("jdbc:sqlite:poem.db", "", "", "org.sqlite.JDBC")
        BasicDBObject query = new BasicDBObject();
        def sql_id_list = "select * from poem where _id >0 and _id<4s000 "

        sql_poem.eachRow(sql_id_list) { row ->
            def pname = row["mingcheng"]
            def pauthor = row["zuozhe"]
            def pid = row["_id"]
            println 'pid:' + pid + ', zuozhe:' + pauthor + ', pname:' + pname

            boolean succeed = requestWord(pid, pauthor, pname)
            if (!succeed) {
                def newName = pname.toString().replace('。', '·')
                succeed = requestWord(pid, pauthor, newName)
                def names = newName.split('·')
                names.each { name ->
                    succeed = requestWord(pid, pauthor, name)
                }
                names = newName.split('/')
                names.each { name ->
                    succeed = requestWord(pid, pauthor, name)
                }
            }
        }
        //requestWord('五弦弹－恶郑之夺雅也')

        writeErrors()
    }

    void writeErrors() {
        String wordstr = ''
        notExistWords.each {
            wordstr += (it.toString() + '\r\n')
        }
        errorText(rootDir, 'error', wordstr)
    }

    boolean requestWord(pid, zuozhe, word) {
        def url = "http://baike.baidu.com/item/" + word
        def text = getUrlTextContent(rootDir, url);
        println 'requestWord:' + word
        if (!text || text.contains('百度百科错误页') && text.contains('您所访问的页面不存在')) {
            println '[' + pid + ', ' + word + ']   不存在!'
            notExistWords << pid + '<<' + word
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
            it.name() == 'DIV' && it.@class == 'lemma-summary'
        }

        def summary = ''
        if (summaryDivs) {
            summaryDivs.each {
                summary += it.toString()
            }
        }
        if (summary) {
            summary = summary.toString().trim()
        }



        def tags = page."**".findAll {
            it.name() == 'DIV' && it.@class == 'para-title level-2'
        }
        println 'tags:' + tags.toString()

        def engdTags = page."**".findAll {
            it.name() == 'DL' && it.@class == 'lemma-reference collapse nslog-area log-set-param'
        }

        def includeDl = true
        if (engdTags.size() == 0) {
            includeDl = false
            engdTags = page."**".findAll {
                it.name() == 'DIV' && it.@id == 'open-tag'
            }
        }
        //println 'engdTags:' + engdTags.toString()

        def divs = page."**".findAll {
            (it.name() == 'DIV' || (includeDl && it.name() == 'DL')) && !(it.@class in blockDivClasses)
        }

        def divMap = [:]
        def divChilds = []
        def lastDivTag = null
        divs.each { div ->
            //println 'div:'+div.name()+', class:'+div.@class
            if (div in tags || div in engdTags) {
                //println 'div is useful!!!!!!!'
                if (lastDivTag) {

                    def key = lastDivTag.toString().trim()


                    StringBuilder sb = new StringBuilder()
                    for (int i = 0; i < divChilds.size(); i++) {
                        def line = divChilds.get(i)

                        boolean include = true;
                        if (key.contains('原文') && i == 0 && line instanceof NodeChild) {
                            NodeChild nc = (NodeChild) line

                            def node = nc.getAt(0)
                            //println node.getClass().getName()
                            if (node != null && node instanceof groovy.util.slurpersupport.Node) {
                                groovy.util.slurpersupport.Node childNode = node;
                                def children = childNode.children()
                                if (children?.size() > 0) {
                                    //println children.get(0).getClass().getName()
                                    if (children.get(0) instanceof groovy.util.slurpersupport.Node) {
                                        include = false
                                    }
                                }
                            }
                            //println line.toString() + '||||'+nc.localText()
                        }
                        if (include) {
                            sb.append(line).append("\n")
                        }
                    }
//                    divChilds.each { line ->
//
//
//                    }
                    String content = sb.toString()
                    content = content.replace('\n\n', '\n')
                    //println divChilds.size() + ":    key:" + lastDivTag + "--->" + content
                    // println '!!!!!!!!!!!!!!!div:'+div.toString() +',$$$$$'+lastDivTag
                    //println 'key:'+lastDivTag+', content:'+content +'*****'


                    if (key.contains(word) && key.contains('原文') && key.contains('编辑')) {
                        key = 'yuanwen'
                    } else if (key.contains('作者') && key.contains('编辑')) {
                        key = 'zzjj'
                    } else if (key.contains('鉴赏') && key.contains('编辑')) {
                        key = 'shangxi1'
                    } else if (key.contains('赏析') && key.contains('编辑')) {
                        key = 'shangxi2'
                    } else if (key.contains('注释') && key.contains('编辑')) {
                        key = 'zhushi'
                    } else if (key.contains('注解') && key.contains('编辑')) {
                        key = 'zhujie'
                    } else if (key.contains('译文') && key.contains('编辑')) {
                        key = 'yiwen'
                    } else if (key.contains('背景') && key.contains('编辑')) {
                        key = 'beijing'
                    } else if (key.contains('出处')) {
                        key = 'chuchu'
                    } else if (key.contains('别名')) {
                        key = 'bieming'
                    } else if (key.contains('影响')) {
                        key = 'yingxiang'
                    } else if (key.contains('简析')) {
                        key = 'shangxi3'
                    }  else if (key.contains('简介')) {
                        key = 'shangxi4'
                    } else if (key.contains('正文')) {
                        key = 'zhengwen'
                    } else if (key.contains('诗歌简介')) {
                        key = 'shangxi4'
                    } else if (key.contains('作品评析')) {
                        key = 'shangxi5'
                    } else if (key.contains('释义')) {
                        key = 'shiyi'
                    }

                    def oldValue = divMap[key]
                    if (oldValue == null) {
                        oldValue = ''
                    } else {
                        oldValue = '\n'
                    }
                    divMap[key] = oldValue + '' + content.toString().trim()
                    divChilds.clear()
                }


                lastDivTag = div
                if (div in engdTags) {
                    lastDivTag = null
                    //println 'set lastDivTag null'
                }
            } else {
                //println 'lastDivTag:'+lastDivTag +', divChilds.size:'+divChilds.size()
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
            } else if (name.contains('出处')) {
                name = 'chuchu'
            } else if (name.contains('别名')) {
                name = 'bieming'
            }

            //println "" + name + ":" + value
            divMap[name] = value
        }

        divMap['link'] = url
        divMap['sum'] = summary
        divMap['pid'] = pid


        def shangxiList = []
        10.times {
            def key = 'shangxi' + it
            def sx = divMap[key]
            if (sx != null && sx.length() > 0) {
                shangxiList << sx
            }
        }
        divMap['shangxis'] = shangxiList


        def yuanwen = divMap['yuanwen']
        if (!yuanwen) {
            yuanwen = ''
            def yuanwenlist = page."**".findAll {
                it.name() == 'DIV' && it.@class == 'para'
            }

            yuanwenlist.each { line ->
                if (line.toString().equals(summary)) {
                    return
                }
                yuanwen += (line.toString().trim() + '\n')
            }
            yuanwen = yuanwen.toString().trim()
            yuanwen = yuanwen.toString().replace('\n\n', '\n')

            shangxiList.each { sx ->
                yuanwen = yuanwen.toString().replace(sx, '')
            }
            yuanwen = yuanwen.toString().replace('\n\n', '\n')

            divMap.each { it ->
                yuanwen = yuanwen.toString().replace(it.value.toString(), '')
            }
            yuanwen = yuanwen.toString().replace('\n\n', '\n')

            divMap['yuanwen'] = yuanwen
        }

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
        if (author == null || author.equals("null") || title == null || title.equals('null')) {
            println "[" + word + ']   failed!  author:' +author +', title:'+title
            notExistWords << pid + '<<' + word
            return false;
        }


        if (title == '《' + word + "》") {
            title = word
            divMap['title'] = title
        }

        if (title.toString().startsWith('《') && title.toString().endsWith('》')) {
            title = title.toString().substring(1, title.toString().length() - 1)
            divMap['title'] = title
        }


        if (title != word && ld(title, word)>2) {
            println '[' + pid + ', ' + word + ']   失败了!!  author:' +author +', title:'+title
            notExistWords << pid + '<<' + word
            return false;
        }

        if (author != zuozhe && author != '无名氏') {
            println '[' + pid + ', ' + word + '] 作者不匹配  失败了!!  author:' +author +', title:'+title
            notExistWords << pid + '<<' + word
            return false;
        }

        def zzjj = divMap['zzjj']
        if (zzjj) {
            zzjj = zzjj.toString().replace(author + '像', '').trim()
            divMap['zzjj'] = zzjj
        }

        def builder = new JsonBuilder()
        // 构建json格式的poem
        builder {
            poem divMap
        }

        def finalData = JsonOutput.prettyPrint(builder.toString())
        //println finalData

        def fname = pid + '_' + author + "_" + title + ".json"
        fname = formatString(fname)
        fname = fname.replace("<", "")
        fname = fname.replace(">", "")
        fname = fname.replace("\"", "")
        fname = fname.replace(":", "")
        fname = fname.replace("=", "")
        saveToFile(rootDir, pid, fname, finalData)

        return true;
    }

    static void main(args) {
        new BaiduBaike2Loader().perform()
    }

}
