package util

import com.mongodb.client.MongoDatabase
import db.GroovyDataLoader
import org.bson.Document

/**
 * Created by SHAOPENGXIANG on 2016/12/5.
 */
class ErrorHandler {

    def collectionName = 'error'

    @Delegate
    GroovyDataLoader dbLoader = GroovyDataLoader.instance

    def errorCl = dbLoader.getOnlineCl(collectionName)

    void saveError(datamap) {
        Document document = new Document();
        def errors = datamap['errors']
        def errorCodes = ''
        if (errors != null) {
            errors.each { err ->
                if (errorCodes.length() > 0) {
                    errorCodes += ','
                }
                errorCodes +=  err['code']
            }
        }

        datamap['errorcodes'] = errorCodes

        datamap.each() {
            document.append(it.key, it.value.toString());
        }
        errorCl.insertOne(document);
    }
}
