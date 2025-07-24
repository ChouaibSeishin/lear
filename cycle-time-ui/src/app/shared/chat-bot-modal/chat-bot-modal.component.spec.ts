import { ComponentFixture, TestBed } from '@angular/core/testing';

import { ChatBotModalComponent } from './chat-bot-modal.component';

describe('ChatBotModalComponent', () => {
  let component: ChatBotModalComponent;
  let fixture: ComponentFixture<ChatBotModalComponent>;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      declarations: [ChatBotModalComponent]
    })
    .compileComponents();

    fixture = TestBed.createComponent(ChatBotModalComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
