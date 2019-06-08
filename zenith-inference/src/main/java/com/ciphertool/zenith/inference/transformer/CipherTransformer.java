package com.ciphertool.zenith.inference.transformer;

import com.ciphertool.zenith.inference.entities.Cipher;

import java.util.List;

public interface CipherTransformer {
    Cipher transform(Cipher cipher);

    Cipher transform(Cipher cipher, List<Integer> columnIndices);
}
