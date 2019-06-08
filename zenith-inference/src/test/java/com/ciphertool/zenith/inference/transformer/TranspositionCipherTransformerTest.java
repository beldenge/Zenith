package com.ciphertool.zenith.inference.transformer;

import com.ciphertool.zenith.inference.entities.Cipher;
import com.ciphertool.zenith.inference.entities.Ciphertext;
import org.junit.Test;

public class TranspositionCipherTransformerTest {
    @Test
    public void testTransposeOnce() {
        TranspositionCipherTransformer transpositionCipherTransformer = new TranspositionCipherTransformer();
        transpositionCipherTransformer.transpositionIterations = 1;
        transpositionCipherTransformer.transpositionKeyString = "TOMATO";

        Cipher cipher = new Cipher("tomato", 7, 6);

        cipher.addCiphertextCharacter(new Ciphertext(0, "T"));
        cipher.addCiphertextCharacter(new Ciphertext(1, "I"));
        cipher.addCiphertextCharacter(new Ciphertext(2, "N"));
        cipher.addCiphertextCharacter(new Ciphertext(3, "E"));
        cipher.addCiphertextCharacter(new Ciphertext(4, "S"));
        cipher.addCiphertextCharacter(new Ciphertext(5, "A"));
        cipher.addCiphertextCharacter(new Ciphertext(6, "X"));
        cipher.addCiphertextCharacter(new Ciphertext(7, "E"));
        cipher.addCiphertextCharacter(new Ciphertext(8, "O"));
        cipher.addCiphertextCharacter(new Ciphertext(9, "A"));
        cipher.addCiphertextCharacter(new Ciphertext(10, "H"));
        cipher.addCiphertextCharacter(new Ciphertext(11, "T"));
        cipher.addCiphertextCharacter(new Ciphertext(12, "F"));
        cipher.addCiphertextCharacter(new Ciphertext(13, "X"));
        cipher.addCiphertextCharacter(new Ciphertext(14, "H"));
        cipher.addCiphertextCharacter(new Ciphertext(15, "T"));
        cipher.addCiphertextCharacter(new Ciphertext(16, "L"));
        cipher.addCiphertextCharacter(new Ciphertext(17, "T"));
        cipher.addCiphertextCharacter(new Ciphertext(18, "H"));
        cipher.addCiphertextCharacter(new Ciphertext(19, "E"));
        cipher.addCiphertextCharacter(new Ciphertext(20, "Y"));
        cipher.addCiphertextCharacter(new Ciphertext(21, "M"));
        cipher.addCiphertextCharacter(new Ciphertext(22, "A"));
        cipher.addCiphertextCharacter(new Ciphertext(23, "I"));
        cipher.addCiphertextCharacter(new Ciphertext(24, "I"));
        cipher.addCiphertextCharacter(new Ciphertext(25, "A"));
        cipher.addCiphertextCharacter(new Ciphertext(26, "I"));
        cipher.addCiphertextCharacter(new Ciphertext(27, "X"));
        cipher.addCiphertextCharacter(new Ciphertext(28, "T"));
        cipher.addCiphertextCharacter(new Ciphertext(29, "A"));
        cipher.addCiphertextCharacter(new Ciphertext(30, "P"));
        cipher.addCiphertextCharacter(new Ciphertext(31, "N"));
        cipher.addCiphertextCharacter(new Ciphertext(32, "G"));
        cipher.addCiphertextCharacter(new Ciphertext(33, "D"));
        cipher.addCiphertextCharacter(new Ciphertext(34, "L"));
        cipher.addCiphertextCharacter(new Ciphertext(35, "O"));
        cipher.addCiphertextCharacter(new Ciphertext(36, "S"));
        cipher.addCiphertextCharacter(new Ciphertext(37, "T"));
        cipher.addCiphertextCharacter(new Ciphertext(38, "N"));
        cipher.addCiphertextCharacter(new Ciphertext(39, "H"));
        cipher.addCiphertextCharacter(new Ciphertext(40, "M"));
        cipher.addCiphertextCharacter(new Ciphertext(41, "X"));

        Cipher transformed = transpositionCipherTransformer.transform(cipher);

        System.out.println(transformed);
    }

