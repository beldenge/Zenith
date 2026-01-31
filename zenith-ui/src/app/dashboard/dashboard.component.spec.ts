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
import { DashboardComponent } from './dashboard.component';
import { ReactiveFormsModule } from '@angular/forms';
import { MatSnackBar } from '@angular/material/snack-bar';
import { ConfigurationService } from '../configuration.service';
import { IntroductionService } from '../introduction.service';
import { SolutionService } from '../solution.service';
import { signal } from '@angular/core';
import { Cipher } from '../models/Cipher';
import { of } from 'rxjs';
import { NoopAnimationsModule } from '@angular/platform-browser/animations';

describe('DashboardComponent', () => {
  let component: DashboardComponent;
  let fixture: ComponentFixture<DashboardComponent>;
  let mockConfigurationService: any;
  let mockSolutionService: any;
  let mockIntroductionService: any;
  let mockSnackBar: any;

  const testCipher = new Cipher('Z340', 20, 17, ['A', 'B', 'C']);

  beforeEach(async () => {
    mockConfigurationService = {
      selectedCipher: signal(testCipher),
      epochs: signal(1),
      selectedOptimizer: signal({ name: 'SimulatedAnnealing', displayName: 'Simulated Annealing' }),
      selectedFitnessFunction: signal({
        name: 'TestFitness',
        displayName: 'Test Fitness',
        form: null
      }),
      simulatedAnnealingConfiguration: signal({
        samplerIterations: 1000,
        annealingTemperatureMin: 0.01,
        annealingTemperatureMax: 1.0
      }),
      geneticAlgorithmConfiguration: signal(null),
      appliedPlaintextTransformers: signal([]),
      updateEpochs: jasmine.createSpy('updateEpochs')
    };

    mockSolutionService = {
      solution: signal(null),
      runState: signal(false),
      progressPercentage: signal(0),
      updateSolution: jasmine.createSpy('updateSolution'),
      updateRunState: jasmine.createSpy('updateRunState'),
      updateProgressPercentage: jasmine.createSpy('updateProgressPercentage'),
      solveSolution: jasmine.createSpy('solveSolution').and.returnValue(of('test-request-id')),
      solutionUpdates: jasmine.createSpy('solutionUpdates').and.returnValue(of({
        type: 'SOLUTION',
        solutionData: { plaintext: 'test', scores: [1.0] }
      })),
      handleSolutionUpdate: jasmine.createSpy('handleSolutionUpdate')
    };

    mockIntroductionService = {
      showIntroDashboard: signal(false),
      startIntroDashboard: jasmine.createSpy('startIntroDashboard'),
      updateShowIntroDashboard: jasmine.createSpy('updateShowIntroDashboard')
    };

    mockSnackBar = {
      open: jasmine.createSpy('open')
    };

    await TestBed.configureTestingModule({
      imports: [ReactiveFormsModule, NoopAnimationsModule],
      declarations: [DashboardComponent],
      providers: [
        { provide: ConfigurationService, useValue: mockConfigurationService },
        { provide: SolutionService, useValue: mockSolutionService },
        { provide: IntroductionService, useValue: mockIntroductionService },
        { provide: MatSnackBar, useValue: mockSnackBar }
      ]
    }).compileComponents();

    fixture = TestBed.createComponent(DashboardComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });

  /**
   * BUG FIX TEST: Verifies that transformers without forms don't cause errors.
   * Previously, the code assumed transformer.form always existed, causing
   * "Cannot read property 'model' of null" errors for transformers without config.
   */
  describe('solve with transformers without forms', () => {
    it('should handle plaintext transformer with null form', () => {
      // Set up a transformer that has no form (form is null)
      mockConfigurationService.appliedPlaintextTransformers = signal([
        {
          name: 'RemoveSpacesTransformer',
          displayName: 'Remove Spaces',
          form: null,  // No configuration form
          order: 1,
          helpText: 'Removes spaces from plaintext'
        }
      ]);

      // Re-create component with updated config
      fixture = TestBed.createComponent(DashboardComponent);
      component = fixture.componentInstance;
      component.hyperparametersForm.setValue({ epochs: 1 });
      fixture.detectChanges();

      // This should not throw an error
      expect(() => component.solve()).not.toThrow();
    });

    it('should include transformer with null form in request', () => {
      mockConfigurationService.appliedPlaintextTransformers = signal([
        {
          name: 'RemoveSpacesTransformer',
          displayName: 'Remove Spaces',
          form: null,
          order: 1,
          helpText: 'Removes spaces'
        }
      ]);

      fixture = TestBed.createComponent(DashboardComponent);
      component = fixture.componentInstance;
      component.hyperparametersForm.setValue({ epochs: 1 });
      fixture.detectChanges();

      component.solve();

      expect(mockSolutionService.solveSolution).toHaveBeenCalled();
      const request = mockSolutionService.solveSolution.calls.mostRecent().args[0];
      expect(request.plaintextTransformers).toBeDefined();
      expect(request.plaintextTransformers.length).toBe(1);
      expect(request.plaintextTransformers[0].transformerName).toBe('RemoveSpacesTransformer');
      expect(request.plaintextTransformers[0].data).toBeNull();
    });

    it('should handle mix of transformers with and without forms', () => {
      const mockFormGroup = {
        valid: true
      };

      mockConfigurationService.appliedPlaintextTransformers = signal([
        {
          name: 'RemoveSpacesTransformer',
          displayName: 'Remove Spaces',
          form: null,  // No form
          order: 1
        },
        {
          name: 'ConfigurableTransformer',
          displayName: 'Configurable',
          form: {
            model: { key: 'value' },
            form: mockFormGroup
          },
          order: 2
        }
      ]);

      fixture = TestBed.createComponent(DashboardComponent);
      component = fixture.componentInstance;
      component.hyperparametersForm.setValue({ epochs: 1 });
      fixture.detectChanges();

      expect(() => component.solve()).not.toThrow();

      const request = mockSolutionService.solveSolution.calls.mostRecent().args[0];
      expect(request.plaintextTransformers.length).toBe(2);
      expect(request.plaintextTransformers[0].data).toBeNull();
      expect(request.plaintextTransformers[1].data).toEqual({ key: 'value' });
    });

    it('should treat transformer with null form as valid', () => {
      mockConfigurationService.appliedPlaintextTransformers = signal([
        {
          name: 'NoFormTransformer',
          displayName: 'No Form',
          form: null,
          order: 1
        }
      ]);

      fixture = TestBed.createComponent(DashboardComponent);
      component = fixture.componentInstance;
      component.hyperparametersForm.setValue({ epochs: 1 });
      fixture.detectChanges();

      component.solve();

      // Should not show validation error snackbar
      expect(mockSnackBar.open).not.toHaveBeenCalledWith(
        'Errors exist in plaintext transformers.  Please correct them before solving.',
        jasmine.any(String),
        jasmine.any(Object)
      );
    });

    it('should show error when transformer form is invalid', () => {
      const invalidFormGroup = {
        valid: false
      };

      mockConfigurationService.appliedPlaintextTransformers = signal([
        {
          name: 'InvalidTransformer',
          displayName: 'Invalid',
          form: {
            model: {},
            form: invalidFormGroup
          },
          order: 1
        }
      ]);

      fixture = TestBed.createComponent(DashboardComponent);
      component = fixture.componentInstance;
      component.hyperparametersForm.setValue({ epochs: 1 });
      fixture.detectChanges();

      component.solve();

      expect(mockSnackBar.open).toHaveBeenCalledWith(
        'Errors exist in plaintext transformers.  Please correct them before solving.',
        '',
        jasmine.any(Object)
      );
    });
  });

  describe('solve validation', () => {
    it('should not solve if form is invalid', () => {
      component.hyperparametersForm.setValue({ epochs: 101 }); // Invalid: max is 100

      component.solve();

      expect(mockSolutionService.solveSolution).not.toHaveBeenCalled();
    });

    it('should not solve if already running', () => {
      mockSolutionService.runState = signal(true);
      fixture = TestBed.createComponent(DashboardComponent);
      component = fixture.componentInstance;
      component.hyperparametersForm.setValue({ epochs: 1 });
      fixture.detectChanges();

      component.solve();

      expect(mockSolutionService.solveSolution).not.toHaveBeenCalled();
    });
  });
});
