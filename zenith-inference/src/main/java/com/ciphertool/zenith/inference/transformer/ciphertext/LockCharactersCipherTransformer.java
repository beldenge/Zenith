package com.ciphertool.zenith.inference.transformer.ciphertext;

import com.ciphertool.zenith.inference.entities.Cipher;
import lombok.NoArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.Map;

@NoArgsConstructor
@Component
public class LockCharactersCipherTransformer extends AbstractRangeLimitedCipherTransformer {
    public LockCharactersCipherTransformer(Map<String, Object> data) {
        super(data);
    }

    @Override
    public Cipher transform(Cipher cipher) {
        int start = 0;
        int end = cipher.length();

        if (rangeStart != null) {
            start = Math.max(rangeStart, 0);
        }

        if (rangeEnd != null) {
            end = Math.min(rangeEnd, cipher.length());
        }

        for (int i = start; i <= end; i++) {
            cipher.getCiphertextCharacters().get(i).setLocked(true);
        }

        return cipher;
    }

    @Override
    public CipherTransformer getInstance(Map<String, Object> data) {
        return new LockCharactersCipherTransformer(data);
    }

    @Override
    public int getOrder() {
        return 25;
    }

    @Override
    public String getHelpText() {
        return "Locks a range of ciphertext characters such that they are skipped by further transformers";
    }
}
