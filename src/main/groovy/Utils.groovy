/**
 * Created by shaopengxiang on 2016/11/15.
 */
class Utils {

    def getFileSubDir(int uid) {
        for (int i = 1000; i < 100000; i += 1000) {
            if (uid <i) {
                return dir + "/" + i + "/";
            }
        }
        return dir + "/big/";
    }

    def saveToFile(uid, fn, content) {
        def subDir = getFileSubDir(uid);
        def file = new File(subDir);
        if (!file.exists()) {
            file.mkdirs()
        }

        new File(subDir, fn).withPrintWriter { printWriter ->
            printWriter.println(content)
        }

        new File(subDir, "error_" + uid + ".txt").delete()
    }
}
