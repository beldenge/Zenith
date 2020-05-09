import { TestBed } from '@angular/core/testing';

import { FitnessFunctionService } from './fitness-function.service';

describe('FitnessFunctionService', () => {
  let service: FitnessFunctionService;

  beforeEach(() => {
    TestBed.configureTestingModule({});
    service = TestBed.inject(FitnessFunctionService);
  });

  it('should be created', () => {
    expect(service).toBeTruthy();
  });
});
