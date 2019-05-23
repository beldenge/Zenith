package com.ciphertool.zenith.inference.transformer;

import com.ciphertool.zenith.inference.entities.Cipher;

public interface CipherTransformer {
    Cipher transform(Cipher cipher);
}
