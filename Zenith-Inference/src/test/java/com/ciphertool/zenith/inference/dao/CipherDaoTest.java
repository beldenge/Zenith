/**
 * Copyright 2017-2019 George Belden
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

package com.ciphertool.zenith.inference.dao;

import com.ciphertool.zenith.inference.entities.Cipher;
import org.junit.BeforeClass;
import org.junit.Test;

import static org.junit.Assert.assertNull;

public class CipherDaoTest {
	private static CipherDao		cipherDao;

	@BeforeClass
	public static void setUp() {
		cipherDao = new CipherDao();
	}

	@Test
	public void testFindByCipherNameNull() {
		Cipher cipherReturned = cipherDao.findByCipherName(null);

		assertNull(cipherReturned);
	}

	@Test
	public void testFindByCipherNameEmpty() {
		Cipher cipherReturned = cipherDao.findByCipherName("");

		assertNull(cipherReturned);
	}
}
