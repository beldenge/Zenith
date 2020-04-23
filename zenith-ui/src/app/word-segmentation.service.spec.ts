import { TestBed } from '@angular/core/testing';

import { WordSegmentationService } from './word-segmentation.service';

describe('WordSegmentationService', () => {
  let service: WordSegmentationService;

  beforeEach(() => {
    TestBed.configureTestingModule({});
    service = TestBed.inject(WordSegmentationService);
  });

  it('should be created', () => {
    expect(service).toBeTruthy();
  });
});
