import { async, ComponentFixture, TestBed } from '@angular/core/testing';

import { PlaintextComponent } from './plaintext.component';

describe('PlaintextComponent', () => {
  let component: PlaintextComponent;
  let fixture: ComponentFixture<PlaintextComponent>;

  beforeEach(async(() => {
    TestBed.configureTestingModule({
      declarations: [ PlaintextComponent ]
    })
    .compileComponents();
  }));

  beforeEach(() => {
    fixture = TestBed.createComponent(PlaintextComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
