import { async, ComponentFixture, TestBed } from '@angular/core/testing';

import { NewCipherModalComponent } from './new-cipher-modal.component';

describe('NewCipherModalComponent', () => {
  let component: NewCipherModalComponent;
  let fixture: ComponentFixture<NewCipherModalComponent>;

  beforeEach(async(() => {
    TestBed.configureTestingModule({
      declarations: [ NewCipherModalComponent ]
    })
    .compileComponents();
  }));

  beforeEach(() => {
    fixture = TestBed.createComponent(NewCipherModalComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
