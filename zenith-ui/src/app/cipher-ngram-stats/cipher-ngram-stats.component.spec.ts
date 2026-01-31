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

import { ComponentFixture, TestBed, fakeAsync, tick } from '@angular/core/testing';
import { CipherNgramStatsComponent } from './cipher-ngram-stats.component';
import { CipherStatisticsService } from '../cipher-statistics.service';
import { ConfigurationService } from '../configuration.service';
import { signal, WritableSignal } from '@angular/core';
import { Cipher } from '../models/Cipher';
import { of } from 'rxjs';
import { NoopAnimationsModule } from '@angular/platform-browser/animations';
import { MatExpansionModule } from '@angular/material/expansion';

describe('CipherNgramStatsComponent', () => {
  let component: CipherNgramStatsComponent;
  let fixture: ComponentFixture<CipherNgramStatsComponent>;
  let mockStatisticsService: any;
  let mockConfigurationService: any;
  let selectedCipherSignal: WritableSignal<Cipher | null>;

  const testCipher1 = new Cipher('Z340', 20, 17, ['A', 'B', 'C']);
  const testCipher2 = new Cipher('Z408', 24, 17, ['X', 'Y', 'Z']);

  beforeEach(async () => {
    selectedCipherSignal = signal(testCipher1);

    mockStatisticsService = {
      getNgramStatistics: jasmine.createSpy('getNgramStatistics').and.returnValue(of({
        firstNGramCounts: [{ ngram: 'A', count: 5 }],
        secondNGramCounts: [{ ngram: 'AB', count: 3 }],
        thirdNGramCounts: [{ ngram: 'ABC', count: 1 }]
      }))
    };

    mockConfigurationService = {
      selectedCipher: selectedCipherSignal
    };

    await TestBed.configureTestingModule({
      imports: [NoopAnimationsModule, MatExpansionModule],
      declarations: [CipherNgramStatsComponent],
      providers: [
        { provide: CipherStatisticsService, useValue: mockStatisticsService },
        { provide: ConfigurationService, useValue: mockConfigurationService }
      ]
    }).compileComponents();

    fixture = TestBed.createComponent(CipherNgramStatsComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });

  /**
   * BUG FIX TEST: Verifies ngram data is cleared when cipher changes.
   * Previously, when switching ciphers, the old cipher's ngram data would
   * be briefly displayed when expanding the panel, since ngramsDataSources
   * was not reset on cipher change.
   */
  describe('data clearing on cipher change', () => {
    it('should clear ngramsDataSources when cipher changes', fakeAsync(() => {
      // First, load some data for cipher 1
      component.onExpand();
      tick();

      expect(component.ngramsDataSources.length).toBe(1);

      // Now change the cipher
      selectedCipherSignal.set(testCipher2);
      fixture.detectChanges();
      tick();

      // Data should be cleared
      expect(component.ngramsDataSources.length).toBe(0);
    }));

    it('should close panel when cipher changes', fakeAsync(() => {
      const closeSpy = spyOn(component.matExpansionPanelElement, 'close');

      selectedCipherSignal.set(testCipher2);
      fixture.detectChanges();
      tick();

      expect(closeSpy).toHaveBeenCalled();
    }));

    it('should load fresh data when expanding panel after cipher change', fakeAsync(() => {
      // Load data for cipher 1
      component.onExpand();
      tick();

      const initialCallCount = mockStatisticsService.getNgramStatistics.calls.count();

      // Change cipher (this clears data)
      selectedCipherSignal.set(testCipher2);
      fixture.detectChanges();
      tick();

      // Expand again
      component.onExpand();
      tick();

      // Should have made a new API call
      expect(mockStatisticsService.getNgramStatistics.calls.count()).toBe(initialCallCount + 1);

      // Should be called with the new cipher
      const lastCall = mockStatisticsService.getNgramStatistics.calls.mostRecent();
      expect(lastCall.args[0]).toBe(testCipher2);
    }));

    it('should not show old cipher data when expanding after cipher change', fakeAsync(() => {
      // Load data for cipher 1
      component.onExpand();
      tick();

      expect(component.ngramsDataSources.length).toBeGreaterThan(0);

      // Change cipher
      selectedCipherSignal.set(testCipher2);
      fixture.detectChanges();
      tick();

      // Before expanding, data should be cleared
      expect(component.ngramsDataSources.length).toBe(0);
    }));
  });

  describe('onExpand', () => {
    it('should load data if none exists', fakeAsync(() => {
      expect(component.ngramsDataSources.length).toBe(0);

      component.onExpand();
      tick();

      expect(component.ngramsDataSources.length).toBe(1);
    }));

    it('should not reload data if already loaded', fakeAsync(() => {
      component.onExpand();
      tick();

      const callCount = mockStatisticsService.getNgramStatistics.calls.count();

      component.onExpand();
      tick();

      expect(mockStatisticsService.getNgramStatistics.calls.count()).toBe(callCount);
    }));
  });

  describe('onCollapse', () => {
    it('should not throw when collapsing', fakeAsync(() => {
      expect(() => component.onCollapse()).not.toThrow();
      tick();
    }));
  });

  describe('onMore', () => {
    it('should load additional pages of statistics', fakeAsync(() => {
      component.onMore();
      tick();

      expect(component.ngramsDataSources.length).toBe(1);

      component.onMore();
      tick();

      expect(component.ngramsDataSources.length).toBe(2);
    }));

    it('should call service with correct page number', fakeAsync(() => {
      component.onMore();
      tick();

      expect(mockStatisticsService.getNgramStatistics).toHaveBeenCalledWith(testCipher1, 0);

      component.onMore();
      tick();

      expect(mockStatisticsService.getNgramStatistics).toHaveBeenCalledWith(testCipher1, 1);
    }));
  });
});
