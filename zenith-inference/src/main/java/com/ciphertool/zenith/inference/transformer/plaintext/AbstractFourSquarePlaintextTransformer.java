/*
 * Copyright 2017-2020 George Belden
 *
 * This file is part of Zenith.
 *
 * Zenith is free software: you can redistribute it and/or modify it under
 * the terms of the GNU General Public License as published by the Free Software
 * Foundation, either version 3 of the License, or (at your option) any later
 * version.
 *
 * Zenith is distributed in the hope that it will be useful, but WITHOUT
 * ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS
 * FOR A PARTICULAR PURPOSE. See the GNU General Public License for more
 * details.
 *
 * You should have received a copy of the GNU General Public License along with
 * Zenith. If not, see <http://www.gnu.org/licenses/>.
 */

package com.ciphertool.zenith.inference.transformer.plaintext;

import com.ciphertool.zenith.inference.entities.FormlyForm;
import com.ciphertool.zenith.inference.entities.FormlyFormField;
import com.ciphertool.zenith.inference.entities.FormlyTemplateOptions;
import lombok.AllArgsConstructor;
import lombok.NoArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@NoArgsConstructor
public abstract class AbstractFourSquarePlaintextTransformer implements PlaintextTransformer {
    protected Logger log = LoggerFactory.getLogger(getClass());

    protected static final int KEY_LENGTH = 25;
    protected static final int SQUARE_SIZE = 5;
    public static final String KEY_TOP_LEFT = "keyTopLeft";
    public static final String KEY_TOP_RIGHT = "keyTopRight";
    public static final String KEY_BOTTOM_LEFT = "keyBottomLeft";
    public static final String KEY_BOTTOM_RIGHT = "keyBottomRight";

    protected String keyTopLeft;

    protected String keyTopRight;

    protected String keyBottomLeft;

    protected String keyBottomRight;

    protected Map<Character, FourSquarePlaintextTransformer.Coordinates> keyTopLeftMap;
    protected Map<Character, FourSquarePlaintextTransformer.Coordinates> keyTopRightMap;
    protected Map<Character, FourSquarePlaintextTransformer.Coordinates> keyBottomLeftMap;
    protected Map<Character, FourSquarePlaintextTransformer.Coordinates> keyBottomRightMap;

    public void init() {
        if (keyTopLeft.length() != KEY_LENGTH) {
            throw new IllegalArgumentException("Key specified by four-square-transformer.key.top-left must be of length " + KEY_LENGTH + ".");
        }

        if (keyTopRight.length() != KEY_LENGTH) {
            throw new IllegalArgumentException("Key specified by four-square-transformer.key.top-right must be of length " + KEY_LENGTH + ".");
        }

        if (keyBottomLeft.length() != KEY_LENGTH) {
            throw new IllegalArgumentException("Key specified by four-square-transformer.key.bottom-left must be of length " + KEY_LENGTH + ".");
        }

        if (keyBottomRight.length() != KEY_LENGTH) {
            throw new IllegalArgumentException("Key specified by four-square-transformer.key.bottom-right must be of length " + KEY_LENGTH + ".");
        }

        keyTopLeftMap = getKeyMapFromKeyString(keyTopLeft);
        keyTopRightMap = getKeyMapFromKeyString(keyTopRight);
        keyBottomLeftMap = getKeyMapFromKeyString(keyBottomLeft);
        keyBottomRightMap = getKeyMapFromKeyString(keyBottomRight);
    }

    public AbstractFourSquarePlaintextTransformer(Map<String, Object> data) {
        keyTopLeft = (String) data.get(KEY_TOP_LEFT);
        keyTopRight = (String) data.get(KEY_TOP_RIGHT);
        keyBottomLeft = (String) data.get(KEY_BOTTOM_LEFT);
        keyBottomRight = (String) data.get(KEY_BOTTOM_RIGHT);
        init();
    }

