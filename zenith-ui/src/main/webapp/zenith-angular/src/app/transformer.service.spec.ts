import { TestBed } from '@angular/core/testing';

import { TransformerService } from './transformer.service';

describe('TransformerService', () => {
  beforeEach(() => TestBed.configureTestingModule({}));

  it('should be created', () => {
    const service: TransformerService = TestBed.get(TransformerService);
    expect(service).toBeTruthy();
  });
});
