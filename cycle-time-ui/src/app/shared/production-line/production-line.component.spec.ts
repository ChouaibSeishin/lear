import { ComponentFixture, TestBed } from '@angular/core/testing';

import { ProductionLineComponent } from './production-line.component';

describe('ProductionLineComponent', () => {
  let component: ProductionLineComponent;
  let fixture: ComponentFixture<ProductionLineComponent>;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      declarations: [ProductionLineComponent]
    })
    .compileComponents();

    fixture = TestBed.createComponent(ProductionLineComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
