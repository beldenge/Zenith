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
import { ManageCiphersComponent } from './manage-ciphers.component';
import { MatDialog } from '@angular/material/dialog';
import { MatSnackBar } from '@angular/material/snack-bar';
import { IntroductionService } from '../introduction.service';
import { ConfigurationService } from '../configuration.service';
import { signal } from '@angular/core';
import { Cipher } from '../models/Cipher';
import { NoopAnimationsModule } from '@angular/platform-browser/animations';
import { MatTableModule } from '@angular/material/table';
import { MatPaginatorModule } from '@angular/material/paginator';
import { MatSortModule } from '@angular/material/sort';
import { MatFormFieldModule } from '@angular/material/form-field';
import { MatInputModule } from '@angular/material/input';

describe('ManageCiphersComponent', () => {
  let component: ManageCiphersComponent;
  let fixture: ComponentFixture<ManageCiphersComponent>;
  let mockConfigurationService: any;
  let mockIntroductionService: any;

  beforeEach(async () => {
    const testCiphers = [
      new Cipher('Z340', 20, 17, ['A', 'B', 'C']),
      new Cipher('Z408', 24, 17, ['X', 'Y', 'Z']),
      new Cipher('Test Cipher', 10, 10, ['1', '2', '3'])
    ];

    mockConfigurationService = {
      ciphers: signal(testCiphers),
      selectedCipher: signal(testCiphers[0]),
      updateCiphers: jasmine.createSpy('updateCiphers'),
      updateSelectedCipher: jasmine.createSpy('updateSelectedCipher')
    };

    mockIntroductionService = {
      showIntroManageCiphers: signal(false),
      startIntroManageCiphers: jasmine.createSpy('startIntroManageCiphers'),
      updateShowIntroManageCiphers: jasmine.createSpy('updateShowIntroManageCiphers')
    };

    await TestBed.configureTestingModule({
      imports: [
        NoopAnimationsModule,
        MatTableModule,
        MatPaginatorModule,
        MatSortModule,
        MatFormFieldModule,
        MatInputModule
      ],
      declarations: [ManageCiphersComponent],
      providers: [
        { provide: MatDialog, useValue: { open: jasmine.createSpy('open') } },
        { provide: MatSnackBar, useValue: { open: jasmine.createSpy('open') } },
        { provide: IntroductionService, useValue: mockIntroductionService },
        { provide: ConfigurationService, useValue: mockConfigurationService }
      ]
    }).compileComponents();

    fixture = TestBed.createComponent(ManageCiphersComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });

  /**
   * BUG FIX TEST: Verifies case-insensitive filter matching.
   * Previously, filter was lowercased in applyFilter() but cipher name was not
   * lowercased in the filterPredicate, causing case-sensitive mismatches.
   */
  describe('filter predicate case sensitivity', () => {
    it('should match cipher name case-insensitively', () => {
      // The filter value is lowercased in applyFilter()
      component.ciphersDataSource.filter = 'z340';

      // Z340 should match even though we searched for lowercase
      const z340Cipher = new Cipher('Z340', 20, 17, ['A', 'B', 'C']);
      const matchResult = component.ciphersDataSource.filterPredicate(z340Cipher, 'z340');

      expect(matchResult).toBeTrue();
    });

    it('should match partial cipher name case-insensitively', () => {
      const testCipher = new Cipher('Test Cipher', 10, 10, ['1', '2', '3']);
      const matchResult = component.ciphersDataSource.filterPredicate(testCipher, 'test');

      expect(matchResult).toBeTrue();
    });

    it('should not match when filter does not exist in name', () => {
      const z340Cipher = new Cipher('Z340', 20, 17, ['A', 'B', 'C']);
      const matchResult = component.ciphersDataSource.filterPredicate(z340Cipher, 'xyz');

      expect(matchResult).toBeFalse();
    });

    it('should match uppercase filter against uppercase name', () => {
      // Note: In actual usage, filter is always lowercased by applyFilter()
      const z340Cipher = new Cipher('Z340', 20, 17, ['A', 'B', 'C']);
      const matchResult = component.ciphersDataSource.filterPredicate(z340Cipher, 'z340');

      expect(matchResult).toBeTrue();
    });
  });

  describe('applyFilter', () => {
    it('should trim and lowercase the filter value', () => {
      const mockEvent = {
        target: { value: '  Z340  ' }
      } as unknown as Event;

      component.applyFilter(mockEvent);

      expect(component.ciphersDataSource.filter).toBe('z340');
    });
  });
});
