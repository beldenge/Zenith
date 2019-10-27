import { async, ComponentFixture, TestBed } from '@angular/core/testing';

import { CipherStatsSummaryComponent } from './cipher-stats-summary.component';

describe('CipherStatsSummaryComponent', () => {
  let component: CipherStatsSummaryComponent;
  let fixture: ComponentFixture<CipherStatsSummaryComponent>;

  beforeEach(async(() => {
    TestBed.configureTestingModule({
      declarations: [ CipherStatsSummaryComponent ]
    })
    .compileComponents();
  }));

  beforeEach(() => {
    fixture = TestBed.createComponent(CipherStatsSummaryComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
