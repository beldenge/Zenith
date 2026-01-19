package com.ciphertool.zenith.inference.transformer.ciphertext;

import com.ciphertool.zenith.inference.entities.Cipher;
import lombok.NoArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.Map;

@NoArgsConstructor
@Component
public class ShiftCharactersLeftCipherTransformer extends AbstractRangeLimitedCipherTransformer {
    public ShiftCharactersLeftCipherTransformer(Map<String, Object> data) {
        super(data);
    }

    @Override
    public Cipher transform(Cipher cipher) {
        Cipher transformed = cipher.clone();
        int start = 0;
        int end = cipher.length();

        if (rangeStart != null) {
            start = Math.max(rangeStart, 0);
        }

        if (rangeEnd != null) {
            end = Math.min(rangeEnd, cipher.length());
        }

        int rangeLength = end - start;

        int k = start;
        for (int i = start; i <= end; i++) {
            k++;
            if (i == end) {
                k -= rangeLength - 1;
            }
            transformed.replaceCiphertextCharacter(i, cipher.getCiphertextCharacters().get(k).clone());
        }

        return transformed;
    }

    @Override
    public CipherTransformer getInstance(Map<String, Object> data) {
        return new ShiftCharactersLeftCipherTransformer(data);
    }

    @Override
    public int getOrder() {
        return 23;
    }

    @Override
    public String getHelpText() {
        return "Shifts characters in the range to the left by 1, wrapping as needed";
    }
}
