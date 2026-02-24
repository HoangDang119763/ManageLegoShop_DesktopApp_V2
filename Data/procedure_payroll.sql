CREATE DEFINER=`root`@`localhost` PROCEDURE `sp_CalculateMonthlyPayroll_V3`(
    IN p_employee_id INT,
    IN p_salary_period DATE -- Truyền vào ngày đầu tháng, ví dụ '2026-02-01'
)
BEGIN
    -- Khai báo biến
    DECLARE v_base_salary DECIMAL(15,2) DEFAULT 0;
    DECLARE v_daily_rate DECIMAL(15,2) DEFAULT 0;
    DECLARE v_allowance DECIMAL(15,2) DEFAULT 0;
    DECLARE v_bonus_fine DECIMAL(15,2) DEFAULT 0; -- Tiền từ bảng fine (thưởng/phạt trực tiếp)
    DECLARE v_leave_fine DECIMAL(15,2) DEFAULT 0; -- Tiền phạt từ leave_request
    DECLARE v_insurance_deduction DECIMAL(15,2) DEFAULT 0;
    DECLARE v_ot_hours DECIMAL(10,2) DEFAULT 0;
    DECLARE v_ot_days DECIMAL(10,4) DEFAULT 0;
    DECLARE v_ot_amount DECIMAL(15,2) DEFAULT 0;
    DECLARE v_tax_amount DECIMAL(15,2) DEFAULT 0;
    DECLARE v_taxable_income DECIMAL(15,2) DEFAULT 0;
    DECLARE v_net_salary DECIMAL(15,2) DEFAULT 0;
    DECLARE v_is_tax INT DEFAULT 0;

    -- 1. Lấy Lương cơ bản và kiểm tra Thuế TNCN
    SELECT (s.base * s.coefficient), e.is_personal_income_tax 
    INTO v_base_salary, v_is_tax
    FROM employee e
    JOIN role r ON e.role_id = r.id
    JOIN salary s ON r.salary_id = s.id
    WHERE e.id = p_employee_id;

    SET v_daily_rate = v_base_salary / 26;

    -- 2. Lấy Phụ cấp
    SELECT IFNULL(SUM(transportation_support + accommodation_support + attendance_bonus), 0) 
    INTO v_allowance
    FROM allowance
    WHERE employee_id = p_employee_id AND salary_period = p_salary_period;

    -- 3. Lấy Thưởng/Phạt từ bảng Fine
    SELECT IFNULL(SUM(amount), 0) INTO v_bonus_fine
    FROM fine
    WHERE employee_id = p_employee_id 
    AND MONTH(created_at) = MONTH(p_salary_period) 
    AND YEAR(created_at) = YEAR(p_salary_period);

    -- 4. TÍNH PHẠT NGHỈ PHÉP (MỚI)
    -- Quét bảng leave_request, kết nối leave_type để lấy fine_amount
    -- Điều kiện: status_id = 20 và nằm trong tháng tính lương
    SELECT IFNULL(SUM(lt.fine_amount), 0) INTO v_leave_fine
    FROM leave_request lr
    JOIN leave_type lt ON lr.leave_type_id = lt.id
    WHERE lr.employee_id = p_employee_id 
    AND lr.status_id = 20
    AND (MONTH(lr.start_date) = MONTH(p_salary_period) AND YEAR(lr.start_date) = YEAR(p_salary_period));

    -- 5. Lấy Bảo hiểm
    -- Đoạn điều chỉnh trong Store Procedure
-- Kiểm tra nếu giá trị khác 0 thì mới cộng vào tiền khấu trừ
SELECT 
    (IF(health_insurance <> 0, health_insurance, 0) + 
     IF(social_insurance <> 0, social_insurance, 0) + 
     IF(unemployment_insurance <> 0, unemployment_insurance, 0))
INTO v_insurance_deduction
FROM deduction
WHERE employee_id = p_employee_id AND salary_period = p_salary_period;

    -- 6. Tính tiền OT (12h = 0.5 ngày, nhân hệ số 1.5 lương cơ bản)
    SELECT IFNULL(SUM(ot_hours), 0) INTO v_ot_hours
    FROM time_sheet
    WHERE employee_id = p_employee_id 
    AND MONTH(check_in) = MONTH(p_salary_period)
    AND YEAR(check_in) = YEAR(p_salary_period);
    
    SET v_ot_days = v_ot_hours / 24;
    SET v_ot_amount = v_ot_days * v_daily_rate * 1.5;

    -- 7. Tính tổng thu nhập chịu thuế
    -- Công thức: Lương CB + Phụ cấp + OT + Thưởng/Phạt Fine - Phạt Nghỉ Phép - Bảo hiểm
    SET v_taxable_income = (v_base_salary + v_allowance + v_ot_amount + v_bonus_fine) 
                           - v_leave_fine 
                           - v_insurance_deduction;

    -- 8. Tính thuế TNCN (10% nếu có tick chọn)
    IF v_is_tax = 1 AND v_taxable_income > 0 THEN
        SET v_tax_amount = v_taxable_income * 0.1;
    ELSE
        SET v_tax_amount = 0;
    END IF;

    -- 9. Lương thực lĩnh
    SET v_net_salary = v_taxable_income - v_tax_amount;

    -- 10. Lưu hoặc cập nhật vào bảng payroll_history
    -- Ở đây mình cộng dồn tất cả các khoản trừ (Fine + Leave_fine + Insurance + Tax) vào cột total_deduction
    INSERT INTO payroll_history (
        employee_id, salary_period, temporary_salary, overtime_amount, 
        total_allowance, total_fine, total_deduction, net_salary, paid_date
    ) VALUES (
        p_employee_id, p_salary_period, v_base_salary, v_ot_amount, 
        v_allowance, (v_bonus_fine - v_leave_fine), (v_insurance_deduction + v_tax_amount), v_net_salary, NOW()
    )
    ON DUPLICATE KEY UPDATE 
        temporary_salary = VALUES(temporary_salary),
        overtime_amount = VALUES(overtime_amount),
        total_allowance = VALUES(total_allowance),
        total_fine = VALUES(total_fine),
        total_deduction = VALUES(total_deduction),
        net_salary = VALUES(net_salary),
        paid_date = NOW();

    -- Trả về kết quả hiển thị
    SELECT 
        v_base_salary AS 'Lương CB',
        v_ot_amount AS 'Tiền OT (1.5)',
        v_allowance AS 'Phụ cấp',
        v_bonus_fine AS 'Thưởng/Phạt (Fine)',
        v_leave_fine AS 'Phạt nghỉ phép',
        v_tax_amount AS 'Thuế TNCN',
        v_net_salary AS 'Thực lĩnh cuối cùng'
    FROM DUAL;

END;