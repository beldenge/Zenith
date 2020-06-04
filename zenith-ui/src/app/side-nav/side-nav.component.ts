/*
 * Copyright 2017-2020 George Belden
 *
 * This file is part of Zenith.
 *
 * Zenith is free software: you can redistribute it and/or modify it under
 * the terms of the GNU General Public License as published by the Free Software
 * Foundation, either version 3 of the License, or (at your option) any later
 * version.
 *
 * Zenith is distributed in the hope that it will be useful, but WITHOUT
 * ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS
 * FOR A PARTICULAR PURPOSE. See the GNU General Public License for more
 * details.
 *
 * You should have received a copy of the GNU General Public License along with
 * Zenith. If not, see <http://www.gnu.org/licenses/>.
 */

import { Component, OnDestroy, OnInit, Renderer2 } from '@angular/core';
import { environment } from "../../environments/environment";
import { SidebarService } from "../sidebar.service";
import { Subscription } from "rxjs";

@Component({
  selector: 'app-side-nav',
  templateUrl: './side-nav.component.html',
  styleUrls: ['./side-nav.component.css']
})
export class SideNavComponent implements OnInit, OnDestroy {
  applicationVersion: string = environment.applicationVersion;
  sidebarToggleSubscription: Subscription;
  toggled: boolean = true;

  constructor(private sidebarService: SidebarService, private renderer: Renderer2) {}

  ngOnInit() {
    this.sidebarToggleSubscription = this.sidebarService.getSidebarToggleAsObservable().subscribe(toggled => {
      this.toggled = toggled;

      if (toggled) {
        if (!document.body.classList.contains('sidebar-toggled')) {
          this.renderer.addClass(document.body, 'sidebar-toggled');
        }
      } else {
        this.renderer.removeClass(document.body, 'sidebar-toggled');
      }
    });
  }

  ngOnDestroy() {
    this.sidebarToggleSubscription.unsubscribe();
  }
}
