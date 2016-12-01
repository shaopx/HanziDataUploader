package util

/**
 * Created by SHAOPENGXIANG on 2016/12/1.
 */
class ParseUtils {

    static String getTag(String raw){
        if(raw.equals('作者')){
            return 'zuozhe'
        } else if(raw.contains('作品介绍')){
            return 'jieshao'
        } else if(raw.contains('作者介绍') || raw.contains('作者简介')){
            return 'zzjj'
        } else if(raw.contains('原文') || raw.contains('正文')){
            return 'yuanwen'
        } else if(raw.contains('注释') || raw.contains('注解')){
            return 'zhujie'
        }  else if(raw.contains('繁体') || raw.contains('对照')){
            return 'fanti'
        } else {
            return 'xxxx'
        }
    }
}
