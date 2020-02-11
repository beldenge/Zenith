import { async, ComponentFixture, TestBed } from '@angular/core/testing';

import { CiphertextTransformersComponent } from './ciphertext-transformers.component';

describe('CiphertextTransformersComponent', () => {
  let component: CiphertextTransformersComponent;
  let fixture: ComponentFixture<CiphertextTransformersComponent>;

  beforeEach(async(() => {
    TestBed.configureTestingModule({
      declarations: [ CiphertextTransformersComponent ]
    })
    .compileComponents();
  }));

  beforeEach(() => {
    fixture = TestBed.createComponent(CiphertextTransformersComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
