package com.ciphertool.zenith.api.model;

public class CipherResponseItem {
    private String name;
    private int rows;
    private int columns;
    private String ciphertext;
    private double multiplicity;
    private double entropy;
    private double indexOfCoincidence;
    private double chiSquared;
    private int bigramRepeats;
    private int cycleScore;

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

    public double getMultiplicity() {
        return multiplicity;
    }

    public void setMultiplicity(double multiplicity) {
        this.multiplicity = multiplicity;
    }

    public double getEntropy() {
        return entropy;
    }

    public void setEntropy(double entropy) {
        this.entropy = entropy;
    }

    public double getIndexOfCoincidence() {
        return indexOfCoincidence;
    }

    public void setIndexOfCoincidence(double indexOfCoincidence) {
        this.indexOfCoincidence = indexOfCoincidence;
    }

    public double getChiSquared() {
        return chiSquared;
    }

    public void setChiSquared(double chiSquared) {
        this.chiSquared = chiSquared;
    }

    public int getBigramRepeats() {
        return bigramRepeats;
    }

    public void setBigramRepeats(int bigramRepeats) {
        this.bigramRepeats = bigramRepeats;
    }

    public int getCycleScore() {
        return cycleScore;
    }

    public void setCycleScore(int cycleScore) {
        this.cycleScore = cycleScore;
    }
}
