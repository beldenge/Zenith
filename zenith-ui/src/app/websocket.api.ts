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

import * as Stomp from 'stompjs';
import SockJS from 'sockjs-client';

export class WebSocketAPI {
  stompClient: any;

  connectAndSend(request, successHandler, errorHandler) {
    const socket = new SockJS('ws'); // relative URL
    const self = this;
    self.stompClient = Stomp.over(socket);
    self.stompClient.connect({}, frame => {
      self.send(request);

      self.stompClient.subscribe('/topic/solutions', successHandler, errorHandler);
    });
  }

  disconnect() {
    if (this.stompClient !== null) {
      this.stompClient.disconnect();
    }
  }

  send(message: any) {
    this.stompClient.send('/app/solutions', {}, JSON.stringify(message));
  }
}
