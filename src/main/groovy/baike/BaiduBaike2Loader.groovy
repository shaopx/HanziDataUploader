package baike

import com.mongodb.BasicDBObject
import com.mongodb.client.MongoCollection
import db.GroovyDataLoader
import groovy.sql.Sql
import groovy.util.slurpersupport.NodeChild
import org.bson.Document
import org.cyberneko.html.parsers.SAXParser
import util.ErrorHandler
import util.ParseUtils
import util.Utils

/**
 * Created by SHAOPENGXIANG on 2016/11/30.
 */
class BaiduBaike2Loader {
    def rootDir = "c:/dev/data/poem/baidu/";
    def category = 'baike'

    def notExistWords = []

    def dbLoader = GroovyDataLoader.instance
    MongoCollection<Document> baikeCl = null

    @Delegate
    def ErrorHandler errorHandler = new ErrorHandler()

    def blockDivClasses = ['promotion_title', 'side-content', 'lemmaWgt-promotion-vbaike', 'promotion_viewport',
                           'clear', 'side-box lemma-statistics', 'description', 'credit-title',
                           'bd_weixin_popup_header', 'top-collect', 'collect-tip', 'bdsharebuttonbox', 'promotion-declaration', 'open-tag']

    void perform() {
        dbLoader.copyDbs()



        def mongoDb = dbLoader.getOnlineDb()
        baikeCl = mongoDb.getCollection("baike");

        def sql_poem = Sql.newInstance("jdbc:sqlite:poem.db", "", "", "org.sqlite.JDBC")

        def sql_id_list = "select * from poem where _id >0 and _id<=300000 "

        sql_poem.eachRow(sql_id_list) { row ->
            def pname = row["mingcheng"]
            def pauthor = row["zuozhe"]
            def pid = row["_id"]

            def time = Utils.getCurentTime('yyyy-MM-dd HH:mm:ss')

            def datamap = [:]
            datamap['pid'] = pid
            datamap['zuozhe'] = pauthor
            datamap['mingcheng'] = pname
            datamap['date'] = time
            datamap['category'] = category


            if (isPidExist(pid)) {
                println '[' + pid + ']' + pname + ' exist!'
                return
            }

            println 'pid:' + pid + ', zuozhe:' + pauthor + ', pname:' + pname


            boolean succeed = requestWord(pid, pauthor, pname, datamap)

            if (!succeed) {
                saveError(datamap)
            } else {
                saveWord(datamap)
            }
        }
        //doRequestWord('五弦弹－恶郑之夺雅也')

        writeErrors()
    }

    boolean isPidExist(pid) {
        BasicDBObject query = new BasicDBObject();
        query.put("pid", pid.toString());

        def pidFind = baikeCl.find(query)
        //println 'pidFind.size:'+pidFind.size()
        if (pidFind.size() == 0) {
            return false
        }
        return true;
    }

    void writeErrors() {
        String wordstr = ''
        notExistWords.each {
            wordstr += (it.toString() + '\r\n')
        }
        Utils.errorText(rootDir, 'error', wordstr)
    }

    boolean requestWord(pid, pauthor, pname, datamap) {
        boolean succeed = doRequestWord(pid, pauthor, pname, datamap)
        if (!succeed) {
            def newName = pname.toString().replace('。', '·')
            if (newName != pname) {
                succeed = doRequestWord(pid, pauthor, newName, datamap)
            }

            if (succeed) {
                return true
            }


            def names = newName.split('·')

            names.each { name ->
                if (name != pname) {
                    succeed = succeed || doRequestWord(pid, pauthor, name, datamap)
                    if (succeed) {
                        return true
                    }
                }

            }

            if (succeed) {
                return true
            }


            names = newName.split('/')
            names.each { name ->
                if (name != pname) {
                    succeed = succeed || doRequestWord(pid, pauthor, name, datamap)
                }

            }
            if (succeed) {
                return true
            }

            names = newName.split(' ')
            names.each { name ->
                if (name != pname) {
                    succeed = succeed || doRequestWord(pid, pauthor, name, datamap)
                }

            }
            if (succeed) {
                return true
            }

            names = newName.split('　')
            names.each { name ->
                if (name != pname) {
                    succeed = succeed || doRequestWord(pid, pauthor, name, datamap)
                }

            }
            if (succeed) {
                return true
            }
        }

        if (!succeed) {
//                saveError(datamap)
            return false
        } else {
            return true;
        }

    }

