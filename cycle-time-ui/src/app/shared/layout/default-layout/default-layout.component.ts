import { Component } from '@angular/core';
import {NgbModal} from "@ng-bootstrap/ng-bootstrap";
import {ChatBotModalComponent} from "../../chat-bot-modal/chat-bot-modal.component";



@Component({
  selector: 'app-default-layout',
  standalone: false,
  templateUrl: './default-layout.component.html',
  styleUrl: './default-layout.component.css'
})
export class DefaultLayoutComponent {

  constructor(private modalService: NgbModal) {}

  openChatBotModal() {
    this.modalService.open(ChatBotModalComponent, {
      size: 'md',
      backdrop:'static',
      keyboard: true
    });
  }
}
