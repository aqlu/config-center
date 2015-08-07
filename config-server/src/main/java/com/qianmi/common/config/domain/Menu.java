package com.qianmi.common.config.domain;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

/**
 * Menu Entity
 * Created by aqlu on 15/6/13.
 */
public class Menu implements Serializable {

    private static final long serialVersionUID = 1L;

    /**
     * 主键，唯一标识符
     */
    protected String id;

    /**
     * 名称,不能包含"/"字符
     */
    protected String text;

    /**
     * 别名，可用来快速搜索
     */
    protected String alias;

    /**
     * 父节点ID
     */
    protected String parentId;

    /**
     * 图表样式
     */
    protected String iconCls;

    /**
     * 是否叶子节点
     */
    protected boolean leaf;

    /**
     * 对应功能的url
     */
    protected String url;

    /**
     * 节点类型
     */
    protected String nodeClass;

    /**
     * 菜单对应的权限信息
     */
    protected List<String> authorities = new ArrayList<String>();

    public String getNodeClass() {
        return nodeClass;
    }

    public void setNodeClass(String nodeClass) {
        this.nodeClass = nodeClass;
    }

    public List<String> getAuthorities() {
        return authorities;
    }

    public void setAuthorities(List<String> authorities) {
        this.authorities = authorities;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getText() {
        return text;
    }

    public void setText(String text) {
        this.text = text;
    }

    public String getAlias() {
        return alias;
    }

    public void setAlias(String alias) {
        this.alias = alias;
    }

    public String getParentId() {
        return parentId;
    }

    public void setParentId(String parentId) {
        this.parentId = parentId;
    }

    public String getIconCls() {
        return iconCls;
    }

    public void setIconCls(String iconCls) {
        this.iconCls = iconCls;
    }

    public boolean isLeaf() {
        return leaf;
    }

    public void setLeaf(boolean leaf) {
        this.leaf = leaf;
    }

    public String getUrl() {
        return url;
    }

    public void setUrl(String url) {
        this.url = url;
    }

    public String toString() {
        return "Menu{" +
                "id='" + id + '\'' +
                ", text='" + text + '\'' +
                ", alias='" + alias + '\'' +
                ", parentId='" + parentId + '\'' +
                ", iconCls='" + iconCls + '\'' +
                ", leaf=" + leaf +
                ", url='" + url + '\'' +
                ", authorities=" + authorities +
                '}';
    }
}
