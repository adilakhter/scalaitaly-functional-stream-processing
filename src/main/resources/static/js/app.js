var phonecatApp = angular.module('phonecatApp', [
  'ngRoute',
  'mainApp'
]);

phonecatApp.config(['$routeProvider',
  function($routeProvider) {
    $routeProvider.
      when('/home', {
        templateUrl: '/twirl-home'  // a twirl template
      }).
      when('/websocket', {
        templateUrl: 'partials/websocket.html',
        controller: 'websocketController'
      }).
      when('/websocket-v2', {
        templateUrl: 'partials/websocket-v2.html',
        controller: 'websocketControllerV2'
      }).
      otherwise({
        redirectTo: '/home'
      });
  }]);