    void saveWord(datamap) {
        Document document = new Document();

        datamap.each() {
            document.append(it.key, it.value.toString());
        }
        baikeCl.insertOne(document);
    }

    def onError(category, desc, code, datamap) {
        println 'erorr[' + desc + ']'
        def pid = datamap['pid']
        def word = datamap['word']
        notExistWords << pid + '<<' + word
        datamap.put("category", category.toString());
        datamap.put("desc", desc.toString());
        datamap.put("code", code.toString());
    }

    boolean doRequestWord(pid, zuozhe, word, datamap) {
        def url = "http://baike.baidu.com/item/" + word

        def errormap = [:]

//        datamap['url'] = url

//        errormap.putAll(datamap)
        errormap['url'] = url
        errormap['word'] = word
//        errormap['pid'] = pid

        def errors = datamap['errors']
        if (errors == null) {
            errors = []
            datamap['errors'] = errors
        }
        datamap['errors'] << errormap

        def text = Utils.getUrlTextContent(rootDir, url, errormap);
        println 'doRequestWord:' + word
        if (!text || text.contains('百度百科错误页') && text.contains('您所访问的页面不存在')) {
            onError(category, '百科没有本词条', 1, errormap)
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
        //println 'tags:' + tags.toString()

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

                    String content = sb.toString()
                    content = content.replace('\n\n', '\n')
                    //println divChilds.size() + ":    key:" + lastDivTag + "--->" + content
                    // println '!!!!!!!!!!!!!!!div:'+div.toString() +',$$$$$'+lastDivTag
                    //println 'key:'+lastDivTag+', content:'+content +'*****'

                    key = ParseUtils.getTag(key)

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

            name = ParseUtils.getTag(name)

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

        //println '=============================='


        if (zuozhe == '无名氏' || zuozhe == '佚名') {
            divMap['zuozhe'] = zuozhe
        }

        def author = divMap['zuozhe']
        def title = divMap['title']
        if (title == null || title.equals('null')) {
            onError(category, '百科名称不能解析', 2, errormap)
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


        if (title != word && !(title.toString().contains(word)) && Utils.ld(title, word) > 2) {
            datamap['title'] = title
            onError(category, '名称相差太大', 3, errormap)
            return false;
        }

        if (author != zuozhe && author != '无名氏') {
            datamap['baike_author'] = author
            datamap['title'] = title

            onError(category, '作者不一致', 4, errormap)
            return false;
        }

        def zzjj = divMap['zzjj']
        if (zzjj) {
            zzjj = zzjj.toString().replace(author + '像', '').trim()
            divMap['zzjj'] = zzjj
        }

//        def builder = new JsonBuilder()
//        // 构建json格式的poem
//        builder {
//            poem divMap
//        }
//
//        def finalData = JsonOutput.prettyPrint(builder.toString())
//        //println finalData
//
//        def fname = pid + '_' + author + "_" + title + ".json"
//        fname = Utils.formatString(fname)
//        fname = fname.replace("<", "")
//        fname = fname.replace(">", "")
//        fname = fname.replace("\"", "")
//        fname = fname.replace(":", "")
//        fname = fname.replace("=", "")
//        Utils.saveToFile(rootDir, pid, fname, finalData)

        errormap.clear()
        datamap.putAll(divMap)
        return true;
    }

    static void main(args) {
        new BaiduBaike2Loader().perform()
    }

}
