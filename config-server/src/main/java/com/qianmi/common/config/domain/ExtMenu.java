package com.qianmi.common.config.domain;

import java.util.ArrayList;
import java.util.List;

/**
 * Ext 菜单
 * Created by aqlu on 15/6/16.
 */
public class ExtMenu extends Menu {

    /**
     * 是否展开
     */
    private boolean expandable;

    /**
     * 单击展开
     */
    private boolean singleExpand;


    /**
     * id path. 全路径
     */
    protected String idPath;

    /**
     * text path. 全路径
     */
    protected String textPath;

    /**
     * 子节点
     */
    private List<Menu> children = new ArrayList<Menu>();

    public ExtMenu(){}

    public ExtMenu(String id, String text, String iconCls, String parentId, boolean leaf, String idPath, String textPath, String nodeClass){
        this.id = id;
        this.text = text;
        this.textPath = textPath;
        this.iconCls = iconCls;
        this.parentId = parentId;
        this.leaf = leaf;
        this.idPath = idPath;
        this.textPath = textPath;
        this.nodeClass = nodeClass;
    }

    public ExtMenu(Menu menu){
        this.id = menu.id;
        this.text = menu.text;
        this.iconCls = menu.iconCls;
        this.parentId = menu.parentId;
        this.leaf = menu.leaf;
        this.alias = menu.alias;
        this.url = menu.url;
        this.authorities = menu.authorities;
    }

    public boolean isExpandable() {
        return expandable;
    }

    public void setExpandable(boolean expandable) {
        this.expandable = expandable;
    }

    public boolean isSingleExpand() {
        return singleExpand;
    }

    public void setSingleExpand(boolean singleExpand) {
        this.singleExpand = singleExpand;
    }

    public List<Menu> getChildren() {
        return children;
    }

    public void setChildren(List<Menu> children) {
        this.children = children;
    }

    public void addChildren(Menu menu) {
        this.children.add(menu);
    }

    public String getTextPath() {
        return textPath;
    }

    public void setTextPath(String textPath) {
        this.textPath = textPath;
    }

    public String getIdPath() {
        return idPath;
    }

    public void setIdPath(String idPath) {
        this.idPath = idPath;
    }


}
