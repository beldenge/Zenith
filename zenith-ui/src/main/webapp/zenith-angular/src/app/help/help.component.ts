import { Component, OnInit } from '@angular/core';
import { IntroductionService } from "../introduction.service";

@Component({
  selector: 'app-help',
  templateUrl: './help.component.html',
  styleUrls: ['./help.component.css']
})
export class HelpComponent implements OnInit {

  constructor(private introductionService: IntroductionService) { }

  ngOnInit() {
  }

  replayIntroduction() {
    this.introductionService.startIntro();
  }
}
