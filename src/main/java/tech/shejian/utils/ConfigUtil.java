/*
 * @Description: 配置文件读取工具
 * @Date: 2021/4/20
 */
package tech.shejian.utils;

import jdk.nashorn.internal.runtime.regexp.joni.Config;

import java.io.IOException;
import java.util.Properties;

/***
 * 1. 可以读取不同的配置文件
 * 2. 可以读取配置文件中的某一个变量
 */
public class ConfigUtil {

    private String filePath;
    private Properties props = new Properties();

    /***
     * 读取文件
     * @param filePath
     */
    public ConfigUtil read(String filePath) {
        this.filePath = filePath;
        return this;
    }

    /***
     *
     * @param key
     * @return
     */
    public String get(String key){
        try {
            this.props.load(ConfigUtil.class.getClassLoader().getResourceAsStream(this.filePath.trim()));
        } catch (IOException e) {
            e.printStackTrace();
        }
        return this.props.getProperty(key.trim());
    }

    public static ConfigUtil builder(){
        return new ConfigUtil();
    }
}
