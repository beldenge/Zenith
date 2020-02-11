import { async, ComponentFixture, TestBed } from '@angular/core/testing';

import { PlaintextTransformersComponent } from './plaintext-transformers.component';

describe('PlaintextTransformersComponent', () => {
  let component: PlaintextTransformersComponent;
  let fixture: ComponentFixture<PlaintextTransformersComponent>;

  beforeEach(async(() => {
    TestBed.configureTestingModule({
      declarations: [ PlaintextTransformersComponent ]
    })
    .compileComponents();
  }));

  beforeEach(() => {
    fixture = TestBed.createComponent(PlaintextTransformersComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
