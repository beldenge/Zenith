import { TestBed } from '@angular/core/testing';

import { CiphertextTransformerService } from './ciphertext-transformer.service';

describe('CiphertextTransformerService', () => {
  beforeEach(() => TestBed.configureTestingModule({}));

  it('should be created', () => {
    const service: CiphertextTransformerService = TestBed.get(CiphertextTransformerService);
    expect(service).toBeTruthy();
  });
});
