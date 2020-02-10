import { async, ComponentFixture, TestBed } from '@angular/core/testing';

import { CipherModalComponent } from './cipher-modal.component';

describe('CipherModalComponent', () => {
  let component: CipherModalComponent;
  let fixture: ComponentFixture<CipherModalComponent>;

  beforeEach(async(() => {
    TestBed.configureTestingModule({
      declarations: [ CipherModalComponent ]
    })
    .compileComponents();
  }));

  beforeEach(() => {
    fixture = TestBed.createComponent(CipherModalComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
