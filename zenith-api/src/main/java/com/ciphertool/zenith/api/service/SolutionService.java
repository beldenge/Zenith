package com.ciphertool.zenith.api.service;

import com.ciphertool.zenith.inference.entities.CipherSolution;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.*;

import java.util.Collections;
import java.util.List;

@RestController
@RequestMapping(value = "/api/solutions", consumes = MediaType.APPLICATION_JSON_VALUE, produces = MediaType.APPLICATION_JSON_VALUE)
public class SolutionService {
    @PostMapping
    @ResponseBody
    public List<CipherSolution> solve() {
        return Collections.emptyList();
    }
}
