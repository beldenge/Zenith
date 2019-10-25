package com.ciphertool.zenith.api.model;

import java.util.ArrayList;
import java.util.List;

public class CipherResponse {
    private List<CipherResponseItem> ciphers = new ArrayList<>();

    public List<CipherResponseItem> getCiphers() {
        return ciphers;
    }

    public void setCiphers(List<CipherResponseItem> ciphers) {
        this.ciphers = ciphers;
    }
}
