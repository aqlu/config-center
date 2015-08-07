package com.qianmi.common.config.loader.file;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.util.Assert;

import java.io.*;
import java.nio.charset.Charset;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.*;

/**
 * <p>Properties文件加载接口</p>
 * 文件命名规则为：filename.version.properties，示例：db.1.0.properties
 * Created by aqlu on 15/5/15.
 */
public class PropertiesFileLoader implements FileLoader<Properties> {

    protected Logger logger = LoggerFactory.getLogger(getClass());

    /**
     * 加载配置文件
     * @param filePath 配置文件全路径
     * @return 配置集合
     * @throws Exception
     */
    public Properties load(String filePath) throws Exception {
        Assert.hasText(filePath, "argument of [filePath] must not be null or empty!");

        Properties properties = new Properties();
        try (InputStream inputStream = new FileInputStream(new File(filePath))){
            properties.load(inputStream);
        } catch (Exception e) {
            logger.warn("**** load file: {} failed! ", filePath);
            throw e;
        }
        return properties;
    }

    public void destroy() {
        logger.debug("PropertiesFileLoader destroy");
    }

    /**
     * 保存配置文件
     * @param filePath 配置文件全路径
     * @param properties 配置集合
     * @param comments 注释信息
     * @throws Exception
     */
    public void save(String filePath, Properties properties, String comments)  throws Exception{
        Assert.hasText(filePath, "argument of [filePath] must not be null or empty!");
        Assert.notNull(properties, "argument of [properties] must not be null");

        Path configDirPath = Paths.get(filePath).getParent();
        if (Files.notExists(configDirPath)) {
            Files.createDirectories(configDirPath);
        }

        try(Writer writer = new OutputStreamWriter(new FileOutputStream(filePath), Charset.forName("UTF-8"))){
            properties.store(writer, comments);
        } catch (Exception e) {
            logger.warn("*** save file: {} failed! properties: {}", filePath, properties);
            throw e;
        }
    }
}
