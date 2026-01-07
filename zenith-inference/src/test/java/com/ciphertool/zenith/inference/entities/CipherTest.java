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

package com.ciphertool.zenith.inference.entities;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

public class CipherTest {
    @Test
    public void testConstructor() {
        String nameToSet = "cipherName";
        int rowsToSet = 5;
        int columnsToSet = 10;
        Cipher cipher = new Cipher(nameToSet, rowsToSet, columnsToSet);

        assertSame(nameToSet, cipher.getName());
        assertEquals(rowsToSet, cipher.getRows());
        assertEquals(columnsToSet, cipher.getColumns());
    }

    @Test
    public void testSetName() {
        Cipher cipher = new Cipher();
        String nameToSet = "cipherName";
        cipher.setName(nameToSet);

        assertEquals(nameToSet, cipher.getName());
    }

    @Test
    public void testSetColumns() {
        Cipher cipher = new Cipher();
        int columnsToSet = 10;
        cipher.setColumns(columnsToSet);

        assertEquals(columnsToSet, cipher.getColumns());
    }

    @Test
    public void testSetRows() {
        Cipher cipher = new Cipher();
        int rowsToSet = 5;
        cipher.setRows(rowsToSet);

        assertEquals(rowsToSet, cipher.getRows());
    }

    @Test
    public void testHasKnownSolution() {
        Cipher cipher = new Cipher();
        cipher.putKnownSolutionMapping("a", "b");

        assertTrue(cipher.hasKnownSolution());
    }

    @Test
    public void testCiphertextCharactersUnmodifiable() {
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
    public void getNullCiphertextCharacters() {
        Cipher cipher = new Cipher();
        assertNotNull(cipher.getCiphertextCharacters());
    }

    @Test
    public void testAddCiphertextCharacter() {
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
    public void testRemoveCiphertextCharacter() {
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
    public void testLength() {
        Cipher cipher = new Cipher();
        assertEquals(0, cipher.length());

        cipher.setRows(2);
        cipher.setColumns(3);

        assertEquals(6, cipher.length());
    }

    @Test
    public void testEquals() {
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
}
