package com.ciphertool.zenith.inference.transformer.ciphertext;

import com.ciphertool.zenith.inference.entities.Cipher;
import com.ciphertool.zenith.inference.entities.FormlyFieldProps;
import com.ciphertool.zenith.inference.entities.FormlyForm;
import com.ciphertool.zenith.inference.entities.FormlyFormField;
import lombok.NoArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

@NoArgsConstructor
@Component
public class NDownMAcrossCipherTransformer extends AbstractRangeLimitedCipherTransformer {
    public static final String DOWN = "down";
    public static final String ACROSS = "across";
    protected Integer down;
    protected Integer across;

    public NDownMAcrossCipherTransformer(Map<String, Object> data) {
        super(data);
        down = (Integer) data.get(DOWN);
        across = (Integer) data.get(ACROSS);
    }

    @Override
    public Cipher transform(Cipher cipher) {
        Cipher transformed = cipher.clone();

        int rowStart = 0;
        int rowEnd = cipher.getRows() - 1;

        if (rangeStart != null) {
            rowStart = Math.max(rangeStart, 0);
        }

        if (rangeEnd != null) {
            rowEnd = Math.min(rangeEnd, cipher.getRows() - 1);
        }

        if (rowStart > rowEnd) {
            return transformed;
        }

        int charStart = rowStart * cipher.getColumns();
        int charEnd = (rowEnd + 1) * cipher.getColumns(); // We want the end of the last row in the range

        int j = charStart;
        int k = charStart;
        List<Integer> toAppend = new ArrayList<>();

        for (int i = charStart; i < charEnd; i++) {
            if (!cipher.getCiphertextCharacters().get(k).isLocked()) {
                transformed.replaceCiphertextCharacter(j, cipher.getCiphertextCharacters().get(k).clone());
                j++;
            } else {
                toAppend.add(k);
            }

            k += offset(cipher);

            if (needsVerticalWrap(k, charEnd)) {
                k = (charStart + (k % cipher.getColumns())); // Cannot do a modulus since we're handling begin/end ranges
            } else if (needsHorizontalWrap(k, cipher)) {
                k -= cipher.getColumns();
            }
        }

        for (int i = 0; i < toAppend.size(); i++) {
            var index = charEnd - toAppend.size() + i;
            transformed.replaceCiphertextCharacter(index, cipher.getCiphertextCharacters().get(toAppend.get(i)).clone());
        }

        return transformed;
    }

    private int offset(Cipher cipher) {
        return (down * cipher.getColumns()) + across;
    }

    private boolean needsVerticalWrap(int k, int rangeEnd) {
        return k - across >= rangeEnd;
    }

    private boolean needsHorizontalWrap(int k, Cipher cipher) {
        return ((k - offset(cipher)) % cipher.getColumns()) > (k % cipher.getColumns());
    }

    @Override
    public CipherTransformer getInstance(Map<String, Object> data) {
        return new NDownMAcrossCipherTransformer(data);
    }

    @Override
    public int getOrder() {
        return 22;
    }

    @Override
    public String getHelpText() {
        return "Begins with the first symbol and goes n-down and m-across, wrapping both vertically and horizontally";
    }

    @Override
    public FormlyForm getForm() {
        FormlyForm form = super.getForm();
        AbstractRangeLimitedCipherTransformer.makeRowBased(form);

        FormlyFieldProps downProps = new FormlyFieldProps();
        downProps.setLabel("Down");
        downProps.setRequired(true);
        downProps.setType("number");
        downProps.setRequired(true);
        downProps.setMin(0.0);

        FormlyFormField down = new FormlyFormField();
        down.setKey(DOWN);
        down.setType("input");
        down.setProps(downProps);

        FormlyFieldProps acrossProps = new FormlyFieldProps();
        acrossProps.setLabel("Across");
        acrossProps.setRequired(true);
        acrossProps.setType("number");
        acrossProps.setRequired(true);
        acrossProps.setMin(0.0);

        FormlyFormField across = new FormlyFormField();
        across.setKey(ACROSS);
        across.setType("input");
        across.setProps(acrossProps);

        List<FormlyFormField> fields = new ArrayList<>(form.getFields());
        fields.add(down);
        fields.add(across);
        form.setFields(fields);

        return form;
    }
}
