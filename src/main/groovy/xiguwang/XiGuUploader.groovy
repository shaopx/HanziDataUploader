package xiguwang

import com.mongodb.BasicDBObject
import com.mongodb.client.MongoCollection
import db.GroovyDataLoader
import groovy.json.JsonSlurper
import groovy.sql.Sql
import org.bson.Document
import util.ParseUtils

/**
 * Created by SHAOPENGXIANG on 2016/12/1.
 */
class XiGuUploader {
    def rootDir = "c:/dev/data/poem/xigutang/gushi/";
    def dbLoader = GroovyDataLoader.instance
    MongoCollection<Document> xigutangCl = null
    def sql_poem

    void perform() {
        def mongoDb = dbLoader.getOnlineDb()
        xigutangCl = mongoDb.getCollection("xigutang");

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

    def getPid(pname, pauthor) {
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
            if (!(pid in pids)) {
                pids << pid
            }
        }
        return pids
    }

    boolean insertOnePoem(pname, pauthor, fileJson) {

        def pids = getPid(pname, pauthor)
        println 'pids:' + pids

        BasicDBObject query = new BasicDBObject();
        query.put("n", pname);
        query.put("a", pauthor);
        def findPoem = xigutangCl.find(query)
        if (findPoem.size() == 0) {
            //插入

            Document document = new Document();
            document.append("pids", "" + pids);
            document.append("n", pname);
            document.append("a", pauthor);
            if (pids.size() == 1) {
                document.append("pid", "" + pids[0]);
            }
            if (pids.size() == 0) {
                document.append("pidcount", "0");
            } else {
                document.append("pidcount", "" + pids.size());
            }

            def data = fileJson.poem.data
            data.each {
                //println 'key:' + it.key + ', value:' + it.value
                def tag = ParseUtils.getTag(it.key)
                document.append(tag, it.value.toString());
            }

            xigutangCl.insertOne(document);
        }
    }

    static void main(args) {
        new XiGuUploader().perform()
    }
}
