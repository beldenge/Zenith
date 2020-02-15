import { TestBed } from '@angular/core/testing';

import { CipherStatisticsService } from './cipher-statistics.service';

describe('CipherStatisticsService', () => {
  beforeEach(() => TestBed.configureTestingModule({}));

  it('should be created', () => {
    const service: CipherStatisticsService = TestBed.get(CipherStatisticsService);
    expect(service).toBeTruthy();
  });
});
