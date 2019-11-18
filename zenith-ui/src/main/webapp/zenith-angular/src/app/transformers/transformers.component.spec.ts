import { async, ComponentFixture, TestBed } from '@angular/core/testing';

import { TransformersComponent } from './transformers.component';

describe('TransformersComponent', () => {
  let component: TransformersComponent;
  let fixture: ComponentFixture<TransformersComponent>;

  beforeEach(async(() => {
    TestBed.configureTestingModule({
      declarations: [ TransformersComponent ]
    })
    .compileComponents();
  }));

  beforeEach(() => {
    fixture = TestBed.createComponent(TransformersComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
