import { async, ComponentFixture, TestBed } from '@angular/core/testing';

import { CiphertextComponent } from './ciphertext.component';

describe('CiphertextComponent', () => {
  let component: CiphertextComponent;
  let fixture: ComponentFixture<CiphertextComponent>;

  beforeEach(async(() => {
    TestBed.configureTestingModule({
      declarations: [ CiphertextComponent ]
    })
    .compileComponents();
  }));

  beforeEach(() => {
    fixture = TestBed.createComponent(CiphertextComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
