package xiguwang

import com.mongodb.BasicDBObject
import com.mongodb.client.MongoCollection
import db.GroovyDataLoader
import groovy.json.JsonSlurper
import groovy.sql.Sql
import org.bson.Document
import util.ParseUtils
import util.Utils

/**
 * Created by SHAOPENGXIANG on 2017/1/22.
 */
class XiGuUploader2 {
    def rootDir = "c:/dev/data/poem/xigutang/gushi/";
    def dbLoader = GroovyDataLoader.instance
    MongoCollection<Document> zhujieCollection = null
    MongoCollection<Document> shangxiCollection = null
    MongoCollection<Document> yiwenCollection = null
    def sql_poem

    void perform() {
        def mongoDb = dbLoader.getOnlineDb()
        zhujieCollection = mongoDb.getCollection("zhujie_xigu");
        shangxiCollection = mongoDb.getCollection("shangxi_xigu");
        yiwenCollection = mongoDb.getCollection("yiwen_xigu");

        dbLoader.copyDbs()

        sql_poem = Sql.newInstance("jdbc:sqlite:poem.db", "", "", "org.sqlite.JDBC")

        def dir = new File(rootDir)
        if (dir.isDirectory()) {
            dir.eachFileRecurse { file ->
                if (!file.isDirectory()) {
                    def fileJson = new JsonSlurper().parseText(file.text)
//                    println 'fileJson:'+fileJson
                    //println 'n:'+fileJson.poem.n +', a:'+fileJson.poem.a
                    insertOnePoem(fileJson.poem.n, fileJson.poem.a, fileJson)
                }
            }
        }

    }

    def getPid(pname, pauthor, yuanwen) {
        String name = pname.toString()
        def ns = name.tokenize("·，,.-")
        String condition = ""
        ns.each {
            if (condition.length() > 0) {
                condition += " or "
            }
            condition += " mingcheng like'%" + it + "%' "
        }

        condition += "and zuozhe =='" + pauthor + "'"

        def sql_id_list = "select * from poem where " + condition

        println sql_id_list
        def pids = []
        sql_poem.eachRow(sql_id_list) { row ->
            def pid = row["_id"]
            def yuanwen2 = row["yuanwen"]
            if (!(pid in pids)) {
                if(like(yuanwen, yuanwen2)){
                    pids<<pid
                }
            }
        }
        return pids
    }

    boolean like(yuanwen1, yuanwen2){
        return Utils.sim(yuanwen1, yuanwen2)>0.5
    }

    boolean insertOnePoem(pname, pauthor, fileJson) {
        Document filedoc = new Document();
        def data = fileJson.poem.data
        data.each {
            //println 'key:' + it.key + ', value:' + it.value
            def tag = ParseUtils.getTag(it.key)
            filedoc.append(tag, it.value.toString());
        }

        def zhujie = filedoc.get("zhujie");
        def zhushi = filedoc.get("zhushi");
        def shiyi = filedoc.get("shiyi");
        def yiwen = filedoc.get("yiwen");
        def yuanwen = filedoc.get("yuanwen");
        def beijing = filedoc.get("beijing");

        def pids = getPid(pname, pauthor, yuanwen)
        println 'pids:' + pids

        if (pids.size() > 0) {
            def pid = pids[0];
            Document document = new Document();
            document.append("pid", pid);
            document.append("name", pname);
            document.append("author", pauthor);



            if (zhujie) {
                insertZhujie(pid, pname, pauthor, yuanwen, zhujie)
            }

            if (zhushi) {
                insertZhujie(pid, pname, pauthor, yuanwen, zhushi)
            }

            if (shiyi) {
                insertZhujie(pid, pname, pauthor, yuanwen, shiyi)
            }

            if (yiwen) {
                insertYiwen(pid, pname, pauthor, yuanwen, yiwen)
            }

            if (beijing) insertShangxi(pid, pname, pauthor, yuanwen, beijing, 'beijing')

            def shangxi_1 = document.get("shangxi1");
            if (shangxi_1) insertShangxi(pid, pname, pauthor, yuanwen, shangxi_1, 'shangxi1')

            def shangxi_2 = document.get("shangxi2");
            if (shangxi_2) insertShangxi(pid, pname, pauthor, yuanwen, shangxi_2, 'shangxi2')

            def shangxi_3 = document.get("shangxi3");
            if (shangxi_3) insertShangxi(pid, pname, pauthor, yuanwen, shangxi_3, 'shangxi3')

            def shangxi_4 = document.get("shangxi4");
            if (shangxi_4) insertShangxi(pid, pname, pauthor, yuanwen, shangxi_4, 'shangxi4')

            def shangxi_5 = document.get("shangxi5");
            if (shangxi_5) insertShangxi(pid, pname, pauthor, yuanwen, shangxi_5, 'shangxi5')

        }


    }

    boolean insertZhujie(pid, pname, pauthor, yuanwen, zhujie) {
        BasicDBObject query = new BasicDBObject();
        query.put("pid", pid);
        def findZhujie = zhujieCollection.find(query)
        if (findZhujie.size() == 0) {

            Document document = new Document();
            document.append("pid", "" + pid);
            document.append("name", "" + pname);
            document.append("author", "" + pauthor);
            document.append("yuanwen", "" + yuanwen);
            document.append("zhujie", zhujie);
            document.append("src", 'xigutang');
            document.append("srcDesc", '习古堂');


            zhujieCollection.insertOne(document);
        }
    }

    boolean insertYiwen(pid, pname, pauthor, yuanwen, yiwen) {
        BasicDBObject query = new BasicDBObject();
        query.put("pid", pid);
        def findZhujie = yiwenCollection.find(query)
        if (findZhujie.size() == 0) {

            Document document = new Document();
            document.append("pid", "" + pid);
            document.append("name", "" + pname);
            document.append("author", "" + pauthor);
            document.append("yuanwen", "" + yuanwen);
            document.append("yiwen", yiwen);
            document.append("src", 'xigutang');
            document.append("srcDesc", '习古堂');


            yiwenCollection.insertOne(document);
        }
    }

    boolean insertShangxi(pid, pname, pauthor, yuanwen, shangxi, index) {
        BasicDBObject query = new BasicDBObject();
        query.put("pid", pid);
        def findZhujie = zhujieCollection.find(query)
        if (findZhujie.size() == 0) {

            Document document = new Document();
            document.append("pid", "" + pid);
            document.append("name", "" + pname);
            document.append("author", "" + pauthor);
            document.append("yuanwen", "" + yuanwen);
            document.append("shangxi", "" + shangxi);
            document.append("src", 'xigutang');
            document.append("srcDesc", '习古堂');
            document.append("index", index);

            shangxiCollection.insertOne(document);
        }
    }

    static void main(args) {
        new XiGuUploader2().perform()
    }
}
