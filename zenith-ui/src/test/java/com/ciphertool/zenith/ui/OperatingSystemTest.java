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

package com.ciphertool.zenith.ui;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class OperatingSystemTest {

    @Test
    void given_validInput_when_checkingLinuxForLinuxReturnsTrue_then_returnsTrue() {
        assertTrue(OperatingSystem.LINUX.isLinux());
    }

    @Test
    void given_validInput_when_checkingLinuxForSolarisReturnsTrue_then_returnsTrue() {
        // SOLARIS is treated as Linux-compatible
        assertTrue(OperatingSystem.SOLARIS.isLinux());
    }

    @Test
    void given_validInput_when_checkingLinuxForMacOsReturnsFalse_then_returnsFalse() {
        assertFalse(OperatingSystem.MACOS.isLinux());
    }

    @Test
    void given_validInput_when_checkingLinuxForWindowsReturnsFalse_then_returnsFalse() {
        assertFalse(OperatingSystem.WINDOWS.isLinux());
    }

    @Test
    void given_missingInput_when_checkingLinuxForUnknownReturnsFalse_then_returnsFalse() {
        assertFalse(OperatingSystem.UNKNOWN.isLinux());
    }

    @Test
    void given_validInput_when_checkingMacForMacOsReturnsTrue_then_returnsTrue() {
        assertTrue(OperatingSystem.MACOS.isMac());
    }

    @Test
    void given_validInput_when_checkingMacForLinuxReturnsFalse_then_returnsFalse() {
        assertFalse(OperatingSystem.LINUX.isMac());
    }

    @Test
    void given_validInput_when_checkingMacForWindowsReturnsFalse_then_returnsFalse() {
        assertFalse(OperatingSystem.WINDOWS.isMac());
    }

    @Test
    void given_validInput_when_checkingWindowsForWindowsReturnsTrue_then_returnsTrue() {
        assertTrue(OperatingSystem.WINDOWS.isWindows());
    }

    @Test
    void given_validInput_when_checkingWindowsForLinuxReturnsFalse_then_returnsFalse() {
        assertFalse(OperatingSystem.LINUX.isWindows());
    }

    @Test
    void given_validInput_when_checkingWindowsForMacOsReturnsFalse_then_returnsFalse() {
        assertFalse(OperatingSystem.MACOS.isWindows());
    }
}
