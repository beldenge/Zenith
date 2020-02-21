import { TestBed } from '@angular/core/testing';

import { IntroductionService } from './introduction.service';

describe('IntroductionService', () => {
  beforeEach(() => TestBed.configureTestingModule({}));

  it('should be created', () => {
    const service: IntroductionService = TestBed.get(IntroductionService);
    expect(service).toBeTruthy();
  });
});
