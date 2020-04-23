import { TestBed } from '@angular/core/testing';

import { SolutionService } from './solution.service';

describe('SolutionService', () => {
  let service: SolutionService;

  beforeEach(() => {
    TestBed.configureTestingModule({});
    service = TestBed.inject(SolutionService);
  });

  it('should be created', () => {
    expect(service).toBeTruthy();
  });
});
