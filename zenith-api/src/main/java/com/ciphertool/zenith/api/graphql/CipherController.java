package com.ciphertool.zenith.api.graphql;

import com.ciphertool.zenith.inference.dao.CipherDao;
import com.ciphertool.zenith.inference.entities.Cipher;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.graphql.data.method.annotation.QueryMapping;
import org.springframework.graphql.data.method.annotation.SchemaMapping;
import org.springframework.stereotype.Controller;

import java.util.List;
import java.util.Map;

@Controller
public class CipherController {
    @Autowired
    private CipherDao cipherDao;

    @QueryMapping
    public List<Cipher> ciphers() {
        return cipherDao.findAll();
    }

    @SchemaMapping
    public String name(Cipher cipher) {
        return cipher.getName();
    }

    @SchemaMapping
    public int rows(Cipher cipher) {
        return cipher.getRows();
    }

    @SchemaMapping
    public int columns(Cipher cipher) {
        return cipher.getColumns();
    }

    @SchemaMapping
    public boolean readOnly(Cipher cipher) {
        return cipher.isReadOnly();
    }

    @SchemaMapping
    public List<String> ciphertext(Cipher cipher) {
        return cipher.getCiphertext();
    }

    @SchemaMapping
    public Map<String, String> knownSolutionKey(Cipher cipher) {
        return cipher.getKnownSolutionKey();
    }
}
