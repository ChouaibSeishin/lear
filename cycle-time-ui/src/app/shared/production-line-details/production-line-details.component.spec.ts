import { ComponentFixture, TestBed } from '@angular/core/testing';

import { ProductionLineDetailsComponent } from './production-line-details.component';

describe('ProductionLineDetailsComponent', () => {
  let component: ProductionLineDetailsComponent;
  let fixture: ComponentFixture<ProductionLineDetailsComponent>;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      declarations: [ProductionLineDetailsComponent]
    })
    .compileComponents();

    fixture = TestBed.createComponent(ProductionLineDetailsComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