    @Override
    public FormlyForm getForm() {
        FormlyForm form = new FormlyForm();

        List<FormlyFormField> fields = new ArrayList<>(4);

        FormlyTemplateOptions keyTopLeftOptions = new FormlyTemplateOptions();
        keyTopLeftOptions.setLabel("Top Left Key");
        keyTopLeftOptions.setRequired(true);
        keyTopLeftOptions.setCols(5);
        keyTopLeftOptions.setRows(5);
        keyTopLeftOptions.setMinLength(25);
        keyTopLeftOptions.setMaxLength(25);
        keyTopLeftOptions.setPattern("[A-Za-z]+");

        FormlyFormField keyTopLeft = new FormlyFormField();
        keyTopLeft.setKey(KEY_TOP_LEFT);
        keyTopLeft.setType("textarea");
        keyTopLeft.setTemplateOptions(keyTopLeftOptions);
        keyTopLeft.setDefaultValue("abcdefghiklmnopqrstuvwxyz");

        fields.add(keyTopLeft);

        FormlyTemplateOptions keyTopRightOptions = new FormlyTemplateOptions();
        keyTopRightOptions.setLabel("Top Right Key");
        keyTopRightOptions.setRequired(true);
        keyTopRightOptions.setCols(5);
        keyTopRightOptions.setRows(5);
        keyTopRightOptions.setMinLength(25);
        keyTopRightOptions.setMaxLength(25);
        keyTopRightOptions.setPattern("[A-Za-z]+");

        FormlyFormField keyTopRight = new FormlyFormField();
        keyTopRight.setKey(KEY_TOP_RIGHT);
        keyTopRight.setType("textarea");
        keyTopRight.setTemplateOptions(keyTopRightOptions);

        fields.add(keyTopRight);

        FormlyTemplateOptions keyBottomLeftOptions = new FormlyTemplateOptions();
        keyBottomLeftOptions.setLabel("Bottom Left Key");
        keyBottomLeftOptions.setRequired(true);
        keyBottomLeftOptions.setCols(5);
        keyBottomLeftOptions.setRows(5);
        keyBottomLeftOptions.setMinLength(25);
        keyBottomLeftOptions.setMaxLength(25);
        keyBottomLeftOptions.setPattern("[A-Za-z]+");

        FormlyFormField keyBottomLeft = new FormlyFormField();
        keyBottomLeft.setKey(KEY_BOTTOM_LEFT);
        keyBottomLeft.setType("textarea");
        keyBottomLeft.setTemplateOptions(keyBottomLeftOptions);

        fields.add(keyBottomLeft);

        FormlyTemplateOptions keyBottomRightOptions = new FormlyTemplateOptions();
        keyBottomRightOptions.setLabel("Bottom Right Key");
        keyBottomRightOptions.setRequired(true);
        keyBottomRightOptions.setCols(5);
        keyBottomRightOptions.setRows(5);
        keyBottomRightOptions.setMinLength(25);
        keyBottomRightOptions.setMaxLength(25);
        keyBottomRightOptions.setPattern("[A-Za-z]+");

        FormlyFormField keyBottomRight = new FormlyFormField();
        keyBottomRight.setKey(KEY_BOTTOM_RIGHT);
        keyBottomRight.setType("textarea");
        keyBottomRight.setTemplateOptions(keyBottomRightOptions);
        keyBottomRight.setDefaultValue("abcdefghiklmnopqrstuvwxyz");

        fields.add(keyBottomRight);

        form.setFields(fields);

        return form;
    }

    @AllArgsConstructor
    protected class Coordinates {
        protected int row;
        protected int column;
    }

    protected Map<Character, Coordinates> getKeyMapFromKeyString(String key) {
        key = key.toLowerCase();

        Map<Character, Coordinates> keyMap = new HashMap<>();

        for (int i = 0; i < SQUARE_SIZE; i++) {
            for (int j = 0; j < SQUARE_SIZE; j ++) {
                keyMap.put(key.charAt((i * SQUARE_SIZE) + j), new Coordinates(i, j));
            }
        }

        return keyMap;
    }

    protected Character getCharacterAtCoordinates(String key, int row, int column) {
        return key.charAt((row * SQUARE_SIZE) + column);
    }

    protected Character ifJThenI(Character c) {
        return c == 'j' ? 'i' : c;
    }
}
