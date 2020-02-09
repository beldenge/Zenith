import { async, ComponentFixture, TestBed } from '@angular/core/testing';

import { ManageCiphersComponent } from './manage-ciphers.component';

describe('ManageCiphersComponent', () => {
  let component: ManageCiphersComponent;
  let fixture: ComponentFixture<ManageCiphersComponent>;

  beforeEach(async(() => {
    TestBed.configureTestingModule({
      declarations: [ ManageCiphersComponent ]
    })
    .compileComponents();
  }));

  beforeEach(() => {
    fixture = TestBed.createComponent(ManageCiphersComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
