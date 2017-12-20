pragma solidity ^0.4.18;

contract AttendanceTracker {
    
    struct Attendance {
        string studentId;
        uint attendedAt;
    }
    
    mapping(string => Attendance[]) sessionsAttendances;
    
    function recordAttendance(string sessionId, string studentId, uint attendedAt) public {
        sessionsAttendances[sessionId].push(Attendance(studentId, attendedAt));
    }
    
    function hasAttended(string sessionId, string studentId, uint timestamp, uint allowedDelta) public constant returns(bool) {
        Attendance[] memory sessionAttendances = sessionsAttendances[sessionId];
        
        if (sessionAttendances.length == 0) {
            return false;
        }
        
        for (uint i=0; i<sessionAttendances.length; i++) {
            if (keccak256(sessionAttendances[i].studentId) != keccak256(studentId))
                continue;
            
            int timestampsDelta = int(timestamp - sessionAttendances[i].attendedAt);
            if (abs(timestampsDelta) <= allowedDelta)
                return true;
        }
        
        return false;
    }
    
    function abs(int input) pure private returns (uint){ return uint( (input < 0) ? -input : input ); }
}