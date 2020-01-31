import * as Stomp from 'stompjs';
import * as SockJS from 'sockjs-client';

export class WebSocketAPI {
  stompClient: any;

  connectAndSend(request, successHandler, errorHandler) {
    let socket = new SockJS('http://localhost:8080/ws');
    const self = this;
    self.stompClient = Stomp.over(socket);
    self.stompClient.connect({}, function (frame) {
      self.send(request);

      console.log('Connected*: ' + frame);
      self.stompClient.subscribe('/topic/solutions', successHandler, errorHandler);
    });
  };

  disconnect() {
    if (this.stompClient !== null) {
      this.stompClient.disconnect();
    }
  }

  send(message) {
    this.stompClient.send('/app/solutions', {}, JSON.stringify(message));
  }
}
