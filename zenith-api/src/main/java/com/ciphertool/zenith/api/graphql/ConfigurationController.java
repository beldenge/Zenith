package com.ciphertool.zenith.api.graphql;

import com.ciphertool.zenith.inference.entities.config.ApplicationConfiguration;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.graphql.data.method.annotation.QueryMapping;
import org.springframework.stereotype.Controller;

@Controller
public class ConfigurationController {
    @Autowired
    private ApplicationConfiguration applicationConfiguration;

    @QueryMapping
    public ApplicationConfiguration configuration() {
        return applicationConfiguration;
    }
}
