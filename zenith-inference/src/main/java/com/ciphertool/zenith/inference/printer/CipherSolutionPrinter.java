package com.ciphertool.zenith.inference.printer;

import com.ciphertool.zenith.inference.entities.Cipher;
import com.ciphertool.zenith.inference.entities.CipherSolution;
import com.ciphertool.zenith.inference.transformer.plaintext.PlaintextTransformer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
public class CipherSolutionPrinter {
    private Logger log = LoggerFactory.getLogger(getClass());

    @Autowired
    @Qualifier("activePlaintextTransformers")
    private List<PlaintextTransformer> plaintextTransformers;

    public void print(CipherSolution solution) {
        String plaintext = solution.asSingleLineString();

        for (PlaintextTransformer plaintextTransformer : plaintextTransformers) {
            plaintext = plaintextTransformer.transform(plaintext);
        }

        Cipher cipher = solution.getCipher();
        StringBuilder sb = new StringBuilder();
        sb.append("Solution [probability=" + solution.getProbability() + ", logProbability=" + solution.getLogProbability() + ", score=" + solution.getScore()
                + ", indexOfCoincidence=" + solution.getIndexOfCoincidence() + ", chiSquared=" + solution.getChiSquared() + (cipher.hasKnownSolution() ? ", proximity="
                + String.format("%1$,.2f", solution.evaluateKnownSolution() * 100.0) + "%" : "") + "]\n");

        for (int i = 0; i < plaintext.length(); i++) {
            sb.append(" ");
            sb.append(plaintext.charAt(i));
            sb.append(" ");

            /*
             * Print a newline if we are at the end of the row. Add 1 to the index so the modulus function doesn't
             * break.
             */
            if (((i + 1) % cipher.getColumns()) == 0) {
                sb.append("\n");
            } else {
                sb.append(" ");
            }
        }

       log.info(sb.toString());
    }
}
