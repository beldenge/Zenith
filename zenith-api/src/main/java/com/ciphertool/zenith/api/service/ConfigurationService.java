package com.ciphertool.zenith.api.service;

import com.ciphertool.zenith.inference.entities.Cipher;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.RestController;

import java.util.Collections;
import java.util.List;

@RestController
@RequestMapping(value = "/api/configurations", consumes = MediaType.APPLICATION_JSON_VALUE, produces = MediaType.APPLICATION_JSON_VALUE)
public class ConfigurationService {
    @GetMapping
    @ResponseBody
    public List<Cipher> readConfiguration() {
        return Collections.singletonList(new Cipher());
    }
}
