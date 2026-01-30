/*
 * Copyright 2017-2026 George Belden
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

package com.ciphertool.zenith.inference.entities;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import java.util.Arrays;
import java.util.Map;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

public class CipherTest {
    @Test
    public void given_validInput_when_constructing_then_returnsSameInstance() {
        String nameToSet = "cipherName";
        int rowsToSet = 5;
        int columnsToSet = 10;
        Cipher cipher = new Cipher(nameToSet, rowsToSet, columnsToSet);

        assertSame(nameToSet, cipher.getName());
        assertEquals(rowsToSet, cipher.getRows());
        assertEquals(columnsToSet, cipher.getColumns());
    }

    @Test
    public void given_validInput_when_settingName_then_returnsExpectedValue() {
        Cipher cipher = new Cipher();
        String nameToSet = "cipherName";
        cipher.setName(nameToSet);

        assertEquals(nameToSet, cipher.getName());
    }

    @Test
    public void given_validInput_when_settingColumns_then_returnsExpectedValue() {
        Cipher cipher = new Cipher();
        int columnsToSet = 10;
        cipher.setColumns(columnsToSet);

        assertEquals(columnsToSet, cipher.getColumns());
    }

    @Test
    public void given_validInput_when_settingRows_then_returnsExpectedValue() {
        Cipher cipher = new Cipher();
        int rowsToSet = 5;
        cipher.setRows(rowsToSet);

        assertEquals(rowsToSet, cipher.getRows());
    }

    @Test
    public void given_knownSolution_when_checkingKnownSolution_then_returnsTrue() {
        Cipher cipher = new Cipher();
        cipher.putKnownSolutionMapping("a", "b");

        assertTrue(cipher.hasKnownSolution());
    }

    @Test
    public void given_validInput_when_ciphertextCharactersUnmodifiable_then_throwsUnsupportedOperationException() {
        Cipher cipher = new Cipher();
        cipher.addCiphertextCharacter(new Ciphertext("a"));
        cipher.addCiphertextCharacter(new Ciphertext("b"));
        cipher.addCiphertextCharacter(new Ciphertext("c"));

        List<Ciphertext> ciphertextCharacters = cipher.getCiphertextCharacters();

        Assertions.assertThrows(UnsupportedOperationException.class, () -> {
            ciphertextCharacters.remove(0); // should throw exception
        });
    }

    @Test
    public void given_nullInput_when_gettingNullCiphertextCharacters_then_returnsNotNull() {
        Cipher cipher = new Cipher();
        assertNotNull(cipher.getCiphertextCharacters());
    }

    @Test
    public void given_validInput_when_addingCiphertextCharacter_then_returnsSameInstance() {
        Cipher cipher = new Cipher();
        assertEquals(0, cipher.getCiphertextCharacters().size());

        Ciphertext ciphertext1 = new Ciphertext("a");
        cipher.addCiphertextCharacter(ciphertext1);
        Ciphertext ciphertext2 = new Ciphertext("b");
        cipher.addCiphertextCharacter(ciphertext2);
        Ciphertext ciphertext3 = new Ciphertext("c");
        cipher.addCiphertextCharacter(ciphertext3);

        assertEquals(3, cipher.getCiphertextCharacters().size());
        assertSame(ciphertext1, cipher.getCiphertextCharacters().get(0));
        assertSame(ciphertext2, cipher.getCiphertextCharacters().get(1));
        assertSame(ciphertext3, cipher.getCiphertextCharacters().get(2));
    }

    @Test
    public void given_validInput_when_removingCiphertextCharacter_then_returnsSameInstance() {
        Cipher cipher = new Cipher();

        Ciphertext ciphertext1 = new Ciphertext("a");
        cipher.addCiphertextCharacter(ciphertext1);
        Ciphertext ciphertext2 = new Ciphertext("b");
        cipher.addCiphertextCharacter(ciphertext2);
        Ciphertext ciphertext3 = new Ciphertext("c");
        cipher.addCiphertextCharacter(ciphertext3);

        assertEquals(3, cipher.getCiphertextCharacters().size());

        cipher.removeCiphertextCharacter(1);

        assertEquals(2, cipher.getCiphertextCharacters().size());
        assertSame(ciphertext1, cipher.getCiphertextCharacters().get(0));
        assertSame(ciphertext3, cipher.getCiphertextCharacters().get(1));
    }

    @Test
    public void given_validInput_when_calculatingLength_then_returnsExpectedValue() {
        Cipher cipher = new Cipher();
        assertEquals(0, cipher.length());

        cipher.setRows(2);
        cipher.setColumns(3);

        assertEquals(6, cipher.length());
    }

    @Test
    public void given_validInput_when_equals_then_comparesAsExpected() {
        String baseName = "baseName";
        int baseRows = 10;
        int baseColumns = 5;
        Ciphertext ciphertext1 = new Ciphertext("a");
        Ciphertext ciphertext2 = new Ciphertext("b");
        Ciphertext ciphertext3 = new Ciphertext("c");

        Cipher base = new Cipher(baseName, baseRows, baseColumns);
        base.addCiphertextCharacter(ciphertext1);
        base.addCiphertextCharacter(ciphertext2);
        base.addCiphertextCharacter(ciphertext3);

        Cipher cipherEqualToBase = new Cipher(baseName, baseRows, baseColumns);
        cipherEqualToBase.addCiphertextCharacter(ciphertext1);
        cipherEqualToBase.addCiphertextCharacter(ciphertext2);
        cipherEqualToBase.addCiphertextCharacter(ciphertext3);
        assertEquals(base, cipherEqualToBase);

        Cipher cipherWithDifferentName = new Cipher("differentName", baseRows, baseColumns);
        cipherWithDifferentName.addCiphertextCharacter(ciphertext1);
        cipherWithDifferentName.addCiphertextCharacter(ciphertext2);
        cipherWithDifferentName.addCiphertextCharacter(ciphertext3);
        assertFalse(base.equals(cipherWithDifferentName));

        Cipher cipherWithDifferentRows = new Cipher(baseName, 9, baseColumns);
        cipherWithDifferentRows.addCiphertextCharacter(ciphertext1);
        cipherWithDifferentRows.addCiphertextCharacter(ciphertext2);
        cipherWithDifferentRows.addCiphertextCharacter(ciphertext3);
        assertFalse(base.equals(cipherWithDifferentRows));

        Cipher cipherWithDifferentColumns = new Cipher(baseName, baseRows, 4);
        cipherWithDifferentColumns.addCiphertextCharacter(ciphertext1);
        cipherWithDifferentColumns.addCiphertextCharacter(ciphertext2);
        cipherWithDifferentColumns.addCiphertextCharacter(ciphertext3);
        assertFalse(base.equals(cipherWithDifferentColumns));

        Cipher cipherWithDifferentCiphertextCharacters = new Cipher(baseName, baseRows, baseColumns);
        cipherWithDifferentCiphertextCharacters.addCiphertextCharacter(ciphertext3);
        cipherWithDifferentCiphertextCharacters.addCiphertextCharacter(ciphertext2);
        cipherWithDifferentCiphertextCharacters.addCiphertextCharacter(ciphertext1);
        assertFalse(base.equals(cipherWithDifferentColumns));

        Cipher cipherWithNullPropertiesA = new Cipher();
        Cipher cipherWithNullPropertiesB = new Cipher();
        assertEquals(cipherWithNullPropertiesA, cipherWithNullPropertiesB);
    }

    @Test
    public void given_validInput_when_gettingCipherSymbolIndicesMap_then_matchesExpectations() {
        Cipher cipher = new Cipher("test", 1, 5);
        cipher.setCiphertext(Arrays.asList("A", "B", "A", "C", "B"));

        Map<String, int[]> indices = cipher.getCipherSymbolIndicesMap();

        assertArrayEquals(new int[] { 0, 2 }, indices.get("A"));
        assertArrayEquals(new int[] { 1, 4 }, indices.get("B"));
        assertArrayEquals(new int[] { 3 }, indices.get("C"));
    }

    @Test
    public void given_validInput_when_replacingCiphertextCharacterClearsAndRebuildsIndices_then_clearsState() {
        Cipher cipher = new Cipher("test", 1, 5);
        cipher.setCiphertext(Arrays.asList("A", "B", "A", "C", "B"));

        Map<String, int[]> original = cipher.getCipherSymbolIndicesMap();
        assertArrayEquals(new int[] { 1, 4 }, original.get("B"));

        cipher.replaceCiphertextCharacter(1, new Ciphertext("A"));

        Map<String, int[]> updated = cipher.getCipherSymbolIndicesMap();
        assertArrayEquals(new int[] { 0, 1, 2 }, updated.get("A"));
        assertArrayEquals(new int[] { 4 }, updated.get("B"));
        assertArrayEquals(new int[] { 3 }, updated.get("C"));
    }

    @Test
    public void given_validInput_when_cloningCopiesState_then_copiesState() {
        Cipher cipher = new Cipher("clone", 2, 2, true);
        cipher.addCiphertextCharacter(new Ciphertext("X", true));
        cipher.addCiphertextCharacter(new Ciphertext("Y", false));
        cipher.putKnownSolutionMapping("a", "b");

        Cipher cloned = cipher.clone();

        assertEquals(cipher.getName(), cloned.getName());
        assertEquals(cipher.getRows(), cloned.getRows());
        assertEquals(cipher.getColumns(), cloned.getColumns());
        assertEquals(cipher.isReadOnly(), cloned.isReadOnly());
        assertEquals(cipher.getKnownSolutionKey(), cloned.getKnownSolutionKey());

        assertEquals(cipher.getCiphertextCharacters().size(), cloned.getCiphertextCharacters().size());
        assertNotSame(cipher.getCiphertextCharacters().get(0), cloned.getCiphertextCharacters().get(0));
        assertEquals(cipher.getCiphertextCharacters().get(0).getValue(), cloned.getCiphertextCharacters().get(0).getValue());
    }

    @Test
    public void given_validInput_when_asSingleLineString_then_returnsExpectedValue() {
        Cipher cipher = new Cipher("test", 1, 3);
        cipher.addCiphertextCharacter(new Ciphertext("A"));
        cipher.addCiphertextCharacter(new Ciphertext("B"));
        cipher.addCiphertextCharacter(new Ciphertext("C"));

        assertEquals("A B C", cipher.asSingleLineString());
    }
}
