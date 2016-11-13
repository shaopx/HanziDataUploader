/**
 * Created by Administrator on 2016/11/13.
 */
class FileDataHandlerA {


    def storagePath = 'd:/data/crawler/gushiwen/raw'
    def storagePath2 = 'd:/data/crawler/gushiwen/round1/'

    def usefulTags = ['作者', '作者：', '译文', '孟子', '历史来源', '家族名人', '地望分布']
    def replacedTags = ['|翻译', '|赏析', '_古诗文网']
    def lineMap = ["": 0];

    def perform() {
        def dir = new File(storagePath)
        if (dir.isDirectory()) {
            dir.eachFileRecurse { file ->
                //println "do file: ${file.getAbsolutePath()}"
                file.eachLine { line ->

                    if (line.trim().length() == 0) {
                        return;
                    }
                    def value = lineMap.get(line);
                    lineMap.put(line, value == null ? 1 : value + 1)
                }

                //println 'line map size:' + lineMap.size()
            }
        }
        println 'final line map size:' + lineMap.size()

//        lineMap.s

        def time1 = System.currentTimeMillis();

        def lineMapEntrySet = lineMap.entrySet();
        def lineList = lineMapEntrySet.toList();

//        for (i in 0..40) {
//            def key = lineList[i].getKey();
//            def value = lineList[i].getValue();
//            println "[" + i + "] count: " + value + " line: " + key
//        }


        lineList.sort { a, b ->
            return b.getValue() - a.getValue()
        }

        def time2 = System.currentTimeMillis();
        println 'sor use time:' + (time2 - time1) + "ms"
        def removableLines = []
        for (item in lineList) {
            if (item.getValue() > 100) {
                removableLines.add(item.getKey());
            }
        }


        for (i in 0..1000) {
            def key = lineList[i].getKey();
            def value = lineList[i].getValue();
            println "[" + i + "]count: " + value + " line: " + key
        }

        for (item in removableLines) {
            println "remove item " + item
        }

        ///第二轮处理
        def dir2 = new File(storagePath2)
        if (!dir2.exists())
            dir2.mkdirs()

        if (dir.isDirectory()) {
            dir.eachFileRecurse { file ->
                //println "do file: ${file.getName()}"

                def fileData = []

                new File(dir2, file.getName()).withPrintWriter { printWriter ->
                    file.eachLine { line ->

                        if (line.trim().length() == 0) {
                            return;
                        }

                        if (removableLines.contains(line) && !usefulTags.contains(line) && !line.contains("作者：")) {
                            return;
                        }

                        for (replace in replacedTags) {
                            line = line.replace(replace, '')
                        }

                        //println 'write line:' + line
                        fileData.add(line);
                        printWriter.println(line)

                    }

                }

            }
        }

    }

    public static void main(args) {
        new FileDataHandlerA().perform();
    }
}
