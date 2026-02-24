IMPLEMENTATION PLAN - HR Management Tabs Refactoring
=======================================================

COMPLETED:
✅ 1. Added new HR permissions to PermissionKey ENUM
✅ 2. Created hr_permissions.sql with permission inserts and role grants
✅ 3. Updated DeductionTabController to load insurance codes from employee table
   - Auto-displays participation status (mã: code ✓ or Chưa tham gia)
   - Shows insurance codes in tooltip when hovering over employee name

REMAINING TASKS:

1. DATABASE PERMISSIONS
   - Run: mysql -u root -p java_sql < hr_permissions.sql
   - This adds 8 new permissions and grants them to roles

2. STANDALONE HR TABS (Outside EmployeeInfoUI)
   
   A. LeaveRequestListController + LeaveRequestListUI.fxml
      - Employee dropdown selector (ComboBox with search)
      - Displays all leave requests for selected employee
      - Create/Approve/Reject buttons with permission checks
      - Status: EMPLOYEE_LEAVE_REQUEST_MANAGE for approvals
      
   B. FineListController + FineListUI.fxml  
      - Employee dropdown selector
      - Displays all fines/rewards for selected employee
      - Add/Edit/Delete buttons with permission checks
      - Status: EMPLOYEE_FINE_REWARD_MANAGE
      
   C. AttendanceListController + AttendanceListUI.fxml
      - Employee dropdown selector
      - Month/Date range selector
      - Display timesheet records WITH OT_HOURS column
      - Calculate total hours and OT hours
      - Status: EMPLOYEE_ATTENDANCE_MANAGE

3. UPDATE PAYROLL TAB
   - Add: Role/Position ComboBox
   - Add: Update Position button
   - Logic: When role changes -> auto-update salary from role.salary_id
   - Requires: EmployeeBUS.updateRole(employeeId, roleId) method

4. UPDATE ATTENDANCE TAB
   - Add OT_HOURS column display
   - Add total OT hours calculation in summary
   - Already in timesheet table, just need to display

5. PERMISSION CHECKS
   Apply to each tab:
   - Check if user has required permission
   - If not: Disable edit/delete buttons or show error
   - For employees: Can only view own data (employeeId == currentEmployeeId)
   - For managers: EMPLOYEE_FINE_REWARD_MANAGE allows full access

6. MAIN MENU INTEGRATION
   - Add these new tabs to main dashboard/menu
   - Only show if user has required permissions
   - Link from EmployeeUI to standalone tabs for quick access

ARCHITECTURE NOTES:

Employee Dropdown Pattern:
- ComboBox<EmployeeDTO> with custom toString() for display
- Load all active employees on controller initialize
- Add filter/search using FilteredList
- Selection triggers data reload

Permission Checking Pattern:
- Use SessionManagerService.hasPermission(PermissionKey key)
- Check at controller initialize() and on button actions
- Disable buttons if no permission (setDisable(true))
- Show tooltip explaining why button is disabled

SQL Changes:
- Insurance codes stored in employee table (health_ins_code, social_insurance_code, etc.)
- If code = '0' or empty -> not participating
- If code has value -> participating (auto-loaded and displayed)
- Deduction amounts stored in deduction table per salary_period

Next Steps:
1. Run hr_permissions.sql to update database
2. Implement standalone tab controllers one by one
3. Add permission checks throughout
4. Update menu to include new tabs
5. Test permission-based access for different roles
