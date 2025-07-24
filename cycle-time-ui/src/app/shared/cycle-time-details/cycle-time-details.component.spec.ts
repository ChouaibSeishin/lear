import { ComponentFixture, TestBed } from '@angular/core/testing';

import { CycleTimeDetailsComponent } from './cycle-time-details.component';

describe('CycleTimeDetailsComponent', () => {
  let component: CycleTimeDetailsComponent;
  let fixture: ComponentFixture<CycleTimeDetailsComponent>;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      declarations: [CycleTimeDetailsComponent]
    })
    .compileComponents();

    fixture = TestBed.createComponent(CycleTimeDetailsComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
