pragma solidity ^0.4.19;

contract AttendanceTracker {

    // Maps sessionId => studentId[]
    mapping(string => string[]) private attendances;

    function recordAttendance(string sessionId, string studentId) public {
        attendances[sessionId].push(studentId);
    }

    function hasAttented(string sessionId, string studentId) public constant returns(bool) {
        string[] memory sessionAttendances = attendances[sessionId];

        if (sessionAttendances.length == 0) {
            return false;
        }

        for (uint i=0; i<sessionAttendances.length; i++) {
            if (keccak256(sessionAttendances[i]) == keccak256(studentId))
                return true;
        }

        return false;
    }
}