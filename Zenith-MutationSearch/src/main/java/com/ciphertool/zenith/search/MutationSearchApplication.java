package com.ciphertool.zenith.search;

import com.ciphertool.zenith.inference.dao.CipherDao;
import com.ciphertool.zenith.inference.transformer.TranspositionCipherTransformer;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication(scanBasePackageClasses = { MutationSearchApplication.class, CipherDao.class, TranspositionCipherTransformer.class })
public class MutationSearchApplication implements CommandLineRunner {
    @Autowired
    private TranspositionSearcher searcher;

    public static void main(String[] args) {
        SpringApplication.run(MutationSearchApplication.class, args).close();
    }

    @Override
    public void run(String... arg0) {
		searcher.run();
    }
}
