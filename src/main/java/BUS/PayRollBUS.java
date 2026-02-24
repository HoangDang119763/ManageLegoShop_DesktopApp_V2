package BUS;

import java.sql.Connection;
import java.sql.CallableStatement;
import java.sql.SQLException;
import DAL.ConnectApplication;

public class PayRollBUS {
    public boolean calculateMonthlySalary(int employeeId, String periodDate) {
        // periodDate format: "2026-02-01"
        String sql = "{CALL sp_CalculateMonthlyPayroll_V3(?, ?)}";
        try {
            Connection conn = ConnectApplication.getInstance().getConnectionFactory().newConnection();
            CallableStatement cstmt = conn.prepareCall(sql);
            
            cstmt.setInt(1, employeeId);
            cstmt.setString(2, periodDate);
            cstmt.execute();
            
            ConnectApplication.getInstance().getConnectionFactory().closeConnection(conn);
            return true;
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }
}