    @Test
    public void testTransposeTwice() {
        TranspositionCipherTransformer transpositionCipherTransformer = new TranspositionCipherTransformer();
        transpositionCipherTransformer.transpositionIterations = 2;
        transpositionCipherTransformer.transpositionKeyString = "TOMATO";

        Cipher cipher = new Cipher("tomato", 7, 6);

        cipher.addCiphertextCharacter(new Ciphertext(0, "E"));
        cipher.addCiphertextCharacter(new Ciphertext(1, "A"));
        cipher.addCiphertextCharacter(new Ciphertext(2, "T"));
        cipher.addCiphertextCharacter(new Ciphertext(3, "M"));
        cipher.addCiphertextCharacter(new Ciphertext(4, "X"));
        cipher.addCiphertextCharacter(new Ciphertext(5, "D"));
        cipher.addCiphertextCharacter(new Ciphertext(6, "H"));
        cipher.addCiphertextCharacter(new Ciphertext(7, "N"));
        cipher.addCiphertextCharacter(new Ciphertext(8, "O"));
        cipher.addCiphertextCharacter(new Ciphertext(9, "H"));
        cipher.addCiphertextCharacter(new Ciphertext(10, "Y"));
        cipher.addCiphertextCharacter(new Ciphertext(11, "I"));
        cipher.addCiphertextCharacter(new Ciphertext(12, "G"));
        cipher.addCiphertextCharacter(new Ciphertext(13, "N"));
        cipher.addCiphertextCharacter(new Ciphertext(14, "I"));
        cipher.addCiphertextCharacter(new Ciphertext(15, "E"));
        cipher.addCiphertextCharacter(new Ciphertext(16, "X"));
        cipher.addCiphertextCharacter(new Ciphertext(17, "E"));
        cipher.addCiphertextCharacter(new Ciphertext(18, "A"));
        cipher.addCiphertextCharacter(new Ciphertext(19, "N"));
        cipher.addCiphertextCharacter(new Ciphertext(20, "T"));
        cipher.addCiphertextCharacter(new Ciphertext(21, "A"));
        cipher.addCiphertextCharacter(new Ciphertext(22, "T"));
        cipher.addCiphertextCharacter(new Ciphertext(23, "T"));
        cipher.addCiphertextCharacter(new Ciphertext(24, "I"));
        cipher.addCiphertextCharacter(new Ciphertext(25, "A"));
        cipher.addCiphertextCharacter(new Ciphertext(26, "O"));
        cipher.addCiphertextCharacter(new Ciphertext(27, "X"));
        cipher.addCiphertextCharacter(new Ciphertext(28, "T"));
        cipher.addCiphertextCharacter(new Ciphertext(29, "X"));
        cipher.addCiphertextCharacter(new Ciphertext(30, "F"));
        cipher.addCiphertextCharacter(new Ciphertext(31, "H"));
        cipher.addCiphertextCharacter(new Ciphertext(32, "I"));
        cipher.addCiphertextCharacter(new Ciphertext(33, "P"));
        cipher.addCiphertextCharacter(new Ciphertext(34, "S"));
        cipher.addCiphertextCharacter(new Ciphertext(35, "S"));
        cipher.addCiphertextCharacter(new Ciphertext(36, "H"));
        cipher.addCiphertextCharacter(new Ciphertext(37, "L"));
        cipher.addCiphertextCharacter(new Ciphertext(38, "A"));
        cipher.addCiphertextCharacter(new Ciphertext(39, "T"));
        cipher.addCiphertextCharacter(new Ciphertext(40, "L"));
        cipher.addCiphertextCharacter(new Ciphertext(41, "M"));

        Cipher transformed = transpositionCipherTransformer.transform(cipher);

        System.out.println(transformed);
    }
}
