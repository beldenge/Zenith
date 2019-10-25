package com.ciphertool.zenith.api.model;

public class CipherResponseItem {
    private String name;
    private int rows;
    private int columns;
    private String ciphertext;

    public CipherResponseItem() {
    }

    public CipherResponseItem(String name, int rows, int columns, String ciphertext) {
        this.name = name;
        this.rows = rows;
        this.columns = columns;
        this.ciphertext = ciphertext;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public int getRows() {
        return rows;
    }

    public void setRows(int rows) {
        this.rows = rows;
    }

    public int getColumns() {
        return columns;
    }

    public void setColumns(int columns) {
        this.columns = columns;
    }

    public String getCiphertext() {
        return ciphertext;
    }

    public void setCiphertext(String ciphertext) {
        this.ciphertext = ciphertext;
    }
}
