package com.ciphertool.zenith.inference.transformer.ciphertext;

import com.ciphertool.zenith.inference.entities.Cipher;
import lombok.NoArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.Map;

@NoArgsConstructor
@Component
public class ShiftCharactersRightCipherTransformer extends AbstractRangeLimitedCipherTransformer {
    public ShiftCharactersRightCipherTransformer(Map<String, Object> data) {
        super(data);
    }

    @Override
    public Cipher transform(Cipher cipher) {
        Cipher transformed = cipher.clone();
        int start = 0;
        int end = cipher.length() - 1;

        if (rangeStart != null) {
            start = Math.max(rangeStart, 0);
        }

        if (rangeEnd != null) {
            end = Math.min(rangeEnd, cipher.length() - 1);
        }

        if (start > end) {
            return transformed;
        }

        int rangeLength = end - start + 1;

        int k = end;
        for (int i = end; i >= start; i--) {
            k--;
            if (i == start) {
                k += rangeLength;
            }
            transformed.replaceCiphertextCharacter(i, cipher.getCiphertextCharacters().get(k).clone());
        }

        return transformed;
    }

    @Override
    public CipherTransformer getInstance(Map<String, Object> data) {
        return new ShiftCharactersRightCipherTransformer(data);
    }

    @Override
    public int getOrder() {
        return 24;
    }

    @Override
    public String getHelpText() {
        return "Shifts characters in the range to the right by 1, wrapping as needed";
    }
}
