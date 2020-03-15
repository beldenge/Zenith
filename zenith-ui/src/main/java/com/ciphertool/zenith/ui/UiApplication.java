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

package com.ciphertool.zenith.ui;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.annotation.Bean;
import org.springframework.context.event.EventListener;
import org.springframework.web.servlet.config.annotation.CorsRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

import java.awt.*;
import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.List;

@SpringBootApplication(scanBasePackages = {
        "com.ciphertool.zenith.api",
        "com.ciphertool.zenith.ui"
})
public class UiApplication {
    private static Logger log = LoggerFactory.getLogger(UiApplication.class);

    public static void main(String[] args) {
        SpringApplication.run(UiApplication.class, args);
    }

    @Bean
    public WebMvcConfigurer corsConfigurer() {
        return new WebMvcConfigurer() {
            @Override
            public void addCorsMappings(CorsRegistry registry) {
                registry.addMapping("/**").allowedMethods("GET");
            }
        };
    }

    @EventListener({ ApplicationReadyEvent.class })
    public void applicationReadyEvent() {
        try {
            browse(new URI("http://localhost:8080"));
        } catch (URISyntaxException e) {
            log.error("Unable to open browser automatically.", e);
        }
    }

    /**
     * This method of browsing cross-platform is derived from the following:
     * https://stackoverflow.com/questions/18004150/desktop-api-is-not-supported-on-the-current-platform?noredirect=1&lq=1
     */
    public static boolean browse(URI uri) {
        if (openSystemSpecific(uri.toString())) {
            return true;
        }

        return browseDesktop(uri);
    }

    private static boolean openSystemSpecific(String what) {
        OperatingSystem os = getOs();

        if (os.isLinux()) {
            if (runCommand("kde-open", "%s", what)) {
                return true;
            }

            if (runCommand("gnome-open", "%s", what)) {
                return true;
            }

            if (runCommand("xdg-open", "%s", what)) {
                return true;
            }
        }

        if (os.isMac() && runCommand("open", "%s", what)) {
            return true;
        }

        return os.isWindows() && runCommand("explorer", "%s", what);
    }

    private static boolean browseDesktop(URI uri) {
        log.info("Trying to use Desktop.getDesktop().browse() with {}", uri.toString());

        try {
            if (!Desktop.isDesktopSupported()) {
                log.error("Platform is not supported.");
                return false;
            }

            if (!Desktop.getDesktop().isSupported(Desktop.Action.BROWSE)) {
                log.error("BROWSE is not supported.");
                return false;
            }

            Desktop.getDesktop().browse(uri);

            return true;
        } catch (Throwable t) {
            log.error("Error using desktop browse.", t);
            return false;
        }
    }

    private static boolean runCommand(String command, String args, String file) {
        String[] parts = prepareCommand(command, args, file);

        try {
            Process p = Runtime.getRuntime().exec(parts);

            if (p == null) {
                return false;
            }

            try {
                int retval = p.exitValue();

                if (retval == 0) {
                    log.error("Process ended immediately.");
                    return false;
                }

                log.error("Process crashed.");
                return false;
            } catch (IllegalThreadStateException itse) {
                log.debug("Process is running.");
                return true;
            }
        } catch (IOException e) {
            log.debug("Error running command.", e);
            return false;
        }
    }

    private static String[] prepareCommand(String command, String args, String file) {
        List<String> parts = new ArrayList<>();
        parts.add(command);

        if (args != null) {
            for (String s : args.split(" ")) {
                s = String.format(s, file);

                parts.add(s.trim());
            }
        }

        return parts.toArray(new String[parts.size()]);
    }

    public static OperatingSystem getOs() {
        String s = System.getProperty("os.name").toLowerCase();

        if (s.contains("win")) {
            return OperatingSystem.WINDOWS;
        }

        if (s.contains("mac")) {
            return OperatingSystem.MACOS;
        }

        if (s.contains("solaris")) {
            return OperatingSystem.SOLARIS;
        }

        if (s.contains("sunos")) {
            return OperatingSystem.SOLARIS;
        }

        if (s.contains("linux")) {
            return OperatingSystem.LINUX;
        }

        if (s.contains("unix")) {
            return OperatingSystem.LINUX;
        }

        return OperatingSystem.UNKNOWN;
    }
}
