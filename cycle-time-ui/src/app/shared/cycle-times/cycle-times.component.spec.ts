import { ComponentFixture, TestBed } from '@angular/core/testing';

import { CycleTimesComponent } from './cycle-times.component';

describe('CycleTimesComponent', () => {
  let component: CycleTimesComponent;
  let fixture: ComponentFixture<CycleTimesComponent>;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      declarations: [CycleTimesComponent]
    })
    .compileComponents();

    fixture = TestBed.createComponent(CycleTimesComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
