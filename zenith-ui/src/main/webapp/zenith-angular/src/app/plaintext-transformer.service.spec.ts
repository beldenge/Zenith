import { TestBed } from '@angular/core/testing';

import { PlaintextTransformerService } from './plaintext-transformer.service';

describe('PlaintextTransformerService', () => {
  beforeEach(() => TestBed.configureTestingModule({}));

  it('should be created', () => {
    const service: PlaintextTransformerService = TestBed.get(PlaintextTransformerService);
    expect(service).toBeTruthy();
  });
});
