/**
 * Created by shaopengxiang on 2016/11/15.
 */
class Utils {

    static String getFileSubDir(rootdir, uid) {
        for (int i = 1000; i < 1000000; i += 1000) {
            if (uid <i) {
                return rootdir + "/" + i + "/";
            }
        }
        return rootdir + "/big/";
    }

    static void saveToFile(rootdir, uid, fn, content) {
        def subDir = getFileSubDir(rootdir, uid);
        def file = new File(subDir);
        if (!file.exists()) {
            file.mkdirs()
        }

        new File(subDir, fn).withPrintWriter { printWriter ->
            printWriter.println(content)
        }

        if(!fn.startsWith("error")){
            new File(subDir, "error_" + uid + ".txt").delete()
        }

    }


    static void errorText(rootdir, int uid, String text) {
        System.err.println("error")
        saveToFile(rootdir, uid, "error_" + uid + ".txt", text);
    }

    static String formatString(String str) {
        return str.replaceAll("，", ",").replaceAll("。", ".").
                replace("<span class=\"comment\">", "(").
                replace("</span>", ")").replace("\t", "").replace("{", "(").replace("}", ")").replace("\\", "_").replace("/", "");
    }

    public static void main(args){
        def subDir = getFileSubDir('d:/dev', 3457)
        println subDir
    }
}
