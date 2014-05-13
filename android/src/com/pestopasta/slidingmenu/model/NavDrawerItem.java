package com.pestopasta.slidingmenu.model;

public class NavDrawerItem {

    private String title;
    private int icon;
    private boolean isUserImage;
    private String itemName;
    private boolean isCheckBox;
    private boolean isEditText;
    private String helpText;

    public NavDrawerItem(){}

    // customItemType = 0 --> UserImage
    // customItemType = 1 --> Checkbox
    public NavDrawerItem(int customItemType, String itemName) {
        super();
        this.icon = 0;
        if (customItemType == 0) {
            this.isUserImage = true;
        } else if (customItemType == 1) {
            this.isEditText = true;
            this.helpText = itemName;
        } else if (customItemType == 2) {
            this.isCheckBox = true;
            this.itemName = itemName;
        }
    }

    public NavDrawerItem(String itemName, int icon){
        super();
        this.itemName = itemName;
        this.icon = icon;
    }

    public NavDrawerItem(String title) {
        this(null, 0);
        this.title = title;
    }

    public String getTitle(){
        return this.title;
    }

    public int getIcon(){
        return this.icon;
    }

    public void setTitle(String title){
        this.title = title;
    }

    public void setIcon(int icon){
        this.icon = icon;
    }

    public boolean isUserImage() {
        return isUserImage;
    }

    public boolean isCheckBox() {
        return isCheckBox;
    }

    public boolean isEditText() {
        return isEditText;
    }

    public String getItemName() {
        return itemName;
    }

    public void setItemName(String itemName) { this.itemName = itemName; }

    public String getHelpText() {
        return helpText;
    }

    public void setHelpText(String helpText) { this.helpText = helpText; }

}