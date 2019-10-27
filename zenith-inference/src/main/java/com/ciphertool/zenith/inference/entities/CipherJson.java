package com.ciphertool.zenith.inference.entities;

import java.util.HashMap;
import java.util.Map;

public class CipherJson {
    private String name;

    private int columns;

    private int rows;

    private String ciphertext;

    private Map<String, String> knownSolutionKey = new HashMap<>();

    public CipherJson() {
    }

    public CipherJson(String name, int rows, int columns) {
        this.name = name;
        this.rows = rows;
        this.columns = columns;
    }

    public CipherJson(Cipher cipher) {
        this(cipher.getName(), cipher.getRows(), cipher.getColumns());

        StringBuilder sb = new StringBuilder();

        boolean first = true;
        for (Ciphertext ciphertext : cipher.getCiphertextCharacters()) {
            if (!first) {
                sb.append(" ");
            }

            first = false;

            sb.append(ciphertext.getValue());
        }

        ciphertext = sb.toString();

        if (cipher.hasKnownSolution()) {
            for (Map.Entry<String, String> entry : cipher.getKnownSolutionKey().entrySet()) {
                knownSolutionKey.put(entry.getKey(), entry.getValue());
            }
        }
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public int getColumns() {
        return columns;
    }

    public void setColumns(int columns) {
        this.columns = columns;
    }

    public int getRows() {
        return rows;
    }

    public void setRows(int rows) {
        this.rows = rows;
    }

    public String getCiphertext() {
        return ciphertext;
    }

    public void setCiphertext(String ciphertext) {
        this.ciphertext = ciphertext;
    }

    public Map<String, String> getKnownSolutionKey() {
        return knownSolutionKey;
    }

    public void setKnownSolutionKey(Map<String, String> knownSolutionKey) {
        this.knownSolutionKey = knownSolutionKey;
    }
}
