import { async, ComponentFixture, TestBed } from '@angular/core/testing';

import { PlaintextSampleComponent } from './plaintext-sample.component';

describe('PlaintextSampleComponent', () => {
  let component: PlaintextSampleComponent;
  let fixture: ComponentFixture<PlaintextSampleComponent>;

  beforeEach(async(() => {
    TestBed.configureTestingModule({
      declarations: [ PlaintextSampleComponent ]
    })
    .compileComponents();
  }));

  beforeEach(() => {
    fixture = TestBed.createComponent(PlaintextSampleComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
