/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package jlotoprint.model;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.annotations.Expose;
import java.util.ArrayList;
import java.util.HashMap;
import jlotoprint.model.MarkInfo;

/**
 *
 * @author Marcel.Barbosa
 */
public class Model {

    //separator for source file numbers

    public static String NUMBER_SEPARATOR = "-";
    //mark nubmer counter
    public static String NUMBER_COUNT_TYPE = "numberCount";
    //mark option value
    public static String OPTION_TYPE = "option";

    @Expose
    private String name;
    @Expose
    private String image = "";
    @Expose
    private String imagePreview = "";
    @Expose
    private double imageWidth = 0;
    @Expose
    private double imageHeight = 0;
    @Expose
    private int dpi = 200;
    @Expose
    public HashMap<String, ArrayList<MarkInfo>> groupMap = new HashMap<>();
    @Expose
    public ArrayList<MarkInfo> numberCountMap = new ArrayList<>();
    @Expose
    public ArrayList<String> groupList = new ArrayList<>();

    public ArrayList<String> getGroupList() {
        if (groupList.size() == 0) {
            ArrayList<String> defaultList = new ArrayList<>();
            defaultList.add("Group_1");
            defaultList.add("Group_2");
            setGroupList(defaultList);
            return defaultList;
        } else {
            return groupList;
        }
    }

    public void setGroupList(ArrayList<String> groupList) {
        this.groupList = groupList;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getImage() {
        return image;
    }

    public void setImage(String image) {
        this.image = image;
    }

    public String getImagePreview() {
        return imagePreview;
    }

    public void setImagePreview(String imagePreview) {
        this.imagePreview = imagePreview;
    }

    public double getImageWidth() {
        return imageWidth;
    }

    public void setImageWidth(double imageWidth) {
        this.imageWidth = imageWidth;
    }

    public double getImageHeight() {
        return imageHeight;
    }

    public void setImageHeight(double imageHeight) {
        this.imageHeight = imageHeight;
    }

    public HashMap<String, ArrayList<MarkInfo>> getGroupMap() {
        return groupMap;
    }

    public void setGroupMap(HashMap<String, ArrayList<MarkInfo>> groupMap) {
        this.groupMap = groupMap;
    }

    public int getDpi() {
        return dpi;
    }

    public void setDpi(int dpi) {
        this.dpi = dpi;
    }

    public ArrayList<MarkInfo> getNumberCountMap() {
        return numberCountMap;
    }

    public void setNumberCountMap(ArrayList<MarkInfo> numberCountMap) {
        this.numberCountMap = numberCountMap;
    }

    public Model() {

    }

    public String toJson() {
        Gson g = new GsonBuilder().excludeFieldsWithoutExposeAnnotation().create();
        return g.toJson(this);
    }
}
