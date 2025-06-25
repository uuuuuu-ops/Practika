package org.example.service.enums;

public enum LinkType {
    Get_DOC("file/get-doc"),
    Get_PHOTO("file/get-photo");
    private final String link;

    LinkType(String link) {
        this.link = link;
    }
    @Override
    public String toString(){
        return link;
    }
}
