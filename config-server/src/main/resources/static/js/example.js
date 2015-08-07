angular.module('config-server', ['ui.bootstrap']);

angular.module('config-server').controller('ApplicationCtrl', function ($scope, $http) {

    $http.get("/data/").success(function (data) {

    }).error(function (data, status, header, config) {

    });

    $scope.apps = [
        {
            name: "Gavin"
        }, {
            name: "Acct"
        }, {
            name: "CardBase"
        }
    ];

    $scope.active = "Gavin";

    $scope.clickApp = function (index) {
        if ($scope.apps[index]) {
            $scope.active = $scope.apps[index].name;
        }

        // todo load versions
    };

});

angular.module('config-server').controller('VersionCtrl', function ($scope, $http) {

    $http.get('http://localhost:8080/data/springcontent.json').
        success(function (data) {
            $scope.user = data;
        });

    $scope.versions = [
        {
            name: "1.0"
        }, {
            name: "2.0"
        }, {
            name: "3.0"
        }
    ];

    $scope.active = "1.0";

    $scope.clickVersion = function (index) {
        if ($scope.versions[index]) {
            $scope.active = $scope.versions[index].name;
        }

        // todo load versions
    };

});

angular.module('config-server').controller('GroupCtrl', function ($scope) {

    $scope.groups = [
        {
            name: "database",
            active: "active"
        }, {
            name: "runtime",
            active: ""
        }, {
            name: "constant",
            active: ""
        }
    ];

    $scope.active = 0;

    $scope.clickGroup = function (index) {
        if ($scope.groups[index]) {
            if ($scope.groups[$scope.active]) {
                $scope.groups[$scope.active].active = "";
            }
            $scope.groups[index].active = "active";
            $scope.active = index;

            // todo load groups
        }
    };
});

angular.module('config-server').controller('PropCtrl', function ($scope) {

    $scope.props = [
        {
            name: "driver",
            value: "oracle.jdbc.driver.OracleDriver",
            description: "连接驱动"
        }, {
            name: "url",
            value: "jdbc:oracle:thin:@172.21.4.253:1521:ofdb",
            description: "连接url"
        }, {
            name: "username",
            value: "user",
            description: "用户"
        }, {
            name: "password",
            value: "password",
            description: "密码"
        }
    ];

    $scope.groupName = "database";

    $scope.preUpdate = function (index) {
        if ($scope.props[index]) {
            alert($scope.props[index].name);
        }
    };

    $scope.delete = function (index) {

        if ($scope.props[index]) {
            alert($scope.props[index].name);
        }
    };

    $scope.create = function () {
    }
});