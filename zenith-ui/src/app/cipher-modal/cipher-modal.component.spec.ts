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

import { ComponentFixture, TestBed } from '@angular/core/testing';
import { CipherModalComponent } from './cipher-modal.component';
import { MAT_DIALOG_DATA, MatDialogRef } from '@angular/material/dialog';
import { MatSnackBar } from '@angular/material/snack-bar';
import { ConfigurationService } from '../configuration.service';
import { ReactiveFormsModule } from '@angular/forms';
import { signal } from '@angular/core';
import { Cipher } from '../models/Cipher';
import { NoopAnimationsModule } from '@angular/platform-browser/animations';

describe('CipherModalComponent', () => {
  let component: CipherModalComponent;
  let fixture: ComponentFixture<CipherModalComponent>;
  let mockConfigurationService: any;
  let mockDialogRef: any;
  let mockSnackBar: any;

  const testCiphers = [
    new Cipher('Z340', 20, 17, ['A', 'B', 'C', 'D']),
    new Cipher('Z408', 24, 17, ['X', 'Y', 'Z', 'W'])
  ];

  beforeEach(async () => {
    mockConfigurationService = {
      ciphers: signal(testCiphers),
      selectedCipher: signal(testCiphers[0]),
      updateCiphers: jasmine.createSpy('updateCiphers'),
      updateSelectedCipher: jasmine.createSpy('updateSelectedCipher')
    };

    mockDialogRef = {
      close: jasmine.createSpy('close')
    };

    mockSnackBar = {
      open: jasmine.createSpy('open')
    };

    await TestBed.configureTestingModule({
      imports: [ReactiveFormsModule, NoopAnimationsModule],
      declarations: [CipherModalComponent],
      providers: [
        { provide: MatDialogRef, useValue: mockDialogRef },
        { provide: MAT_DIALOG_DATA, useValue: { mode: 'CREATE' } },
        { provide: MatSnackBar, useValue: mockSnackBar },
        { provide: ConfigurationService, useValue: mockConfigurationService }
      ]
    }).compileComponents();

    fixture = TestBed.createComponent(CipherModalComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });

  /**
   * BUG FIX TEST: Verifies ciphertext is saved as string[] not string.
   * Previously, ciphertext was passed as a plain string to constructors
   * that expected string[], causing type mismatches and incorrect data handling.
   */
  describe('save ciphertext as array', () => {
    it('should convert ciphertext input to array when creating cipher', () => {
      component.newCipherForm.setValue({
        name: 'NewCipher',
        ciphertext: 'A B C\nD E F'
      });

      component.save();

      expect(mockConfigurationService.updateCiphers).toHaveBeenCalled();
      const callArgs = mockConfigurationService.updateCiphers.calls.mostRecent().args[0];
      const newCipher = callArgs[callArgs.length - 1];

      // Verify ciphertext is an array, not a string
      expect(Array.isArray(newCipher.ciphertext)).toBeTrue();
      expect(newCipher.ciphertext).toEqual(['A', 'B', 'C', 'D', 'E', 'F']);
    });

    it('should handle single-character symbols correctly', () => {
      component.newCipherForm.setValue({
        name: 'SingleChar',
        ciphertext: 'X Y Z'
      });

      component.save();

      const callArgs = mockConfigurationService.updateCiphers.calls.mostRecent().args[0];
      const newCipher = callArgs[callArgs.length - 1];

      expect(newCipher.ciphertext).toEqual(['X', 'Y', 'Z']);
    });

    it('should handle multi-character symbols correctly', () => {
      component.newCipherForm.setValue({
        name: 'MultiChar',
        ciphertext: 'AA BB CC'
      });

      component.save();

      const callArgs = mockConfigurationService.updateCiphers.calls.mostRecent().args[0];
      const newCipher = callArgs[callArgs.length - 1];

      expect(newCipher.ciphertext).toEqual(['AA', 'BB', 'CC']);
    });
  });

  describe('determineDimensions', () => {
    it('should correctly determine rows and columns', () => {
      const ciphertext = 'A B C\nD E F\nG H I';
      const dimensions = component.determineDimensions(ciphertext);

      expect(dimensions.rows).toBe(3);
      expect(dimensions.columns).toBe(3);
    });

    it('should return -1 for empty ciphertext', () => {
      const dimensions = component.determineDimensions('');

      expect(dimensions.rows).toBe(-1);
      expect(dimensions.columns).toBe(-1);
    });

    it('should return -1 columns for uneven rows', () => {
      const ciphertext = 'A B C\nD E'; // 3 columns then 2 columns
      const dimensions = component.determineDimensions(ciphertext);

      expect(dimensions.columns).toBe(-1);
    });
  });
});
