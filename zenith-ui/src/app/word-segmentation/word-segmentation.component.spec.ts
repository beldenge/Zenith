import { async, ComponentFixture, TestBed } from '@angular/core/testing';

import { WordSegmentationComponent } from './word-segmentation.component';

describe('WordSegmentationComponent', () => {
  let component: WordSegmentationComponent;
  let fixture: ComponentFixture<WordSegmentationComponent>;

  beforeEach(async(() => {
    TestBed.configureTestingModule({
      declarations: [ WordSegmentationComponent ]
    })
    .compileComponents();
  }));

  beforeEach(() => {
    fixture = TestBed.createComponent(WordSegmentationComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
